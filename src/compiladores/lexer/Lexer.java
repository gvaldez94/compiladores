package compiladores.lexer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Scanner;
import static java.lang.Character.isDigit;
import compiladores.lexer.OSDetect;

/**
 * 
 * @author gvaldez
 *
 */

public class Lexer {
	public static Buffer archivo;
	public static int nroLinea = 1;
	public static String cadena = ""; // representa a un literal obtenido del
										// archivo
	public static Token token = null;
	public static LinkedList<Token> lista = new LinkedList<Token>();

	public static Token[] prList = new Token[3]; // tres palabras reservadas

	public static FileWriter fwriter = null;
	public static PrintWriter pw = null;
	public static FileReader freader = null;
	public static File path = null;

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

	public static String outputPath = "";

	public static void main(String[] args) {
		// inicializa palabras reservadas
		setPrList();
		Scanner entrada = new Scanner(System.in);

		OSDetect os = new OSDetect();

		// se obtiene el path del archivo
		do {
			System.out.print("Ruta del archivo: ");
			path = new File(entrada.nextLine());
		} while (!path.canRead());

		// se intenta leer el archivo
		try {
			outputPath = System.getProperty("user.dir");

			freader = new FileReader(path);
			archivo = new Buffer(freader);
			// se obtienen los lexemas
			do {
				getToken();
			} while (!("EOF".equals(lista.get(lista.size() - 1).compLex)));

			if (os.isWindows())
				outputPath = outputPath + "\\output.txt";
			else if (os.isUnix() || os.isMac())
				outputPath = outputPath + "/output.txt";

			fwriter = new FileWriter(outputPath);
			pw = new PrintWriter(fwriter);
			int nroLineaAux = 1;
			for (Token t : lista) {
				if (t.nroLinea == nroLineaAux) {
					pw.print(t.compLex + " ");
				} else {
					pw.print("\n" + t.compLex + " ");
					nroLineaAux = t.nroLinea;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fwriter != null)
					fwriter.close();
				if (freader != null)
					freader.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		entrada.close();
	}

	
	public static LinkedList<Token> iniciarLexer(){
        Scanner in = new Scanner(System.in);
        FileReader freader = null;
        File path = null;
        
        do{
            System.out.print("Ingrese la ruta del archivo: ");
            path = new File(in.nextLine());
        }while(!path.canRead());
        
        try {
            freader = new FileReader(path);
            archivo = new Buffer(freader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        in.close();
        return lista;
    }
    
    public static void cerrarArchivo(){
     try {
            if(fwriter != null)
                fwriter.close();
            if(freader != null)
                freader.close();
            if(archivo != null)
                archivo.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
	
	
	// obtiene todos los tokens
	public static void getToken() throws IOException {
		char c;
		// hasta que se encuentre un EOF
		while ((c = archivo.getchar()) != (char) -1) {
			cadena = "";
			if (c == ' ' || c == '\t' || c == '\r') { // si es un espacio
				continue;
			} else if (c == '\n') { // salto de linea
				nroLinea++;
				continue;
			} else if (c == '"') { // comienza un string
				int estado = 1;
				boolean acepto = false;
				cadena = cadena + c;
				while (!acepto) {
					switch (estado) {
					case 1:
						c = archivo.getchar();
						if (c == '\\') { // caracter de escape
							cadena = cadena + c;
							estado = 2;
						} else if (c == '"') { // fin de cadena
							cadena = cadena + c;
							estado = 3;
						}
						// salto de linea o EOF, cadena sin terminar
						else if (c == '\n' || c == (char) -1) {
							estado = -1;
						} else {
							cadena = cadena + c;
							estado = 1;
						}
						break;
					case 2:
						c = archivo.getchar();
						// caracteres de escape
						if (c == '"' || c == 'n' || c == 't' || c == 'f' || c == 'b' || c == 'r' || c == '\\'
								|| c == '/' || c == 'u') {
							cadena = cadena + c;
							estado = 1;
						} else {
							estado = -2;
						}
						break;
					case 3:
						// estado de aceptacion
						acepto = true;
						lista.add(token = new Token("LITERAL_CADENA", cadena, LITERAL_CADENA, nroLinea));
						break;
					case -1:
						if (c == '\n') {
							archivo.ungetchar(); // devolver el caracter
						}
						msgError("cadena sin cerrar");
						return;
					case -2:
						msgError("caracter de escape no valido");
						char caux = c;
						while (caux != '\n' && caux != (char) -1) {
							caux = archivo.getchar();
						}
						archivo.ungetchar();
						return;
					}// fin switch
				} // fin while
				break;
			} else if (c == ':') { // simbolos
				lista.add(token = new Token("DOS_PUNTOS", ":", DOS_PUNTOS, nroLinea));
				break;
			} else if (c == '[') {
				lista.add(token = new Token("L_CORCHETE", "[", L_CORCHETE, nroLinea));
				break;
			} else if (c == ']') {
				lista.add(token = new Token("R_CORCHETE", "]", R_CORCHETE, nroLinea));
				break;
			} else if (c == '{') {
				lista.add(token = new Token("L_LLAVE", "{", L_LLAVE, nroLinea));
				break;
			} else if (c == '}') {
				lista.add(token = new Token("R_LLAVE", "}", R_LLAVE, nroLinea));
				break;
			} else if (c == ',') {
				lista.add(token = new Token("COMA", ",", COMA, nroLinea));
				break;
			} else if (Character.isLetter(c)) { // palabras reservadas
				do {
					cadena = cadena + c;
					c = archivo.getchar();
				} while (Character.isLetter(c)); // repite mientras sea letra
				archivo.ungetchar();
				for (Token word : prList) {
					if (word.lexema.equals(cadena) || word.lexema.toLowerCase().equals(cadena)) {
						lista.add(token = new Token(word.compLex, cadena, word.id, nroLinea));
						return; // si es una palabra reservada se agrega a la
								// lista y se sale de la maquina
					}
				}
				msgError("lexema no valido " + cadena); // sino, es un error
				return;
			} else if (isDigit(c)) { // digitos
				int estado = 0;
				boolean acepto = false;
				cadena = cadena + c;

				while (!acepto) {
					switch (estado) {
					case 0: // secuencia solo de digitos, puede ocurrir . o e
						c = archivo.getchar();
						if (isDigit(c)) {
							cadena = cadena + c;
							estado = 0;
						} else if (c == '.') {
							cadena = cadena + c;
							estado = 1;
						} else if (Character.toLowerCase(c) == 'e') {
							cadena = cadena + c;
							estado = 3;
						} else {
							estado = 6;
						}
						break;
					case 1: // punto, sigue un digito
						c = archivo.getchar();
						if (isDigit(c)) {
							cadena = cadena + c;
							estado = 2;
						} else {
							msgError("No se esperaba " + c);
							estado = -1;
						}
						break;
					case 2: // fraccion decimal, siguen digitos o e
						c = archivo.getchar();
						if (isDigit(c)) {
							cadena = cadena + c;
							estado = 2;
						} else if (Character.toLowerCase(c) == 'e') {
							cadena = cadena + c;
							estado = 3;
						} else {
							estado = 6;
						}
						break;
					case 3: // una e, puede seguir un +, un - o digitos
						c = archivo.getchar();
						if (c == '+' || c == '-') {
							cadena = cadena + c;
							estado = 4;
						} else if (isDigit(c)) {
							cadena = cadena + c;
							estado = 5;
						} else {
							msgError("No se esperaba " + c);
							estado = -1;
						}
						break;
					case 4: // necesariamente debe venir por lo menos un digito
						c = archivo.getchar();
						if (isDigit(c)) {
							cadena = cadena + c;
							estado = 5;
						} else {
							msgError("No se esperaba " + c);
							estado = -1;
						}
						break;
					case 5: // secuencia de digitos del exponente
						c = archivo.getchar();
						if (isDigit(c)) {
							cadena = cadena + c;
							estado = 5;
						} else {
							estado = 6;
						}
						break;
					case 6: // estado de aceptacion
						if (c != (char) -1) {
							archivo.ungetchar();
						} else {
							c = (char) 0;
						}
						acepto = true;
						lista.add(token = new Token("LITERAL_NUM", cadena, LITERAL_NUM, nroLinea));
						break;
					case -1:
						if (c == (char) -1) {
							msgError("No se esperaba fin de archivo");
						} else if (c == '\n') {
							// devolver el caracter para contar nro de linea
							archivo.ungetchar();
							msgError("No se esperaba fin de linea");
						}
						return;
					} // fin switch
				}
			} else {
				msgError("Caracter no valido " + c);
			}
		}
		if (c == (char) -1) {
			lista.add(token = new Token("EOF", "EOF", EOF, nroLinea));
		}
		//System.out.println("Linea: " + nroLinea + "\t compLex: " +  token.compLex);
	}

	public static void msgError(String msg) {
		System.out.println(String.format("Linea %-4d" + " " + msg, nroLinea));
		lista.add(token = new Token("ERROR", "ERROR", ERROR, nroLinea));
	}

	public static void setPrList() {
		prList[0] = new Token("PR_TRUE", "TRUE", 9, 0);
		prList[1] = new Token("PR_FALSE", "FALSE", 10, 0);
		prList[2] = new Token("PR_NULL", "NULL", 11, 0);
	}
}
