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

`timescale 1ns / 1ps

module testbench;

  localparam STEP  = 20; // 20 ns: 50MHz
  localparam TICKS = 10000;

  localparam TRUE = 1'b1;
  localparam FALSE = 1'b0;
  localparam ONE = 1'd1;
  localparam ZERO = 1'd0;
  localparam DEPTH_REG = 5;

  reg clk;
  reg reset;
  wire [9:0] led;

  integer            i;
  initial
    begin
      $dumpfile("wave.vcd");
      $dumpvars(10, testbench);
      for (i = 0; i < (1 << DEPTH_REG); i = i + 1)
        begin
          $dumpvars(0, testbench.mini16_soc_0.mini16_cpu_0.reg_file.rw_port_ram_a.ram[i]);
          $dumpvars(0, testbench.mini16_soc_0.mem_d.ram[i]);
        end
      $monitor("time: %d reset: %d led: %d", $time, reset, led);
    end

  // generate clk
  initial
    begin
      clk = 1'b1;
      forever
        begin
          #(STEP / 2) clk = ~clk;
        end
    end

  // generate reset signal
  initial
    begin
      reset = 1'b0;
      repeat (10) @(posedge clk) reset <= 1'b1;
      @(posedge clk) reset <= 1'b0;
    end

  // stop simulation after TICKS
  initial
    begin
      repeat (TICKS) @(posedge clk);
      $finish;
    end

  mini16_soc mini16_soc_0
    (
     .clk (clk),
     .reset (reset),
     .led (led)
     );

endmodule
