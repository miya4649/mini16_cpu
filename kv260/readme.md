# Project files for AMD (Xilinx) Kria KV260, KR260

## Preparation of peripheral circuits

Prepare a device that can use a 3.3V UART interface, such as a RaspberryPi or FTDI TTL-232R-3V3, LED, 330 ohm resistor, and connect them to the PMOD J2 port on the board as follows.

(The pin number is the number indicated on the board and on the schematic.)

PMOD_J2_Pin1 --- resistor 330 ohm --- (+)LED(-) --- PMOD_J2_Pin9(GND)

PMOD_J2_Pin3 --- UART RX

PMOD_J2_Pin5 --- UART TX

PMOD_J2_Pin9 --- UART GND

Connect the board to the host PC with a USB cable, connect the KV260 to another monitor with an HDMI cable, and turn on the power.

## Usage

By default, a project is generated for KV260; for KR260, replace "set board_type kv260" in vivado.tcl with "set board_type kr260".

In a Linux terminal (bash),

$ source <VITIS INSTALLATION PATH>/settings64.sh

(e.g. $ source /opt/Xilinx/Vitis/2023.2/settings64.sh )

$ cd mini16_cpu/kv260

$ make

Start the Vitis Unified IDE,

"File": "Open Workspace",

Select the path mini16_cpu/kv260/vitis_workspace and OK.

"VIEW": "Flow" to show,

"FLOW": "Component", select "Project_1_app",

"FLOW": "Run" to run it.

### Transfer and execute software

In a terminal (bash) on a Linux machine connected via UART,

$ cd mini16_cpu/kv260

$ export UART_DEVICE=/dev/ttyUSB0

(The device name may change depending on the situation; specify the device name of the UART interface.)

$ make run

(Launch default LED Flasher program)

There are some example programs in mini16_cpu/asm/Examples.java (LED flasher, UART counter, UART HelloWorld.)
