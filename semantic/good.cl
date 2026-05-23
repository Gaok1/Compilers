(*
 * good.cl - programa COOL semanticamente válido para o TP04.
 *
 * Exercita o máximo possível de construções legais que o analisador
 * semântico precisa reconhecer:
 *   - hierarquia de classes com herança
 *   - atributos com e sem inicializador
 *   - métodos com formals, override correto e SELF_TYPE
 *   - dispatch dinâmico e estático (com @Type)
 *   - new SELF_TYPE e new T
 *   - if/then/else com lub de tipos diferentes
 *   - while/loop
 *   - let com múltiplos bindings
 *   - case com várias branches
 *   - blocos
 *   - operações aritméticas, relacionais, igualdade, not, ~, isvoid
 *   - uso dos métodos das classes básicas (IO, String, Object)
 *)

class Shape {
    sides : Int;
    label : String <- "shape";

    -- método com SELF_TYPE no retorno + retorna self
    init(s : Int, l : String) : SELF_TYPE {
        {
            sides <- s;
            label <- l;
            self;
        }
    };

    -- método com formal e expressão simples
    name() : String { label };

    -- método sobrescrito mais abaixo
    area() : Int { 0 };
};

class Circle inherits Shape {
    radius : Int <- 1;

    -- override com mesma assinatura (legal)
    init(s : Int, l : String) : SELF_TYPE {
        {
            sides <- 0;
            label <- l;
            self;
        }
    };

    setRadius(r : Int) : SELF_TYPE {
        {
            radius <- r;
            self;
        }
    };

    -- aritmética: Int * Int -> Int
    area() : Int { 3 * radius * radius };
};

class Square inherits Shape {
    side : Int <- 1;

    setSide(s : Int) : SELF_TYPE {
        {
            side <- s;
            self;
        }
    };

    area() : Int { side * side };
};

(*
 * Lista ligada genérica para exercitar isvoid e SELF_TYPE com auto-referência.
 *)
class List {
    head : Object;
    tail : List;

    init(h : Object, t : List) : SELF_TYPE {
        {
            head <- h;
            tail <- t;
            self;
        }
    };

    isNil() : Bool { isvoid tail };

    cons(x : Object) : List {
        (new List).init(x, self)
    };
};

class Main inherits IO {
    n : Int <- 5;
    s : Shape;

    -- recursão + let com múltiplos bindings + while + not + comparação
    sumDownTo(x : Int) : Int {
        let acc : Int <- 0,
            i   : Int <- x
        in
            {
                while (not (i = 0)) loop
                    {
                        acc <- acc + i;
                        i   <- i - 1;
                    }
                pool;
                acc;
            }
    };

    -- typcase exercitando branches de tipos diferentes (lub esperado: Object)
    describe(o : Object) : String {
        case o of
            i : Int    => "int";
            b : Bool   => "bool";
            st : String => "string";
            x : Object => "other";
        esac
    };

    -- if cujo then/else têm tipos diferentes (lub deve ser Shape)
    pick(flag : Bool) : Shape {
        if flag then new Circle else new Square fi
    };

    -- exercita static dispatch usando init de Shape diretamente
    raw(c : Circle) : Shape {
        c@Shape.init(4, "via-static")
    };

    main() : Object {
        let total : Int <- sumDownTo(n),
            msg   : String <- describe(total),
            sh    : Shape  <- pick(total <= n),
            big   : Shape  <- raw(new Circle)
        in
            {
                out_string("total = ");
                out_int(total);
                out_string("\n");

                out_string("kind = ");
                out_string(msg);
                out_string("\n");

                if total < 100
                then out_string("small\n")
                else out_string("big\n")
                fi;

                -- chama método polimórfico em referência Shape
                out_string("area = ");
                out_int(sh.area());
                out_string("\n");

                -- complemento + igualdade entre Strings (basic type, mesmo lado)
                if not (msg = "int")
                then out_string("not int\n")
                else out_string("is int\n")
                fi;

                -- negação inteira
                out_int(~total);
                out_string("\n");

                self;
            }
    };
};
