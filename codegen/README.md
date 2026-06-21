# TP-05: Geração de Código (COOL → MIPS)

## Integrantes

- Luis Phillip
- Pedro Malta
- Fernanda Rodrigues
- Arthur Oliveira

## O que foi feito

Implementamos o gerador de código da linguagem COOL em Java (esqueleto
PA5J). Ele recebe a AST já tipada pela fase semântica (TP-04), lida da
entrada padrão pelo `Cgen`/`ASTParser`, e produz um arquivo `.s` em
assembly MIPS que roda no SPIM junto com o runtime de COOL
(`trap.handler`).

A geração acontece em duas passadas, como o enunciado sugere. A
primeira decide o layout: percorre o grafo de herança atribuindo um
*class tag* a cada classe, calcula o offset de cada atributo dentro do
objeto e monta a tabela de despacho de cada classe. A segunda emite o
código de fato — constantes, *prototype objects*, as tabelas globais
(`class_nameTab`, `class_objTab`, dispatch tables), os métodos `_init`
e os corpos dos métodos do usuário.

No nível das expressões, geramos código para tudo: aritmética e
comparações sobre `Int`, igualdade (com o atalho de ponteiro antes de
chamar `equality_test`), `not`/`isvoid`, `if`, `while`, `let`, blocos,
`case`, `new`/`new SELF_TYPE`, despacho dinâmico e estático, atribuição
e acesso a variáveis e atributos. Também tratamos os três erros de
execução exigidos: despacho sobre `void`, `case` sobre `void` e `case`
sem ramo compatível, cada um chamando a rotina de abort do runtime com
o nome do arquivo e a linha.

Conferimos a saída comparando byte a byte, sob o SPIM, com a do
compilador de referência `coolc`.

## Arquivos modificados

Conforme a parte "5 — O que entregar" do enunciado, todo o código ficou
nos arquivos do esqueleto:

- **`CgenClassTable.java`** — concentra o grosso do gerador: tags,
  layout dos objetos e das dispatch tables, emissão das tabelas
  globais e dos *prototype objects*, os métodos `_init`, os corpos dos
  métodos, mais o ambiente de variáveis e os utilitários de emissão
  (prólogo/epílogo, push/pop de temporários, etc.).
- **`CgenNode.java`** — guarda por classe o tag, a faixa de tags da
  subárvore, a lista ordenada de atributos e a tabela de despacho.
- **`cool-tree.java`** — o `code(PrintStream)` de cada nó de expressão.
- **`BoolConst.java`, `IntSymbol.java`, `StringSymbol.java`** — só a
  referência à dispatch table que faltava na definição das constantes.
- **`TreeConstants.java`** — não precisou de nada novo.

Testes:

- **`example.cl`** — programa que exercita o máximo de construções:
  herança e despacho dinâmico, despacho estático (`e@Tipo.m`),
  `SELF_TYPE` (`setName` devolvendo `self`, `new`), atributos com
  inicializador e a cadeia de `_init`, recursão (fatorial), `while`,
  `let`, `case`, `isvoid`, métodos de `String` e IO.
- **`tests/`** — suíte de 8 casos (aritmética, controle de fluxo,
  despacho, `case`, strings e os três aborts) com golden e um runner
  (`tests/run.sh`, também via `make test`). Os golden foram gerados com
  o `coolc` de referência, então o runner valida o nosso gerador contra
  o compilador oficial.

O `Makefile` foi substituído (o original é symlink read-only). Única
mudança: adicionar `-d .` ao `javac`. Mesma adaptação que fizemos no
TP-03 e TP-04.

## Como rodar

```bash
make source     # cria os symlinks do framework
make cgen       # compila tudo e gera o script ./cgen
make dotest     # roda o gerador em example.cl
make test       # roda a suíte tests/ comparando com os golden
```

Para gerar e executar um programa à mão:

```bash
./mycoolc example.cl
spim -exception_file /var/tmp/cool/lib/trap.handler -file example.s
```

(Depois de um `make clean` os executáveis de referência são apagados —
basta `make lexer parser semant` ou `make source` para recriá-los.)

## Decisões de projeto

**Class tags e o `case`.** Os tags são atribuídos em pré-ordem na
árvore de herança, de modo que a subárvore de uma classe ocupa um
intervalo contíguo `[tag, maxTag]`. Com isso o `case` vira uma
comparação de faixa: o objeto de tag `t` casa com o ramo de tipo `C` se
`C.tag <= t <= C.maxTag`. Os ramos são testados do tag maior para o
menor, então o ancestral mais específico ganha.

**Layout e dispatch.** O cabeçalho tem as 3 palavras de praxe (tag,
tamanho, ponteiro pra dispatch table) seguidas dos atributos. Tanto a
lista de atributos quanto a de métodos começam como cópia da do pai, e
override de método mantém o slot original — assim o offset de um
atributo ou método nunca muda nos descendentes. Os métodos das classes
básicas não são gerados: vêm do runtime, a dispatch table só aponta
pros rótulos.

**Convenção de chamada.** Argumentos empilhados da esquerda pra direita,
`self` em `$a0`. O prólogo salva `$fp`, `$s0` e `$ra`; formais ficam
acima de `$fp` e temporários (`let`/`case`) abaixo. Como todo acesso a
variável é relativo a `$fp` ou `$s0`, mexer no `$sp` durante a avaliação
não atrapalha. O `$s0` é preservado por todas as chamadas (inclusive
`Object.copy`), então `self` está sempre à mão.

**Coletor de lixo.** O padrão é sem coletor. Para suportar `-g`, as
escritas em atributos (`assign` e `_init`) emitem `_GenGC_Assign`
quando o coletor está ligado; no padrão essa chamada não sai.

## O que foi complicado

O esqueleto do PA5J vem inconsistente: o parser fixo (`ASTParser.java`)
e o `ClassTable.java` constroem nós chamados `programc`, `class_c` e
`formalc`, mas os arquivos editáveis do esqueleto (`cool-tree.java`,
`CgenClassTable.java`, `CgenNode.java`) usavam `program`, `class_` e
`formal`. Do jeito que vem, não compila. Renomeamos esses três nós para
a forma com `c` nos arquivos editáveis pra casar com o parser — mesma
situação que tivemos no TP-04.

O outro detalhe foi o de sempre: o `javac` moderno escreve o `.class`
ao lado do `.java` de origem, e como os arquivos de suporte são symlinks
pro diretório compartilhado, os `.class` acabavam em
`/var/tmp/cool/src/PA5J/` e o `./cgen` não os achava. O `-d .` no
Makefile resolve.

## Testes

A suíte em `tests/` cobre aritmética/comparações, `while`/`let`
(incluindo shadowing), despacho dinâmico e estático com `SELF_TYPE`,
seleção do ramo certo no `case` e os três aborts. Cada `.cl` tem um
golden `.out` gerado pelo `coolc`, e `make test` (ou `tests/run.sh`)
compila com o nosso gerador, roda no SPIM e compara.

Além disso, rodamos os 15 exemplos do curso (`hello_world`, `primes`,
`list`, `complex`, `new_complex`, `cells`, `sort_list`, `book_list`,
`graph`, `cool`, `lam`, `hairyscary`, `life`, `palindrome`, `atoi`) com
o nosso gerador e com o `coolc`, e a saída foi idêntica em todos.
