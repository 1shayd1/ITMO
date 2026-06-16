org 0x0593; начало программы
length: word 0x0007
buffer: word 0x0000
flag: word 0x0000
symbols: word 0x0597
word 0xE5C4
word 0xE5F0
word 0xEEE2
word 0x003F
START: CLA; чисто символически обнуляем аккумулятор, ну а хули, собственно говоря

len: IN 7; читаем длину слова (тупое условие, зачем выводить длину слова???)
AND #0x40
BEQ len; спин-луп
LD length
OUT 6
BEQ exit; пидорасы, которые любят тыкать нули будут находиться в фрустрации

s: IN 7; читаем блядский символ 
AND #0x40
BEQ s; спин-луп

LD flag
BEQ loadNewCell
LD buffer
OUT 6
LD #0x00
ST flag
JUMP loopJump

loadNewCell: LD (symbols)+
OUT 6
SWAB
ST buffer 
LD #0x01
ST flag

loopJump: LOOP length
JUMP s
exit:HLT
