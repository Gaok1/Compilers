(*
 * bad_cycle.cl - exercita detecção de ciclo de herança e ausência de
 * Main em um arquivo onde nenhum outro erro estrutural mais "forte"
 * (como pai inexistente) mascara o ciclo.
 *)

class A inherits B { };
class B inherits C { };
class C inherits A { };
