# Compiladores — COOL

Repositório dos trabalhos práticos da disciplina de Compiladores,
implementando incrementalmente um compilador para a linguagem COOL.

| TP | Fase | Diretório | Documentação |
|----|------|-----------|--------------|
| 02 | Análise léxica | `lexical/` | `lexical/README.md` |
| 03 | Análise sintática (parser + AST) | `parser/` | `parser/README.md` |
| 04 | Análise semântica (type-check) | `semantic/` | `semantic/README.md` |
| 05 | Geração de código (MIPS) | `codegen/` | `codegen/README.md` |

## TP-05 — Geração de Código

O `codegen/` contém o gerador de código em Java (PA5J). A partir da AST
já tipada, ele emite assembly MIPS que roda no SPIM com o runtime de
COOL: define *class tags*, layout de objetos, *prototype objects*,
`class_nameTab`/`class_objTab`, dispatch tables, métodos `_init` e os
corpos dos métodos do usuário. Trata os três aborts exigidos
(despacho/`case` sobre `void` e `case` sem ramo).

A saída foi validada **byte a byte** contra o compilador de referência
`coolc` em 15 exemplos do curso. Para rodar:

```bash
cd codegen
make cgen
./mycoolc example.cl
spim -exception_file /var/tmp/cool/lib/trap.handler -file example.s
```

Detalhes das decisões de projeto em `codegen/README` (entrega) e
`codegen/README.md`.

## TP-04 — Análise Semântica

O `semantic/` contém o analisador semântico em Java (PA4J). Ele:

- monta o grafo de herança das classes do usuário + básicas (`Object`, `IO`, `Int`, `Bool`, `String`);
- valida restrições estruturais (redefinição, herança ilegal, ciclo, `Main` ausente);
- faz o type-check de cada expressão segundo o manual de COOL e anota a AST com tipos.

Saída de `./mysemant good.cl` e `./mysemant bad.cl` é **diff-zero**
contra o `semant` de referência. Para rodar:

```bash
cd semantic
make semant
make dotest
```

Detalhes (decisões, escopos, SELF_TYPE, supressão de cascata, testes)
em `semantic/README.md`.

---

# TP03 — Parser da Linguagem COOL

## O que foi feito

Implementamos o analisador sintático da linguagem COOL usando **CUP**. O parser consome os tokens produzidos pelo lexer do TP02 e constrói a **AST** da linguagem por meio de ações semânticas declaradas em `parser/cool.cup`.

O foco desta etapa foi:

- descrever a gramática principal de COOL em CUP
- construir os nós da AST durante o parsing
- tratar precedência e associatividade das expressões
- adicionar recuperação básica de erros sintáticos para continuar a análise

## Arquivos principais

- `parser/cool.cup` — gramática do parser e ações semânticas
- `parser/tests/good.cl` — entrada válida para teste
- `parser/tests/bad*.cl` — entradas com erros sintáticos para testar recuperação
- `parser/README.md` — documentação completa da entrega

## Como compilar e rodar

```bash
cd parser
make parser
./myparser tests/good.cl
./myparser tests/bad.cl
./myparser tests/bad_class_feature.cl
./myparser tests/bad_let_block.cl
```

Para executar o conjunto de testes previsto no diretório do parser:

```bash
cd parser
make dotest
```

## Decisões de implementação

### Construção da AST

Cada produção relevante da gramática possui uma ação semântica que instancia o nó correspondente da AST. Assim, o parser já entrega uma árvore sintática abstrata pronta para as próximas fases do compilador.

### Precedência e associatividade

A gramática define precedência para evitar ambiguidades nas expressões, cobrindo operadores aritméticos, comparação, atribuição e formas sintáticas como `dispatch`, `if`, `while`, `let` e blocos.

### Recuperação de erros

Incluímos pontos de sincronização com o símbolo especial `error` do CUP em construções como:

- classes
- features
- blocos
- `let`
- listas de argumentos e expressões

Quando encontra erro, o parser descarta tokens até um delimitador seguro, reporta a falha com número de linha e tenta continuar. A recuperação é local: ela melhora o relatório de erros, mas não garante reconstrução perfeita da estrutura restante.

## Testes

Além do `good.cl`, o parser agora tem uma pequena suíte de arquivos inválidos separada por cenário:

- `bad.cl` — suíte geral original
- `bad_class_feature.cl` — erros em classe e feature
- `bad_let_block.cl` — erros em `let` e bloco

O alvo `make dotest` no diretório `parser/` executa todos eles.

## Estrutura do repositório

```text
.
├── lexical/   # TP02: analisador léxico
├── parser/    # TP03: analisador sintático
├── semantic/  # TP04: analisador semântico
└── docs/      # enunciados e material de apoio
```

## Observação

O detalhamento completo do TP03, incluindo conflitos esperados da gramática e estratégia de recuperação por produção, está em `parser/README.md`.
