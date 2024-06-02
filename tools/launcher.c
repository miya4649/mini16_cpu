/*
  Copyright (c) 2017, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#include <fcntl.h>
#include <termios.h>
#include <stdlib.h>
#include <strings.h>
#include <unistd.h>
#include <stdio.h>
#include <stdint.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "uartlib.h"

//#define UART_DEVICE "/dev/ttyAMA0"
#define UART_DEVICE "/dev/ttyUSB0"
#define CODE_FILENAME "../bin_code.bin"
#define DATA_FILENAME "../bin_data.bin"
#define MAX_FILE_SIZE 0x100000
#define MINI16_CODE_ADDR 0x400
#define MINI16_DATA_ADDR 0x800
#define SYS_RESET_ADDR 0xc00

typedef struct
{
  uint8_t *buffer;
  int size;
} buffer_t;

void usage(char *command)
{
  printf("Usage: %s code_file data_file [UART_device]\nSupported ENV: UART_DEVICE\n", command);
  exit(EXIT_FAILURE);
}

int open_datafile(buffer_t *buffer, char *filename)
{
  FILE *fp;
  struct stat st;
  bzero(&st, sizeof(st));
  int read_size;
  fp = fopen(filename, "rb");
  if (fp == NULL)
  {
    perror("Error: file open");
    return -1;
  }
  if (fstat(fileno(fp), &st) == -1)
  {
    perror("Error: fstat");
    return -1;
  }
  buffer->size = st.st_size;
  if (buffer->size > MAX_FILE_SIZE)
  {
    perror("Error: MAX_FILE_SIZE");
    return -1;
  }
  buffer->buffer = malloc(buffer->size);
  if (buffer->buffer == NULL)
  {
    perror("Error: malloc");
    return -1;
  }
  read_size = fread(buffer->buffer, 1, buffer->size, fp);
  fclose(fp);
  if (read_size < 1)
  {
    perror("Error: fread");
    return -1;
  }
  return 0;
}

void close_datafile(buffer_t *buffer)
{
  free(buffer->buffer);
}

int main(int argc, char *argv[])
{
  int uart;
  char *devicename;
  char *cfilename;
  char *dfilename;
  buffer_t buffer;

  // check opts
  if ((argc < 3) || (argc > 4))
  {
    usage(argv[0]);
  }
  devicename = getenv("UART_DEVICE");
  if (argc == 4)
  {
    devicename = argv[3];
  }
  if (devicename == NULL)
  {
    printf("Error: bad device\n");
    return -1;
  }

  cfilename = argv[1];
  dfilename = argv[2];

  uart = uart_open(devicename);

  uart_send_word(uart, SYS_RESET_ADDR, 1); // cpu reset

  printf("Sending code...\n");
  if (open_datafile(&buffer, cfilename)) return -1;
  printf("size: %d\n", buffer.size);
  uart_send_data(uart, buffer.size, buffer.buffer, MINI16_CODE_ADDR);
  close_datafile(&buffer);

  printf("Sending data...\n");
  if (open_datafile(&buffer, dfilename)) return -1;
  printf("size: %d\n", buffer.size);
  uart_send_data(uart, buffer.size, buffer.buffer, MINI16_DATA_ADDR);
  close_datafile(&buffer);

  uart_send_word(uart, SYS_RESET_ADDR, 0); // cpu reset off

  uart_close(uart);
  printf("Finished.\n");
  return 0;
}
