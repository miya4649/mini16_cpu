set rtl_files {top.v topinclude.v ../default_master_code_mem.v ../default_master_data_mem.v ../dual_clk_ram.v ../mini16_cpu.v ../mini16_soc.v ../r2w1_port_ram.v ../rw_port_ram.v ../shift_register_vector.v ../uart.v ../uart_io.v}
set pin_xdc_file {pins.xdc}
set timing_xdc_file {timings.xdc}
set project_name project_1
set project_dir project_1
set part_name xcku3p-ffva676-3-e
set top_module top

# create project
create_project -name $project_name -force -dir $project_dir -part $part_name

add_files -fileset constrs_1 -norecurse $pin_xdc_file

add_files -fileset constrs_1 -norecurse $timing_xdc_file
set_property used_in_synthesis false [get_files $timing_xdc_file]

add_files -fileset sources_1 -norecurse $rtl_files

set_property is_global_include true [get_files topinclude.v]

create_ip -name clk_wiz -vendor xilinx.com -library ip -version 6.0 -module_name clk_wiz_0

set_property -dict [list \
CONFIG.CLKOUT1_REQUESTED_OUT_FREQ {710} \
CONFIG.PRIM_SOURCE {Differential_clock_capable_pin} \
] [get_ips clk_wiz_0]

set_property top $top_module [current_fileset]

update_compile_order -fileset sources_1
