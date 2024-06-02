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
#include "uartlib.h"

//#define debug

int uart_open(char *device_name)
{
  int fd;
  struct termios tio;
  fd = open(device_name, O_RDWR);
  if (fd == -1)
  {
    perror("Error: device open");
    exit(-1);
  }
  bzero(&tio, sizeof(tio));
  tio.c_cflag = CS8 | CLOCAL | CREAD;
  cfmakeraw(&tio);
  cfsetospeed(&tio, BAUDRATE);
  cfsetispeed(&tio, BAUDRATE);
  tcflush(fd, TCIFLUSH);
  tcsetattr(fd, TCSANOW, &tio);
  return fd;
}

void uart_read(int uart, uint8_t *buf, size_t count)
{
  int err = read(uart, buf, count);
  if (err == -1) {perror("Error: read_uart");}
}

void uart_write(int uart, uint8_t *buf, size_t count)
{
  int err = write(uart, buf, count);
  if (err == -1) {perror("Error: uart_write");}
}

void uart_send_data(int uart, int file_size, uint8_t *buffer, unsigned int offset_address)
{
  uint8_t start_byte = SC1_START_CHAR;
  uint8_t end_byte = SC1_END_CHAR;
  uint32_t address[1];
  int i, j;
  for (i = 0; i < file_size; i += 4)
  {
    address[0] = i / 4 + offset_address;
    if (!is_little_endian())
    {
      address[0] = convert_endian(address[0]);
    }
    uint8_t *addrchar = (uint8_t *)&address[0];
    uart_write(uart, &start_byte, 1);
    for (j = 0; j < 4; j++)
    {
#ifdef debug
      printf("%02x ", addrchar[j]);
#endif
      uart_write(uart, &addrchar[j], 1);
    }
#ifdef debug
    printf(": ");
#endif
    for (j = 3; j > -1; j--)
    {
#ifdef debug
      printf("%02x ", buffer[i + j]);
#endif
      uart_write(uart, &buffer[i + j], 1);
    }
    uart_write(uart, &end_byte, 1);
#ifdef debug
    printf("\n");
#endif
  }
#ifdef debug
  printf("\n");
#endif
}

void uart_close(int fd)
{
  close(fd);
}

int is_little_endian()
{
  int one = 1;
  char is_little_endian = *(char *)&one;
  one = (int)is_little_endian;
  return one;
}

uint32_t convert_endian(uint32_t in)
{
  uint32_t out =
    ( in >> 24)                |
    ((in >> 16)  & 0x0000ff00) |
    ((in >>  8)  & 0x00ff0000) |
    ( in         & 0xff000000);
  return out;
}

void uart_send_word(int uart, uint32_t address, uint32_t data)
{
  uint8_t start_byte = SC1_START_CHAR;
  uint8_t end_byte = SC1_END_CHAR;
  uint8_t *buf;
  int j;

  uart_write(uart, &start_byte, 1);

  if (!is_little_endian())
  {
    address = convert_endian(address);
    data = convert_endian(data);
  }

  buf = (uint8_t *)&address;
  for (j = 0; j < 4; j++)
  {
    uart_write(uart, &buf[j], 1);
  }

  buf = (uint8_t *)&data;
  for (j = 0; j < 4; j++)
  {
    uart_write(uart, &buf[j], 1);
  }

  uart_write(uart, &end_byte, 1);
}
