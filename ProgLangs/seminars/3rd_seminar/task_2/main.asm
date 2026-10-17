%include "lib.inc"

global _start

_start:
    mov rdi, "afffa"
    call print_hex
    call exit
