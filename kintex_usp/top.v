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

module top
  (
   input        CLK_P,
   input        CLK_N,
   input  [1:0] BTN,
`ifdef USE_UART
   output       UART_TXD,
   input        UART_RXD,
`endif
   output [1:0] LED
   );

  localparam UART_CLK_HZ = 710000000;
  localparam UART_SCLK_HZ = 115200;

  wire [15:0]   led;
  assign LED = led;

`ifdef USE_UART
  // uart
  wire uart_txd;
  wire uart_rxd;
  assign UART_TXD = uart_txd;
  assign uart_rxd = UART_RXD;
`endif

  // generate reset signal (push button 1)
  reg  reset;
  reg  reset1;

  always @(posedge clk)
    begin
      reset1 <= ~pll_locked;
      reset <= reset1;
    end

  // pll
  wire clk;
  wire pll_locked;

  clk_wiz_0 clk_wiz_0_inst_0
   (
    .clk_out1 (clk),
    .reset (~BTN[0]),
    .locked (pll_locked),
    .clk_in1_p (CLK_P),
    .clk_in1_n (CLK_N)
   );

  mini16_soc
    #(
      .UART_CLK_HZ (UART_CLK_HZ),
      .UART_SCLK_HZ (UART_SCLK_HZ)
      )
  mini16_soc_0
    (
`ifdef USE_UART
     .uart_rxd (uart_rxd),
     .uart_txd (uart_txd),
`endif
     .clk (clk),
     .reset (reset),
     .led (led)
     );

endmodule
