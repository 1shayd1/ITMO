ASMFLAGS = -felf64 -g -F dwarf

task3: task3.o lib.o
	ld -o $@ $^

%.o: %.asm
	nasm $(ASMFLAGS) $< -o $@

clean:
	rm -f *.o task3
