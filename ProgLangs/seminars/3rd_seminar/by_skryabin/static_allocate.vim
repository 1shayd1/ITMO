n_0:
db 0
n_1:
db 1
n_2:
db 2

%assign count 0
%macro allocate_n 0
	%assign count count + 1
	n_ %+ count:
	db count
%endmacro

%rep 100
allocate_n
%endrep
