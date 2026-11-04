    .data
input_addr:          .word 0x80
output_addr:         .word 0x84
minus_one:           .word -1
shift:               .word 31
overflow_value:      .word 0xCCCCCCCC
     
    .text
    .org 0x14
_start:
    lui s1, %hi(input_addr) / / /                                 ; адрес порта ввода
    addi s1, s1, %lo(input_addr) / / /
    lui s5, %hi(minus_one)/ lui s2, %hi(output_addr) / lw s1, 0(s1) /

    addi s5, s5, %lo(minus_one) / addi s2, s2, %lo(output_addr) / lw s4, 0(s1) /    
    lui s6, %hi(shift)/ / lw s5, 0(s5) / blt s4, zero, fail_n
    addi s6, s6, %lo(shift) / / lw s2, 0(s2) / blt s4, zero,fail_n          

    / / lw s6, 0(s6) / j loop

    .text
    .org 0x88                                    

loop:
    / / lw a0, 0(s1) / beqz s4, end_loop                          ; a
    / / lw a1, 0(s1) /                                            ; b
    / / lw a2, 0(s1) /                                            ; c
    / / lw a3, 0(s1) /                                            ; d
    / / lw a4, 0(s1) /                                            ; x

    mul s7, a0, a4 / mulh t1, a0, a4 / lw a5, 0(s1) /             ; a*x, y

    sra t2, s7, s6 / mul s8, a1, a5 / /                           ; b*y
    xor t3, t1, t2 / mulh t4, a1, a5 / /                          ; b*y

    sra t5, s8, s6 / mul s9, a2, a4 / / bnez t3, overflow         ; c*x
    xor t6, t4, t5 / mulh t1, a2, a4 / /                          ; c*x

    sra t2, s9, s6 / mul s10, a3, a5 / / bnez t6, overflow        ; d*y
    xor t3, t1, t2 / mulh t4, a3, a5 / /                          ; d*y

    sra t5, s10, s6 / add a6, s7, s8 / / bnez t3, overflow  
    xor t6, t4, t5 / add a7, s9, s10 / /                        

    xor t1, a6, s7 / xor t2, a6, s8 / / bnez t6, overflow        
    and t3, t1, t2 / add s4, s4, s5 / /                          

    xor t1, a7, s9 / xor t2, a7, s10 / / blt t3, zero, overflow 
    and t3, t1, t2 / / /                                       

    / / / blt t3, zero, overflow                                 
    / / sw a6, 0(s2) /                                        
    / / sw a7, 0(s2) / bnez s4, loop 

end_loop:
    / / / halt

fail_n:
    / / sw s5, 0(s2) / j end_loop         

overflow:
    lui t0, %hi(overflow_value) / / /
    addi t0, t0, %lo(overflow_value) / / /
    / / lw t0, 0(t0) /                     
    / / sw t0, 0(s2) / j end_loop
