(* Herança, despacho dinâmico, despacho estático e SELF_TYPE. *)
class Shape inherits IO {
    area() : Int { 0 };
    describe() : SELF_TYPE {
        { out_string("area="); out_int(area()); out_string("\n"); self; }
    };
};

class Square inherits Shape {
    side : Int <- 3;
    area() : Int { side * side };
};

class Box inherits Square {
    area() : Int { side * side * side };   -- redefine de novo
};

class Main inherits IO {
    main() : Object {
        let bx : Box <- new Box, s : Shape <- bx in {
            (new Square).describe();        -- area=9  (despacho dinâmico)
            s.describe();                   -- area=27 (Shape -> Box.area)
            -- despacho estático: força a versão de Square em um Box
            out_string("static=");
            out_int(bx@Square.area());      -- 9
            out_string("\n");
        }
    };
};
