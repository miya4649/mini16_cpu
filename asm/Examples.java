/*
  Copyright (c) 2019, miya
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
  private int DEBUG = 0;

  private int U2M_ADDR_H;
  private int U2M_ADDR_SHIFT;
  private int IO_REG_W_ADDR_H;
  private int IO_REG_W_ADDR_SHIFT;
  private int IO_REG_R_ADDR_H;
  private int IO_REG_R_ADDR_SHIFT;

  private void f_get_io_reg_w_addr()
  {
    // input: R3: device reg num
    // output: R3:io_reg_w_addr
    int io_reg_w_addr = 3;
    int tmp0 = LREG0;
    // io_reg_w_addr = (IO_REG_W_ADDR_H << IO_REG_W_ADDR_SHIFT) + R3;
    label("f_get_io_reg_w_addr");
    lib_push(SP_REG_LINK);
    lib_set_im(tmp0, IO_REG_W_ADDR_H);
    lib_sli(tmp0, IO_REG_W_ADDR_SHIFT);
    lib_wait_dependency();
    as_add(io_reg_w_addr, tmp0);
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  private void f_get_io_reg_r_addr()
  {
    // input: R3: device reg num
    // output: R3:io_reg_r_addr
    int io_reg_r_addr = 3;
    int tmp0 = LREG0;
    // io_reg_r_addr = (IO_REG_R_ADDR_H << IO_REG_R_ADDR_SHIFT) + R3;
    label("f_get_io_reg_r_addr");
    lib_push(SP_REG_LINK);
    lib_set_im(tmp0, IO_REG_R_ADDR_H);
    lib_sli(tmp0, IO_REG_R_ADDR_SHIFT);
    lib_wait_dependency();
    as_add(io_reg_r_addr, tmp0);
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  private void f_get_u2m_addr()
  {
    // output: R3:u2m_addr
    int u2m_addr = 3;
    // u2m_addr = U2M_ADDR_H << U2M_ADDR_SHIFT;
    label("f_get_u2m_addr");
    lib_push(SP_REG_LINK);
    lib_set_im(u2m_addr, U2M_ADDR_H);
    lib_sli(u2m_addr, U2M_ADDR_SHIFT);
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  private void example_led()
  {
    /*
    led_addr = (MASTER_W_BANK_IO_REG << DEPTH_B_M_W) + IO_REG_W_LED;
    counter = 0;
    counter2 = 0;
    shift = 7;
    do
    {
      led = counter >> shift;
      mem[led_addr] = counter;
      counter++;
      do
      {
        counter2++;
      } while (counter2 != 0);
    } while (1);
    */
    int led_addr = R3;
    int counter = R4;
    int counter2 = R5;
    int led = R6;
    int shift = R7;
    as_nop();
    lib_init_stack();
    lib_set_im(R3, IO_REG_W_LED);
    lib_call("f_get_io_reg_w_addr");
    as_mvi(counter, 0);
    as_mvi(counter2, 0);
    as_mvi(shift, 5);
    // normal: shift=5
    label("example_led_L_0");
    lib_wait_dep_pre();
    as_mv(led, counter);
    lib_wait_dep_post();
    lib_sr(led, shift);
    lib_wait_dependency();
    as_st(led_addr, led);
    as_addi(counter, 1);
    label("example_led_L_1");
    as_cnz(SP_REG_CP, counter2);
    as_addi(counter2, 1);
    lib_bc("example_led_L_1");
    lib_ba("example_led_L_0");
    // link library
    f_get_io_reg_w_addr();
    f_lib_sr();
    f_lib_sl();
  }

  private void example_counter()
  {
    as_nop();
    lib_init_stack();
    as_mvi(R3, 0);
    as_mvi(R4, 0);
    label("example_counter_L_0");
    lib_call("f_uart_hex_word_ln");
    as_mv(R3, R4);
    as_addi(R4, 1);
    lib_ba("example_counter_L_0");
    // link library
    f_lib_sl();
    f_lib_sr();
    f_uart_char();
    f_uart_hex();
    f_uart_hex_word();
    f_uart_hex_word_ln();
  }

  private void example_helloworld()
  {
    as_nop();
    lib_init_stack();
    lib_wait_dep_pre();
    as_mvi(R4, MASTER_R_BANK_MEM_D);
    lib_wait_dep_post();
    lib_sli(R4, DEPTH_B_M_R);
    lib_set_im(R3, addr_abs("d_helloworld"));
    as_add(R3, R4);
    lib_call("f_uart_print_16");
    lib_call("f_halt");
    // link library
    f_uart_char();
    f_uart_print_16();
    f_halt();
    f_get_u2m_data();
    f_lib_sl();
    f_lib_sr();
  }

  private void example_helloworld_data()
  {
    label("d_helloworld");
    string_data16("Hello, world!\r\n");
  }

  // copy data from U2M to MEM_D
  // call before lib_init_stack()
  public void f_get_u2m_data()
  {
    int addr_dst = LREG0;
    int addr_src = LREG1;
    int size = LREG2;
    int data = LREG3;
    label("f_get_u2m_data");
    lib_push(SP_REG_LINK);
    as_mvi(size, 1);
    lib_wait_dep_pre();
    as_mvi(addr_src, U2M_ADDR_H);
    lib_wait_dep_post();
    lib_sli(addr_src, U2M_ADDR_SHIFT);
    as_mvi(addr_dst, 0);
    lib_sli(size, DEPTH_M_D);
    lib_wait_dependency();
    label("f_get_u2m_data_L_0");
    as_ld(data, addr_src);
    as_subi(size, 1);
    lib_wait_dep_pre();
    as_addi(addr_src, 1);
    lib_wait_dep_post();
    as_st(addr_dst, data);
    as_cnz(SP_REG_CP, size);
    as_addi(addr_dst, 1);
    lib_bc("f_get_u2m_data_L_0");
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  @Override
  public void init(String[] args)
  {
    super.init(args);
    U2M_ADDR_H = MASTER_R_BANK_U2M;
    U2M_ADDR_SHIFT = DEPTH_B_M_R;
    IO_REG_W_ADDR_H = MASTER_W_BANK_IO_REG;
    IO_REG_W_ADDR_SHIFT = DEPTH_B_M_W;
    IO_REG_R_ADDR_H = MASTER_R_BANK_IO_REG;
    IO_REG_R_ADDR_SHIFT = DEPTH_B_M_R;
  }

  @Override
  public void program()
  {
    set_filename("default_master_code");
    set_rom_width(WIDTH_I);
    set_rom_depth(DEPTH_M_I);
    example_led();
    //example_counter();
    //example_helloworld();
  }

  @Override
  public void data()
  {
    set_filename("default_master_data");
    set_rom_width(WIDTH_M_D);
    set_rom_depth(DEPTH_M_D);
    label("d_rand");
    dat(0xfc720c27);
    example_helloworld_data();
  }
}
