/*
  Copyright (c) 2015, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/*
 ver. 2024/04/21
 write delay: immediately
 read delay: 2 clock cycle
*/

module dual_clk_ram
  #(
    parameter DATA_WIDTH = 8,
    parameter ADDR_WIDTH = 12,
    parameter RAM_TYPE = "auto"
    )
  (
   input wire [(DATA_WIDTH-1):0] data_in,
   input wire [(ADDR_WIDTH-1):0] read_addr,
   input wire [(ADDR_WIDTH-1):0] write_addr,
   input wire                    we,
   input wire                    read_clock,
   input wire                    write_clock,
   output reg [(DATA_WIDTH-1):0] data_out
   );

  reg [(ADDR_WIDTH-1):0]         read_addr_reg;
  always @(posedge read_clock)
    begin
      read_addr_reg <= read_addr;
    end

  generate
    if (RAM_TYPE == "xi_distributed")
      begin: gen
        (* ram_style = "distributed" *) reg [DATA_WIDTH-1:0] ram [0:(1 << ADDR_WIDTH)-1];
        always @(posedge read_clock)
          begin
            data_out <= ram[read_addr_reg];
          end
        always @(posedge write_clock)
          begin
            if (we)
              begin
                ram[write_addr] <= data_in;
              end
          end
      end
    else if (RAM_TYPE == "xi_block")
      begin: gen
        (* ram_style = "block" *) reg [DATA_WIDTH-1:0] ram [0:(1 << ADDR_WIDTH)-1];
        always @(posedge read_clock)
          begin
            data_out <= ram[read_addr_reg];
          end
        always @(posedge write_clock)
          begin
            if (we)
              begin
                ram[write_addr] <= data_in;
              end
          end
      end
    else if (RAM_TYPE == "xi_register")
      begin: gen
        (* ram_style = "register" *) reg [DATA_WIDTH-1:0] ram [0:(1 << ADDR_WIDTH)-1];
        always @(posedge read_clock)
          begin
            data_out <= ram[read_addr_reg];
          end
        always @(posedge write_clock)
          begin
            if (we)
              begin
                ram[write_addr] <= data_in;
              end
          end
      end
    else if (RAM_TYPE == "xi_ultra")
      begin: gen
        (* ram_style = "ultra" *) reg [DATA_WIDTH-1:0] ram [0:(1 << ADDR_WIDTH)-1];
        always @(posedge read_clock)
          begin
            data_out <= ram[read_addr_reg];
          end
        always @(posedge write_clock)
          begin
            if (we)
              begin
                ram[write_addr] <= data_in;
              end
          end
      end
    else if (RAM_TYPE == "al_logic")
      begin: gen
        (* ramstyle = "logic" *) reg [DATA_WIDTH-1:0] ram [0:(1 << ADDR_WIDTH)-1];
        always @(posedge read_clock)
          begin
            data_out <= ram[read_addr_reg];
          end
        always @(posedge write_clock)
          begin
            if (we)
              begin
                ram[write_addr] <= data_in;
              end
          end
      end
    else if (RAM_TYPE == "al_mlab")
      begin: gen
        (* ramstyle = "MLAB" *) reg [DATA_WIDTH-1:0] ram [0:(1 << ADDR_WIDTH)-1];
        always @(posedge read_clock)
          begin
            data_out <= ram[read_addr_reg];
          end
        always @(posedge write_clock)
          begin
            if (we)
              begin
                ram[write_addr] <= data_in;
              end
          end
      end
    else
      begin: gen
        reg [DATA_WIDTH-1:0] ram [0:(1 << ADDR_WIDTH)-1];
        always @(posedge read_clock)
          begin
            data_out <= ram[read_addr_reg];
          end
        always @(posedge write_clock)
          begin
            if (we)
              begin
                ram[write_addr] <= data_in;
              end
          end
      end
  endgenerate

endmodule
