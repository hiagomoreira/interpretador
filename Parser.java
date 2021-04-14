package interpretador;

import java.io.FileReader;
import java.io.IOException;

public class Parser {

    private Token lookAhead;
    private Lexer lexer;
    private AnalisadorSemantico as;
    private MaquinaVirtual mv;
    private boolean condicaoFOR = false;

    public Parser(String arquivoFonte) throws IOException {
        //Instanciar um Lexer
        lexer = new Lexer(new FileReader(arquivoFonte));

        //Inicializar o lookAhead. lookAhead obt�m o pr�ximo Token
        lookAhead = (Token) lexer.yylex();

        // Criar uma inst�ncia do analisador sem�ntico.
        as = new AnalisadorSemantico();

        // Criar uma inst�ncia da m�quina virtual.
        mv = new MaquinaVirtual(as);
    }

    public void match(TipoToken esperado){
        //Se o tipo do TOKEN esperado pela gram�tica foi o mesmo que foi lido
        if(esperado == lookAhead.getTipo()){
            //Continua
            try{
                lookAhead = (Token) lexer.yylex();
            }catch (IOException ex){
                System.out.println("Erro ao ler o arquivo!");
            }
        }else{
            erro("esperado: "+String.valueOf(esperado)+" lido: "+String.valueOf(lookAhead.getTipo()));
        }
    }

    public void erro(String msg){
        System.out.println("Erro na linha "+lexer.linha+": "+msg);
        System.exit(0);
    }

    //N�o terminal "Programa"
    public void programa(){
        //Se caso o primeiro token n�o tiver LEXEMA igual a int, erro.
        if(!lookAhead.getLexema().equals("int"))
            erro("main deve ser do tipo int.");
        match(TipoToken.TIPO_DADO);

        //Se caso o segundo token n�o tiver LEXEMA igual a main, erro.
        if(!lookAhead.getLexema().equals("main"))
            erro("n�o foi encontrada a fun��o \"main\"");
        match(TipoToken.IDENTIFICADOR);

        match(TipoToken.ABRE_PARENTESES);
        match(TipoToken.FECHA_PARENTESES);

        match(TipoToken.INICIO_ESCOPO);
        corpo();
        System.out.println(as);
        match(TipoToken.FIM_ESCOPO);
    }

    private void corpo(){
        declaracao();
        corpoCMD();

        //Avaliar o return "0"
        if(!lookAhead.getLexema().equals("return"))
            erro("Est� faltando a palavra reservada \"return\"");
        match(TipoToken.PALAVRA_RESERVADA);
        match(TipoToken.CONSTANTE_INTEIRA);
        match(TipoToken.FIM_COMANDO);
    }
    
    //inicio da se��o de declarar as variaveis
    private void declaracao(){
        if(lookAhead.getTipo() == TipoToken.TIPO_DADO){

            String tipoDado = lookAhead.getLexema();
            match(TipoToken.TIPO_DADO);

            String variavelEsquerda = lookAhead.getLexema();

            // Avaliar se a vari�vel N�O foi previamente declarada.
            if(!as.verificaVariavelDuplicada(lookAhead))
                // Se n�o existir na tabela de s�mbolos, adicion�-la.
                as.insereVariavel(lookAhead, tipoDado);
            else
                erro("VARIAVEL "+lookAhead.getLexema()+ ", previamente declarada");
            match(TipoToken.IDENTIFICADOR);
            listaVar(tipoDado, variavelEsquerda);
        }
    }

    //Permite a declara��o de mais que 1 variavel
    private void listaVar(String tipo, String variavelEsquerda){
        if (lookAhead.getTipo()==TipoToken.FIM_COMANDO){
            match(TipoToken.FIM_COMANDO);
            declaracao();
        }else if(lookAhead.getTipo()==TipoToken.SEPARADOR_ARGUMENTO){
            match(TipoToken.SEPARADOR_ARGUMENTO);
            // Avaliar se a vari�vel N�O foi previamente declarada.
            if(!as.verificaVariavelDuplicada(lookAhead))
                as.insereVariavel(lookAhead, tipo);
            else
                erro("VARIAVEL "+lookAhead.getLexema()+ ", previamente declarada");
            String novaV = lookAhead.getLexema();
            match(TipoToken.IDENTIFICADOR);
            listaVar(tipo, novaV);
        }else if (lookAhead.getTipo()==TipoToken.OPERADOR_ATRIBUICAO){
            match(TipoToken.OPERADOR_ATRIBUICAO);

            if(TipoToken.IDENTIFICADOR == lookAhead.getTipo()){
                // Inicializar o tipo_express�o
                as.inicializaAvaliacaoTipo();

                expressao();

                // Aqui retorna o valor final da express�o aritm�tica. Atualizar a vari�vel do lado esquerdo.
                String valorFinal = mv.getResultadoFinal();
                if(valorFinal != null)
                    as.atualizaValor(variavelEsquerda, valorFinal);


            }else if (TipoToken.CONSTANTE_INTEIRA == lookAhead.getTipo()){
                // Inicializar o tipo_express�o
                as.inicializaAvaliacaoTipo();

                expressao();

                // Aqui retorna o valor final da express�o aritm�tica. Atualizar a vari�vel do lado esquerdo.
                String valorFinal = mv.getResultadoFinal();
                if(valorFinal != null)
                    as.atualizaValor(variavelEsquerda, valorFinal);

            }else if (TipoToken.CONSTANTE_FLOAT == lookAhead.getTipo()){
                // Inicializar o tipo_express�o
                as.inicializaAvaliacaoTipo();

                expressao();

                // Aqui retorna o valor final da express�o aritm�tica. Atualizar a vari�vel do lado esquerdo.
                String valorFinal = mv.getResultadoFinal();
                if(valorFinal != null)
                    as.atualizaValor(variavelEsquerda, valorFinal);
            }else if (TipoToken.CONSTANTE_DOUBLE == lookAhead.getTipo()){
                // Inicializar o tipo_express�o
                as.inicializaAvaliacaoTipo();

                expressao();

                // Aqui retorna o valor final da express�o aritm�tica. Atualizar a vari�vel do lado esquerdo.
                String valorFinal = mv.getResultadoFinal();
                if(valorFinal != null)
                    as.atualizaValor(variavelEsquerda, valorFinal);
            }
            listaVar(tipo, variavelEsquerda);
        }
    }

    //Permite a manipula��o de dados e o uso de la�os condicionais e loops
    private void corpoCMD(){
        declaracao();

        if(lookAhead.getTipo() == TipoToken.IDENTIFICADOR) {
            Token ladoEsquerdo = lookAhead;

            //Verifica se a variavel ja existe
            verificaVariavelDeclarada(lookAhead);

            if (lookAhead.getTipo() == TipoToken.INCREMENTO){
                match(TipoToken.INCREMENTO);
                match(TipoToken.FIM_COMANDO);
                corpoCMD();
            }else if(lookAhead.getTipo() == TipoToken.DECREMENTO){
                match(TipoToken.DECREMENTO);
                match(TipoToken.FIM_COMANDO);
                corpoCMD();
            }else if(lookAhead.getTipo() == TipoToken.OPERADOR_ATRIBUICAO){
                match(TipoToken.OPERADOR_ATRIBUICAO);

            }else if(lookAhead.getTipo() == TipoToken.DECREMENTO_IGUAL) {
            	match(TipoToken.DECREMENTO_IGUAL);

            }else if(lookAhead.getTipo() == TipoToken.INCREMENTO_IGUAL) {
            	match(TipoToken.INCREMENTO_IGUAL);
            }

            // Inicializar o tipo_express�o
            as.inicializaAvaliacaoTipo();

            expressao();

            // Aqui retorna o valor final da express�o aritm�tica. Atualizar a vari�vel do lado esquerdo.
            String valorFinal = mv.getResultadoFinal();
            if(valorFinal != null)
                as.atualizaValor(ladoEsquerdo.getLexema(), valorFinal);

            if(as.checarPerdaPrecisao(ladoEsquerdo))
                System.out.println("ALERTA: Perda de precis�o na linha "+lexer.linha);
            match(TipoToken.FIM_COMANDO);
            corpoCMD();


        }else if(lookAhead.getTipo() == TipoToken.INCREMENTO){
            match(TipoToken.INCREMENTO);

            //Verifica se a variavel ja existe
            verificaVariavelDeclarada(lookAhead);

            match(TipoToken.FIM_COMANDO);
            corpoCMD();
        }else if(lookAhead.getTipo() == TipoToken.DECREMENTO){
            match(TipoToken.DECREMENTO);

            //Verifica se a variavel ja existe
            verificaVariavelDeclarada(lookAhead);

            match(TipoToken.FIM_COMANDO);
            corpoCMD();
        }else if(lookAhead.getTipo()==TipoToken.PALAVRA_RESERVADA && !lookAhead.getLexema().equals("return")){
            comandoBloco();
            corpoCMD();
        }
    }

    //Permite a opera��o artimetica, vereficando os parentes e opera��o + e -
    private void expressao(){
        termo();
        while(lookAhead.getLexema().equals("+") || lookAhead.getLexema().equals("-")){
            // Empilha o operador + ou -.
            mv.empilha(lookAhead);
            match(TipoToken.OPERADOR_ARITMETICO);
            termo();
            // A��o sem�ntica de calcular a express�o.
            mv.calcularExpressao();
        }
    }

    //Permite a opera��o artimetica, vereficando se a opera��o e * ou /
    private void termo(){
        fator();
        while(lookAhead.getLexema().equals("*") || lookAhead.getLexema().equals("/")){
            // Empilha o * ou /.
            mv.empilha(lookAhead);
            match(TipoToken.OPERADOR_ARITMETICO);
            fator();
            // A��o sem�ntica de calcular a express�o.
            mv.calcularExpressao();
        }
    }

  //Permite a opera��o artimetica, vereficando se os identificadores s�o constantes inteiras ou float
    private void fator() {
        if(lookAhead.getTipo() == TipoToken.IDENTIFICADOR){
            // Aqui � avaliado o tipo do token encontrado!
            as.atualizaTipo(lookAhead);
            // A��o sem�ntica de empilha o valor de um identificador.
            mv.empilha(lookAhead);

            //Verifica se a variavel ja existe
            verificaVariavelDeclarada(lookAhead);
        }
        else if	((lookAhead.getTipo() == TipoToken.CONSTANTE_INTEIRA) || (lookAhead.getTipo() == TipoToken.CONSTANTE_FLOAT || (lookAhead.getTipo() == TipoToken.CONSTANTE_DOUBLE))) {
            if (lookAhead.getTipo() == TipoToken.CONSTANTE_INTEIRA) {
                // Aqui � avaliado o tipo do token encontrado!
                as.atualizaTipo(lookAhead);
                // A��o sem�ntica de empilha o valor de uma constante inteira.
                mv.empilha(lookAhead);
                match(TipoToken.CONSTANTE_INTEIRA);
            }
            if (lookAhead.getTipo() == TipoToken.CONSTANTE_FLOAT){
                // Aqui � avaliado o tipo do token encontrado!
                as.atualizaTipo(lookAhead);
                // A��o sem�ntica de empilha o valor de uma constante inteira.
                mv.empilha(lookAhead);
                match(TipoToken.CONSTANTE_FLOAT);
            }if (lookAhead.getTipo() == TipoToken.CONSTANTE_DOUBLE){
                // Aqui � avaliado o tipo do token encontrado!
                as.atualizaTipo(lookAhead);
                // A��o sem�ntica de empilha o valor de uma constante inteira.
                mv.empilha(lookAhead);
                match(TipoToken.CONSTANTE_DOUBLE);
            }
        }else{
            match(TipoToken.ABRE_PARENTESES);
            expressao();
            match(TipoToken.FECHA_PARENTESES);
        }
    }

    //Realiza as verefica��o de consdi��es, repeti��es e loops
    private void comandoBloco(){
    	//verefica se s�o if
        if(lookAhead.getLexema().equals("if")) {
            match(TipoToken.PALAVRA_RESERVADA);
            match(TipoToken.ABRE_PARENTESES);
            condicao();

            // Verificar se a condi��oo � falsa
            String valor = mv.getResultadoCondicao();

            if (valor != null)
                if (valor.equals("false") && mv.isHabilitada())
                    mv.desabilitaMaquina();
            match(TipoToken.FECHA_PARENTESES);
            match(TipoToken.INICIO_ESCOPO);
            corpoCMD();

            //Se n�o rodou o IF, entra aqui e habilita a maquina
            if (!mv.isHabilitada() && valor != null) {
                mv.habilitaMaquina();
                match(TipoToken.FIM_ESCOPO);

                //Procura por else, se achar executa o bloco
                if(lookAhead.getLexema().equals("else")){
                    match(TipoToken.PALAVRA_RESERVADA);

                    //Verifica se � um else if ou n�o
                    if(lookAhead.getLexema().equals("if")){
                        comandoBloco();
                    }else{ //Sen�o ser� apenas o else que ser� executado
                        match(TipoToken.INICIO_ESCOPO);
                        corpoCMD();
                        match(TipoToken.FIM_ESCOPO);
                    }
                }
            }else{
                if(lookAhead.getTipo() == TipoToken.FIM_ESCOPO)
                    match(TipoToken.FIM_ESCOPO);

                //Procura por else, se achar executa o bloco
                if(lookAhead.getLexema().equals("else")){
                    match(TipoToken.PALAVRA_RESERVADA);
                    mv.desabilitaMaquina();
                    //Verifica se � um else if ou n�o
                    if(lookAhead.getLexema().equals("if")){
                        comandoBloco();
                    }else{ //Sen�o ser� apenas o else que ser� executado
                        match(TipoToken.INICIO_ESCOPO);
                        corpoCMD();
                        match(TipoToken.FIM_ESCOPO);
                    }
                }
            }

            if(!mv.isHabilitada() && valor != null)
                mv.habilitaMaquina();

        }else if(lookAhead.getLexema().equals("while")){
            //Pega linha de execucao
            int linhaExecutada = lexer.getLinhaExecucao();

            // A��o sem�ntica - Antes de ler o pr�ximo token, valor marcar esta posicao no buffer
            int posicao = lexer.getPosicaoAtual();
            match(TipoToken.PALAVRA_RESERVADA);
            match(TipoToken.ABRE_PARENTESES);
            condicao();
            // Verificar se a condi��o � falsa
            String valor = mv.getResultadoCondicao();

            if(valor != null)
                if(valor.equals("false") && mv.isHabilitada())
                    mv.desabilitaMaquina();

            match(TipoToken.FECHA_PARENTESES);
            match(TipoToken.INICIO_ESCOPO);

            corpoCMD();

            // Se a condi��o � falsa, passa reto...
            if(!mv.isHabilitada() && valor != null)
                mv.habilitaMaquina();
            else if(valor != null) {
                // Se a condi��o � verdadeira, retorna para a posi��o do buffer demarcada anteriormente.
                lexer.setPosicaoAtual(posicao);
                lexer.setLinhaExecucao(lexer.getLinhaExecucao()-(lexer.getLinhaExecucao()-linhaExecutada));
            }
            match(TipoToken.FIM_ESCOPO);

        //Verefica se � um do-while
        } else if(lookAhead.getLexema().equals("do")){
            //Pega linha de execucao
            int linhaExecutada = lexer.getLinhaExecucao();

            // salvar a posi��o do buffer
            int posicao = lexer.getPosicaoAtual();
            match(TipoToken.PALAVRA_RESERVADA);
            match(TipoToken.INICIO_ESCOPO);
            corpoCMD();
            match(TipoToken.FIM_ESCOPO);
            match(TipoToken.PALAVRA_RESERVADA);
            match(TipoToken.ABRE_PARENTESES);
            condicao();

            match(TipoToken.FECHA_PARENTESES);

            String resultado = mv.getResultadoCondicao();
            if(resultado != null){
                if(resultado.equals("true")) {
                    lexer.setPosicaoAtual(posicao);
                    lexer.setLinhaExecucao(lexer.getLinhaExecucao()-(lexer.getLinhaExecucao()-linhaExecutada));
                }
                if(lookAhead.getTipo() == TipoToken.FIM_COMANDO)
                    match(TipoToken.FIM_COMANDO);
            }

            //Verefica se e um for
        } else if(lookAhead.getLexema().equals("for")) { //for(int e = 2; e < 10; e++)
            //Pega linha de execucao
            int linhaExecutada = lexer.getLinhaExecucao();

            //Salva posi��o no buffer
            int posicao = lexer.getPosicaoAtual();

            match(TipoToken.PALAVRA_RESERVADA);
            match(TipoToken.ABRE_PARENTESES);

            Token variavelEsquerda = null;

            if (!this.condicaoFOR) {
                if (lookAhead.getTipo() == TipoToken.IDENTIFICADOR) {
                    variavelEsquerda = lookAhead;
                    verificaVariavelDeclarada(lookAhead);
                    match(TipoToken.OPERADOR_ATRIBUICAO);

                    // Inicializar o tipo_express�o
                    as.inicializaAvaliacaoTipo();

                    expressao();

                    // Aqui retorna o valor final da express�o aritm�tica. Atualizar a vari�vel do lado esquerdo.
                    String valorFinal = mv.getResultadoFinal();
                    if (valorFinal != null)
                        as.atualizaValor(variavelEsquerda.getLexema(), valorFinal);

                    match(TipoToken.FIM_COMANDO);
                }else{
                    declaracao();
                }
            }else {
                if (lookAhead.getTipo() == TipoToken.TIPO_DADO)
                    match(TipoToken.TIPO_DADO);

                if (lookAhead.getTipo() == TipoToken.IDENTIFICADOR) {
                    verificaVariavelDeclarada(lookAhead);
                    match(TipoToken.OPERADOR_ATRIBUICAO);
                    expressao();
                    match(TipoToken.FIM_COMANDO);
                }
            }
            condicao();
            String resultado = mv.getResultadoCondicao();
            if(resultado != null){
                if(resultado.equals("false") && mv.isHabilitada())
                    mv.desabilitaMaquina();
            }
            match(TipoToken.FIM_COMANDO);
            Token aux = lookAhead;
            match(TipoToken.IDENTIFICADOR);
            Token aux1 = lookAhead;
            if(lookAhead.getTipo() == TipoToken.INCREMENTO)
                match(TipoToken.INCREMENTO);
            else if (lookAhead.getTipo() == TipoToken.DECREMENTO)
                match(TipoToken.DECREMENTO);
            else
                erro("Falta operador de incremento/decremento");

            match(TipoToken.FECHA_PARENTESES);
            match(TipoToken.INICIO_ESCOPO);
            corpoCMD();

            if(!mv.isHabilitada() && resultado != null){
                mv.habilitaMaquina();
                condicaoFOR = false;

            }else if(resultado != null) {
                if(aux1.getTipo() == TipoToken.INCREMENTO)
                    as.atualizaValor(aux.getLexema(), String.valueOf(Integer.parseInt((as.getValor(aux.getLexema())))+1));
                else if (aux1.getTipo() == TipoToken.DECREMENTO)
                    as.atualizaValor(aux.getLexema(), String.valueOf(Integer.parseInt((as.getValor(aux.getLexema())))-1));

                condicaoFOR = true;
                lexer.setPosicaoAtual(posicao);
                lexer.setLinhaExecucao(lexer.getLinhaExecucao()-(lexer.getLinhaExecucao()-linhaExecutada));
            }
            match(TipoToken.FIM_ESCOPO);
		}
    }

    private void condicao(){
        expressaoRelacional();
        while(lookAhead.getTipo() == TipoToken.OPERADOR_LOGICO){
            // Empilha o operador L�gico
            mv.empilhaBooleana(lookAhead);
            match(TipoToken.OPERADOR_LOGICO);
            expressaoRelacional();
            // Calcular a express�o.
            mv.avaliarCondicao();
        }
    }

    private void expressaoRelacional(){
        expressao();
        // Empilhar o topo da pilha de express�es aritm�ticas.
        mv.empilhaBooleana();
        while(lookAhead.getTipo() == TipoToken.OPERADOR_RELACIONAL){
            // Empilha o operador Relacional.
            mv.empilhaBooleana(lookAhead);
            match(TipoToken.OPERADOR_RELACIONAL);
            expressao();
            // Empilhar o topo da pilha de express�es aritm�ticas.
            mv.empilhaBooleana();
            // Calcular a express�o.
            mv.avaliarCondicao();
        }
    }

    private void verificaVariavelDeclarada(Token lookAhead){
        // Avaliar se a vari�vel foi previamente declarada.
        if (!as.verificaNaoVariavelDeclarada(lookAhead))
            match(TipoToken.IDENTIFICADOR);
        else
            erro("VARIAVEL " + lookAhead.getLexema() + ", n�o foi declarada");
    }
}