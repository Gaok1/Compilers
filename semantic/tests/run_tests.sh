#!/bin/bash
#
# run_tests.sh — roda a suíte ampliada de testes do TP-04.
#
# Para cada arquivo .cl em semantic/tests/, executa o NOSSO mysemant e
# o SEMANT DE REFERÊNCIA fornecido pelo curso. Compara as duas saídas
# em "modo conjunto" (ordena as linhas antes do diff). Esse modo de
# comparação ignora apenas diferenças de ORDEM entre mensagens — o
# CONTEÚDO precisa bater, byte a byte, depois de ordenado.
#
# Por que comparar ordenado: nosso analisador e o de referência podem
# emitir os mesmos erros em ordens ligeiramente diferentes (ex: 3 erros
# de ciclo de herança). O manual não fixa a ordem; o que importa é o
# conjunto de mensagens.
#
# Uso (de dentro de semantic/):
#   ./tests/run_tests.sh                # roda tudo
#   ./tests/run_tests.sh -v             # modo verboso (mostra diff completo)
#
# Saída:
#   PASS xxxx.cl       quando o conjunto de saída bate
#   FAIL xxxx.cl       quando difere; um diff resumido segue logo abaixo
#
# Código de saída: 0 se todos passaram, 1 caso contrário.

set -u

VERBOSE=0
[ "${1:-}" = "-v" ] && VERBOSE=1

# Ancora em semantic/, independentemente de onde o script foi chamado.
HERE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$HERE"

REF="/var/tmp/cool/lib/.x86_64/semant"
if [ ! -x "$REF" ]; then
    echo "ERRO: semant de referência não encontrado em $REF"
    echo "      Esta suíte precisa do framework do curso instalado."
    exit 2
fi

# Garante que o nosso semant está compilado.
if [ ! -x "./semant" ] || [ ! -f "./mysemant" ]; then
    echo "Compilando semant..."
    make semant > /dev/null
fi

PASS=0
FAIL=0
FAILED=""

for cl in tests/*.cl; do
    name=$(basename "$cl")

    our_out=$(./mysemant "$cl" 2>&1)
    ref_out=$(./lexer "$cl" 2>/dev/null | ./parser "$cl" 2>/dev/null | "$REF" 2>&1)

    # Compara em modo "conjunto": ordena as linhas antes do diff.
    if diff <(printf '%s\n' "$ref_out" | sort) \
            <(printf '%s\n' "$our_out" | sort) > /dev/null; then
        printf "  \033[32mPASS\033[0m  %s\n" "$name"
        PASS=$((PASS + 1))
    else
        printf "  \033[31mFAIL\033[0m  %s\n" "$name"
        FAIL=$((FAIL + 1))
        FAILED="$FAILED $name"
        if [ "$VERBOSE" = "1" ]; then
            echo "        --- ref ---"
            printf '%s\n' "$ref_out" | sed 's/^/        /'
            echo "        --- ours ---"
            printf '%s\n' "$our_out" | sed 's/^/        /'
        else
            diff <(printf '%s\n' "$ref_out" | sort) \
                 <(printf '%s\n' "$our_out" | sort) \
                 | head -6 | sed 's/^/        /'
        fi
    fi
done

echo
echo "================================="
printf "Resultado: %d passaram, %d falharam\n" "$PASS" "$FAIL"
if [ "$FAIL" -gt 0 ]; then
    echo "Falhas:$FAILED"
    exit 1
fi
echo "Tudo verde."
