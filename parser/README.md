# TP-03: Análise Sintática (Parser COOL)

## O que foi feito

Implementamos um parser para a linguagem COOL usando o **CUP** (gerador de parsers em Java). O parser lê os tokens que o lexer (TP-02) produz e monta uma **AST** (árvore sintática abstrata).

O arquivo que escrevemos é o `cool.cup`. Ele descreve as regras da gramática e, pra cada regra, tem uma ação que constrói o nó correspondente na AST. O CUP pega esse arquivo e gera o `CoolParser.java` automaticamente.

---

## Como rodar

```bash
# compilar
make

# testar
make dotest

# ou individualmente
./myparser tests/good.cl    # deve printar a AST sem erros
./myparser tests/bad.cl     # deve printar os erros e tentar continuar
```

---

## Estrutura de arquivos

```
parser/
├── cool.cup          ← onde escrevemos a gramática (arquivo nosso)
├── Makefile          ← build system (adaptado do original do curso)
├── README.md         ← esse arquivo
├── README            ← instruções originais do curso
├── tests/
│   ├── good.cl       ← programa COOL válido pra testar
│   └── bad.cl        ← programa com erros pra testar recuperação
├── notes/            ← pasta pra anotações
├── [*.java]          ← arquivos do curso (symlinks pra /var/tmp/cool/src/PA3J/)
└── .gitignore
```

---

## O que foi simples

A maior parte da gramática foi direta: olhamos a Figura 1 do manual e traduzimos cada regra pra sintaxe do CUP. Cada regra tem uma ação semântica que chama `new NóDaAST(...)` com os valores dos símbolos. Quando o parser casa uma regra, a ação roda e coloca o nó no `RESULT`.

A recuperação de erros também foi simples: o CUP tem um pseudo-terminal chamado `error`. Basta escrever `error SEMI` em uma alternativa da regra pra dizer "se der erro aqui, pula tudo até o próximo `;` e continua". Fizemos isso em quatro lugares: na classe, na feature, no let e dentro de bloco.

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

## Testes

**`good.cl`** tem exemplos de tudo que a gramática permite:
- classes com e sem herança
- métodos com zero, um e vários parâmetros
- atributos com e sem valor inicial
- `if`, `while`, blocos, `let`, `case`
- todos os operadores: `+ - * / ~ < <= = not`
- `new`, `isvoid`
- dispatch simples, estático (`@`) e encadeado

**`bad.cl`** testa se o parser consegue continuar depois de erros:
1. classe com nome em minúscula (deveria ser tipo)
2. herança de identificador de objeto
3. feature sem `;` no final
4. método sem `}` de fechamento
5. `let` sem `in`
6. expressão inválida dentro de bloco
7. binding de `let` malformado

**Resultado:**
- `good.cl` → AST gerada, sem erros
- `bad.cl` → 5 erros reportados com número de linha, parser continua após cada um
