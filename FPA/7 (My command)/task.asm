org 0x0100
result: word 0x0001

result1: word 0x1234
result2: word 0x0000
result3: word 0xABCD

arg1: word 0x1234
arg2: word 0x0000
arg3: word 0xABCD

check1: word 0x0000
check2: word 0x0000
check3: word 0x0000

START:CLA
CALL $test1
CALL $test2
CALL $test3
LD result
AND check1
AND check2
AND check3
ST result
HLT

test1:LD $arg1
PUSH
LD #0x52
word 0x0F03
CMP #0x52
BNE error1
POP
CMP result1
BEQ done1
POP
CLA
RET
error1:POP
POP
CLA 
RET
done1:POP
LD #0x01
ST check1
CLA
RET


test2:LD $arg2
PUSH
LD #0x52
word 0x0F03
CMP #0x52
BNE error2
POP
CMP result2
BEQ done2
POP
CLA
RET
error2:POP
POP
CLA
RET
done2:POP
LD #0x01
ST check2
CLA
RET


test3:LD $arg3
PUSH
LD #0x52
word 0x0F03
CMP #0x52
BNE error3
POP
CMP result3
BEQ done3
POP
CLA
RET
error3:POP
POP
CLA
RET
done3:POP
LD #0x01
ST check3
CLA
RET
