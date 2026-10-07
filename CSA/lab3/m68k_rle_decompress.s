    .text

_start: 
    clr.l                   D4                 ; счетчик прочитанных символов
    clr.l                   D5                 ; показывает, было ли переполнение
    clr.l                   D6                 ; счетчик длины записи
    clr.l                   D7                 ; флаг, который показывает, что была ввод некорректен
    
    movea.l input_addr,     A5                 ; адрес ввода
    movea.l (A5),           A0

    movea.l output_addr,    A5                 ; адрес вывода
    movea.l (A5),           A1

    movea.l buffer_start,   A5                 ; адрес начала буфера
    movea.l (A5),           A2

    movea.l stack_addr,     A5                 ; адрес стека
    movea.l (A5),           A7

    jsr                     fill_buffer_loop

    jsr                     writing

    halt



fill_buffer_loop:
    clr.l                   D1
    move.b (A0),            D1 
    cmp.b 0xA,              D1
    beq                     finish_filling

    cmp.b 0x31,             D1
    blt                     error_flag                    
  
    cmp.b 0x39,             D1
    ble                     read_symbol                     

error_flag:
    move.b 1,               D7            

read_symbol:
    sub.b 0x30,             D1
    add.l D1,               D6
    clr.l                   D2

    jmp                     second_part

    .text
    .org 0x88

second_part:

    move.b (A0),            D2
    cmp.b 0xA,              D2                 ; читаем все, что есть из порта ввода 
    bne                     continue_symbol

    move.b 0xA,             (A2)
    move.b 1,               D7                 ; это уже некорректно, но нужно прочитать \n из буфера 
    jmp                     nulling_buffer

continue_symbol:
    move.b D1,              (A2)+
    move.b D2,              (A2)+
    add.b 2,                D4
    cmp.b 0x80,             D4
    beq                     overflow

    jmp                     fill_buffer_loop

finish_filling:
    move.b 0xA,             (A2)               ; в конце добавляем стоп символ
nulling_buffer:
    movea.l buffer_start,   A5
    movea.l (A5),           A2                 ; обнуляем адрес буфера
    rts

writing:
    cmp.b 1,                D7
    beq                     error

    jsr                     check_length_proc
    cmp.b 1,                D5
    beq                     overflow

write_loop:
    clr.l                   D1
    move.b (A2)+,           D1
    cmp.b 0xA,              D1
    beq                     finish_writing

    clr.l                   D2
    move.b (A2)+,           D2

    move.b D1,              -(A7)              ; забросили цифру на стек
    move.b D2,              -(A7)              ; забросили символ на стек

    jsr                     write_out_proc     ; вызываем процедуру записи в output

    move.b (A7)+,           D3
    move.b (A7)+,           D3                 ; скидываем значения со стека

    jmp                     write_loop

finish_writing:
    rts



error:
    move.l -1,              (A1)
    halt

overflow:
    move.l 0xCCCCCCCC,      (A1)
    halt



write_out_proc:
    move.b 5(A7),           D1             ; цифру положили раньше
    move.b 4(A7),           D2             ; символ позже

no_overflow_write:
    move.b D2,              (A1)
    sub.b 1,                D1
    cmp.b 0,                D1
    bne                     no_overflow_write
    rts


check_length_proc:
    cmp.l 0x3F,             D6
    ble                     no_overflow
    move.b 1,               D5     
      
no_overflow:
    rts



    .data
    .org 0x16C

input_addr:                 .word 0x80
output_addr:                .word 0x84
buffer_start:               .word 0x17C
stack_addr:                 .word 0x205
