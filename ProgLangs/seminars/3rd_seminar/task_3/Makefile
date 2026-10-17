ASM = nasm
ASMFLAGS = -felf64 -o

%.o: %.asm
	$(ASM) $(ASMFLAGS) $@ $<

main: main.o lib.o
	ld -o $@ $^

.PHONY clear:
	rm -f *.o main
