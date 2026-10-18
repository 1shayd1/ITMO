%include "colon.inc"

section .data

colon "four", num3
db "jodye", 0

colon "three", num2
db "flacko", 0

colon "two", num1
db "pretty", 0

colon "one", num4
db "lord", 0

%define KEY_255 ""
%rep 255
    %strcat KEY_255 KEY_255, "a"
%endrep
colon KEY_255, key_255
db "value_255", 0
