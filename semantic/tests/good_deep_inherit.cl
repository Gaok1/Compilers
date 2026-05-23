(*
 * good_deep_inherit.cl
 *
 * Foco: hierarquia profunda + dispatch resolvendo o override certo.
 *
 * A < B < C < D < E
 *   A define foo() e bar()
 *   B sobrescreve foo()
 *   C sobrescreve bar()
 *   D sobrescreve foo() de novo
 *   E não sobrescreve nada
 *
 * Em e : E:
 *   e.foo()      -> resolve para D.foo (último override na cadeia)
 *   e.bar()      -> resolve para C.bar
 *   e@A.foo()    -> static dispatch força A.foo
 *   e@B.foo()    -> static dispatch força B.foo
 *
 * Erros típicos que isso pega:
 *   - lookupMethod parando no primeiro encontrado em vez de ir até o fim
 *   - lookupMethod ignorando classes intermediárias na cadeia
 *   - static dispatch não respeitando o tipo declarado
 *)

class A {
    foo() : String { "A.foo" };
    bar() : Int    { 1 };
};

class B inherits A {
    foo() : String { "B.foo" };   -- override
};

class C inherits B {
    bar() : Int    { 3 };          -- override de bar (foo continua = B)
};

class D inherits C {
    foo() : String { "D.foo" };    -- override de novo
};

class E inherits D { };

class Main inherits IO {
    main() : Object {
        let e : E <- new E in {
            out_string(e.foo());        -- "D.foo"
            out_string("\n");
            out_int(e.bar());           -- 3
            out_string("\n");
            out_string(e@A.foo());      -- "A.foo"
            out_string("\n");
            out_string(e@B.foo());      -- "B.foo"
            out_string("\n");
            out_string(e@C.foo());      -- ainda "B.foo" (C não sobrescreve)
            out_string("\n");
        }
    };
};
