org 0x0150
START: CLA

input: IN 0x19
AND #0x40
BEQ input

LD buffer
BEQ loadFirstPart;                                             если буффер пустой -> загружаем первый символ в ячейке
ST buffer
CLA
IN 0x18
CMP stopSymbol                         
BEQ fail2;                                                ВВОД СИМВОЛОВ С КЛАВИАТУРЫ И СОХРАНЕНИЕ В ПАМЯТЬ ДО СТОП-СИМВОЛА
PUSH
CALL $countNumberFunction
POP
ADD buffer
ST (arrayOfSymbols)+
CLA
ST buffer
LD symbolLength
INC
ST symbolLength
JUMP input

loadFirstPart: IN 0x18
CMP stopSymbol;                                                если не стоп-символ -> разворачиваем и сохраняем в буфер
BEQ fail1
PUSH
CALL $countNumberFunction
POP
SWAB
ST buffer
LD symbolLength
INC
ST symbolLength
JUMP input

symbolLength: word 0x0000
numberLength: word 0x0000
stopSymbol: word 0x0020
flag: word 0x0000;                                                   ОБЛАСТЬ ПАМЯТИ, ГДЕ ХРАНЯТСЯ ФЛАГИ И ДАННЫЕ
buffer: word 0x0000
checker: word 0x00FF
arrayOfSymbols: word 0x0179

org 0x1C0
fail2: ADD buffer
ST (arrayOfSymbols)+
LD symbolLength
INC
ST symbolLength
JUMP show
fail1:SWAB;                                                             ЧАСТЬ, ГДЕ ОБРАБАТЫВАЕТСЯ СОХРАНЕНИЕ СТОП-СИМВОЛА
ADD stopSymbol 
ST (arrayOfSymbols)+
LD symbolLength
INC
INC
ST symbolLength

show: IN 0x03
AND #0x40
BEQ show


newCell: LD flag
BNE loadSecondPart
LD #0x01
ST flag
LD $symbolLength
DEC
ST $symbolLength
LD -(arrayOfSymbols)
ST buffer
AND checker
PUSH
CALL $checkNumberFunction
POP
BEQ loadSecondPart
OUT 2
LD $numberLength
DEC
BEQ exit
ST $numberLength
JUMP show

loadSecondPart: LD #0x00
ST flag
LD buffer
SWAB
AND checker
PUSH
CALL $checkNumberFunction
POP
BEQ loopJump
OUT 2
LD $numberLength
DEC
BEQ exit
ST $numberLength
JUMP show

loopJump: LOOP $symbolLength
JUMP newCell

exit: HLT

org 0x0300
checkNumberFunction: LD &1
SUB #0x30
BMI notNumber
LD &1
SUB #0x3A;
BPL notNumber;                                              ФУНКЦИЯ, ОПРЕДЕЛЯЮЩАЯ ЯВЛЯЕТСЯ ЛИ НАШ СИМВОЛ ЧИСЛО ИЛИ НЕТ
RET
notNumber: LD #0x00
ST &1
RET

org 0x0320
countNumberFunction: LD &1
SUB #0x30
BMI noNumber
LD &1 
SUB #0x3A
BPL noNumber
LD $numberLength
INC
ST $numberLength
noNumber:RET
