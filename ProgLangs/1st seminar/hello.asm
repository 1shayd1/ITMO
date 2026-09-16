; hello.asm 
  section .data
  message: db  'hello, garry!', 10
  error_message: db 'error', 10

  section .text
  global _start

  _start:
      mov     rax, 1           ; 'write' syscall number
      mov     rdi, 1           ; stdout descriptor
      mov     rsi, message     ; string address
      mov     rdx, 14          ; string length in bytes
      syscall

      mov     rax, 1
      mov     rdi, 2
      mov     rsi, error_message
      mov     rdx, 6
      syscall

      mov     rax, 60          ; 'exit' syscall number
      xor     rdi, rdi
      syscall
