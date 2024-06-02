# KV260 PMOD J2, KR260 PMOD#1 J2
# PMOD                   top    bottom
#1, HDA11, som240_1_a17, H12    #2, HDA15,    som240_1_b20, B10
#3, HDA12, som240_1_d20, E10    #4, HDA16_CC, som240_1_b21, E12
#5, HDA13, som240_1_d21, D10    #6, HDA17,    som240_1_b22, D11
#7, HDA14, som240_1_d22, C11    #8, HDA18,    som240_1_c22, B11
#9,  GND                        #10, GND
#11, 3V3                        #12, 3V3

set_property PACKAGE_PIN H12 [get_ports led_0]
set_property IOSTANDARD LVCMOS33 [get_ports led_0]
set_property DRIVE 16 [get_ports led_0]
set_property SLEW SLOW [get_ports led_0]

set_property PACKAGE_PIN E10 [get_ports uart_txd_0]
set_property IOSTANDARD LVCMOS33 [get_ports uart_txd_0]
set_property PACKAGE_PIN D10 [get_ports uart_rxd_0]
set_property IOSTANDARD LVCMOS33 [get_ports uart_rxd_0]
