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


class CoolLexer implements java_cup.runtime.Scanner {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final int YY_BOL = 128;
	private final int YY_EOF = 129;

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
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private boolean yy_at_bol;
	private int yy_lexical_state;

	CoolLexer (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	CoolLexer (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private CoolLexer () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yy_at_bol = true;
		yy_lexical_state = YYINITIAL;

/*
 * Tudo dentro de %init{ %init} e copiado para o construtor da classe CoolLexer.
 * Inicializacoes extras do lexer viriam aqui.
 */
    // empty for now
	}

	private boolean yy_eof_done = false;
	private final int STRING = 2;
	private final int STRING_ERROR = 3;
	private final int YYINITIAL = 0;
	private final int COMMENT = 1;
	private final int yy_state_dtrans[] = {
		0,
		68,
		90,
		94
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private int yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_end () {
		if (yy_buffer_end > yy_buffer_start &&
		    '\n' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
		if (yy_buffer_end > yy_buffer_start &&
		    '\r' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
	}
	private boolean yy_last_was_cr=false;
	private void yy_mark_start () {
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
		yy_at_bol = (yy_buffer_end > yy_buffer_start) &&
		            ('\r' == yy_buffer[yy_buffer_end-1] ||
		             '\n' == yy_buffer[yy_buffer_end-1] ||
		             2028/*LS*/ == yy_buffer[yy_buffer_end-1] ||
		             2029/*PS*/ == yy_buffer[yy_buffer_end-1]);
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;

		int commaIndex;
		String workString;

		int res[][] = new int[size1][size2];
		for (int i= 0; i < size1; i++) {
			for (int j= 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex==-1) ? st :
					st.substring(0, commaIndex);
				st = st.substring(commaIndex+1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j]=Integer.parseInt(workString);
					continue;
				}
				lengthString =
					workString.substring(colonIndex+1);
				sequenceLength=Integer.parseInt(lengthString);
				workString=workString.substring(0,colonIndex);
				sequenceInteger=Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}
	private int yy_acpt[] = {
		/* 0 */ YY_NOT_ACCEPT,
		/* 1 */ YY_NO_ANCHOR,
		/* 2 */ YY_NO_ANCHOR,
		/* 3 */ YY_NO_ANCHOR,
		/* 4 */ YY_NO_ANCHOR,
		/* 5 */ YY_NO_ANCHOR,
		/* 6 */ YY_NO_ANCHOR,
		/* 7 */ YY_NO_ANCHOR,
		/* 8 */ YY_NO_ANCHOR,
		/* 9 */ YY_NO_ANCHOR,
		/* 10 */ YY_NO_ANCHOR,
		/* 11 */ YY_NO_ANCHOR,
		/* 12 */ YY_NO_ANCHOR,
		/* 13 */ YY_NO_ANCHOR,
		/* 14 */ YY_NO_ANCHOR,
		/* 15 */ YY_NO_ANCHOR,
		/* 16 */ YY_NO_ANCHOR,
		/* 17 */ YY_NO_ANCHOR,
		/* 18 */ YY_NO_ANCHOR,
		/* 19 */ YY_NO_ANCHOR,
		/* 20 */ YY_NO_ANCHOR,
		/* 21 */ YY_NO_ANCHOR,
		/* 22 */ YY_NO_ANCHOR,
		/* 23 */ YY_NO_ANCHOR,
		/* 24 */ YY_NO_ANCHOR,
		/* 25 */ YY_NO_ANCHOR,
		/* 26 */ YY_NO_ANCHOR,
		/* 27 */ YY_NO_ANCHOR,
		/* 28 */ YY_NO_ANCHOR,
		/* 29 */ YY_NO_ANCHOR,
		/* 30 */ YY_NO_ANCHOR,
		/* 31 */ YY_NO_ANCHOR,
		/* 32 */ YY_NO_ANCHOR,
		/* 33 */ YY_NO_ANCHOR,
		/* 34 */ YY_NO_ANCHOR,
		/* 35 */ YY_NO_ANCHOR,
		/* 36 */ YY_NO_ANCHOR,
		/* 37 */ YY_NO_ANCHOR,
		/* 38 */ YY_NO_ANCHOR,
		/* 39 */ YY_NO_ANCHOR,
		/* 40 */ YY_NO_ANCHOR,
		/* 41 */ YY_NO_ANCHOR,
		/* 42 */ YY_NO_ANCHOR,
		/* 43 */ YY_NO_ANCHOR,
		/* 44 */ YY_NO_ANCHOR,
		/* 45 */ YY_NO_ANCHOR,
		/* 46 */ YY_NO_ANCHOR,
		/* 47 */ YY_NO_ANCHOR,
		/* 48 */ YY_NO_ANCHOR,
		/* 49 */ YY_NO_ANCHOR,
		/* 50 */ YY_NO_ANCHOR,
		/* 51 */ YY_NO_ANCHOR,
		/* 52 */ YY_NO_ANCHOR,
		/* 53 */ YY_NO_ANCHOR,
		/* 54 */ YY_NO_ANCHOR,
		/* 55 */ YY_NO_ANCHOR,
		/* 56 */ YY_NO_ANCHOR,
		/* 57 */ YY_NO_ANCHOR,
		/* 58 */ YY_NO_ANCHOR,
		/* 59 */ YY_NO_ANCHOR,
		/* 60 */ YY_NO_ANCHOR,
		/* 61 */ YY_NO_ANCHOR,
		/* 62 */ YY_NO_ANCHOR,
		/* 63 */ YY_NO_ANCHOR,
		/* 64 */ YY_NO_ANCHOR,
		/* 65 */ YY_NO_ANCHOR,
		/* 66 */ YY_NO_ANCHOR,
		/* 67 */ YY_NO_ANCHOR,
		/* 68 */ YY_NOT_ACCEPT,
		/* 69 */ YY_NO_ANCHOR,
		/* 70 */ YY_NO_ANCHOR,
		/* 71 */ YY_NO_ANCHOR,
		/* 72 */ YY_NO_ANCHOR,
		/* 73 */ YY_NO_ANCHOR,
		/* 74 */ YY_NO_ANCHOR,
		/* 75 */ YY_NO_ANCHOR,
		/* 76 */ YY_NO_ANCHOR,
		/* 77 */ YY_NO_ANCHOR,
		/* 78 */ YY_NO_ANCHOR,
		/* 79 */ YY_NO_ANCHOR,
		/* 80 */ YY_NO_ANCHOR,
		/* 81 */ YY_NO_ANCHOR,
		/* 82 */ YY_NO_ANCHOR,
		/* 83 */ YY_NO_ANCHOR,
		/* 84 */ YY_NO_ANCHOR,
		/* 85 */ YY_NO_ANCHOR,
		/* 86 */ YY_NO_ANCHOR,
		/* 87 */ YY_NO_ANCHOR,
		/* 88 */ YY_NO_ANCHOR,
		/* 89 */ YY_NO_ANCHOR,
		/* 90 */ YY_NOT_ACCEPT,
		/* 91 */ YY_NO_ANCHOR,
		/* 92 */ YY_NO_ANCHOR,
		/* 93 */ YY_NO_ANCHOR,
		/* 94 */ YY_NOT_ACCEPT,
		/* 95 */ YY_NO_ANCHOR,
		/* 96 */ YY_NO_ANCHOR,
		/* 97 */ YY_NO_ANCHOR,
		/* 98 */ YY_NO_ANCHOR,
		/* 99 */ YY_NO_ANCHOR,
		/* 100 */ YY_NO_ANCHOR,
		/* 101 */ YY_NO_ANCHOR,
		/* 102 */ YY_NO_ANCHOR,
		/* 103 */ YY_NO_ANCHOR,
		/* 104 */ YY_NO_ANCHOR,
		/* 105 */ YY_NO_ANCHOR,
		/* 106 */ YY_NO_ANCHOR,
		/* 107 */ YY_NO_ANCHOR,
		/* 108 */ YY_NO_ANCHOR,
		/* 109 */ YY_NO_ANCHOR,
		/* 110 */ YY_NO_ANCHOR,
		/* 111 */ YY_NO_ANCHOR,
		/* 112 */ YY_NO_ANCHOR,
		/* 113 */ YY_NO_ANCHOR,
		/* 114 */ YY_NO_ANCHOR,
		/* 115 */ YY_NO_ANCHOR,
		/* 116 */ YY_NO_ANCHOR,
		/* 117 */ YY_NO_ANCHOR,
		/* 118 */ YY_NO_ANCHOR,
		/* 119 */ YY_NO_ANCHOR,
		/* 120 */ YY_NO_ANCHOR,
		/* 121 */ YY_NO_ANCHOR,
		/* 122 */ YY_NO_ANCHOR,
		/* 123 */ YY_NO_ANCHOR,
		/* 124 */ YY_NO_ANCHOR,
		/* 125 */ YY_NO_ANCHOR,
		/* 126 */ YY_NO_ANCHOR,
		/* 127 */ YY_NO_ANCHOR,
		/* 128 */ YY_NO_ANCHOR,
		/* 129 */ YY_NO_ANCHOR,
		/* 130 */ YY_NO_ANCHOR,
		/* 131 */ YY_NO_ANCHOR,
		/* 132 */ YY_NO_ANCHOR,
		/* 133 */ YY_NO_ANCHOR,
		/* 134 */ YY_NO_ANCHOR,
		/* 135 */ YY_NO_ANCHOR,
		/* 136 */ YY_NO_ANCHOR,
		/* 137 */ YY_NO_ANCHOR,
		/* 138 */ YY_NO_ANCHOR,
		/* 139 */ YY_NO_ANCHOR,
		/* 140 */ YY_NO_ANCHOR,
		/* 141 */ YY_NO_ANCHOR,
		/* 142 */ YY_NO_ANCHOR,
		/* 143 */ YY_NO_ANCHOR,
		/* 144 */ YY_NO_ANCHOR,
		/* 145 */ YY_NO_ANCHOR,
		/* 146 */ YY_NO_ANCHOR,
		/* 147 */ YY_NO_ANCHOR,
		/* 148 */ YY_NO_ANCHOR,
		/* 149 */ YY_NO_ANCHOR,
		/* 150 */ YY_NO_ANCHOR,
		/* 151 */ YY_NO_ANCHOR,
		/* 152 */ YY_NO_ANCHOR,
		/* 153 */ YY_NO_ANCHOR,
		/* 154 */ YY_NO_ANCHOR,
		/* 155 */ YY_NO_ANCHOR,
		/* 156 */ YY_NO_ANCHOR,
		/* 157 */ YY_NO_ANCHOR,
		/* 158 */ YY_NO_ANCHOR,
		/* 159 */ YY_NO_ANCHOR,
		/* 160 */ YY_NO_ANCHOR,
		/* 161 */ YY_NO_ANCHOR,
		/* 162 */ YY_NO_ANCHOR,
		/* 163 */ YY_NO_ANCHOR,
		/* 164 */ YY_NO_ANCHOR,
		/* 165 */ YY_NO_ANCHOR,
		/* 166 */ YY_NO_ANCHOR,
		/* 167 */ YY_NO_ANCHOR,
		/* 168 */ YY_NO_ANCHOR,
		/* 169 */ YY_NO_ANCHOR,
		/* 170 */ YY_NO_ANCHOR,
		/* 171 */ YY_NO_ANCHOR,
		/* 172 */ YY_NO_ANCHOR,
		/* 173 */ YY_NO_ANCHOR,
		/* 174 */ YY_NO_ANCHOR,
		/* 175 */ YY_NO_ANCHOR
	};
	private int yy_cmap[] = unpackFromString(1,130,
"16,4:8,8,2,4,8,1,4:18,8,4,9,4:5,5,7,6,55,59,3,58,56,15,34:9,61,60,52,53,54," +
"4,64,35,36,37,38,39,22,36,40,41,36:2,42,36,24,43,44,36,45,46,27,47,48,49,36" +
":3,4,10,4:2,50,4,19,11,17,30,21,14,51,25,23,51:2,18,51,13,29,31,51,26,20,12" +
",33,28,32,51:3,62,4,63,57,4,0:2")[0];

	private int yy_rmap[] = unpackFromString(1,176,
"0,1,2,1,3,1,4,5,1:2,6,7,8,9,10,1:10,11,1:2,6,12,6:2,1:3,6:15,1:18,13,14,15," +
"16,17,16:15,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38," +
"39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63," +
"64,65,66,67,68,69,70,71,72,16,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87," +
"88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104")[0];

	private int yy_nxt[][] = unpackFromString(105,65,
"1,2,3,4,5,6,7,8,2,9,5,10,163,136,69,11,5,164,142,10:2,165,12,91,70,10:2,137" +
",10,95,10,166,172,10,11,143:2,147,143,149,143,92,151,96,153,143:4,155,5,10," +
"13,14,5,15,16,17,18,19,20,21,22,23,24,-1:66,2,-1:6,2,-1:59,25,-1:67,26,-1:6" +
"5,27,-1:68,10:5,-1,10:35,-1:28,11,-1:18,11,-1:41,143:5,-1,143:6,71,143:17,7" +
"1,143:10,-1:16,32,-1:49,33,-1:65,34,-1:11,25,-1,25:62,-1:11,10:5,-1,10:8,17" +
"5,10:14,175,10:11,-1:13,1,-1,50,51:2,88,93,51:58,-1:11,10:5,-1,10:2,167,10:" +
"3,28,10:11,167,10:5,28,10:10,-1:24,143:5,-1,143:4,98,143:7,100,143:9,98,143" +
":3,100,143:8,-1:24,143:5,-1,143:35,-1:24,143:5,-1,143:8,124,143:14,124,143:" +
"11,-1:19,52,-1:60,58,59:8,60,61,62,63,64,59:49,1,-1,54,55:6,56,89,55:5,57,5" +
"5:48,-1:11,10:2,29,30,10,-1,10:3,173,10,30,10,29,10:21,173,10:5,-1:24,143:2" +
",72,73,143,-1,143:3,108,143,73,143,72,143:21,108,143:5,-1:20,53,-1:57,1,-1," +
"65,66:6,67,66:55,-1:11,10:3,31,10,-1,10:5,31,10:29,-1:24,143:3,74,143,-1,14" +
"3:5,74,143:29,-1:24,10:5,-1,10:15,35,10:16,35,10:2,-1:24,143:5,-1,143:15,75" +
",143:16,75,143:2,-1:24,10,36,10:3,-1,10:10,36,10:24,-1:24,143,76,143:3,-1,1" +
"43:10,76,143:24,-1:24,10,37,10:3,-1,10:10,37,10:24,-1:24,143:5,-1,143:4,116" +
",143:17,116,143:12,-1:24,10:2,38,10:2,-1,10:7,38,10:27,-1:24,143:5,-1,143:2" +
",144,143:15,144,143:16,-1:24,10:5,-1,10:4,39,10:17,39,10:12,-1:24,143:5,-1," +
"143:3,118,143:25,118,143:5,-1:24,10:5,-1,10:4,40,10:17,40,10:12,-1:24,143:5" +
",-1,143:11,145,143:19,145,143:3,-1:24,10:5,-1,10:14,41,10:12,41,10:7,-1:24," +
"143,77,143:3,-1,143:10,77,143:24,-1:24,10:5,-1,10:4,42,10:17,42,10:12,-1:24" +
",143:5,-1,143:12,126,143:13,126,143:8,-1:24,10:5,-1,43,10:19,43,10:14,-1:24" +
",143:5,-1,143:6,128,143:17,128,143:10,-1:24,10:5,-1,10,44,10:23,44,10:9,-1:" +
"24,143:2,78,143:2,-1,143:7,78,143:27,-1:24,10:5,-1,10:4,45,10:17,45,10:12,-" +
"1:24,143:5,-1,143:4,79,143:17,79,143:12,-1:24,10:5,-1,10:3,46,10:25,46,10:5" +
",-1:24,143:5,-1,143:4,81,143:17,81,143:12,-1:24,10:5,-1,10:4,47,10:17,47,10" +
":12,-1:24,143:5,-1,82,143:19,82,143:14,-1:24,10:5,-1,10:13,48,10:7,48,10:13" +
",-1:24,143:5,-1,143:4,130,143:17,130,143:12,-1:24,10:5,-1,10:3,49,10:25,49," +
"10:5,-1:24,143:5,-1,143:14,80,143:12,80,143:7,-1:24,143:5,-1,143,83,143:23," +
"83,143:9,-1:24,143:5,-1,143,132,143:23,132,143:9,-1:24,143:5,-1,143:3,84,14" +
"3:25,84,143:5,-1:24,143:5,-1,143:9,141,143:18,141,143:6,-1:24,143:5,-1,143:" +
"6,133,143:17,133,143:10,-1:24,143:5,-1,143:4,85,143:17,85,143:12,-1:24,143:" +
"5,-1,143:13,86,143:7,86,143:13,-1:24,143,135,143:3,-1,143:10,135,143:24,-1:" +
"24,143:5,-1,143:3,87,143:25,87,143:5,-1:24,10:5,-1,10:4,97,10:7,99,10:9,97," +
"10:3,99,10:8,-1:24,143:5,-1,143:8,102,143:14,102,143:11,-1:24,143:5,-1,143:" +
"2,122,143:15,122,143:16,-1:24,143:5,-1,143:3,120,143:25,120,143:5,-1:24,143" +
":5,-1,143:12,127,143:13,127,143:8,-1:24,143:5,-1,143:6,134,143:17,134,143:1" +
"0,-1:24,10:5,-1,10:4,101,10:7,152,10:9,101,10:3,152,10:8,-1:24,143:5,-1,143" +
":3,129,143:25,129,143:5,-1:24,143:5,-1,143:12,131,143:13,131,143:8,-1:24,10" +
":5,-1,10:4,103,10:17,103,10:12,-1:24,143:5,-1,143,104,106,143:15,106,143:6," +
"104,143:9,-1:24,10:5,-1,10:16,105,10:13,105,10:4,-1:24,143:5,-1,143,139,143" +
",138,143:21,139,143:3,138,143:5,-1:24,10:5,-1,10:3,107,10:25,107,10:5,-1:24" +
",143:5,-1,143:4,110,143:7,112,143:9,110,143:3,112,143:8,-1:24,10:5,-1,10:12" +
",109,10:13,109,10:8,-1:24,143:5,-1,143:12,140,143:13,140,143:8,-1:24,10:5,-" +
"1,10:3,111,10:25,111,10:5,-1:24,143:5,-1,143:8,114,143:14,114,143:11,-1:24," +
"10:5,-1,10:2,113,10:15,113,10:16,-1:24,10:5,-1,10:12,115,10:13,115,10:8,-1:" +
"24,10:5,-1,10:3,117,10:25,117,10:5,-1:24,10:5,-1,10:3,119,10:25,119,10:5,-1" +
":24,10:5,-1,10,121,10:23,121,10:9,-1:24,10:5,-1,10:6,123,10:17,123,10:10,-1" +
":24,10,125,10:3,-1,10:10,125,10:24,-1:24,10:5,-1,10:8,146,148,10:13,146,10:" +
"4,148,10:6,-1:24,10:5,-1,10,168,150,10:15,150,10:6,168,10:9,-1:24,10:5,-1,1" +
"0,154,10,156,10:21,154,10:3,156,10:5,-1:24,10:5,-1,10:12,157,10:13,157,10:8" +
",-1:24,10:5,-1,10,158,10:23,158,10:9,-1:24,10:5,-1,10:2,159,10:15,159,10:16" +
",-1:24,10:5,-1,10:6,160,10:17,160,10:10,-1:24,10:5,-1,10:12,161,10:13,161,1" +
"0:8,-1:24,10:5,-1,10:6,162,10:17,162,10:10,-1:24,10:5,-1,10:8,169,10:14,169" +
",10:11,-1:24,10:5,-1,10:11,170,10:19,170,10:3,-1:24,10:5,-1,10:9,171,10:18," +
"171,10:6,-1:24,10:5,-1,10:4,174,10:17,174,10:12,-1:13");

	public java_cup.runtime.Symbol next_token ()
		throws java.io.IOException {
		int yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			if (yy_initial && yy_at_bol) yy_lookahead = YY_BOL;
			else yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			if (YY_EOF == yy_lookahead && true == yy_initial) {

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
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_move_end();
					}
					yy_to_mark();
					switch (yy_last_accept_state) {
					case 1:
						
					case -2:
						break;
					case 2:
						{ /* Whitespace (espaco, tab, form feed, carriage return): ignorado silenciosamente. */ }
					case -3:
						break;
					case 3:
						{ curr_lineno++; /* Newline: nao retorna token, apenas incrementa o contador de linhas. */ }
					case -4:
						break;
					case 4:
						{ return new Symbol(TokenConstants.MINUS); }
					case -5:
						break;
					case 5:
						{ return new Symbol(TokenConstants.ERROR, yytext()); /* Catch-all: qualquer char invalido vira um token ERROR. DEVE ser a ultima regra — tudo valido ja foi tratado acima. */ }
					case -6:
						break;
					case 6:
						{ return new Symbol(TokenConstants.LPAREN); }
					case -7:
						break;
					case 7:
						{ return new Symbol(TokenConstants.MULT); }
					case -8:
						break;
					case 8:
						{ return new Symbol(TokenConstants.RPAREN); }
					case -9:
						break;
					case 9:
						{ string_buf.setLength(0); string_too_long = false; yybegin(STRING); /* Abre uma string: limpa o buffer, reseta flag de erro, entra no estado STRING para ler char a char. */ }
					case -10:
						break;
					case 10:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -11:
						break;
					case 11:
						{ return new Symbol(TokenConstants.INT_CONST,
                                     AbstractTable.inttable.addString(yytext())); /* Inteiro: sequencia de digitos. O valor e guardado como String em inttable para que o parser recupere depois. yytext() retorna o texto casado, ex: "42". */ }
					case -12:
						break;
					case 12:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -13:
						break;
					case 13:
						{ return new Symbol(TokenConstants.LT);     /* menor que */ }
					case -14:
						break;
					case 14:
						{ return new Symbol(TokenConstants.EQ);     /* igualdade */ }
					case -15:
						break;
					case 15:
						{ return new Symbol(TokenConstants.PLUS); }
					case -16:
						break;
					case 16:
						{ return new Symbol(TokenConstants.DIV); }
					case -17:
						break;
					case 17:
						{ return new Symbol(TokenConstants.NEG);    /* negacao inteira: ~x equivale a -x em Cool */ }
					case -18:
						break;
					case 18:
						{ return new Symbol(TokenConstants.DOT);    /* acesso a metodo: obj.metodo() */ }
					case -19:
						break;
					case 19:
						{ return new Symbol(TokenConstants.COMMA); }
					case -20:
						break;
					case 20:
						{ return new Symbol(TokenConstants.SEMI); }
					case -21:
						break;
					case 21:
						{ return new Symbol(TokenConstants.COLON);  /* separador de tipo: x : Int */ }
					case -22:
						break;
					case 22:
						{ return new Symbol(TokenConstants.LBRACE); }
					case -23:
						break;
					case 23:
						{ return new Symbol(TokenConstants.RBRACE); }
					case -24:
						break;
					case 24:
						{ return new Symbol(TokenConstants.AT);     /* dispatch estatico: obj@Tipo.metodo() */ }
					case -25:
						break;
					case 25:
						{ /* Comentario de linha: "--" seguido de tudo ate o fim da linha. [^\n]* = zero ou mais chars que nao sao newline. O \n em si nao e consumido aqui — a regra de \n acima cuida dele. */ }
					case -26:
						break;
					case 26:
						{ comment_depth = 1; yybegin(COMMENT); /* Inicia comentario de bloco. Profundidade comeca em 1. Muda para estado COMMENT. */ }
					case -27:
						break;
					case 27:
						{ return new Symbol(TokenConstants.ERROR, "Unmatched *)"); /* "*)" fora de comentario: erro lexico. */ }
					case -28:
						break;
					case 28:
						{ return new Symbol(TokenConstants.FI); }
					case -29:
						break;
					case 29:
						{ return new Symbol(TokenConstants.IN); }
					case -30:
						break;
					case 30:
						{ return new Symbol(TokenConstants.IF); }
					case -31:
						break;
					case 31:
						{ return new Symbol(TokenConstants.OF); }
					case -32:
						break;
					case 32:
						{ return new Symbol(TokenConstants.ASSIGN); /* atribuicao: x <- 5 */ }
					case -33:
						break;
					case 33:
						{ return new Symbol(TokenConstants.LE);     /* menor ou igual. Vem antes de "<" para que "<=" nao seja lido como "<" + "=". */ }
					case -34:
						break;
					case 34:
						{ return new Symbol(TokenConstants.DARROW); /* seta de case: of Int => ... */ }
					case -35:
						break;
					case 35:
						{ return new Symbol(TokenConstants.NEW); }
					case -36:
						break;
					case 36:
						{ return new Symbol(TokenConstants.NOT); }
					case -37:
						break;
					case 37:
						{ return new Symbol(TokenConstants.LET); }
					case -38:
						break;
					case 38:
						{ return new Symbol(TokenConstants.THEN); }
					case -39:
						break;
					case 39:
						{ return new Symbol(TokenConstants.BOOL_CONST, Boolean.TRUE);  /* Booleano true: primeira letra OBRIGATORIAMENTE minuscula. Ex: true, tRuE, tRUE -> BOOL_CONST. True, TRUE -> TYPEID (identificador de tipo). */ }
					case -40:
						break;
					case 40:
						{ return new Symbol(TokenConstants.CASE); }
					case -41:
						break;
					case 41:
						{ return new Symbol(TokenConstants.LOOP); }
					case -42:
						break;
					case 42:
						{ return new Symbol(TokenConstants.ELSE); }
					case -43:
						break;
					case 43:
						{ return new Symbol(TokenConstants.ESAC); }
					case -44:
						break;
					case 44:
						{ return new Symbol(TokenConstants.POOL); }
					case -45:
						break;
					case 45:
						{ return new Symbol(TokenConstants.BOOL_CONST, Boolean.FALSE); /* Booleano false: mesma regra — primeiro char deve ser 'f' minusculo. */ }
					case -46:
						break;
					case 46:
						{ return new Symbol(TokenConstants.CLASS);    /* Keywords: case-insensitive em Cool. [cC] casa 'c' ou 'C', etc. Ficam ANTES dos identificadores para ter precedencia — se o texto for "class", esta regra casa antes da regra de OBJECTID. */ }
					case -47:
						break;
					case 47:
						{ return new Symbol(TokenConstants.WHILE); }
					case -48:
						break;
					case 48:
						{ return new Symbol(TokenConstants.ISVOID); }
					case -49:
						break;
					case 49:
						{ return new Symbol(TokenConstants.INHERITS); }
					case -50:
						break;
					case 50:
						{ curr_lineno++; /* Conta linhas mesmo dentro de comentarios. */ }
					case -51:
						break;
					case 51:
						{ /* Qualquer outro char dentro do comentario e ignorado. "." casa qualquer char exceto \n. */ }
					case -52:
						break;
					case 52:
						{ comment_depth++; /* Comentario aninhado: incrementa a profundidade. Cool suporta aninhamento. */ }
					case -53:
						break;
					case 53:
						{ if (--comment_depth == 0) yybegin(YYINITIAL); /* Fecha um nivel. Se chegou a 0, voltamos ao estado normal. */ }
					case -54:
						break;
					case 54:
						{ curr_lineno++; yybegin(YYINITIAL);
                          return new Symbol(TokenConstants.ERROR, "Unterminated string constant"); /* Newline real sem escape dentro de string: erro. A string nao foi fechada corretamente. */ }
					case -55:
						break;
					case 55:
						{ append_char(yytext().charAt(0)); /* Char normal: adiciona ao buffer. yytext().charAt(0) pega o unico char casado pelo ".". */ }
					case -56:
						break;
					case 56:
						{ /* Fecha a string com ".
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
					case -57:
						break;
					case 57:
						{ yybegin(STRING_ERROR);
                          return new Symbol(TokenConstants.ERROR, "String contains null character"); /* Null byte real dentro de string: erro. Muda para STRING_ERROR para consumir o resto sem reportar novos erros. */ }
					case -58:
						break;
					case 58:
						{ curr_lineno++; append_char('\n'); /* Escape de newline: "\" seguido de \n real. Permite strings multi-linha com \ no final da linha. Conta a linha e adiciona \n ao conteudo. */ }
					case -59:
						break;
					case 59:
						{ append_char(yytext().charAt(1)); /* Qualquer outro escape \x -> o proprio char x. Ex: \a->'a', \\->'\\'. yytext().charAt(1) pega o char apos a barra. */ }
					case -60:
						break;
					case 60:
						{ append_char('\b'); /* \b -> backspace */ }
					case -61:
						break;
					case 61:
						{ append_char('\t'); /* \t -> tab */ }
					case -62:
						break;
					case 62:
						{ append_char('\n'); /* \n (escapado) -> newline. Diferente do \n real, que e erro de string nao terminada. */ }
					case -63:
						break;
					case 63:
						{ append_char('\f'); /* \f -> form feed */ }
					case -64:
						break;
					case 64:
						{ append_char('0');  /* \0 escapado -> char '0' (nao null byte). Comportamento definido pela spec do Cool. */ }
					case -65:
						break;
					case 65:
						{ curr_lineno++; yybegin(YYINITIAL); /* Newline tambem encerra a string com erro. */ }
					case -66:
						break;
					case 66:
						{ /* Qualquer outro char apos o erro na string e ignorado silenciosamente. */ }
					case -67:
						break;
					case 67:
						{ yybegin(YYINITIAL); /* Fecha a string com erro: volta ao estado normal sem retornar token. */ }
					case -68:
						break;
					case 69:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -69:
						break;
					case 70:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -70:
						break;
					case 71:
						{ return new Symbol(TokenConstants.FI); }
					case -71:
						break;
					case 72:
						{ return new Symbol(TokenConstants.IN); }
					case -72:
						break;
					case 73:
						{ return new Symbol(TokenConstants.IF); }
					case -73:
						break;
					case 74:
						{ return new Symbol(TokenConstants.OF); }
					case -74:
						break;
					case 75:
						{ return new Symbol(TokenConstants.NEW); }
					case -75:
						break;
					case 76:
						{ return new Symbol(TokenConstants.NOT); }
					case -76:
						break;
					case 77:
						{ return new Symbol(TokenConstants.LET); }
					case -77:
						break;
					case 78:
						{ return new Symbol(TokenConstants.THEN); }
					case -78:
						break;
					case 79:
						{ return new Symbol(TokenConstants.CASE); }
					case -79:
						break;
					case 80:
						{ return new Symbol(TokenConstants.LOOP); }
					case -80:
						break;
					case 81:
						{ return new Symbol(TokenConstants.ELSE); }
					case -81:
						break;
					case 82:
						{ return new Symbol(TokenConstants.ESAC); }
					case -82:
						break;
					case 83:
						{ return new Symbol(TokenConstants.POOL); }
					case -83:
						break;
					case 84:
						{ return new Symbol(TokenConstants.CLASS);    /* Keywords: case-insensitive em Cool. [cC] casa 'c' ou 'C', etc. Ficam ANTES dos identificadores para ter precedencia — se o texto for "class", esta regra casa antes da regra de OBJECTID. */ }
					case -84:
						break;
					case 85:
						{ return new Symbol(TokenConstants.WHILE); }
					case -85:
						break;
					case 86:
						{ return new Symbol(TokenConstants.ISVOID); }
					case -86:
						break;
					case 87:
						{ return new Symbol(TokenConstants.INHERITS); }
					case -87:
						break;
					case 88:
						{ /* Qualquer outro char dentro do comentario e ignorado. "." casa qualquer char exceto \n. */ }
					case -88:
						break;
					case 89:
						{ append_char(yytext().charAt(0)); /* Char normal: adiciona ao buffer. yytext().charAt(0) pega o unico char casado pelo ".". */ }
					case -89:
						break;
					case 91:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -90:
						break;
					case 92:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -91:
						break;
					case 93:
						{ /* Qualquer outro char dentro do comentario e ignorado. "." casa qualquer char exceto \n. */ }
					case -92:
						break;
					case 95:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -93:
						break;
					case 96:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -94:
						break;
					case 97:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -95:
						break;
					case 98:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -96:
						break;
					case 99:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -97:
						break;
					case 100:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -98:
						break;
					case 101:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -99:
						break;
					case 102:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -100:
						break;
					case 103:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -101:
						break;
					case 104:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -102:
						break;
					case 105:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -103:
						break;
					case 106:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -104:
						break;
					case 107:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -105:
						break;
					case 108:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -106:
						break;
					case 109:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -107:
						break;
					case 110:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -108:
						break;
					case 111:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -109:
						break;
					case 112:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -110:
						break;
					case 113:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -111:
						break;
					case 114:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -112:
						break;
					case 115:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -113:
						break;
					case 116:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -114:
						break;
					case 117:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -115:
						break;
					case 118:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -116:
						break;
					case 119:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -117:
						break;
					case 120:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -118:
						break;
					case 121:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -119:
						break;
					case 122:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -120:
						break;
					case 123:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -121:
						break;
					case 124:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -122:
						break;
					case 125:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -123:
						break;
					case 126:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -124:
						break;
					case 127:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -125:
						break;
					case 128:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -126:
						break;
					case 129:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -127:
						break;
					case 130:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -128:
						break;
					case 131:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -129:
						break;
					case 132:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -130:
						break;
					case 133:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -131:
						break;
					case 134:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -132:
						break;
					case 135:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -133:
						break;
					case 136:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -134:
						break;
					case 137:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -135:
						break;
					case 138:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -136:
						break;
					case 139:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -137:
						break;
					case 140:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -138:
						break;
					case 141:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -139:
						break;
					case 142:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -140:
						break;
					case 143:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -141:
						break;
					case 144:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -142:
						break;
					case 145:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -143:
						break;
					case 146:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -144:
						break;
					case 147:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -145:
						break;
					case 148:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -146:
						break;
					case 149:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -147:
						break;
					case 150:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -148:
						break;
					case 151:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -149:
						break;
					case 152:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -150:
						break;
					case 153:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -151:
						break;
					case 154:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -152:
						break;
					case 155:
						{ return new Symbol(TokenConstants.TYPEID,
                                     AbstractTable.idtable.addString(yytext())); /* TYPEID: identificador que comeca com MAIUSCULA — e um nome de tipo. Ex: MyClass, Object, IO. */ }
					case -153:
						break;
					case 156:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -154:
						break;
					case 157:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -155:
						break;
					case 158:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -156:
						break;
					case 159:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -157:
						break;
					case 160:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -158:
						break;
					case 161:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -159:
						break;
					case 162:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -160:
						break;
					case 163:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -161:
						break;
					case 164:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -162:
						break;
					case 165:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -163:
						break;
					case 166:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -164:
						break;
					case 167:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -165:
						break;
					case 168:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -166:
						break;
					case 169:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -167:
						break;
					case 170:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -168:
						break;
					case 171:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -169:
						break;
					case 172:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -170:
						break;
					case 173:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -171:
						break;
					case 174:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -172:
						break;
					case 175:
						{ return new Symbol(TokenConstants.OBJECTID,
                                     AbstractTable.idtable.addString(yytext())); /* OBJECTID: identificador que comeca com minuscula — e um nome de variavel/metodo. Ex: myVar, self. */ }
					case -173:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
						yy_mark_end();
					}
				}
			}
		}
	}
}
