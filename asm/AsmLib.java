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

public class AsmLib extends Asm
{
  public int DEPTH_REG;

  public int ENABLE_MVIL;
  public int ENABLE_MUL;
  public int ENABLE_MVC;
  public int ENABLE_WA;
  public int ENABLE_UART;
  public int ENABLE_MULTI_BIT_SHIFT;

  public int WIDTH_I;
  public int DEPTH_IO_REG;
  public int WIDTH_M_D;
  public int DEPTH_M_I;
  public int DEPTH_M_D;
  public int DEPTH_U2M;
  public int DEPTH_B_U;
  public int DEPTH_V_U;
  public int DEPTH_B_M_W;
  public int DEPTH_V_M_W;
  public int DEPTH_B_M_R;
  public int DEPTH_V_M_R;

  public int MASTER_W_BANK_MEM_D;
  public int MASTER_W_BANK_IO_REG;
  public int MASTER_R_BANK_MEM_D;
  public int MASTER_R_BANK_IO_REG;
  public int MASTER_R_BANK_U2M;
  public int IO_REG_R_UART_BUSY;
  public int IO_REG_W_LED;
  public int IO_REG_W_UART;

  public int REGS;
  public int SP_REG_STACK_POINTER;

  public int LREG0;
  public int LREG1;
  public int LREG2;
  public int LREG3;
  public int LREG4;
  public int LREG5;
  public int LREG6;

  public int STACK_ADDRESS;

  public static final int SP_REG_CP = 0;
  public static final int SP_REG_MVIL = 1;
  public static final int SP_REG_LINK = 2;

  public static final int R3 = 3;
  public static final int R4 = 4;
  public static final int R5 = 5;
  public static final int R6 = 6;
  public static final int R7 = 7;

  public static final int WAIT_DELAYSLOT = 5;
  public static final int WAIT_DEPENDENCY = 5;

  public static final int MVIL_ADDR_LIMIT = 0x800;

  public GetOpt opts;

  public void init(String[] args)
  {
    // init: must be implemented in sub-classes
    opts = new GetOpt();
    opts.setArgs(args);
    opts.setDefault("lreg_start", 24);
    opts.setDefault("enable_mvil", 1);
    opts.setDefault("enable_mul", 0);
    opts.setDefault("enable_mvc", 0);
    opts.setDefault("enable_wa", 0);
    opts.setDefault("enable_multi_bit_shift", 0);
    opts.setDefault("enable_uart", 1);
    opts.setDefault("width_m_d", 16);
    opts.setDefault("depth_m_i", 10);
    opts.setDefault("depth_m_d", 8);
    opts.setDefault("depth_u2m", 8);

    DEPTH_REG = 5;

    ENABLE_MVIL = opts.getIntValue("enable_mvil");
    ENABLE_MUL = opts.getIntValue("enable_mul");
    ENABLE_MVC = opts.getIntValue("enable_mvc");
    ENABLE_WA = opts.getIntValue("enable_wa");
    ENABLE_MULTI_BIT_SHIFT = opts.getIntValue("enable_multi_bit_shift");
    ENABLE_UART = opts.getIntValue("enable_uart");

    WIDTH_I = 16;
    DEPTH_IO_REG = 5;
    WIDTH_M_D = opts.getIntValue("width_m_d");
    DEPTH_M_I = opts.getIntValue("depth_m_i");
    DEPTH_M_D = opts.getIntValue("depth_m_d");
    DEPTH_U2M = opts.getIntValue("depth_u2m");
    DEPTH_B_U = Integer.max(DEPTH_M_I, DEPTH_U2M);
    DEPTH_V_U = (DEPTH_B_U + 2);
    DEPTH_B_M_W = Integer.max(DEPTH_M_D, DEPTH_IO_REG);
    DEPTH_V_M_W = (DEPTH_B_M_W + 1);
    DEPTH_B_M_R = Integer.max(DEPTH_M_D, Integer.max(DEPTH_IO_REG, DEPTH_U2M));
    DEPTH_V_M_R = (DEPTH_B_M_R + 2);

    MASTER_W_BANK_MEM_D = 0;
    MASTER_W_BANK_IO_REG = 1;
    MASTER_R_BANK_MEM_D = 0;
    MASTER_R_BANK_IO_REG = 1;
    MASTER_R_BANK_U2M = 2;
    IO_REG_R_UART_BUSY = 0;
    IO_REG_W_LED = 1;
    IO_REG_W_UART = 2;

    REGS = (1 << DEPTH_REG);
    SP_REG_STACK_POINTER = (REGS - 1);

    LREG0 = 24;
    LREG1 = 25;
    LREG2 = 26;
    LREG3 = 27;
    LREG4 = 28;
    LREG5 = 29;
    LREG6 = 30;

    STACK_ADDRESS = ((1 << DEPTH_M_D) - 1);
  }

  // jump to label
  public void lib_ba(String name)
  {
    // modify: SP_REG_MVIL
    int addr = addr_abs(name);
    if (addr > MVIL_ADDR_LIMIT)
    {
      print_error("lib_ba: exceeds address limit");
    }
    lib_mvil(addr);
    lib_wait_ds_pre();
    as_ba(SP_REG_MVIL);
    lib_wait_ds_post();
  }

  // branch to label
  public void lib_bc(String name)
  {
    // modify: SP_REG_MVIL
    int addr = addr_abs(name);
    if (addr > MVIL_ADDR_LIMIT)
    {
      print_error("lib_bc: exceeds address limit");
    }
    lib_mvil(addr);
    lib_wait_ds_pre();
    as_bc(SP_REG_CP, SP_REG_MVIL);
    lib_wait_ds_post();
  }

  // call function
  public void lib_call(String name)
  {
    // modify: SP_REG_MVIL
    int addr = addr_abs(name);
    if (addr > MVIL_ADDR_LIMIT)
    {
      print_error("lib_call: exceeds address limit");
    }
    lib_mvil(addr);
    lib_wait_ds_pre();
    as_bl(SP_REG_LINK, SP_REG_MVIL);
    lib_wait_ds_post();
  }

  // initialize stack
  public void lib_init_stack()
  {
    /* prerequisite: mem_d depth <= 11 */
    lib_mvil(STACK_ADDRESS);
    lib_wait_dep_pre();
    as_mv(SP_REG_STACK_POINTER, SP_REG_MVIL);
    lib_wait_dep_post();
  }

  // load with label
  public void lib_ld(int reg, String name)
  {
    int addr = addr_abs(name);
    if (addr > MVIL_ADDR_LIMIT)
    {
      print_error("lib_ld: exceeds address limit");
    }
    lib_mvil(addr);
    as_ld(reg, SP_REG_MVIL);
  }

  // MVIL wrapper
  public void lib_mvil(int value)
  {
    lib_wait_dep_pre();
    as_mvil(value);
    lib_wait_dep_post();
  }

  // store with label
  public void lib_st(String name, int reg)
  {
    int addr = addr_abs(name);
    if (addr > MVIL_ADDR_LIMIT)
    {
      print_error("lib_st: exceeds address limit");
    }
    lib_mvil(addr);
    as_st(SP_REG_MVIL, reg);
  }

  // simple NOP x repeat
  public void lib_nop(int repeat)
  {
    for (int i = 0; i < repeat; i++)
    {
      as_nop();
    }
  }

  // stack: push r[reg]
  public void lib_push(int reg)
  {
    lib_wait_dep_pre();
    as_nop();
    lib_wait_dep_post();
    as_st(SP_REG_STACK_POINTER, reg);
    as_subi(SP_REG_STACK_POINTER, 1);
  }

  // stack: pop to r[reg]
  public void lib_pop(int reg)
  {
    lib_wait_dep_pre();
    as_nop();
    lib_wait_dep_post();
    lib_wait_dep_pre();
    as_addi(SP_REG_STACK_POINTER, 1);
    lib_wait_dep_post();
    as_ld(reg, SP_REG_STACK_POINTER);
  }

  // stack: push r[reg_s] ~ r[reg_s + num - 1]
  public void lib_push_regs(int reg_s, int num)
  {
    lib_wait_dep_pre();
    as_nop();
    lib_wait_dep_post();
    if ((reg_s < 0) || (reg_s >= LREG0))
    {
      print_error("lib_push_regs: invalid register number");
    }
    if ((num < 1) || (num > 7))
    {
      print_error("lib_push_regs: 1 <= num <= 7");
    }
    for (int i = 1; i < num; i++)
    {
      as_mv(LREG0 + i, SP_REG_STACK_POINTER);
    }
    as_mv(LREG0, SP_REG_STACK_POINTER);
    as_subi(SP_REG_STACK_POINTER, num);
    if (num < 5)
    {
      lib_nop(5 - num);
    }
    for (int i = 1; i < num; i++)
    {
      as_subi(LREG0 + i, i);
    }
    as_st(LREG0, reg_s);
    if (num < 6)
    {
      lib_nop(6 - num);
    }
    for (int i = 1; i < num; i++)
    {
      as_st(LREG0 + i, reg_s + i);
    }
  }

  // stack: pop to r[reg_s + num - 1] ~ r[reg_s]
  public void lib_pop_regs(int reg_s, int num)
  {
    lib_wait_dep_pre();
    as_nop();
    lib_wait_dep_post();
    if ((reg_s < 0) || (reg_s >= LREG0))
    {
      print_error("lib_pop_regs: invalid register number");
    }
    if ((num < 1) || (num > 7))
    {
      print_error("lib_pop_regs: 1 <= num <= 7");
    }
    for (int i = 0; i < num; i++)
    {
      as_mv(LREG0 + i, SP_REG_STACK_POINTER);
    }
    as_addi(SP_REG_STACK_POINTER, num);
    if (num < 5)
    {
      lib_nop(5 - num);
    }
    for (int i = 0; i < num; i++)
    {
      as_addi(LREG0 + i, i + 1);
    }
    if (num < 6)
    {
      lib_nop(6 - num);
    }
    for (int i = 0; i < num; i++)
    {
      as_ld(num - 1 - i + reg_s, LREG0 + i);
    }
  }

  // return
  public void lib_return()
  {
    lib_wait_dep_pre();
    as_nop();
    lib_wait_dep_post();
    lib_wait_ds_pre();
    as_ba(SP_REG_LINK);
    lib_wait_ds_post();
  }

  // Reg[reg] = value (11bit)
  public void lib_set_im(int reg, int value)
  {
    lib_mvil(value);
    lib_wait_dep_pre();
    as_mv(reg, SP_REG_MVIL);
    lib_wait_dep_post();
  }

  // wait: delay_slot
  public void lib_wait_ds_pre()
  {
    if (ENABLE_WA == 1)
    {
      as_wa(WAIT_DELAYSLOT);
    }
  }

  // wait: delay_slot
  public void lib_wait_ds_post()
  {
    if (ENABLE_WA != 1)
    {
      lib_nop(WAIT_DELAYSLOT);
    }
  }

  // wait: dependency
  public void lib_wait_dep_pre()
  {
    if (ENABLE_WA == 1)
    {
      as_wa(WAIT_DEPENDENCY);
    }
  }

  // wait: dependency
  public void lib_wait_dep_post()
  {
    if (ENABLE_WA != 1)
    {
      lib_nop(WAIT_DEPENDENCY);
    }
  }

  // wait: delay_slot
  public void lib_wait_delay_slot()
  {
    if (ENABLE_WA == 1)
    {
      as_wa(WAIT_DELAYSLOT - 1);
      as_nop();
    }
    else
    {
      lib_nop(WAIT_DELAYSLOT);
    }
  }

  // wait: dependency
  public void lib_wait_dependency()
  {
    if (ENABLE_WA == 1)
    {
      as_wa(WAIT_DEPENDENCY - 1);
      as_nop();
    }
    else
    {
      lib_nop(WAIT_DEPENDENCY);
    }
  }


  // functions ---------------------------------------------

  public void f_halt()
  {
    // halt
    // input: none
    // output: none
    label("f_halt");
    lib_ba("f_halt");
  }

  public void f_memcpy()
  {
    // input: R3:dst_addr R4:src_addr R5:copy_size
    /*
    do
    {
      size -= 1;
      data = mem[addr_src];
      mem[addr_dst] = data;
      addr_src++;
      addr_dst++;
    } while (size != 0)
    mem[addr_reset] = 0;
    */
    int addr_dst = R3;
    int addr_src = R4;
    int size = R5;
    int data = LREG0;
    label("f_memcpy");
    label("f_memcpy_L_0");
    as_ld(data, addr_src);
    as_subi(size, 1);
    as_addi(addr_src, 1);
    lib_nop(3);
    as_st(addr_dst, data);
    as_cnz(SP_REG_CP, size);
    as_addi(addr_dst, 1);
    lib_bc("f_memcpy_L_0");
    lib_return();
  }

  // return random value r3
  public void f_rand()
  {
    // input: none
    // output: r3:random
    label("f_rand");
    m_f_rand();
    lib_return();
  }

  // return random value (0 ~ (N - 1))
  public void f_nrand()
  {
    // input: r3:max number N (16bit)
    // output: r3:random (0 ~ (N - 1))
    /*
    rmax = R3;
    rand = f_rand();
    rand &= 0xffff;
    rand *= rmax;
    rand >>= 16;
    */
    int rmax = LREG3;
    int constff = LREG4;
    int const16 = LREG5;
    label("f_nrand");
    lib_push(SP_REG_LINK);
    as_mv(rmax, R3);
    lib_call("f_rand");
    lib_wait_dep_pre();
    as_mvi(const16, 8);
    lib_wait_dep_post();
    as_addi(const16, 8);
    lib_wait_dep_pre();
    as_mvi(constff, -1);
    lib_wait_dep_post();
    lib_sr(constff, const16);
    lib_wait_dependency();
    lib_wait_dep_pre();
    as_and(R3, constff);
    lib_wait_dep_post();
    lib_wait_dep_pre();
    as_mul(R3, rmax);
    lib_wait_dep_post();
    lib_sr(R3, const16);
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  public void f_rand_init()
  {
    // input: R3:random seed
    label("f_rand_init");
    int Raddr = LREG0;
    int Rseed1 = LREG1;
    int Rseed2 = LREG2;
    int Rseed3 = LREG3;
    lib_set_im(Raddr, addr_abs("d_rand"));
    lib_set_im(Rseed1, 0x7b4);
    lib_set_im(Rseed2, 0x4ae);
    lib_set_im(Rseed3, 0x3b1);
    lib_sli(Rseed1, 11);
    lib_wait_dependency();
    lib_sli(Rseed2, 11);
    lib_wait_dependency();
    lib_sli(Rseed1, 10);
    lib_wait_dep_pre();
    as_or(Rseed2, Rseed3);
    lib_wait_dep_post();
    lib_wait_dep_pre();
    as_or(Rseed1, Rseed2);
    lib_wait_dep_post();
    lib_wait_dep_pre();
    as_add(Rseed1, R3);
    lib_wait_dep_post();
    as_st(Raddr, Rseed1);
    lib_return();
  }

  // macro of f_rand
  public void m_f_rand()
  {
    /*
    seed_addr = addr_abs("d_rand");
    const17 = 17;
    rand0 = mem[seed_addr];
    rand1 = mem[seed_addr];
    rand1 <<= 13;
    rand0 ^= rand1;
    rand1 = rand0;
    rand1 >>= const17;
    rand0 ^= rand1;
    rand1 = rand0;
    rand1 <<= 5;
    rand0 ^= rand1;
    mem[seed_addr] = rand0;
    */
    int rand0 = R3;
    int rand1 = LREG0;
    int const17 = LREG1;
    int seed_addr = LREG2;
    lib_set_im(seed_addr, addr_abs("d_rand"));
    lib_set_im(const17, 17);
    as_ld(rand0, seed_addr);
    lib_wait_dep_pre();
    as_ld(rand1, seed_addr);
    lib_wait_dep_post();
    lib_sli(rand1, 13);
    lib_wait_dependency();
    lib_wait_dep_pre();
    as_xor(rand0, rand1);
    lib_wait_dep_post();
    lib_wait_dep_pre();
    as_mv(rand1, rand0);
    lib_wait_dep_post();
    lib_sri(rand1, const17);
    lib_wait_dependency();
    lib_wait_dep_pre();
    as_xor(rand0, rand1);
    lib_wait_dep_post();
    lib_wait_dep_pre();
    as_mv(rand1, rand0);
    lib_wait_dep_post();
    lib_sli(rand1, 5);
    lib_wait_dependency();
    lib_wait_dep_pre();
    as_xor(rand0, rand1);
    lib_wait_dep_post();
    as_st(seed_addr, rand0);
  }

  public void lib_sl(int reg_d, int reg_a)
  {
    if (ENABLE_MULTI_BIT_SHIFT == 1)
    {
      as_sl(reg_d, reg_a);
    }
    else
    {
      as_mv(LREG5, reg_d);
      as_mv(LREG6, reg_a);
      lib_call("f_lib_sl");
      as_mv(reg_d, LREG5);
    }
  }

  public void lib_sli(int reg_d, int shift_width)
  {
    if ((shift_width < 0) || (shift_width > 15))
    {
      print_error("lib_sli: shift_width exceeds limit 0 to 15");
    }
    if (ENABLE_MULTI_BIT_SHIFT == 1)
    {
      as_sli(reg_d, shift_width);
    }
    else
    {
      as_mv(LREG5, reg_d);
      as_mvi(LREG6, shift_width);
      lib_call("f_lib_sl");
      as_mv(reg_d, LREG5);
    }
  }

  public void f_lib_sl()
  {
    // sl for 1bit-shift
    // input
    // LREG5: input value
    // LREG6: shift width
    // output
    // LREG5: result
    label("f_lib_sl");
    as_cnz(SP_REG_CP, LREG6);
    lib_bc("f_lib_sl_L_0");
    lib_return();
    label("f_lib_sl_L_0");
    as_sli(LREG5, 1);
    lib_wait_dep_pre();
    as_subi(LREG6, 1);
    lib_wait_dep_post();
    lib_ba("f_lib_sl");
  }

  public void lib_sr(int reg_d, int reg_a)
  {
    if (ENABLE_MULTI_BIT_SHIFT == 1)
    {
      as_sr(reg_d, reg_a);
    }
    else
    {
      as_mv(LREG5, reg_d);
      as_mv(LREG6, reg_a);
      lib_call("f_lib_sr");
      as_mv(reg_d, LREG5);
    }
  }

  public void lib_sri(int reg_d, int shift_width)
  {
    if ((shift_width < 0) || (shift_width > 15))
    {
      print_error("lib_sri: shift_width exceeds limit 0 to 15");
    }
    if (ENABLE_MULTI_BIT_SHIFT == 1)
    {
      as_sri(reg_d, shift_width);
    }
    else
    {
      as_mv(LREG5, reg_d);
      as_mvi(LREG6, shift_width);
      lib_call("f_lib_sr");
      as_mv(reg_d, LREG5);
    }
  }

  public void f_lib_sr()
  {
    /*
    L0:
    if (shift != 0) goto L1;
    return;
    L1:
    value >> 1;
    shift--;
    goto L0;
    */
    // sl for 1bit-shift
    // input
    // LREG5: input value
    // LREG6: shift width
    // output
    // LREG5: result
    label("f_lib_sr");
    as_cnz(SP_REG_CP, LREG6);
    lib_bc("f_lib_sr_L_0");
    lib_return();
    label("f_lib_sr_L_0");
    as_sri(LREG5, 1);
    lib_wait_dep_pre();
    as_subi(LREG6, 1);
    lib_wait_dep_post();
    lib_ba("f_lib_sr");
  }

  public void lib_sra(int reg_d, int reg_a)
  {
    if (ENABLE_MULTI_BIT_SHIFT == 1)
    {
      as_sra(reg_d, reg_a);
    }
    else
    {
      as_mv(LREG5, reg_d);
      as_mv(LREG6, reg_a);
      lib_call("f_lib_sra");
      as_mv(reg_d, LREG5);
    }
  }

  public void lib_srai(int reg_d, int shift_width)
  {
    if ((shift_width < 0) || (shift_width > 15))
    {
      print_error("lib_srai: shift_width exceeds limit 0 to 15");
    }
    if (ENABLE_MULTI_BIT_SHIFT == 1)
    {
      as_srai(reg_d, shift_width);
    }
    else
    {
      as_mv(LREG5, reg_d);
      as_mvi(LREG6, shift_width);
      lib_call("f_lib_sra");
      as_mv(reg_d, LREG5);
    }
  }

  public void f_lib_sra()
  {
    /*
    L0:
    if (shift != 0) goto L1;
    return;
    L1:
    value >>> 1;
    shift--;
    goto L0;
    */
    // sl for 1bit-shift
    // input
    // LREG5: input value
    // LREG6: shift width
    // output
    // LREG5: result
    label("f_lib_sra");
    as_cnz(SP_REG_CP, LREG6);
    lib_bc("f_lib_sra_L_0");
    lib_return();
    label("f_lib_sra_L_0");
    as_srai(LREG5, 1);
    lib_wait_dep_pre();
    as_subi(LREG6, 1);
    lib_wait_dep_post();
    lib_ba("f_lib_sra");
  }

  public void f_uart_char()
  {
    // uart put char
    // input: r3: char to send
    // output: none
    label("f_uart_char");
    lib_push(SP_REG_LINK);
    // LREG0 = UART_BUSY addr
    as_mvi(LREG0, MASTER_R_BANK_IO_REG);
    // LREG1 = UART TX addr
    lib_wait_dep_pre();
    as_mvi(LREG1, MASTER_W_BANK_IO_REG);
    lib_wait_dep_post();
    lib_sli(LREG0, DEPTH_B_M_R);
    lib_wait_dependency();
    lib_sli(LREG1, DEPTH_B_M_W);
    lib_wait_dependency();
    as_addi(LREG0, IO_REG_R_UART_BUSY);
    as_addi(LREG1, IO_REG_W_UART);
    // while (uart busy){}
    label("f_uart_char_L_0");
    lib_wait_dep_pre();
    as_ld(LREG2, LREG0);
    lib_wait_dep_post();
    as_cnz(SP_REG_CP, LREG2);
    lib_bc("f_uart_char_L_0");
    as_st(LREG1, R3);
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  public void f_uart_hex()
  {
    // uart put hex digit
    // input: r3:value(4bit)
    // output: none
    // modify: r3
    // depend: f_uart_char
    label("f_uart_hex");
    /*
    r3 = r3 & 15;
    LREG0 = 48;
    LREG1 = r3;
    LREG2 = 87;
    LREG3 = 9;
    if (LREG1 > LREG3) {LREG0 = 87};
    r3 = r3 + LREG0;
    */
    lib_push(SP_REG_LINK);
    as_andi(R3, 15);
    lib_set_im(LREG0, 48);
    as_mv(LREG1, R3);
    as_mvi(LREG3, 9);
    lib_set_im(LREG2, 87);
    lib_wait_dep_pre();
    as_sub(LREG3, LREG1);
    lib_wait_dep_post();
    lib_wait_dep_pre();
    as_cnm(SP_REG_CP, LREG3);
    lib_wait_dep_post();
    lib_bc("f_uart_hex_L1");
    lib_wait_dep_pre();
    as_mv(LREG0, LREG2);
    lib_wait_dep_post();
    label("f_uart_hex_L1");
    as_add(R3, LREG0);
    lib_call("f_uart_char");
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  public void f_uart_hex_word()
  {
    // uart put hex word
    // input: r3:value
    // output: none
    // modify: r3
    // depend: f_uart_char, f_uart_hex
    label("f_uart_hex_word");
    /*
    push(r4 to r6);
    r4 = r3;
    r5 = WIDTH_M_D - 4;
    do
    {
      r6 = r4;
      r3 = r6 >> r5;
      r5 -= 4;
      call f_uart_hex;
    } while (r5 != 0)
    pop(r6 to r4);
    */
    lib_push(SP_REG_LINK);
    lib_push_regs(R4, 3);
    as_mv(R4, R3);
    lib_set_im(R5, WIDTH_M_D - 4);
    label("f_uart_hex_word_L_0");
    lib_wait_dep_pre();
    as_mv(R6, R4);
    lib_wait_dep_post();
    lib_sr(R6, R5);
    lib_wait_dependency();
    as_mv(R3, R6);
    as_subi(R5, 4);
    lib_call("f_uart_hex");
    as_cnm(SP_REG_CP, R5);
    lib_bc("f_uart_hex_word_L_0");
    lib_pop_regs(R4, 3);
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  public void f_uart_hex_word_ln()
  {
    // uart register monitor
    // input: r3:value
    // output: none
    // depend: f_uart_hex_word, f_uart_hex, f_uart_char
    label("f_uart_hex_word_ln");
    lib_push(SP_REG_LINK);
    lib_call("f_uart_hex_word");
    as_mvi(R3, 13);
    lib_call("f_uart_char");
    as_mvi(R3, 10);
    lib_call("f_uart_char");
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  public void f_uart_memory_dump()
  {
    // uart memory dump
    // input: r3:start_address r4:dump_size(words)
    // output: none
    // depend: f_uart_char, f_uart_hex, f_uart_hex_word
    label("f_uart_memory_dump");
    /*
    push r5;
    r5 = r3;
    do
    {
      r3 = r5;
      call f_uart_hex_word (put address)
      r3 = 32;
      call f_uart_char (put space)
      r3 = mem[r5];
      r5++;
      r4--;
      call f_uart_hex_word (put data)
      r3 = 10;
      call f_uart_char (put enter)
    } while (r4 != 0)
    pop r5;
     */
    lib_push(SP_REG_LINK);
    lib_push(R5);
    lib_wait_dep_pre();
    as_mv(R5, R3);
    lib_wait_dep_post();
    label("f_uart_memory_dump_L_0");
    as_mv(R3, R5);
    lib_call("f_uart_hex_word");
    lib_set_im(R3, 32);
    lib_call("f_uart_char");
    as_ld(R3, R5);
    as_addi(R5, 1);
    as_subi(R4, 1);
    lib_call("f_uart_hex_word");
    as_mvi(R3, 13);
    lib_call("f_uart_char");
    as_mvi(R3, 10);
    lib_call("f_uart_char");
    as_cnz(SP_REG_CP, R4);
    lib_bc("f_uart_memory_dump_L_0");
    lib_pop(R5);
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  public void f_uart_print()
  {
    // uart print string
    // input: r3:text_start_address
    // output: none
    // depend: f_uart_char
    label("f_uart_print");
    /*
    addr:r4 shift:r5 char:r6
    addr = r3;
    shift = 24;
    do
    {
      char = mem[addr] >> shift;
      r3 = char & 0xff;
      if (r3 == 0) break;
      call f_uart_char;
      shift -= 8;
      if (shift < 0)
      {
        shift = 24;
        addr++;
      }
    } while (1)
    */
    int Rchar = R3;
    int Raddr = R4;
    int Rshift = R5;
    int Ri0xff = R6;
    int Ri24 = R7;

    lib_push(SP_REG_LINK);
    lib_push_regs(R4, 4);
    as_mv(Raddr, R3);
    lib_set_im(Ri24, 24);
    lib_set_im(Ri0xff, 0xff);
    as_mv(Rshift, Ri24);
    label("f_uart_print_L_0");
    lib_wait_dep_pre();
    as_ld(Rchar, Raddr);
    lib_wait_dep_post();
    lib_sr(Rchar, Rshift);
    lib_wait_dependency();
    lib_wait_dep_pre();
    as_and(Rchar, Ri0xff);
    lib_wait_dep_post();
    as_cnz(SP_REG_CP, Rchar);
    lib_bc("f_uart_print_L_1");
    lib_ba("f_uart_print_L_2");
    label("f_uart_print_L_1");
    lib_call("f_uart_char");
    lib_wait_dep_pre();
    as_subi(Rshift, 8);
    lib_wait_dep_post();
    as_cnm(SP_REG_CP, Rshift);
    lib_bc("f_uart_print_L_0");
    as_mv(Rshift, Ri24);
    as_addi(Raddr, 1);
    lib_ba("f_uart_print_L_0");
    label("f_uart_print_L_2");
    lib_pop_regs(R4, 4);
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  public void f_uart_print_16()
  {
    // uart print string
    // input: r3:text_start_address
    // output: none
    // depend: f_uart_char
    label("f_uart_print_16");
    /*
    addr:r4 shift:r5 char:r3
    addr = r3;
    shift = 8;
    do
    {
      char = mem[addr] >> shift;
      r3 = char & 0xff;
      if (r3 == 0) break;
      call f_uart_char;
      shift -= 8;
      if (shift < 0)
      {
        shift = 8;
        addr++;
      }
    } while (1)
    */
    int Rchar = R3;
    int Raddr = R4;
    int Rshift = R5;
    int Ri0xff = R6;
    int Ri8 = R7;

    lib_push(SP_REG_LINK);
    lib_push_regs(R4, 4);
    as_mv(Raddr, R3);
    lib_set_im(Ri8, 8);
    lib_set_im(Ri0xff, 0xff);
    as_mv(Rshift, Ri8);
    label("f_uart_print_16_L_0");
    lib_wait_dep_pre();
    as_ld(Rchar, Raddr);
    lib_wait_dep_post();
    lib_wait_dep_pre();
    lib_sr(Rchar, Rshift);
    lib_wait_dep_post();
    lib_wait_dep_pre();
    as_and(Rchar, Ri0xff);
    lib_wait_dep_post();
    as_cnz(SP_REG_CP, Rchar);
    lib_bc("f_uart_print_16_L_1");
    lib_ba("f_uart_print_16_L_2");
    label("f_uart_print_16_L_1");
    lib_call("f_uart_char");
    lib_wait_dep_pre();
    as_subi(Rshift, 8);
    lib_wait_dep_post();
    as_cnm(SP_REG_CP, Rshift);
    lib_bc("f_uart_print_16_L_0");
    as_mv(Rshift, Ri8);
    as_addi(Raddr, 1);
    lib_ba("f_uart_print_16_L_0");
    label("f_uart_print_16_L_2");
    lib_pop_regs(R4, 4);
    lib_pop(SP_REG_LINK);
    lib_return();
  }

  public void f_wait()
  {
    // simple wait ()
    // input: r3:count
    // modify: r3
    label("f_wait");
    lib_wait_dep_pre();
    as_subi(R3, 1);
    lib_wait_dep_post();
    as_cnz(SP_REG_CP, R3);
    lib_bc("f_wait");
    lib_return();
  }
}
