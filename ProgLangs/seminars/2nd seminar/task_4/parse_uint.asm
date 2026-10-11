section .data
code: db '526769gang'
newline: db 10

global _start
section .text

parse_uint:
    xor rax, rax
    xor rdx, rdx;        счетчик
    .loop:
	movzx r8, byte [rdi + rdx]
        cmp r8, 0x30
        jl .finish
	cmp r8, 0x39
        jg .finish
        sub r8, 0x30
        imul rax, rax, 10
        add rax, r8
        inc rdx
        jmp .loop
    .finish:
	ret

_start:
    mov rdi, code
    call parse_uint

    mov rsi, rdi
    mov rdi, 1
    mov rax, 1
    syscall
  
    add rdx, 0x30
    push rdx

    mov rsi, newline
    mov rdx, 1
    mov rax, 1
    syscall

    mov rsi, rsp
    mov rax, 1
    syscall
    

    mov rax, 60
    xor rdi, rdi 
    syscall
