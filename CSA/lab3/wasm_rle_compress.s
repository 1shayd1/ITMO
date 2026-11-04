    .data
input_addr:  .word 0x80
output_addr: .word 0x84
overflow:    .word 0xCCCCCCCC
buffer_addr: .word 0x18f

    .text
    .org 0x88

_start:
    .frame () $in, $out, $curr, $prev, $cnt, $pos, $len
    locals.reserve 7

    i32.const input_addr i32.load local.set $in                                       ; input = 0x80
    i32.const output_addr i32.load local.set $out                                     ; output = 0x84
    i32.const buffer_addr i32.load local.set $pos                                     ; pos = 0x0

    block                                                         
        block                                                     
            local.get $in i32.load8_u                                                    ; 1st sym
            local.tee $curr local.tee $prev                                           ; curr = prev = 1st sym
            i32.const 0xA i32.eq
            br_if 1                                             
            i32.const 1 local.set $cnt                                                ; cnt = 1
            i32.const 1 local.set $len                                                ; len = 1

            block                                                
                loop
                    local.get $in i32.load8_u local.tee $curr       
                    i32.const 0xA i32.eq
                    if                                        
                        local.get $pos local.get $cnt i32.const 48 i32.add i32.store8
                        local.get $pos local.get $prev i32.store8 1
                        local.get $pos i32.const 2 i32.add local.set $pos
                        br 1
                    end

                    local.get $len i32.const 1 i32.add local.tee $len                  ; len += 1
                    i32.const 64 i32.eq
                    br_if 2              

                    local.get $curr local.get $prev i32.xor                             ; same symb
                    local.get $cnt i32.const 9 i32.eq                                   ; cnt = 9
                    i32.or
                    if                                           
                        local.get $pos local.get $cnt i32.const 48 i32.add i32.store8
                        local.get $pos local.get $prev i32.store8 1
                        local.get $pos i32.const 2 i32.add local.set $pos

                        i32.const 1 local.set $cnt               
                        local.get $curr local.set $prev           
                    else
                        local.get $cnt i32.const 1 i32.add local.set $cnt                ; cnt += 1
                    end
                    br 0
                end
            end

            local.get $pos i32.const buffer_addr i32.load
            i32.sub i32.const 63 i32.gt_s
            br_if 0                                         

            i32.const buffer_addr i32.load local.set $curr                    
            loop                                               
                local.get $out local.get $curr i32.load8_u i32.store8
                local.get $curr i32.const 1 i32.add local.tee $curr    
                local.get $pos i32.lt_s
                br_if 0                                      
            end
            br 1                                             
        end

        local.get $out i32.const overflow i32.load i32.store     
    end
    halt 
