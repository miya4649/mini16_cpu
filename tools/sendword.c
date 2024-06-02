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

void usage(char *command)
{
  printf("Usage: %s address data [UART_device]\nSupported ENV: UART_DEVICE\n", command);
  exit(EXIT_FAILURE);
}

uint32_t str2ui32(char *word)
{
  char *endptr = NULL;
  uint32_t out = (uint32_t)strtoul(word, &endptr, 0);
  if (endptr[0] != '\0')
  {
    printf("Error: input %s\n", endptr);
    out = 0;
  }
  return out;
}

int main(int argc, char *argv[])
{
  int uart;
  char *devicename;
  unsigned int address, data;

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

  address = str2ui32(argv[1]);
  data = str2ui32(argv[2]);

  uart = uart_open(devicename);
  uart_send_word(uart, address, data);
  uart_close(uart);
  return 0;
}
