package compiladores.parser;

import java.io.IOException;
import java.util.LinkedList;
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
	static LinkedList<Token> tokenList = null;
	
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
        tokenList = Lexer.iniciarLexer();
        Lexer.setPrList();
        getToken();
        //element(new int[]{EOF});
        element();
        Lexer.cerrarArchivo();
        if(aceptar)
            System.out.println("Sin errores sintacticos");
    }

	static void getToken(){
        do{
            try 
            {
                Lexer.getToken();
                token = Lexer.token;
            } catch (IOException ex) {
                Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }while (token.id==ERROR);
    }
    
    static void match(int tokenEsperado){
        if (token.id==tokenEsperado){
            getToken();
        }
        else{
            error();
        }
    }

    static void error() {
        System.out.println("Error de sintaxis en linea "+ Lexer.nroLinea+" no se esperaba "+token);
        aceptar=false;
        if (token.id==EOF)//esto evita que se produzacan llamadas recursivas infinitas en ciertos casos de error
            System.exit(0);
    }
    
    static void checkinput(int[] firsts, int[] follows){
        if(!(in(firsts))){
            error();
            scanto(union(firsts, follows));
        }
    }
    /*
        funcion union
        devuelve un array de tokens que contiene todos los tokens del conjunto1
        y el conjunto2
    */
    static int[] union(int[] conjunto1, int[] conjunto2){
        int[] conjunto3 = new int[conjunto1.length+conjunto2.length];
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
    
    static void scanto (int[] synchset){ 
        int consumidos = 0;//indica cuantos token se cosumieron hasta alcanzar un token de sincronizacion
        while(!(in(synchset) || token.id==EOF)){
            getToken();
            consumidos++;
        }
        System.out.println("Se comsumieron "+consumidos+" tokens");
    }
    /*
        funcion in
        devuelve true si el token actual se encuentra en el conjunto
    */
    static boolean in(int[] conjunto){
        for (int s : conjunto) {
            if(token.id==s){
               return true;
            } 
        }
        return false;
    }
	
    
    static void element(){
    	System.out.println("Metodo element");
    	switch (token.id){
    	case L_CORCHETE:
    		match(L_CORCHETE);
    		tagname();
    		if (token.id == COMA) match(COMA);
    		if (token.id == L_LLAVE) attributes();
    		if (token.id == COMA) match(COMA);
    		if (token.id == L_CORCHETE || token.id == LITERAL_CADENA) elementList();
    		match(R_CORCHETE);
    		break;
    	case LITERAL_CADENA:
    		match(LITERAL_CADENA);
    		break;
    	default:
    		System.out.println("Error de element");
    		error();
    	}
    }
    
    static void tagname(){
    	System.out.println("Metodo tagname");
    	switch (token.id){
    	case LITERAL_CADENA:
    		match(LITERAL_CADENA);
    		break;
    	default:
    		System.out.println("Error de tagname");
    		error();
    	}
    }
    
    static void attributes(){
    	System.out.println("Metodo attributes");
    	switch(token.id){
    	case L_LLAVE:
    		match(L_LLAVE);
    		attributeList();
    		match(R_LLAVE);
    		break;
    	default:
    		System.out.println("Error de attributes");
    		error();
    	}
    }
    
    static void attributeList(){
    	System.out.println("Metodo attributeList");
    	switch(token.id){
    	case LITERAL_CADENA:
    		attribute();
    		attribP();
    		break;
    	default:
    		System.out.println("Error de attributeList");
    		error();
    	}
    }
    
    static void attribP(){
    	System.out.println("Metodo attribP");
    	switch(token.id){
    	case COMA:
    		match(COMA);
    		attributeList();
    		attribP();
    		break;
    	}
    }
    
    static void attribute(){
    	System.out.println("Metodo attribute");
    	switch(token.id){
    	case LITERAL_CADENA:
    		attributeName();
    		match(DOS_PUNTOS);
    		attributeValue();
    		break;
    	default:
    		System.out.println("Error de attribute");
    		error();
    	}
    }
    
    static void attributeName(){
    	System.out.println("Metodo attributeName");
    	if (token.id == LITERAL_CADENA){
    		match(LITERAL_CADENA);
    	}
    	else {
    		System.out.println("Error de attributeName");
    		error();
    	}
    }
    
    static void attributeValue(){
    	System.out.println("Metodo attributeValue");
    	switch(token.id){
    	case LITERAL_CADENA:
    		match(LITERAL_CADENA);
    		break;
    	case LITERAL_NUM:
    		match(LITERAL_NUM);
    		break;
    	case PR_TRUE:
    		match(PR_TRUE);
    		break;
    	case PR_FALSE:
    		match(PR_FALSE);
    		break;
    	case PR_NULL:
    		match(PR_NULL);
    		break;
    	default:
    		System.out.println("Error de attributeValue");
    		error();
    	}
    }
    
    static void elementList(){
    	System.out.println("Metodo elementList");
    	switch(token.id){
    	case L_CORCHETE:
    		element();
    		eleListP();
    		break;
    	case LITERAL_CADENA:
    		element();
    		eleListP();
    		break;
    	default:
    		System.out.println("Error de elementList");
    		error();
    	}
    }
    
    static void eleListP(){
    	System.out.println("Metodo eleListP");
    	if (token.id == COMA){
    		match(COMA);
    		elementList();
    		eleListP();
    	}
    }
    /*
    static void element(int[] synchset){
        //                  conjunto primero              EOF
        checkinput(new int[]{L_CORCHETE,LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            switch(token.id){
                case L_CORCHETE:
                    match(L_CORCHETE);
                    tagname(new int[]{R_CORCHETE, COMA});
                    aux(new int[]{R_CORCHETE});
                    match(R_CORCHETE);
                    break;
                case LITERAL_CADENA:
                    match(LITERAL_CADENA);
                    break;
                default:
                    error();
            }
            checkinput(synchset, new int[]{L_CORCHETE,LITERAL_CADENA});
        }
    }

    static void tagname(int[] synchset) {
        checkinput(new int[]{LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            match(LITERAL_CADENA);
        }
        checkinput(synchset, new int[]{LITERAL_CADENA});
    }

    private static void aux(int[] synchset) {
        //un caso especial son las funciones que pueden tomar vacio:
        //es valido que venga la coma o que venga algo de su conjunto siguiente
        checkinput(union(new int[]{COMA},synchset), new int[]{});
        if(!(in(synchset))){
            match(COMA);
            aux2(new int[]{R_CORCHETE});
            checkinput(synchset, new int[]{LITERAL_CADENA});
        }
    }

    private static void aux2(int[] synchset) {
        checkinput(new int[]{L_LLAVE, L_CORCHETE, LITERAL_CADENA }, synchset);
        if(!in(synchset)){
            switch(token.id){
                case L_LLAVE:
                    atributes(new int[]{COMA, R_CORCHETE});
                    aux3(new int[]{R_CORCHETE});
                    break;
                case L_CORCHETE:
                    elementlist(new int[]{R_CORCHETE});
                    break;
                case LITERAL_CADENA:
                    elementlist(new int[]{R_CORCHETE});//el corchete del element que le contiene
                    break;
                default:
                    error();
            }
        }
        checkinput(synchset, new int[]{L_LLAVE, L_CORCHETE, LITERAL_CADENA });
    }

    private static void atributes(int[]synchset) {
        checkinput(new int[]{L_LLAVE}, synchset);
        if(!in(synchset)){
            switch(token.id){
                case L_LLAVE:
                    match(L_LLAVE);
                    aux7(new int[]{R_LLAVE});
                    match(R_LLAVE);
                    break;
                default:
                    error();
            }
        }
        checkinput(synchset, new int[]{L_LLAVE});
    }

    private static void aux3(int[]synchset) {
        checkinput(union(new int[]{COMA},synchset), synchset);
        if(!(in(synchset))){
            match(COMA);
            elementlist(new int[]{R_CORCHETE});
            checkinput(synchset, new int[]{COMA});
        }
    }

    private static void elementlist(int[] synchset) {
        checkinput(new int[]{L_CORCHETE,LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            switch(token.id){
                case L_CORCHETE:
                    element(new int[]{COMA, R_CORCHETE});
                    aux5(new int[]{R_CORCHETE});
                    break;
                case LITERAL_CADENA:
                    element(new int[]{COMA, R_CORCHETE});
                    aux5(new int[]{R_CORCHETE});
                    break;
                default:
                    error();
            }
            checkinput(synchset, new int[]{L_CORCHETE,LITERAL_CADENA});
        }
    }

    private static void aux7(int[] synchset) {
        checkinput(union(new int[]{LITERAL_CADENA},synchset), synchset);
        if(!(in(synchset))){
            atributeslist(new int[]{R_LLAVE});
            checkinput(synchset, new int[]{LITERAL_CADENA});
        }
    }

    private static void atributeslist(int[] synchset) {
        checkinput(new int[]{LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            switch(token.id){
                case LITERAL_CADENA:
                    atribute(new int[]{COMA,R_LLAVE});
                    aux4(new int[]{R_LLAVE});
                    break;    
                default:
                    error();
            }
            checkinput(synchset, new int[]{LITERAL_CADENA});
        }
    }

    private static void aux5(int[] synchset) {
        checkinput(union(new int[]{COMA},synchset), synchset);
        if(!(in(synchset))){
            match(COMA);
            element(new int[]{COMA, R_CORCHETE});
            aux5(new int[]{R_CORCHETE});
            checkinput(synchset, new int[]{COMA});
        }
    }

    private static void atribute(int[] synchset) {
        checkinput(new int[]{LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            switch(token.id){
                case LITERAL_CADENA:
                    attribute_name(new int[]{DOS_PUNTOS});
                    match(DOS_PUNTOS);
                    attribute_value(new int[]{COMA,R_LLAVE});
                    break;
                default:
                    error();
            }
            checkinput(synchset, new int[]{LITERAL_CADENA});
        }
    }

    private static void aux4(int[] synchset) {
        checkinput(union(new int[]{COMA},synchset), new int[]{});
        if(!(in(synchset))){
            match(COMA);
            atribute(new int[]{COMA,R_LLAVE});
            aux4(new int[]{R_LLAVE});
            checkinput(synchset, new int[]{COMA});
        }
    }

    private static void attribute_name(int[] synchset) {
        checkinput(new int[]{LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            switch(token.id){
                case LITERAL_CADENA:
                    match(LITERAL_CADENA);
                    break;    
                default:
                    error();
            }
            checkinput(synchset, new int[]{LITERAL_CADENA});
        }
    }

    private static void attribute_value(int[] synchset) {
        checkinput(new int[]{LITERAL_CADENA,LITERAL_NUM,PR_TRUE,PR_FALSE,PR_NULL}, synchset);
        if(!(in(synchset))){
            switch(token.id){
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
                    match(PR_NULL);
                    break;    
                default:
                    error();
            }
            checkinput(synchset, new int[]{LITERAL_CADENA,LITERAL_NUM,PR_TRUE,PR_FALSE,PR_NULL});
        }
    }
    */
}
