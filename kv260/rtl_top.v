/*
  Copyright (c) 2024, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

module rtl_top
  (
   input wire  clk,
`ifdef USE_UART
   input wire  uart_rxd,
   output wire uart_txd,
`endif
   input wire  resetn,
   output wire led
   );

  localparam UART_CLK_HZ = 510000000;
  localparam UART_SCLK_HZ = 115200;
  localparam ZERO = 1'd0;
  localparam ONE = 1'd1;
  localparam TRUE = 1'b1;
  localparam FALSE = 1'b0;

  wire [15:0] led_soc;
  assign led = led_soc[0];

  // reset
  reg reset;
  reg reset1;
  reg reset2;

  always @(posedge clk)
    begin
      reset1 <= resetn;
      reset2 <= reset1;
      reset <= ~reset2;
    end

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
     .led (led_soc)
     );

endmodule
