EXES=launcher reciever sendword
CFLAGS=-O3 -Wall
SRCS=$(wildcard *.c)
HEADERS=$(wildcard *.h)
OBJS=$(SRCS:.c=.o)
LIBSRCS=$(wildcard *lib.c)
LIBOBJS=$(LIBSRCS:.c=.o)
UART_DEVICE?=/dev/ttyUSB0

all: $(EXES)

run: $(EXES)
	./launcher ../default_master_code.bin ../default_master_data.bin $(UART_DEVICE)

$(EXES):%:%.o $(LIBOBJS)
	gcc $(CFLAGS) -o $@ $(LIBOBJS) $@.o

$(OBJS):%.o:%.c $(HEADERS)
	gcc -c $(CFLAGS) -o $@ $<

clean:
	rm -f $(EXES) $(OBJS)
