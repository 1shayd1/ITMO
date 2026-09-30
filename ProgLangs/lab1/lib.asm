section .text
 
 
; Принимает код возврата и завершает текущий процесс
exit: 
    xor rax, rax
    ret 

; Принимает указатель на нуль-терминированную строку, возвращает её длину
string_length:
    xor rax, rax
    .loop:
        mov r8b, [rdi + rax]    
        cmp r8b, 0
        je .done
        inc rax
        jmp .loop
    .done:
        ret

; Принимает указатель на нуль-терминированную строку, выводит её в stdout
print_string:
    push rdi
    call string_length         ;   получаем длину строки в rax
    pop rdi
    mov rsi, rdi
    mov rdx, rax
    mov rdi, 1
    mov rax, 1
    syscall
    ret

; Принимает код символа и выводит его в stdout
print_char:
    push rdi
    mov rax, 1
    mov rdx, 1
    mov rsi, rsp
    mov rdi, 1
    syscall
    pop rdi
    ret

; Переводит строку (выводит символ с кодом 0xA)
print_newline:
    push 0xA
    mov rax, 1
    mov rdx, 1
    mov rsi, rsp
    mov rdi, 1
    syscall
    pop rax
    xor rax, rax
    ret

; Выводит беззнаковое 8-байтовое число в десятичном формате 
; Совет: выделите место в стеке и храните там результаты деления
; Не забудьте перевести цифры в их ASCII коды.
print_uint:
    xor rax, rax
    ret
            
    

; Выводит знаковое 8-байтовое число в десятичном формате 
print_int:
    xor rax, rax
    ret

; Принимает два указателя на нуль-терминированные строки, возвращает 1 если они равны, 0 иначе
string_equals:
    xor rcx, rcx
    .loop:
	mov r9b, [rdi + rcx]
        mov r8b, [rsi + rcx]
        cmp r9b, r8b
        jne .fail
        cmp r9b, 0
        je .success
        add rcx, 1
        jmp .loop
    .success:
	mov rax, 1
        ret
    .fail:
	mov rax, 0
    	ret

; Читает один символ из stdin и возвращает его. Возвращает 0 если достигнут конец потока
read_char:
    xor rax, rax
    ret 

; Принимает: адрес начала буфера, размер буфера
; Читает в буфер слово из stdin, пропуская пробельные символы в начале, .
; Пробельные символы это пробел 0x20, табуляция 0x9 и перевод строки 0xA.
; Останавливается и возвращает 0 если слово слишком большое для буфера
; При успехе возвращает адрес буфера в rax, длину слова в rdx.
; При неудаче возвращает 0 в rax
; Эта функция должна дописывать к слову нуль-терминатор

read_word:
    xor rax, rax
    ret
 

; Принимает указатель на строку, пытается
; прочитать из её начала беззнаковое число.
; Возвращает в rax: число, rdx : его длину в символах
; rdx = 0 если число прочитать не удалось
parse_uint:
    xor rax, rax
    ret




; Принимает указатель на строку, пытается
; прочитать из её начала знаковое число.
; Если есть знак, пробелы между ним и числом не разрешены.
; Возвращает в rax: число, rdx : его длину в символах (включая знак, если он был) 
; rdx = 0 если число прочитать не удалось
parse_int:
    xor rax, rax
    ret 

; Принимает указатель на строку, указатель на буфер и длину буфера
; Копирует строку в буфер
; Возвращает длину строки если она умещается в буфер, иначе 0
string_copy:
    push rdi
    push rsi
    push rdx
    call string_length   ;      получаем длину строки в rax
    pop rdx
    pop rsi
    pop rdi
    cmp rdx, rax
    jle .fail

    xor rcx, rcx
    .writing_loop:
        cmp rax, rcx
        je .finish
	mov r8b, [rdi + rcx]
        mov [rsi + rcx], r8b
        add rcx, 1
        jmp .writing_loop

    .finish:
        mov byte [rsi + rcx], 0
	ret
        
    .fail:
	mov rax, 0
        ret


