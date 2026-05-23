(*
 * bad_self_misuse.cl
 *
 * Foco: todos os usos proibidos do identificador `self` em um só
 * arquivo. `self` é especial — não pode ser nome de atributo,
 * formal, identificador de let ou case branch, e não pode aparecer
 * no lado esquerdo de uma atribuição.
 *
 * Erros esperados (5 ao todo):
 *   - 'self' cannot be the name of an attribute.
 *   - 'self' cannot be the name of a formal parameter.
 *   - Cannot assign to 'self'.
 *   - 'self' cannot be bound in a 'let' expression.
 *   - 'self' bound in 'case'.
 *)

class A {
    -- 1) atributo chamado self
    self : Int;

    -- 2) formal chamado self
    foo(self : Int) : Int { self };

    -- 3) atribuir a self
    bar() : Object { self <- self };

    -- 4) let self
    baz() : Object { let self : Int <- 0 in self };

    -- 5) case branch chamada self
    qux() : Object {
        case 1 of
            self : Int => 0;
        esac
    };
};

class Main {
    main() : Object {
        (new A).foo(1)
    };
};
