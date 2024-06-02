PROJECT = project_1
VITIS_WORKSPACE = vitis_workspace
VIVADO = $(XILINX_VIVADO)/bin/vivado
VITIS = $(XILINX_VITIS)/bin/vitis
RM = rm -rf

all: asm tool vivado vivado-run vitis

asm:
	make -C ../asm

tool:
	make -C ../tools

vivado:
	$(VIVADO) -mode batch -notrace -source vivado.tcl

vivado-run:
	$(VIVADO) -mode batch -notrace -source vivado-run.tcl

vitis:
	$(VITIS) -source vitisnew.py

run:
	make -C ../tools run

clean:
	$(RM) $(PROJECT) $(VITIS_WORKSPACE) vivado.log vivado*.jou .Xil *dynamic* *.log *.xpe .gitignore .lock .peers.ini
	make -C ../tools clean
	make -C ../asm clean
