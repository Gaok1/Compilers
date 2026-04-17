-- good.cl: testa construções válidas da gramática COOL

-- Classe simples sem herança (herda de Object implicitamente)
class Counter {
    count : Int;

    init() : Counter {
        {
            count <- 0;
            self;
        }
    };

    increment() : Counter {
        {
            count <- count + 1;
            self;
        }
    };

    get() : Int { count };
};

-- Classe com herança
class LimitedCounter inherits Counter {
    limit : Int;

    setLimit(n : Int) : LimitedCounter {
        {
            limit <- n;
            self;
        }
    };

    -- método com múltiplos parâmetros
    incrBy(n : Int, capped : Bool) : LimitedCounter {
        {
            if capped then
                if count + n < limit then
                    count <- count + n
                else
                    count <- limit
                fi
            else
                count <- count + n
            fi;
            self;
        }
    };
};

-- Classe que testa let, case, dispatch e operadores
class Tester {
    -- atributo com inicialização
    name : String <- "tester";

    -- atributo sem inicialização
    value : Int;

    -- testa let simples com múltiplos bindings
    testLet() : Int {
        let x : Int <- 10,
            y : Int <- 20,
            z : Int
        in x + y + z
    };

    -- testa let aninhado explícito
    testLetNested() : Int {
        let a : Int <- 1 in
            let b : Int <- a + 1 in
                a + b
    };

    -- testa while
    testWhile() : Int {
        let i : Int <- 0,
            sum : Int <- 0
        in {
            while i < 10 loop {
                sum <- sum + i;
                i <- i + 1;
            } pool;
            sum;
        }
    };

    -- testa case
    testCase(x : Object) : String {
        case x of
            i : Int    => "int";
            s : String => "string";
            b : Bool   => "bool";
            o : Object => "object";
        esac
    };

    -- testa operadores aritméticos e comparação
    testArith(a : Int, b : Int) : Bool {
        let sum  : Int <- a + b,
            diff : Int <- a - b,
            prod : Int <- a * b,
            quot : Int <- a / b,
            neg  : Int <- ~a
        in sum < prod
    };

    -- testa isvoid
    testVoid(x : Object) : Bool {
        isvoid x
    };

    -- testa not e comparações <=, =
    testBool(a : Int, b : Int) : Bool {
        if not (a = b) then
            a <= b
        else
            false
        fi
    };

    -- testa new
    testNew() : Counter {
        new Counter
    };

    -- testa dispatch encadeado
    testDispatch() : Counter {
        (new Counter).init().increment().increment()
    };

    -- testa static dispatch
    testStaticDispatch(c : Counter) : Int {
        c@Counter.get()
    };

    -- testa bloco com múltiplas expressões
    testBlock() : Int {
        {
            let x : Int <- 1 in x;
            let y : Int <- 2 in y;
            let z : Int <- 3 in z;
        }
    };

    -- testa self-dispatch
    helper() : Bool { true };

    testSelfDispatch() : Bool {
        helper()
    };
};

-- Classe Main obrigatória
class Main {
    main() : Object {
        let c : Counter <- (new Counter).init() in {
            c.increment();
            c.increment();
            c.increment();
            c.get();
        }
    };
};
