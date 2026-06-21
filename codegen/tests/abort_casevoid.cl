(* Erro de execução: case sobre void. *)
class Main inherits IO {
    x : Object;                 -- fica void
    main() : Object {
        case x of y : Object => 1; esac
    };
};
