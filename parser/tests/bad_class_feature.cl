(*
 * bad_class_feature.cl: erros de recuperação em classe e feature.
 *)

-- Classe válida de abertura
class Warmup {
    ok : Int <- 0;
};

-- ERRO: nome da classe inválido
class badClassName {
};

-- Classe válida depois do erro de classe
class AfterClassRecovery {
    value : Int <- 1;
};

-- ERRO: atributo sem tipo
class BrokenFeatureOne {
    broken : ;
    ok : Int <- 2;
};

-- ERRO: método malformado
class BrokenFeatureTwo {
    sum(x : Int, y : ) : Int {
        x + y
    };
    fallback() : Int { 0 };
};

-- Classe válida no fim
class Main {
    main() : Object { 0 };
};
