package ixoroturg.cli.spi;

import java.util.*;
import java.util.stream.Stream;
import java.lang.reflect.*;

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
		Pointer<Object> lastArg = new Pointer<>();
		Pointer<Integer> pos = new Pointer<>(1);
		Field[] fields = clazz.getDeclaredFields();
		Method[] methods = clazz.getDeclaredMethods();

		AnnotatedElement[] members = Stream.concat(Arrays.stream((AnnotatedElement[])fields), Arrays.stream((AnnotatedElement[])methods)).toArray(AnnotatedElement[]::new);

		// Pointer<Object> instance = new Pointer<>();
		Object instance = null;
		try {
			instance= clazz.getConstructor().newInstance();
			// if(args.length > 0){
			// 	if(args[1].equals("--help")){
			// 		Arrays.stream(members).dropWhile(m -> {
			// 			return !m.isAnnotationPresent(Help.class);
			// 		})
			// 		.map(mem -> {
			// 			try {
			// 				if(mem instanceof Field f){
			// 					return (String) f.get(instance.value);
			// 				} else if (mem instanceof Method m){
			// 					return (String) m.invoke(instance.value);
			// 				}
			// 			} catch (Exception e) {
			// 				e.printStackTrace();
			// 				System.exit(-1);
			// 			}
			// 			return null;
			// 		})
			// 		.findFirst().orElse("There is no help for you");
			// 	}
			// }
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}



		// Arrays.stream(args)
		// 	.reduce(instance, (result, arg) -> {
		// 		if(arg.equals("--help")){
		// 			Arrays.stream(members).dropWhile(m -> {
		// 				if(lastArg.value == null){
		// 					return !m.isAnnotationPresent(Help.class);
		// 				}
		// 				// return !m.isAnnotationPresent(Help.class);
		//
		// 			})
		// 			.map(mem -> {
		// 				try {
		// 					if(mem instanceof Field f){
		// 						return (String) f.get(instance.value);
		// 					} else if (mem instanceof Method m){
		// 						return (String) m.invoke(instance.value);
		// 					}
		// 				} catch (Exception e) {
		// 					e.printStackTrace();
		// 					System.exit(-1);
		// 				}
		// 				return null;
		// 			})
		// 			.findFirst().orElse("There is no help for you");
		// 		}
		// 		if(pos.value == 1){
		// 			// if("--help".equals(arg)){
		// 			//
		// 			// }
		// 		}
		//
		//
		// 		pos.value++;
		// 		return result;
		// 	}, (a,b)->{return a;});
		
		Set<String> oneOfWas = new TreeSet<>();
		try{
			for(int i = 0; i < args.length; i++){
				String arg = args[i];
					if(arg.equals("--version")){
						String version = takeVersion(instance);
						System.out.println(version);
						System.exit(0);
					}
			}
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	private Object findByShortName(char name, Object instance){
		return null;
	}
	private Object findByLongName(String name, Object instance){
		return null;
	}
	private Object findByPosition(int i, Object instance){
		return null;
	}
	private Object findByAnnotation(Class annClazz, Class insClazz){
		return Stream.concat(Arrays.stream((AnnotatedElement[]) insClazz.getDeclaredFields()), Arrays.stream((AnnotatedElement[]) insClazz.getDeclaredMethods()))
			.dropWhile(mem -> {
				return !mem.isAnnotationPresent(annClazz);
			})
			.findFirst().orElse(null);
	}
	private String takeVersion(Object instance) throws Exception{
		Object mem = findByAnnotation(Version.class, instance.getClass());
		String version = "There is no version for you";
		if(mem != null){
			if(mem instanceof Field f){
				version = (String) f.get(instance);
			} else if (mem instanceof Method m){
				version = (String) m.invoke(instance);
			}
		}
		return version;
	}
}
