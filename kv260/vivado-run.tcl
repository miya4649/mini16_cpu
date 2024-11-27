set project_name project_1
set project_dir $project_name
set project_file $project_dir/$project_name.xpr
set design_name design_1
set xsa_file $project_dir/${design_name}_wrapper.xsa

if {[catch {current_project}]} {
    open_project $project_file
} else {
    puts "The project is already opened."
}

reset_run synth_1
reset_run impl_1

launch_runs synth_1
wait_on_run synth_1

if {[string match "2024*" [version -short]]} {
    create_run impl_2 -parent_run synth_1 -flow {Vivado Implementation 2024} -strategy Performance_Explore
    current_run [get_runs impl_2]
    launch_runs impl_2 -to_step write_bitstream
    wait_on_run impl_2
    update_compile_order -fileset sources_1
} else {
    launch_runs impl_1 -to_step write_bitstream
    wait_on_run impl_1
}

write_hw_platform -fixed -force -include_bit -file $xsa_file
validate_hw_platform -verbose $xsa_file
