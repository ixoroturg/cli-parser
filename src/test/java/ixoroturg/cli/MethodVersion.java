package ixoroturg.cli;

import ixoroturg.cli.annotation.Version;

public class MethodVersion {

	@Version
	public static String version(){
		return  "my version from method";
	}
	public static void main(String[] args) {
		Parser.parse(MethodVersion.class, "--version");
	}
}
