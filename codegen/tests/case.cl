(* Selação do ramo mais específico em case sobre a hierarquia. *)
class A inherits IO { };
class B inherits A { };
class C inherits B { };

class Main inherits IO {
    name(x : A) : String {
        case x of
            c : C => "C";
            b : B => "B";
            a : A => "A";
        esac
    };

    main() : Object {
        {
            out_string(name(new A)); out_string("\n");   -- A
            out_string(name(new B)); out_string("\n");   -- B
            out_string(name(new C)); out_string("\n");   -- C
        }
    };
};
