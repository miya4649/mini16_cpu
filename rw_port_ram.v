/*
  Copyright (c) 2015-2016 miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

module rw_port_ram
  #(
    parameter DATA_WIDTH=8,
    parameter ADDR_WIDTH=12
    )
  (
   input                         clk,
   input [(ADDR_WIDTH-1):0]      addr_r,
   input [(ADDR_WIDTH-1):0]      addr_w,
   input [(DATA_WIDTH-1):0]      data_in,
   input                         we,
   output reg [(DATA_WIDTH-1):0] data_out
   );

  reg [DATA_WIDTH-1:0]           ram [0:(1 << ADDR_WIDTH)-1];

  always @(posedge clk)
    begin
      data_out <= ram[addr_r];
      if (we)
        begin
          ram[addr_w] <= data_in;
        end
    end

endmodule
