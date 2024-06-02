/*
  Copyright (c) 2019, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.io.*;
import java.util.*;

public class GetOpt
{
  private final HashMap<String, String> p_opts = new HashMap<String, String>();

  public void setArgs(String[] args)
  {
    p_opts.clear();
    for (int i = 0; i < args.length; i++)
    {
      String[] arg = args[i].split("=");
      if (arg.length == 2)
      {
        String key = arg[0].replace("-", "");
        String value = arg[1];
        p_opts.put(key, value);
      }
    }
  }

  public void print_error(String err)
  {
    System.out.printf("Error: GetOpt: %s\n", err);
    System.exit(1);
  }

  public String getValue(String key)
  {
    if (p_opts.get(key) == null)
    {
      print_error("getValue: " + key);
    }
    return p_opts.get(key);
  }

  public int getIntValue(String key)
  {
    if (p_opts.get(key) == null)
    {
      print_error("getIntValue: " + key);
    }
    return Integer.parseInt(p_opts.get(key));
  }

  public void setDefault(String key, String value)
  {
    if (p_opts.get(key) == null)
    {
      set(key, value);
    }
  }

  public void setDefault(String key, int value)
  {
    if (p_opts.get(key) == null)
    {
      set(key, value);
    }
  }

  public void set(String key, String value)
  {
    p_opts.put(key, value);
  }

  public void set(String key, int value)
  {
    p_opts.put(key, Integer.toString(value));
  }
}
