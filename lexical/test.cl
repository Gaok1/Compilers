-- Arquivo de testes do scanner Cool (TP02)

-- 1. Keywords em varios cases
CLASS class Class cLaSs
ELSE else Else
FI fi Fi
IF if If
IN in In
INHERITS inherits Inherits
ISVOID isvoid Isvoid
LET let Let
LOOP loop Loop
POOL pool Pool
THEN then Then
WHILE while While
CASE case Case
ESAC esac Esac
NEW new New
OF of Of
NOT not Not

-- 2. Booleanos: primeira letra obrigatoriamente minuscula
true TRUE True tRuE
false FALSE False fAlSe

-- 3. Inteiros
0 42 12345 007

-- 4. Identificadores
myVar _x x1 X1 MyType Object IO

-- 5. Operadores
x <- 5
x <= y
x => y
x + y - z * w / v
x < y
x = y
~x
x.method
(a, b; c: d)
{e} @ f

-- 6. Strings normais
"hello world"
"tab:\there"
"newline:\nhere"
"backspace:\bhere"
"formfeed:\fhere"
"escaped quote: \""
"escaped backslash: \\"
"escaped other: \a \z"
"escaped newline: \
continues here"

-- 7. String vazia
""

-- 8. String muito longa (> 1024 chars)
"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"

-- 9. String nao terminada (erro)
-- "string sem fechamento

-- 10. Comentario de bloco simples
(* comentario simples *)

-- 11. Comentario de bloco aninhado
(* nivel1 (* nivel2 (* nivel3 *) ainda nivel2 *) ainda nivel1 *)

-- 12. Comentario sem fechamento (EOF in comment)
(*
