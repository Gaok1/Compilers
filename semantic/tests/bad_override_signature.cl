(*
 * bad_override_signature.cl
 *
 * Foco: regras de override de método. Em COOL, sobrescrever um método
 * herdado exige assinatura EXATAMENTE igual: mesma quantidade de
 * formais, mesmos tipos de formais (na mesma ordem) e mesmo tipo de
 * retorno. Qualquer diferença é erro.
 *
 * Cada classe filha abaixo viola UMA dessas regras.
 *
 * Erros esperados (4):
 *   - Child1: parameter type Int is different from original type String.
 *   - Child2: return type Int is different from original return type Bool.
 *   - Child3: Incompatible number of formal parameters in redefined method foo.
 *   - Child4: incompatibilidade de número de formais + tipo de retorno.
 *)

class Base {
    foo(a : Int, b : String) : Bool { true };
};

class Child1 inherits Base {
    -- muda o tipo do segundo formal
    foo(a : Int, b : Int) : Bool { true };
};

class Child2 inherits Base {
    -- muda só o tipo de retorno
    foo(a : Int, b : String) : Int { 0 };
};

class Child3 inherits Base {
    -- tira um formal
    foo(a : Int) : Bool { true };
};

class Child4 inherits Base {
    -- tira todos os formais e muda o retorno
    foo() : SELF_TYPE { self };
};

class Main {
    main() : Object { 0 };
};
