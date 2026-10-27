%assign count 0

%macro my_call 1
	call %1
        %assign count count + 1
%endmacro

loop:
my_call loop
my_call loop
my_call loop

db count
