(*
 * good_let_defaults.cl
 *
 * Foco: `let` sem inicializador, usando os defaults da linguagem.
 *
 * No COOL é legal escrever `let x : T in <body>` (sem `<-`). O valor
 * inicial é o default do tipo:
 *   - Int    -> 0
 *   - Bool   -> false
 *   - String -> ""
 *   - outros -> void
 *
 * O analisador semântico não precisa simular esses valores, mas tem
 * que aceitar a forma sem init e dar o tipo declarado para o body.
 *
 * Por que isso é "cabuloso": é fácil esquecer que `init` pode ser um
 * no_expr e tentar verificar conformidade contra No_type, o que
 * dispararia um falso positivo.
 *)

class Main inherits IO {
    main() : Object {
        let
            x : Int,        -- sem init: tipo Int, valor implícito 0
            s : String,     -- sem init: tipo String, valor implícito ""
            b : Bool,       -- sem init: tipo Bool, valor implícito false
            o : Object      -- sem init: tipo Object, valor implícito void
        in {
            out_int(x);              -- usa x como Int -> OK
            out_string(s);           -- usa s como String -> OK
            if b then 1 else 0 fi;   -- usa b como Bool  -> OK
            isvoid o;                -- isvoid aceita qualquer tipo
        }
    };
};
