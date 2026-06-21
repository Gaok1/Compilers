#!/usr/bin/env bash
#
# Roda cada tests/*.cl pelo gerador de código + SPIM e compara a saída
# com o golden tests/*.out. Uso: tests/run.sh   (a partir de codegen/).
# O caminho do trap.handler pode ser sobrescrito com COOL_TRAP=...
#
set -u
cd "$(dirname "$0")/.."                 # codegen/
TRAP="${COOL_TRAP:-/var/tmp/cool/lib/trap.handler}"

# Remove o banner do SPIM e o aviso do símbolo "main" (não usado pelo
# runtime de COOL, que entra por __start), deixando só a saída do programa.
filter() {
    grep -vE '^(SPIM Version|Copyright|All Rights Reserved|See the file|Loaded: |The following symbols are undefined:|main$|Instruction references undefined|\[0x)' \
        | sed '/./,$!d'
}

pass=0; fail=0
for cl in tests/*.cl; do
    base="${cl%.cl}"
    exp="$base.out"
    [ -f "$exp" ] || { echo "SKIP  $cl (sem golden)"; continue; }
    ./mycoolc "$cl" >/dev/null 2>&1
    got="$(spim -exception_file "$TRAP" -file "$base.s" </dev/null 2>&1 | filter)"
    if [ "$got" = "$(cat "$exp")" ]; then
        echo "PASS  $cl"; pass=$((pass + 1))
    else
        echo "FAIL  $cl"; fail=$((fail + 1))
        diff <(printf '%s\n' "$got") "$exp" | sed 's/^/      /'
    fi
done
echo "-----"
echo "$pass passaram, $fail falharam"
[ "$fail" -eq 0 ]
