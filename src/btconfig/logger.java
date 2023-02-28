
//MIT License
//
//Copyright (c) 2023 bluetailtech
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.
//
//

package btconfig;

import java.nio.*;
import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.*;
import javax.swing.*;
import java.util.concurrent.TimeUnit;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class logger {

FileOutputStream fos;
BTFrame parent;
boolean debug=false;


  /////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////
  public logger(BTFrame p) {
    parent = p;
  }

  /////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////
  public void write_log( String rec, String fname, String header ) {
    try {
       File f = new File(fname);
       boolean b = f.exists();
       fos = new FileOutputStream(fname, true);
       if(!b) {
         fos.write( header.getBytes(), 0, header.getBytes().length );
       }
       fos.write( rec.getBytes(), 0, rec.getBytes().length );
       fos.flush();
       fos.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

}

