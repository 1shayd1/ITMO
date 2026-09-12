org 0x0100; сложение 5-элементов массива и формирование 32-разрядного результата
x1: word 0x1234;
x2: word 0x1234;
x3: word 0x7777;
x4: word 0x7777;
x5: word 0x7777;
resultFirst16: word 0x0000;
resultSecond16: word 0x0000;
currentAddress: word 0x0100;
arraySize: word 0x0005;

START:CLA;
arrayLoop: LD (currentAddress)+;
	PUSH;
	CALL $FUNC;
	POP;
	LOOP arraySize;
	JUMP arrayLoop;
HLT;

org 0x0150
addition: word 0x0000;

FUNC:LD &1;
BMI yesMinus;
LD #0x00;
ST addition;
JUMP continue;
yesMinus: LD #0xFF;
ST addition;
continue: LD &1;
ADD $resultSecond16;
ST $resultSecond16;
LD $resultFirst16;
ADC addition;
ST $resultFirst16;
CLC;
RET;
