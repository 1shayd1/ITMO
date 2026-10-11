ASMFLAGS = -felf64 -g -F dwarf

parse_uint: parse_uint.o
	ld -o $@ $^

%.o:%.asm
	nasm $(ASMFLAGS) $< -o $@

clean:
	rm -rf *.o parse_uint
