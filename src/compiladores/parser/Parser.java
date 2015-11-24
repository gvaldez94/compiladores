package compiladores.parser;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import compiladores.lexer.Lexer;
import compiladores.lexer.Token;

/**
 * 
 * @author gvaldez
 *
 */

public class Parser {
	static boolean aceptar = true;
	static Token token = new Token(null, null, -1, -1);

	// codigos
	public static final int L_CORCHETE = 256;
	public static final int R_CORCHETE = 257;
	public static final int L_LLAVE = 258;
	public static final int R_LLAVE = 259;
	public static final int COMA = 260;
	public static final int DOS_PUNTOS = 261;
	public static final int LITERAL_CADENA = 262;
	public static final int LITERAL_NUM = 263;
	public static final int PR_TRUE = 264;
	public static final int PR_FALSE = 265;
	public static final int PR_NULL = 266;
	public static final int EOF = 267;
	public static final int ERROR = -1;

	public static void main(String[] args) {
		Lexer.iniciarLexer();
		Lexer.setPrList();

		getToken();
		element(new int[] { EOF });
		Lexer.cerrarArchivo();
		if (aceptar)
			System.out.println("Sin errores sintacticos");
	}

	static void getToken() {
		do {
			try {
				Lexer.getToken();
				token = Lexer.token;
			} catch (IOException ex) {
				Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
			}
		} while (token.id == ERROR);
	}

	static void match(int tokenEsperado) {
		if (token.id == tokenEsperado) {
			getToken();
		} else {
			error();
		}
	}

	static void error() {
		System.out.println("Error de sintaxis en linea " + Lexer.nroLinea + " no se esperaba " + token);
		aceptar = false;
		if (token.id == EOF)// esto evita que se produzcan llamadas recursivas
							// infinitas en ciertos casos de error
			System.exit(0);
	}

	static void checkinput(int[] firsts, int[] follows) {
		if (!(in(firsts))) {
			error();
			scanto(union(firsts, follows));
		}
	}

	/*
	 * funcion union devuelve un array de tokens que contiene todos los tokens
	 * del conjunto1 y el conjunto2
	 */
	static int[] union(int[] conjunto1, int[] conjunto2) {
		int[] conjunto3 = new int[conjunto1.length + conjunto2.length];
		int i = 0;
		for (int s : conjunto1) {
			conjunto3[i] = s;
			i++;
		}
		for (int s : conjunto2) {
			conjunto3[i] = s;
			i++;
		}
		return conjunto3;
	}

	static void scanto(int[] synchset) {
		int consumidos = 0;// indica cuantos token se cosumieron hasta alcanzar
							// un token de sincronizacion
		while (!(in(synchset) || token.id == EOF)) {
			getToken();
			consumidos++;
		}
		System.out.println("Se consumieron " + consumidos + " tokens");
	}

	/*
	 * funcion in devuelve true si el token actual se encuentra en el conjunto
	 */
	static boolean in(int[] conjunto) {
		for (int s : conjunto) {
			if (token.id == s) {
				return true;
			}
		}
		return false;
	}

	static void element(int[] synchset) {
		// conjunto primero EOF
		//System.out.println("Entra a element");
		checkinput(new int[] { L_CORCHETE, LITERAL_CADENA }, synchset);
		if (!(in(synchset))) {
			switch (token.id) {
			case L_CORCHETE:
				match(L_CORCHETE);
				tagname(new int[] { R_CORCHETE, COMA });
				aux(new int[] { R_CORCHETE });
				match(R_CORCHETE);
				break;
			case LITERAL_CADENA:
				match(LITERAL_CADENA);
				break;
			default:
				error();
			}
			checkinput(synchset, new int[] { L_CORCHETE, LITERAL_CADENA });
		}
		//System.out.println("Sale de element");
	}

	static void tagname(int[] synchset) {
		//System.out.println("Entra a tagname");
		checkinput(new int[] { LITERAL_CADENA }, synchset);
		if (!(in(synchset))) {
			match(LITERAL_CADENA);
		}
		checkinput(synchset, new int[] { LITERAL_CADENA });
		//System.out.println("Sale de tagname");
	}

	private static void aux(int[] synchset) {
		// un caso especial son las funciones que pueden tomar vacio:
		// es valido que venga la coma o que venga algo de su conjunto siguiente
		//System.out.println("Entra a aux");
		checkinput(union(new int[] { COMA }, synchset), new int[] {});
		if (!(in(synchset))) {
			match(COMA);
			aux2(new int[] { R_CORCHETE });
			checkinput(synchset, new int[] { LITERAL_CADENA });
		}
		//System.out.println("Sale de aux");
	}

	private static void aux2(int[] synchset) {
		//System.out.println("Entra a aux2");
		checkinput(new int[] { L_LLAVE, L_CORCHETE, LITERAL_CADENA }, synchset);
		if (!in(synchset)) {
			switch (token.id) {
			case L_LLAVE:
				attributes(new int[] { COMA, R_CORCHETE });
				aux3(new int[] { R_CORCHETE });
				break;
			case L_CORCHETE:
				elementList(new int[] { R_CORCHETE });
				break;
			case LITERAL_CADENA:
				elementList(new int[] { R_CORCHETE });// el corchete del element
														// que le contiene
				break;
			default:
				error();
			}
		}
		checkinput(synchset, new int[] { L_LLAVE, L_CORCHETE, LITERAL_CADENA });
		//System.out.println("Sale de aux2");
	}

	private static void attributes(int[] synchset) {
		//System.out.println("Entra a attributes");
		checkinput(new int[] { L_LLAVE }, synchset);
		if (!in(synchset)) {
			switch (token.id) {
			case L_LLAVE:
				match(L_LLAVE);
				aux7(new int[] { R_LLAVE });
				match(R_LLAVE);
				break;
			default:
				error();
			}
		}
		checkinput(synchset, new int[] { L_LLAVE });
		//System.out.println("Sale de attributes");
	}

	private static void aux3(int[] synchset) {
		//System.out.println("Entra a aux3");
		checkinput(union(new int[] { COMA }, synchset), synchset);
		if (!(in(synchset))) {
			match(COMA);
			elementList(new int[] { R_CORCHETE });
			checkinput(synchset, new int[] { COMA });
		}
		//System.out.println("Sale de aux3");
	}

	private static void elementList(int[] synchset) {
		//System.out.println("Entra a elementList");
		checkinput(new int[] { L_CORCHETE, LITERAL_CADENA }, synchset);
		if (!(in(synchset))) {
			switch (token.id) {
			case L_CORCHETE:
				element(new int[] { COMA, R_CORCHETE });
				aux5(new int[] { R_CORCHETE });
				break;
			case LITERAL_CADENA:
				element(new int[] { COMA, R_CORCHETE });
				aux5(new int[] { R_CORCHETE });
				break;
			default:
				error();
			}
			checkinput(synchset, new int[] { L_CORCHETE, LITERAL_CADENA });
		}
		//System.out.println("Sale de elementList");
	}

	private static void aux7(int[] synchset) {
		//System.out.println("Entra a aux7");
		checkinput(union(new int[] { LITERAL_CADENA }, synchset), synchset);
		if (!(in(synchset))) {
			attributesList(new int[] { R_LLAVE });
			checkinput(synchset, new int[] { LITERAL_CADENA });
		}
		//System.out.println("Sale de aux7");
	}

	private static void attributesList(int[] synchset) {
		//System.out.println("Entra a attributesList");
		checkinput(new int[] { LITERAL_CADENA }, synchset);
		if (!(in(synchset))) {
			switch (token.id) {
			case LITERAL_CADENA:
				attribute(new int[] { COMA, R_LLAVE });
				aux4(new int[] { R_LLAVE });
				break;
			default:
				error();
			}
			checkinput(synchset, new int[] { LITERAL_CADENA });
		}
		//System.out.println("Sale de attributesList");
	}

	private static void aux5(int[] synchset) {
		//System.out.println("Entra a aux5");
		checkinput(union(new int[] { COMA }, synchset), synchset);
		if (!(in(synchset))) {
			match(COMA);
			element(new int[] { COMA, R_CORCHETE });
			aux5(new int[] { R_CORCHETE });
			checkinput(synchset, new int[] { COMA });
		}
		//System.out.println("Sale de aux5");
	}

	private static void attribute(int[] synchset) {
		//System.out.println("Entra a attribute");
		checkinput(new int[] { LITERAL_CADENA }, synchset);
		if (!(in(synchset))) {
			switch (token.id) {
			case LITERAL_CADENA:
				attributeName(new int[] { DOS_PUNTOS });
				//System.out.println("Token id> " + token.id + " Token lexema> " + token.lexema + " Token compLex> "+ token.compLex);
				match(DOS_PUNTOS);
				//System.out.println("Token id> " + token.id + " Token lexema> " + token.lexema + " Token compLex> "+ token.compLex);
				attributeValue(new int[] { COMA, R_LLAVE });
				break;
			default:
				error();
			}
			checkinput(synchset, new int[] { LITERAL_CADENA });
		}
		//System.out.println("Sale de attribute");
	}

	private static void aux4(int[] synchset) {
		//System.out.println("Entra a aux4");
		checkinput(union(new int[] { COMA }, synchset), new int[] {});
		if (!(in(synchset))) {
			match(COMA);
			attribute(new int[] { COMA, R_LLAVE });
			aux4(new int[] { R_LLAVE });
			checkinput(synchset, new int[] { COMA });
		}
		//System.out.println("Sale de aux4");
	}

	private static void attributeName(int[] synchset) {
		//System.out.println("Entra a attributename");
		checkinput(new int[] { LITERAL_CADENA }, synchset);
		//System.out.println("Token id> " + token.id + " Token lexema> " + token.lexema + " Token compLex> " + token.compLex);
		if (!(in(synchset))) {
			switch (token.id) {
			case LITERAL_CADENA:
				match(LITERAL_CADENA);
				break;
			default:
				error();
			}
			checkinput(synchset, new int[] { LITERAL_CADENA });
		}
		//System.out.println("Sale de attributename");
	}

	private static void attributeValue(int[] synchset) {
		//System.out.println("Entra a attributevalue");
		//System.out.println("Token id> " + token.id + " Token lexema> " + token.lexema + " Token compLex> " + token.compLex);
		checkinput(new int[] { LITERAL_CADENA, LITERAL_NUM, PR_TRUE, PR_FALSE, PR_NULL }, synchset);
		//System.out.println("check");
		if (!(in(synchset))) {
			switch (token.id) {
			case LITERAL_NUM:
				match(LITERAL_NUM);
				break;
			case LITERAL_CADENA:
				match(LITERAL_CADENA);
				break;
			case PR_TRUE:
				match(PR_TRUE);
				break;
			case PR_FALSE:
				match(PR_FALSE);
				break;
			case PR_NULL:
				//System.out.println("Debe matchear con PR_NULL");
				match(PR_NULL);
				break;
			default:
				error();
			}
			checkinput(synchset, new int[] { LITERAL_CADENA, LITERAL_NUM, PR_TRUE, PR_FALSE, PR_NULL });
		}
		//System.out.println("Sale de attributevalue");
	}
}