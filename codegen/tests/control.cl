(* Laço while, recursão e escopo aninhado de let (shadowing). *)
class Main inherits IO {
    sum(n : Int) : Int {
        let s : Int <- 0, i : Int <- 1 in {
            while i <= n loop { s <- s + i; i <- i + 1; } pool;
            s;
        }
    };

    main() : Object {
        {
            out_int(sum(5));                -- 15
            out_string("\n");
            let x : Int <- 10 in {
                out_int(x);                 -- 10
                out_string("\n");
                let x : Int <- 20 in out_int(x);   -- 20 (sombreia)
                out_string("\n");
                out_int(x);                 -- 10 (volta ao externo)
                out_string("\n");
            };
        }
    };
};
