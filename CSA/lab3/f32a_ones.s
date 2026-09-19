    .data
input_addr:        .word 0x80
output_addr:       .word 0x84
counter:           .word 0x00


    .text
    .org 0x200

_start:
    @p             input_addr    \ прочитали значение из указанного адреса на дата стек 
    a!                           \ сохранили значение дата стека в регистр а
    @                            \ положили введенное число на дата стек

loop:                            \ цикл бесконечный, т.к непонятна длина числа
    dup
    if             finish        \ чекаем на 0
    dup
    lit            1             \ закинули наверх 1
    and                          \ проверили, четное ли число            
    if             even              
    
odd:
    2/                           \ убираем один знак
    lit            0x7FFFFFFF
    and                          \ если число отрицательное, убираем у него знак   
    @p             counter       \ загружаем counter на дата стек
    lit            1
    +                            \ увеличиваес counter на 1
    !p             counter       \ сохраняем counter
    loop           ;

even: 
    2/                           \ сдвигаем число вправо на один знак
    lit            0x7FFFFFFF
    and                          \ та же тема, что и в нечетных числах
    loop           ;             \ мутим цикл по новой 


finish:
    drop
    @p             counter
    @p             output_addr
    a!
    !                           \ сохраняем ответ
    halt
