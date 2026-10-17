%macro comma_string 0-*
    %assign %%length %0
    %rep %0
        %assign %%length %%length - 1
  	db %1
        %if %%length > 0
	    db ", "
        %endif
        %rotate 1
    %endrep
%endmacro

section .data
string:
    comma_string "garry", "paython", "second"
string_length equ $ - string

global _start
section .text
    
exit:
    mov rax, 60
    xor rdi, rdi
    syscall

_start:
    mov rdx, string_length
    mov rdi, 1
    mov rax, 1
    mov rsi, string
    syscall
    call exit
