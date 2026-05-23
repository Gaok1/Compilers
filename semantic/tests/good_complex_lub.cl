(*
 * good_complex_lub.cl
 *
 * Foco: LUB (least common ancestor) em construções condicionais.
 *
 * Casos cobertos:
 *   1. if cujos braços têm tipos irmãos -> resultado = pai comum
 *   2. case com branches de tipos primitivos diferentes -> Object
 *   3. if aninhado (cada nível faz seu próprio LUB)
 *   4. case onde uma branch retorna SELF_TYPE da própria classe e
 *      outra retorna Object -> precisa resolver SELF_TYPE para a
 *      classe corrente antes de calcular o LCA
 *
 * Se o LUB estiver errado, programas legais aqui passam a falhar
 * com erros como "Inferred type X does not conform to Y".
 *)

class Animal {
    name() : String { "animal" };
};

class Dog inherits Animal {
    name() : String { "dog" };
};

class Cat inherits Animal {
    name() : String { "cat" };
};

class Main inherits IO {

    -- if Dog/Cat -> LUB esperado: Animal
    pick(flag : Bool) : Animal {
        if flag then new Dog else new Cat fi
    };

    -- case com tipos primitivos completamente desconexos -> Object
    describe(o : Object) : Object {
        case o of
            d : Dog    => d.name();
            c : Cat    => "cat";
            i : Int    => i;
            s : String => s;
        esac
    };

    -- if aninhado dentro de if (cada um faz LUB localmente)
    classify(x : Int) : Object {
        if x < 0
            then "neg"
            else
                if x = 0
                    then 0
                    else new Object
                fi
        fi
    };

    -- case retornando self em uma branch:
    -- self é SELF_TYPE_Main (no contexto deste método).
    -- A outra branch devolve new Object.
    -- LUB(SELF_TYPE_Main, Object) = LUB(Main, Object) = Object.
    selfBranch(o : Object) : Object {
        case o of
            i : Int    => self;
            s : String => new Object;
        esac
    };

    main() : Object {
        {
            out_string(pick(true).name());
            out_string("\n");
            describe(42);
            classify(7);
            selfBranch(0);
        }
    };
};
