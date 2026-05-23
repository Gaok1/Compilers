(*
 * bad_cascade.cl
 *
 * Foco: comportamento de recuperação em cadeia de dispatches.
 *
 * `undefined.foo().bar().baz()` envolve 4 erros em uma única
 * expressão:
 *   1. undefined         -> identificador não declarado
 *   2. <Object>.foo()    -> dispatch para método inexistente
 *   3. <Object>.bar()    -> idem
 *   4. <Object>.baz()    -> idem
 *
 * Nosso analisador atribui Object ao identificador desconhecido e
 * continua, então deve emitir os QUATRO erros e não abortar no
 * primeiro. Isso confirma que a recuperação está funcionando.
 *)

class Main {
    main() : Object {
        undefined.foo().bar().baz()
    };
};
