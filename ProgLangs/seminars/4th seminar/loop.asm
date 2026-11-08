section .data
correct: dq -1

section .text
global _start

extern foo

_start:
jmp _start
