/*
 *  The scanner definition for COOL.
 *
 *  ESTRUTURA DO ARQUIVO:
 *  Secao 1 (antes do primeiro %%): imports Java
 *  Secao 2 (entre os dois %%): declaracoes JLex (%class, %state, blocos %{ %}, etc.)
 *  Secao 3 (apos o segundo %%): regras lexicas (regex + acao Java)
 *
 *  IMPORTANTE SOBRE COMENTARIOS NO JLEX:
 *  - Na secao 1: comentarios de bloco e de linha funcionam normalmente (e codigo Java)
 *  - Na secao 2: comentarios so funcionam DENTRO de blocos %{ %}, %init{}, %eofval{}
 *  - Na secao 3: comentarios so funcionam DENTRO das acoes { } das regras
 *    Comentarios soltos nas secoes 2 e 3 causam erros de parse no JLex.
 *
 *  Symbol e a classe do java_cup que representa um token.
 *  Cada token retornado pelo lexer e um Symbol(tipo) ou Symbol(tipo, valor).
 */
import java_cup.runtime.Symbol;

%%

%{
/*
 * Tudo dentro de %{ %} e copiado literalmente para dentro da classe CoolLexer gerada.
 * Aqui ficam variaveis de instancia e metodos auxiliares do lexer.
 *
 * MAX_STR_CONST: tamanho maximo de uma string constante (bytes, com terminador \0).
 *   Strings com mais de 1024 chars sao erro lexico em Cool.
 *
 * string_buf: buffer para montar o conteudo de uma string char por char
 *   dentro do estado STRING. StringBuffer e mutavel, por isso e usado aqui.
 *
 * curr_lineno: numero da linha atual. Comeca em 1, incrementado a cada \n.
 *   Usado pelo parser para reportar erros com numero de linha correto.
 *
 * filename: nome do arquivo sendo processado, como AbstractSymbol
 *   (entrada na tabela de strings) para economizar memoria.
 *
 * comment_depth: nivel de aninhamento de comentarios de bloco (* ... *).
 *   Cool permite comentarios aninhados: (* (* *) *) e valido.
 *   Abre "(*" -> depth++. Fecha "*)" -> depth--. Chega a 0 -> saiu do comentario.
 *
 * string_too_long: flag que indica que a string atual ja passou de 1024 chars.
 *   Quando true, continuamos lendo ate fechar a string, mas retornamos erro.
 *   Isso evita erros em cascata.
 *
 * append_char: tenta adicionar um char ao buffer da string.
 *   Se ja atingiu o limite, ativa string_too_long em vez de adicionar.
 */

    static int MAX_STR_CONST = 1025;

    StringBuffer string_buf = new StringBuffer();

    private int curr_lineno = 1;
    int get_curr_lineno() {
	return curr_lineno;
    }

    private AbstractSymbol filename;

    void set_filename(String fname) {
	filename = AbstractTable.stringtable.addString(fname);
    }

    AbstractSymbol curr_filename() {
	return filename;
    }

    private int comment_depth = 0;

    private boolean string_too_long = false;

    private void append_char(char c) {
        if (string_buf.length() >= MAX_STR_CONST - 1) {
            string_too_long = true;
        } else {
            string_buf.append(c);
        }
    }
%}

%init{
/*
 * Tudo dentro de %init{ %init} e copiado para o construtor da classe CoolLexer.
 * Inicializacoes extras do lexer viriam aqui.
 */
    // empty for now
%init}

%eofval{
/*
 * Tudo dentro de %eofval{ %eofval} e executado quando o lexer atinge o EOF.
 * Verificamos em qual estado estamos para retornar o erro correto.
 * yy_lexical_state e uma variavel interna do JLex com o estado atual.
 *
 * YYINITIAL: EOF normal, apenas encerra.
 * COMMENT:   EOF dentro de comentario de bloco — comentario nunca foi fechado.
 * STRING:    EOF dentro de string — string nunca foi fechada com ".
 * STRING_ERROR: EOF apos erro de string — ja reportamos o erro antes, apenas encerra.
 *
 * Em qualquer caso, ao fim retornamos EOF para sinalizar ao parser.
 */
    switch(yy_lexical_state) {
    case YYINITIAL:
	break;
    case COMMENT:
        yybegin(YYINITIAL);
        return new Symbol(TokenConstants.ERROR, "EOF in comment");
    case STRING:
        yybegin(YYINITIAL);
        return new Symbol(TokenConstants.ERROR, "EOF in string constant");
    case STRING_ERROR:
        yybegin(YYINITIAL);
        break;
    }
    return new Symbol(TokenConstants.EOF);
%eofval}

%class CoolLexer
%cup

%state COMMENT
%state STRING
%state STRING_ERROR

%%

<YYINITIAL>[ \t\f\r]+  { /* Whitespace (espaco, tab, form feed, carriage return): ignorado silenciosamente. */ }
<YYINITIAL>\n           { curr_lineno++; /* Newline: nao retorna token, apenas incrementa o contador de linhas. */ }

<YYINITIAL>"--"[^\n]*  { /* Comentario de linha: "--" seguido de tudo ate o fim da linha. [^\n]* = zero ou mais chars que nao sao newline. O \n em si nao e consumido aqui — a regra de \n acima cuida dele. */ }

<YYINITIAL>"(*"         { comment_depth = 1; yybegin(COMMENT); /* Inicia comentario de bloco. Profundidade comeca em 1. Muda para estado COMMENT. */ }
<YYINITIAL>"*)"         { return new Symbol(TokenConstants.ERROR, "Unmatched *)"); /* "*)" fora de comentario: erro lexico. */ }

<COMMENT>"(*"           { comment_depth++; /* Comentario aninhado: incrementa a profundidade. Cool suporta aninhamento. */ }
<COMMENT>"*)"           { if (--comment_depth == 0) yybegin(YYINITIAL); /* Fecha um nivel. Se chegou a 0, voltamos ao estado normal. */ }
<COMMENT>\n             { curr_lineno++; /* Conta linhas mesmo dentro de comentarios. */ }
<COMMENT>.              { /* Qualquer outro char dentro do comentario e ignorado. "." casa qualquer char exceto \n. */ }

<YYINITIAL>\"           { string_buf.setLength(0); string_too_long = false; yybegin(STRING); /* Abre uma string: limpa o buffer, reseta flag de erro, entra no estado STRING para ler char a char. */ }

<STRING>\"              { /* Fecha a string com ".
                           * Se estava muito longa: retorna erro.
                           * Caso contrario: adiciona a string a tabela stringtable e retorna STR_CONST.
                           * AbstractTable.stringtable faz "interning": strings iguais compartilham o mesmo objeto na memoria. */
                          if (string_too_long) {
                              yybegin(YYINITIAL);
                              return new Symbol(TokenConstants.ERROR, "String constant too long");
                          }
                          yybegin(YYINITIAL);
                          return new Symbol(TokenConstants.STR_CONST,
                              AbstractTable.stringtable.addString(string_buf.toString())); }

<STRING>\\(\r?\n)            { curr_lineno++; append_char('\n'); /* Escape de newline: "\" seguido de \n real. Permite strings multi-linha com \ no final da linha. Conta a linha e adiciona \n ao conteudo. */ }
<STRING>\\b             { append_char('\b'); /* \b -> backspace */ }
<STRING>\\t             { append_char('\t'); /* \t -> tab */ }
<STRING>\\n             { append_char((char)10); /* \n (escapado) -> newline. Diferente do \n real, que e erro de string nao terminada. */ }
<STRING>\\f             { append_char('\f'); /* \f -> form feed */ }
<STRING>\\0             { append_char('0');  /* \0 escapado -> char '0' (nao null byte). Comportamento definido pela spec do Cool. */ }
<STRING>\\.             { append_char(yytext().charAt(1)); /* Qualquer outro escape \x -> o proprio char x. Ex: \a->'a', \\->'\\'. yytext().charAt(1) pega o char apos a barra. */ }
<STRING>\n              { curr_lineno++; yybegin(YYINITIAL);
                          if (string_too_long) {
                              return new Symbol(TokenConstants.ERROR, "String constant too long");
                          }
                          return new Symbol(TokenConstants.ERROR, "Unterminated string constant"); /* Newline real sem escape dentro de string: erro. A string nao foi fechada corretamente. */ }
<STRING>\0              { yybegin(STRING_ERROR);
                          return new Symbol(TokenConstants.ERROR, "String contains null character"); /* Null byte real dentro de string: erro. Muda para STRING_ERROR para consumir o resto sem reportar novos erros. */ }
<STRING>.               { append_char(yytext().charAt(0)); /* Char normal: adiciona ao buffer. yytext().charAt(0) pega o unico char casado pelo ".". */ }

<STRING_ERROR>\"        { yybegin(YYINITIAL); /* Fecha a string com erro: volta ao estado normal sem retornar token. */ }
<STRING_ERROR>\n        { curr_lineno++; yybegin(YYINITIAL); /* Newline tambem encerra a string com erro. */ }
<STRING_ERROR>.         { /* Qualquer outro char apos o erro na string e ignorado silenciosamente. */ }

<YYINITIAL>[cC][lL][aA][sS][sS]              { return new Symbol(TokenConstants.CLASS);    /* Keywords: case-insensitive em Cool. [cC] casa 'c' ou 'C', etc */ }
<YYINITIAL>[eE][lL][sS][eE]                  { return new Symbol(TokenConstants.ELSE); }
<YYINITIAL>[fF][iI]                          { return new Symbol(TokenConstants.FI); }
<YYINITIAL>[iI][fF]                          { return new Symbol(TokenConstants.IF); }
<YYINITIAL>[iI][nN]                          { return new Symbol(TokenConstants.IN); }
<YYINITIAL>[iI][nN][hH][eE][rR][iI][tT][sS] { return new Symbol(TokenConstants.INHERITS); }
<YYINITIAL>[iI][sS][vV][oO][iI][dD]         { return new Symbol(TokenConstants.ISVOID); }
<YYINITIAL>[lL][eE][tT]                      { return new Symbol(TokenConstants.LET); }
<YYINITIAL>[lL][oO][oO][pP]                  { return new Symbol(TokenConstants.LOOP); }
<YYINITIAL>[pP][oO][oO][lL]                  { return new Symbol(TokenConstants.POOL); }
<YYINITIAL>[tT][hH][eE][nN]                  { return new Symbol(TokenConstants.THEN); }
<YYINITIAL>[wW][hH][iI][lL][eE]             { return new Symbol(TokenConstants.WHILE); }
<YYINITIAL>[cC][aA][sS][eE]                  { return new Symbol(TokenConstants.CASE); }
<YYINITIAL>[eE][sS][aA][cC]                  { return new Symbol(TokenConstants.ESAC); }
<YYINITIAL>[nN][eE][wW]                      { return new Symbol(TokenConstants.NEW); }
<YYINITIAL>[oO][fF]                          { return new Symbol(TokenConstants.OF); }
<YYINITIAL>[nN][oO][tT]                      { return new Symbol(TokenConstants.NOT); }

<YYINITIAL>t[rR][uU][eE]      { return new Symbol(TokenConstants.BOOL_CONST, Boolean.TRUE);  /* Booleano true: primeira letra tem que ser minuscula. True, TRUE -> TYPEID (identificador de tipo). */ }
<YYINITIAL>f[aA][lL][sS][eE]  { return new Symbol(TokenConstants.BOOL_CONST, Boolean.FALSE); /* Booleano false: mesma regra — primeiro char deve ser 'f' minusculo. */ }

<YYINITIAL>[0-9]+              { return new Symbol(TokenConstants.INT_CONST,
                                     AbstractTable.inttable.addString(yytext())); /* Inteiro: sequencia de digitos. O valor e guardado como String em inttable para que o parser recupere depois. yytext() retorna o texto casado, ex: "42". */ }

<YYINITIAL>[A-Z][a-zA-Z0-9_]* { return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador tem que comecar com letra maiuscula eh um nome de tipo. Ex: MyClass, Object, IO. */ }
<YYINITIAL>[a-z][a-zA-Z0-9_]* { return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula eh um nome de variavel/metodo. Ex: myVar, self. */ }

<YYINITIAL>"<-"  { return new Symbol(TokenConstants.ASSIGN); /* atribuicao: x <- 5 */ }
<YYINITIAL>"<="  { return new Symbol(TokenConstants.LE);     /* menor ou igual. Vem antes de "<" para que "<=" nao seja lido como "<" + "=". */ }
<YYINITIAL>"=>"  { return new Symbol(TokenConstants.DARROW); /* seta de case of Int => */ }
<YYINITIAL>"+"   { return new Symbol(TokenConstants.PLUS); }
<YYINITIAL>"-"   { return new Symbol(TokenConstants.MINUS); }
<YYINITIAL>"*"   { return new Symbol(TokenConstants.MULT); }
<YYINITIAL>"/"   { return new Symbol(TokenConstants.DIV); }
<YYINITIAL>"<"   { return new Symbol(TokenConstants.LT);     /* menor que */ }
<YYINITIAL>"="   { return new Symbol(TokenConstants.EQ);     /* igualdade */ }
<YYINITIAL>"~"   { return new Symbol(TokenConstants.NEG);    /* negacao inteira: ~x equivale a -x em cool */ }
<YYINITIAL>"."   { return new Symbol(TokenConstants.DOT);    /* acesso a metodo: obj.metodo() */ }
<YYINITIAL>","   { return new Symbol(TokenConstants.COMMA); }
<YYINITIAL>";"   { return new Symbol(TokenConstants.SEMI); }
<YYINITIAL>":"   { return new Symbol(TokenConstants.COLON);  /* separador de tipo: x : Int */ }
<YYINITIAL>"("   { return new Symbol(TokenConstants.LPAREN); }
<YYINITIAL>")"   { return new Symbol(TokenConstants.RPAREN); }
<YYINITIAL>"{"   { return new Symbol(TokenConstants.LBRACE); }
<YYINITIAL>"}"   { return new Symbol(TokenConstants.RBRACE); }
<YYINITIAL>"@"   { return new Symbol(TokenConstants.AT);     /* dispatch estatico: obj@Tipo.metodo() */ }

<YYINITIAL>.     { return new Symbol(TokenConstants.ERROR, yytext()); /* Catch-all: qualquer char invalido vira um token ERROR. deve ser a ultima regra — tudo valido ja foi tratado acima. */ }
