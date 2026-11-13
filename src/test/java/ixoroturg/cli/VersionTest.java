package ixoroturg.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class VersionTest {

	// public static void main(String[] args){
	// 	CommandClass cmd = Parser.parse(CommandClass.class, "--version");
	// 	// shouldAnswerWithTrue();
	// }
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()  throws Exception {
		// ProcessBuilder proc = new ProcessBuilder("java", "-jar", "target/CLI-Parser.jar", "--version");
		ProcessBuilder proc = new ProcessBuilder("java", "-cp", "target/classes:target/test-classes:/home/ixoroturg/.m2/repository/ru/ixoroturg/maven/util/1.0-SNAPSHOT/util-1.0-SNAPSHOT.jar", "ixoroturg.cli.EmptyVersion");
		String emptyVersion = new String(proc.start().getInputStream().readAllBytes());
		assertTrue("There is no version for you\n".equals(emptyVersion), "Given:\n"+emptyVersion);

		proc.command("java", "-cp", "target/classes:target/test-classes:/home/ixoroturg/.m2/repository/ru/ixoroturg/maven/util/1.0-SNAPSHOT/util-1.0-SNAPSHOT.jar", "ixoroturg.cli.FieldVersion");
		emptyVersion = new String(proc.start().getInputStream().readAllBytes());
		assertTrue((FieldVersion.version+"\n").equals(emptyVersion), "Given:\n"+emptyVersion);

		proc.command("java", "-cp", "target/classes:target/test-classes:/home/ixoroturg/.m2/repository/ru/ixoroturg/maven/util/1.0-SNAPSHOT/util-1.0-SNAPSHOT.jar", "ixoroturg.cli.MethodVersion");
		emptyVersion = new String(proc.start().getInputStream().readAllBytes());
		assertTrue((MethodVersion.version()+"\n").equals(emptyVersion));
    }
}
