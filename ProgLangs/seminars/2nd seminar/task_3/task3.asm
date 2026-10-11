extern print_hex

global _start

section .text

exit:
    mov rax, 60
    xor rdi, rdi
    syscall

print_three_locals:
    push rbp
    mov rbp, rsp
    sub rsp, 24
    mov qword [rbp - 8], 0xaa
    mov qword [rbp - 16], 0xbb
    mov qword [rbp - 24], 0xff

    mov rdi, [rbp - 8]
    call print_hex
    mov rdi, [rbp - 16]
    call print_hex
    mov rdi, [rbp - 24]
    call print_hex

    mov rsp, rbp
    pop rbp 
    ret

_start:
    call print_three_locals
    call exit
