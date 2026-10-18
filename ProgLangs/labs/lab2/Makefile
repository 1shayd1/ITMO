ASMFLAGS = -felf64

.PHONY: test clean

%.o: %.asm
	nasm $(ASMFLAGS) -o $@ $<

main: dict.o lib.o main.o
	ld -o $@ $^

test: main
	bash tests.sh

clean:
	rm -f *.o main
