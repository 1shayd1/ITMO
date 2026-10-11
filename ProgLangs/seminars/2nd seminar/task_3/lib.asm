default rel

global print_hex

section .rodata
codes: db '0123456789ABCDEF'
newline: db 10

section .text

print_hex:
    mov  rax, rdi
    mov  rdi, 1
    mov  rdx, 1
    mov  rcx, 64
.loop:
    push rax
    sub  rcx, 4
    sar  rax, cl
    and  rax, 0xf
    lea  rsi, [codes]
    add rsi, rax
    mov  rax, 1
    push rcx
    syscall
    pop  rcx
    pop rax
    test rcx, rcx
    jnz .loop

    mov rax, 1
    mov rsi, newline
    syscall
    ret
