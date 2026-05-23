(*
 * good_self_type.cl
 *
 * Foco: SELF_TYPE no dispatch.
 *
 * COOL distingue dois casos sutis:
 *   - self.foo()      retorna SELF_TYPE       (preserva o tipo dinâmico)
 *   - (new T).foo()   retorna T               (tipo estático fixo)
 *
 * Se isso for confundido, programas legais quebram (caso típico:
 * encadear .copy().copy() ou usar o resultado em let : SELF_TYPE).
 *
 * O programa abaixo exercita:
 *   1. método herdado que retorna SELF_TYPE
 *   2. chamada via self  -> tipo SELF_TYPE
 *   3. chamada via new T -> tipo T
 *   4. encadeamento de duas chamadas a um método SELF_TYPE
 *   5. atribuição do resultado a um identificador do tipo concreto
 *)

class A {
    clone() : SELF_TYPE { self };
};

class B inherits A {
    -- self.clone() devolve SELF_TYPE; .clone() de novo também.
    -- O tipo do corpo é SELF_TYPE, que conforma a SELF_TYPE declarado.
    twice() : SELF_TYPE { clone().clone() };
};

class C inherits B {
    -- mostra que herança preserva o resultado correto:
    -- self é SELF_TYPE_C, twice() devolve SELF_TYPE = C.
    label() : String { "C" };
};

class Main inherits IO {
    main() : Object {
        let
            a : A <- (new A).clone(),  -- (new A).clone() : A
            b : B <- (new B).twice(),  -- (new B).twice() : B
            c : C <- (new C).twice()   -- (new C).twice() : C  (não A nem B!)
        in
            out_string(c.label())
    };
};
