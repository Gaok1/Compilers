(*
 * bad_main_args.cl
 *
 * Foco: classe Main existe e tem main(), mas com argumentos.
 *
 * O manual exige que main() não receba parâmetros.
 *
 * Erro esperado: "'main' method in class Main should have no arguments."
 *)

class Main {
    main(x : Int) : Object { x };
};
