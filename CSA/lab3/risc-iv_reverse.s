    .data
buffer:              .byte 0x00

    .text
    .org 0x20
_start:
    lui t0,          %hi(input_addr)
    addi t0,         t0, %lo(input_addr)   ; t0 <- 0x80, адрес чтения
    lw t0,           0(t0)     

    lui t1,          %hi(start_string)
    addi t1,         t1, %lo(start_string) ; t1 <- сохранили адрес начала массива

    lui t6,          %hi(buffer)
    addi t6,         t6, %lo(buffer)       ; t6 <- сохранили адрес буфера

    addi t4,         zero, 0               ; счетчк длины = 0
    addi t2,         zero, 0x0A            ; t2 <- проверка символа '\n'

    lui t5,          %hi(filling_gap)
    addi t5,         t5, %lo(filling_gap)  ; t5 <- положили число, которым заполняем цикл
    lb t5,           0(t5)
    
    lui t3,          %hi(loop_length)
    addi t3,         t3, %lo(loop_length)  ; t3 <- положили количество итераций цикла = 32
    lb t3,           0(t3)

filling_loop:
    beq t3, zero,    finish_filling
    sb t5,           0(t6)
    addi t3,         t3, -1                ; мутим цикл по заполнению первых 32 ячеек числами 5f 
    addi t6,         t6, 1                 ; адрес буффера сдвигается до 32
    j                filling_loop

finish_filling:
    lui t5,         %hi(output_addr)
    addi t5,        t5, %lo(output_addr)   ; t5 <- адрес вывода 
    lw t5,          0(t5)
    j               second_part

    .text
    .org 0x88
second_part:    
    addi t6,        t6, -32                ; t6 <- вернули изначальный адрес buffer (0)
    addi a0,        zero, 31               ; a0 <- 31, для сравнения лимита длины

read_loop:
    lb t3,           0(t0)                 ; t3 <- читаем символ из порта входа
    beq t3, t2,      finish_read           ; если символ = '\n' - заканчивам выполнение

    sb t3,           0(t1)                 ; сохраняем символ 
    addi t1,         t1, 1                 ; увеличиваем счетчик массива на 1
    addi t4,         t4, 1                 ; увеличиваем длину слова на 1

    bgt t4, a0,       overflow              ; если символов больше, чем 31, то переполнение
    j                read_loop
    
finish_read:
    addi t1,        t1, -1                 ; смещаем указатель на 1 назад
    sb t4,          0(t6)                  ; сохряняем длину строки в ответ первым
    addi t6,        t6, 1               

write_loop:
    beq t4, zero,    end
    addi t4,        t4, -1                 ; уменьшаем количество оставшихся символов
    
    lb t3,          0(t1)                  ; t3 <- символ с конца массива
    sb t3,          0(t5)                  ; так это вывод
    sb t3,          0(t6)                  ; так это ответ в начале аккумулятора
    addi t6,        t6, 1

    addi t1,        t1, -1                 ; сдвигаем указатель массива на 1 назад
    
    j write_loop

end: 
   halt

overflow:
    lui t0,         0xCCCCC
    addi t0, t0,    0xCCC
    sw t0,          0(t5)
    halt

    .data
input_addr:         .word 0x80
output_addr:        .word 0x84
loop_length:        .byte 0x20
filling_gap:        .byte 0x5f
start_string:       .byte 0x00
