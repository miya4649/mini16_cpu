SRCS=$(wildcard *.java)
OBJS=$(SRCS:.java=.class)
BIN=$(wildcard ../*_code.bin ../*_data.bin ../*_code_mem.v ../*_data_mem.v)

all: $(OBJS)
	java AsmTop

kv260: $(OBJS)
	java AsmTop

$(OBJS):%.class:%.java
	javac $<

clean:
	rm -f *.class
	rm -f $(BIN)
