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

public class AsmLib extends Asm
{
  public static final int DEPTH_D = 8;

  public static final int REGS = 32;

  public static final int SP_REG_CP = 0;
  public static final int SP_REG_MVIL = 1;
  public static final int SP_REG_LINK = 2;
  public static final int SP_REG_STACK_POINTER = (REGS - 1);

  public int stackAddress = ((1 << DEPTH_D) - 1);

  // set default stack address
  public void set_stack_address(int addr)
  {
    stackAddress = addr;
  }

  // jump to label
  public void lib_ba(String name)
  {
    // modify: SP_REG_MVIL
    int addr = addr_abs(name);
    if (addr > 0x800)
    {
      print_error("lib_ba: exceed address limit");
    }
    as_mvil(addr);
    lib_wait_dependency();
    as_ba(SP_REG_MVIL);
    lib_wait_delay_slot();
  }

  // branch to label
  public void lib_bc(String name)
  {
    // modify: SP_REG_MVIL
    int addr = addr_abs(name);
    if (addr > 0x800)
    {
      print_error("lib_bc: exceed address limit");
    }
    as_mvil(addr);
    lib_wait_dependency();
    as_bc(SP_REG_CP, SP_REG_MVIL);
    lib_wait_delay_slot();
  }

  // call function
  public void lib_call(String name)
  {
    // modify: SP_REG_MVIL
    int addr = addr_abs(name);
    if (addr > 0x800)
    {
      print_error("lib_call: exceed address limit");
    }
    as_mvil(addr);
    lib_wait_dependency();
    as_bl(SP_REG_LINK, SP_REG_MVIL);
    lib_wait_delay_slot();
  }

  // initialize stack
  public void lib_init_stack()
  {
    as_mvil(stackAddress);
    lib_wait_dependency();
    as_mv(SP_REG_STACK_POINTER, SP_REG_MVIL);
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
    lib_wait_dependency();
    as_st(SP_REG_STACK_POINTER, reg);
    as_subi(SP_REG_STACK_POINTER, 1);
  }

  // stack: pop to r[reg]
  public void lib_pop(int reg)
  {
    lib_wait_dependency();
    as_addi(SP_REG_STACK_POINTER, 1);
    lib_wait_dependency();
    as_ld(reg, SP_REG_STACK_POINTER);
  }

  // stack: push r[reg_s] ~ r[reg_e]
  public void lib_push_regs(int reg_s, int reg_e)
  {
    if ((reg_s < 0) || (reg_s >= REGS) || (reg_e < 0) || (reg_e >= REGS))
    {
      print_error("lib_push_regs: invalid register number");
    }
    if ((reg_e - reg_s) < 1)
    {
      print_error("lib_push_regs: reg_e > reg_s");
    }
    for (int i = reg_s; i <= reg_e; i++)
    {
      lib_push(i);
    }
  }

  // stack: pop to r[reg_e] ~ r[reg_s]
  public void lib_pop_regs(int reg_e, int reg_s)
  {
    if ((reg_e - reg_s) < 1)
    {
      print_error("lib_push_regs: reg_e > reg_s");
    }
    if ((reg_s < 0) || (reg_s >= REGS) || (reg_e < 0) || (reg_e >= REGS))
    {
      print_error("lib_push_regs: invalid register number");
    }
    for (int i = reg_e; i >= reg_s; i--)
    {
      lib_pop(i);
    }
  }

  // return
  public void lib_return()
  {
    as_ba(SP_REG_LINK);
    lib_wait_delay_slot();
  }

  // Reg[reg] = value (11bit)
  public void lib_set_im(int reg, int value)
  {
    as_mvil(value);
    lib_wait_dependency();
    as_mv(reg, SP_REG_MVIL);
  }

  // wait: delay_slot
  public void lib_wait_delay_slot()
  {
    lib_nop(5);
  }

  // wait: dependency
  public void lib_wait_dependency()
  {
    lib_nop(5);
  }
}
