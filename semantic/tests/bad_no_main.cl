(*
 * bad_no_main.cl
 *
 * Foco: ausência da classe Main.
 *
 * Mesmo que tudo o mais esteja certo, um programa COOL é inválido
 * sem uma classe chamada Main com um método main() sem argumentos.
 *
 * Erro esperado: "Class Main is not defined."
 *)

class Foo {
    x : Int <- 0;
    inc() : Int { x + 1 };
};

class Bar inherits Foo {
    y : Int <- 42;
};
