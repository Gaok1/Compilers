-- TP02: Arquivo de testes completo do scanner COOL
-- Cobre todos os tokens validos e todos os casos de erro.

-- ===================================================================
-- SECAO 1: KEYWORDS (case-insensitive em COOL)
-- Cada keyword deve ser reconhecida em qualquer combinacao de caixa.
-- ===================================================================

CLASS class Class cLaSs
ELSE else Else eLsE
FI fi Fi fI
IF if If iF
IN in In iN
INHERITS inherits Inherits iNhErItS
ISVOID isvoid Isvoid iSvOiD
LET let Let lEt
LOOP loop Loop lOoP
POOL pool Pool pOoL
THEN then Then tHeN
WHILE while While wHiLe
CASE case Case cAsE
ESAC esac Esac eSaC
NEW new New nEw
OF of Of oF
NOT not Not nOt

-- ===================================================================
-- SECAO 2: BOOLEANOS
-- Primeira letra OBRIGATORIAMENTE minuscula para ser BOOL_CONST.
-- True/TRUE/False/FALSE comecam com maiuscula -> sao TYPEID.
-- ===================================================================

true tRuE tRUE
false fAlSe fALSE
True TRUE
False FALSE

-- ===================================================================
-- SECAO 3: INTEIROS
-- ===================================================================

0 1 42 007 9999 1000000

-- ===================================================================
-- SECAO 4: IDENTIFICADORES
-- TYPEID: comeca com letra maiuscula
-- OBJECTID: comeca com letra minuscula
-- Underscore no inicio nao e valido -> ERROR
-- ===================================================================

MyClass Object IO String Int Bool SELF_TYPE
myVar self x1 obj_name abc123
_invalido _

-- ===================================================================
-- SECAO 5: OPERADORES E PONTUACAO
-- ===================================================================

x <- 5
a <= b
c => d
p + q - r * s / t
u < v
w = x
~y
obj.method
(a, b; c: d)
{e} @ f

-- ===================================================================
-- SECAO 6: STRINGS VALIDAS COM ESCAPES
-- ===================================================================

-- escape de tab
"tab:\there"

-- escape de newline (sequencia \n dentro da string)
"newline:\nhere"

-- escape de backspace
"backspace:\bhere"

-- escape de form feed
"formfeed:\fhere"

-- escape de aspas
"escaped quote: \""

-- escape de barra invertida
"escaped backslash: \\"

-- qualquer outro escape \x -> o proprio char x
"escaped outros: \a \z \9"

-- escape de newline real: \ no final da linha continua a string
"linha continuada: \
continua aqui"

-- string vazia
""

-- string com conteudo normal
"hello world"

-- ===================================================================
-- SECAO 7: COMENTARIOS DE LINHA
-- ===================================================================

-- este e um comentario de linha simples
-- comentario com -- duplo tracinho -- dentro
-- comentario com simbolos especiais: (* *) { } " @ <- =>

-- ===================================================================
-- SECAO 8: COMENTARIOS DE BLOCO
-- ===================================================================

(* comentario de bloco simples *)

(* comentario de bloco
   com multiplas linhas
   deve contar linhas corretamente *)

-- aninhamento: Cool permite (* (* *) *) como valido
(* nivel1 (* nivel2 (* nivel3 *) volta2 *) volta1 *)

-- comentario de bloco com conteudo especial
(* contem "strings", <- operadores, 123 numeros *)

-- ===================================================================
-- SECAO 9 (ERRO): STRING NAO TERMINADA POR NEWLINE REAL
-- Um newline real dentro de string sem escape -> ERROR "Unterminated string constant"
-- O lexer retorna ao estado inicial e tokeniza o restante normalmente.
-- ===================================================================

"esta string nao tem fechamento na mesma linha
token_apos_erro_de_string

-- ===================================================================
-- SECAO 10 (ERRO): STRING MUITO LONGA (> 1024 chars)
-- MAX_STR_CONST = 1025, limite e >= 1024 chars de conteudo.
-- Esta string tem 1025 'a' -> deve retornar ERROR "String constant too long"
-- ===================================================================

"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"

-- ===================================================================
-- SECAO 11 (ERRO): CHARS INVALIDOS
-- Qualquer char nao reconhecido pelo lexer -> ERROR com o proprio char
-- ===================================================================

#
!

-- ===================================================================
-- SECAO 12 (ERRO): *) SEM ABERTURA DE COMENTARIO
-- -> ERROR "Unmatched *)"
-- ===================================================================

*)

-- ===================================================================
-- SECAO 13 (ERRO): COMENTARIO SEM FECHAMENTO (EOF in comment)
-- DEVE SER O ULTIMO TESTE: consome tudo ate o EOF
-- -> ERROR "EOF in comment"
-- ===================================================================

(*