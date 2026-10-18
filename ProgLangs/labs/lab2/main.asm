%include "words.inc"
%include "dict.inc"
%include "lib.inc"

section .data
error_message: db "the key isn't in dictionary, or it's length is bigger than 255 symbols", 0
buffer: times 256 db 0

section .text
global _start

_start:
    mov rdi, buffer
    mov rsi, 256
    call read_word;      если все ок, то адрес буфера - в rax, длина - в rdx
    test rax, rax
    jz .error

    mov rdi, rax
    mov rsi, address;    это из colon макроса указатель на ласт строчку
    call find_word;      если все ок, то в rax - указатель на 0-терм строку
    test rax, rax
    jz .error
    
    lea rdi, [rax + 8]
    push rdi
    call string_length
    pop rdi
    add rdi, rax 
    inc rdi
    
    call print_string
    call print_newline
    call exit

    .error:
        mov rdi, error_message
        call string_length
        mov rdx, rax
	mov rsi, error_message
        mov rdi, 2
        mov rax, 1
        syscall
        call exit
