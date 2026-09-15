    .data

input_addr:     .word 0x80
output_addr:    .word 0x84
a:              .word 0x00
b:              .word 0x00
temp_a:         .word 0x00
temp_b:         .word 0x00
temp:           .word 0x00
gcd:            .word 0x01


    .text
    .org 0x33
_start:
    load           input_addr
    load_acc
    store_addr     a
    store_addr     temp_a
    
    bgtz           check_b
    load           input_addr
    load_acc
    load_imm       -1
    store_ind       output_addr
    halt

check_b:
    load           input_addr
    load_acc
    jmp            second_part_of_code

    .org 0x88
second_part_of_code:
    store_addr     b
    store_addr     temp_b

    bgtz           find_gcd
    load_imm       -1
    store_ind       output_addr
    halt

find_gcd:
    load_addr      temp_b
    beqz           finish_gcd
    store_addr     temp
    load_addr      temp_a
    rem            temp_b
    store_addr     temp_b
    load_addr      temp
    store_addr     temp_a
    jmp            find_gcd
    

finish_gcd:
    load_addr      temp_a
    store_addr     gcd

find_lcm:
    load_addr      a
    div            gcd
    mul            b
    bvs            overflow
    store_ind      output_addr
    halt

overflow:
    load_imm       0xCCCCCCCC
    store_ind      output_addr
    halt
