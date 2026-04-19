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
└── docs/      # enunciados e material de apoio
```

## Observação

O detalhamento completo do TP03, incluindo conflitos esperados da gramática e estratégia de recuperação por produção, está em `parser/README.md`.
