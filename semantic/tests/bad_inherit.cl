(*
 * bad_inherit.cl - exercita os erros estruturais de herança que não
 * cabem em bad.cl (que precisa de grafo válido para chegar à fase de
 * type-check). Cada um destes erros sozinho já interrompe a compilação
 * antes do type-check.
 *)

-- 1) Redefinição de classe básica
class Int { };

-- 2) Redefinição de classe do usuário (Main aparece duas vezes)
class Main { main() : Object { self }; };
class Main { main() : Object { self }; };

-- 3) Herança proibida (de Bool, Int, String, SELF_TYPE)
class BadParent inherits Bool { };

-- 4) Pai inexistente
class Orphan inherits Ghost { };
