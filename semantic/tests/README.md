# Suíte ampliada de testes — TP-04

Esta pasta tem os testes "cabulosos" que vão além dos exemplos
mínimos de `semantic/good.cl` e `semantic/bad.cl`. Cada arquivo
foca em **uma característica específica** do analisador semântico e
tem um comentário no topo explicando o que está sendo testado.

A entrega do curso continua exigindo só `good.cl` e `bad.cl` no
diretório `semantic/`. Os arquivos daqui são **complementares**: a
gente usou para encontrar bugs durante o desenvolvimento e mantém
como rede de segurança.

## Como rodar

A partir do diretório `semantic/`:

```bash
./tests/run_tests.sh        # roda tudo, mostra PASS/FAIL
./tests/run_tests.sh -v     # modo verboso: imprime saída inteira nas falhas
```

O script:

1. compila `semant` se ainda não estiver compilado;
2. para cada `.cl` em `tests/`, executa o NOSSO `mysemant` e o
   `semant` de **referência** (`/var/tmp/cool/lib/.x86_64/semant`);
3. compara as duas saídas em **modo conjunto** — ou seja, ordena
   linha a linha antes do diff. Diferenças apenas de ORDEM são
   ignoradas; o CONTEÚDO precisa bater byte-a-byte;
4. reporta PASS/FAIL por arquivo e um resumo no fim.

Por que comparar ordenado: o manual do COOL não fixa a ordem de
emissão das mensagens, e o compilador de referência usa uma ordem
levemente diferente da nossa em certos casos (ex: 3 erros de ciclo
de herança). O que importa é o **conjunto** de mensagens.

Código de saída: `0` se todos passaram, `1` se algum falhou.

## Estado atual

```
14 passaram, 0 falharam
```

## Catálogo dos testes positivos

Programas válidos — o analisador não deve emitir nenhum erro e a AST
anotada precisa bater com a saída da referência.

| Arquivo | O que testa |
|---|---|
| `good_self_type.cl` | Preservação de `SELF_TYPE`: `self.copy()` mantém `SELF_TYPE`, `(new T).copy()` retorna `T`. Encadeamento de chamadas. |
| `good_let_defaults.cl` | `let` SEM `<-` (init implícito). Verifica que o tipo declarado é usado mesmo sem expressão de init. |
| `good_recursion.cl` | Método chamando a si mesmo (`fact(n-1)`) + tipo que se referencia (`List.tail : List`). Confirma que o lookup de métodos não exige declaração antes do uso. |
| `good_deep_inherit.cl` | Hierarquia `A < B < C < D < E` com overrides intercalados. Verifica resolução do método correto e static dispatch (`e@A.foo`, `e@B.foo`, `e@C.foo`). |
| `good_string_basic.cl` | Métodos `length`, `concat`, `substr` da classe básica `String`. Dispatch encadeado e mistura com aritmética inteira. |
| `good_complex_lub.cl` | LUB em `if` (irmãos → ancestral comum), `case` com tipos desconexos (→ `Object`), `case` com branch retornando `self` (LUB com SELF_TYPE). |

## Catálogo dos testes negativos

Programas inválidos — o analisador deve emitir o mesmo conjunto de
mensagens que a referência.

| Arquivo | O que testa | Erros esperados |
|---|---|---|
| `bad_inherit_self.cl` | Classe herda dela mesma (ciclo de 1 nó). | 1 (ciclo) |
| `bad_no_main.cl` | Programa sem classe `Main`. | 1 (`Class Main is not defined.`) |
| `bad_main_args.cl` | `Main.main` declarado com parâmetro. | 1 (`should have no arguments`) |
| `bad_cascade.cl` | `undefined.foo().bar().baz()` — uma cadeia de erros em uma única expressão. | 4 (undecl + 3 dispatches em `Object`) |
| `bad_self_misuse.cl` | Os 5 usos proibidos de `self` em um arquivo só (attr, formal, assign, let, case). | 5 |
| `bad_override_signature.cl` | 4 classes filhas, cada uma violando uma regra de override (formal type, return type, count, ambos). | 4 (uma por classe — ver regra de prioridade abaixo) |
| `bad_inherit.cl` | Erros estruturais de herança que abortam antes do type-check (redefinição de classe básica + de usuário, herança de `Bool`, pai inexistente). | 4 |
| `bad_cycle.cl` | Ciclo `A → B → C → A` (cada classe reporta separadamente). | 3 |

## Detalhes implementados que vieram desses testes

Vários comportamentos do nosso analisador foram descobertos /
calibrados ao comparar com a referência via essa suíte. Vale a pena
documentar:

### Override de método: prioridade da mensagem de erro

Quando um método sobrescreve outro com várias diferenças, o
compilador de referência reporta **uma única** mensagem por método,
priorizando:

1. tipo de retorno diferente;
2. quantidade de formais diferente;
3. tipo de algum formal diferente.

Por exemplo, em `bad_override_signature.cl`:

- `Child2` tem mesma aridade, mesmos tipos, retorno diferente
  → mensagem sobre retorno.
- `Child3` tem aridade diferente, mesmo retorno
  → mensagem sobre aridade.
- `Child4` tem aridade E retorno diferentes
  → mensagem sobre retorno (somente — aridade fica suprimida).

Nosso `checkOverrideSignature` segue essa ordem com `return` no fim
de cada nível, espelhando a referência.

### Linha de erro do `main` com argumentos

A mensagem `'main' method in class Main should have no arguments.`
sai com o número de linha da **classe Main**, não do método `main`
em si. Replicamos isso usando `semantError(main)` no lugar de
`semantError(m)`.

### Recuperação em cadeia (`bad_cascade.cl`)

Quando uma sub-expressão falha, atribuímos `Object` a ela e seguimos
em frente. Isso faz com que `undefined.foo().bar().baz()` emita 4
erros distintos — um para o identificador, três para os dispatches
em `Object`. Sem essa recuperação, só o primeiro apareceria.

### Comparação em modo conjunto

A referência ordena cyclic errors do bind C→B→A (do fim para o
começo), enquanto o nosso analisador percorre na ordem de declaração
(A, B, C). Em vez de complicar a iteração só para casar a ordem, o
`run_tests.sh` compara em conjunto. Para `good.cl` e `bad.cl`
(entregáveis), o diff direto (sem ordenar) também é vazio.

## Como adicionar um teste novo

1. Crie `tests/good_<nome>.cl` (válido) ou `tests/bad_<nome>.cl`
   (inválido).
2. Escreva no topo do arquivo, em comentário `(* ... *)`, o que o
   teste verifica e (no caso de `bad_`) os erros que devem aparecer.
3. Rode `./tests/run_tests.sh` — se passar, está bom; se falhar com
   diferença de ordem, considere se o `run_tests.sh` em modo
   conjunto resolve; se for diferença real, é bug a investigar.

## Limitações conhecidas

- Os testes só rodam quando o `semant` de referência está em
  `/var/tmp/cool/lib/.x86_64/semant`. Em ambientes sem o framework
  do curso instalado, o script para com mensagem de erro.
- A comparação em conjunto NÃO detecta duplicação de mensagens
  (ex: se nosso analisador emite a mesma linha duas vezes, o `sort`
  vai mostrar duas linhas, mas como o `uniq` não é aplicado, isso
  ainda apareceria no diff). Na prática, não temos esse caso hoje.
