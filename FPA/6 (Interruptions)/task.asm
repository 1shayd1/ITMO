org 0x0002
V1: word $INT1, 0x180; вектор прерывания 1
org 0x0006
V3: word $INT3, 0x180; вектор прерывания 3

org 0x003B
X: word 0x0005; ОДЗ [-66;61]
leftBound: word 0xFF7E
rightBound: word 0x003D

org 0x0040
START:DI
CLA
LD #9
OUT 3
LD #0xB
OUT 7

prog: EI 
LD $X
ADD #0x03
CMP $rightBound
BPL outOfBound
CMP $leftBound
BMI outOfBound
ST $X
JUMP prog
outOfBound: LD $leftBound
ST $X
JUMP prog

INT1: HLT
PUSH
ADD $X
ADD #0x05
NEG
OUT 2
POP
HLT
IRET

INT3: HLT
PUSH
CLA
IN 6
SXTB
NEG
ADD $X
CMP $rightBound
BPL outOfBounds
CMP $leftBound
BMI outOfBounds
JUMP basic

outOfBounds:LD $leftBound

basic: ST $X 
POP
HLT
IRET
