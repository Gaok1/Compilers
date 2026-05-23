(*
 * good_recursion.cl
 *
 * Foco: recursão de método + tipo recursivo (lista ligada).
 *
 * O type-check tem que aceitar que `length()` chama `tail.length()`
 * mesmo antes do corpo de `length()` ter sido analisado por completo
 * — métodos não precisam estar "declarados antes do uso" em COOL.
 *
 * Também testa:
 *   - atributo cujo tipo é a própria classe (List.tail : List)
 *   - isvoid usado para terminar a recursão
 *   - método retornando SELF_TYPE encadeado em construção
 *)

class List {
    head : Int;
    tail : List;

    init(h : Int, t : List) : SELF_TYPE {
        { head <- h; tail <- t; self; }
    };

    -- chamada recursiva via tail.length()
    length() : Int {
        if isvoid tail then 1 else 1 + tail.length() fi
    };

    sum() : Int {
        if isvoid tail then head else head + tail.sum() fi
    };
};

class Main inherits IO {

    -- recursão direta (fact chama fact)
    fact(n : Int) : Int {
        if n <= 0 then 1 else n * fact(n - 1) fi
    };

    main() : Object {
        let
            lst : List <- (new List).init(1, (new List).init(2, (new List).init(3, new List)))
        in {
            out_string("fact=");
            out_int(fact(5));
            out_string(" length=");
            out_int(lst.length());
            out_string(" sum=");
            out_int(lst.sum());
        }
    };
};
