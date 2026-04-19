# TP-03: Análise Sintática (Parser COOL)

## O que foi feito

Implementamos um parser para a linguagem COOL usando o **CUP** (gerador de parsers em Java). O parser lê os tokens que o lexer (TP-02) produz e monta uma **AST** (árvore sintática abstrata).

O arquivo que escrevemos é o `cool.cup`. Ele descreve as regras da gramática e, pra cada regra, tem uma ação que constrói o nó correspondente na AST. O CUP pega esse arquivo e gera o `CoolParser.java` automaticamente.

---

## Como rodar

```bash
# preparar os links/scripts do framework
make source

# gerar o parser e compilar as classes Java
make parser

# testar
make dotest

# ou individualmente
./myparser tests/good.cl
./myparser tests/bad.cl
./myparser tests/bad_class_feature.cl
./myparser tests/bad_let_block.cl
```

---

## Estrutura de arquivos

```
parser/
├── cool.cup          ← onde escrevemos a gramática (arquivo nosso)
├── Makefile          ← build system (adaptado do original do curso)
├── README            ← arquivo de entrega pedido pelo curso
├── README.md         ← versão em Markdown com o mesmo conteúdo
├── tests/
│   ├── good.cl                 ← programa COOL válido pra testar
│   ├── bad.cl                  ← suíte original de recuperação
│   ├── bad_class_feature.cl    ← erros de classe e feature
│   └── bad_let_block.cl        ← erros de let e bloco
├── notes/            ← pasta pra anotações
├── [*.java]          ← arquivos do curso (symlinks pra /var/tmp/cool/src/PA3J/)
└── .gitignore
```

---

## O que foi simples

A maior parte da gramática foi direta: olhamos a Figura 1 do manual e traduzimos cada regra pra sintaxe do CUP. Cada regra tem uma ação semântica que chama `new NóDaAST(...)` com os valores dos símbolos. Quando o parser casa uma regra, a ação roda e coloca o nó no `RESULT`.

A recuperação de erros também foi simples: o CUP tem um pseudo-terminal chamado `error`. Basta escrever `error SEMI` em uma alternativa da regra pra dizer "se der erro aqui, pula tudo até o próximo `;` e continua". Fizemos isso em quatro lugares: na classe, na feature, no let e dentro de bloco. No caso de `feature`, a produção retorna `null` e a lista ignora esse valor, para não inserir uma feature inválida na AST.

A precedência dos operadores também foi tirada diretamente do manual (seção 11.1) — não precisamos pensar nisso, só copiar na ordem certa:

```
<-          (right — atribuição)
not         (right)
<= < =      (nonassoc — comparações não associam entre si)
+ -         (left)
* /         (left)
isvoid      (left)
~           (left — negação inteira)
@           (left — static dispatch)
.           (left — maior precedência)
```

---

## O que foi complicado

**Nomes das classes na AST:**
O skeleton do CUP vinha com `nonterminal program program` e `new program(...)`. Mas não existe uma classe Java chamada `program` — a classe abstrata é `Program` (P maiúsculo) e a concreta é `programc`. Mesma coisa pra `class_` vs `Class_` / `class_c`. Sem entender isso o javac não compilava. Tivemos que olhar o `cool-tree.java` pra descobrir os nomes certos.

**O `let` e a ambiguidade:**
O `let` pode ter vários bindings separados por vírgula: `let x:Int, y:Int in ...`. A regra é recursiva — cada binding vira um `let` aninhado dentro do próximo. Mas isso cria um conflito shift-reduce: quando o parser vê `let x:T in e1 + e2`, ele não sabe se o `+ e2` é parte do corpo do `let` ou de uma expressão externa. O comportamento correto, conforme o manual, é que o corpo do `let` se estende o máximo possível pra direita — ou seja, `e1 + e2` todo pertence ao corpo. Isso é exatamente o que acontece quando o parser dá preferência ao shift (continuar lendo) em vez de reduce (fechar o let antes). Em parsers LALR(1), shift tem prioridade sobre reduce por padrão em conflitos desse tipo, então o comportamento correto sai naturalmente. O `-expect 10000` no Makefile apenas suprime os avisos sobre esses conflitos conhecidos.

**Os `.class` iam parar no lugar errado:**
Os arquivos `.java` do framework são links simbólicos pra `/var/tmp/cool/src/PA3J/`. Quando o javac compila um symlink, ele coloca o `.class` no diretório onde o arquivo real está, não onde você está. Então as classes sumiam da pasta do projeto e o programa não rodava. A correção foi adicionar `-d .` no comando de compilação do Makefile, que força os `.class` a ficarem na pasta atual.

---

## O que o parser trata e como trata

Implementamos recuperação de erro com o pseudo-terminal `error` do CUP. A estratégia é sempre a mesma: descartar tokens até um delimitador seguro e retomar o parsing a partir dali.

**1. Erro em definição de classe**

Regra usada:

```cup
class ::= error SEMI
```

Como funciona:
- se a definição de uma classe ficar inválida, o parser descarta tokens até o `;` que fecha a classe
- depois disso ele tenta ler a próxima classe do arquivo

**2. Erro em feature**

Regra usada:

```cup
feature ::= error SEMI
```

Como funciona:
- o parser descarta a feature inválida até o próximo `;`
- essa produção retorna `null`
- a lista de features ignora esse `null`, então a AST não recebe uma feature inválida

**3. Erro em binding de `let`**

Regras usadas:

```cup
let_expr ::= error IN expr
          |  error COMMA let_expr
```

Como funciona:
- se o binding estiver quebrado antes do `in`, o parser joga fora os tokens até `in` e segue com o corpo do `let`
- se o binding estiver quebrado antes de uma vírgula, o parser joga fora os tokens até a vírgula e tenta continuar no próximo binding

**4. Erro em expressão dentro de bloco**

Regras usadas:

```cup
block_expr_list ::= error SEMI
                  | block_expr_list error SEMI
```

Como funciona:
- se uma expressão do bloco estiver inválida, o parser descarta tokens até o próximo `;`
- depois disso ele continua tentando reconhecer as expressões seguintes do mesmo bloco

**Limites dessa recuperação**

- Ela é local, não global. Se o contexto ao redor estiver muito quebrado, o parser pode ficar desalinhado.
- O objetivo não é “adivinhar” o programa correto, e sim voltar a um ponto seguro para continuar relatando erros.
- Em arquivos inválidos, o comportamento esperado é emitir mensagens de erro e encerrar com falha; a AST só é garantida para entradas válidas.

---

## Testes

**`good.cl`** tem exemplos de tudo que a gramática permite:
- classes com e sem herança
- métodos com zero, um e vários parâmetros
- atributos com e sem valor inicial
- `if`, `while`, blocos, `let`, `case`
- todos os operadores: `+ - * / ~ < <= = not`
- `new`, `isvoid`
- dispatch simples, estático (`@`) e encadeado

**Arquivos `bad*.cl`** testam os pontos de recuperação implementados:
1. `bad.cl` cobre a suíte original com classe, feature, `let` e bloco
2. `bad_class_feature.cl` reforça recuperação de classe e de feature com mais de um formato de erro
3. `bad_let_block.cl` cobre os dois caminhos de recuperação do `let` (`COMMA` e `IN`) e um bloco com continuação após `;`

**Resultado esperado:**
- `good.cl` → AST gerada, sem erros
- `bad*.cl` → erros reportados com número de linha e parser continua até os próximos pontos de sincronização
