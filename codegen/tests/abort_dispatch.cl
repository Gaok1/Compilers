(* Erro de execução: despacho sobre void. *)
class Main inherits IO {
    x : Object;                 -- fica void
    main() : Object {
        x.copy()
    };
};
