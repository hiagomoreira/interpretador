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

        //Inicializar o lookAhead. lookAhead obtém o próximo Token
        lookAhead = (Token) lexer.yylex();

        // Criar uma instância do analisador semântico.
        as = new AnalisadorSemantico();

        // Criar uma instância da máquina virtual.
        mv = new MaquinaVirtual(as);
    }

    public void match(TipoToken esperado){
        //Se o tipo do TOKEN esperado pela gramática foi o mesmo que foi lido
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

    //Não terminal "Programa"
    public void programa(){
        //Se caso o primeiro token não tiver LEXEMA igual a int, erro.
        if(!lookAhead.getLexema().equals("int"))
            erro("main deve ser do tipo int.");
        match(TipoToken.TIPO_DADO);

        //Se caso o segundo token não tiver LEXEMA igual a main, erro.
        if(!lookAhead.getLexema().equals("main"))
            erro("não foi encontrada a função \"main\"");
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
            erro("Está faltando a palavra reservada \"return\"");
        match(TipoToken.PALAVRA_RESERVADA);
        match(TipoToken.CONSTANTE_INTEIRA);
        match(TipoToken.FIM_COMANDO);
    }
    
    //inicio da seção de declarar as variaveis
    private void declaracao(){
        if(lookAhead.getTipo() == TipoToken.TIPO_DADO){

            String tipoDado = lookAhead.getLexema();
            match(TipoToken.TIPO_DADO);

            String variavelEsquerda = lookAhead.getLexema();

            // Avaliar se a variável NÃO foi previamente declarada.
            if(!as.verificaVariavelDuplicada(lookAhead))
                // Se não existir na tabela de símbolos, adicioná-la.
                as.insereVariavel(lookAhead, tipoDado);
            else
                erro("VARIAVEL "+lookAhead.getLexema()+ ", previamente declarada");
            match(TipoToken.IDENTIFICADOR);
            listaVar(tipoDado, variavelEsquerda);
        }
    }

    //Permite a declaração de mais que 1 variavel
    private void listaVar(String tipo, String variavelEsquerda){
        if (lookAhead.getTipo()==TipoToken.FIM_COMANDO){
            match(TipoToken.FIM_COMANDO);
            declaracao();
        }else if(lookAhead.getTipo()==TipoToken.SEPARADOR_ARGUMENTO){
            match(TipoToken.SEPARADOR_ARGUMENTO);
            // Avaliar se a variável NÃO foi previamente declarada.
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
                // Inicializar o tipo_expressão
                as.inicializaAvaliacaoTipo();

                expressao();

                // Aqui retorna o valor final da expressão aritmética. Atualizar a variável do lado esquerdo.
                String valorFinal = mv.getResultadoFinal();
                if(valorFinal != null)
                    as.atualizaValor(variavelEsquerda, valorFinal);


            }else if (TipoToken.CONSTANTE_INTEIRA == lookAhead.getTipo()){
                // Inicializar o tipo_expressão
                as.inicializaAvaliacaoTipo();

                expressao();

                // Aqui retorna o valor final da expressão aritmética. Atualizar a variável do lado esquerdo.
                String valorFinal = mv.getResultadoFinal();
                if(valorFinal != null)
                    as.atualizaValor(variavelEsquerda, valorFinal);

            }else if (TipoToken.CONSTANTE_FLOAT == lookAhead.getTipo()){
                // Inicializar o tipo_expressão
                as.inicializaAvaliacaoTipo();

                expressao();

                // Aqui retorna o valor final da expressão aritmética. Atualizar a variável do lado esquerdo.
                String valorFinal = mv.getResultadoFinal();
                if(valorFinal != null)
                    as.atualizaValor(variavelEsquerda, valorFinal);
            }else if (TipoToken.CONSTANTE_DOUBLE == lookAhead.getTipo()){
                // Inicializar o tipo_expressão
                as.inicializaAvaliacaoTipo();

                expressao();

                // Aqui retorna o valor final da expressão aritmética. Atualizar a variável do lado esquerdo.
                String valorFinal = mv.getResultadoFinal();
                if(valorFinal != null)
                    as.atualizaValor(variavelEsquerda, valorFinal);
            }
            listaVar(tipo, variavelEsquerda);
        }
    }

    //Permite a manipulação de dados e o uso de laços condicionais e loops
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

            // Inicializar o tipo_expressão
            as.inicializaAvaliacaoTipo();

            expressao();

            // Aqui retorna o valor final da expressão aritmética. Atualizar a variável do lado esquerdo.
            String valorFinal = mv.getResultadoFinal();
            if(valorFinal != null)
                as.atualizaValor(ladoEsquerdo.getLexema(), valorFinal);

            if(as.checarPerdaPrecisao(ladoEsquerdo))
                System.out.println("ALERTA: Perda de precisão na linha "+lexer.linha);
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

    //Permite a operação artimetica, vereficando os parentes e operação + e -
    private void expressao(){
        termo();
        while(lookAhead.getLexema().equals("+") || lookAhead.getLexema().equals("-")){
            // Empilha o operador + ou -.
            mv.empilha(lookAhead);
            match(TipoToken.OPERADOR_ARITMETICO);
            termo();
            // Ação semântica de calcular a expressão.
            mv.calcularExpressao();
        }
    }

    //Permite a operação artimetica, vereficando se a operação e * ou /
    private void termo(){
        fator();
        while(lookAhead.getLexema().equals("*") || lookAhead.getLexema().equals("/")){
            // Empilha o * ou /.
            mv.empilha(lookAhead);
            match(TipoToken.OPERADOR_ARITMETICO);
            fator();
            // Ação semântica de calcular a expressão.
            mv.calcularExpressao();
        }
    }

  //Permite a operação artimetica, vereficando se os identificadores são constantes inteiras ou float
    private void fator() {
        if(lookAhead.getTipo() == TipoToken.IDENTIFICADOR){
            // Aqui é avaliado o tipo do token encontrado!
            as.atualizaTipo(lookAhead);
            // Ação semântica de empilha o valor de um identificador.
            mv.empilha(lookAhead);

            //Verifica se a variavel ja existe
            verificaVariavelDeclarada(lookAhead);
        }
        else if	((lookAhead.getTipo() == TipoToken.CONSTANTE_INTEIRA) || (lookAhead.getTipo() == TipoToken.CONSTANTE_FLOAT || (lookAhead.getTipo() == TipoToken.CONSTANTE_DOUBLE))) {
            if (lookAhead.getTipo() == TipoToken.CONSTANTE_INTEIRA) {
                // Aqui é avaliado o tipo do token encontrado!
                as.atualizaTipo(lookAhead);
                // Ação semântica de empilha o valor de uma constante inteira.
                mv.empilha(lookAhead);
                match(TipoToken.CONSTANTE_INTEIRA);
            }
            if (lookAhead.getTipo() == TipoToken.CONSTANTE_FLOAT){
                // Aqui é avaliado o tipo do token encontrado!
                as.atualizaTipo(lookAhead);
                // Ação semântica de empilha o valor de uma constante inteira.
                mv.empilha(lookAhead);
                match(TipoToken.CONSTANTE_FLOAT);
            }if (lookAhead.getTipo() == TipoToken.CONSTANTE_DOUBLE){
                // Aqui é avaliado o tipo do token encontrado!
                as.atualizaTipo(lookAhead);
                // Ação semântica de empilha o valor de uma constante inteira.
                mv.empilha(lookAhead);
                match(TipoToken.CONSTANTE_DOUBLE);
            }
        }else{
            match(TipoToken.ABRE_PARENTESES);
            expressao();
            match(TipoToken.FECHA_PARENTESES);
        }
    }

    //Realiza as vereficação de consdições, repetições e loops
    private void comandoBloco(){
    	//verefica se são if
        if(lookAhead.getLexema().equals("if")) {
            match(TipoToken.PALAVRA_RESERVADA);
            match(TipoToken.ABRE_PARENTESES);
            condicao();

            // Verificar se a condiçãoo é falsa
            String valor = mv.getResultadoCondicao();

            if (valor != null)
                if (valor.equals("false") && mv.isHabilitada())
                    mv.desabilitaMaquina();
            match(TipoToken.FECHA_PARENTESES);
            match(TipoToken.INICIO_ESCOPO);
            corpoCMD();

            //Se não rodou o IF, entra aqui e habilita a maquina
            if (!mv.isHabilitada() && valor != null) {
                mv.habilitaMaquina();
                match(TipoToken.FIM_ESCOPO);

                //Procura por else, se achar executa o bloco
                if(lookAhead.getLexema().equals("else")){
                    match(TipoToken.PALAVRA_RESERVADA);

                    //Verifica se é um else if ou não
                    if(lookAhead.getLexema().equals("if")){
                        comandoBloco();
                    }else{ //Senão será apenas o else que será executado
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
                    //Verifica se é um else if ou não
                    if(lookAhead.getLexema().equals("if")){
                        comandoBloco();
                    }else{ //Senão será apenas o else que será executado
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

            // Ação semântica - Antes de ler o próximo token, valor marcar esta posicao no buffer
            int posicao = lexer.getPosicaoAtual();
            match(TipoToken.PALAVRA_RESERVADA);
            match(TipoToken.ABRE_PARENTESES);
            condicao();
            // Verificar se a condição é falsa
            String valor = mv.getResultadoCondicao();

            if(valor != null)
                if(valor.equals("false") && mv.isHabilitada())
                    mv.desabilitaMaquina();

            match(TipoToken.FECHA_PARENTESES);
            match(TipoToken.INICIO_ESCOPO);

            corpoCMD();

            // Se a condição é falsa, passa reto...
            if(!mv.isHabilitada() && valor != null)
                mv.habilitaMaquina();
            else if(valor != null) {
                // Se a condição é verdadeira, retorna para a posição do buffer demarcada anteriormente.
                lexer.setPosicaoAtual(posicao);
                lexer.setLinhaExecucao(lexer.getLinhaExecucao()-(lexer.getLinhaExecucao()-linhaExecutada));
            }
            match(TipoToken.FIM_ESCOPO);

        //Verefica se é um do-while
        } else if(lookAhead.getLexema().equals("do")){
            //Pega linha de execucao
            int linhaExecutada = lexer.getLinhaExecucao();

            // salvar a posição do buffer
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

            //Salva posição no buffer
            int posicao = lexer.getPosicaoAtual();

            match(TipoToken.PALAVRA_RESERVADA);
            match(TipoToken.ABRE_PARENTESES);

            Token variavelEsquerda = null;

            if (!this.condicaoFOR) {
                if (lookAhead.getTipo() == TipoToken.IDENTIFICADOR) {
                    variavelEsquerda = lookAhead;
                    verificaVariavelDeclarada(lookAhead);
                    match(TipoToken.OPERADOR_ATRIBUICAO);

                    // Inicializar o tipo_expressão
                    as.inicializaAvaliacaoTipo();

                    expressao();

                    // Aqui retorna o valor final da expressão aritmética. Atualizar a variável do lado esquerdo.
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
            // Empilha o operador Lógico
            mv.empilhaBooleana(lookAhead);
            match(TipoToken.OPERADOR_LOGICO);
            expressaoRelacional();
            // Calcular a expressão.
            mv.avaliarCondicao();
        }
    }

    private void expressaoRelacional(){
        expressao();
        // Empilhar o topo da pilha de expressões aritméticas.
        mv.empilhaBooleana();
        while(lookAhead.getTipo() == TipoToken.OPERADOR_RELACIONAL){
            // Empilha o operador Relacional.
            mv.empilhaBooleana(lookAhead);
            match(TipoToken.OPERADOR_RELACIONAL);
            expressao();
            // Empilhar o topo da pilha de expressões aritméticas.
            mv.empilhaBooleana();
            // Calcular a expressão.
            mv.avaliarCondicao();
        }
    }

    private void verificaVariavelDeclarada(Token lookAhead){
        // Avaliar se a variável foi previamente declarada.
        if (!as.verificaNaoVariavelDeclarada(lookAhead))
            match(TipoToken.IDENTIFICADOR);
        else
            erro("VARIAVEL " + lookAhead.getLexema() + ", não foi declarada");
    }
}