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
#include <signal.h>
#include <stdint.h>
#include "uartlib.h"

static int quit = 0;

void usage(char *command)
{
  printf("Usage: %s [UART_device]\nSupported ENV: UART_DEVICE\n", command);
  exit(EXIT_FAILURE);
}

void sig_handler()
{
  quit = 1;
}

int main(int argc, char *argv[])
{
  int uart;
  char *devicename;
  uint8_t data = 0;
  struct sigaction sa;

  quit = 0;
  bzero(&sa, sizeof(struct sigaction));
  sa.sa_handler = sig_handler;
  if (sigaction(SIGINT, &sa, NULL) == -1)
  {
    perror("Error: sigaction");
    return -1;
  }

  // check opts
  if (argc > 2)
  {
    usage(argv[0]);
  }
  devicename = getenv("UART_DEVICE");
  if (argc == 2)
  {
    devicename = argv[1];
  }
  if (devicename == NULL)
  {
    printf("Error: bad device\n");
    return -1;
  }

  uart = uart_open(devicename);

  while (quit == 0)
  {
    uart_read(uart, &data, 1);
    printf("%c", data);
  }

  uart_close(uart);
  printf("Exited.\n");
  return 0;
}
