%macro comma_string 0-*
    %rep %0
      db %1
      %if %0 > 1
          db, ","
      %endif
    %rotate 1
    %endrep  
%endmacro


global _start
section .text
    
exit:
    mov rax, 60
    xor rdi, rdi
    syscall

_start: 
    comma_string "garry", "paython", "second" 
    call exit
