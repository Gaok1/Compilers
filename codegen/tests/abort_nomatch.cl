(* Erro de execução: case sem ramo compatível. *)
class A { };
class B { };
class Main inherits IO {
    main() : Object {
        case (new A) of b : B => 1; esac
    };
};
