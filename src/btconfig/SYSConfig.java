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
import javax.swing.filechooser.*;
import javax.swing.*;
import javax.swing.*;




//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
class SYSConfig
{

  int read_serial_delay = 50;
  int write_serial_delay = 50;

  public final int DMR_CC1=(1<<0);
  public final int DMR_CC2=(1<<1);
  public final int DMR_CC3=(1<<2);
  public final int DMR_CC4=(1<<3);
  public final int DMR_CC5=(1<<4);
  public final int DMR_CC6=(1<<5);
  public final int DMR_CC7=(1<<6);
  public final int DMR_CC8=(1<<7);
  public final int DMR_CC9=(1<<8);
  public final int DMR_CC10=(1<<9);
  public final int DMR_CC11=(1<<10);
  public final int DMR_CC12=(1<<11);
  public final int DMR_CC13=(1<<12);
  public final int DMR_CC14=(1<<13);
  public final int DMR_CC15=(1<<14);
  public final int DMR_ISCC=(1<<15);
  public final int DMR_SLOT1=(1<<16);
  public final int DMR_SLOT2=(1<<17);

java.util.Timer utimer;
BTFrame parent;
SerialPort serial_port;
java.text.SimpleDateFormat formatter_date;

int did_warning=0;
int did_crc_reset=0;
int prev_op_mode=-1;



///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
void do_usb_watchdog(SerialPort sp) {

  /*
  try {
    byte[] out_buffer = new byte[16+32]; //size of bl_op
    ByteBuffer bb = ByteBuffer.wrap(out_buffer);
    bb.order(ByteOrder.LITTLE_ENDIAN);

    bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
    bb.putInt( (int) Long.parseLong("9", 10) ); //usb watchdog reset
    bb.putInt( (int) new Long((long) 0x00000000 ).longValue() );  //address to return
    bb.putInt( (int) Long.parseLong("0", 10) );  //data len  to return

    if(sp!=null) sp.writeBytes( out_buffer, 48); //16 + data len=0

  } catch(Exception e) {
    e.printStackTrace();
  }
  */
}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
Boolean is_valid_freq(double freq) {
  int band = 0;

  if(freq >= 27.0 && freq <= 512.0) band= 1;  //band 1
  if(freq >= 750.0 && freq <= 824.0) band= 2;  //band 2
  if(freq >= 849.0 && freq <= 869.0) band= 3;  //band 3
  if(freq >= 894.0 && freq <= 960.0) band= 5;  //band 4
  if(freq >= 1240.0 && freq <= 1300.0) band= 6;  //band 5


  if(band!=0) return true;

  return false; 
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public SYSConfig(BTFrame parent) {
  this.parent = parent;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
private void SLEEP(long val) {
  try {
    parent.SLEEP(val);
  } catch(Exception e) {
    e.printStackTrace();
  }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void read_sysconfig(BTFrame parent, SerialPort serial_port)
{
  this.serial_port = serial_port;

  byte[] image_buffer = new byte[128 * 1024 * 6];

  for( int i=0; i< 128 * 1024 *6; i++) {
    image_buffer[i] = (byte) 0xff;
  }

  int config_length = 0;
  int CONFIG_SIZE=1024;

  try {

    int state = -1; 

    while(true) {


        if(state==-1) {
          if(serial_port!=null && serial_port.isOpen()) {
            state=0;
          } 
        }
        else {
          parent.setProgress(-1);
          parent.setStatus("\r\ncouldn't find device");
          return;
        }

        try {
          serial_port.removeDataListener();
        } catch(Exception e) {
        }

      //get the number of recs
        if(state==0) {

          parent.setProgress(5);
          parent.setStatus("Reading sys_config from P25RX device..."); 


          byte[] bresult=new byte[64];
          //stop following
          String cmd = "f 0"+"\r\n";
          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
          SLEEP(write_serial_delay);
          int rlen=serial_port.readBytes( bresult, 64);
          System.out.println("bresult: "+new String(bresult) );
          SLEEP(read_serial_delay);


          int offset = 0;
          //while(offset<config_length) {


          int nrecs=0;
          int timeout=0;
          while(true) {

              if(timeout++>10) break;

              byte[] out_buffer = new byte[16+32]; //size of bl_op
              ByteBuffer bb = ByteBuffer.wrap(out_buffer);
              bb.order(ByteOrder.LITTLE_ENDIAN);

              bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
              bb.putInt( (int) Long.parseLong("6", 10) ); //read cfg flash
              bb.putInt( (int) new Long((long) 0x08100000 + offset).longValue() );  //address to return
              bb.putInt( (int) Long.parseLong("32", 10) );  //data len  to return



              byte[] input_buffer = new byte[48];
              rlen=0;
              while(rlen!=48) {
                serial_port.writeBytes( out_buffer, 48, 0); //16 + data len=0

                  try {
                    int count=0;
                    while(serial_port.bytesAvailable()<48) {
                      SLEEP(1);
                      if(count++>50) break;
                    }
                  } catch(Exception e) {
                    e.printStackTrace();
                  }

                rlen=serial_port.readBytes( input_buffer, 48 );
                if(rlen==48) {
                  break;
                }
                //else {
                 // System.out.println("rlen<>48");
                //}
              }

              ByteBuffer bb2 = ByteBuffer.wrap(input_buffer);
              bb2.order(ByteOrder.LITTLE_ENDIAN);


              if( bb2.getInt()== 0xd35467A6) {//magic
                bb2.getInt();  //op
                bb2.getInt();  //address
                bb2.getInt();  //len
                nrecs = bb2.getInt();
                if(nrecs>0 && nrecs<1280000) break;
              }
              else {
                //flush the input buffers
                byte[] b = new byte[ serial_port.bytesAvailable()+1 ];
                if(b.length>0)serial_port.readBytes( b, b.length-1 );  //flush buffer
              }
          }

          if(nrecs>0) {
            parent.setStatus("\r\nCompleted reading sys_config. nrecs: "+nrecs);
          }
          else {
            parent.setStatus("\r\nNo talkgroup records found.");
          }
          parent.setProgress(10);



          offset = 0; //skip the nrecs int

          while(true) {

              byte[] out_buffer = new byte[16+32]; //size of bl_op
              ByteBuffer bb = ByteBuffer.wrap(out_buffer);
              bb.order(ByteOrder.LITTLE_ENDIAN);

              bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
              bb.putInt( (int) Long.parseLong("6", 10) ); //read cfg flash
              bb.putInt( (int) new Long((long) 0x08100000 + offset).longValue() );  //address to return
              bb.putInt( (int) Long.parseLong("32", 10) );  //data len  to return


              byte[] input_buffer = new byte[32000];
              rlen=0;
              while(rlen!=48) {
                serial_port.writeBytes( out_buffer, 48, 0); //16 + data len=0

                  try {
                    int count=0;
                    while(serial_port.bytesAvailable()<48) {
                      SLEEP(1);
                      if(count++>50) break;
                    }
                  } catch(Exception e) {
                    e.printStackTrace();
                  }

                rlen=serial_port.readBytes( input_buffer, 48 );
                if(rlen==48) {
                  break;
                }
                else {
                  serial_port.readBytes( input_buffer, serial_port.bytesAvailable() );
                }
              }

              ByteBuffer bb2 = ByteBuffer.wrap(input_buffer);
              bb2.order(ByteOrder.LITTLE_ENDIAN);


              if( bb2.getInt()== 0xd35467A6) {//magic
                bb2.getInt();  //op
                int raddress = (bb2.getInt()-0x08100000) ;  //address
                bb2.getInt();  //len

                if(raddress>=0) {
                  for(int i=0;i<32;i++) {
                    image_buffer[i+raddress] = bb2.get();
                  }

                  offset+=32;
                  //if(offset >= 552+32) { //finished?
                  if(offset >= CONFIG_SIZE+32) { //finished?

                    ByteBuffer bb3 = ByteBuffer.wrap(image_buffer);
                    bb3.order(ByteOrder.LITTLE_ENDIAN);
                    int crc = crc32.crc32_range(image_buffer, CONFIG_SIZE-4);
                    parent.system_crc=crc;
                    System.out.println(String.format("config crc 0x%08x", crc));

                    int config_crc = bb3.getInt(CONFIG_SIZE-4);  //1024-4

                      if(crc==0 || config_crc == 0xffffffff) {
                        if(parent.fw_completed==0) {
                            fw_update_speed fwus = new fw_update_speed(parent);
                            fwus.init();
                            fwus.setVisible(true);
                          parent.do_update_firmware=1;
                          parent.do_update_firmware2=1;
                        }
                        parent.do_read_talkgroups=0;
                        parent.did_read_talkgroups=1;
                        parent.is_connected=1;

                        parent.do_read_config=0;
                          //int result2 = JOptionPane.showConfirmDialog(parent, "Would you like to erase talk group and roaming frequency flash?", "Erase Config Areas?", JOptionPane.YES_NO_OPTION);
                          //if(result2==JOptionPane.YES_OPTION) {
                           // String cmd = "clear_configs\r\n";
                            //serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                            //SLEEP(3000);
                          //}
                            SLEEP(3000);
                        return;
                      }


                    if(crc == config_crc) {
                        parent.do_read_talkgroups=1;
                        parent.do_read_config=1;

                      parent.system_crc=crc;

                      parent.setStatus("sys_config crc ok."); 

                      System.out.println( String.format("\r\nfrequency: %3.6f",bb3.getDouble()) );
                      System.out.println( String.format("\r\nis_control: %d",bb3.getInt(36)) );
                      System.out.println( String.format("\r\nvolume: %3.2f",bb3.getFloat(12)) );
                      System.out.println( String.format("\r\nbluetooth: %d",bb3.getInt(88)) );
                      System.out.println( String.format("\r\nbluetooth reset: %d",bb3.getInt(260)/5) );
                      System.out.println( String.format("\r\nbt_gain: %3.2f",bb3.getFloat(176)) );
                      System.out.println( String.format("\r\nled_mode: %d",bb3.getInt(196)) );
                      System.out.println( String.format("\r\nallow unknown tg: %d",bb3.getShort(130)) );
                      System.out.println( String.format("\r\nenable_roaming %d",bb3.getInt(68)) );
                      System.out.println( String.format("\r\nno_voice_roam_sec",bb3.getInt(280)) );

                      System.out.println( String.format("\r\nconfig verson: %d",bb3.getInt(544)) );
                      System.out.println( String.format("\r\nconfig crc: 0x%08x",config_crc) );
                      String fw_ver = "";
                      byte[] fw_version = new byte[12];
                      for(int c=0;c<12;c++) {
                        fw_version[c] = (byte) bb3.get(264+c);
                      } 

                      fw_ver = new String( fw_version );
                      parent.fw_installed.setText("   Installed FW: "+fw_ver);

                      if(parent.fw_ver.getText().contains(fw_ver)) {
                        parent.fw_ver.setVisible(false);
                        parent.fw_installed.setVisible(false);
                      }
                      else {
                        parent.fw_ver.setVisible(true);
                        parent.fw_installed.setVisible(true);
                      }

                      if( did_warning==0 && !parent.fw_ver.getText().contains(fw_ver) ) {
                        //int result = JOptionPane.showConfirmDialog(parent, "Proceed With Firmware Update?  Cancel To Exit Application.", "Update Firmware?", JOptionPane.OK_CANCEL_OPTION);
                        //if(result==JOptionPane.OK_OPTION) {
                        if(parent.fw_completed==0) {
                            fw_update_speed fwus = new fw_update_speed(parent);
                            fwus.init();
                            fwus.setVisible(true);
                            parent.do_update_firmware=1;
                            parent.do_update_firmware2=1;
                        }
                        else {
                          JOptionPane.showMessageDialog(parent, "Firmware update is complete. You will need to re-start the software.");
                          System.exit(0);
                          //parent.is_connected=1;
                          //parent.do_read_talkgroups=0;
                          //parent.did_read_talkgroups=1;
                          //did_warning=1;
                        }
                      }
                      else if( did_warning==1 && !parent.fw_ver.getText().contains(fw_ver) ) {
                        if(parent.fw_completed==0) {
                            parent.do_update_firmware2=1;
                            parent.do_update_firmware=1;
                        }
                          parent.is_connected=1;
                          parent.do_read_talkgroups=0;
                          parent.did_read_talkgroups=1;
                      }




                      parent.do_read_config=0;

                      if( parent.do_write_config==0) {
                        try {
                          int op_mode = bb3.getInt(516);

                          parent.is_dmr_mode=0;
                          if(op_mode==2) parent.is_dmr_mode=1;

                          //if(op_mode==3) parent.freq_label.setText("FM NB Frequency");

                          if(op_mode==1) parent.op_mode.setSelectedIndex( 0 );
                          else if(op_mode==2) parent.op_mode.setSelectedIndex( 1 );
                          else if(op_mode==5) parent.op_mode.setSelectedIndex( 2 );
                          else if(op_mode==3) parent.op_mode.setSelectedIndex( 3 );
                          else {
                            parent.op_mode.setSelectedIndex( 0 );
                          }

                          parent.frequency_tf1.setText( String.format("%3.6f", bb3.getDouble(0)) );

                          int p2lsm = bb3.getInt(804);
                          if(p2lsm>0) parent.p2_lsm.setSelected(true);
                              else parent.p2_lsm.setSelected(false);

                          int vrep = bb3.getInt(288);
                          if(vrep>0) parent.vrep.setSelected(true);
                              else parent.vrep.setSelected(false);

                          int vga_step = bb3.getInt(740);
                          parent.vga_step.setText( String.format("%d", vga_step) );

                          int vga_target = bb3.getInt(732);
                          int vga_evmth = bb3.getInt(736);

                          try {
                            parent.vga_target.setText( Integer.valueOf(vga_target).toString() );
                          } catch(Exception e) {
                          }

                          int tgzone = bb3.getInt(772);
                          try {
                            parent.current_tgzone = tgzone;

                            parent.update_zones();

                          } catch(Exception e) {
                          }



                          int skip_tg_to = bb3.getInt(552);
                          parent.skip_tg_to.setText( Integer.toString(skip_tg_to/1000/60) );

                          int enc_mode = bb3.getInt(564);

                          int en_tg_pri_int = bb3.getInt(568);
                          if(en_tg_pri_int==1) parent.allow_tg_pri_int.setSelected(true);
                            else parent.allow_tg_pri_int.setSelected(false);

                          int en_tg_int_tone = bb3.getInt(576);
                          if(en_tg_int_tone==1) parent.en_tg_int_tone.setSelected(true);
                            else parent.en_tg_int_tone.setSelected(false);

                          if(enc_mode==1) parent.enc_mode.setSelected(true);
                            else parent.enc_mode.setSelected(false);

                          /*
                          int ch_flt = bb3.getInt(788);
                          if(ch_flt<0) ch_flt=0; 
                          if(ch_flt>3) ch_flt=3; 
                          parent.ch_flt.setSelectedIndex(ch_flt);
                          */

                          int rf_hyst = bb3.getInt(628);
                          if(rf_hyst==3) parent.rf_hyst.setSelectedIndex(0);
                          else if(rf_hyst==6) parent.rf_hyst.setSelectedIndex(1);
                          else if(rf_hyst==10) parent.rf_hyst.setSelectedIndex(2);
                          else if(rf_hyst==16) parent.rf_hyst.setSelectedIndex(3);
                          else if(rf_hyst==22) parent.rf_hyst.setSelectedIndex(4);
                          else parent.rf_hyst.setSelectedIndex(1);

                          int enctimeout = bb3.getInt(760);
                          parent.enc_timeout.setText( new Integer(enctimeout).toString() );

                          int enccount = bb3.getInt(764);
                          parent.enc_count.setText( new Integer(enccount).toString() );


                          float agc_max_gain = bb3.getFloat(168);
                          try {
                            parent.audio_agc_max.setText( String.format("%3.2f", agc_max_gain) );
                          } catch(Exception e) {
                          }

                          int demod = bb3.getInt(780);
                          if(demod<0) demod=0;
                          if(demod>1) demod=1;
                          parent.demod.setSelectedIndex(demod);


                          int but1_cfg = bb3.getInt(540);
                          int but2_cfg = bb3.getInt(544);
                          int but3_cfg = bb3.getInt(548);
                          int but4_cfg = bb3.getInt(556);

                          int vga_gain = bb3.getInt(636);

                          int p1_ssync = bb3.getInt(708);
                          int p2_ssync = bb3.getInt(712);

                          try {
                            parent.p1_sync_thresh.setText( new Integer(p1_ssync).toString() );
                          } catch(Exception e) {
                          }
                          try {
                            parent.p2_sync_thresh.setText( new Integer(p2_ssync).toString() );
                          } catch(Exception e) {
                          }


                          int agc_mode = bb3.getInt(136);
                          if(agc_mode < 1) agc_mode=1;
                          if(agc_mode > 6) agc_mode=6;


                          int lna_gain = bb3.getInt(616);
                          parent.rfgain.setSelectedIndex(lna_gain+1);

                          int mgain = bb3.getInt(728);
                          parent.mixgain.setSelectedIndex(mgain+1);


                          //if(vga_gain < 0) vga_gain=0;
                          //if(vga_gain > 15) vga_gain=15;
                          parent.vga_gain.setSelectedIndex(vga_gain+1);

                          int roam_ret_to_cc = bb3.getInt(560);

                          if( roam_ret_to_cc == 1) parent.roaming_ret_to_cc.setSelected(true);
                            else parent.roaming_ret_to_cc.setSelected(false); 

                          if( but1_cfg == 0 ) parent.single_click_opt1.setSelected(true);
                          else if( but1_cfg == 1 ) parent.single_click_opt2.setSelected(true);
                          else if( but1_cfg == 2 ) parent.single_click_opt3.setSelected(true);
                          else if( but1_cfg == 3 ) parent.single_click_opt4.setSelected(true);
                          else if( but1_cfg == 4 ) parent.single_click_opt5.setSelected(true);
                          else if( but1_cfg == 5 ) parent.single_click_opt6.setSelected(true);
                          else parent.single_click_opt1.setSelected(true); //default

                          if( but2_cfg == 0 ) parent.double_click_opt1.setSelected(true);
                          else if( but2_cfg == 1 ) parent.double_click_opt2.setSelected(true);
                          else if( but2_cfg == 2 ) parent.double_click_opt3.setSelected(true);
                          else if( but2_cfg == 3 ) parent.double_click_opt4.setSelected(true);
                          else if( but2_cfg == 4 ) parent.double_click_opt5.setSelected(true);
                          else if( but2_cfg == 5 ) parent.double_click_opt6.setSelected(true);
                          else parent.double_click_opt2.setSelected(true); //default

                          if( but3_cfg == 0 ) parent.triple_click_opt1.setSelected(true);
                          else if( but3_cfg == 1 ) parent.triple_click_opt2.setSelected(true);
                          else if( but3_cfg == 2 ) parent.triple_click_opt3.setSelected(true);
                          else if( but3_cfg == 3 ) parent.triple_click_opt4.setSelected(true);
                          else if( but3_cfg == 4 ) parent.triple_click_opt5.setSelected(true);
                          else if( but3_cfg == 5 ) parent.triple_click_opt6.setSelected(true);
                          else parent.triple_click_opt3.setSelected(true); //default

                          if( but4_cfg == 0 ) parent.quad_click_opt1.setSelected(true);
                          else if( but4_cfg == 1 ) parent.quad_click_opt2.setSelected(true);
                          else if( but4_cfg == 2 ) parent.quad_click_opt3.setSelected(true);
                          else if( but4_cfg == 3 ) parent.quad_click_opt4.setSelected(true);
                          else if( but4_cfg == 4 ) parent.quad_click_opt5.setSelected(true);
                          else if( but4_cfg == 5 ) parent.quad_click_opt6.setSelected(true);
                          else parent.quad_click_opt6.setSelected(true); //default



                          int iscontrol = bb3.getInt(36);
                          int is_analog = bb3.getInt(52);

                          if( iscontrol==0 ) parent.conventionalchannel.setSelected(true);
                            else parent.controlchannel.setSelected(true);

                          if(parent.conventionalchannel.isSelected()) parent.freq_label.setText("Conventional Channel Frequency");
                          if(parent.controlchannel.isSelected()) parent.freq_label.setText("Control Channel Frequency");



                          int dmr_config = bb3.getInt(512);

                          if( (dmr_config & DMR_CC1) > 0 ) parent.dmr_cc_en1.setSelected(true);
                            else parent.dmr_cc_en1.setSelected(false);
                          if( (dmr_config & DMR_CC2) > 0 ) parent.dmr_cc_en2.setSelected(true);
                            else parent.dmr_cc_en2.setSelected(false);
                          if( (dmr_config & DMR_CC3) > 0 ) parent.dmr_cc_en3.setSelected(true);
                            else parent.dmr_cc_en3.setSelected(false);
                          if( (dmr_config & DMR_CC4) > 0 ) parent.dmr_cc_en4.setSelected(true);
                            else parent.dmr_cc_en4.setSelected(false);
                          if( (dmr_config & DMR_CC5) > 0 ) parent.dmr_cc_en5.setSelected(true);
                            else parent.dmr_cc_en5.setSelected(false);
                          if( (dmr_config & DMR_CC6) > 0 ) parent.dmr_cc_en6.setSelected(true);
                            else parent.dmr_cc_en6.setSelected(false);
                          if( (dmr_config & DMR_CC7) > 0 ) parent.dmr_cc_en7.setSelected(true);
                            else parent.dmr_cc_en7.setSelected(false);
                          if( (dmr_config & DMR_CC8) > 0 ) parent.dmr_cc_en8.setSelected(true);
                            else parent.dmr_cc_en8.setSelected(false);
                          if( (dmr_config & DMR_CC9) > 0 ) parent.dmr_cc_en9.setSelected(true);
                            else parent.dmr_cc_en9.setSelected(false);
                          if( (dmr_config & DMR_CC10) > 0 ) parent.dmr_cc_en10.setSelected(true);
                            else parent.dmr_cc_en10.setSelected(false);
                          if( (dmr_config & DMR_CC11) > 0 ) parent.dmr_cc_en11.setSelected(true);
                            else parent.dmr_cc_en11.setSelected(false);
                          if( (dmr_config & DMR_CC12) > 0 ) parent.dmr_cc_en12.setSelected(true);
                            else parent.dmr_cc_en12.setSelected(false);
                          if( (dmr_config & DMR_CC13) > 0 ) parent.dmr_cc_en13.setSelected(true);
                            else parent.dmr_cc_en13.setSelected(false);
                          if( (dmr_config & DMR_CC14) > 0 ) parent.dmr_cc_en14.setSelected(true);
                            else parent.dmr_cc_en14.setSelected(false);
                          if( (dmr_config & DMR_CC15) > 0 ) parent.dmr_cc_en15.setSelected(true);
                            else parent.dmr_cc_en15.setSelected(false);

                          if( (dmr_config & DMR_ISCC) > 0 ) parent.dmr_conplus.setSelected(true);
                            else parent.dmr_conventional.setSelected(true);
                          parent.update_dmr_lcn1_label();

                          if( (dmr_config & DMR_SLOT1) > 0 ) parent.dmr_slot1.setSelected(true);
                            else parent.dmr_slot1.setSelected(false);

                          if( (dmr_config & DMR_SLOT2) > 0 ) parent.dmr_slot2.setSelected(true);
                            else parent.dmr_slot2.setSelected(false);


                          parent.dmr_sys_id.setText( String.format("%d", bb3.getInt(536)) );


                          float vol = bb3.getFloat(12);
                          vol *= 100.0f;
                          parent.lineout_vol_slider.setValue( (int) vol );
                          parent.volume_label.setText( String.format("%3.2f", vol/100.0f) );

                          float bt_gain = bb3.getFloat(176);
                          bt_gain *= 100.0f;
                          parent.bt_vol_slider1.setValue( (int) bt_gain );
                          parent.volume_label1.setText( String.format("%3.2f", bt_gain/100.0f) );

                          float p25_tone_vol = bb3.getFloat(244);
                          parent.p25_tone_vol.setText( String.format("%3.2f", p25_tone_vol) );

                          Boolean b = true;
                          if(bb3.getInt(88)==1) b=true;
                              else b=false;
                          parent.en_bluetooth_cb.setSelected(b); 

                          b = false;
                          if(bb3.getInt(236)==1) b=true;  //en_encout
                              else b=false;
                          parent.en_encout.setSelected(b); 


                          b = false;
                          if(bb3.getInt(216)==1) b=true;  //en_p2_tones
                              else b=false;
                          parent.en_p2_tones.setSelected(b); 



                          parent.lcn1_freq.setText( String.format("%3.6f", bb3.getDouble(384)) );
                          parent.lcn2_freq.setText( String.format("%3.6f", bb3.getDouble(392)) );
                          parent.lcn3_freq.setText( String.format("%3.6f", bb3.getDouble(400)) );
                          parent.lcn4_freq.setText( String.format("%3.6f", bb3.getDouble(408)) );
                          parent.lcn5_freq.setText( String.format("%3.6f", bb3.getDouble(416)) );
                          parent.lcn6_freq.setText( String.format("%3.6f", bb3.getDouble(424)) );
                          parent.lcn7_freq.setText( String.format("%3.6f", bb3.getDouble(432)) );
                          parent.lcn8_freq.setText( String.format("%3.6f", bb3.getDouble(440)) );
                          parent.lcn9_freq.setText( String.format("%3.6f", bb3.getDouble(448)) );
                          parent.lcn10_freq.setText( String.format("%3.6f", bb3.getDouble(456)) );
                          parent.lcn11_freq.setText( String.format("%3.6f", bb3.getDouble(464)) );
                          parent.lcn12_freq.setText( String.format("%3.6f", bb3.getDouble(472)) );
                          parent.lcn13_freq.setText( String.format("%3.6f", bb3.getDouble(480)) );
                          parent.lcn14_freq.setText( String.format("%3.6f", bb3.getDouble(488)) );
                          parent.lcn15_freq.setText( String.format("%3.6f", bb3.getDouble(496)) );

                          if(parent.lcn1_freq.getText().equals("0.000000")) parent.lcn1_freq.setText("");
                          if(parent.lcn2_freq.getText().equals("0.000000")) parent.lcn2_freq.setText("");
                          if(parent.lcn3_freq.getText().equals("0.000000")) parent.lcn3_freq.setText("");
                          if(parent.lcn4_freq.getText().equals("0.000000")) parent.lcn4_freq.setText("");
                          if(parent.lcn5_freq.getText().equals("0.000000")) parent.lcn5_freq.setText("");
                          if(parent.lcn6_freq.getText().equals("0.000000")) parent.lcn6_freq.setText("");
                          if(parent.lcn7_freq.getText().equals("0.000000")) parent.lcn7_freq.setText("");
                          if(parent.lcn8_freq.getText().equals("0.000000")) parent.lcn8_freq.setText("");
                          if(parent.lcn9_freq.getText().equals("0.000000")) parent.lcn9_freq.setText("");
                          if(parent.lcn10_freq.getText().equals("0.000000")) parent.lcn10_freq.setText("");
                          if(parent.lcn11_freq.getText().equals("0.000000")) parent.lcn11_freq.setText("");
                          if(parent.lcn12_freq.getText().equals("0.000000")) parent.lcn12_freq.setText("");
                          if(parent.lcn13_freq.getText().equals("0.000000")) parent.lcn13_freq.setText("");
                          if(parent.lcn14_freq.getText().equals("0.000000")) parent.lcn14_freq.setText("");
                          if(parent.lcn15_freq.getText().equals("0.000000")) parent.lcn15_freq.setText("");


                          int tgtimeout = bb3.getInt(372);
                          switch(tgtimeout) {
                            case  100  :
                              parent.vtimeout.setSelectedIndex(0);
                            break;
                            case  250  :
                              parent.vtimeout.setSelectedIndex(1);
                            break;
                            case  500  :
                              parent.vtimeout.setSelectedIndex(2);
                            break;
                            case  1000  :
                              parent.vtimeout.setSelectedIndex(3);
                            break;
                            case  1500  :
                              parent.vtimeout.setSelectedIndex(4);
                            break;
                            case  2000  :
                              parent.vtimeout.setSelectedIndex(5);
                            break;
                            case  3000  :
                              parent.vtimeout.setSelectedIndex(6);
                            break;
                            case  5000  :
                              parent.vtimeout.setSelectedIndex(7);
                            break;
                            case  10000  :
                              parent.vtimeout.setSelectedIndex(8);
                            break;
                            case  30000  :
                              parent.vtimeout.setSelectedIndex(9);
                            break;
                            default :
                              parent.vtimeout.setSelectedIndex(5);
                            break;
                          }


                          //int rfmg = bb3.getInt(296)-4;
                          //if(rfmg<0) rfmg=0;
                          //parent.rfmaxgain.setSelectedIndex( rfmg ); 

                          if(bb3.getShort(130)==1) b=true;
                              else b=false;
                          parent.allow_unknown_tg_cb.setSelected(b); 

                          if(bb3.getInt(196)==1) b=true;
                              else b=false;
                          parent.enable_leds.setSelected(b); 

                          if(bb3.getInt(68)==1) b=true;
                              else b=false;
                          parent.roaming.setSelected(b); 
                          if(b) {
                            parent.no_voice_panel.setVisible(true);
                          }
                          else {
                            parent.no_voice_panel.setVisible(false);
                          }


                          int no_voice_secs = bb3.getInt(280);
                          parent.no_voice_secs.setText( String.format("%d", no_voice_secs) );


                          parent.update_op_mode(op_mode);



                          /*
                          byte[] result=new byte[64];
                          String cmd = "mac_id\r\n";  
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(100);
                          rlen=serial_port.readBytes( result, 64);

                          String macid = new String(result,0,16).trim();
                          if(macid.startsWith("0x")) {
                            System.out.println("mac_id:"+macid +":");
                            parent.sys_mac_id = macid;
                          }
                          */

                        } catch(Exception e) {
                          e.printStackTrace();
                        }

                      }
                      else {
                        try {

                          cmd = ""; 
                          parent.setStatus("writing configuration to flash..."); 



                          /*
                          if( parent.roaming.isSelected() ) {
                            byte[] result=new byte[64];
                            cmd = "save_alt_cc\r\n";
                            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                            SLEEP(3000);
                            rlen=serial_port.readBytes( result, 64);
                          }
                          */
                          byte[] result=new byte[64];

                          cmd = "logging -999"+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);

                          result=new byte[64];
                          //stop following
                          cmd = "f 0"+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);

                          int reset_on_save=0;


                          int op_mode = parent.op_mode.getSelectedIndex();
                          int opmode=1;
                          if(op_mode==0) opmode=1; //P25
                          if(op_mode==1) opmode=2; //DMR
                          if(op_mode==2) opmode=5; //TDMA CC
                          if(op_mode==3) opmode=3; //NXDN4800

                          if(op_mode==2) parent.is_dmr_mode=1;
                            else parent.is_dmr_mode=0;

                          if(parent.is_dmr_mode==1) { 
                            try {
                              parent.frequency_tf1.setText( parent.lcn1_freq.getText() ); 
                            } catch(Exception e) {
                            }
                          }

                          result=new byte[64];
                          cmd = "op_mode "+opmode+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);


                          int vgastep = Integer.valueOf( parent.vga_step.getText() );

                          result=new byte[64];
                          cmd = "vga_step "+vgastep+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);


                          int tgzone = 0;
                          try {
                            if( parent.z1.isSelected() ) tgzone |= 0x01;
                            if( parent.z2.isSelected() ) tgzone |= 0x02;
                            if( parent.z3.isSelected() ) tgzone |= 0x04;
                            if( parent.z4.isSelected() ) tgzone |= 0x08;
                            if( parent.z5.isSelected() ) tgzone |= 0x10;
                            if( parent.z6.isSelected() ) tgzone |= 0x20;
                            if( parent.z7.isSelected() ) tgzone |= 0x40;
                            if( parent.z8.isSelected() ) tgzone |= 0x80;
                            if( parent.z9.isSelected() ) tgzone |= 0x100;
                            if( parent.z10.isSelected() ) tgzone |= 0x200;
                            if( parent.z11.isSelected() ) tgzone |= 0x400;
                            if( parent.z12.isSelected() ) tgzone |= 0x800;
                          } catch(Exception e) {
                          }
                          result=new byte[64];
                          cmd = "tgzone "+tgzone+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);



                          /*
                          int chflt = parent.ch_flt.getSelectedIndex();
                          result=new byte[64];
                          cmd = "ch_flt "+chflt+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          */



                          int demod_type = parent.demod.getSelectedIndex();
                          result=new byte[64];
                          cmd = "demod "+demod_type+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);

                          int vga = parent.vga_gain.getSelectedIndex();
                          result=new byte[64];
                          cmd = "vga_gain "+(vga-1)+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);

                          String freq_to_use="";
                          double freq_d = 859.9625;

                          //parent.freq.setText( "Freq: "+parent.frequency_tf1.getText().trim() );
                          freq_to_use=parent.frequency_tf1.getText().trim();


                          op_mode = parent.op_mode.getSelectedIndex();
                          opmode=1;
                          if(op_mode==0) opmode=1;
                          if(op_mode==1) opmode=2;
                          if(op_mode==2) opmode=5;

                          if(op_mode==2) parent.is_dmr_mode=1;
                            else parent.is_dmr_mode=0;

                          if(parent.is_dmr_mode==1) {
                            try {
                              freq_to_use = parent.lcn1_freq.getText(); 
                              parent.frequency_tf1.setText(freq_to_use);
                            } catch(Exception e) {
                            }
                          }

                          try {
                            freq_d = new Double(freq_to_use).doubleValue();
                          } catch(Exception e) {
                            freq_to_use="859.9625";
                          }

                          result=new byte[64];
                          cmd = "freq "+freq_to_use+"\r\n";  

                          if( !is_valid_freq(freq_d) ) {
                            JOptionPane.showMessageDialog(parent, "Invalid Frequency "+freq_to_use);
                            freq_to_use = "859.9625";
                            cmd = "freq "+freq_to_use+"\r\n";  
                          }

                          SLEEP(read_serial_delay);
                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);


                          result=new byte[64];
                          cmd = "enc_timeout "+parent.enc_timeout.getText()+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          result=new byte[64];
                          cmd = "agc_max_gain "+parent.audio_agc_max.getText()+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          result=new byte[64];
                          cmd = "enc_count "+parent.enc_count.getText()+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          result=new byte[64];
                          cmd = "vga_target "+parent.vga_target.getText()+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          result=new byte[64];
                          cmd = "no_voice_roam_sec "+parent.no_voice_secs.getText()+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          result=new byte[64];
                          cmd = "vol "+(float) parent.lineout_vol_slider.getValue()/100.0f+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          result=new byte[64];
                          cmd = "bt_gain "+(float) parent.bt_vol_slider1.getValue()/100.0f+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          try {
                            result=new byte[64];
                            cmd = "p25_tone_vol "+(float) Float.valueOf( parent.p25_tone_vol.getText() )+"\r\n";
                            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                            SLEEP(read_serial_delay);
                            rlen=serial_port.readBytes( result, 64);
                            System.out.println("result: "+new String(result) );
                          } catch(Exception e) {
                            e.printStackTrace();
                          }



                          int vt = parent.vtimeout.getSelectedIndex();
                          int vto = 1000;
                          switch(vt) {
                            case  0  :
                              vto = 100;
                            break;
                            case  1  :
                              vto = 250;
                            break;
                            case  2  :
                              vto = 500;
                            break;
                            case  3  :
                              vto = 1000;
                            break;
                            case  4  :
                              vto = 1500;
                            break;
                            case  5  :
                              vto = 2000;
                            break;
                            case  6  :
                              vto = 3000;
                            break;
                            case  7  :
                              vto = 5000;
                            break;
                            case  8  :
                              vto = 10000;
                            break;
                            case  9  :
                              vto = 30000;
                            break;
                            default :
                              vto = 2000;
                            break;
                          }
                          result=new byte[64];
                          cmd = "tgtimeout "+vto+"\r\n";  
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          result=new byte[64];


                          //cmd = "bt_reset "+parent.bluetooth_reset.getText()+"\r\n";
                          cmd = "bt_reset 0"+"\r\n";  //always disabled for now
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(50);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );



                          boolean b = parent.vrep.isSelected();
                          if(b) cmd = "vrep 1\r\n";
                            else cmd = "vrep 0\r\n"; 

                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(50);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          b = parent.p2_lsm.isSelected();
                          if(b) cmd = "p2_lsm 1\r\n";
                            else cmd = "p2_lsm 0\r\n"; 

                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(50);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );



                          b = parent.controlchannel.isSelected();
                          if(b) cmd = "is_control 1\r\n";
                            else cmd = "is_control 0\r\n"; 

                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(50);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          int rfhyst = parent.rf_hyst.getSelectedIndex(); 
                          int hyst = 1;
                          if(rfhyst==0) hyst=3;
                          else if(rfhyst==1) hyst=6;
                          else if(rfhyst==2) hyst=10;
                          else if(rfhyst==3) hyst=16;
                          else if(rfhyst==4) hyst=22;
                          else hyst=6;

                          result=new byte[64];
                          cmd = "rf_hyst "+hyst+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(50);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          int p1sync = new Integer( parent.p1_sync_thresh.getText() ).intValue();
                          int p2sync = new Integer( parent.p2_sync_thresh.getText() ).intValue();

                          result=new byte[64];
                          cmd = "p1_ssync "+p1sync+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(50);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          result=new byte[64];
                          cmd = "p2_ssync "+p2sync+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(50);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          result=new byte[64];

                          b = parent.en_bluetooth_cb.isSelected();
                          if(b) cmd = "bluetooth 1\r\n";
                            else cmd = "bluetooth 0\r\n"; 

                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(50);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          b = parent.allow_unknown_tg_cb.isSelected();
                          if(b) cmd = "en_unknown_tg 1\r\n";
                            else cmd = "en_unknown_tg 0\r\n"; 

                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(50);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          b = parent.en_encout.isSelected();
                          if(b) cmd = "en_encout 1\r\n";
                            else cmd = "en_encout 0\r\n"; 

                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          b = parent.en_p2_tones.isSelected();
                          if(b) cmd = "en_p2_tones 1\r\n";
                            else cmd = "en_p2_tones 0\r\n"; 

                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          b = parent.enc_mode.isSelected();
                          if(b) cmd = "enc_mode 1\r\n";
                            else cmd = "enc_mode 0\r\n"; 

                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          b = parent.allow_tg_pri_int.isSelected();
                          if(b) cmd = "en_tg_pri_int 1\r\n";
                            else cmd = "en_tg_pri_int 0\r\n"; 

                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          b = parent.en_tg_int_tone.isSelected();
                          if(b) cmd = "en_tg_int_tone 1\r\n";
                            else cmd = "en_tg_int_tone 0\r\n"; 

                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          b = parent.roaming.isSelected();
                          if(b) cmd = "roaming 1\r\n";
                            else cmd = "roaming 0\r\n"; 

                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          result=new byte[64];
                          b = parent.enable_leds.isSelected();
                          if(b) cmd = "led_mode 1\r\n";
                            else cmd = "led_mode 0\r\n"; 
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          result=new byte[64];
                          cmd = "sys_name "+parent.system_alias.getText()+"\r\n"; 
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          int roam_ret_to_cc = 0;
                          if( parent.roaming_ret_to_cc.isSelected() ) roam_ret_to_cc = 1;

                          result=new byte[64];
                          cmd = "roam_ret_to_cc "+roam_ret_to_cc+"\r\n"; 
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          int optb1 = 0;
                          if( parent.single_click_opt1.isSelected() ) optb1 = 0;
                          else if( parent.single_click_opt2.isSelected() ) optb1 = 1;
                          else if( parent.single_click_opt3.isSelected() ) optb1 = 2;
                          else if( parent.single_click_opt4.isSelected() ) optb1 = 3;
                          else if( parent.single_click_opt5.isSelected() ) optb1 = 4;
                          else if( parent.single_click_opt6.isSelected() ) optb1 = 5;

                          int optb2 = 0;
                          if( parent.double_click_opt1.isSelected() ) optb2 = 0;
                          else if( parent.double_click_opt2.isSelected() ) optb2 = 1;
                          else if( parent.double_click_opt3.isSelected() ) optb2 = 2;
                          else if( parent.double_click_opt4.isSelected() ) optb2 = 3;
                          else if( parent.double_click_opt5.isSelected() ) optb2 = 4;
                          else if( parent.double_click_opt6.isSelected() ) optb2 = 5;

                          int optb3 = 0;
                          if( parent.triple_click_opt1.isSelected() ) optb3 = 0;
                          else if( parent.triple_click_opt2.isSelected() ) optb3 = 1;
                          else if( parent.triple_click_opt3.isSelected() ) optb3 = 2;
                          else if( parent.triple_click_opt4.isSelected() ) optb3 = 3;
                          else if( parent.triple_click_opt5.isSelected() ) optb3 = 4;
                          else if( parent.triple_click_opt6.isSelected() ) optb3 = 5;

                          int optb4 = 0;
                          if( parent.quad_click_opt1.isSelected() ) optb4 = 0;
                          else if( parent.quad_click_opt2.isSelected() ) optb4 = 1;
                          else if( parent.quad_click_opt3.isSelected() ) optb4 = 2;
                          else if( parent.quad_click_opt4.isSelected() ) optb4 = 3;
                          else if( parent.quad_click_opt5.isSelected() ) optb4 = 4;
                          else if( parent.quad_click_opt6.isSelected() ) optb4 = 5;

                          int mixgain = parent.mixgain.getSelectedIndex();

                          result=new byte[64];
                          cmd = "mgain "+(mixgain-1)+"\r\n"; 
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          int rfg = parent.rfgain.getSelectedIndex();

                          result=new byte[64];
                          cmd = "lna_gain "+(rfg-1)+"\r\n"; 
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          result=new byte[64];
                          cmd = "but1_cfg "+optb1+"\r\n"; 
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          result=new byte[64];
                          cmd = "but2_cfg "+optb2+"\r\n"; 
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          result=new byte[64];
                          cmd = "but3_cfg "+optb3+"\r\n"; 
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          result=new byte[64];
                          cmd = "but4_cfg "+optb4+"\r\n"; 
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );

                          result=new byte[64];
                          int skip_tg_to = 60; 
                          try {
                            skip_tg_to = Integer.valueOf( parent.skip_tg_to.getText() );
                          } catch(Exception e) {
                          }
                          cmd = "skip_tg_to "+skip_tg_to+"\r\n"; 
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );


                          //int maxgain = parent.rfmaxgain.getSelectedIndex()+4;
                          //cmd = "rf_max_gain "+maxgain+"\r\n"; 
                          //serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          //SLEEP(read_serial_delay);
                          //rlen=serial_port.readBytes( result, 64);
                          //System.out.println("result: "+new String(result) );

                          //do this one last
                          //cmd = "is_control 1\r\n";
                          //serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          //SLEEP(write_serial_delay);
                          //rlen=serial_port.readBytes( result, 64);
                          //System.out.println("result: "+new String(result) );
                          //SLEEP(read_serial_delay);

                          result=new byte[64];
                          int dmr_sys_id = 1; 
                          try {
                            dmr_sys_id = new Integer( parent.dmr_sys_id.getText() ).intValue();
                          } catch(Exception e) {
                          }
                          if(dmr_sys_id<=0) dmr_sys_id=1;

                          cmd = "dmr_sys_id "+dmr_sys_id+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);


                          int dmr_config = 0;
                          if(parent.dmr_slot1.isSelected()) dmr_config |= DMR_SLOT1;
                          if(parent.dmr_slot2.isSelected()) dmr_config |= DMR_SLOT2;
                          if(parent.dmr_cc_en1.isSelected()) dmr_config |= DMR_CC1;
                          if(parent.dmr_cc_en2.isSelected()) dmr_config |= DMR_CC2;
                          if(parent.dmr_cc_en3.isSelected()) dmr_config |= DMR_CC3;
                          if(parent.dmr_cc_en4.isSelected()) dmr_config |= DMR_CC4;
                          if(parent.dmr_cc_en5.isSelected()) dmr_config |= DMR_CC5;
                          if(parent.dmr_cc_en6.isSelected()) dmr_config |= DMR_CC6;
                          if(parent.dmr_cc_en7.isSelected()) dmr_config |= DMR_CC7;
                          if(parent.dmr_cc_en8.isSelected()) dmr_config |= DMR_CC8;
                          if(parent.dmr_cc_en9.isSelected()) dmr_config |= DMR_CC9;
                          if(parent.dmr_cc_en10.isSelected()) dmr_config |= DMR_CC10;
                          if(parent.dmr_cc_en11.isSelected()) dmr_config |= DMR_CC11;
                          if(parent.dmr_cc_en12.isSelected()) dmr_config |= DMR_CC12;
                          if(parent.dmr_cc_en13.isSelected()) dmr_config |= DMR_CC13;
                          if(parent.dmr_cc_en14.isSelected()) dmr_config |= DMR_CC14;
                          if(parent.dmr_cc_en15.isSelected()) dmr_config |= DMR_CC15;
                          if(parent.dmr_conplus.isSelected()) dmr_config |= DMR_ISCC;

                          result=new byte[64];
                          cmd = "dmr_config "+String.format("%08x", dmr_config)+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);

                          if( parent.lcn1_freq.getText().equals("") ) parent.lcn1_freq.setText("0.000000");
                          if( parent.lcn2_freq.getText().equals("") ) parent.lcn2_freq.setText("0.000000");
                          if( parent.lcn3_freq.getText().equals("") ) parent.lcn3_freq.setText("0.000000");
                          if( parent.lcn4_freq.getText().equals("") ) parent.lcn4_freq.setText("0.000000");
                          if( parent.lcn5_freq.getText().equals("") ) parent.lcn5_freq.setText("0.000000");
                          if( parent.lcn6_freq.getText().equals("") ) parent.lcn6_freq.setText("0.000000");
                          if( parent.lcn7_freq.getText().equals("") ) parent.lcn7_freq.setText("0.000000");
                          if( parent.lcn8_freq.getText().equals("") ) parent.lcn8_freq.setText("0.000000");
                          if( parent.lcn9_freq.getText().equals("") ) parent.lcn9_freq.setText("0.000000");
                          if( parent.lcn10_freq.getText().equals("") ) parent.lcn10_freq.setText("0.000000");
                          if( parent.lcn11_freq.getText().equals("") ) parent.lcn11_freq.setText("0.000000");
                          if( parent.lcn12_freq.getText().equals("") ) parent.lcn12_freq.setText("0.000000");
                          if( parent.lcn13_freq.getText().equals("") ) parent.lcn13_freq.setText("0.000000");
                          if( parent.lcn14_freq.getText().equals("") ) parent.lcn14_freq.setText("0.000000");
                          if( parent.lcn15_freq.getText().equals("") ) parent.lcn15_freq.setText("0.000000");

                          result=new byte[64];
                          cmd = "dmr_lcn1 "+String.format("%3.6f", Double.valueOf(parent.lcn1_freq.getText()))+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          result=new byte[64];
                          cmd = "dmr_lcn2 "+String.format("%3.6f", Double.valueOf(parent.lcn2_freq.getText()))+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          result=new byte[64];
                          cmd = "dmr_lcn3 "+String.format("%3.6f", Double.valueOf(parent.lcn3_freq.getText()))+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          result=new byte[64];
                          cmd = "dmr_lcn4 "+String.format("%3.6f", Double.valueOf(parent.lcn4_freq.getText()))+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          result=new byte[64];
                          cmd = "dmr_lcn5 "+String.format("%3.6f", Double.valueOf(parent.lcn5_freq.getText()))+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          result=new byte[64];
                          cmd = "dmr_lcn6 "+String.format("%3.6f", Double.valueOf(parent.lcn6_freq.getText()))+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          result=new byte[64];
                          cmd = "dmr_lcn7 "+String.format("%3.6f", Double.valueOf(parent.lcn7_freq.getText()))+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          result=new byte[64];
                          cmd = "dmr_lcn8 "+String.format("%3.6f", Double.valueOf(parent.lcn8_freq.getText()))+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          result=new byte[64];
                          cmd = "dmr_lcn9 "+String.format("%3.6f", Double.valueOf(parent.lcn9_freq.getText()))+"\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          cmd = "dmr_lcn10 "+String.format("%3.6f", Double.valueOf(parent.lcn10_freq.getText()))+"\r\n";
                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          cmd = "dmr_lcn11 "+String.format("%3.6f", Double.valueOf(parent.lcn11_freq.getText()))+"\r\n";
                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          cmd = "dmr_lcn12 "+String.format("%3.6f", Double.valueOf(parent.lcn12_freq.getText()))+"\r\n";
                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          cmd = "dmr_lcn13 "+String.format("%3.6f", Double.valueOf(parent.lcn13_freq.getText()))+"\r\n";
                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          cmd = "dmr_lcn14 "+String.format("%3.6f", Double.valueOf(parent.lcn14_freq.getText()))+"\r\n";
                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);
                          cmd = "dmr_lcn15 "+String.format("%3.6f", Double.valueOf(parent.lcn15_freq.getText()))+"\r\n";

                          result=new byte[64];
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(write_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(read_serial_delay);



                          if(parent.is_fast_mode==1) {
                            result=new byte[64];
                            cmd = "fast_mode\r\n";
                            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                            SLEEP(write_serial_delay);
                            rlen=serial_port.readBytes( result, 64);
                            System.out.println("result: "+new String(result) );
                            SLEEP(read_serial_delay);
                          }

                          result=new byte[64];
                          if(reset_on_save==1) {
                            cmd = "save 1\r\n";
                          }
                          else {
                            cmd = "save\r\n";
                          }
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(read_serial_delay);
                          rlen=serial_port.readBytes( result, 64);
                          System.out.println("result: "+new String(result) );
                          SLEEP(2000);

                          parent.setStatus("sys_config update ok."); 


                          parent.do_write_config=0;


                          if(reset_on_save==1) {
                            parent.is_connected=0;
                            parent.do_connect=1;
                          }
                          else {
                            parent.do_read_config=1;
                          }
                        } catch(Exception e) {
                          e.printStackTrace();
                        }
                      }


                    }
                    else {
                      parent.setStatus("sys_config crc not ok."); 
                      System.out.println(String.format("sys_config crc NOT OK. Resetting device.  0x%08x, 0x%08x", crc, config_crc));
                        //parent.is_connected=0;
                        //parent.do_connect=1;
                        //SLEEP(1000);
                        //return;

                      //if(did_crc_reset==0) {
                      //  did_crc_reset=1;

                        parent.setStatus("\r\nresetting device");
                        cmd = "system_reset\r\n";
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                        try {
                          SLEEP(5000*5);
                        } catch(Exception e) {
                        }

                      //}

                      parent.is_connected=0;
                      parent.do_connect=1;
                    }


                    parent.setProgress(-1); 

                    return; 
                  }

                  //parent.setStatus("read "+offset+" bytes");
                  parent.setStatus("read sys_config."); 
                  //parent.setProgress( (int) ((float)offset/552.0f * 100.0) );
                  parent.setProgress( (int) ((float)offset/1024.0f * 100.0) );
                }
              }
              else {
                //flush buffers
                byte[] b = new byte[ serial_port.bytesAvailable()+1 ];
                if(b.length>0)serial_port.readBytes( b, b.length-1 );  //flush buffer
              }
          }


        }

    } //while(true) 
  } catch (Exception e) {
    e.printStackTrace();
  }
}


}
