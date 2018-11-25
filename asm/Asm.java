/*
  Copyright (c) 2015-2018, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.io.*;
import java.util.*;

public class Asm
{
  private static final int DATA_WIDTH = 16;
  private static final int OPERAND_BITS = 5;
  private static final int REG_IM_BITS = 5;
  private static final int W_CODE = 0;
  private static final int W_DATA = 1;

  private int romDepth = 8;
  private int codeROMDepth = 8;
  private int dataROMDepth = 8;
  private int pAddress;
  private int pass;
  private String fileName = "default";
  private final ArrayList<Integer> data = new ArrayList<Integer>();
  private final HashMap<String, Integer> labelValue = new HashMap<String, Integer>();

  // opcode
  private static final int I_NOP  = 0x00; // 5'b00000;
  private static final int I_ST   = 0x01; // 5'b00001;
  private static final int I_MVC  = 0x02; // 5'b00010;
  private static final int I_BA   = 0x04; // 5'b00100;
  private static final int I_BC   = 0x05; // 5'b00101;
  private static final int I_WA   = 0x06; // 5'b00110;
  private static final int I_BL   = 0x07; // 5'b00111;
  private static final int I_ADD  = 0x08; // 5'b01000;
  private static final int I_SUB  = 0x09; // 5'b01001;
  private static final int I_AND  = 0x0a; // 5'b01010;
  private static final int I_OR   = 0x0b; // 5'b01011;
  private static final int I_XOR  = 0x0c; // 5'b01100;
  private static final int I_MUL  = 0x0d; // 5'b01101;
  private static final int I_MV   = 0x10; // 5'b10000;
  private static final int I_MVIL = 0x11; // 5'b10001;
  private static final int I_LD   = 0x17; // 5'b10111;
  private static final int I_SR   = 0x18; // 5'b11000;
  private static final int I_SL   = 0x19; // 5'b11001;
  private static final int I_SRA  = 0x1a; // 5'b11010;
  private static final int I_CNZ  = 0x1c; // 5'b11100;
  private static final int I_CNM  = 0x1d; // 5'b11101;

  public void write_mem(int mode)
  {
    try
    {
      String name;
      if (mode == W_CODE)
      {
        name = "code";
        romDepth = codeROMDepth;
      }
      else
      {
        name = "data";
        romDepth = dataROMDepth;
      }
      String hdlName = fileName + "_" + name + "_mem";
      File file = new File("../" + hdlName + ".v");
      file.createNewFile();
      PrintWriter writer = new PrintWriter(file);
      writer.printf(
        "module %s\n", hdlName);
      writer.printf(
        "  #(\n" +
        "    parameter DATA_WIDTH=16,\n");
      writer.printf(
        "    parameter ADDR_WIDTH=%d\n", romDepth);
      writer.printf(
        "    )\n" +
        "  (\n" +
        "   input                         clk,\n" +
        "   input [(ADDR_WIDTH-1):0]      addr_r,\n" +
        "   input [(ADDR_WIDTH-1):0]      addr_w,\n" +
        "   input [(DATA_WIDTH-1):0]      data_in,\n" +
        "   input                         we,\n" +
        "   output reg [(DATA_WIDTH-1):0] data_out\n" +
        "   );\n" +
        "\n" +
        "  reg [DATA_WIDTH-1:0]           ram [0:(1 << ADDR_WIDTH)-1];\n" +
        "\n" +
        "  always @(posedge clk)\n" +
        "    begin\n" +
        "      data_out <= ram[addr_r];\n" +
        "      if (we)\n" +
        "        begin\n" +
        "          ram[addr_w] <= data_in;\n" +
        "        end\n" +
        "    end\n" +
        "\n" +
        "  initial\n" +
        "    begin\n");
      for (int i = 0; i < (1 << romDepth); i++)
      {
        int d;
        if (i < data.size())
        {
          d = data.get(i);
        }
        else
        {
          d = 0;
        }
        writer.printf("      ram[16'h%04x] = 16'h%04x;\n", i, d);
      }
      writer.printf(
        "    end\n" +
        "\n" +
        "endmodule\n");
      writer.close();
    }
    catch (Exception e)
    {
    }
  }

  public void init()
  {
    // init: must be implemented in sub-classes
  }

  public void program()
  {
    // program: must be implemented in sub-classes
  }

  public void data()
  {
    // data: must be implemented in sub-classes
  }

  // Print Error
  public void print_error(String err)
  {
    System.out.printf("Error: %s Address: %d\n", err, pAddress);
    System.exit(1);
  }

  // Print Info
  public void print_info(String info)
  {
    if (pass == 1)
    {
      System.out.println(info);
    }
  }

  // set rom depth
  public void set_rom_depth(int depth)
  {
    romDepth = depth;
    codeROMDepth = depth;
    dataROMDepth = depth;
  }

  // set rom depth
  public void set_rom_depth(int code_depth, int data_depth)
  {
    romDepth = code_depth;
    codeROMDepth = code_depth;
    dataROMDepth = data_depth;
  }

  // set filename
  public void set_filename(String name)
  {
    fileName = name;
  }

  // label (hold the current program counter)
  public void label(String key)
  {
    if (pass == 0)
    {
      labelValue.put(key, pAddress);
    }
    print_info(String.format("Label: %s: %x", key, pAddress));
  }

  // return the absolute address of the label
  public int addr_abs(String key)
  {
    if (pass == 0)
    {
      return 0;
    }
    if (labelValue.get(key) == null)
    {
      print_error("addr_abs: key: " + key);
      return 0;
    }
    return labelValue.get(key);
  }

  // return the relative address between the current line and the label
  public int addr_rel(String key)
  {
    if (pass == 0)
    {
      return 0;
    }
    if (labelValue.get(key) == null)
    {
      print_error("addr_rel: key: " + key);
      return 0;
    }
    return labelValue.get(key) - pAddress;
  }

  public void do_asm()
  {
    init();

    labelValue.clear();
    data.clear();

    pass = 0; // pass 1
    pAddress = 0;
    data.clear();
    program();
    pAddress = 0;
    data.clear();
    data();

    pass++; // pass 2
    pAddress = 0;
    data.clear();
    program();
    write_mem(W_CODE);
    pAddress = 0;
    data.clear();
    data();
    write_mem(W_DATA);
  }

  private void store_inst(int inst)
  {
    if (pass == 1)
    {
      if (pAddress > (1 << romDepth))
      {
        print_error("memory full");
      }
      data.add(pAddress, inst);
    }
    pAddress++;
  }

  private int cut_bits(int bits, int value)
  {
    return (value & ((1 << bits) - 1));
  }

  private int set_field(int shift, int bits, int value)
  {
    return (cut_bits(bits, value) << shift);
  }

  private void set_inst_normal(int reg_d, int reg_a, int is_im, int op)
  {
    int inst = 0;
    inst |= set_field(11, 5, reg_d);
    inst |= set_field(6, 5, reg_a);
    inst |= set_field(5, 1, is_im);
    inst |= set_field(0, 5, op);
    store_inst(inst);
  }

  private void set_inst_mvil(int im, int op)
  {
    int inst = 0;
    inst |= set_field(5, 11, im);
    inst |= set_field(0, 5, op);
    store_inst(inst);
  }

  // data store
  public void d16(int value0)
  {
    store_inst(value0);
  }

  // data store x4
  public void d16x4(int value0, int value1, int value2, int value3)
  {
    store_inst(value0);
    store_inst(value1);
    store_inst(value2);
    store_inst(value3);
  }

  // store byte to int
  public int byte_store(int value_int, int value_byte, int position)
  {
    int mask = ~(0xff << (position * 8));
    int out = (value_int & mask) | (value_byte << (position * 8));
    return out;
  }

  // string data store
  public void string_data(String s)
  {
    int length = (s.length() + 2) & (~1);
    int byte_count = 1;
    int value_int = 0;
    for (int i = 0; i < length; i++)
    {
      int value_byte = 0;
      if (i < s.length())
      {
        value_byte = s.charAt(i);
      }
      value_int = byte_store(value_int, value_byte, byte_count);
      if (byte_count == 0)
      {
        store_inst(value_int);
        byte_count = 1;
      }
      else
      {
        byte_count--;
      }
    }
  }

  // assembly

  public void as_nop()
  {
    set_inst_normal(0, 0, 0, I_NOP);
  }

  public void as_ba(int reg_a)
  {
    set_inst_normal(0, reg_a, 0, I_BA);
  }

  public void as_bc(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_BC);
  }

  public void as_st(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_ST);
  }

  public void as_sti(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 1, I_ST);
  }

  public void as_wa(int reg_a)
  {
    set_inst_normal(0, reg_a, 0, I_WA);
  }

  public void as_mv(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_MV);
  }

  public void as_mvi(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 1, I_MV);
  }

  public void as_add(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_ADD);
  }

  public void as_addi(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 1, I_ADD);
  }

  public void as_sub(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_SUB);
  }

  public void as_subi(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 1, I_SUB);
  }

  public void as_and(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_AND);
  }

  public void as_andi(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 1, I_AND);
  }

  public void as_or(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_OR);
  }

  public void as_ori(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 1, I_OR);
  }

  public void as_xor(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_XOR);
  }

  public void as_xori(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 1, I_XOR);
  }

  public void as_mul(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_MUL);
  }

  public void as_muli(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 1, I_MUL);
  }

  public void as_mvil(int im)
  {
    set_inst_mvil(im, I_MVIL);
  }

  public void as_mvc(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_MVC);
  }

  public void as_sr(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_SR);
  }

  public void as_sri(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 1, I_SR);
  }

  public void as_sl(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_SL);
  }

  public void as_sli(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 1, I_SL);
  }

  public void as_sra(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_SRA);
  }

  public void as_srai(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 1, I_SRA);
  }

  public void as_bl(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_BL);
  }

  public void as_ld(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_LD);
  }

  public void as_cnz(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_CNZ);
  }

  public void as_cnm(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_CNM);
  }

}
