package ixoroturg.cli;

import java.util.ServiceLoader;

import ixoroturg.util.*;

public interface Parser {
	class ParserInfo {
		String className = null;
		ParserProvider provider = null;
	}
	static Pointer<ParserInfo> p = new Pointer<>(new ParserInfo());
	public static void setProvider(String className){
		p.value.className = className;
		
	}
	public static ParserProvider getProvider(){
		if(p.value.provider != null){
			return p.value.provider;
		}
		p.value.provider = ServiceLoader.load(ParserProvider.class)
			.stream()
			.dropWhile(prov -> {
				return !prov.type().getName().equals(p.value.className);
			})
			.map(ServiceLoader.Provider::get)
			.findFirst().orElse(new ixoroturg.cli.spi.IParserProvider());
		return p.value.provider;
	}
	public static void reloadProvider(){
		p.value.provider = null;
	}
	public static String getProviderClassName(){
		return p.value.className;
	}


	public static <T> T parse(Class<T> clazz, String... args){
		return getProvider().parse(clazz, args);
	}
}
