package ixoroturg.cli;

import java.nio.file.*;
/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
		shouldAnswerWithTrue(args);
    }
	final static int format_input = 1, format_output = 2, input_file = 3, output_file = 4, TYPE = 5, EXT = 6;
    public static void shouldAnswerWithTrue(String[] args) {
		Parser par = Parser.builder()
			.flag(format_input, 'f', "format-input", "Format", "Format input data")
			.flag(format_output, 'F', "format-output", "Format", "Format output data")

			.file(input_file, Path.of("def_input"), 'i', "input-file", "IO", "File for standard input")
			.file(output_file, Path.of("def_output"), 'o', "output-file","IO",  "File for standard output")

			.constantOption(TYPE, new String[]{"def_value","val1","val2"},"def_value", 't', "type", "OPTIONS", "Type of proccessing")
			.variableOption(EXT, "txt", 'e', "extension", "OPTIONS", "Extension for output file")

			.version("ixoroturg cli parser 1.0.0\nFor easy cli argument parsing")
			.noName("Input", "Файлы для преобразования")
				.build();
		par.parse(args);
			
		System.out.println("Опция формата ввода: "+par.isFlag(format_input));
		System.out.println("Опция формата вывода: "+par.isFlag(format_output));

		System.out.println("Опция файла ввода: "+par.file(input_file));
		System.out.println("Опция файла вывода: "+par.file(output_file));

		System.out.println("Опция типа: "+par.opt(TYPE));
		System.out.println("Опция расширения: "+par.opt(EXT));
    }
}
