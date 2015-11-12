package compiladores.traductor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import compiladores.lexer.Lexer;
import compiladores.lexer.OSDetect;
import compiladores.lexer.Token;
import compiladores.parser.Parser;



/**
 *
 * @author gvaldez
 */
public class Traductor {
    static int index = -1;//usado para manejar la entrada
    static int acepto=0;//para mostrar mensaje de aceptado
    static Token token  = new Token(null, null, -1, 0);//token actual
    
    
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
        String traduccion = "";
        FileWriter fwriter = null;
        PrintWriter pw = null;
        File path = null;
        OSDetect os = new OSDetect();
        
        Lexer.iniciarLexer();
        path = Lexer.path;//el fuente xml se envia al mismo directorio del archivo json
        //System.out.println(path);
        getToken();
        traduccion = element(new int[]{EOF});
        Lexer.cerrarArchivo();
        
        if(acepto==0){
            System.out.println("Sin errores sintacticos");
            try {
            	if (os.isWindows())
            		fwriter = new FileWriter(path.getParent()+"\\out.xml");
    			else if (os.isUnix() || os.isMac())
    				fwriter = new FileWriter(path.getParent()+"/out.xml");
                pw = new PrintWriter(fwriter);
                pw.print("<?xml version = \"1.0\" encoding=\"UTF-8\"?>"+traduccion);
            } catch (IOException ex) {
                Logger.getLogger(Traductor.class.getName()).log(Level.SEVERE, null, ex);
            }finally{
                try {
                    if(fwriter != null)
                        fwriter.close();
                } 
                catch (IOException ex) {
                    ex.printStackTrace();
                }
             }
        }
        
    }
    
    /*asigna un nuevo token en la variable global token*/
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
        System.out.println("error de sintaxis en linea "+Lexer.nroLinea+" no se esperaba "+token);
        acepto=-1;
        if (token.id==EOF)
            System.exit(0);
    }
    
    static void checkinput(int[] firsts, int[] follows){
        if(!(in(firsts))){
            error();
            scanto(union(firsts, follows));
        }
    }
    
    static int[] union(int[] array1, int[] array2){
        int[] array3 = new int[array1.length+array2.length];
        int i = 0;
        for (int s : array1) {
            array3[i] = s;
            i++;
        }
        for (int s : array2) {
            array3[i] = s;
            i++;
        }
        return array3;
    }
    
    static void scanto (int[] synchset){ 
        int consumidos = 0;
        while(!(in(synchset) || token.id==EOF)){
            getToken();
            consumidos++;
        }
        
        System.out.println("se comsumieron "+consumidos+" tokens");
    }
    
    static boolean in(int[] array){
        for (int s : array) {
            if(token.id==s){
               return true;
            } 
        }
        return false;
    }
    
    static String sinComillas(String c){
        c = c.substring(1, c.length()-1);
        return c;
    }
    
    
    static String element(int[] synchset){
        String tagnameTrad;
        String auxTrad;
        //                  conjunto primero              EOF
        checkinput(new int[]{L_CORCHETE,LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            switch(token.id){
                case L_CORCHETE:
                    match(L_CORCHETE);
                    tagnameTrad = tagname(new int[]{R_CORCHETE, COMA});
                    auxTrad = aux(new int[]{R_CORCHETE});
                    match(R_CORCHETE);
                    checkinput(synchset, new int[]{L_CORCHETE,LITERAL_CADENA});//luego de return ya no funciona el ultimo check
                    return "<"+tagnameTrad+auxTrad+" </"+tagnameTrad+">";//element-->[tagname aux]
                case LITERAL_CADENA:
                    String cadena = token.lexema;//despues del match la cadena se pierde por getToken
                    match(LITERAL_CADENA);
                    checkinput(synchset, new int[]{L_CORCHETE,LITERAL_CADENA});
                    return sinComillas(cadena);//element--> string
                default:
                    error();
            }
            checkinput(synchset, new int[]{L_CORCHETE,LITERAL_CADENA});
        }
        return "";//aqui se llega solo si ocurre un error de sintaxis
    }

    static String tagname(int[] synchset) {
        String cadena="";
        checkinput(new int[]{LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            cadena = token.lexema;//despues del match la cadena se pierde por getToken
            match(LITERAL_CADENA);
        }
        checkinput(synchset, new int[]{LITERAL_CADENA});
        return sinComillas(cadena);//tagname--> string
    }

    static String aux(int[] synchset) {
        //un caso especial son las funciones que pueden tomar vacio:
        //es valido que venga la coma o que venga algo de su conjunto siguiente
        String aux2Trad = "";
        checkinput(union(new int[]{COMA},synchset), new int[]{});
        if(!(in(synchset))){
            match(COMA);
            aux2Trad = aux2(new int[]{R_CORCHETE});
            checkinput(synchset, new int[]{LITERAL_CADENA});
            return aux2Trad;//aux--> ,aux2
        }
        return " >";//aux--> vacio
    }

    static String aux2(int[] synchset) {
        String atributesTrad = "";
        String aux3Trad = "";
        String elementlistTrad = "";
        checkinput(new int[]{L_LLAVE, L_CORCHETE, LITERAL_CADENA }, synchset);
        if(!in(synchset)){
            switch(token.id){
                case L_LLAVE:
                    atributesTrad = atributes(new int[]{COMA, R_CORCHETE});
                    aux3Trad = aux3(new int[]{R_CORCHETE});
                    checkinput(synchset, new int[]{L_LLAVE, L_CORCHETE, LITERAL_CADENA });
                    return " "+atributesTrad+">"+aux3Trad;//aux2--> atributes aux3
                case L_CORCHETE:
                    elementlistTrad = elementlist(new int[]{R_CORCHETE});
                    checkinput(synchset, new int[]{L_LLAVE, L_CORCHETE, LITERAL_CADENA });
                    return ">\n"+elementlistTrad;//aux2--> elementlist
                case LITERAL_CADENA:
                    elementlistTrad = elementlist(new int[]{R_CORCHETE});//el corchete del element que le contiene
                    checkinput(synchset, new int[]{L_LLAVE, L_CORCHETE, LITERAL_CADENA });
                    return "\n"+elementlistTrad;//aux2--> elementlist
                default:
                    error();
            }
        }
        checkinput(synchset, new int[]{L_LLAVE, L_CORCHETE, LITERAL_CADENA });
        return "";//aqui se llega solo en caso de error de sintaxis
    }

    static String atributes(int[]synchset) {
        String aux7Trad = "";
        checkinput(new int[]{L_LLAVE}, synchset);
        if(!in(synchset)){
            switch(token.id){
                case L_LLAVE:
                    match(L_LLAVE);
                    aux7Trad = aux7(new int[]{R_LLAVE});
                    match(R_LLAVE);
                    break;
                default:
                    error();
            }
        }
        checkinput(synchset, new int[]{L_LLAVE});
        return aux7Trad;//atributes--> {aux7}
    }

    static String aux3(int[]synchset) {
        String elementlistTrad = "";
        checkinput(union(new int[]{COMA},synchset), synchset);
        if(!(in(synchset))){
            match(COMA);
            elementlistTrad = elementlist(new int[]{R_CORCHETE});
            checkinput(synchset, new int[]{COMA});
            return "\n"+elementlistTrad;//aux3--> ,elementlist
        }
        return "";//aux3--> vacio
    }

    static String elementlist(int[] synchset) {
        String elementTrad = "";
        String aux5Trad = "";
        checkinput(new int[]{L_CORCHETE,LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            switch(token.id){
                case L_CORCHETE:
                    elementTrad = element(new int[]{COMA, R_CORCHETE});
                    aux5Trad = aux5(new int[]{R_CORCHETE});
                    checkinput(synchset, new int[]{L_CORCHETE,LITERAL_CADENA});
                    return elementTrad+aux5Trad;//elementlist--> element aux5
                case LITERAL_CADENA:
                    elementTrad = element(new int[]{COMA, R_CORCHETE});
                    aux5Trad = aux5(new int[]{R_CORCHETE});
                    checkinput(synchset, new int[]{L_CORCHETE,LITERAL_CADENA});
                    return elementTrad+aux5Trad;//elementlist--> element aux5
                default:
                    error();
            }
            checkinput(synchset, new int[]{L_CORCHETE,LITERAL_CADENA});
        }
        return "";//aqui se llega solo si ocurre un error de sintaxis
    }

    private static String aux7(int[] synchset) {
        String atributeslistTrad = "";
        checkinput(union(new int[]{LITERAL_CADENA},synchset), synchset);
        if(!(in(synchset))){
            atributeslistTrad = atributeslist(new int[]{R_LLAVE});
            checkinput(synchset, new int[]{LITERAL_CADENA});
            return atributeslistTrad;//aux7--> atributeslist
        }
        return "";//aux7--> vacio
    }

    private static String atributeslist(int[] synchset) {
        String atributeTrad = "";
        String aux4Trad = "";
        checkinput(new int[]{LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            switch(token.id){
                case LITERAL_CADENA:
                    atributeTrad = atribute(new int[]{COMA,R_LLAVE});
                    aux4Trad = aux4(new int[]{R_LLAVE});
                    checkinput(synchset, new int[]{LITERAL_CADENA});
                    return atributeTrad+aux4Trad;//atributelist-->atribute aux4  
                default:
                    error();
            }
            checkinput(synchset, new int[]{LITERAL_CADENA});
        }
        return "";//aqui se llega solo si ocurre un error de sintaxis
    }

    private static String aux5(int[] synchset) {
        String elementTrad = "";
        String aux5Trad = "";
        checkinput(union(new int[]{COMA},synchset), synchset);
        if(!(in(synchset))){
            match(COMA);
            elementTrad = element(new int[]{COMA, R_CORCHETE});
            aux5Trad = aux5(new int[]{R_CORCHETE});
            checkinput(synchset, new int[]{COMA});
            return "\n"+elementTrad+aux5Trad;//aux5--> ,element aux5
        }
        return "";//aux5--> vacio
    }

    private static String atribute(int[] synchset) {
        String attribute_nameTrad = "";
        String attribute_valueTrad = "";
        checkinput(new int[]{LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            switch(token.id){
                case LITERAL_CADENA:
                    attribute_nameTrad = attribute_name(new int[]{DOS_PUNTOS});
                    match(DOS_PUNTOS);
                    attribute_valueTrad = attribute_value(new int[]{COMA,R_LLAVE});
                    checkinput(synchset, new int[]{LITERAL_CADENA});
                    return attribute_nameTrad+" = "+attribute_valueTrad;//atribute--> attribute_name : attribuete_value
                default:
                    error();
            }
            checkinput(synchset, new int[]{LITERAL_CADENA});
        }
        return "";//aqui se llega solo si ocurre un error de sintaxis
    }

    private static String aux4(int[] synchset) {
        String atributeTrad = "";
        String aux4Trad = "";
        checkinput(union(new int[]{COMA},synchset), new int[]{});
        if(!(in(synchset))){
            match(COMA);
            atributeTrad = atribute(new int[]{COMA,R_LLAVE});
            aux4Trad = aux4(new int[]{R_LLAVE});
            checkinput(synchset, new int[]{COMA});
            return " "+atributeTrad+aux4Trad;//aux4--> , atribute aux4
        }
        return "";//aux4--> vacio
    }

    private static String attribute_name(int[] synchset) {
        String cadena = "";
        checkinput(new int[]{LITERAL_CADENA}, synchset);
        if(!(in(synchset))){
            switch(token.id){
                case LITERAL_CADENA:
                    cadena = token.lexema;//despues del match la cadena se pierde por getToken
                    match(LITERAL_CADENA);
                    checkinput(synchset, new int[]{LITERAL_CADENA});
                    return sinComillas(cadena);    
                default:
                    error();
            }
            checkinput(synchset, new int[]{LITERAL_CADENA});
        }
        return "";//aqui se llega solo si ocurre un error de sintaxis
    }

    private static String attribute_value(int[] synchset) {
        String cadena = "";
        checkinput(new int[]{LITERAL_CADENA,LITERAL_NUM,PR_TRUE,PR_FALSE,PR_NULL}, synchset);
        if(!(in(synchset))){
            switch(token.id){
                case LITERAL_NUM:
                    cadena = token.lexema;
                    match(LITERAL_NUM);
                    checkinput(synchset, new int[]{LITERAL_CADENA,LITERAL_NUM,PR_TRUE,PR_FALSE,PR_NULL});
                    return cadena;//attribute_value--> LITERAL_NUM
                case LITERAL_CADENA:
                    cadena = token.lexema;
                    match(LITERAL_CADENA);
                    checkinput(synchset, new int[]{LITERAL_CADENA,LITERAL_NUM,PR_TRUE,PR_FALSE,PR_NULL});
                    return cadena;//attribute_value--> LITERAL_CADENA
                case PR_TRUE:
                    match(PR_TRUE);
                    checkinput(synchset, new int[]{LITERAL_CADENA,LITERAL_NUM,PR_TRUE,PR_FALSE,PR_NULL});
                    return "true";//attribute_value--> PR_TRUE
                case PR_FALSE:
                    match(PR_FALSE);
                    checkinput(synchset, new int[]{LITERAL_CADENA,LITERAL_NUM,PR_TRUE,PR_FALSE,PR_NULL});
                    return "false";//attribute_value--> PR_FALSE
                case PR_NULL:
                    match(PR_NULL);
                    checkinput(synchset, new int[]{LITERAL_CADENA,LITERAL_NUM,PR_TRUE,PR_FALSE,PR_NULL});
                    return " ";//attribute_value--> PR_NULL
                default:
                    error();
            }
            checkinput(synchset, new int[]{LITERAL_CADENA,LITERAL_NUM,PR_TRUE,PR_FALSE,PR_NULL});
        }
        return "";//aqui se llega solo si ocurre un error de sintaxis
    }
}