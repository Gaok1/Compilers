(*  example.cl
 *
 *  Exercises a broad slice of the code generator: inheritance and
 *  dynamic dispatch, static dispatch, attributes with initializers and
 *  the per-class _init chain, arithmetic and comparisons on boxed Ints,
 *  if/while/let/case/isvoid, object creation (including new SELF_TYPE),
 *  String methods from the runtime, and the three run-time aborts
 *  (kept commented out so the program terminates normally).
 *)

class Animal inherits IO {
    name : String <- "animal";

    name() : String { name };

    setName(n : String) : SELF_TYPE {
        { name <- n; self; }
    };

    -- Dynamically dispatched; overridden by subclasses.
    speak() : String { "..." };

    introduce() : Object {
        {
            out_string(name).out_string(" says ").out_string(speak());
            out_string("\n");
        }
    };
};

class Dog inherits Animal {
    speak() : String { "woof" };
};

class Cat inherits Animal {
    speak() : String { "meow" };
};

(* Recursion + arithmetic on Int. *)
class Math {
    fact(n : Int) : Int {
        if n <= 1 then 1 else n * fact(n - 1) fi
    };

    sumTo(n : Int) : Int {
        let acc : Int <- 0, i : Int <- 1 in
            {
                while i <= n loop { acc <- acc + i; i <- i + 1; } pool;
                acc;
            }
    };
};

class Main inherits IO {
    m : Math <- new Math;

    -- describe an animal by its dynamic class using a case expression.
    kind(a : Animal) : String {
        case a of
            d : Dog => "a dog";
            c : Cat => "a cat";
            x : Animal => "some animal";
        esac
    };

    main() : Object {
        let d : Animal <- new Dog,
            c : Animal <- (new Cat).setName("Felix"),
            v : Animal     -- left void on purpose
        in {
            d.introduce();
            c.introduce();

            out_string("kind(d) = ").out_string(kind(d)).out_string("\n");
            out_string("kind(c) = ").out_string(kind(c)).out_string("\n");

            out_string("5! = ").out_int(m.fact(5)).out_string("\n");
            out_string("sum 1..10 = ").out_int(m.sumTo(10)).out_string("\n");

            -- static dispatch: call Animal's version of speak on a Dog.
            out_string("d@Animal.speak = ").out_string(d@Animal.speak()).out_string("\n");

            out_string("isvoid v = ")
                .out_string(if isvoid v then "true" else "false" fi)
                .out_string("\n");

            out_string("\"Felix\".length = ").out_int(c.name().length()).out_string("\n");

            out_string("(2 < 3) = ")
                .out_string(if 2 < 3 then "true" else "false" fi)
                .out_string("\n");

            out_string("equal strings = ")
                .out_string(if "ab" = "ab" then "true" else "false" fi)
                .out_string("\n");
        }
    };
};
