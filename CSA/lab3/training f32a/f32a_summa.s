    .data
input_addr:      .word 0x80
output_addr:     .word 0x84

    .text
_start:
    @p input_addr a! @        \ 1st number on stack
    @p input_addr a! @        \ 2nd number on stack
    +                         \ sum to highest values on stack
    @p output_addr a! !       \ store the result
    halt
