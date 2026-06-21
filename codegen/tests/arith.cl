(* Aritmética sobre Int, negação e comparações. *)
class Main inherits IO {
    main() : Object {
        {
            out_int(2 + 3 * 4 - 1);                 -- 13
            out_string("\n");
            out_int(20 / 6);                        -- 3
            out_string("\n");
            out_int(~5 + 10);                       -- 5
            out_string("\n");
            out_string(if 3 < 5 then "lt\n" else "ge\n" fi);
            out_string(if 5 <= 5 then "le\n" else "gt\n" fi);
            out_string(if not (1 = 2) then "neq\n" else "eq\n" fi);
        }
    };
};
