SRCS=$(wildcard *.java)
OBJS=$(SRCS:.java=.class)

all: $(OBJS)
	java AsmTop

$(OBJS):%.class:%.java
	javac $<

clean:
	rm -f *.class
