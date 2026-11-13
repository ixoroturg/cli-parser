package ixoroturg.cli;

import ixoroturg.cli.annotation.Version;

public class FieldVersion {

	@Version
	public static String version = "my version from field";
	public static void main(String[] args) {
		Parser.parse(FieldVersion.class, "--version");
	}
}
