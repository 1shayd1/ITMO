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
    .org 0x20
_start:
    load           input_addr
    load_acc
    store          a
    store          temp_a
    
    bgtz           check_b
    load           input_addr
    load_acc
    load_imm       -1
    store_ind       output_addr
    halt

check_b:
    load           input_addr
    load_acc
    store          b
    store          temp_b

    bgtz           find_gcd
    load_imm       -1
    store_ind      output_addr
    halt

find_gcd:
    load           temp_b
    beqz           finish_gcd
    store          temp
    load           temp_a
    rem            temp_b
    store          temp_b
    load           temp
    store          temp_a
    jmp            find_gcd

    jmp            finish_gcd

    .text
    .org 0x88
finish_gcd:
    load           temp_a
    store          gcd

find_lcm:
    load           a
    div            gcd
    mul            b
    bvs            overflow
    store_ind      output_addr
    halt

overflow:
    load_imm       0xCCCCCCCC
    store_ind      output_addr
    halt
