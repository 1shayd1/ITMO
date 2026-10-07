    .data
buffer:              .byte 0x00

    .text
    .org 0x20
_start:
    lui sp,          %hi(0x00000)       
    addi sp,         sp, %lo(0x800)

    lui s0,          %hi(input_addr)
    addi s0,         s0, %lo(input_addr)   ; s0 <- адрес ввода
    lw s0,           0(s0)     

    lui s1,          %hi(start_string)
    addi s1,         s1, %lo(start_string) ; s1 <- адрес начала массива

    lui s3,          %hi(buffer)
    addi s3,         s3, %lo(buffer)       ; s3 <- адрес буфера

    lui s4,          %hi(output_addr)
    addi s4,         s4, %lo(output_addr)  ; s4 <- адрес вывода 
    lw s4,           0(s4)

    addi t0,         zero, 0               ; t0 <- счетчк длины = 0
    addi t1,         zero, 0x0A            ; t1 <- проверка символа '\n'

    lui t2,          %hi(filling_gap)
    addi t2,         t2, %lo(filling_gap)  ; t2 <- положили число, которым заполняем цикл
    lb t2,           0(t2)
    
    lui t3,          %hi(loop_length)
    addi t3,         t3, %lo(loop_length)  ; t3 <- положили количество итераций цикла = 32
    lb t3,           0(t3)

filling_loop:
    beq t3, zero,    filling_end
    sb t2,           0(s3)
    j                second_part








    .text
    .org 0x88
second_part:
    addi t3,         t3, -1
    addi s3,         s3, 1
    j                filling_loop    

filling_end:
    addi s3,         s3, -32               ; s3 <- вернули изначальный адрес buffer (0)
    addi t4,         zero, 31              ; t4 <- 31, для сравнения лимита длины

    jal ra,          read_loop             ; процедура чтения слова

    halt






read_loop:
    addi sp,         sp, -4
    sw ra,           0(sp)                 ; сохраняем адрес возврата из первой процедуры на стеке
    
    lb t3,           0(s0)                 ; t3 <- читаем символ из порта входа
    beq t3, t1,      finish_read           ; если символ = '\n' - заканчивам выполнение

    sb t3,           0(s1)                 ; сохраняем символ 
    addi s1,         s1, 1                 ; увеличиваем счетчик массива на 1
    addi t0,         t0, 1                 ; увеличиваем длину слова на 1

    bgt t0, t4,      overflow              ; если символов больше, чем 31, то переполнение
    j                read_loop
    
finish_read:
    addi s1,         s1, -1                 ; смещаем указатель на 1 назад
    sb t0,           0(s3)                  ; сохряняем длину строки в ответ первым
    addi s3,         s3, 1               

    jal ra,          write_loop             ; вложенная процедура записи слова

end_of_read:
    lw ra,           0(sp)
    addi sp,         sp, 4
    jr ra

overflow:
    lui t0,         0xCCCCC
    addi t0, t0,    0xCCC
    sw t0,          0(s4)
    j               end_of_read






write_loop:
    addi sp,         sp, -4
    sw ra,           0(sp)                  ; сохраняем адрес возврата вложенной процедуры на стеке

    beq t0, zero,    end_of_write
    addi t0,         t0, -1                 ; уменьшаем количество оставшихся символов
    
    lb t3,           0(s1)                  ; t3 <- символ с конца массива
    sb t3,           0(s4)                  ; так это вывод
    sb t3,           0(s3)                  ; так это ответ в начале аккумулятора

    addi s3,         s3, 1
    addi s1,         s1, -1                 ; сдвигаем указатель массива на 1 назад
    
    j                write_loop

end_of_write:
    lw ra,           0(sp)
    addi sp,         sp, 4
    jr ra

    .data
input_addr:         .word 0x80
output_addr:        .word 0x84
loop_length:        .byte 0x20
filling_gap:        .byte 0x5f
start_string:       .byte 0x00
