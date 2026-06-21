(* Métodos de String do runtime e igualdade por conteúdo. *)
class Main inherits IO {
    main() : Object {
        let s : String <- "hello" in {
            out_int(s.length());                    -- 5
            out_string("\n");
            out_string(s.concat(" world"));         -- hello world
            out_string("\n");
            out_string(s.substr(1, 3));             -- ell
            out_string("\n");
            out_string(if "ab" = "ab" then "eq\n" else "neq\n" fi);   -- eq
            out_string(if "ab" = "ac" then "eq\n" else "neq\n" fi);   -- neq
        }
    };
};
