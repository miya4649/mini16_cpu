EXES=main
VCDS=$(wildcard *.vcd)
WARN=-Wanachronisms -Wimplicit -Wmacro-replacement -Wportbind -Wselect-range -Wsensitivity-entire-array

all: $(EXES)

run: $(EXES)
	vvp $(EXES)

$(EXES): program testbench.v ../*.v
	iverilog -g2012 $(WARN) -o $(EXES) testbench.v ../*.v

program:
	make -C ../asm

clean:
	make -C ../asm clean
	rm -f $(EXES) $(EXES16) $(VCDS)
