package ixoroturg.cli;

import java.util.Map;
import java.util.TreeMap;
import java.nio.file.Path;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;

public class Parser {
	
	private static final byte NO_VAL = 0, FLAG = 1, FILE = 2, VAR_OPT = 3, OPT = 4, NO_NAME = 6;
	
	Map<Integer, cliOption> options;
	String version;
	boolean noNameAsFiles = true;
	cliOption noName;
	String littleHelp = "type --help to show help";

	public void parse(String[] args){
	
		if(args.length == 0){
			return;
		}
		String helpTest = args[0];
		if("--version".equals(helpTest)){
			System.out.println();
			System.out.println(version);
			System.exit(0);
		}
		if("--help".equals(helpTest)){
			System.out.println();
			System.out.println(version);
			printHelp();
			System.out.println();
			System.exit(0);
		}
	
		cliOption opt;
		for(int i = 0; i < args.length; i++){

			if(args[i].charAt(0) != '-'){
				if(noNameAsFiles){
					noName.noNameFiles.add(Path.of(args[i]));
				}
					noName.noName.add(args[i]);
				continue;
			}
		
			if(args[i].startsWith("--")){
				String name = args[i].substring(2,args[i].length());
				opt = findByLongName(name);
				if(name == null){
					System.err.println("Unknown option: "+args[i]);
					System.err.println(littleHelp);
					throw new RuntimeException("Unknown option");
				}
				processOption(opt,args,i);
				if(opt.type != FLAG)
					i++;
				continue;
			}
			String opts = args[i].substring(1,args[i].length());
			for(int j = 0; j < opts.length(); j++){
				char o = opts.charAt(j);
				opt = findByShortName(o);
				processOption(opt, args, i);
				if(opt.type != FLAG){
					i++;
				}
			}
		}
		options.put(-1,noName);
	}




	public Path file(int id){
		return options.get(id).file;
	}
	public boolean isFlag(int id){
		return options.get(id).isPresent;
	}
	public String opt(int id){
		return options.get(id).defaultValue;
	}
	public List<Path> noNameFiles(){
		return noName.noNameFiles;
	}
	public List<String> noNameOpts(){
		return noName.noName;
	}
	

	private void printHelp(){
		Set<String> sections = new TreeSet<String>();
		options.forEach((k,v)->{
			sections.add(v.section);
		});
		System.out.println();
			System.out.println("-".repeat(noName.section.length()+4));
			System.out.println("| "+noName.section+" |");
			System.out.println("-".repeat(noName.section.length()+4));
		System.out.println();
		System.out.println("\t"+noName.description);

		for(String section: sections){
			System.out.println();
			System.out.println("-".repeat(section.length()+4));
			System.out.println("| "+section+" |");
			System.out.println("-".repeat(section.length()+4));
			System.out.println();
			
			options.forEach((k,v)->{
				if(v.section.equals(section)){
					System.out.println("\t" + "-"+v.shortName+", --"+v.longName+"\t\t"+v.description);
				}
			});
		}
	}
	

	private void processOption(cliOption opt, String[] args, int index){
		if(opt.type == FLAG){
			opt.isPresent = true;
			return;
		}
		if(index+1 >= args.length){
			System.err.println("Missing argument for: " + opt.shortName+" ("+opt.longName+")");
			System.err.println(littleHelp);
			throw new RuntimeException("Missing argument");
		}
		String nextArg = args[index+1];
		if(opt.type == OPT){
			boolean test = false;
			for(String valid: opt.validValues){
				if(valid.equals(nextArg)){
					test = true;
					break;
				}
			}
			if(!test){
				System.err.println("Wrong option \""+nextArg+"\" for: "+ opt.shortName + " ("+opt.longName+")");
				System.err.println(littleHelp);
				throw new RuntimeException("Wrong option");
			}
			opt.defaultValue = nextArg;
			return;
		}
		if(opt.type == VAR_OPT){
			opt.defaultValue = nextArg;
		}
		if(opt.type == FILE){
			opt.file = Path.of(nextArg);
		}
		
	}

	private cliOption findByShortName(char name){
		for(int key: options.keySet()){
			cliOption test = options.get(key);
			if(test.shortName == name)
				return test;
		}
		return null;
	}
	private cliOption findByLongName(String name){
		for(int key: options.keySet()){
			cliOption test = options.get(key);
			if(name.equals(test.longName))
				return test;
		}
		return null;
	}

	public static Builder builder(){
		return new Builder();
	}
	
	private static class cliOption {
		String section;
		char shortName;
		String longName;
		String description;
		String defaultValue;
		String[] validValues;
		Path file;
		// List<Path> files;
		List<String> noName;
		List<Path> noNameFiles;
		byte type;
		boolean isPresent = false;
		private cliOption(char shortName, String longName, String section, String description, byte type){
			this.section = section;
			this.shortName = shortName;
			this.longName = longName;
			this.description = description;
			this.type = type;
		}
	}
	
	public static class Builder {
		// Set<Integer> flags = new TreeSet<>();
		// Map<Integer, Path[]> files;
		// Map<Integer, String> variableOptions = new TreeMap<>();
		// Map<Integer, Integer> constantOptions;
		Map<Integer, cliOption> options = new TreeMap<>();
		String version = "Set version via cli-parser";
		boolean noNameAsFiles = true;
		cliOption noName;

		public Builder flag(int id, char shortName, String longName, String section, String description){
			// flags.add(id);
			cliOption opt = new cliOption(shortName,longName, section, description,FLAG);
			options.put(id, opt);
			return this;
		}
		public Builder noNameAsFiles(){
			noNameAsFiles = true;
			return this;
		}
		public Builder noNameAsString(){
			noNameAsFiles = false;
			return this;
		}
		public Builder variableOption(int id, String defaultValue, char shortName, String longName, String section, String description){
			cliOption opt = new cliOption(shortName,longName, section, description, VAR_OPT);
			opt.defaultValue =defaultValue;
			options.put(id,opt);
			return this;
		}
		public Builder constantOption(int id, String[] validOptions, String defaultValue,  char shortName, String longName, String section, String description){
			cliOption opt = new cliOption(shortName,longName, section, description, OPT);
			opt.validValues = validOptions;
			opt.defaultValue = defaultValue;
			options.put(id,opt);
			return this;
		}
		public Builder file(int id, Path defaultValue, char shortName, String longName, String section, String description){
			cliOption opt = new cliOption(shortName,longName, section, description, FILE);
			opt.file = defaultValue;
			options.put(id,opt);
			return this;
		}
		// public Builder files(int id, Path[] defaultValue, char shortName, String longName, String section, String description){
		// 	cliOption opt = new cliOption(shortName,longName, section, description, FILES);
		// 	for(Path p: defaultValue){
		// 		opt.files.add(p);
		// 	}
		// 	options.put(id,opt);
		// 	return this;
		// }
		public Builder version(String version){
			this.version = version;
			return this;
		}
		public Builder noName(String section, String description){
			cliOption opt = new cliOption(' ', null, section, description, NO_NAME);
			noName = opt;
			return this;
		}
		public Parser build(){
			Parser p = new Parser();
			p.options = options;
			p.noNameAsFiles = noNameAsFiles;
			p.noName = noName;
			p.version = version;
			return p;
		}
	}
}
