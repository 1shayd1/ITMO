%include "lib.inc"

global find_word

section .text

find_word:
    .loop:
    	test rsi, rsi
    	jz .not_found
        
        push rdi
        push rsi

        lea rsi, [rsi + 8]
    	call string_equals

        pop rsi
        pop rdi

    	cmp rax, 1
   	je .return
    	mov rsi, [rsi]
    	jmp .loop

    .return:
	mov rax, rsi
        ret

    .not_found:
        mov rax, 0
	ret
