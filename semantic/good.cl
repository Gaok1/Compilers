-- good.cl: Testa combinacoes semanticas legais da linguagem Cool.
-- Cada secao cobre um aspecto diferente da analise semantica.

-- ============================================================
-- 1. Heranca simples e multiplos niveis
-- ============================================================
class Animal {
    name : String;
    sound() : String { "..." };
    getName() : String { name };
};

class Dog inherits Animal {
    sound() : String { "Woof" };    -- override com mesma assinatura
    fetch() : Bool { true };
};

class Puppy inherits Dog {
    age : Int;
    getAge() : Int { age };
};

-- ============================================================
-- 2. Atributos de varios tipos, incluindo inicializacao
-- ============================================================
class TypesDemo {
    i  : Int <- 42;
    b  : Bool <- true;
    s  : String <- "hello";
    o  : Object <- new Object;

    getInt()    : Int    { i };
    getBool()   : Bool   { b };
    getString() : String { s };
};

-- ============================================================
-- 3. SELF_TYPE em atributo, metodo e new
-- ============================================================
class Copyable {
    copy2() : SELF_TYPE { self };
    make()  : SELF_TYPE { new SELF_TYPE };
};

class CopyChild inherits Copyable {
    extra : Int <- 7;
};

-- ============================================================
-- 4. Operacoes aritmeticas e comparacoes
-- ============================================================
class MathOps {
    calc(a : Int, b : Int) : Int {
        {
            a + b;
            a - b;
            a * b;
            a / b;
            ~a;
            if a < b  then 1 else 2 fi;
            if a <= b then 1 else 2 fi;
            if a = b  then 1 else 0 fi;
        }
    };
};

-- ============================================================
-- 5. Operacoes booleanas
-- ============================================================
class BoolOps {
    check(x : Bool, y : Bool) : Bool {
        if not x then y else not y fi
    };
    cmpBool(a : Bool, b : Bool) : Bool { a = b };
};

-- ============================================================
-- 6. Expressoes let (aninhadas) e escopo
-- ============================================================
class LetDemo {
    nested() : Int {
        let x : Int <- 1 in
        let y : Int <- x + 1 in
        let z : Int <- x + y in
        z
    };

    noInit() : String {
        let s : String in
        {
            s <- "ok";
            s;
        }
    };
};

-- ============================================================
-- 7. Case expression (typcase)
-- ============================================================
class CaseDemo {
    classify(o : Object) : String {
        case o of
            i  : Int    => "int";
            b  : Bool   => "bool";
            s  : String => "string";
            x  : Object => "other";
        esac
    };
};

-- ============================================================
-- 8. While loop
-- ============================================================
class LoopDemo {
    countDown(n : Int) : Object {
        while 0 < n loop
            n <- n - 1
        pool
    };
};

-- ============================================================
-- 9. Dispatch, static dispatch e encadeamento
-- ============================================================
class DispatchDemo {
    run() : String {
        let d : TypesDemo <- new TypesDemo in
            d.getString()
    };

    staticRun() : String {
        let d : TypesDemo <- new TypesDemo in
            d@TypesDemo.getString()
    };

    chainRun() : String {
        (new TypesDemo).getString()
    };
};

-- ============================================================
-- 10. isvoid
-- ============================================================
class IsVoidDemo {
    check(x : Object) : Bool { isvoid x };
    checkNull() : Bool {
        let x : Object in isvoid x    -- x nao inicializado = void
    };
};

-- ============================================================
-- 11. Uso de IO
-- ============================================================
class IODemo inherits IO {
    greet() : SELF_TYPE {
        out_string("Hello from Cool!\n")
    };
    readNum() : Int {
        in_int()
    };
};

-- ============================================================
-- 12. Classe Main obrigatoria com main() sem argumentos
-- ============================================================
class Main {
    main() : Object {
        {
            -- Testa heranca e polimorfismo
            let a : Animal <- new Dog in a.sound();

            -- Testa let e case
            let demo : CaseDemo <- new CaseDemo in {
                demo.classify(42);
                demo.classify("hello");
                demo.classify(true);
            };

            -- Testa operacoes matematicas
            let m : MathOps <- new MathOps in m.calc(10, 3);

            -- Testa isvoid
            let v : IsVoidDemo <- new IsVoidDemo in {
                v.check(new Object);
                v.checkNull();
            };

            -- Testa SELF_TYPE
            let c : Copyable <- new Copyable in c.copy2();
        }
    };
};
