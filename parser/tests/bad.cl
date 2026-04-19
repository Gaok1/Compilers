(*
 * bad.cl: testa recuperação de erros sintáticos do parser.
 *
 * Casos cobrados pelo enunciado:
 *   1. erro em definição de classe, retomando na próxima classe
 *   2. erro em feature, retomando na próxima feature
 *   3. erro em binding de let, retomando no próximo binding
 *   4. erro em expressão de bloco, continuando após o ';'
 *)

-- Classe válida antes dos erros
class ValidBefore {
    x : Int;
};

-- ERRO: nome de classe inválido (OBJECTID onde se espera TYPEID)
class badClass {
};

-- Classe válida: confirma recuperação após erro de classe
class ValidAfterClassError {
    y : Int;
};

-- ERRO em feature: atributo malformado
class FeatureRecovery {
    broken : ;
    ok : Int;
};

-- Classe válida depois do erro em feature
class AfterFeatureRecovery {
    value : Int <- 1;
};

-- ERRO em binding de let: falta o identificador antes de ':'
class LetBindingRecovery {
    test() : Int {
        let : Int, y : Int <- 2 in y
    };
};

-- ERRO dentro de bloco: parser deve continuar até o próximo ';'
class BlockRecovery {
    test() : Int {
        {
            42;
            class;
            99;
        }
    };
};

-- Classe válida ao final: confirma que o parser chegou até aqui
class Main {
    main() : Object { 0 };
};
