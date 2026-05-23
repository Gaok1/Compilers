(*
 * bad.cl - programa COOL com o maior número possível de erros
 * SEMÂNTICOS detectáveis pelo analisador. Para que muitos erros sejam
 * efetivamente reportados em uma única execução, o grafo de herança
 * deste arquivo é estruturalmente válido (sem ciclo, sem herança de
 * tipo básico, sem pai inexistente). Cada erro está anotado com um
 * comentário próximo à linha que o dispara.
 *
 * Cenários estruturais isolados (redefinição de classe, ciclo, etc.)
 * vivem em arquivos separados (ver bad_inherit.cl).
 *)

class Base {
    counter : Int <- 0;

    foo(a : Int) : Int { a };
};

class Unrelated { };

class Child inherits Base {
    -- 1) atributo redefinindo herdado (counter já vem de Base)
    counter : Int <- 1;

    -- 2) atributo chamado self
    self : Int;

    -- 3) atributo duplicado na mesma classe
    y : Int;
    y : Bool;

    -- 4) atributo de tipo inexistente
    z : NoSuchClass;

    -- 5) init de atributo não conforma com tipo declarado
    s : String <- 42;

    -- 6) override com número diferente de formais
    foo(a : Int, b : Int) : Int { a };

    -- 7) formal chamado self / formal SELF_TYPE
    bar(self : Int, k : SELF_TYPE) : Int { k };

    -- 8) formais duplicados
    baz(a : Int, a : Int) : Int { a };

    -- 9) formal de tipo inexistente
    qux(a : NoType) : Int { 0 };

    -- 10) corpo do método não conforma ao retorno declarado
    wrong() : Int { "string" };

    -- 11) tipo de retorno declarado inexistente
    badret() : Mystery { 0 };
};

class Main {

    main() : Object {
        {
            -- 12) identificador não declarado
            undecl;

            -- 13) atribuição a 'self'
            self <- self;

            -- 14) atribuição com tipo não conformante
            let n : Int <- 0 in n <- "no";

            -- 15) predicado de if não é Bool
            if 1 then 2 else 3 fi;

            -- 16) predicado de while não é Bool
            while "x" loop 0 pool;

            -- 17) aritmética com não-Int
            1 + "two";

            -- 18) comparação com não-Int
            1 < true;

            -- 19) igualdade entre basic types diferentes
            1 = "1";

            -- 20) argumento de 'not' não é Bool
            not 42;

            -- 21) argumento de '~' não é Int
            ~"oops";

            -- 22) dispatch para método inexistente
            (new Base).inexistente();

            -- 23) dispatch com número errado de argumentos
            (new Base).foo();

            -- 24) dispatch com tipo de argumento errado
            (new Base).foo("x");

            -- 25) static dispatch para classe inexistente
            (new Base)@Mystery.foo(1);

            -- 26) static dispatch onde expressão não conforma ao tipo declarado
            (new Base)@Unrelated.foo(1);

            -- 27) new com classe inexistente
            new Mystery;

            -- 28) let com identificador 'self'
            let self : Int <- 0 in self;

            -- 29) let com tipo inexistente
            let v : Mystery <- 0 in v;

            -- 30) let init não conforma com tipo declarado
            let n : Int <- "x" in n;

            -- 31) case com branches duplicadas e SELF_TYPE proibido
            case 1 of
                a : Int       => 1;
                b : Int       => 2;
                c : SELF_TYPE => 3;
            esac;

            -- 32) case branch com tipo inexistente
            case 1 of
                p : Phantom => p;
            esac;
        }
    };
};
