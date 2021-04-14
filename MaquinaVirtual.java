package interpretador;

import java.util.Stack;

public class MaquinaVirtual {

    private Stack <Token> aritmetica;
    private Stack <Token> booleana;
    private AnalisadorSemantico as;
    private boolean habilitada = true;


    public MaquinaVirtual(AnalisadorSemantico as){
        aritmetica = new Stack<>();
        booleana   = new Stack<>();
        this.as = as;
    }

    public void empilhaBooleana(){
        // "Poda"
        if(!habilitada)
            return;

        Token t = null;

        if(as.getExpressaoTipo() == 1)
            t = new Token(getResultadoFinal(), TipoToken.CONSTANTE_INTEIRA);
        else if(as.getExpressaoTipo() == 2)
            t = new Token(getResultadoFinal(), TipoToken.CONSTANTE_FLOAT);
        else if(as.getExpressaoTipo() == 3)
            t = new Token(getResultadoFinal(), TipoToken.CONSTANTE_DOUBLE);
        else if(as.getExpressaoTipo() == 0)
            t = new Token(getResultadoFinal(), TipoToken.CONSTANTE_CHAR);

        // Empilha um novo token.
        booleana.push(t);
    }

    public void empilhaBooleana(Token t){
        // "Poda"
        if(!habilitada)
            return;
        // Empilha um novo token.
        booleana.push(t);
    }

    public void empilha(Token t){
        // "Poda"
        if(!habilitada)
            return;

        // Empilhar o novo Lexema de um token.
        aritmetica.push(t);
    }

    private String getValor(Token t){

        String valor = "";
        if(t.getTipo() == TipoToken.IDENTIFICADOR)
            // Obter o valor da tabela de símbolos.
            valor = as.getValor(t.getLexema());
        else if(t.getTipo() == TipoToken.CONSTANTE_INTEIRA || t.getTipo() == TipoToken.CONSTANTE_BOOLEANA || t.getTipo() == TipoToken.CONSTANTE_DOUBLE || t.getTipo() == TipoToken.CONSTANTE_FLOAT || t.getTipo() == TipoToken.CONSTANTE_CHAR)
            valor = t.getLexema();
        return valor;
    }

    public void avaliarCondicao(){
        // "Poda"
        if(!habilitada)
            return;

        Token tokenOperando2 = booleana.pop();
        Token tokenOperador  = booleana.pop();
        Token tokenOperando1 = booleana.pop();

        String operando1 = getValor(tokenOperando1);
        String operando2 = getValor(tokenOperando2);
        String operador  = tokenOperador.getLexema();

        boolean resultado = true;

        // Verificar o lexema do operador, converter os operandos e avaliar.
        if (operador.equals("&&"))
            resultado = Boolean.parseBoolean(operando1) && Boolean.parseBoolean(operando2);
        else if (operador.equals("||"))
            resultado = Boolean.parseBoolean(operando1) || Boolean.parseBoolean(operando2);

        if(as.getExpressaoTipo() == 1) {
            if (operador.contentEquals("=="))
                resultado = Integer.parseInt(operando1) == Integer.parseInt(operando2);
            else if (operador.contentEquals("!="))
                resultado = Integer.parseInt(operando1) != Integer.parseInt(operando2);
            else if (operador.contentEquals(">="))
                resultado = Integer.parseInt(operando1) >= Integer.parseInt(operando2);
            else if (operador.contentEquals("<="))
                resultado = Integer.parseInt(operando1) <= Integer.parseInt(operando2);
            else if (operador.contentEquals(">"))
                resultado = Integer.parseInt(operando1) > Integer.parseInt(operando2);
            else if (operador.contentEquals("<"))
                resultado = Integer.parseInt(operando1) < Integer.parseInt(operando2);

        }else if (as.getExpressaoTipo() == 2){
            if (operador.contentEquals("=="))
                resultado = Float.parseFloat(operando1) == Float.parseFloat(operando2);
            else if (operador.contentEquals("!="))
                resultado = Float.parseFloat(operando1) != Float.parseFloat(operando2);
            else if (operador.contentEquals(">="))
                resultado = Float.parseFloat(operando1) >= Float.parseFloat(operando2);
            else if (operador.contentEquals("<="))
                resultado = Float.parseFloat(operando1) <= Float.parseFloat(operando2);
            else if (operador.contentEquals(">"))
                resultado = Float.parseFloat(operando1) > Float.parseFloat(operando2);
            else if (operador.contentEquals("<"))
                resultado = Float.parseFloat(operando1) < Float.parseFloat(operando2);

        }else if (as.getExpressaoTipo() == 3){
            if (operador.contentEquals("=="))
                resultado = Double.parseDouble(operando1) == Double.parseDouble(operando2);
            else if (operador.contentEquals("!="))
                resultado = Double.parseDouble(operando1) != Double.parseDouble(operando2);
            else if (operador.contentEquals(">="))
                resultado = Double.parseDouble(operando1) >= Double.parseDouble(operando2);
            else if (operador.contentEquals("<="))
                resultado = Double.parseDouble(operando1) <= Double.parseDouble(operando2);
            else if (operador.contentEquals(">"))
                resultado = Double.parseDouble(operando1) > Double.parseDouble(operando2);
            else if (operador.contentEquals("<"))
                resultado = Double.parseDouble(operando1) < Double.parseDouble(operando2);

        }else if (as.getExpressaoTipo() == 0) {
            if (operador.contentEquals("=="))
                resultado = operando1.charAt(0) == operando2.charAt(0);
            else if (operador.contentEquals("!="))
                resultado = operando1.charAt(0) != operando2.charAt(0);
            else if (operador.contentEquals(">="))
                resultado = operando1.charAt(0) >= operando2.charAt(0);
            else if (operador.contentEquals("<="))
                resultado = operando1.charAt(0) <= operando2.charAt(0);
            else if (operador.contentEquals(">"))
                resultado = operando1.charAt(0) > operando2.charAt(0);
            else if (operador.contentEquals("<"))
                resultado = operando1.charAt(0) < operando2.charAt(0);
        }
        // Converter o resultado em String.
        String resultadoString = String.valueOf(resultado);
        // Montar o novo token que será empilhado.
        Token resultadoToken = new Token(resultadoString, TipoToken.CONSTANTE_BOOLEANA);
        // Empilhar!
        booleana.push(resultadoToken);
    }

    public void calcularExpressao(){
        // "Poda"
        if(!habilitada)
            return;

        Token tokenOperando2 = aritmetica.pop();
        Token tokenOperador = aritmetica.pop();
        Token tokenOperando1 = aritmetica.pop();

        String operando1 = getValor(tokenOperando1);
        String operando2 = getValor(tokenOperando2);
        String operador  = tokenOperador.getLexema();

        Token tokenResultado = null;

        /*if((tokenOperando1.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando1.getLexema()).equals("int")) || tokenOperando1.getTipo().equals(TipoToken.CONSTANTE_INTEIRA)) {
            if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("int")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_INTEIRA)) {
                tokenResultado = calcularResultado(operando1, operando2, operador,1);
            }else if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("float")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_FLOAT)){
                tokenResultado = calcularResultado(operando1, operando2, operador, 2);
            }else if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("double")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_DOUBLE)) {
                tokenResultado = calcularResultado(operando1, operando2, operador, 3);
                System.out.println("Veio aq?");
            } else if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("char")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_CHAR)){
                tokenResultado = calcularResultado(operando1, operando2, operador, 0);
            }
        }else if((tokenOperando1.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando1.getLexema()).equals("float")) || tokenOperando1.getTipo().equals(TipoToken.CONSTANTE_FLOAT)) {
            if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("float")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_FLOAT)) {
                tokenResultado = calcularResultado(operando1, operando2, operador, 2);
            }else if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("int")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_INTEIRA)){
                tokenResultado = calcularResultado(operando1, operando2, operador, 2);
            }else if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("double")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_DOUBLE)) {
                tokenResultado = calcularResultado(operando1, operando2, operador, 3);
            } else if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("char")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_CHAR)){
                tokenResultado = calcularResultado(operando1, operando2, operador, 0);
            }
        }else if((tokenOperando1.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando1.getLexema()).equals("double")) || tokenOperando1.getTipo().equals(TipoToken.CONSTANTE_DOUBLE)) {
            if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("double")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_DOUBLE)) {
                tokenResultado = calcularResultado(operando1, operando2, operador, 3);

            }else if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("int")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_INTEIRA)){
                tokenResultado = calcularResultado(operando1, operando2, operador, 3);
            }else if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("float")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_FLOAT)) {
                tokenResultado = calcularResultado(operando1, operando2, operador, 3);
            } else if ((tokenOperando2.getTipo() == TipoToken.IDENTIFICADOR && as.getTipo(tokenOperando2.getLexema()).equals("char")) || tokenOperando2.getTipo().equals(TipoToken.CONSTANTE_CHAR)){
                tokenResultado = calcularResultado(operando1, operando2, operador, 0);
            }
        }else{
            //tokenResultado = calcularResultado(operando1, operando2, operador, 3);
            System.out.println("Entrando aqui no else");
        }*/

        tokenResultado = calcularResultado(operando1, operando2, operador, as.getExpressaoTipo());

        // Empilhar o resultado
        aritmetica.push(tokenResultado);
    }

    private Token calcularResultado(String numOperando1, String numOperando2, String operador, int tipo){
        String numResultado = "";
        Token resultadoToken = null;

        if(tipo == 1){
            int resultado = 0;
            if(operador.equals("+"))
                resultado = (int) Integer.parseInt(numOperando1) + Integer.parseInt(numOperando2);
            else if(operador.equals("-"))
                resultado = (int) Integer.parseInt(numOperando1) - Integer.parseInt(numOperando2);
            else if(operador.equals("*"))
                resultado = (int) Integer.parseInt(numOperando1) * Integer.parseInt(numOperando2);
            else if(operador.equals("/"))
                resultado = (int) Integer.parseInt(numOperando1) / Integer.parseInt(numOperando2);

            numResultado = String.valueOf(resultado);

            resultadoToken = new Token(numResultado, TipoToken.CONSTANTE_INTEIRA);

        }else if(tipo == 2){
            float resultado = 0;
            if(operador.equals("+"))
                resultado = Float.parseFloat(numOperando1) + Float.parseFloat(numOperando2);
            else if(operador.equals("-"))
                resultado = Float.parseFloat(numOperando1) - Float.parseFloat(numOperando2);
            else if(operador.equals("*"))
                resultado = Float.parseFloat(numOperando1) * Float.parseFloat(numOperando2);
            else if(operador.equals("/"))
                resultado = Float.parseFloat(numOperando1) / Float.parseFloat(numOperando2);

            numResultado = String.valueOf(resultado);

            resultadoToken = new Token(numResultado, TipoToken.CONSTANTE_FLOAT);

        }else if(tipo == 3){
            double resultado = 0;
            if(operador.equals("+"))
                resultado = Double.parseDouble(numOperando1) + Double.parseDouble(numOperando2);
            else if(operador.equals("-"))
                resultado = Double.parseDouble(numOperando1) - Double.parseDouble(numOperando2);
            else if(operador.equals("*"))
                resultado = Double.parseDouble(numOperando1) * Double.parseDouble(numOperando2);
            else if(operador.equals("/"))
                resultado = Double.parseDouble(numOperando1) / Double.parseDouble(numOperando2);

            numResultado = String.valueOf(resultado);

            resultadoToken = new Token(numResultado, TipoToken.CONSTANTE_DOUBLE);

        }else if(tipo == 0){
            char resultado = 0;
            if(operador.equals("+"))
                resultado = (char) (numOperando1.charAt(0) + numOperando2.charAt(0));
            else if(operador.equals("-"))
                resultado = (char) (numOperando1.charAt(0) - numOperando2.charAt(0));
            else if(operador.equals("*"))
                resultado = (char) (numOperando1.charAt(0) * numOperando2.charAt(0));
            else if(operador.equals("/"))
                resultado = (char) (numOperando1.charAt(0) / numOperando2.charAt(0));

            numResultado = String.valueOf(resultado);

            resultadoToken = new Token(numResultado, TipoToken.CONSTANTE_CHAR);
        }

        return resultadoToken;
    }

    public String getResultadoFinal(){
        // "Poda"
        if(!habilitada)
            return null;

        Token topo = aritmetica.pop();
        return getValor(topo);
    }

    public String getResultadoCondicao(){
        // "Poda"
        if(!habilitada)
            return null;

        return booleana.pop().getLexema();
    }

    public void habilitaMaquina(){
        this.habilitada = true;
    }

    public void desabilitaMaquina(){
        this.habilitada = false;
    }

    public boolean isHabilitada(){
        return this.habilitada;
    }
}
