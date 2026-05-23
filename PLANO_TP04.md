# TP04 — Análise Semântica em Cool (Java)

## Status

Implementação completa e funcionando. `gmake dotest` passa sem erros em `good.cl`
e detecta todos os 29 erros esperados em `bad.cl`.

---

## Quick Start para o Grupo

> Faça isso toda vez que clonar/copiar o projeto em uma máquina nova.

```bash
# 1. Entre no diretório do trabalho
cd semantic/

# 2. Copie as classes de suporte que o javac não coloca no lugar certo (explicação abaixo)
cp /var/tmp/cool/src/PA4J/SymbolTable.class .
cp /var/tmp/cool/src/PA4J/SymtabExample.class .

# 3. Compile
gmake semant

# 4. Rode os testes
gmake dotest
```

**O que esperar:**
- `good.cl` → AST anotada com tipos impressa no terminal, zero mensagens de erro.
- `bad.cl` → 29 linhas de erro semântico + `Compilation halted due to static semantic errors.`

Para testar um arquivo qualquer:
```bash
./mysemant meu_arquivo.cl
```

---

## Por que o `cp` é necessário (problema do classpath)

O `javac` grava o `.class` no mesmo diretório do **arquivo-fonte real**, não de onde
você rodou o comando. Como vários `.java` do projeto são symlinks para
`/var/tmp/cool/src/PA4J/`, seus `.class` são gerados lá — não na pasta local.

O script `semant` gerado pelo `Makefile` usa `-classpath .`, então qualquer `.class`
que esteja só em `/var/tmp/cool/src/PA4J/` não é encontrado em tempo de execução.

Os dois únicos arquivos afetados são `SymbolTable.java` e `SymtabExample.java`.
Depois de copiados localmente, o `Makefile` nunca os sobrescreve (ele recompila os
symlinks e os `.class` vão para PA4J, mas a cópia local fica intacta).

---

## Como Entregar

O PDF pede compactar o diretório e submeter pelo Canvas:

```bash
# Do diretório pai (Compilers/)
tar cvzf PA4.tar.gz semantic/
uuencode PA4.tar.gz PA4.tar.gz > PA4.u
rm PA4.tar.gz
# Submeter PA4.u pelo Canvas
```

O `gmake dotest` gera `good.output` e `bad.output` automaticamente — certifique-se
de rodar antes de empacotar.

**Arquivos que precisam estar no pacote:**

| Arquivo | Status |
|---|---|
| `cool-tree.java` | ✅ implementado |
| `ClassTable.java` | ✅ implementado |
| `TreeConstants.java` | ✅ presente (sem alterações) |
| `good.cl` | ✅ 12 classes, zero erros |
| `bad.cl` | ✅ 29 erros recuperáveis |
| `good.output` | ✅ gerado por `gmake dotest` |
| `bad.output` | ✅ gerado por `gmake dotest` |
| `README` | ✅ write-up completo |

---

## O Que Foi Implementado

### `ClassTable.java` — reescrito completamente

**Estrutura central:** `HashMap<AbstractSymbol, class_> classMap` — mapeia nome de
classe para seu nó AST. Usado em todo o Passo 2 para resolver tipos.

**Construtor `ClassTable(Classes cls)` — sequência de validações:**

1. Instala as 5 classes básicas (`Object`, `IO`, `Int`, `Bool`, `String`) no `classMap`.
2. Registra classes do usuário, detectando redefinição de básicas e nomes duplicados.
3. Verifica restrições de herança: proibido herdar de `Int`/`Bool`/`String`; pai deve existir.
4. Detecta ciclos: para cada classe, sobe a cadeia de pais com um `HashSet`; se encontrar nó repetido antes de `Object`, há ciclo.
5. Verifica que `Main` existe e tem método `main()` sem parâmetros.

Se houver qualquer erro aqui → `System.exit(1)`. Sem hierarquia válida não dá para fazer type-checking.

**Métodos auxiliares (usados pelo Passo 2):**

```java
boolean isSubtype(child, ancestor, currentClass)
// Sobe a cadeia de herança de child até encontrar ancestor.
// Resolve SELF_TYPE → currentClass antes de comparar.

AbstractSymbol lub(t1, t2, currentClass)
// Ancestral comum mais próximo — usado em if/else e case.
// Constrói lista de ancestrais de t1, sobe t2 até encontrar o primeiro que está na lista.

class_ getClass_(name)
// Retorna o nó AST da classe pelo nome.

HashMap<AbstractSymbol, method> getMethodEnv(className)
// Métodos visíveis (próprios + herdados). Recursivo, sobe até Object.

HashMap<AbstractSymbol, AbstractSymbol> getAttrEnv(className)
// Atributos visíveis (próprios + herdados). Mesmo esquema.

boolean isValidType(type)
// Verifica se um tipo existe no classMap ou é SELF_TYPE.
```

---

### `cool-tree.java` — adições ao arquivo existente

**`program.semant()`:** instancia `ClassTable` (Passo 1), aborta se há erros,
itera sobre todas as classes chamando `class_.semant(ct)` (Passo 2), aborta se há erros.

**`class_.semant(ClassTable ct)`:**
1. Cria `SymbolTable objEnv`, entra em escopo.
2. Insere `self : SELF_TYPE`.
3. Insere atributos herdados via `getAttrEnv(parent)`.
4. Valida atributos próprios: sem nome `self`, sem redefinição de herdados, sem duplicatas.
5. Chama `attr.semant()` ou `method.semant()` para cada feature.

**`attr.semant()`:** valida nome (≠ `self`), tipo declarado (deve existir), e conformidade da inicialização.

**`method.semant()`:** valida tipo de retorno, formais (nome, tipo, sem duplicatas),
compatibilidade com override do pai (tipos de params e retorno são invariantes em Cool),
e conformidade do corpo com o tipo de retorno declarado.

**`typecheck()` em cada nó de expressão:**

| Nó | Tipo resultante | Verificações principais |
|---|---|---|
| `int_const` | `Int` | — |
| `bool_const` | `Bool` | — |
| `string_const` | `Str` | — |
| `object` | lookup no `objEnv`; `self` → `SELF_TYPE` | erro se não declarado |
| `no_expr` | `No_type` | — |
| `assign` | tipo da expressão atribuída | nome ≠ `self`; tipo conforma |
| `plus/sub/mul/divide` | `Int` | ambos operandos devem ser `Int` |
| `neg` | `Int` | operando deve ser `Int` |
| `lt`, `leq` | `Bool` | ambos operandos devem ser `Int` |
| `eq` | `Bool` | se algum é `Int`/`Bool`/`String`, ambos devem ser do mesmo tipo |
| `comp` (`not`) | `Bool` | operando deve ser `Bool` |
| `isvoid` | `Bool` | — |
| `new_` | `type_name` | tipo deve existir (`SELF_TYPE` é permitido) |
| `block` | tipo da última expressão | — |
| `cond` | `lub(then, else)` | predicado deve ser `Bool` |
| `loop` | `Object` | condição deve ser `Bool` |
| `let` | tipo do corpo | nome ≠ `self`; tipo declarado existe; init conforma |
| `typcase` | `lub` de todos os branches | sem tipos duplicados entre branches |
| `dispatch` | tipo de retorno do método | método existe; args conformam |
| `static_dispatch` | tipo de retorno do método | tipo estático existe; receptor conforma |

**Detalhe de SELF_TYPE em dispatch:** se o tipo de retorno do método é `SELF_TYPE`,
o resultado é o tipo estático do receptor (não `SELF_TYPE` em si). Isso preserva
o tipo correto em cadeias de herança.

**Recuperação de erros:** expressão com tipo inválido recebe `Object` e a análise
continua. Evita cascata de erros falsos — mesmo comportamento do `coolc` de referência.

**Classes alias no final do arquivo:** `ASTParser.java` referencia `programc`,
`class_c` e `formalc` por nome. Como não existiam, adicionamos como subclasses
triviais no final de `cool-tree.java`. Sem isso a compilação quebra.

---

## Casos de Teste

### `good.cl` — 12 classes, zero erros esperados

| Classe | O que cobre |
|---|---|
| `Animal`, `Dog`, `Puppy` | Herança em 3 níveis; override com mesma assinatura |
| `TypesDemo` | Atributos `Int`, `Bool`, `String`, `Object` com inicialização |
| `Copyable`, `CopyChild` | `SELF_TYPE` como retorno, `new SELF_TYPE`, `self` como expressão |
| `MathOps` | `+`, `-`, `*`, `/`, `~`, `<`, `<=`, `=` com `Int` |
| `BoolOps` | `not`, `=` entre dois `Bool` |
| `LetDemo` | `let` aninhado com dependência entre variáveis; `let` sem init |
| `CaseDemo` | `case` com 4 branches, tipo resultado = LUB dos branches |
| `LoopDemo` | `while` com condição `Bool` |
| `DispatchDemo` | Dispatch normal, static dispatch, dispatch em `new` direto |
| `IsVoidDemo` | `isvoid` com objeto não-nulo e com variável não inicializada |
| `IODemo` | Herança de `IO`; `out_string`, `in_int` |
| `Main` | Combina herança/polimorfismo, `let`, `case`, dispatch, `isvoid`, `SELF_TYPE` |

### `bad.cl` — 29 erros, todos recuperáveis

Classes auxiliares simples (`Base`, `BaseWithParam`, `ParentWithAttr`) ficam no topo
para os testes de override/herança. Os erros de expressão ficam concentrados em `Main`
para facilitar a leitura. Essa separação é necessária porque erros de hierarquia causam
`System.exit(1)` imediato e não podem misturar com erros de tipo (recuperáveis).

| # | Erro testado |
|---|---|
| 1 | Override com tipo de retorno diferente |
| 2 | Override com tipo de parâmetro diferente |
| 3 | Redefinição de atributo herdado |
| 4 | `self` como nome de atributo |
| 5 | `self` como parâmetro de método |
| 6 | Tipo de atributo não definido |
| 7 | Tipo de parâmetro não definido |
| 8 | Tipo de retorno não definido |
| 9–11 | Inicialização incompatível de atributo (3 combinações de tipos) |
| 12 | Identificador não declarado |
| 13 | Atribuição a `self` |
| 14 | `+` com `Bool` |
| 15 | `-` com `String` |
| 16 | `not` em `Int` |
| 17 | `~` em `Bool` |
| 18 | `<` com `String` |
| 19 | `=` entre `Int` e `Bool` |
| 20 | `=` entre `Int` e `String` |
| 21 | Predicado de `if` não é `Bool` |
| 22 | Condição de `while` não é `Bool` |
| 23 | Dispatch para método inexistente |
| 24 | Número errado de argumentos |
| 25 | Argumento de tipo errado |
| 26 | Static dispatch com tipo não definido |
| 27 | `new` de tipo não definido |
| 28 | `let` com tipo não definido |
| 29 | `let` com inicialização incompatível |
| 30 | `self` como variável de `let` |

> **Erros de hierarquia** (herdar de `Int`/`Bool`/`String`, pai não definido, ciclos)
> causam `System.exit(1)` imediato. Precisam ser testados em arquivos separados.

---

## Problemas Encontrados Durante o Desenvolvimento

Registrados aqui para quem precisar depurar situações parecidas.

**1. `ClassNotFoundException: SymbolTable` ao rodar `gmake dotest`**

Causa: `SymbolTable.java` é symlink para PA4J, então `javac` grava `SymbolTable.class`
em PA4J, não localmente. O script `semant` usa `-classpath .` e não encontra a classe.

Solução: `cp /var/tmp/cool/src/PA4J/SymbolTable.class .` (e o mesmo para `SymtabExample`).

**2. `cannot find symbol: class programc` (e `class_c`, `formalc`)**

Causa: `ASTParser.java` referencia essas classes por nome, mas elas não existiam
em `cool-tree.java`.

Solução: adicionamos subclasses triviais no final de `cool-tree.java`:
```java
class programc extends program { ... }
class class_c extends class_ { ... }
class formalc extends formal { ... }
```

**3. Compilação duplicada no `gmake semant`**

O `Makefile` roda `javac` duas vezes por design (bug conhecido na configuração do curso).
É normal — pode ignorar.

**4. `gmake dotest` sempre reconstrói o script `semant`**

A regra `semant` no Makefile depende de `cool-tree.class`, que é tocado após cada
compilação. Isso faz o `semant` ser sempre recriado. O script recriado usa o classpath
padrão (`.`), o que funciona corretamente desde que os `.class` de PA4J estejam
copiados localmente.
