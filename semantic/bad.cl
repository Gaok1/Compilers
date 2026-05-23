-- bad.cl: Erros semanticos da linguagem Cool com recuperacao.
--
-- NOTA: Erros na hierarquia de heranca (herdar de Int/Bool/String,
-- classe pai nao definida, ciclos) causam halt imediato do compilador,
-- pois a especificacao permite isso. Por isso este arquivo exercita
-- erros de tipos (recuperaveis), com comentarios explicando cada um.
--
-- Para testar erros de hierarquia isoladamente, use arquivos separados.

-- ============================================================
-- Classe auxiliar para testes de dispatch
-- ============================================================
class Base {
    foo() : Int { 0 };
};

class BaseWithParam {
    bar(x : Int) : Int { x };
};

class ParentWithAttr {
    x : Int;
};

-- ERRO 1: Override com tipo de retorno diferente
class BadOverrideReturn inherits Base {
    foo() : Bool { true };
};

-- ERRO 2: Override com tipos de parametros diferentes
class BadOverrideParamType inherits BaseWithParam {
    bar(x : Bool) : Int { 0 };
};

-- ERRO 3: Redefir atributo herdado
class BadRedefAttr inherits ParentWithAttr {
    x : Bool;
};

-- ERRO 4: 'self' como nome de atributo
class BadSelfAttr {
    self : Int;
    dummy : Int <- 0;
};

-- ERRO 5: 'self' como parametro de metodo
class BadSelfParam {
    foo(self : Int) : Int { 0 };
};

-- ERRO 6: Tipo de atributo nao definido
class BadAttrType {
    x : UndefinedType;
    dummy : Int <- 0;
};

-- ERRO 7: Tipo de parametro de metodo nao definido
class BadParamType {
    foo(x : UndefinedType2) : Int { 0 };
};

-- ERRO 8: Tipo de retorno de metodo nao definido
class BadReturnType {
    foo() : UndefinedRetType { 0 };
};

-- ERRO 9: Tipo de atributo compativel, mas inicializacao errada
class BadAttrInit {
    x : Int <- "string value";   -- String nao conforma com Int
    y : Bool <- 42;              -- Int nao conforma com Bool
    z : String <- true;          -- Bool nao conforma com String
};

-- ============================================================
-- Classe Main com erros de type-checking (recuperaveis)
-- ============================================================
class Main {
    main() : Object {
        {
        -- ERRO 10: Identificador nao declarado
        undeclaredVariable;

        -- ERRO 11: Atribuicao a 'self'
        self <- new Object;

        -- ERRO 12: Aritmetica com Bool (+ exige Int)
        let b : Bool <- true in
        let i : Int  <- 1 in
            i + b;

        -- ERRO 13: Subtracao com String
        let s : String <- "x" in
        let i2 : Int   <- 1 in
            i2 - s;

        -- ERRO 14: 'not' aplicado a Int (exige Bool)
        not 42;

        -- ERRO 15: Negacao aritmetica (~) em Bool
        ~true;

        -- ERRO 16: Comparacao < com tipos nao-Int
        "hello" < "world";

        -- ERRO 17: Comparacao = entre primitivos de tipos diferentes
        1 = true;

        -- ERRO 18: Comparacao = entre Int e String
        1 = "x";

        -- ERRO 19: Condicional com predicado nao Bool
        if 42 then 1 else 2 fi;

        -- ERRO 20: Loop com condicao nao Bool
        while 0 loop 1 pool;    -- 0 e Int, nao Bool

        -- ERRO 21: Dispatch a metodo inexistente
        (new Base).nonExistentMethod();

        -- ERRO 22: Numero errado de argumentos em dispatch
        (new BaseWithParam).bar(1, 2, 3);

        -- ERRO 23: Argumento de tipo errado em dispatch
        (new BaseWithParam).bar(true);   -- Bool nao conforma com Int

        -- ERRO 24: Static dispatch tipo nao definido
        (new Base)@UndefinedType.foo();

        -- ERRO 25: new de tipo nao definido
        new UndefinedClass2;

        -- ERRO 26: let com tipo nao definido
        let y : UndefinedLetType <- 0 in y;

        -- ERRO 27: let com inicializacao incompativel
        let z : Bool <- 42 in z;

        -- ERRO 28: 'self' como variavel let
        let self : Int <- 0 in 0;

        -- ERRO 29: Retorno do metodo incompativel (retorna String em metodo Int)
        let b2 : Base <- new Base in b2.foo();   -- ok, so para ter valor

        0;
        }
    };
};
