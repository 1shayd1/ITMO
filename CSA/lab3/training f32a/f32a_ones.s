    .data
input_addr:      .word 0x80
output_addr:     .word 0x84


    .text
_start:
    @p input_addr a! @         \ read number on the stack
    @p output_addr b!

while:
    dup
    if end
    1
    !b
    -1 +
    while;

end:
    halt
