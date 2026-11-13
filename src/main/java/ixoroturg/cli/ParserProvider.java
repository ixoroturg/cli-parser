package ixoroturg.cli;

public interface ParserProvider {
	public String getVersion();
	public String[] getAuthors();

	public <T> T parse(Class<T> clazz, String... args);
}
