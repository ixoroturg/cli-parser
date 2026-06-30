package ixoroturg.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.*;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigorous Test :-)
     */

	final int format_input = 1, format_output = 2, input_file = 3, output_file = 4, TYPE = 5, EXT = 6;
    @Test
    public void shouldAnswerWithTrue() {
		Parser par = Parser.builder()
			.flag(format_input, 'f', "format-input", "Format", "Format input data")
			.flag(format_output, 'F', "format-output", "Format", "Format output data")

			.file(input_file, Path.of("def_input"), 'i', "input-file", "IO", "File for standard input")
			.file(output_file, Path.of("def_output"), 'o', "output-file","IO",  "File for standard output")

			.constantOption(TYPE, new String[]{"def_value","val1","val2"},"def_value", 't', "type", "OPTIONS", "Type of proccessing")
			.variableOption(EXT, "txt", 'e', "extension", "OPTIONS", "Extension for output file")
				.build();
		// par.parse(args);
			
		System.out.println("Опция формата ввода: "+par.isFlag(format_input));
		System.out.println("Опция формата вывода: "+par.isFlag(format_output));

		System.out.println("Опция файла ввода: "+par.file(input_file));
		System.out.println("Опция файла вывода: "+par.isFlag(output_file));

		System.out.println("Опция типа: "+par.opt(TYPE));
		System.out.println("Опция расширения: "+par.opt(EXT));
    }
}
