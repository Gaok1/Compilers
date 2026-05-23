(*
 * bad_inherit_self.cl
 *
 * Foco: classe que herda dela mesma — caso degenerado de ciclo.
 *
 * É um ciclo de comprimento 1 (A -> A). Detectores ingênuos de ciclo
 * que pulam o "primeiro" nó visitado podem deixar isso passar. Nosso
 * hasCycle usa um Set de visitados que captura mesmo o auto-loop.
 *
 * Erro esperado: Class A, or an ancestor of A, is involved in an
 * inheritance cycle.
 *)

class A inherits A { };

class Main {
    main() : Object { 0 };
};
