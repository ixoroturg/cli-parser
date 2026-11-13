package ixoroturg.cli.spi;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import com.sun.jdi.InvocationException;

import java.io.File;
import java.lang.reflect.*;
import java.nio.file.Path;

import ixoroturg.cli.annotation.*;
import ixoroturg.cli.ParserProvider;
import ixoroturg.util.Pointer;

public class IParserProvider implements ParserProvider{

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"ixoroturg"};
	}

	@Override
	public <T> T parse(Class<T> clazz, String... args) {

		// System.err.println("Принято: "+Arrays.toString(args));


		// Pointer<Object> lastArg = new Pointer<>();
		// Pointer<Integer> pos = new Pointer<>(1);
		// Field[] fields = clazz.getDeclaredFields();
		// Method[] methods = clazz.getDeclaredMethods();

		// AnnotatedElement[] members = Stream.concat(Arrays.stream((AnnotatedElement[])fields), Arrays.stream((AnnotatedElement[])methods)).toArray(AnnotatedElement[]::new);
		T instance = null;
		try {
			instance= clazz.getConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		Context<T> ctx = new Context<T>(instance, args);

		try{
			for(int i = 0; i < args.length; i++){
				String arg = args[i];
				if(arg.equals("--version")){
					String version = takeVersion(ctx.instance);
					if(version == null){
						version = "There is no version for you";
					}
					System.out.println(version);
					System.exit(0);
				}
				if("--".equals(arg)){
					if(i+1 < args.length){
						String[] rest = Arrays.copyOfRange(args, i+1, args.length);
						ctx.remaind.addAll(List.of(rest));
					}
				} else if (arg.startsWith("--")){
					int index = arg.indexOf("=");
					String name = null;
					if(index != -1){
						name = arg.substring(2,index);
					} else {
						name = arg.substring(2);
					}
					AnnotatedElement mem = findByLongName(name, ctx.clazz);
					if(index != -1 && arg.length() > index+1){
						name = arg.substring(index+1);
					} else {
						name = args[i+1];
						i++;
					}
					loadArg(ctx, mem, name);
				} else if (arg.startsWith("-")){
					if(arg.length() > 3){
							char name = arg.charAt(1);
							AnnotatedElement mem = findByShortName(name, ctx.clazz);
						if (arg.charAt(2) == '='){
							loadArg(ctx, mem, arg.substring(3));
						} else {
							if(isNumeric(mem)){
								try{
									long number = Long.parseLong(arg.substring(2));
									loadArg(ctx, mem, arg.substring(2));
								}catch(NumberFormatException e){
									i = processKeys(ctx, i);
								}
							}
						}
					} else {
						i = processKeys(ctx, i);
					}
				} else {
					AnnotatedElement mem = findByPosition(ctx, clazz);
					if(mem != null){
						loadArg(ctx, mem, arg);
					} else {
						ctx.remaind.add(arg);
					}
				}
			}
			processRemainder(ctx);
			postParse(ctx);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
		return (T) ctx.instance;
	}

	private void postParse(Context ctx) throws Exception{
		AnnotatedElement mem = findByAnnotation(AfterParse.class, ctx.clazz);
		if(mem instanceof Method m){
			m.invoke(ctx.instance);
		}
	}
	
	private void processRemainder(Context<?> ctx) throws Exception{
		if(ctx.remaind.size() == 0){
			return;
		}
		AnnotatedElement elem = findByAnnotation(Remainder.class, ctx.clazz);
		byte type = collectionType(elem);
		Class collectionClass = collectionClass(elem);
		Object[] remain = (Object[]) Array.newInstance(collectionClass, ctx.remaind.size());
		for(int i = 0; i < ctx.remaind.size(); i++){
			remain[i] = argToObject(collectionClass, ctx.remaind.get(i));
		}
		// remain = ctx.remaind.toArray(remain);
		
		// String[] remain = ctx.remaind.toArray((Object[])Array.newInstance(collectionClass, ctx.remaind.size()));
		if(type == ARRAY){
			if(elem instanceof Field f){
				f.set(ctx.instance, (Object) remain);
			} else if (elem instanceof Method m){
				m.invoke(ctx.instance, (Object) remain);
			}
			// loadArg(ctx, elem, ctx.remaind.toArray(String[]::new));
		} else if(elem instanceof Field f){
			ctx.remaind.forEach(s -> {
				try{
					Object obj = argToObject(collectionClass, s);
					switch(type){
						case LIST -> {
							Object list = f.get(ctx.instance);
							if(list == null){
								f.set(ctx.instance, List.of(remain));
								return;
							}
							Method mt = list.getClass().getDeclaredMethod("add", String.class);
							mt.invoke(list,obj);
						}
						case SET -> {
							Object set = f.get(ctx.instance);
							if(set == null){
								f.set(ctx.instance, Set.of(remain));
								return;
							}
							Method mt = set.getClass().getDeclaredMethod("add", String.class);
							mt.invoke(set,obj);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					System.exit(-1);
				}
			});
		} else if (elem instanceof Method m){
			switch(type){
				case LIST -> {
					List l = List.of(remain);
					m.invoke(ctx.instance, l);
				}
				case SET -> {
					Set s = Set.of(remain);
					m.invoke(ctx.instance, s);
				}
			}
		}
	}

	private int processKeys(Context ctx, int i){
		String arg = ctx.args[i];
		String keys = arg.substring(1);
		for(int j = 0; j < keys.length(); j++){
			char ch = keys.charAt(j);
			AnnotatedElement mem = findByShortName(ch, ctx.clazz);
			i++;
			if(i < ctx.args.length){
				String pass = ctx.args[i];
				loadArg(ctx, mem, pass);
				// ctx.last = mem;
			}
		}
		return i;
	}

	private void loadArg(Context ctx, AnnotatedElement mem, String arg){
		try{
			String argName = getName(mem);
			if(mem.isAnnotationPresent(OneOf.class)){
				OneOf ann = mem.getAnnotation(OneOf.class);
				if(!ctx.oneOfWas.containsKey(ann.value())){
					ctx.oneOfWas.put(ann.value(), argName);
				} else {
					System.err.println(argName+" is not applicable with "+ctx.oneOfWas.get(ann.value()));
					System.exit(-1);
				}
				
			}
			if(mem.isAnnotationPresent(Allow.class)){
				Allow all = mem.getAnnotation(Allow.class);
				boolean allow = false;
				for(int i = 0; i < all.value().length; i++){
					if(arg.equals(all.value()[i])){
						allow = true;
						break;
					}
				}
				if(!allow){
					System.err.println("For parameter "+getName(mem)+" given value is not allowed: "+arg+"\nOnly: "+Arrays.toString(all.value()));
					System.exit(-1);
				}
				if(isNumeric(mem)){
					long number = Long.parseLong(arg);
					
					if(mem.isAnnotationPresent(Min.class)){
						Min ann = mem.getAnnotation(Min.class);
						long min = ann.value();
						if(number < min){
							System.err.println("For parameter "+getName(mem)+" minimal value is "+min);
							System.exit(-1);
						}
					}
					if(mem.isAnnotationPresent(Max.class)){
						Max ann = mem.getAnnotation(Max.class);
						long max = ann.value();
						if(number < max){
							System.err.println("For parameter "+getName(mem)+" maximum value is "+max);
							System.exit(-1);
						}
					}
				}
			}
			applyArg(ctx, mem, arg);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	private static String getName(Object mem){
		if(mem instanceof Field f){
			return f.getName();
		} else if (mem instanceof Method m){
			return m.getName();
		}
		return null;
	}

	private void applyArg(Context ctx, AnnotatedElement mem, String arg) throws IllegalAccessException, Exception {
		Class type = getType(mem);
		String s = arg;
		if(mem instanceof Field f){
			String argName = f.getName();
			if(type == File.class){
				f.set(ctx.instance,new File(s));
			}
			if(type == Path.class){
				f.set(ctx.instance, Path.of(s));
			}
			if(type == String.class){
				f.set(ctx.instance, s);
			}
			if(type == char.class && s.length() > 0){
				f.setChar(ctx.instance, s.charAt(0));
			}
			try{
				if(type == byte.class){
					f.setByte(ctx.instance, Byte.parseByte(s));
				}
				if(type == short.class){
					f.setShort(ctx.instance, Short.parseShort(s));
				}
				if(type == int.class){
					f.setInt(ctx.instance, Integer.parseInt(s));
				}
				if(type == long.class){
					f.setLong(ctx.instance, Long.parseLong(s));
				}
				if(type == float.class){
					f.setFloat(ctx.instance, Float.parseFloat(s));
				}
				if(type == double.class){
					f.setDouble(ctx.instance, Double.parseDouble(s));
				}
			}catch(Exception e){
				System.err.println("Expected numeric value for: "+argName);
			}
			try{
				if(type == boolean.class){
					if(s != null){
						f.set(ctx.instance, Boolean.parseBoolean(s));
					} else {
						if(mem.isAnnotationPresent(Disable.class)){
							f.setBoolean(ctx.instance, false);
						} else {
							f.setBoolean(ctx.instance, true);
						}
					}
				}
			}catch(Exception e){
				System.err.println("Expected boolean value for: "+argName);
			}
		} else if(mem instanceof Method m) {

			String argName = m.getName();
			if(type == File.class){
				m.invoke(ctx.instance,new File(s));
			}
			if(type == Path.class){
				m.invoke(ctx.instance, Path.of(s));
			}
			if(type == String.class){
				m.invoke(ctx.instance, s);
			}
			if(type == char.class && s.length() > 0){
				m.invoke(ctx.instance, s.charAt(0));
			}
			try{
				if(type == byte.class){
					m.invoke(ctx.instance, Byte.parseByte(s));
				}
				if(type == short.class){
					m.invoke(ctx.instance, Short.parseShort(s));
				}
				if(type == int.class){
					m.invoke(ctx.instance, Integer.parseInt(s));
				}
				if(type == long.class){
					m.invoke(ctx.instance, Long.parseLong(s));
				}
				if(type == float.class){
					m.invoke(ctx.instance, Float.parseFloat(s));
				}
				if(type == double.class){
					m.invoke(ctx.instance, Double.parseDouble(s));
				}
			}catch(Exception e){
				System.err.println("Expected numeric value for: "+argName);
			}
			try{
				if(type == boolean.class){
					if(s != null){
						m.invoke(ctx.instance, Boolean.parseBoolean(s));
					} else {
						if(mem.isAnnotationPresent(Disable.class)){
							m.invoke(ctx.instance, false);
						} else {
							m.invoke(ctx.instance, true);
						}
					}
				}
			}catch(Exception e){
				System.err.println("Expected boolean value for: "+argName);
			}
		}
		String name = getName(mem);
		ctx.required.remove(name);
		ctx.last = mem;
	}

	private Class getType(Object mem){
		Class type = null;
		if(mem instanceof Field f){
			type = f.getType();
		} else if (mem instanceof Method m) {
			Parameter[] params = m.getParameters();
			if(params.length == 1){
				type = params[0].getClass();
			}
		}
		return type;
	}
	final byte LIST = 0, SET = 1, QUEUE = 2, DEQUE = 3, ARRAY = 4;
	private byte collectionType(Object mem){
		byte result = ARRAY;
		Class type = getType(mem);
		if(List.class.isAssignableFrom(type)){
			result = LIST;
		} else if(Set.class.isAssignableFrom(type)) {
			result = SET;
		} else if (Queue.class.isAssignableFrom(type)) {
			result = QUEUE;
		} else if (Deque.class.isAssignableFrom(type)){
			result = DEQUE;
		}
		return result;
	}

	private Class collectionClass(Object mem){
		if(collectionType(mem) != ARRAY){
			if(mem instanceof Field f){
				return f.getGenericType().getClass();
			} else if (mem instanceof Method m) {
				Parameter[] params = m.getParameters();
				if(params.length == 1){
					return params[0].getParameterizedType().getClass();
				}
			}
		}
		return String.class;
	}

	private boolean isNumeric(Object mem){
		Class type = getType(mem);
		if(type == byte.class || type == short.class || type == int.class || type == long.class || type == float.class || type == double.class ||
				Number.class.isAssignableFrom(type)){
			return true;
		}
		return false;
	}

	private Object argToObject(Class type, String arg){
		String s = arg;
		if(type == File.class){
			return new File(s);
		}
		if(type == Path.class){
			return Path.of(s);
		}
		try{
			if(type == byte.class){
				return Byte.parseByte(s);
			}
			if(type == short.class){
				return Short.parseShort(s);
			}
			if(type == int.class){
				return Integer.parseInt(s);
			}
			if(type == long.class){
				return Long.parseLong(s);
			}
			if(type == float.class){
				return Float.parseFloat(s);
			}
			if(type == double.class){
				return Double.parseDouble(s);
			}
		}catch(Exception e){
			System.err.println("Expected numeric value");
		}
		try{
			if(type == boolean.class){
				return Boolean.parseBoolean(s);
			}
		}catch(Exception e){
			System.err.println("Expected boolean value");
		}
		return null;
	}

	private Object[] loadRemainderArray(String[] rest, Class type1, AnnotatedElement arg){
		if(type1 == String.class){
			return rest;
		}
		byte col = ARRAY;
		if(List.class.isAssignableFrom(type1)){
			col = LIST;
		} else if(Set.class.isAssignableFrom(type1)) {
			col = SET;
		} else if (Queue.class.isAssignableFrom(type1)) {
			col = QUEUE;
		} else if (Deque.class.isAssignableFrom(type1)){
			col = DEQUE;
		}
		if(col != ARRAY){
			if(arg instanceof Field f){
				type1 = f.getGenericType().getClass();
			} else if (arg instanceof Method m){
				Parameter[] params = m.getParameters();
				if(params.length == 1){
					type1 = params[0].getParameterizedType().getClass();
				}
			}
		}
		
		// byte col1 = col;
		// Object[] result = (Object[]) Array.newInstance(type, rest.length);
		Class type = type1;
		return Arrays.stream(rest)
			.map(s -> {
				if(type == File.class){
					return new File(s);
				}
				if(type == Path.class){
					return Path.of(s);
				}
				try{
					if(type == byte.class){
						return Byte.parseByte(s);
					}
					if(type == short.class){
						return Short.parseShort(s);
					}
					if(type == int.class){
						return Integer.parseInt(s);
					}
					if(type == long.class){
						return Long.parseLong(s);
					}
					if(type == float.class){
						return Float.parseFloat(s);
					}
					if(type == double.class){
						return Double.parseDouble(s);
					}
				}catch(Exception e){
					System.err.println("Expected numeric value");
				}
				try{
					if(type == boolean.class){
						return Boolean.parseBoolean(s);
					}
				}catch(Exception e){
					System.err.println("Expected boolean value");
				}
				return null;
			}).toArray();
	}
	

	private static class Context<T>{
		Class clazz;
		T instance;
		String[] args = null;
		Map<String, String> oneOfWas = new TreeMap<>();
		List<String> remaind = new ArrayList<String>();
		// String currentFlags = null;
		Object last = null;
		int position = 1;
		List<String> required = new ArrayList<>();
		// int pointer = 0;
		Context(T instance, String... args){
			this.instance = instance;
			clazz = instance.getClass();
			this.args = args;
			required = Stream.concat(Arrays.stream((AnnotatedElement[]) clazz.getDeclaredFields()), Arrays.stream((AnnotatedElement[]) clazz.getDeclaredMethods()))
				.filter(elem -> {
					return elem.isAnnotationPresent(Required.class);
				})
				.map(elem -> {
					String type = getName(elem);
					return type;
				})
				.toList();
		}
	}

	private AnnotatedElement findByShortName(char name, Class insClazz){
		AnnotatedElement result = Stream.concat(Arrays.stream((AnnotatedElement[]) insClazz.getDeclaredFields()), Arrays.stream((AnnotatedElement[]) insClazz.getDeclaredMethods()))
			.filter(mem -> {
				return mem.isAnnotationPresent(Key.class);
			})
			.filter(mem -> {
				Key ann = mem.getAnnotation(Key.class);
				return ann.value() == name;
			})
			.findFirst().orElse(null);
		return result;
	}
	private AnnotatedElement findByLongName(String name, Class insClazz){
		AnnotatedElement result = Stream.concat(Arrays.stream((AnnotatedElement[]) insClazz.getDeclaredFields()), Arrays.stream((AnnotatedElement[]) insClazz.getDeclaredMethods()))
			.filter(mem -> {
				return mem.isAnnotationPresent(LongName.class);
			})
			.filter(mem -> {
				LongName ann = mem.getAnnotation(LongName.class);
				return name.equals(ann.value());
			})
			.findFirst().orElse(null);
		return result;
	}
	private AnnotatedElement findByPosition(Context ctx, Class insClazz){
		int i = ctx.position;
		AnnotatedElement result = Stream.concat(Arrays.stream((AnnotatedElement[]) insClazz.getDeclaredFields()), Arrays.stream((AnnotatedElement[]) insClazz.getDeclaredMethods()))
			.filter(mem -> {
				return mem.isAnnotationPresent(Position.class);
			})
			.filter(mem -> {
				Position ann = mem.getAnnotation(Position.class);
				return ann.value() == i;
			})
			.findFirst().orElse(null);
		ctx.position++;
		return result;
	}
	private AnnotatedElement findByAnnotation(Class annClazz, Class insClazz){
		AnnotatedElement result = Stream.concat(Arrays.stream((AnnotatedElement[]) insClazz.getDeclaredFields()), Arrays.stream((AnnotatedElement[]) insClazz.getDeclaredMethods()))
			.dropWhile(mem -> {
				return !mem.isAnnotationPresent(annClazz);
			})
			.findFirst().orElse(null);
		return result;
	}
	private String takeVersion(Object instance) throws Exception{

		// System.err.println("Поиск: "+instance.getClass());
		Object mem = findByAnnotation(Version.class, instance.getClass());
		String version = null;
		if(mem != null){
			// System.err.println("Найдено: "+mem.getClass());
			if(mem instanceof Field f){
				// System.err.println("Поле: "+f.getName());
				version = (String) f.get(instance);
			} else if (mem instanceof Method m){
				// System.err.println("Метод: "+m.getName());
				version = (String) m.invoke(instance);
			}
		}
		return version;
	}
}
