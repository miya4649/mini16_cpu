/*
  Copyright (c) 2018, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.lang.Math;

public class Examples extends AsmLib
{
  private static final int CODE_ROM_DEPTH = 8;
  private static final int DATA_ROM_DEPTH = 8;

  private void example_led()
  {
    int Ri = 2;
    int Rwait_count = 3;
    int Rled_addr = 4;
    int Rlabel0 = 5;
    int Rlabel1 = 6;
    int Rled_value = 7;
    as_nop();
    lib_set_im(Rled_addr, 0x100);
    lib_set_im(Rlabel0, addr_abs("L_0"));
    lib_set_im(Rlabel1, addr_abs("L_1"));
    as_mvil(0x100);
    // 3 for sim, -1 for fpga
    as_mvi(Rwait_count, -1);
    as_mvi(Rled_value, 0);
    lib_nop(3);
    as_mv(Rled_addr, SP_REG_MVIL);
    label("L_0");
    as_mv(Ri, Rwait_count);
    lib_wait_dependency();
    label("L_1");
    as_cnz(SP_REG_CP, Ri);
    as_subi(Ri, 1);
    lib_nop(4);
    as_bc(SP_REG_CP, Rlabel1);
    lib_wait_delay_slot();
    as_st(Rled_addr, Rled_value);
    as_addi(Rled_value, 1);
    as_ba(Rlabel0);
    lib_wait_delay_slot();
  }

  private void example_load_store()
  {
    as_nop();
    // set address
    as_mvi(3, 0);
    as_mvi(4, 1);
    as_mvi(5, 2);
    as_mvi(6, 3);
    as_mvi(7, 4);
    as_mvi(8, 5);
    // store initial value
    as_sti(3, 1);
    as_sti(4, 2);
    as_sti(5, 3);
    as_sti(6, 4);
    as_sti(7, 5);
    as_sti(8, 6);
    // load
    as_ld(9, 3);
    as_ld(10, 4);
    as_ld(11, 5);
    as_ld(12, 6);
    as_ld(13, 7);
    as_ld(14, 8);
    // increment
    as_addi(9, 1);
    as_addi(10, 1);
    as_addi(11, 1);
    as_addi(12, 1);
    as_addi(13, 1);
    as_addi(14, 1);
    // store new value
    as_st(3, 9);
    as_st(4, 10);
    as_st(5, 11);
    as_st(6, 12);
    as_st(7, 13);
    as_st(8, 14);
  }

  private void example_call()
  {
    as_nop();
    as_mvi(3, 1);
    lib_call("f_example_call_sub1");
    as_mvi(3, 3);
    // halt
    label("L_end");
    lib_ba("L_end");
    // link library
    f_example_call_sub1();
  }

  private void f_example_call_sub1()
  {
    label("f_example_call_sub1");
    as_mvi(3, 2);
    lib_return();
  }

  @Override
  public void init()
  {
    set_rom_depth(CODE_ROM_DEPTH, DATA_ROM_DEPTH);
    set_stack_address((1 << DATA_ROM_DEPTH) - 1);
    // use default name
    //set_filename("example");
  }

  @Override
  public void program()
  {
    example_led();
    //example_load_store();
    //example_call();
  }

  @Override
  public void data()
  {
  }
}
