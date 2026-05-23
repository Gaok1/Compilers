import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/** Tabela de classes do analisador semântico do COOL.
 *
 *  Construir esta estrutura faz duas coisas:
 *    1. instala as classes básicas (Object, IO, Int, Bool, String);
 *    2. percorre as classes do usuário e verifica restrições estruturais
 *       de herança (classes redefinidas, heranças ilegais, pais
 *       inexistentes, ciclos e ausência da classe Main).
 *
 *  Após a construção, a tabela também serve como serviço de consulta
 *  durante o type-checking: lookup de classe, de método e de atributo
 *  (com herança), além de conforms (subtipo) e join (LUB).
 */
class ClassTable {
    private int semantErrors;
    private PrintStream errorStream;

    /** Mapa nome -> nó class_c (inclui as básicas). LinkedHashMap para
     *  preservar a ordem de inserção e tornar a ordem de erros estável. */
    private final Map<AbstractSymbol, class_c> classByName = new LinkedHashMap<AbstractSymbol, class_c>();

    /** Mapa nome -> nome do pai. Útil para inheritsFrom/conforms/lub. */
    private final Map<AbstractSymbol, AbstractSymbol> parentOf = new LinkedHashMap<AbstractSymbol, AbstractSymbol>();

    /** Conjunto de classes básicas (não podem ser redefinidas nem herdadas em alguns casos). */
    private final Set<AbstractSymbol> basicClasses = new HashSet<AbstractSymbol>();

    /** true se o grafo de herança está bem formado e podemos prosseguir
     *  para o type-checking. Quando false, program.semant() interrompe. */
    private boolean inheritanceOk = true;

    public ClassTable(Classes cls) {
        semantErrors = 0;
        errorStream = System.err;

        installBasicClasses();

        // 1) Registra todas as classes do usuário, detectando redefinições
        //    e heranças ilegais antes de qualquer outra checagem.
        for (Enumeration e = cls.getElements(); e.hasMoreElements(); ) {
            class_c c = (class_c) e.nextElement();
            AbstractSymbol name = c.getName();
            AbstractSymbol parent = c.getParent();

            if (name == TreeConstants.SELF_TYPE) {
                semantError(c).println("Redefinition of SELF_TYPE.");
                inheritanceOk = false;
                continue;
            }
            if (basicClasses.contains(name)) {
                semantError(c).println("Redefinition of basic class " + name + ".");
                inheritanceOk = false;
                continue;
            }
            if (classByName.containsKey(name)) {
                semantError(c).println("Class " + name + " was previously defined.");
                inheritanceOk = false;
                continue;
            }
            if (parent == TreeConstants.Int
                || parent == TreeConstants.Bool
                || parent == TreeConstants.Str
                || parent == TreeConstants.SELF_TYPE) {
                semantError(c).println("Class " + name
                    + " cannot inherit class " + parent + ".");
                inheritanceOk = false;
                continue;
            }

            classByName.put(name, c);
            parentOf.put(name, parent);
        }

        // 2) Verifica que todo pai citado existe como classe definida.
        for (Map.Entry<AbstractSymbol, AbstractSymbol> entry : parentOf.entrySet()) {
            AbstractSymbol name = entry.getKey();
            AbstractSymbol parent = entry.getValue();
            if (basicClasses.contains(name)) continue;
            if (!classByName.containsKey(parent)) {
                class_c c = classByName.get(name);
                semantError(c).println("Class " + name
                    + " inherits from an undefined class " + parent + ".");
                inheritanceOk = false;
            }
        }

        // 3) Detecta ciclos de herança. Se não houver caminho a Object,
        //    o nome está num ciclo (ou herda de classe inexistente,
        //    o que já reportamos acima — pula esse caso).
        if (inheritanceOk) {
            for (AbstractSymbol name : classByName.keySet()) {
                if (basicClasses.contains(name)) continue;
                if (hasCycle(name)) {
                    class_c c = classByName.get(name);
                    semantError(c).println("Class " + name
                        + ", or an ancestor of " + name
                        + ", is involved in an inheritance cycle.");
                    inheritanceOk = false;
                }
            }
        }

        // 4) Exige Main com método main() sem argumentos.
        if (inheritanceOk) {
            class_c mainCls = classByName.get(TreeConstants.Main);
            if (mainCls == null) {
                semantError().println("Class Main is not defined.");
            }
        }
    }

    /** Instala Object, IO, Int, Bool e String no grafo de herança. */
    private void installBasicClasses() {
        AbstractSymbol filename
            = AbstractTable.stringtable.addString("<basic class>");

        class_c Object_class =
            new class_c(0,
                       TreeConstants.Object_,
                       TreeConstants.No_class,
                       new Features(0)
                           .appendElement(new method(0,
                                              TreeConstants.cool_abort,
                                              new Formals(0),
                                              TreeConstants.Object_,
                                              new no_expr(0)))
                           .appendElement(new method(0,
                                              TreeConstants.type_name,
                                              new Formals(0),
                                              TreeConstants.Str,
                                              new no_expr(0)))
                           .appendElement(new method(0,
                                              TreeConstants.copy,
                                              new Formals(0),
                                              TreeConstants.SELF_TYPE,
                                              new no_expr(0))),
                       filename);

        class_c IO_class =
            new class_c(0,
                       TreeConstants.IO,
                       TreeConstants.Object_,
                       new Features(0)
                           .appendElement(new method(0,
                                              TreeConstants.out_string,
                                              new Formals(0)
                                                  .appendElement(new formalc(0,
                                                                     TreeConstants.arg,
                                                                     TreeConstants.Str)),
                                              TreeConstants.SELF_TYPE,
                                              new no_expr(0)))
                           .appendElement(new method(0,
                                              TreeConstants.out_int,
                                              new Formals(0)
                                                  .appendElement(new formalc(0,
                                                                     TreeConstants.arg,
                                                                     TreeConstants.Int)),
                                              TreeConstants.SELF_TYPE,
                                              new no_expr(0)))
                           .appendElement(new method(0,
                                              TreeConstants.in_string,
                                              new Formals(0),
                                              TreeConstants.Str,
                                              new no_expr(0)))
                           .appendElement(new method(0,
                                              TreeConstants.in_int,
                                              new Formals(0),
                                              TreeConstants.Int,
                                              new no_expr(0))),
                       filename);

        class_c Int_class =
            new class_c(0,
                       TreeConstants.Int,
                       TreeConstants.Object_,
                       new Features(0)
                           .appendElement(new attr(0,
                                            TreeConstants.val,
                                            TreeConstants.prim_slot,
                                            new no_expr(0))),
                       filename);

        class_c Bool_class =
            new class_c(0,
                       TreeConstants.Bool,
                       TreeConstants.Object_,
                       new Features(0)
                           .appendElement(new attr(0,
                                            TreeConstants.val,
                                            TreeConstants.prim_slot,
                                            new no_expr(0))),
                       filename);

        class_c Str_class =
            new class_c(0,
                       TreeConstants.Str,
                       TreeConstants.Object_,
                       new Features(0)
                           .appendElement(new attr(0,
                                            TreeConstants.val,
                                            TreeConstants.Int,
                                            new no_expr(0)))
                           .appendElement(new attr(0,
                                            TreeConstants.str_field,
                                            TreeConstants.prim_slot,
                                            new no_expr(0)))
                           .appendElement(new method(0,
                                              TreeConstants.length,
                                              new Formals(0),
                                              TreeConstants.Int,
                                              new no_expr(0)))
                           .appendElement(new method(0,
                                              TreeConstants.concat,
                                              new Formals(0)
                                                  .appendElement(new formalc(0,
                                                                     TreeConstants.arg,
                                                                     TreeConstants.Str)),
                                              TreeConstants.Str,
                                              new no_expr(0)))
                           .appendElement(new method(0,
                                              TreeConstants.substr,
                                              new Formals(0)
                                                  .appendElement(new formalc(0,
                                                                     TreeConstants.arg,
                                                                     TreeConstants.Int))
                                                  .appendElement(new formalc(0,
                                                                     TreeConstants.arg2,
                                                                     TreeConstants.Int)),
                                              TreeConstants.Str,
                                              new no_expr(0))),
                       filename);

        registerBasic(Object_class);
        registerBasic(IO_class);
        registerBasic(Int_class);
        registerBasic(Bool_class);
        registerBasic(Str_class);
    }

    private void registerBasic(class_c c) {
        classByName.put(c.getName(), c);
        parentOf.put(c.getName(), c.getParent());
        basicClasses.add(c.getName());
    }

    /** Retorna true se name é uma das classes básicas instaladas. */
    public boolean isBasic(AbstractSymbol name) {
        return basicClasses.contains(name);
    }

    /** Retorna true se a classe name existe (básica ou definida pelo usuário). */
    public boolean classExists(AbstractSymbol name) {
        return classByName.containsKey(name);
    }

    /** Recupera o nó class_c pelo nome (ou null se não existe). */
    public class_c lookupClass(AbstractSymbol name) {
        return classByName.get(name);
    }

    /** Enumera todas as classes registradas (inclui básicas). */
    public Vector<class_c> allClasses() {
        return new Vector<class_c>(classByName.values());
    }

    /** True quando todas as verificações estruturais passaram. */
    public boolean inheritanceWellFormed() {
        return inheritanceOk;
    }

    /** Detecta ciclo seguindo a cadeia de pais a partir de name. */
    private boolean hasCycle(AbstractSymbol name) {
        Set<AbstractSymbol> visited = new HashSet<AbstractSymbol>();
        AbstractSymbol cur = name;
        while (cur != null && cur != TreeConstants.No_class) {
            if (!visited.add(cur)) return true;
            cur = parentOf.get(cur);
            if (cur == null) return false;
        }
        return false;
    }

    /** Verifica se child herda de ancestor (estritamente, sem SELF_TYPE).
     *  Reflexivo: inheritsFrom(T, T) = true. */
    public boolean inheritsFrom(AbstractSymbol child, AbstractSymbol ancestor) {
        AbstractSymbol cur = child;
        while (cur != null && cur != TreeConstants.No_class) {
            if (cur == ancestor) return true;
            cur = parentOf.get(cur);
        }
        return false;
    }

    /** Relação de conformidade do COOL (sub <= sup), considerando SELF_TYPE.
     *  - SELF_TYPE_C  <=  SELF_TYPE_C
     *  - SELF_TYPE_C  <=  T   sse  C <= T
     *  - T            <=  SELF_TYPE   nunca
     *  - T1 <= T2 quando T2 é ancestral de T1 no grafo de herança. */
    public boolean conforms(AbstractSymbol sub, AbstractSymbol sup, AbstractSymbol currentClass) {
        if (sub == null || sup == null) return false;
        if (sub == TreeConstants.SELF_TYPE && sup == TreeConstants.SELF_TYPE) {
            return true;
        }
        if (sup == TreeConstants.SELF_TYPE) {
            return false;
        }
        AbstractSymbol resolvedSub = (sub == TreeConstants.SELF_TYPE) ? currentClass : sub;
        return inheritsFrom(resolvedSub, sup);
    }

    /** Menor ancestral comum (join) de duas classes, considerando SELF_TYPE.
     *  Retorna SELF_TYPE quando ambos são SELF_TYPE da mesma classe; do contrário
     *  resolve SELF_TYPE para a classe corrente antes de calcular o LCA. */
    public AbstractSymbol join(AbstractSymbol t1, AbstractSymbol t2, AbstractSymbol currentClass) {
        if (t1 == TreeConstants.SELF_TYPE && t2 == TreeConstants.SELF_TYPE) {
            return TreeConstants.SELF_TYPE;
        }
        AbstractSymbol a = (t1 == TreeConstants.SELF_TYPE) ? currentClass : t1;
        AbstractSymbol b = (t2 == TreeConstants.SELF_TYPE) ? currentClass : t2;
        if (a == null || b == null) return TreeConstants.Object_;
        if (!classByName.containsKey(a) || !classByName.containsKey(b)) {
            return TreeConstants.Object_;
        }
        Set<AbstractSymbol> ancestorsA = new HashSet<AbstractSymbol>();
        AbstractSymbol cur = a;
        while (cur != null && cur != TreeConstants.No_class) {
            ancestorsA.add(cur);
            cur = parentOf.get(cur);
        }
        cur = b;
        while (cur != null && cur != TreeConstants.No_class) {
            if (ancestorsA.contains(cur)) return cur;
            cur = parentOf.get(cur);
        }
        return TreeConstants.Object_;
    }

    /** Procura um método na classe className ou em algum ancestral.
     *  Retorna o nó method declarado, ou null se nenhum encontrado. */
    public method lookupMethod(AbstractSymbol className, AbstractSymbol methodName) {
        AbstractSymbol cur = (className == TreeConstants.SELF_TYPE) ? null : className;
        while (cur != null && cur != TreeConstants.No_class) {
            class_c c = classByName.get(cur);
            if (c == null) return null;
            for (Enumeration e = c.features.getElements(); e.hasMoreElements(); ) {
                Feature f = (Feature) e.nextElement();
                if (f instanceof method && ((method) f).name == methodName) {
                    return (method) f;
                }
            }
            cur = parentOf.get(cur);
        }
        return null;
    }

    /** Procura um atributo na classe className ou em algum ancestral.
     *  Retorna o nó attr declarado, ou null se nenhum encontrado. */
    public attr lookupAttr(AbstractSymbol className, AbstractSymbol attrName) {
        AbstractSymbol cur = (className == TreeConstants.SELF_TYPE) ? null : className;
        while (cur != null && cur != TreeConstants.No_class) {
            class_c c = classByName.get(cur);
            if (c == null) return null;
            for (Enumeration e = c.features.getElements(); e.hasMoreElements(); ) {
                Feature f = (Feature) e.nextElement();
                if (f instanceof attr && ((attr) f).name == attrName) {
                    return (attr) f;
                }
            }
            cur = parentOf.get(cur);
        }
        return null;
    }

    /** Verifica que a classe Main tem um método main() sem parâmetros.
     *  Chamado pelo program.semant() depois que as features foram coletadas. */
    public void checkMainMethod() {
        class_c main = classByName.get(TreeConstants.Main);
        if (main == null) return;
        method m = lookupMethod(TreeConstants.Main, TreeConstants.main_meth);
        if (m == null) {
            semantError(main).println("No 'main' method in class Main.");
            return;
        }
        if (m.formals.getLength() != 0) {
            // O semant de referência reporta esse erro na linha da
            // declaração da classe Main, não na linha do método.
            semantError(main)
                .println("'main' method in class Main should have no arguments.");
        }
    }

    /** Imprime nome do arquivo e número da linha da classe e
     *  incrementa o contador de erros semânticos. */
    public PrintStream semantError(class_c c) {
        return semantError(c.getFilename(), c);
    }

    /** Imprime nome do arquivo e número da linha do nó. */
    public PrintStream semantError(AbstractSymbol filename, TreeNode t) {
        errorStream.print(filename + ":" + t.getLineNumber() + ": ");
        return semantError();
    }

    /** Incrementa contador e devolve o stream para escrita do erro. */
    public PrintStream semantError() {
        semantErrors++;
        return errorStream;
    }

    /** True quando algum erro semântico foi reportado. */
    public boolean errors() {
        return semantErrors != 0;
    }

    // =====================================================================
    //  Type-checking (segunda fase)
    //
    //  Implementamos o type-checking como um visitor em cima da AST, em vez
    //  de espalhar métodos typecheck por cada classe de cool-tree.java. Isso
    //  concentra toda a lógica de tipos em um único arquivo e mantém
    //  cool-tree.java praticamente intocado.
    //
    //  Cada expressão recebe (env, currentClass) e retorna o seu tipo
    //  estático. O tipo também é gravado no nó via set_type, conforme
    //  exigido pela interface com o gerador de código.
    // =====================================================================

    /** Realiza o type-checking do programa. Deve ser chamado depois que o
     *  grafo de herança foi verificado (inheritanceWellFormed()). */
    public void typecheck(programc prog) {
        for (Enumeration e = prog.classes.getElements(); e.hasMoreElements(); ) {
            class_c c = (class_c) e.nextElement();
            typecheckClass(c);
        }
        checkMainMethod();
    }

    /** Type-checa uma classe do usuário. Monta o ambiente de objetos
     *  com todos os atributos da classe e dos ancestrais, e então
     *  visita cada feature. */
    private void typecheckClass(class_c c) {
        SymbolTable env = new SymbolTable();

        // Escopo externo: self + atributos herdados. Manter num escopo
        // separado permite que probe() em declareOwnAttrs() veja apenas
        // os atributos da própria classe.
        env.enterScope();
        env.addId(TreeConstants.self, TreeConstants.SELF_TYPE);
        collectInheritedAttrs(c, env);

        // Escopo interno: atributos declarados na classe atual.
        env.enterScope();
        declareOwnAttrs(c, env);

        // Passo estrutural dos métodos: verifica override antes de
        // type-checar qualquer corpo, alinhando a ordem das mensagens
        // com o compilador de referência.
        for (Enumeration e = c.features.getElements(); e.hasMoreElements(); ) {
            Feature f = (Feature) e.nextElement();
            if (f instanceof method) {
                method m = (method) f;
                method inherited = lookupInheritedMethod(c.getName(), m.name);
                if (inherited != null) {
                    checkOverrideSignature(m, inherited, c);
                }
            }
        }

        // Type-check de corpos (atributos primeiro, depois métodos).
        for (Enumeration e = c.features.getElements(); e.hasMoreElements(); ) {
            Feature f = (Feature) e.nextElement();
            if (f instanceof attr) {
                typecheckAttr((attr) f, env, c);
            }
        }
        for (Enumeration e = c.features.getElements(); e.hasMoreElements(); ) {
            Feature f = (Feature) e.nextElement();
            if (f instanceof method) {
                typecheckMethod((method) f, env, c);
            }
        }
        env.exitScope();
        env.exitScope();
    }

    /** Adiciona todos os atributos herdados (de qualquer ancestral) ao env. */
    private void collectInheritedAttrs(class_c c, SymbolTable env) {
        AbstractSymbol cur = parentOf.get(c.getName());
        // Empilha caminho dos ancestrais (Object primeiro) para que filhos
        // possam sobrescrever ao descobrir redefinição.
        java.util.ArrayList<AbstractSymbol> chain = new java.util.ArrayList<AbstractSymbol>();
        while (cur != null && cur != TreeConstants.No_class) {
            chain.add(0, cur);
            cur = parentOf.get(cur);
        }
        for (AbstractSymbol name : chain) {
            class_c anc = classByName.get(name);
            if (anc == null) continue;
            for (Enumeration e = anc.features.getElements(); e.hasMoreElements(); ) {
                Feature f = (Feature) e.nextElement();
                if (f instanceof attr) {
                    attr a = (attr) f;
                    env.addId(a.name, a.type_decl);
                }
            }
        }
    }

    /** Adiciona os atributos próprios da classe, reportando redefinições
     *  contra atributos herdados ou duplicados no mesmo escopo. */
    private void declareOwnAttrs(class_c c, SymbolTable env) {
        for (Enumeration e = c.features.getElements(); e.hasMoreElements(); ) {
            Feature f = (Feature) e.nextElement();
            if (!(f instanceof attr)) continue;
            attr a = (attr) f;

            if (a.name == TreeConstants.self) {
                semantError(c.getFilename(), a)
                    .println("'self' cannot be the name of an attribute.");
                continue;
            }
            // Redefinição de atributo herdado (probe pega scope corrente,
            // lookup pega herdados via ancestor chain já adicionados).
            if (env.probe(a.name) != null) {
                semantError(c.getFilename(), a)
                    .println("Attribute " + a.name + " is multiply defined in class.");
                continue;
            }
            if (env.lookup(a.name) != null) {
                semantError(c.getFilename(), a)
                    .println("Attribute " + a.name + " is an attribute of an inherited class.");
                continue;
            }
            env.addId(a.name, a.type_decl);
        }
    }

    private void typecheckAttr(attr a, SymbolTable env, class_c current) {
        // O tipo declarado precisa existir (ou ser SELF_TYPE).
        if (a.type_decl != TreeConstants.SELF_TYPE && !classExists(a.type_decl)) {
            semantError(current.getFilename(), a)
                .println("Class " + a.type_decl + " of attribute " + a.name + " is undefined.");
        }
        // Só checa a inicialização se houver. no_expr fica com _no_type.
        if (!(a.init instanceof no_expr)) {
            AbstractSymbol initType = typecheckExpr(a.init, env, current);
            if (initType != null && !conforms(initType, a.type_decl, current.getName())) {
                semantError(current.getFilename(), a)
                    .println("Inferred type " + initType
                        + " of initialization of attribute " + a.name
                        + " does not conform to declared type " + a.type_decl + ".");
            }
        } else {
            a.init.set_type(TreeConstants.No_type);
        }
    }

    private void typecheckMethod(method m, SymbolTable env, class_c current) {
        // Valida o tipo de retorno. Se for inexistente, evitamos checar
        // a conformidade do corpo para não emitir erro em cascata.
        boolean returnTypeOk = (m.return_type == TreeConstants.SELF_TYPE)
            || classExists(m.return_type);
        if (!returnTypeOk) {
            semantError(current.getFilename(), m)
                .println("Undefined return type " + m.return_type
                    + " in method " + m.name + ".");
        }

        // Entra no escopo do método e declara os formais.
        env.enterScope();
        java.util.HashSet<AbstractSymbol> seen = new java.util.HashSet<AbstractSymbol>();
        for (Enumeration e = m.formals.getElements(); e.hasMoreElements(); ) {
            formalc f = (formalc) e.nextElement();
            if (f.name == TreeConstants.self) {
                semantError(current.getFilename(), f)
                    .println("'self' cannot be the name of a formal parameter.");
                continue;
            }
            // Mesmo tipos inválidos (SELF_TYPE ou inexistente) ficam no
            // env com o nome declarado: a referência reporta o erro mas
            // mantém o tipo escrito pelo usuário para próximas checagens.
            if (f.type_decl == TreeConstants.SELF_TYPE) {
                semantError(current.getFilename(), f)
                    .println("Formal parameter " + f.name
                        + " cannot have type SELF_TYPE.");
            } else if (!classExists(f.type_decl)) {
                semantError(current.getFilename(), f)
                    .println("Class " + f.type_decl + " of formal parameter "
                        + f.name + " is undefined.");
            }
            if (!seen.add(f.name)) {
                semantError(current.getFilename(), f)
                    .println("Formal parameter " + f.name + " is multiply defined.");
                continue;
            }
            env.addId(f.name, f.type_decl);
        }

        // Type-check do corpo.
        AbstractSymbol bodyType = typecheckExpr(m.expr, env, current);
        if (returnTypeOk && bodyType != null
            && !conforms(bodyType, m.return_type, current.getName())) {
            semantError(current.getFilename(), m)
                .println("Inferred return type " + bodyType
                    + " of method " + m.name
                    + " does not conform to declared return type "
                    + m.return_type + ".");
        }
        env.exitScope();
    }

    /** Devolve o método herdado de mesmo nome (ou null se for declaração inicial). */
    private method lookupInheritedMethod(AbstractSymbol className, AbstractSymbol methodName) {
        AbstractSymbol cur = parentOf.get(className);
        while (cur != null && cur != TreeConstants.No_class) {
            class_c c = classByName.get(cur);
            if (c == null) return null;
            for (Enumeration e = c.features.getElements(); e.hasMoreElements(); ) {
                Feature f = (Feature) e.nextElement();
                if (f instanceof method && ((method) f).name == methodName) {
                    return (method) f;
                }
            }
            cur = parentOf.get(cur);
        }
        return null;
    }

    private void checkOverrideSignature(method m, method inherited, class_c current) {
        // O compilador de referência reporta no máximo UM erro por
        // método sobrescrito, com prioridade:
        //   1. tipo de retorno diferente
        //   2. quantidade de formais diferente
        //   3. tipo de algum formal diferente
        // Seguimos a mesma ordem para casar as mensagens.
        if (m.return_type != inherited.return_type) {
            semantError(current.getFilename(), m)
                .println("In redefined method " + m.name
                    + ", return type " + m.return_type
                    + " is different from original return type "
                    + inherited.return_type + ".");
            return;
        }
        int n1 = m.formals.getLength();
        int n2 = inherited.formals.getLength();
        if (n1 != n2) {
            semantError(current.getFilename(), m)
                .println("Incompatible number of formal parameters in redefined method "
                    + m.name + ".");
            return;
        }
        Enumeration a = m.formals.getElements();
        Enumeration b = inherited.formals.getElements();
        while (a.hasMoreElements()) {
            formalc fa = (formalc) a.nextElement();
            formalc fb = (formalc) b.nextElement();
            if (fa.type_decl != fb.type_decl) {
                // Sem ponto final no fim — alinha byte-a-byte com a referência.
                semantError(current.getFilename(), m)
                    .println("In redefined method " + m.name
                        + ", parameter type " + fa.type_decl
                        + " is different from original type " + fb.type_decl);
            }
        }
    }

    // ---------------------------------------------------------------------
    //  Type-check de expressões. Sempre retorna um tipo (Object em caso
    //  de erro irrecuperável). O nó é decorado via set_type.
    // ---------------------------------------------------------------------

    private AbstractSymbol typecheckExpr(Expression e, SymbolTable env, class_c current) {
        AbstractSymbol type;
        if (e instanceof int_const) {
            type = TreeConstants.Int;
        } else if (e instanceof string_const) {
            type = TreeConstants.Str;
        } else if (e instanceof bool_const) {
            type = TreeConstants.Bool;
        } else if (e instanceof object) {
            type = checkObject((object) e, env, current);
        } else if (e instanceof assign) {
            type = checkAssign((assign) e, env, current);
        } else if (e instanceof new_) {
            type = checkNew((new_) e, env, current);
        } else if (e instanceof dispatch) {
            type = checkDispatch((dispatch) e, env, current);
        } else if (e instanceof static_dispatch) {
            type = checkStaticDispatch((static_dispatch) e, env, current);
        } else if (e instanceof cond) {
            type = checkCond((cond) e, env, current);
        } else if (e instanceof loop) {
            type = checkLoop((loop) e, env, current);
        } else if (e instanceof block) {
            type = checkBlock((block) e, env, current);
        } else if (e instanceof let) {
            type = checkLet((let) e, env, current);
        } else if (e instanceof typcase) {
            type = checkCase((typcase) e, env, current);
        } else if (e instanceof plus) {
            type = checkArith(((plus) e).e1, ((plus) e).e2, e, "+", env, current);
        } else if (e instanceof sub) {
            type = checkArith(((sub) e).e1, ((sub) e).e2, e, "-", env, current);
        } else if (e instanceof mul) {
            type = checkArith(((mul) e).e1, ((mul) e).e2, e, "*", env, current);
        } else if (e instanceof divide) {
            type = checkArith(((divide) e).e1, ((divide) e).e2, e, "/", env, current);
        } else if (e instanceof neg) {
            type = checkNeg((neg) e, env, current);
        } else if (e instanceof lt) {
            type = checkRelational(((lt) e).e1, ((lt) e).e2, e, "<", env, current);
        } else if (e instanceof leq) {
            type = checkRelational(((leq) e).e1, ((leq) e).e2, e, "<=", env, current);
        } else if (e instanceof eq) {
            type = checkEq((eq) e, env, current);
        } else if (e instanceof comp) {
            type = checkComp((comp) e, env, current);
        } else if (e instanceof isvoid) {
            // isvoid pode receber qualquer expressão.
            typecheckExpr(((isvoid) e).e1, env, current);
            type = TreeConstants.Bool;
        } else if (e instanceof no_expr) {
            type = TreeConstants.No_type;
        } else {
            // Cobrir caso desconhecido (não deveria acontecer).
            type = TreeConstants.Object_;
        }
        e.set_type(type);
        return type;
    }

    private AbstractSymbol checkObject(object o, SymbolTable env, class_c current) {
        if (o.name == TreeConstants.self) return TreeConstants.SELF_TYPE;
        AbstractSymbol declared = (AbstractSymbol) env.lookup(o.name);
        if (declared == null) {
            semantError(current.getFilename(), o)
                .println("Undeclared identifier " + o.name + ".");
            return TreeConstants.Object_;
        }
        return declared;
    }

    private AbstractSymbol checkAssign(assign a, SymbolTable env, class_c current) {
        if (a.name == TreeConstants.self) {
            semantError(current.getFilename(), a)
                .println("Cannot assign to 'self'.");
            typecheckExpr(a.expr, env, current);
            return TreeConstants.Object_;
        }
        AbstractSymbol declared = (AbstractSymbol) env.lookup(a.name);
        AbstractSymbol exprType = typecheckExpr(a.expr, env, current);
        if (declared == null) {
            semantError(current.getFilename(), a)
                .println("Assignment to undeclared variable " + a.name + ".");
            return exprType;
        }
        if (!conforms(exprType, declared, current.getName())) {
            semantError(current.getFilename(), a)
                .println("Type " + exprType
                    + " of assigned expression does not conform to declared type "
                    + declared + " of identifier " + a.name + ".");
            return declared;
        }
        return exprType;
    }

    private AbstractSymbol checkNew(new_ n, SymbolTable env, class_c current) {
        if (n.type_name == TreeConstants.SELF_TYPE) return TreeConstants.SELF_TYPE;
        if (!classExists(n.type_name)) {
            semantError(current.getFilename(), n)
                .println("'new' used with undefined class " + n.type_name + ".");
            return TreeConstants.Object_;
        }
        return n.type_name;
    }

    private AbstractSymbol checkDispatch(dispatch d, SymbolTable env, class_c current) {
        AbstractSymbol recvType = typecheckExpr(d.expr, env, current);
        AbstractSymbol lookupKey = (recvType == TreeConstants.SELF_TYPE) ? current.getName() : recvType;
        method m = lookupMethod(lookupKey, d.name);
        if (m == null) {
            semantError(current.getFilename(), d)
                .println("Dispatch to undefined method " + d.name + ".");
            // Ainda type-check dos argumentos para reportar mais erros.
            for (Enumeration e = d.actual.getElements(); e.hasMoreElements(); ) {
                typecheckExpr((Expression) e.nextElement(), env, current);
            }
            return TreeConstants.Object_;
        }
        checkActualArgs(d.actual, m, d.name, env, current, d);
        return (m.return_type == TreeConstants.SELF_TYPE) ? recvType : m.return_type;
    }

    private AbstractSymbol checkStaticDispatch(static_dispatch sd, SymbolTable env, class_c current) {
        AbstractSymbol recvType = typecheckExpr(sd.expr, env, current);
        if (sd.type_name == TreeConstants.SELF_TYPE) {
            semantError(current.getFilename(), sd)
                .println("Static dispatch to SELF_TYPE.");
            for (Enumeration e = sd.actual.getElements(); e.hasMoreElements(); ) {
                typecheckExpr((Expression) e.nextElement(), env, current);
            }
            return TreeConstants.Object_;
        }
        if (!classExists(sd.type_name)) {
            semantError(current.getFilename(), sd)
                .println("Static dispatch to undefined class " + sd.type_name + ".");
            for (Enumeration e = sd.actual.getElements(); e.hasMoreElements(); ) {
                typecheckExpr((Expression) e.nextElement(), env, current);
            }
            return TreeConstants.Object_;
        }
        // Se o receptor não conforma ao tipo alvo, reportamos só esse erro
        // e paramos — procurar o método ainda assim geraria cascata.
        if (!conforms(recvType, sd.type_name, current.getName())) {
            semantError(current.getFilename(), sd)
                .println("Expression type " + recvType
                    + " does not conform to declared static dispatch type "
                    + sd.type_name + ".");
            for (Enumeration e = sd.actual.getElements(); e.hasMoreElements(); ) {
                typecheckExpr((Expression) e.nextElement(), env, current);
            }
            return TreeConstants.Object_;
        }
        method m = lookupMethod(sd.type_name, sd.name);
        if (m == null) {
            semantError(current.getFilename(), sd)
                .println("Static dispatch to undefined method " + sd.name + ".");
            for (Enumeration e = sd.actual.getElements(); e.hasMoreElements(); ) {
                typecheckExpr((Expression) e.nextElement(), env, current);
            }
            return TreeConstants.Object_;
        }
        checkActualArgs(sd.actual, m, sd.name, env, current, sd);
        return (m.return_type == TreeConstants.SELF_TYPE) ? recvType : m.return_type;
    }

    private void checkActualArgs(Expressions actual, method m, AbstractSymbol callName,
                                 SymbolTable env, class_c current, TreeNode site) {
        int n1 = actual.getLength();
        int n2 = m.formals.getLength();
        if (n1 != n2) {
            semantError(current.getFilename(), site)
                .println("Method " + callName + " called with wrong number of arguments.");
            for (Enumeration e = actual.getElements(); e.hasMoreElements(); ) {
                typecheckExpr((Expression) e.nextElement(), env, current);
            }
            return;
        }
        Enumeration ea = actual.getElements();
        Enumeration ef = m.formals.getElements();
        while (ea.hasMoreElements()) {
            Expression arg = (Expression) ea.nextElement();
            formalc fm = (formalc) ef.nextElement();
            AbstractSymbol argType = typecheckExpr(arg, env, current);
            if (!conforms(argType, fm.type_decl, current.getName())) {
                semantError(current.getFilename(), site)
                    .println("In call of method " + callName
                        + ", type " + argType + " of parameter " + fm.name
                        + " does not conform to declared type " + fm.type_decl + ".");
            }
        }
    }

    private AbstractSymbol checkCond(cond c, SymbolTable env, class_c current) {
        AbstractSymbol predType = typecheckExpr(c.pred, env, current);
        if (predType != TreeConstants.Bool) {
            semantError(current.getFilename(), c)
                .println("Predicate of 'if' does not have type Bool.");
        }
        AbstractSymbol thenType = typecheckExpr(c.then_exp, env, current);
        AbstractSymbol elseType = typecheckExpr(c.else_exp, env, current);
        return join(thenType, elseType, current.getName());
    }

    private AbstractSymbol checkLoop(loop l, SymbolTable env, class_c current) {
        AbstractSymbol predType = typecheckExpr(l.pred, env, current);
        if (predType != TreeConstants.Bool) {
            semantError(current.getFilename(), l)
                .println("Loop condition does not have type Bool.");
        }
        typecheckExpr(l.body, env, current);
        return TreeConstants.Object_;
    }

    private AbstractSymbol checkBlock(block b, SymbolTable env, class_c current) {
        AbstractSymbol last = TreeConstants.Object_;
        for (Enumeration e = b.body.getElements(); e.hasMoreElements(); ) {
            Expression exp = (Expression) e.nextElement();
            last = typecheckExpr(exp, env, current);
        }
        return last;
    }

    private AbstractSymbol checkLet(let l, SymbolTable env, class_c current) {
        if (l.identifier == TreeConstants.self) {
            semantError(current.getFilename(), l)
                .println("'self' cannot be bound in a 'let' expression.");
        }
        boolean declOk = (l.type_decl == TreeConstants.SELF_TYPE)
            || classExists(l.type_decl);
        if (!declOk) {
            semantError(current.getFilename(), l)
                .println("Class " + l.type_decl
                    + " of let-bound identifier " + l.identifier + " is undefined.");
        }
        // Init opcional (no_expr quando ausente). Só checamos a
        // conformância quando o tipo declarado existe — caso contrário
        // a comparação é vacuamente falsa e geraria erro em cascata.
        if (!(l.init instanceof no_expr)) {
            AbstractSymbol initType = typecheckExpr(l.init, env, current);
            if (declOk && !conforms(initType, l.type_decl, current.getName())) {
                semantError(current.getFilename(), l)
                    .println("Inferred type " + initType
                        + " of initialization of " + l.identifier
                        + " does not conform to identifier's declared type "
                        + l.type_decl + ".");
            }
        } else {
            l.init.set_type(TreeConstants.No_type);
        }
        env.enterScope();
        if (l.identifier != TreeConstants.self) {
            env.addId(l.identifier, l.type_decl);
        }
        AbstractSymbol bodyType = typecheckExpr(l.body, env, current);
        env.exitScope();
        return bodyType;
    }

    private AbstractSymbol checkCase(typcase tc, SymbolTable env, class_c current) {
        typecheckExpr(tc.expr, env, current);
        AbstractSymbol result = null;
        java.util.HashSet<AbstractSymbol> branchTypes = new java.util.HashSet<AbstractSymbol>();
        for (Enumeration e = tc.cases.getElements(); e.hasMoreElements(); ) {
            branch br = (branch) e.nextElement();
            if (br.name == TreeConstants.self) {
                semantError(current.getFilename(), br)
                    .println("'self' bound in 'case'.");
            }
            boolean typeOk = true;
            if (br.type_decl == TreeConstants.SELF_TYPE) {
                semantError(current.getFilename(), br)
                    .println("Identifier " + br.name
                        + " declared with type SELF_TYPE in case branch.");
                typeOk = false;
            } else if (!classExists(br.type_decl)) {
                semantError(current.getFilename(), br)
                    .println("Class " + br.type_decl
                        + " of case branch is undefined.");
                typeOk = false;
            }
            if (!branchTypes.add(br.type_decl)) {
                semantError(current.getFilename(), br)
                    .println("Duplicate branch " + br.type_decl
                        + " in case statement.");
            }
            env.enterScope();
            if (br.name != TreeConstants.self) {
                env.addId(br.name, br.type_decl);
            }
            AbstractSymbol bt = typecheckExpr(br.expr, env, current);
            env.exitScope();
            // Branch com tipo declarado inválido contribui Object para
            // o join — evita propagar tipos fantasmas para fora do case.
            if (!typeOk) bt = TreeConstants.Object_;
            result = (result == null) ? bt : join(result, bt, current.getName());
        }
        return (result == null) ? TreeConstants.Object_ : result;
    }

    private AbstractSymbol checkArith(Expression e1, Expression e2, Expression site,
                                      String op, SymbolTable env, class_c current) {
        AbstractSymbol t1 = typecheckExpr(e1, env, current);
        AbstractSymbol t2 = typecheckExpr(e2, env, current);
        if (t1 != TreeConstants.Int || t2 != TreeConstants.Int) {
            semantError(current.getFilename(), site)
                .println("non-Int arguments: " + t1 + " " + op + " " + t2);
        }
        return TreeConstants.Int;
    }

    private AbstractSymbol checkNeg(neg n, SymbolTable env, class_c current) {
        AbstractSymbol t = typecheckExpr(n.e1, env, current);
        if (t != TreeConstants.Int) {
            semantError(current.getFilename(), n)
                .println("Argument of '~' has type " + t + " instead of Int.");
        }
        return TreeConstants.Int;
    }

    private AbstractSymbol checkRelational(Expression e1, Expression e2, Expression site,
                                           String op, SymbolTable env, class_c current) {
        AbstractSymbol t1 = typecheckExpr(e1, env, current);
        AbstractSymbol t2 = typecheckExpr(e2, env, current);
        if (t1 != TreeConstants.Int || t2 != TreeConstants.Int) {
            semantError(current.getFilename(), site)
                .println("non-Int arguments: " + t1 + " " + op + " " + t2);
        }
        return TreeConstants.Bool;
    }

    /** Igualdade. Se um lado é Int/Bool/String, os dois devem ter o mesmo tipo. */
    private AbstractSymbol checkEq(eq e, SymbolTable env, class_c current) {
        AbstractSymbol t1 = typecheckExpr(e.e1, env, current);
        AbstractSymbol t2 = typecheckExpr(e.e2, env, current);
        if (isPrimitiveCompareType(t1) || isPrimitiveCompareType(t2)) {
            if (t1 != t2) {
                semantError(current.getFilename(), e)
                    .println("Illegal comparison with a basic type.");
            }
        }
        return TreeConstants.Bool;
    }

    private boolean isPrimitiveCompareType(AbstractSymbol t) {
        return t == TreeConstants.Int
            || t == TreeConstants.Bool
            || t == TreeConstants.Str;
    }

    private AbstractSymbol checkComp(comp c, SymbolTable env, class_c current) {
        AbstractSymbol t = typecheckExpr(c.e1, env, current);
        if (t != TreeConstants.Bool) {
            semantError(current.getFilename(), c)
                .println("Argument of 'not' has type " + t + " instead of Bool.");
        }
        return TreeConstants.Bool;
    }
}
