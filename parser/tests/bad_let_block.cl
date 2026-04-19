(*
 * bad_let_block.cl: erros de recuperação em let e bloco.
 *)

class LetAndBlockRecovery {
    comma_case() : Int {
        let : Int, y : Int <- 2, z : Int <- 3 in y + z
    };

    in_case() : Int {
        let x : Int <-, y : Int <- 2 in y
    };

    block_case() : Int {
        {
            1;
            class;
            2;
            let a : Int <- 0 in a;
        }
    };
};

class Main {
    main() : Object { 0 };
};
