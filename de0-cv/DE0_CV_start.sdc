create_clock -period "50.0 MHz" [get_ports CLOCK_50]

derive_pll_clocks

derive_clock_uncertainty

set_clock_groups -asynchronous -group CLOCK_50 -group simple_pll_0002_0|altera_pll_i|general[0].gpll~PLL_OUTPUT_COUNTER|divclk

set_false_path -from [get_ports {RESET_N}] -to [get_keepers {resetpll1}]
set_false_path -from [get_keepers {mini16_soc:mini16_soc_0|led[0]}] -to [get_ports {LEDR[0]}]
set_false_path -from [get_keepers {mini16_soc:mini16_soc_0|led[1]}] -to [get_ports {LEDR[1]}]
set_false_path -from [get_keepers {mini16_soc:mini16_soc_0|led[2]}] -to [get_ports {LEDR[2]}]
set_false_path -from [get_keepers {mini16_soc:mini16_soc_0|led[3]}] -to [get_ports {LEDR[3]}]
set_false_path -from [get_keepers {mini16_soc:mini16_soc_0|led[4]}] -to [get_ports {LEDR[4]}]
set_false_path -from [get_keepers {mini16_soc:mini16_soc_0|led[5]}] -to [get_ports {LEDR[5]}]
set_false_path -from [get_keepers {mini16_soc:mini16_soc_0|led[6]}] -to [get_ports {LEDR[6]}]
set_false_path -from [get_keepers {mini16_soc:mini16_soc_0|led[7]}] -to [get_ports {LEDR[7]}]
set_false_path -from [get_keepers {mini16_soc:mini16_soc_0|led[8]}] -to [get_ports {LEDR[8]}]
set_false_path -from [get_keepers {mini16_soc:mini16_soc_0|led[9]}] -to [get_ports {LEDR[9]}]
