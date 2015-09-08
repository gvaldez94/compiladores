package compiladores.lexer;

/**
 * 
 * @author gvaldez
 *
 */

public class Token {
	String compLex;
	String lexema;
	int id;
	int nroLinea;

	public Token(String compLex, String lexema, int id, int nroLinea) {
		this.compLex = compLex;
		this.lexema = lexema;
		this.id = id;
		this.nroLinea = nroLinea;
	}

	@Override
	public String toString() {
		return "" + compLex + "  ";
	}
}
