(*
 * bad.cl: testa recuperação de erros sintáticos do parser.
 *
 * O parser deve:
 *   1. Reportar o erro com linha e nome do arquivo
 *   2. Continuar parsing após o erro (recuperar)
 *   3. Processar classes/features subsequentes corretamente
 *)

-- Classe válida para confirmar que o parser reinicia após erros
class ValidBefore {
    x : Int;
};

-- ERRO: nome de classe começa com minúscula (b não é TYPEID)
class b {
};

-- Classe válida — parser deve continuar aqui após o erro acima
class ValidAfterClassError {
    y : Int;
};

-- ERRO: herança de identificador com minúscula (objeto, não tipo)
class BadInherits inherits notAType {
    z : Int;
};

-- ERRO: feature sem ponto-e-vírgula no final
class MissingSemiInFeature {
    x : Int
    y : Int;
};

-- ERRO: método sem corpo (chave fechando faltando)
class MissingBrace {
    badMethod() : Int {
        42
    ;
};

-- ERRO: let sem corpo (IN faltando)
class BadLet {
    test() : Int {
        let x : Int <- 5
    };
};

-- ERRO: expressão inválida dentro de bloco
class BadBlock {
    test() : Int {
        {
            42;
            class;
            99;
        }
    };
};

-- ERRO: let com binding malformado antes de COMMA
class BadLetBinding {
    test() : Int {
        let : Int, y : Int <- 2 in y
    };
};

-- Classe válida ao final — parser deve chegar aqui
class Main {
    main() : Object { 0 };
};
