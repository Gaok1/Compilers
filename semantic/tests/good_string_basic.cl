(*
 * good_string_basic.cl
 *
 * Foco: métodos da classe básica String herdada de Object via IO.
 *
 *   length()                : Int
 *   concat(arg : String)    : String
 *   substr(arg : Int, arg2 : Int) : String
 *
 * Cada um vem da declaração em installBasicClasses() no ClassTable.
 * Se algum estiver ausente ou com assinatura errada, este teste
 * detecta na hora — o dispatch falha com "undefined method" ou
 * "wrong number of arguments" / "type ... does not conform".
 *
 * Também exercita encadeamento de dispatch ("a".concat("b").concat("c"))
 * e mistura com aritmética inteira.
 *)

class Main inherits IO {
    main() : Object {
        let
            s   : String <- "Hello, World!",
            len : Int    <- s.length(),
            sub : String <- s.substr(0, 5),
            cat : String <- "foo".concat("bar"),
            chain : String <- "a".concat("b").concat("c")
        in {
            out_string("len=");
            out_int(len);
            out_string(" sub=");
            out_string(sub);
            out_string(" cat=");
            out_string(cat);
            out_string(" chain=");
            out_string(chain);
            out_string(" mixed=");
            out_int("xyz".length() + 10);   -- Int devolvido por length() entra em aritmética
            out_string("\n");
        }
    };
};
