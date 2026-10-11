; print_hex.asm
section .data
codes: db '0123456789ABCDEF'
num1: dq 52
num2: dq 67
num3: dq 69
newline: db 10

section .text
global _start


print_hex:
    ; number 1122... in hexadecimal format
    mov  rax, rdi

    mov  rdi, 1
    mov  rdx, 1
    mov  rcx, 64
	; Each 4 bits should be output as one hexadecimal digit
	; Use shift and bitwise AND to isolate them
	; the result is the offset in 'codes' array
.loop:
    push rax
    sub  rcx, 4
	; cl is a register, smallest part of rcx
	; rax -- eax -- ax -- ah + al
	; rcx -- ecx -- cx -- ch + cl
    sar  rax, cl
    and  rax, 0xf

    lea  rsi, [codes + rax]
    mov  rax, 1

    ; syscall leaves rcx and r11 changed
    push rcx
    syscall
    pop  rcx

    pop rax
	; test can be used for the fastest 'is it a zero?' check
	; see docs for 'test' command
    test rcx, rcx
    jnz .loop

    mov rax, 1
    mov rsi, newline
    syscall
    
    ret

_start:
    mov rdi,[rel num1]
    call print_hex
    mov rdi, [rel num2]
    call print_hex
    mov rdi, [rel num3]
    call print_hex

    mov eax, 60
    xor rdi, rdi
    syscall
