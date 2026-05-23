# TP-04: Análise Semântica (Semant COOL)

## Integrantes

- Luis Phillip
- Pedro Malta
- Fernanda Rodrigues
- Arthur Oliveira

## O que foi feito

Implementamos o analisador semântico da linguagem COOL em Java
(esqueleto PA4J). Ele recebe a AST produzida pelo parser (TP-03), faz
duas passadas sobre ela e:

1. monta o **grafo de herança** com as classes do usuário e as básicas
   (`Object`, `IO`, `Int`, `Bool`, `String`);
2. verifica todas as restrições **estruturais** (redefinição de classe,
   herança ilegal, pai inexistente, ciclo, presença da classe `Main`);
3. percorre cada classe construindo a **tabela de símbolos** (atributos
   herdados + próprios, `self`, formais, bindings de `let`/`case`);
4. faz o **type-check** de cada expressão segundo o manual de COOL
   (seções 7 e 12), reportando todos os erros sem abortar o programa
   além do necessário;
5. **anota a AST** com tipos via `set_type`, gerando o mesmo dump
   esperado pelo gerador de código.

A saída do nosso `mysemant` é **idêntica** à do compilador de
referência (`/var/tmp/cool/lib/.x86_64/semant`) em `good.cl` e
`bad.cl`.

---

## Arquivos modificados / criados

Conforme a parte "11 — O que entregar" do enunciado, todo o código
ficou em três arquivos do esqueleto:

- **`ClassTable.java`** — concentra praticamente todo o analisador:
  classes básicas, grafo de herança, conformidade/LUB, e o visitor
  que faz o type-check de cada nó da AST.
- **`cool-tree.java`** — apenas o necessário: ajuste do
  `program.semant()` para chamar a fase 2 e a renomeação dos nós
  concretos para casar com o `ASTParser` (ver "O que foi complicado").
- **`TreeConstants.java`** — não foi necessário adicionar nada além do
  que o esqueleto já fornecia.

Arquivos de teste:

- **`good.cl`** — exercita as construções legais (herança, override
  válido, SELF_TYPE, `let`, `case`, dispatch dinâmico e estático, IO).
- **`bad.cl`** — concentra ~35 erros de tipo em um único arquivo
  estruturalmente válido (assim todos rodam de fato).
- **`tests/`** — suíte ampliada com 14 testes "cabulosos" que vão
  além de `good.cl`/`bad.cl`, cada um focando em uma característica
  específica do analisador (SELF_TYPE no dispatch, let sem init,
  recursão de método, hierarquia profunda, LUB complexo, etc.). Vem
  com um runner (`tests/run_tests.sh`) que compara cada arquivo
  contra a referência. Detalhes em `tests/README.md`.

`Makefile` foi substituído (o original é symlink read-only). Mudança
única: adicionar `-d .` ao `javac` para que os `.class` caiam na pasta
do projeto, não em `/var/tmp/cool/src/PA4J/`. Mesma adaptação que
fizemos no TP-03.

---

## Como rodar

```bash
make source         # cria os symlinks do framework
make semant         # compila tudo e gera o script ./semant
make dotest         # roda mysemant em good.cl e bad.cl
./tests/run_tests.sh  # roda a suíte ampliada (14 casos), compara com a referência
```

Para checar contra a referência manualmente em um arquivo qualquer:

```bash
diff <(./lexer bad.cl | ./parser bad.cl | /var/tmp/cool/lib/.x86_64/semant) \
     <(./mysemant bad.cl)
```

---

## Decisões de design

### Visitor concentrado em `ClassTable.java`

O esqueleto sugere espalhar métodos `typecheck()` por todos os nós de
`cool-tree.java`. Optamos por concentrar a fase 2 em um **visitor com
`instanceof`** dentro de `ClassTable.java`. Razões:

- toda a lógica de tipos fica num arquivo só, fácil de revisar;
- `cool-tree.java` muda muito pouco (só `program.semant()`), o que
  reduz risco de mexer em código gerado;
- `ClassTable` já é o "centro de controle" — já tem `semantError`,
  `conforms`, `lookupMethod`, `lookupAttr`. Pôr o type-check ali deixa
  o fluxo coeso.

### Duas passadas

A análise é dividida em duas passadas explícitas, como o enunciado
sugere:

1. **Construção do grafo + verificação estrutural**: feita no
   construtor de `ClassTable`. Se algo falha (ciclo, herança ilegal,
   `Main` ausente), `program.semant()` aborta antes da segunda fase
   para não cascatear erros.
2. **Type-check de expressões**: `ClassTable.typecheck(program)`
   percorre cada classe do usuário. Para cada classe, fizemos uma
   **sub-passada estrutural** (checagem de override) antes da
   sub-passada de expressões (corpo de método, init de atributo) — é
   exatamente a ordem com que o compilador de referência emite as
   mensagens, e replicar essa ordem facilitou os diffs.

### Escopo do `SymbolTable`

Cada classe abre **dois escopos**:

- escopo externo: `self` e atributos herdados;
- escopo interno: atributos da própria classe.

Esse layout faz `probe()` (escopo corrente) detectar
*"atributo multiplamente definido"* e `lookup()` (todos os escopos)
detectar *"atributo de classe herdada"*. Sem essa separação, os dois
erros se confundiam.

Métodos abrem mais um escopo para os formais; `let` e `case` abrem
escopos curtos para o body e cada branch, respectivamente.

### `SELF_TYPE`, `conforms` e `lub`

Implementamos as quatro regras do manual em `conforms`:

```
SELF_TYPE_C ≤ SELF_TYPE_C
SELF_TYPE_C ≤ T          se C ≤ T
T          ≤ SELF_TYPE   nunca
T1         ≤ T2          se T2 é ancestral de T1 no grafo
```

`join` (LUB) preserva `SELF_TYPE` apenas quando os dois operandos são
o mesmo `SELF_TYPE_C`; caso contrário, resolve para a classe corrente
e calcula o menor ancestral comum no grafo.

### Recuperação de erros

Seguimos a sugestão do enunciado: a expressão que não conseguiu tipo
recebe `Object` e a análise continua. Isso permite reportar todos os
erros de uma só vez. Algumas regras adicionais foram necessárias para
não emitir erro em cascata:

- método com tipo de retorno inexistente: reportamos o erro mas
  **não** checamos conformância do corpo (o tipo declarado nem existe);
- `let v : T` com `T` inexistente: idem para o init;
- `case` com branch de tipo inexistente: o branch contribui `Object`
  para o `lub`, em vez de propagar o tipo fantasma;
- `(e@T.m(...))` com `e` não conforma `T`: paramos no erro de
  conformidade, sem reportar também `m undefined`.

### Override de método: regra de prioridade

Quando um método sobrescreve outro com várias diferenças simultâneas,
o compilador de referência emite **uma única** mensagem por método,
priorizando nesta ordem:

1. tipo de retorno diferente;
2. quantidade de formais diferente;
3. tipo de algum formal diferente.

Implementamos `checkOverrideSignature` com `return` no fim de cada
nível para refletir essa prioridade. O teste
`tests/bad_override_signature.cl` cobre as quatro combinações.

Essas escolhas foram calibradas para casar com a saída do compilador
de referência via a suíte em `tests/`.

---

## O que foi simples

A maior parte da gramática de tipos do COOL é descrita pelo manual
como regras com hipóteses e conclusão, tipo `O |- e : T`. Traduzir
isso para Java foi quase mecânico: cada regra virou um método
`checkXxx` que avalia as sub-expressões, faz as verificações e
devolve o tipo. As regras de aritmética, comparação, igualdade, `if`,
`while`, `not`, `~`, `isvoid` foram de uma sentada.

`SymbolTable` do framework é uma pilha de hashtables com
`enterScope`/`exitScope`/`addId`/`lookup`/`probe`. Bastou usar — não
precisamos implementar nada de estrutura de dados.

A detecção de ciclo é uma travessia trivial: para cada classe, sobe
pelos pais até chegar em `Object` (sem ciclo) ou repetir um nome
(ciclo).

---

## O que foi complicado

**Mismatch do esqueleto:** o `ASTParser.java` do PA4J espera classes
chamadas `programc`, `class_c`, `formalc`, mas o `cool-tree.java`
fornecido pelo curso define `program`, `class_`, `formal` (sem o
sufixo). Sem o renome, o `javac` falha porque o parser de AST referencia
classes que não existem. Renomeamos os três tipos para a forma `*c`,
como já estava no PA3 — é um bug do skeleton de 2019 que muitos grupos
batem.

**`.class` no diretório errado:** o `javac` escreve os `.class` ao
lado do `.java` real, e quase todos os arquivos do framework são
symlinks para `/var/tmp/cool/src/PA4J/`. Adicionar `-d .` no comando
de compilação fixa isso. Mesmo problema do TP-03.

**Conformância vs. LUB com `SELF_TYPE`:** o erro fácil é tratar
`SELF_TYPE` como um tipo qualquer e ignorar o `currentClass`. As
quatro regras de `conforms` precisam ser respeitadas literalmente,
senão `lub` retorna tipos absurdos e o type-check de `if` e `case`
quebra. Resolvemos isolando a lógica num único método e testando com
o `good.cl` (que tem `if`/`case` retornando `Shape`/`Object`).

**Escopos para distinguir "redefinição" de "herdada":** inicialmente
adicionávamos atributos herdados no mesmo escopo dos próprios e a
checagem por `probe()` retornava "multiply defined" mesmo quando o
correto era "attribute of an inherited class". A solução foi
**dois escopos por classe**.

**Suprimir erros em cascata:** o compilador de referência reporta um
erro "raiz" e silencia derivados. Sem cuidado, mostrávamos
`Undefined return type Mystery` *seguido* de `Inferred return type Int
does not conform to Mystery`, o que é redundante. Espalhamos um
booleano `typeOk` pelos pontos onde isso ocorre e só checamos
conformância quando o tipo declarado é válido.

---

## Testes — o que o `good.cl` exercita

| Construção | Onde aparece |
|---|---|
| Herança, override de método | `Shape ← Circle, Square` |
| `SELF_TYPE` em retorno | `init()` em todas as classes |
| `new SELF_TYPE` / `new T` | corpo de `cons`, `main` |
| `dispatch` dinâmico | `sh.area()`, `out_string(...)`  |
| `dispatch` estático | `c@Shape.init(4, "via-static")` |
| `if` com lub de subclasses | `pick()` devolve `Shape` |
| `while`/`not`/`=` | `sumDownTo` |
| `let` com múltiplos bindings | `main()` |
| `case` com `Object` lub | `describe()` |
| Aritmética, `~`, comparações | `area()`, `main()` |
| IO básico (`out_string`, `out_int`) | `main()` |
| Strings, Int, Bool literais | corpo de `main`/`describe` |
| `isvoid` | `List.isNil()` |

## Testes — o que o `bad.cl` exercita

35 erros distintos numa única execução, agrupados:

- **Atributos**: redefinição de herdado, nome `self`, duplicado,
  tipo inexistente, init não conforma.
- **Métodos**: override com aridade diferente, formal `self`,
  formal `SELF_TYPE`, formais duplicados, formal de tipo
  inexistente, corpo não conforma ao retorno, retorno declarado
  inexistente.
- **Identificadores/atribuição**: identificador não declarado,
  atribuição a `self`, atribuição com tipo errado.
- **Estruturas de controle**: `if`/`while` com predicado não-`Bool`.
- **Operadores**: `+`/`<` com não-`Int`, `=` entre tipos básicos
  diferentes, `not` em não-`Bool`, `~` em não-`Int`.
- **Dispatch**: método inexistente, aridade errada, tipo de
  argumento errado.
- **Static dispatch**: classe inexistente, expressão não conforma ao
  alvo.
- **`new`**: classe inexistente.
- **`let`**: bind a `self`, tipo inexistente, init não conforma.
- **`case`**: branches duplicadas, `SELF_TYPE` em branch, tipo
  inexistente em branch.

Os arquivos extras `bad_inherit.cl` e `bad_cycle.cl` cobrem os erros
estruturais (que abortam a compilação antes do type-check e por isso
não cabem em `bad.cl`).

---

## Por que acreditamos que está correto

- a saída do nosso `mysemant good.cl` é **diff-zero** contra o
  `semant` de referência (AST anotada idêntica, mesmos tipos
  inferidos);
- a saída do nosso `mysemant bad.cl` também é **diff-zero** contra a
  referência — mesmas 35 mensagens, na mesma ordem;
- a suíte ampliada em `tests/` (14 testes "cabulosos" cobrindo
  SELF_TYPE, let sem init, recursão, herança profunda, LUB,
  override em todas as combinações, etc.) passa **14/14** quando
  comparada à referência em modo conjunto. Detalhes em
  `tests/README.md`;
- o código está num único arquivo (`ClassTable.java`) com cada
  método curto e nomeado pela regra que implementa, o que torna a
  revisão rápida.

---

## Estrutura do diretório

```
semantic/
├── ClassTable.java     ← analisador (estrutura + type-check)
├── cool-tree.java      ← AST com program.semant() chamando a fase 2
├── TreeConstants.java  ← símbolos do skeleton (intocado)
├── Makefile            ← versão local (acrescenta -d . ao javac)
├── good.cl             ← caso positivo (entregável)
├── bad.cl              ← caso negativo, 35 erros de tipo (entregável)
├── tests/              ← suíte ampliada (não entregável, ver tests/README.md)
│   ├── README.md       ← catálogo de cada teste e o que ele verifica
│   ├── run_tests.sh    ← runner que compara contra o semant de referência
│   ├── good_*.cl       ← 6 casos positivos focados
│   └── bad_*.cl        ← 8 casos negativos focados
├── README              ← write-up exigido pelo curso (texto puro)
└── README.md           ← este arquivo (versão Markdown)
```
