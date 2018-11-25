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

`define ENABLE_MVIL
//`define ENABLE_MUL
//`define ENABLE_MULTI_BIT_SHIFT
//`define ENABLE_MVC
//`define ENABLE_WA
//`define FULL_PIPELINED_ALU

module mini16_cpu
  #(
    parameter WIDTH_I = 16,
    parameter WIDTH_D = 16,
    parameter DEPTH_I = 8,
    parameter DEPTH_D = 8,
    parameter DEPTH_REG = 5
    )
  (
   input                    clk,
   input                    reset,
   output reg [DEPTH_I-1:0] mem_i_r_addr,
   input [WIDTH_I-1:0]      mem_i_r_data,
   output reg [DEPTH_D-1:0] mem_d_r_addr,
   input [WIDTH_D-1:0]      mem_d_r_data,
   output reg [DEPTH_D-1:0] mem_d_w_addr,
   output reg [WIDTH_D-1:0] mem_d_w_data,
   output reg               mem_d_we
   );

  localparam TRUE = 1'b1;
  localparam FALSE = 1'b0;
  localparam ONE = 1'd1;
  localparam ZERO = 1'd0;
  localparam FFFF = {WIDTH_D{1'b1}};
  localparam SHIFT_BITS = 4;
  localparam BL_OFFSET = 1'd1;

  // opcode
  localparam I_NOP  = 5'h00; // 5'b00000;
  localparam I_ST   = 5'h01; // 5'b00001;
  localparam I_MVC  = 5'h02; // 5'b00010;
  localparam I_BA   = 5'h04; // 5'b00100;
  localparam I_BC   = 5'h05; // 5'b00101;
  localparam I_WA   = 5'h06; // 5'b00110;
  localparam I_BL   = 5'h07; // 5'b00111;
  localparam I_ADD  = 5'h08; // 5'b01000;
  localparam I_SUB  = 5'h09; // 5'b01001;
  localparam I_AND  = 5'h0a; // 5'b01010;
  localparam I_OR   = 5'h0b; // 5'b01011;
  localparam I_XOR  = 5'h0c; // 5'b01100;
  localparam I_MUL  = 5'h0d; // 5'b01101;
  localparam I_MV   = 5'h10; // 5'b10000;
  localparam I_MVIL = 5'h11; // 5'b10001;
  localparam I_LD   = 5'h17; // 5'b10111;
  localparam I_SR   = 5'h18; // 5'b11000;
  localparam I_SL   = 5'h19; // 5'b11001;
  localparam I_SRA  = 5'h1a; // 5'b11010;
  localparam I_CNZ  = 5'h1c; // 5'b11100;
  localparam I_CNM  = 5'h1d; // 5'b11101;

  // special register
  localparam SP_REG_CP   = 0;
  localparam SP_REG_MVIL = 1;

  // stage 1 fetch
  reg  [WIDTH_I-1:0]   inst_s1;
  wire [DEPTH_REG-1:0] reg_d_s1;
  wire [DEPTH_REG-1:0] reg_a_s1;
  wire [4:0]           op_s1;
  wire                 is_im_s1;
  assign reg_d_s1 = inst_s1[15:11];
  assign reg_a_s1 = inst_s1[10:6];
  assign is_im_s1 = inst_s1[5];
  assign op_s1 = inst_s1[4:0];
  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          inst_s1 <= ZERO;
        end
      else
        begin
`ifdef ENABLE_WA
          if (wait_en_s2 == TRUE)
            begin
              inst_s1 <= ZERO;
            end
          else
            begin
              inst_s1 <= mem_i_r_data;
            end
`else
          inst_s1 <= mem_i_r_data;
`endif
        end
    end

`ifdef ENABLE_WA
  // stage 2 wait counter
  wire wait_en_s2;
  reg [4:0] wait_count_m1;
  assign wait_en_s2 = (wait_counter_s2 == ZERO) ? FALSE : TRUE;
  reg [9:0] wait_counter_s2;
  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          wait_counter_s2 <= ZERO;
          wait_count_m1 <= ZERO;
        end
      else
        begin
          if (op_s1 == I_WA)
            begin
              wait_counter_s2 <= reg_a_s1;
              wait_count_m1 <= reg_a_s1 - ONE;
            end
          else
            begin
              if (wait_en_s2 == TRUE)
                begin
                  wait_counter_s2 <= wait_counter_s2 - ONE;
                end
            end
        end
    end
`endif

`ifdef ENABLE_MVC
  // stage 2 set reg read addr
  reg [DEPTH_REG-1:0] reg_addr_a_s2;
  reg [DEPTH_REG-1:0] reg_addr_b_s2;
  always @(posedge clk)
    begin
      reg_addr_b_s2 <= reg_a_s1;
      if (op_s1 == I_MVC)
        begin
          reg_addr_a_s2 <= SP_REG_CP;
        end
      else
        begin
          reg_addr_a_s2 <= reg_d_s1;
        end
    end
`endif

  // stage 2 delay
  reg [4:0]           op_s2;
  reg                 is_im_s2;
  reg [DEPTH_REG-1:0] reg_d_s2;
  reg [DEPTH_REG-1:0] reg_a_s2;
  always @(posedge clk)
    begin
      op_s2 <= op_s1;
      is_im_s2 <= is_im_s1;
      reg_d_s2 <= reg_d_s1;
      reg_a_s2 <= reg_a_s1;
    end

  // stage 3 set dest reg addr
  reg [DEPTH_REG-1:0] reg_addr_d_s3;
  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          reg_addr_d_s3 <= ZERO;
        end
      else
        begin
          if (FALSE)
            begin
              // dummy for ifdef
              reg_addr_d_s3 <= reg_d_s2;
            end
`ifdef ENABLE_MVIL
          else if (op_s2 == I_MVIL)
            begin
              reg_addr_d_s3 <= SP_REG_MVIL;
            end
`endif
          else
            begin
              reg_addr_d_s3 <= reg_d_s2;
            end
        end
    end

  // stage 3 delay
  reg [4:0]           op_s3;
  reg                 is_im_s3;
  reg [DEPTH_REG-1:0] reg_a_s3;
  always @(posedge clk)
    begin
      op_s3 <= op_s2;
      is_im_s3 <= is_im_s2;
      reg_a_s3 <= reg_a_s2;
    end
`ifdef ENABLE_MVIL
  reg [DEPTH_REG-1:0] reg_d_s3;
  always @(posedge clk)
    begin
      reg_d_s3 <= reg_d_s2;
    end
`endif

  // stage 4 fetch reg_data
  wire [WIDTH_D-1:0] reg_data_a_s_s3;
  wire [WIDTH_D-1:0] reg_data_b_s_s3;
  reg [WIDTH_D-1:0]  reg_data_a_s4;
  reg [WIDTH_D-1:0]  reg_data_b_s4;
  always @(posedge clk)
    begin
      reg_data_a_s4 <= reg_data_a_s_s3;
      if (reset == TRUE)
        begin
          reg_data_b_s4 <= ZERO;
        end
      else
        begin
          if (FALSE)
            begin
              // dummy for ifdef
              reg_data_b_s4 <= reg_data_b_s_s3;
            end
`ifdef ENABLE_MVIL
          else if (op_s3 == I_MVIL)
            begin
              reg_data_b_s4 <= {reg_d_s3, reg_a_s3, is_im_s3};
            end
`endif
          else if (is_im_s3 == TRUE)
            begin
              reg_data_b_s4 <= $signed(reg_a_s3);
            end
          else
            begin
              reg_data_b_s4 <= reg_data_b_s_s3;
            end
        end
    end

  // stage 4 load address
  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          mem_d_r_addr <= ZERO;
        end
      else
        begin
          if (op_s3 == I_LD)
            begin
              mem_d_r_addr <= reg_data_b_s_s3;
            end
        end
    end

  // stage 4 delay
  reg [4:0]           op_s4;
  reg [DEPTH_REG-1:0] reg_addr_d_s4;
  always @(posedge clk)
    begin
      op_s4 <= op_s3;
      reg_addr_d_s4 <= reg_addr_d_s3;
    end

  // stage 5 execute store
  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          mem_d_w_addr <= ZERO;
          mem_d_w_data <= ZERO;
          mem_d_we <= FALSE;
        end
      else
        begin
          case (op_s4)
            I_ST:
              begin
                mem_d_w_addr <= reg_data_a_s4;
                mem_d_w_data <= reg_data_b_s4;
                mem_d_we <= TRUE;
              end
            default:
              begin
                mem_d_w_addr <= ZERO;
                mem_d_w_data <= ZERO;
                mem_d_we <= FALSE;
              end
          endcase
        end
    end

  // stage 5 calc BL address
  reg [DEPTH_I-1:0] bl_addr_s5;
  always @(posedge clk)
    begin
      bl_addr_s5 <= mem_i_r_addr + BL_OFFSET;
    end

  // stage 5 execute branch
  wire cond_true_s4;
  assign cond_true_s4 = (reg_data_a_s4 != ZERO) ? TRUE : FALSE;
  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          mem_i_r_addr <= ZERO;
        end
      else
        begin
          // branch
          if ((op_s4 == I_BA) || (op_s4 == I_BL) || ((op_s4 == I_BC) && (cond_true_s4)))
            begin
              mem_i_r_addr <= reg_data_b_s4;
            end
`ifdef ENABLE_WA
          else if (op_s4 == I_WA)
            begin
              mem_i_r_addr <= mem_i_r_addr - wait_count_m1;
            end
`endif
          else
            begin
              mem_i_r_addr <= mem_i_r_addr + ONE;
            end
        end
    end

  // stage 5 delay
  reg [4:0]           op_s5;
  reg [DEPTH_REG-1:0] reg_addr_d_s5;
  reg [WIDTH_D-1:0]   reg_data_a_s5;
  reg [WIDTH_D-1:0]   reg_data_b_s5;
  always @(posedge clk)
    begin
      op_s5 <= op_s4;
      reg_addr_d_s5 <= reg_addr_d_s4;
      reg_data_a_s5 <= reg_data_a_s4;
      reg_data_b_s5 <= reg_data_b_s4;
    end
`ifdef ENABLE_MVC
  reg                 cond_true_s5;
  always @(posedge clk)
    begin
      cond_true_s5 <= cond_true_s4;
    end
`endif

  // stage 6 compare
  reg flag_cnz_s6;
  reg flag_cnm_s6;
  always @(posedge clk)
    begin
      if (reg_data_b_s5 == ZERO)
        begin
          flag_cnz_s6 <= FALSE;
        end
      else
        begin
          flag_cnz_s6 <= TRUE;
        end

      if (reg_data_b_s5[WIDTH_D-1] == 1'b0)
        begin
          flag_cnm_s6 <= TRUE;
        end
      else
        begin
          flag_cnm_s6 <= FALSE;
        end
    end

  // stage 6 reg we
  reg reg_we_s6;
  always @(posedge clk)
    begin
      if ((op_s5[4:3] != 2'b00) || (op_s5 == I_BL)
`ifdef ENABLE_MVC
          || ((op_s5 == I_MVC) && (cond_true_s5 == TRUE))
`endif
          )
        begin
          reg_we_s6 <= TRUE;
        end
      else
        begin
          reg_we_s6 <= FALSE;
        end
    end

  // stage 6 delay
  reg [4:0]           op_s6;
  reg [DEPTH_REG-1:0] reg_addr_d_s6;
  reg [WIDTH_D-1:0]   reg_data_a_s6;
  reg [WIDTH_D-1:0]   reg_data_b_s6;
  reg [DEPTH_I-1:0]   bl_addr_s6;
  reg [WIDTH_D-1:0]   mem_d_r_data_s6;
  always @(posedge clk)
    begin
      op_s6 <= op_s5;
      reg_addr_d_s6 <= reg_addr_d_s5;
      reg_data_a_s6 <= reg_data_a_s5;
      reg_data_b_s6 <= reg_data_b_s5;
      bl_addr_s6 <= bl_addr_s5;
      mem_d_r_data_s6 <= mem_d_r_data;
    end

`ifdef FULL_PIPELINED_ALU
  // stage 6 pre-execute
  reg [WIDTH_D-1:0]   reg_data_add_s6;
  reg [WIDTH_D-1:0]   reg_data_sub_s6;
  reg [WIDTH_D-1:0]   reg_data_and_s6;
  reg [WIDTH_D-1:0]   reg_data_or_s6;
  reg [WIDTH_D-1:0]   reg_data_xor_s6;
  always @(posedge clk)
    begin
      reg_data_add_s6 <= reg_data_a_s5 + reg_data_b_s5;
      reg_data_sub_s6 <= reg_data_a_s5 - reg_data_b_s5;
      reg_data_and_s6 <= reg_data_a_s5 & reg_data_b_s5;
      reg_data_or_s6  <= reg_data_a_s5 | reg_data_b_s5;
      reg_data_xor_s6 <= reg_data_a_s5 ^ reg_data_b_s5;
    end
`endif

  // stage 7 execute
  reg [WIDTH_D-1:0]   reg_data_w_s7;
  always @(posedge clk)
    begin
      case (op_s6)
`ifdef FULL_PIPELINED_ALU
        I_ADD:
          begin
            reg_data_w_s7 <= reg_data_add_s6;
          end
        I_SUB:
          begin
            reg_data_w_s7 <= reg_data_sub_s6;
          end
        I_AND:
          begin
            reg_data_w_s7 <= reg_data_and_s6;
          end
        I_OR:
          begin
            reg_data_w_s7 <= reg_data_or_s6;
          end
        I_XOR:
          begin
            reg_data_w_s7 <= reg_data_xor_s6;
          end
`else
        I_ADD:
          begin
            reg_data_w_s7 <= reg_data_a_s6 + reg_data_b_s6;
          end
        I_SUB:
          begin
            reg_data_w_s7 <= reg_data_a_s6 - reg_data_b_s6;
          end
        I_AND:
          begin
            reg_data_w_s7 <= reg_data_a_s6 & reg_data_b_s6;
          end
        I_OR:
          begin
            reg_data_w_s7 <= reg_data_a_s6 | reg_data_b_s6;
          end
        I_XOR:
          begin
            reg_data_w_s7 <= reg_data_a_s6 ^ reg_data_b_s6;
          end
`endif
        I_SR:
          begin
            reg_data_w_s7 <= sr_result_s6;
          end
        I_SL:
          begin
            reg_data_w_s7 <= sl_result_s6;
          end
        I_SRA:
          begin
            reg_data_w_s7 <= sra_result_s6;
          end
        I_CNZ:
          begin
            reg_data_w_s7 <= {WIDTH_D{flag_cnz_s6}};
          end
        I_CNM:
          begin
            reg_data_w_s7 <= {WIDTH_D{flag_cnm_s6}};
          end
        I_BL:
          begin
            reg_data_w_s7 <= bl_addr_s6;
          end
`ifdef ENABLE_MUL
        I_MUL:
          begin
            reg_data_w_s7 <= mul_result_s6;
          end
`endif
        I_LD:
          begin
            reg_data_w_s7 <= mem_d_r_data_s6;
          end
        // I_MV, I_MVIL
        default:
          begin
            reg_data_w_s7 <= reg_data_b_s6;
          end
      endcase
    end

  // stage 7 delay
  reg [DEPTH_REG-1:0] reg_addr_d_s7;
  reg reg_we_s7;
  always @(posedge clk)
    begin
      reg_addr_d_s7 <= reg_addr_d_s6;
      reg_we_s7 <= reg_we_s6;
    end

  r2w1_port_ram
    #(
      .DATA_WIDTH (WIDTH_D),
      .ADDR_WIDTH (DEPTH_REG)
      )
  reg_file
    (
     .clk (clk),
`ifdef ENABLE_MVC
     .addr_r_a (reg_addr_a_s2),
     .addr_r_b (reg_addr_b_s2),
`else
     .addr_r_a (reg_d_s2),
     .addr_r_b (reg_a_s2),
`endif
     .addr_w (reg_addr_d_s7),
     .data_in (reg_data_w_s7),
     .we (reg_we_s7),
     .data_out_a (reg_data_a_s_s3),
     .data_out_b (reg_data_b_s_s3)
     );

`ifdef ENABLE_MUL
  wire [WIDTH_D-1:0] mul_result_s6;
  delayed_mul
    #(
      .WIDTH_D (WIDTH_D)
      )
  delayed_mul_0
    (
     .clk (clk),
     .a (reg_data_a_s4),
     .b (reg_data_b_s4),
     .out (mul_result_s6)
     );
`endif

`ifdef ENABLE_MULTI_BIT_SHIFT
  wire [WIDTH_D-1:0] sr_result_s6;
  wire [WIDTH_D-1:0] sl_result_s6;
  wire [WIDTH_D-1:0] sra_result_s6;

  delayed_sr
    #(
      .WIDTH_D (WIDTH_D),
      .SHIFT_BITS (SHIFT_BITS)
      )
  delayed_sr_0
    (
     .clk (clk),
     .a (reg_data_a_s4),
     .b (reg_data_b_s4[SHIFT_BITS-1:0]),
     .out (sr_result_s6)
     );

  delayed_sl
    #(
      .WIDTH_D (WIDTH_D),
      .SHIFT_BITS (SHIFT_BITS)
      )
  delayed_sl_0
    (
     .clk (clk),
     .a (reg_data_a_s4),
     .b (reg_data_b_s4[SHIFT_BITS-1:0]),
     .out (sl_result_s6)
     );

  delayed_sra
    #(
      .WIDTH_D (WIDTH_D),
      .SHIFT_BITS (SHIFT_BITS)
      )
  delayed_sra_0
    (
     .clk (clk),
     .a (reg_data_a_s4),
     .b (reg_data_b_s4[SHIFT_BITS-1:0]),
     .out (sra_result_s6)
     );
`else
  reg [WIDTH_D-1:0] sr_result_s6;
  reg [WIDTH_D-1:0] sl_result_s6;
  reg [WIDTH_D-1:0] sra_result_s6;
  always @(posedge clk)
    begin
      sr_result_s6 <= {1'b0, reg_data_a_s5[WIDTH_D-1:1]};
      sl_result_s6 <= {reg_data_a_s5[WIDTH_D-2:0], 1'b0};
      sra_result_s6 <= {reg_data_a_s5[WIDTH_D-1], reg_data_a_s5[WIDTH_D-1:1]};
    end
`endif

endmodule

module delayed_mul
  #(
    parameter WIDTH_D = 16
    )
  (
   input                           clk,
   input signed [WIDTH_D-1:0]      a,
   input signed [WIDTH_D-1:0]      b,
   output reg signed [WIDTH_D-1:0] out
   );

  reg signed [WIDTH_D-1:0]         sa;
  reg signed [WIDTH_D-1:0]         sb;

  always @(posedge clk)
    begin
      sa <= a;
      sb <= b;
      out <= sa * sb;
    end
endmodule

module delayed_sr
  #(
    parameter WIDTH_D = 16,
    parameter SHIFT_BITS = 4
    )
  (
   input                    clk,
   input [WIDTH_D-1:0]      a,
   input [SHIFT_BITS-1:0]   b,
   output reg [WIDTH_D-1:0] out
   );

  reg [WIDTH_D-1:0]         sa;
  reg [SHIFT_BITS-1:0]      sb;

  always @(posedge clk)
    begin
      sa <= a;
      sb <= b;
      out <= sa >> sb;
    end
endmodule

module delayed_sl
  #(
    parameter WIDTH_D = 16,
    parameter SHIFT_BITS = 4
    )
  (
   input                    clk,
   input [WIDTH_D-1:0]      a,
   input [SHIFT_BITS-1:0]   b,
   output reg [WIDTH_D-1:0] out
   );

  reg [WIDTH_D-1:0]         sa;
  reg [SHIFT_BITS-1:0]      sb;

  always @(posedge clk)
    begin
      sa <= a;
      sb <= b;
      out <= sa << sb;
    end
endmodule

module delayed_sra
  #(
    parameter WIDTH_D = 16,
    parameter SHIFT_BITS = 4
    )
  (
   input                    clk,
   input [WIDTH_D-1:0]      a,
   input [SHIFT_BITS-1:0]   b,
   output reg [WIDTH_D-1:0] out
   );

  reg signed [WIDTH_D-1:0]  sa;
  reg [SHIFT_BITS-1:0]      sb;

  always @(posedge clk)
    begin
      sa <= a;
      sb <= b;
      out <= sa >>> sb;
    end
endmodule
