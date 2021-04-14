package interpretador;
%%
%class Lexer
%{
    int linha = 1;

    public int getLinhaExecucao(){return this.linha;}

    public void setLinhaExecucao(int linha){this.linha = linha;}

    public int getPosicaoAtual(){return zzStartRead;}

    public void setPosicaoAtual(int pos){
       zzStartRead = pos;
       zzCurrentPos = pos;
       zzMarkedPos  = pos;
    }
%}
%eof{
    System.out.println("Arquivo interpretado com "+linha+" linhas");
    System.exit(0);
%eof}

//Especifica os alfabetos
LETRA = [a-zA-Z]
DIGITO = [0-9]
PONTO = \.
TIPO = "int" | "float" | "char" | "double"
OPERADOR_ARITMETICO = \+ | \- | \* | \/
PALAVRA_RESERVADA = "if" | "while" | "do" | "switch" | "else" | "return" | "for"
OPERADOR_LOGICO = "&&" | "||"
OPERADOR_RELACIONAL = ">" | "<" | "<=" | ">=" | "!=" | "=="
DOUBLE = {DIGITO}+ "." {DIGITO}* | {DIGITO}* "." {DIGITO}+

%%
//Linguagens e expressões regulares

//Quando uma virgula for encontrada, classifica-lo pelo TOKEN
\,								{ return new Token(yytext(), TipoToken.SEPARADOR_ARGUMENTO);}

//Quando um ponto e virgula for encontrada, classifica-lo pelo TOKEN
\;                              { return new Token(yytext(), TipoToken.FIM_COMANDO);}

//Quando um igual for encontrada, classifica-lo pelo TOKEN
\=                              { return new Token(yytext(), TipoToken.OPERADOR_ATRIBUICAO);}

//Quando uma chave aberta for encontrada, classifica-lo pelo TOKEN
\{                              { return new Token(yytext(), TipoToken.INICIO_ESCOPO);}

//Quando uma chave fechada for encontrada, classifica-lo pelo TOKEN
\}                              { return new Token(yytext(), TipoToken.FIM_ESCOPO);}

//Quando um parenteses fechado for encontrado, classifica-lo pelo TOKEN
\)                              { return new Token(yytext(), TipoToken.FECHA_PARENTESES);}

//Quando um parenteses aberto for encontrado, classifica-lo pelo TOKEN
\(                              { return new Token(yytext(), TipoToken.ABRE_PARENTESES);}

//Quando uma das palavras da linguagem for encontrada, classifica-lo pelo TOKEN
{TIPO}                          { return new Token(yytext(), TipoToken.TIPO_DADO);}

//Quando um operador aritmetico for encontrado, classifica-lo pelo TOKEN
{OPERADOR_ARITMETICO}           { return new Token(yytext(), TipoToken.OPERADOR_ARITMETICO);}

//Quando uma palavra reservada for encontrado, classifica-lo pelo TOKEN
{PALAVRA_RESERVADA}           { return new Token(yytext(), TipoToken.PALAVRA_RESERVADA);}

//Ao ler um identificador, classifica-lo pelo TOKEN
{LETRA}({LETRA} | {DIGITO})*    { return new Token(yytext(), TipoToken.IDENTIFICADOR);}

//Ao ler um operador logico, classifica-lo pelo TOKEN
{OPERADOR_LOGICO}				{ return new Token(yytext(), TipoToken.OPERADOR_LOGICO);}

//Ao ler um operador realacional, classifica-lo pelo TOKEN
{OPERADOR_RELACIONAL}			{ return new Token(yytext(), TipoToken.OPERADOR_RELACIONAL);}

//Ao ler um constante inteira, classifica-lo pelo TOKEN
{DIGITO}({DIGITO} | {DIGITO})*             { return new Token(yytext(), TipoToken.CONSTANTE_INTEIRA);}

//Ao ler um constante double, classifica-lo pelo TOKEN
{DIGITO}{DIGITO}*{PONTO}{DIGITO}{DIGITO}*        { return new Token(yytext(), TipoToken.CONSTANTE_FLOAT);}

//Ao ler um constante float, classifica-lo pelo TOKEN
{DOUBLE}                                                                     { return new Token(yytext(), TipoToken.CONSTANTE_DOUBLE);}

//Quanto um incremento for encontrado, classifica-lo pelo TOKEN
\++                      { return new Token(yytext(), TipoToken.INCREMENTO);}

//Quanto um decremento for encontrado, classifica-lo pelo TOKEN
\--                      { return new Token(yytext(), TipoToken.DECREMENTO);}

//Quanto um incremento e igual for encontrado, classifica-lo pelo TOKEN
\+=						 { return new Token(yytext(), TipoToken.INCREMENTO_IGUAL);}

//Quanto um decremento e igual for encontrado, classifica-lo pelo TOKEN
\-=						 { return new Token(yytext(), TipoToken.DECREMENTO_IGUAL);}

//Quando houver espaço em branco
\r                              {}

//Quando \n for lido, incrementar o contador a classe
\n                              {linha++;}

//Qualquer outro caractere lido, não faça nada
.                               {}