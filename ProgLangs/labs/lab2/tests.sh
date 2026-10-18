#!/bin/bash

ERR_MSG="the key isn't in dictionary, or it's length is bigger than 255 symbols"
A255=$(printf 'a%.0s' {1..255})
A256=$(printf 'a%.0s' {1..256})
n=0
failed=0

# check <описание> <ввод> <ожидаемый вывод> <stdout|stderr>
check() {
    n=$((n + 1))
    local desc="$1" input="$2" expected="$3" stream="$4" output

    if [ "$stream" == "stdout" ]; then
        output=$(printf '%s' "$input" | ./main 2>/dev/null)
    else
        output=$(printf '%s' "$input" | ./main 2>&1 >/dev/null)
    fi

    local shown="$input"
    [ ${#input} -gt 20 ] && shown="${input:0:10}... (${#input} символов)"

    if [ "$output" == "$expected" ]; then
        echo "test $n -> ok"
        echo "description -> $desc"
        echo "input $n -> $shown"
        echo "output $n -> $output"
    else
        failed=$((failed + 1))
        echo "test $n -> fail"
        echo "description -> $desc"
        echo "input $n -> $shown"
        echo "expected $n -> $expected"
    fi
    echo
}

# Ключ есть в словаре -> значение в stdout
check "ключ 'one' есть в словаре"        "one"   "lord"      stdout
check "ключ 'two' есть в словаре"        "two"   "pretty"    stdout
check "ключ 'three' есть в словаре"      "three" "flacko"    stdout
check "ключ 'four' есть в словаре"       "four"  "jodye"     stdout
check "ключ из 255 'a' есть в словаре"   "$A255" "value_255" stdout

# Ключа нет -> error_message в stderr
check "ключа 'five' нет в словаре"       "five"  "$ERR_MSG"  stderr
check "'thr' - только начало ключа"      "thr"   "$ERR_MSG"  stderr
check "'fours' - ключ с лишней буквой"   "fours" "$ERR_MSG"  stderr
check "пустой ввод"                      ""      "$ERR_MSG"  stderr
check "ключ из 256 'a' - слишком длинный" "$A256" "$ERR_MSG" stderr

echo "провалено: $failed из $n"
[ $failed -eq 0 ]
