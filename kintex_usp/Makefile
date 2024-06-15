PROJECT = project_1
VIVADO = $(XILINX_VIVADO)/bin/vivado
RM = rm -rf

all: asm vivado vivado-run

asm:
	make -C ../asm

vivado:
	$(VIVADO) -mode batch -notrace -source vivado.tcl

vivado-run:
	$(VIVADO) -mode batch -notrace -source vivado-run.tcl

run:
	make -C ../tools run

clean:
	$(RM) $(PROJECT) vivado.log vivado*.jou .Xil *dynamic* *.log *.xpe .gitignore .lock .peers.ini
	rm -rf $(PROJECT).cache $(PROJECT).gen $(PROJECT).hw $(PROJECT).ip_user_files $(PROJECT).runs $(PROJECT).sim
	make -C ../tools clean
	make -C ../asm clean
