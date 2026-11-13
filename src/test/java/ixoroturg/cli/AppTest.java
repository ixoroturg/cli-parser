package ixoroturg.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

	public static void main(String[] args){
		CommandClass cmd = Parser.parse(CommandClass.class, "--version");
		// shouldAnswerWithTrue();
	}
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()  throws Exception {
		// ProcessBuilder proc = new ProcessBuilder("java", "-jar", "target/CLI-Parser.jar", "--version");
		ProcessBuilder proc = new ProcessBuilder("java", "-cp", "target/classes:target/test-classes:/home/ixoroturg/.m2/repository/ru/ixoroturg/maven/util/1.0-SNAPSHOT/util-1.0-SNAPSHOT.jar", "ixoroturg.cli.AppTest");

		proc.start().inputReader().lines().forEach(System.out::println);
    }
}
