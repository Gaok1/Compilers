
ASSN = 3J
CLASS= compiladores
CLASSDIR= /var/tmp/cool
LIB= -L/usr/lib -lfl
AR= ar
ARCHIVE_NEW= -cr
RANLIB= ar -qs

SRC= cool.cup tests/good.cl tests/bad.cl tests/bad_class_feature.cl tests/bad_let_block.cl README
CSRC= \
	AbstractSymbol.java \
	AbstractTable.java \
	BoolConst.java \
	CgenClassTable.java \
	CgenNode.java \
	CgenSupport.java \
	ClassTable.java \
	CoolTokenLexer.java \
	Flags.java \
	IdSymbol.java \
	IdTable.java \
	IntSymbol.java \
	IntTable.java \
	ListNode.java \
	Parser.java \
	StringSymbol.java \
	StringTable.java \
	SymbolTable.java \
	TreeConstants.java \
	TreeNode.java \
	Utilities.java \
	cool-tree.java
TSRC= myparser mycoolc cool-tree.aps
CGEN= CoolParser.java TokenConstants.java
LIBS= lexer semant cgen
CFIL= ${CSRC} ${CGEN}
HFIL= cool-tree.h cool-tree.handcode.h 
LSRC= Makefile
CLS= ${CFIL:.java=.class}
BAD_TESTS= tests/bad.cl tests/bad_class_feature.cl tests/bad_let_block.cl
BAD_OUTPUTS= ${BAD_TESTS:.cl=.output}
OUTPUT= tests/good.output ${BAD_OUTPUTS}

JAVAC := javac
CUP := java -jar ${CLASSDIR}/lib/java-cup-11a.jar

CUPCLASSPATH := ${CLASSDIR}/lib/java-cup-11a.jar:${CLASSDIR}/lib/jlex.jar

CLASSPATH := ${CUPCLASSPATH}:.
# Diretório onde javac coloca as classes compiladas via symlink
SRCDIR := ${CLASSDIR}/src/PA${ASSN}

.PHONY: all source dotest clean submit-clean

all: source parser

source: ${SRC} ${TSRC} ${LSRC} ${LIBS} ${CSRC}

${OUTPUT}:	source parser tests/good.cl ${BAD_TESTS}
	@rm -f ${OUTPUT}
	./myparser tests/good.cl >tests/good.output 2>&1
	@for test in ${BAD_TESTS}; do \
		out=$${test%.cl}.output; \
		./myparser $$test >$$out 2>&1 || true; \
	done

parser: Makefile ${CLS}
	@rm -f parser
	echo '#!/bin/sh' >> parser
	echo 'java -classpath ${CLASSPATH}:${SRCDIR}:`dirname $$0` Parser $$*' >> parser
	chmod 755 parser

CoolParser.java TokenConstants.java: cool.cup
	${CUP} -parser CoolParser -symbols TokenConstants -expect 10000 -dump -nopositions < $< >cool.output 2>&1 || { egrep '^Error at ' cool.output; false; }


dotest:	source parser tests/good.cl ${BAD_TESTS}
	@echo "\nRunning parser on good.cl\n"
	-./myparser tests/good.cl
	@for test in ${BAD_TESTS}; do \
		echo "\nRunning parser on $$(basename $$test)\n"; \
		./myparser $$test || true; \
	done

## cool-tree.class is not a real class file, but we need to have it
## for dependency tracking.
## Usamos -d . para forçar a saída dos .class no diretório atual,
## evitando que javac coloque os arquivos no diretório dos symlinks.
${CLS}: ${CFIL}
	${JAVAC} -g -nowarn -d . -classpath ${CLASSPATH} ${CFIL}
	touch cool-tree.class

${LIBS}:
	${CLASSDIR}/etc/link-object ${ASSN} $@

# These dependencies allow you to get the starting files for
# the assignment.  They will not overwrite a file you already have.

${SRC} :								
	-${CLASSDIR}/etc/copy-skel ${ASSN} ${SRC}

${LSRC} :
	-${CLASSDIR}/etc/link-shared ${ASSN} ${LSRC}

${TSRC} ${CSRC}:
	-ln -s ${CLASSDIR}/src/PA${ASSN}/$@ $@

${HSRC}:
	-ln -s ${CLASSDIR}/include/PA${ASSN}/$@ $@

submit-clean: ${OUTPUT}
	-rm -f cool.output *.s core *.class *~ ${CGEN} parser cgen semant lexer

clean:
	-rm -f cool.output *.s core *.class *~ ${CGEN} parser cgen semant lexer ${OUTPUT}
