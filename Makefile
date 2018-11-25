ROMS=$(wildcard *_code_mem.v *_data_mem.v)

all: program

program:
	make -C asm

sim:
	make -C testbench run

clean:
	make -C asm clean
	make -C testbench clean
#	rm -f $(ROMS)
