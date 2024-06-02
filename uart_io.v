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

module uart_io
  #(
    parameter CLK_HZ = 50000000,
    parameter SCLK_HZ = 115200
    )
  (
   input             clk,
   input             reset,
   input             uart_rxd,
   input [7:0]       tx_data,
   input             tx_we,
   output            uart_txd,
   output            uart_busy,
   output reg [31:0] rx_addr,
   output reg [31:0] rx_data,
   output reg        rx_we
   );

  localparam TRUE = 1'b1;
  localparam FALSE = 1'b0;
  localparam ONE = 1'd1;
  localparam ZERO = 1'd0;

  localparam UART_WIDTH = 8;
  reg                   uart_start;
  reg [UART_WIDTH-1:0]  uart_data_tx;
  wire                  uart_re;
  reg                   io_we_p1;

  localparam IO_UART_IF_START_BYTE = 8'b10101010;
  localparam IO_UART_IF_END_BYTE = 8'b01010101;
  wire [UART_WIDTH-1:0] uart_data_rx;
  reg [7:0]             rx_buf [0:9];
  reg [3:0]             rx_buf_state;
  reg                   uart_re_d1;
  wire                  uart_re_posedge;

  /* uart_io spec
   start byte [0]: 10101010
   addr [4][3][2][1]
   data [8][7][6][5]
   end byte [9]: 01010101
   */

  // uart read packet
  always @(posedge clk)
    begin
      uart_re_d1 <= uart_re;
    end
  assign uart_re_posedge = ((uart_re == TRUE) && (uart_re_d1 == FALSE)) ? TRUE : FALSE;

  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          rx_buf_state <= 4'd0;
          io_we_p1 <= FALSE;
        end
      else
        begin
          if (uart_re_posedge == TRUE)
            begin
              rx_buf[rx_buf_state] <= uart_data_rx;
              case (rx_buf_state)
                4'd0:
                  begin
                    // check start byte
                    if (uart_data_rx == IO_UART_IF_START_BYTE)
                      begin
                        rx_buf_state <= rx_buf_state + ONE;
                        io_we_p1 <= FALSE;
                      end
                    else
                      begin
                        rx_buf_state <= 4'd0;
                        io_we_p1 <= FALSE;
                      end
                  end
                4'd9:
                  begin
                    // check end byte
                    rx_buf_state <= 4'd0;
                    if (uart_data_rx == IO_UART_IF_END_BYTE)
                      begin
                        io_we_p1 <= TRUE;
                      end
                    else
                      begin
                        io_we_p1 <= FALSE;
                      end
                  end
                default:
                  begin
                    rx_buf_state <= rx_buf_state + ONE;
                    io_we_p1 <= FALSE;
                  end
              endcase
            end
          else
            begin
              io_we_p1 <= FALSE;
            end
        end
    end

  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          rx_we <= FALSE;
        end
      else
        begin
          rx_we <= io_we_p1;
        end
    end

  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          rx_addr <= ZERO;
          rx_data <= ZERO;
        end
      else
        begin
          if (io_we_p1 == TRUE)
            begin
              rx_addr <= {rx_buf[4], rx_buf[3], rx_buf[2], rx_buf[1]};
              rx_data <= {rx_buf[8], rx_buf[7], rx_buf[6], rx_buf[5]};
            end
        end
    end

  // uart send byte
  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          uart_data_tx <= ZERO;
          uart_start <= FALSE;
        end
      else
        begin
          if ((tx_we == TRUE) && (uart_busy == FALSE))
            begin
              uart_data_tx <= tx_data;
              uart_start <= TRUE;
            end
          else
            begin
              uart_data_tx <= ZERO;
              uart_start <= FALSE;
            end
        end
    end

  uart
    #(
      .CLK_HZ (CLK_HZ),
      .SCLK_HZ (SCLK_HZ),
      .WIDTH (UART_WIDTH)
      )
  uart_0
    (
     .clk (clk),
     .reset (reset),
     .rxd (uart_rxd),
     .start (uart_start),
     .data_tx (uart_data_tx),
     .txd (uart_txd),
     .busy (uart_busy),
     .re (uart_re),
     .data_rx (uart_data_rx)
     );

endmodule
