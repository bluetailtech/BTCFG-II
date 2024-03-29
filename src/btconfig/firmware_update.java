//MIT License
//
//Copyright (c) 2020 bluetailtech
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

package btconfig;

import java.util.*;
import java.io.*;
import java.nio.*;
import com.fazecast.jSerialComm.*;
import javax.swing.*;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
class MessageListener2 implements SerialPortMessageListener
{
   @Override
      //data written only works on Windows, so we don't use
   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED | SerialPort.LISTENING_EVENT_DATA_WRITTEN; }
   //public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED ; }

   @Override
   public byte[] getMessageDelimiter() { return new byte[] { (byte)0xa6, (byte)0x67, (byte)0x54, (byte)0xd3 }; }
   //public byte[] getMessageDelimiter() { return new byte[] { (byte)0xd3, (byte)0x54, (byte)0x67, (byte)0xa6 }; }

   @Override
   public boolean delimiterIndicatesEndOfMessage() { return false; }

   @Override
   public void serialEvent(SerialPortEvent event)
   {
       if(event.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
          byte[] newData = event.getReceivedData();

         if(newData.length>=48) {
            System.out.print("\r\nPDATA: ");
            for (int i = 0; i < 16; ++i)
               System.out.print(String.format("0x%02x, ", (byte)newData[i]));
                System.out.print("\r\n");

              firmware_update.pdata = new byte[ newData.length-4];

              for (int i = 0; i < newData.length-4; ++i) {
                firmware_update.pdata[i] = newData[i];
              }
              firmware_update.have_data=1;
          }
         return;
       }
       if(event.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
         System.out.println("data written event");
         return;
       }
   }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
class firmware_update
{

volatile static int have_data=0;
volatile static byte[] pdata;
volatile static MessageListener2 listener; 

int check_mod=0;
String new_firmware_crc = "";
java.util.Timer utimer;
BTFrame parent;
SerialPort serial_port;
int did_save=0;

////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
  public void send_firmware(BTFrame parent, BufferedInputStream bis, SerialPort serial_port)
  {
    this.serial_port = serial_port;
    this.parent = parent;

    byte[] appcrc = new byte[4]; 
    byte[] image_buffer = new byte[128 * 1024 * 6];

    for( int i=0; i< 128 * 1024 *6; i++) {
      image_buffer[i] = (byte) 0xff;
    }


    int firmware_len = 0;

		//File f = new File(firmware_path);
    //parent.setStatus("loading firmware image "+firmware_path);

    try {

			//FileInputStream fis = new FileInputStream(f);

      bis.read(appcrc, 0, 4);
      firmware_len = bis.read(image_buffer, 0, 128*1024*6);

      //int crc = crc32.crc32_range(image_buffer, 128 * 1024 * 6);
      int crc=0;

      ByteBuffer bbcrc = ByteBuffer.wrap(appcrc);
      bbcrc.order(ByteOrder.LITTLE_ENDIAN);
      crc = bbcrc.getInt();

      parent.setProgress(5);
      //parent.setStatus( String.format(" len = %d, crc: 0x%08x", firmware_len, crc) );
      parent.setStatus("Checking installed firmware version..."); 

      int state = -1; 
      int app_crc_valid=0;
      int is_bl = 0;

      while(true) {


          if(state==-1) {
            if(serial_port!=null && serial_port.isOpen()) {
              state=0;
            } 
            else {
              parent.is_connected=0;
              parent.do_connect=1;
              return;
            }
          }


          //state app_crc
          if(state==0) {

            //stop all the high bw stuff on usb i/o
            String cmd= new String("en_voice_send 0\r\n");
            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
            cmd= new String("logging -999\r\n");
            SLEEP(100);
            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
            SLEEP(100);
            /////////////


            String res = send_cmd("app_crc\r\n", 1100);
            //parent.setStatus("resp: "+res+":");

            StringTokenizer st = new StringTokenizer(res," \r\n");
            if(st.countTokens()<2) {
              parent.setStatus("\r\nSearching for device.");
              //System.exit(0);
              //return;
              SLEEP(100);
              //parent.is_connected=0;
              //parent.do_connect=1;
              return;
            }

            while(st!=null && st.hasMoreTokens()) {
              String str1 = st.nextToken();
              if(str1!=null && str1.trim().equals("app_crc") && st.hasMoreTokens()) {

                String crc_str = st.nextToken();
                crc_str = crc_str.substring(2,crc_str.length());


                int app_crc=0;
                try {
                  //must process as long because... signed ints only
                  app_crc = (int) Long.parseLong(crc_str, 16);
                } catch(Exception e) {
                  e.printStackTrace();
                  break;
                }

                if(app_crc == crc) {
                  parent.setStatus("\r\nfirmware is up-to-date");
                  state=1;
                  app_crc_valid=1;
                  parent.setProgress(100);
                  parent.do_update_firmware=0;
                  parent.do_update_firmware2=0;
                  parent.fw_completed=1;

                  if(did_save==0) {
                    send_cmd("save\r\n", 1000); //flush new changes from global_post_read() to flash memory
                    did_save=1;
                  }
                  SLEEP(1000);

                  break;
                }
                else if(app_crc != crc) {
                  parent.setStatus("\r\nfirmware is not up-to-date");
                  parent.setProgress(0);
                  state=1;
                  app_crc_valid=0;
                  did_save=0;
                  break;
                }
              }
            }
          } //state==0

          //state boot_or_app
          if(state==1) {
            String res = send_cmd("bl_or_app\r\n", 1000).trim();
            System.out.println("resp: "+res+":");

            if(res.contains("bl_or_app bl")) {
              parent.setStatus("device is in: bootloader state");
              is_bl=1;
              state=2;
            }
            else if(res.contains("bl_or_app app")) {
              parent.setStatus("device is in: application state");
              is_bl=0;
              if(app_crc_valid==1) state=2;
              if(app_crc_valid==0) state=3;
            }
            else {
              SLEEP(100);
            }

          } //state==1

          //state switch to bootloader mode
          if(state==3 && is_bl==0) {

              SLEEP(500);

              parent.setStatus("\r\nsetting boot cmd to bootloader state");

              byte[] out_buffer = new byte[48]; //size of bl_op
              ByteBuffer bb = ByteBuffer.wrap(out_buffer);
              bb.order(ByteOrder.LITTLE_ENDIAN);
              //uint32_t magic;
              //uint32_t op;
              //uint32_t addr;
              //uint32_t len;
              //uint8_t  data[32]; 

              bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
              bb.putInt( (int) Long.parseLong("2", 10) ); //write bootloader boot_cmd
              bb.putInt( (int) Long.parseLong("08020000", 16) );
              bb.putInt( (int) Long.parseLong("8", 10) );
              //boot cmd area
              bb.putInt( (int) Long.parseLong("1", 10) ); //verify app and boot 
              bb.putInt( (int) crc);  //app crc 

              serial_port.writeBytes( out_buffer, 48, 0);
              //TODO: need to check for ack
              try {
                SLEEP(1000);
              } catch(Exception e) {
              }
              serial_port.writeBytes( out_buffer, 48, 0);

              //TODO: need to check for ack
              try {
                SLEEP(8000);
              } catch(Exception e) {
              }

              //System.exit(0);

              parent.setStatus("\r\nresetting device");
              String cmd = "system_reset\r\n";
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(10);

              try {
                SLEEP(2000);
              } catch(Exception e) {
              }
              //serial_port.closePort();
            //
              parent.is_connected=0;
              parent.do_connect=1;

              state=-1;
          }


          if(state==5) {
              parent.setStatus("\r\nreading boot_cmd area");

              byte[] out_buffer = new byte[48]; //size of bl_op
              ByteBuffer bb = ByteBuffer.wrap(out_buffer);
              bb.order(ByteOrder.LITTLE_ENDIAN);
              //uint32_t magic;
              //uint32_t op;
              //uint32_t addr;
              //uint32_t len;
              //uint8_t  data[32]; 

              bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
              bb.putInt( (int) Long.parseLong("1", 10) ); //read bootloader boot_cmd
              bb.putInt( (int) Long.parseLong("08020000", 16) );
              bb.putInt( (int) Long.parseLong("8", 10) );
              for(int i=0;i<32;i++) {
                bb.put((byte) 0x00);
              }
              serial_port.writeBytes( out_buffer, 48, 0);

              byte[] input_buffer = new byte[48];

              int rlen=0;
              int ack_timeout=0;
                while(rlen!=48) {

                  try {
                    int count=0;
                    while(serial_port.bytesAvailable()<48) {
                      SLEEP(1);
                      if(count++>50) break;
                    }
                  } catch(Exception e) {
                    e.printStackTrace();
                  }

                  rlen=serial_port.readBytes( input_buffer, 48);

                  if(rlen==48) break;

                }

                for(int i=0;i<16+8;i++) {
                  if(i==0) System.out.print(String.format("\r\n%02x,",input_buffer[i]));
                   else System.out.print(String.format("%02x,",input_buffer[i]));
                }
            parent.setStatus("");

            //System.exit(0);
            return;
          }

          //state switch to application mode
          if(state==2 && is_bl==1) {
              if( listener==null ) {
                listener = new MessageListener2();
                this.serial_port.addDataListener(listener);
              }

              try {
                SLEEP(500);
              } catch(Exception e) {
              }

            if(app_crc_valid==1) {
              parent.setStatus("\r\nsetting boot cmd to applicaton state");

              byte[] out_buffer = new byte[48]; //size of bl_op
              ByteBuffer bb = ByteBuffer.wrap(out_buffer);
              bb.order(ByteOrder.LITTLE_ENDIAN);
              //uint32_t magic;
              //uint32_t op;
              //uint32_t addr;
              //uint32_t len;
              //uint8_t  data[32]; 

              bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
              bb.putInt( (int) Long.parseLong("2", 10) ); //write bootloader boot_cmd
              bb.putInt( (int) Long.parseLong("08020000", 16) );
              bb.putInt( (int) Long.parseLong("8", 10) );
              //boot cmd area
              bb.putInt( (int) Long.parseLong("2", 10) ); //verify app and boot 
              bb.putInt( (int) crc);  //app crc 

              serial_port.writeBytes( out_buffer, 48, 0);
              //TODO: need to check for ack
              try {
                SLEEP(1000);
              } catch(Exception e) {
              }
              serial_port.writeBytes( out_buffer, 48, 0);

              //TODO: need to check for ack
              try {
                SLEEP(8000);
              } catch(Exception e) {
              }

              parent.setProgress(90);
              parent.setStatus("\r\nresetting device");
              String cmd = "system_reset\r\n";
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(10);

              try {
                SLEEP(2000);
              } catch(Exception e) {
              }
              //serial_port.closePort();
              parent.is_connected=0;
              parent.do_connect=1;

              state=-1;

                  JOptionPane.showMessageDialog(parent, "Firmware update is complete. You will need to re-start the software.");
                  System.exit(0);
            }

            if(app_crc_valid==0) {
              parent.setStatus("\r\nerasing app area, sending new firmware image...");

              int offset = 0;
              parent.setProgress(20);

              while(offset<firmware_len) {
              //while(offset<(1024*128*6)) {

                byte[] out_buffer = new byte[16+32]; //size of bl_op
                ByteBuffer bb = ByteBuffer.wrap(out_buffer);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                bb.putInt( (int) Long.parseLong("d35467a6", 16) );  //magic
                bb.putInt( (int) Long.parseLong("6", 10) ); //write encrypted flash cmd (same as "3", but decryption first) 
                bb.putInt( (int) new Long((long) 0x08040000 + offset).longValue() );
                bb.putInt( (int) Long.parseLong("32", 10) );  //data len

                for(int i=0;i<32;i++) {
                  bb.put( image_buffer[i+offset] ); 
                }

                have_data=0;

                serial_port.writeBytes( out_buffer, 48, 0);

                int count=0;
                if(offset%131072==0) SLEEP(3000);
                while(have_data==0) {
                  if(count++>500) {
                    System.out.println("timeout");
                    break;
                  }
                  parent.SLEEP_US(5);
                }

                int did_write=0;

                if(have_data==1) {
                  ByteBuffer bb_verify = ByteBuffer.wrap(pdata);
                  bb_verify.order(ByteOrder.LITTLE_ENDIAN);
                  if( bb_verify.getInt()== 0xd35467a6) {//magic
                    int op = bb_verify.getInt();  //op
                    System.out.println("op "+op);
                    if( op==4 && bb_verify.getInt()==0x8040000+offset) { //address
                      did_write=1;
                    }
                  }
                  have_data=0;
                }
                else {
                  System.out.println("no data");
                }

                if(did_write==1) {
                  offset+=32;
                  if(offset%8192==0) System.out.print("\rsent "+offset+"        ");

                  int pcomplete = (int)  (((float) offset/(float) firmware_len)*80.0);
                  parent.setProgress((int) pcomplete);
                }
              }
                System.out.print("\rsent "+offset+"        ");



              //TODO: need to check for ack
              try {
                SLEEP(100);
              } catch(Exception e) {
              }

              parent.setStatus("\r\nresetting device");
              String cmd = "system_reset\r\n";
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(10);

              try {
                SLEEP(2000);
              } catch(Exception e) {
              }

              if(listener!=null) this.serial_port.removeDataListener();
              listener = null; 


              //serial_port.closePort();
              parent.is_connected=0;
              parent.do_connect=1;


              state=-1;
            }

          }

          //are we in application mode with good crc?
          if(state==2 && is_bl==0) {
            //serial_port.closePort();
            parent.setStatus("Firmware is up-to-date.");

            if(did_save==0) {
              send_cmd("save\r\n", 1000); //flush new changes from global_post_read() to flash memory
              did_save=1;
            }
            SLEEP(1000);


            //parent.firmware_checked=1;
            parent.do_read_config=1;
            //parent.do_read_talkgroups=1;
            //parent.is_connected=1;

            parent.do_update_firmware=0;
            parent.do_update_firmware2=0;
            parent.fw_completed=1;

            return;
          }

        } //while(true) 
    } catch (Exception e) {
      e.printStackTrace();
      if(listener!=null) this.serial_port.removeDataListener();
      listener = null; 
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  public String send_cmd(String cmd, int timeout)
  {

    byte[] data_cmd = cmd.getBytes();
    int len = serial_port.writeBytes( data_cmd, data_cmd.length, 0);

    byte[] data_buffer = new byte[2048];
    int i=0;

    int retry=0;
    int avail=0;

    while(true) {
      try {
        SLEEP(timeout);
      } catch(Exception e) {
        e.printStackTrace();
      }

      avail = serial_port.bytesAvailable();
      if(avail==0) return "";

      if(avail>0) break;

      if(retry++>5) break;
    }


      data_buffer = new byte[avail];

      try {
        len = serial_port.readBytes( data_buffer, avail);
      } catch(Exception e) {
      }

      //System.out.println("avail: "+avail+" String: "+new String(data_buffer,0,len)+" cmd:"+cmd);
      System.out.println("cmd: "+cmd);

      if(len>0) { 
        return new String(data_buffer,0,len);
      }

    return "";
  }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
private void SLEEP(long val) {
  try {
    parent.SLEEP(val);
  } catch(Exception e) {
    e.printStackTrace();
  }
}
}
