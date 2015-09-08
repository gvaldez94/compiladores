package compiladores.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * 
 * @author gvaldez
 *
 */

/* clase utilizada para leer caracter a caracter
 * y poder devolver un caracter al input
 */
public class Buffer extends BufferedReader {
	public Buffer(Reader in) {
		super(in);
	}

	public char getchar() throws IOException {
		mark(1);
		return (char) this.read();
	}

	public void ungetchar() throws IOException {
		reset();
	}
}
