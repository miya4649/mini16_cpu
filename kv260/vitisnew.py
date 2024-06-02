#!/usr/bin/env python3
import vitis, json, os, shutil

project_name = 'project_1'
platform_name = project_name + '_pf'
app_name = project_name + '_app'
domain_name = 'standalone_psu_cortexa53_0'
xsa_path = project_name + '/design_1_wrapper.xsa'
vitis_src_path = 'vitis_src'
workspace_path = 'vitis_workspace'
launch_json_path = workspace_path + '/' + app_name + '/_ide/.theia/launch.json'
use_template = True
template_name = 'hello_world'

# Valid templates are: ['memory_tests', 'peripheral_tests', 'lwip_tcp_perf_server', 'dhrystone', 'zynqmp_fsbl', 'lwip_echo_server', 'empty_application', 'lwip_udp_perf_client', 'lwip_tcp_perf_client', 'zynqmp_dram_test', 'hello_world', 'lwip_udp_perf_server']

if use_template == False:
    template_name = 'empty_application'

if os.path.exists(workspace_path):
    shutil.rmtree(workspace_path)

try:
    os.mkdir(workspace_path)
except:
    pass

client = vitis.create_client()
client.set_workspace(path = workspace_path)

# create platform
platform = client.create_platform_component(name = platform_name, hw = xsa_path, os = 'standalone', no_boot_bsp = True)

# add domain
domain = platform.add_domain(name = domain_name, cpu = 'psu_cortexa53_0', support_app = template_name)
platform.build()

# create app
platform_xpfm = platform.project_location + '/export/' + platform_name + '/' + platform_name + '.xpfm'
app = client.create_app_component(name = app_name, platform = platform_xpfm, domain = domain_name, template = template_name)

if use_template == False:
    app.import_files(from_loc = vitis_src_path, dest_dir_in_cmp = 'src')

app.build()

# modify launch settings
fr = open(launch_json_path, 'r')
d0 = json.load(fr)
fr.close

d1 = d0['configurations'][0]['targetSetup']['zuInitialization']
d1['isFsbl'] = False
d1['usingPsuInit']['plPowerup'] = True

fw = open(launch_json_path, 'w')
json.dump(d0, fw, indent = '\t')
fw.write('\n')
fw.close
