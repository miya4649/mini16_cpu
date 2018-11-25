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

module mini16_soc
  (
   input            clk,
   input            reset,
   output reg [15:0] led
   );

  localparam WIDTH_I = 16;
  localparam WIDTH_D = 16;
  localparam DEPTH_I = 8;
  localparam DEPTH_D = 8;
  localparam DEPTH_V = 9;
  localparam DEPTH_REG = 5;
  localparam TRUE = 1'b1;
  localparam FALSE = 1'b0;
  localparam ONE = 1'd1;
  localparam ZERO = 1'd0;
  localparam FFFF = {WIDTH_D{1'b1}};

  always @(posedge clk)
    begin
      if (led_we == TRUE)
        begin
          led <= mem_d_w_data;
        end
    end

  wire [DEPTH_I-1:0] cpu_i_r_addr;
  wire [WIDTH_I-1:0] cpu_i_r_data;
  wire [DEPTH_V-1:0] cpu_d_r_addr;
  wire [WIDTH_D-1:0] cpu_d_r_data;
  wire [DEPTH_V-1:0] cpu_d_w_addr;
  wire [WIDTH_D-1:0] cpu_d_w_data;
  wire               cpu_d_we;

  // mmio
  wire [DEPTH_D-1:0] mem_d_r_addr;
  reg [DEPTH_D-1:0] mem_d_w_addr;
  reg [WIDTH_D-1:0] mem_d_w_data;
  reg               mem_d_we;
  reg               led_we;
  assign mem_d_r_addr = cpu_d_r_addr[DEPTH_D-1:0];
  always @(posedge clk)
    begin
      mem_d_w_addr <= cpu_d_w_addr[DEPTH_D-1:0];
      mem_d_w_data <= cpu_d_w_data;
      if (cpu_d_we == TRUE)
        begin
          if (cpu_d_w_addr[DEPTH_V-1] == FALSE)
            begin
              mem_d_we <= TRUE;
              led_we <= FALSE;
            end
          else
            begin
              mem_d_we <= FALSE;
              led_we <= TRUE;
            end
        end
      else
        begin
          mem_d_we <= FALSE;
          led_we <= FALSE;
        end
    end

  mini16_cpu
    #(
      .WIDTH_I (WIDTH_I),
      .WIDTH_D (WIDTH_D),
      .DEPTH_I (DEPTH_I),
      .DEPTH_D (DEPTH_V),
      .DEPTH_REG (DEPTH_REG)
      )
  mini16_cpu_0
    (
     .clk (clk),
     .reset (reset),
     .mem_i_r_addr (cpu_i_r_addr),
     .mem_i_r_data (cpu_i_r_data),
     .mem_d_r_addr (cpu_d_r_addr),
     .mem_d_r_data (cpu_d_r_data),
     .mem_d_w_addr (cpu_d_w_addr),
     .mem_d_w_data (cpu_d_w_data),
     .mem_d_we (cpu_d_we)
     );

  default_code_mem
    #(
      .DATA_WIDTH (WIDTH_I),
      .ADDR_WIDTH (DEPTH_I)
      )
  mem_i
    (
     .clk (clk),
     .addr_r (cpu_i_r_addr),
     .addr_w (1'b0),
     .data_in (1'b0),
     .we (1'b0),
     .data_out (cpu_i_r_data)
     );

  default_data_mem
    #(
      .DATA_WIDTH (WIDTH_D),
      .ADDR_WIDTH (DEPTH_D)
      )
  mem_d
    (
     .clk (clk),
     .addr_r (mem_d_r_addr),
     .addr_w (mem_d_w_addr),
     .data_in (mem_d_w_data),
     .we (mem_d_we),
     .data_out (cpu_d_r_data)
     );

endmodule
