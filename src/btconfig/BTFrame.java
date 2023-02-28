
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

import java.io.*;
import java.nio.*;
import java.awt.*;
import javax.swing.filechooser.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fazecast.jSerialComm.*;
import net.sourceforge.lame.mp3.*;
import net.sourceforge.lame.lowlevel.*;
//import net.sourceforge.lame.mpeg.*;

import javax.sound.sampled.*;

import java.util.prefs.Preferences;

import javax.swing.JColorChooser;
import java.awt.Color;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class BTFrame extends javax.swing.JFrame {

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
class updateTask extends java.util.TimerTask
{

    public void run()
    {
      try {

      try {
        if(aud_archive!=null) aud_archive.tick_rdio(src_uid);
      } catch(Exception e) {
      }
      try {
        if(aud_archive!=null) aud_archive.tick_bcalls(src_uid);
      } catch(Exception e) {
      }

       long total = rt.totalMemory();
       long free = rt.freeMemory();

       long total_mb = total / (1024*1024);
       long free_mb = free / (1024*1024);
       long used_mb = total_mb - free_mb; 

       //if( used_mb > 200 ) {
        // rt.gc();
       //}

        int aud_check_time = 55;
        if(  aud!=null && ( new java.util.Date().getTime() - audio_tick_start ) > aud_check_time) {
          aud.audio_tick();
          audio_tick_start = new java.util.Date().getTime();

        }

        if(status_timeout>0) {
          status_timeout--;
        }

        if(  status_timeout==0 && new java.util.Date().getTime() - status_time  > 2000) {
          status_time = new java.util.Date().getTime();
          setStatus("");
        }


        /*
        long usb_ctime = new java.util.Date().getTime();
        if(wdog_time==0 || usb_ctime - wdog_time > 5000) {
          wdog_time = usb_ctime;
          if(sys_config!=null && is_mac_osx==0) sys_config.do_usb_watchdog(serial_port);
          //System.out.println("\r\nusb watchdog");
        }
        */

        if(bluetooth_streaming_timer>0) {
          bluetooth_streaming_timer--;
          if(bluetooth_streaming_timer==0) {
            bluetooth_streaming=0;
          }
        }

        if(command_input_timeout>0) {
           command_input_timeout--;
          if(command_input_timeout==0) {
            command_input=0;
          }
        }

        if(p25_status_timeout>0) {
          p25_status_timeout--;
          if(p25_status_timeout==0 || do_write_config==1) {
            p25_status_timeout=3000;
            //status.setText("");
            l3.setText("");
            tg_indicator.setBackground(java.awt.Color.black);
            tg_indicator.setForeground(java.awt.Color.black);
            sq_indicator.setForeground( java.awt.Color.black );
            sq_indicator.setBackground( java.awt.Color.black );

            clear_sys_info();
            l3.setText("NO SIG");

            do_synced=false;

            rx_state=0;
            skip_bytes=0;

          }
          else {
            do_synced=true;
          }
        }


        if(is_connected==1 && do_test_freqs==1 && do_update_firmware==0) {
          do_test_freqs=0;
          if(roaming_tests!=null) roaming_tests.test_selected_freqs(parent,serial_port);
        }

        if(is_connected==1 && do_roam_freq==1 && do_update_firmware==0) {
          do_roam_freq=0;
          if(roaming_tests!=null) {
            int[] rows = freq_table.getSelectedRows();
            if(rows.length>0) {
              freq_table.setRowSelectionInterval(rows[0],rows[0]);


              String freq_to_use = (String) freq_table.getModel().getValueAt(rows[0],3);
              String demod_str = (String) freq_table.getModel().getValueAt(rows[0],7);

              double d = 0.0; 
              try {
                d = new Double(freq_to_use).doubleValue();
                freq_to_use = String.format("%3.6f", d);

                if(freq_table.getModel().getValueAt(rows[0],1)==null) freq_table.getModel().setValueAt("",rows[0],1);
                if(freq_table.getModel().getValueAt(rows[0],8)==null) freq_table.getModel().setValueAt("",rows[0],8);
                if(freq_table.getModel().getValueAt(rows[0],9)==null) freq_table.getModel().setValueAt("",rows[0],9);

                if(demod_str==null) demod_str="";
                if(demod_str.contains("LSM")) demod.setSelectedIndex(0);
                if(demod_str.contains("CQPSK")) demod.setSelectedIndex(1);

                frequency_tf1.setText(freq_to_use);

                do_read_config=1;
                do_write_config=1;
                cpanel.reset_ref_est();

                //current_sys_id = 0;
                //current_wacn_id = 0; 
                wacn.setText("");
                sysid.setText("");
                nac.setText("");

                if(prefs!=null) {
                  String str1 = (String)  parent.freq_table.getModel().getValueAt(rows[0],8);
                  String str2 = (String) parent.freq_table.getModel().getValueAt(rows[0],9);
                  String str3 = (String) parent.freq_table.getModel().getValueAt(rows[0],1);
                  if(str1!=null && str1.length()>0 && str2!=null && str2.length()>0 && str3!=null && str3.length()>0) {
                    if(prefs!=null) parent.prefs.put("city_state_"+freq_to_use.trim(), str1+","+str2+"_"+str3 );
                  }
                }
              } catch(Exception e) {
                e.printStackTrace();
              }
            }
          }
        }

        //auto re-connect?
        if(serial_port!=null && !serial_port.isOpen() && is_connected==1 && do_update_firmware==0) {
          is_connected=0;
          try {
            setStatus("device reset detected");
            SLEEP(100);
          } catch(Exception e) {
                e.printStackTrace();
          }
          do_connect=1;
          do_read_config=1;
            rx_state=0;
            skip_bytes=0;

        }


        if(do_zipsearch==1) {
          do_zipsearch=0;
          if(zs==null) zs = new zipsearch(parent);
          String[] args = new String[2];
          String zip_str = "";
          try {
            zip_str = new Integer(zipcode.getText().trim()).toString();
          } catch(Exception e) {
                e.printStackTrace();
          }
          args[0] = zip_str; 
          args[1] = search_radius.getText().trim();
          zs.search(args);
        }

        if(do_zipsearch2==1) {
          do_zipsearch2=0;
          if(zs==null) zs = new zipsearch(parent);
          String[] args = new String[3];
          args[0] = city.getText().trim();
          args[1] = state.getText().trim();
          args[2] = search_radius.getText().trim();
          zs.search2(args);
        }

        if(do_read_config==1 && serial_port!=null && is_connected==1) {
          if(sys_config==null) sys_config = new SYSConfig(parent);
          if(sys_config!=null) {

            String cmd= new String("en_voice_send 0\r\n");
            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
            cmd= new String("logging -999\r\n");
            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

            sys_config.read_sysconfig(parent, serial_port);

            cmd= new String("en_voice_send 1\r\n");
            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
            SLEEP(600);
            cmd= new String("logging 0\r\n");
            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

            do_read_roaming=1;
          }
        }


        if(do_agc_update==1) {
          do_agc_update=0;
            //System.out.println(evt);
        }

        if(do_restore_roaming==1 && is_connected==1 && do_update_firmware==0 && do_read_talkgroups==0) {

          try {
            JFileChooser chooser = new JFileChooser();

            File cdir = new File(home_dir);
            chooser.setCurrentDirectory(cdir);


            FileNameExtensionFilter filter = new FileNameExtensionFilter( "p25rx_roaming backups", "rom");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showDialog(parent, "Restore Roaming Records");

            BufferedInputStream bis=null;

            if(returnVal == JFileChooser.APPROVE_OPTION) {
              File file = chooser.getSelectedFile();
              bis = new BufferedInputStream(new FileInputStream(file));
              System.out.println("restoring roaming records from: " + file.getAbsolutePath()); 
            }

            if(bis!=null) {
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

              if(roaming_tests!=null) roaming_tests.restore_roaming(parent, bis, serial_port);



              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
            }
            setProgress(-1);
          } catch(Exception e) {
            //e.printStackTrace();
                e.printStackTrace();
          }

          do_restore_roaming=0;
        }

        //Import alias CSV file
        if(do_alias_import==1 && is_connected==1 && do_update_firmware==0 && do_read_talkgroups==0) {

          try {

            JFileChooser chooser = new JFileChooser();

            File cdir = new File(home_dir);
            chooser.setCurrentDirectory(cdir);


            FileNameExtensionFilter filter = new FileNameExtensionFilter( "p25rx_alias_import", "csv");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showDialog(parent, "Import CSV Alias Records");

            LineNumberReader lnr=null;

            if(returnVal == JFileChooser.APPROVE_OPTION) {
              File file = chooser.getSelectedFile();
              lnr = new LineNumberReader( new FileReader(file) );
              System.out.println("importing aliases from: " + file.getAbsolutePath()); 
            }

            if(lnr!=null) {
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

              if(tg_config==null) tg_config = new TGConfig(parent);
              alias.import_alias_csv(parent, lnr);

              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
            }
            setProgress(-1);
          } catch(Exception e) {
                e.printStackTrace();
            //e.printStackTrace();
          }

          do_alias_import=0;
        }

        //RESTORE talkgroup CSV file
        if(do_restore_tg_csv==1 && is_connected==1 && do_update_firmware==0 && do_read_talkgroups==0) {

          try {

            JFileChooser chooser = new JFileChooser();

            File cdir = new File(home_dir);
            if(parent.prefs!=null) {
              cdir = new File( parent.prefs.get("tg_backup_file_path", cdir.getAbsolutePath() ) );
            }
            chooser.setCurrentDirectory(cdir);


            FileNameExtensionFilter filter = new FileNameExtensionFilter( "p25rx_talkgroup backups", "csv","dsd");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showDialog(parent, "Import CSV Talk Group Records");

            LineNumberReader lnr=null;

            if(returnVal == JFileChooser.APPROVE_OPTION) {
              File file = chooser.getSelectedFile();
              lnr = new LineNumberReader( new FileReader(file) );
              System.out.println("importing talkgroups from: " + file.getAbsolutePath()); 
            }

            if(lnr!=null) {
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

              if(tg_config==null) tg_config = new TGConfig(parent);
              tg_config.import_talkgroups_csv(parent, lnr, serial_port);
              do_read_talkgroups=1;

              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
            }
            setProgress(-1);
          } catch(Exception e) {
            //e.printStackTrace();
                e.printStackTrace();
          }

          do_restore_tg_csv=0;
        }

        //RESTORE TGP file
        if(do_restore_tg==1 && is_connected==1 && do_update_firmware==0 && do_read_talkgroups==0) {

          try {

            JFileChooser chooser = new JFileChooser();

            File cdir = new File(home_dir);
            if(parent.prefs!=null) {
              cdir = new File( parent.prefs.get("tg_backup_file_path", cdir.getAbsolutePath() ) );
            }
            chooser.setCurrentDirectory(cdir);


            FileNameExtensionFilter filter = new FileNameExtensionFilter( "p25rx_talkgroup backups", "tgp");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showDialog(parent, "Restore Talk Group Records");

            BufferedInputStream bis=null;

            if(returnVal == JFileChooser.APPROVE_OPTION) {
              File file = chooser.getSelectedFile();
              bis = new BufferedInputStream(new FileInputStream(file));
              System.out.println("restoring talkgroups from: " + file.getAbsolutePath()); 
            }

            if(bis!=null) {
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

              if(tg_config==null) tg_config = new TGConfig(parent);
              tg_config.restore_talkgroups(parent, bis, serial_port);
              do_read_talkgroups=1;

              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
            }
            setProgress(-1);
          } catch(Exception e) {
            //e.printStackTrace();
                e.printStackTrace();
          }

          do_restore_tg=0;
        }


        if(firmware_checked==1) {
          check_firmware.setEnabled(false);
        }

        if(sig_meter_timeout>0) {
          sig_meter_timeout-=10;

          if(sig_meter_timeout<=0) {
            rssim1.setValue(-130,false);
            rssim2.setValue(-130,false);
            //p25_status_timeout=3000;
            sig_meter_timeout=1000;
          }
          else {
          }
        }

        if( is_connected==0 ) {
          send_tg.setEnabled(false);
          read_tg.setEnabled(false);
          discover.setEnabled(true);
          disconnect.setEnabled(false);
          read_config.setEnabled(false);
          write_config.setEnabled(false);

        }
        else {
          send_tg.setEnabled(true);
          read_tg.setEnabled(true);
          discover.setEnabled(false);
          disconnect.setEnabled(true);
          read_config.setEnabled(true);
          write_config.setEnabled(true);
        }

        if(is_connected==1 && do_update_firmware==1) {
          if(firmware_checked==0) {
            check_firmware.setEnabled(false);

              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

            BufferedInputStream bis = new BufferedInputStream( getClass().getResourceAsStream("/btconfig/p25rx-ii-main.aes") );
            new firmware_update().send_firmware(parent, bis, serial_port);
            setProgress(-1);
            //firmware_checked=1;
            //do_update_firmware=0;
              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
          }
        }
        else if(is_connected==1 && do_backup_roaming==1) {
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                if(roaming_tests!=null) roaming_tests.backup_roaming(parent, serial_port);
                setProgress(-1);
                do_backup_roaming=0;

              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        }
        else if(is_connected==1 && do_erase_roaming==1) {
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                if(roaming_tests!=null) roaming_tests.erase_roaming(parent, serial_port);
                setProgress(-1);
                do_erase_roaming=0;

              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        }
        else if(is_connected==1 && do_write_roaming_flash_only==1) {
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                if(roaming_tests!=null) roaming_tests.write_roaming_flash(parent, serial_port);
                setProgress(-1);
                do_write_roaming_flash_only=0;

              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        }
        else if(is_connected==1 && do_append_roaming==1) {
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                if(roaming_tests!=null) roaming_tests.append_roaming(parent, serial_port);
                setProgress(-1);
                do_append_roaming=0;

              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        }
        else if(is_connected==1 && do_update_roaming==1) {
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                if(roaming_tests!=null) roaming_tests.send_roaming(parent, serial_port, 0);
                setProgress(-1);
                do_update_roaming=0;

              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        }
        else if(is_connected==1 && do_update_talkgroups==1) {
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);

              try {
                if(tg_config==null) tg_config = new TGConfig(parent);
                tg_config.send_talkgroups(parent, serial_port);
                setProgress(-1);
              } catch(Exception e) {
              }
              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);

              do_update_talkgroups=0;
        }
        else if(is_connected==1 && do_update_firmware==0 && do_read_roaming==1 && skip_bytes==0) {
          String cmd= new String("en_voice_send 0\r\n");
          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
          cmd= new String("logging -999\r\n");
          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

            if(roaming_tests!=null) roaming_tests.read_roaming(parent, serial_port);
            setProgress(-1);

            //if(parent.zs!=null) {
              //parent.zs.search3();
              //do_read_roaming=0;
            //}

          if(do_read_roaming==0) {
              SLEEP(100);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
          }
        }
        else if(is_connected==1 && do_update_firmware==0 && do_read_talkgroups==1 && skip_bytes==0) {
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
          if(tg_config==null) tg_config = new TGConfig(parent);
          tg_config.read_talkgroups(parent, serial_port);
          setProgress(-1);

          if(do_read_talkgroups==0) {
              SLEEP(100);
              cmd= new String("en_voice_send 1\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);
              cmd= new String("logging 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
          }

          if(do_update_firmware2==1) {
            do_update_firmware2=0;
            do_update_firmware=1;
          }

          status.setText(system_alias.getText());
          status.setVisible(true);
        }
        else if(is_connected==0 && do_connect==1) {

            serial_port = find_serial_port();

            macid.setVisible(false);
            macid.setText("");

            if(serial_port==null) {
              setStatus("\r\ndiscovering device.  Please wait...");
              SLEEP(600);

            }
              rx_state=0;
              skip_bytes=0;

            if(serial_port!=null && serial_port.openPort(200)==false) {
              setStatus("\r\nserial port busy. please wait. retrying....");
            }
            else if(serial_port!=null) {
              do_connect=0;

              serial_port_name = serial_port.getSystemPortName();
              serial_port.setBaudRate( 1000000 ); //this probably doesn't really matter
              serial_port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 500, 0);
              //serial_port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
              //serial_port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

              is_connected=1;
              SLEEP(600);

              check_firmware.setEnabled(true);

                do_read_talkgroups=1;
                do_read_roaming=1;
                do_read_config=1;

              discover.setEnabled(false);
              int baudrate = serial_port.getBaudRate();

              byte[] result=new byte[1024];
              String cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);
              int rlen=serial_port.readBytes( result, 1024);
              //System.out.println("result: "+new String(result) );
              result=new byte[1024];
              cmd= new String("en_voice_send 0\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);
              rlen=serial_port.readBytes( result, 1024);


              result=new byte[64];
              cmd= new String("logging -999\r\n");
              serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
              SLEEP(100);
              rlen=serial_port.readBytes( result, 64);
              //System.out.println("result: "+new String(result) );

              for(int i=0;i<15;i++) {

                  result=new byte[64];
                  String mcmd = "mac_id\r\n";  
                  serial_port.writeBytes( mcmd.getBytes(), mcmd.length(), 0);
                  SLEEP(100);
                  rlen=serial_port.readBytes( result, 64);

                  String mid = ""; 
                  try {
                    mid = mid.replace(" ","_");
                    mid = new String(result,0,16).trim();
                  } catch(Exception e) {
                    e.printStackTrace();
                    mid="";
                  }
                  if(mid.startsWith("0x") && mid.length()==14) {
                    System.out.println("mac_id:"+mid +":");
                    parent.sys_mac_id = mid;

                    update_prefs();

                    open_audio_output_files();
                    macid.setVisible(true);
                    macid.setText("MAC: "+sys_mac_id);

                    edit_display_view.setEnabled(true);

                    if(alias==null) alias = new Alias(parent, parent.sys_mac_id, document_dir);

                    break;
                  }

                  if(i==9) {
                    System.out.println("mac_id_not_good:"+mid +":");
                    //JOptionPane.showMessageDialog(parent, "Couldn't find device serial number.  Closing application.", "ok", JOptionPane.OK_OPTION);
                    //System.exit(0);
                    serial_port=null;
                    do_connect=1;
                    is_connected=0;
                    return;
                  }

                  System.out.println("error reading serial number.  Retry "+i);
                    result=new byte[4096];
                    cmd= new String("en_voice_send 0\r\n");
                    serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                    SLEEP(500);
                    rlen=serial_port.readBytes( result, 4096);
                    SLEEP(500);
                    rlen=serial_port.readBytes( result, 4096);
              }





            }
        }
        else if(is_connected==1 && do_toggle_record==1 && skip_bytes==0) {
          do_toggle_record=0;

          SLEEP(100);
          String cmd= new String("logging 0\r\n");
          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);


          toggle_recording( !record_to_mp3.isSelected() );

        }
        else if(is_connected==1 && do_update_firmware==0) {
          avail = serial_port.bytesAvailable();
          str_idx=0;

          //if( (rx_state>0 && avail>=32) || (rx_state==0 && avail>0 && skip_bytes==0) ) {
          if( avail>0 ) {


            try {
              int len = serial_port.readBytes(b, avail);

              if(len>256000) len = 256000;

              if(save_iq_len>0 && len>0 ) {
                try {
                  fos_iq.write(b,0,len);
                  save_iq_len-=len;
                  iq_out+=len;
                  setStatus("Wrote "+iq_out+" to IQ file");
                  len=0;
                  str_idx=0;
                  if(save_iq_len==0) {
                    fos_iq.close();
                  }
                } catch(Exception e) {
                }

              }

              int do_print=1;

              for(int i=0;i<len;i++) {

                if(skip_bytes>0 && rx_state==6) {
                    skip_bytes--;

                    try {
                      if(tdma_idx<256) tdma_bytes[tdma_idx++] = b[i];

                      if(tdma_idx==256) {
                        tdma_idx=0;
                        rx_state=0;
                        //fos_tdma.write(tdma_bytes,0,256);
                        //fos_tdma.flush();
                      }
                    } catch(Exception e) {
                      e.printStackTrace();
                    }
                }
                else if(skip_bytes>0 && rx_state==9) {
                  skip_bytes--;

                  if(skip_bytes>=0) pcm_bytes[pcm_idx++] = b[i];

                  if(skip_bytes==0) {
                    rx_state=0;
                    pcm_idx=0;

                    ByteBuffer p25_evt = ByteBuffer.wrap(pcm_bytes);
                    p25_evt.order(ByteOrder.LITTLE_ENDIAN);

                    byte op = p25_evt.get();
                    int p1 = p25_evt.getInt();
                    int p2 = p25_evt.getInt();
                    int p3 = p25_evt.getInt();
                    int p4 = p25_evt.getInt();
                    int crc = p25_evt.getInt();


                    int crc1 = crc32.crc32_range(pcm_bytes, 17);

                    if(crc1!=crc) {
                      return;
                    }

                    p25_status_timeout=6000;

                    String op_name="";

                    switch( (int) op) {
                      case  23  :
                        op_name = "CC_STATE_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                      break;
                      case  22  :
                        op_name = "BLUETOOTH_PWR_EVT";
                        addP25Event("\r\nEVT: "+String.format("op=%d, %s, %d, %d, %d", op, op_name, p1, p2, p4) ); 
                      break;
                      case  20  :
                        op_name = "DMR_VGRANT_EVT";
                        addP25Event("\r\nDMR_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                        if(tglog_e!=null && tglog_e.tg_trig_vgrant.isSelected()) do_meta();
                        handle_dmr_vgrant(p1,p2,p3,p4);
                      break;
                      case  13  :
                        op_name = "P25_VGRANT_EVT";
                        addP25Event("\r\nEVT: "+String.format("op=%d, %s, %d, %d, %d", op, op_name, p1, p2, p4) ); 
                        if(tglog_e!=null && tglog_e.tg_trig_vgrant.isSelected()) do_meta();
                      break;
                      case  14  :
                        op_name = "TDMA_VGRANT_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d", op, op_name, p1, p2, p4) ); 
                        if(tglog_e!=null && tglog_e.tg_trig_vgrant.isSelected()) do_meta();
                      break;
                      case  15  :
                        op_name = "ENCRYPTED_VOICE_EVT";
                        addP25Event("\r\nEVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                        is_enc=1;
                        //handle_encrypted( p1, p2, p3, p4);
                        if(tglog_e!=null && tglog_e.tg_trig_enc.isSelected());
                      break;

                      case  21  :
                        op_name = "FREQ_CHANGE_EVT";
                        addP25Event("\r\nEVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                      break;

                      case  12  :
                        op_name = "PATCH_TG_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                        handle_patch( p1, p2, p3, p4);
                      break;

                      case  1 :
                        src_uid = p1;
                        current_talkgroup = String.format("%s", p2);
                        op_name = "RID_EVT";
                        addP25Event("\r\nEVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                        handle_rid( p1, p2);
                      break;

                      case  19 :
                        op_name = "TG_CHANGED_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                      break;

                      case  8 :
                        op_name = "TG_PRI_INT_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                        handle_tg_pri(p1,p2,p3,p4);
                      break;
                      case  6 :
                        op_name = "ADJACENT_SINGLE_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                        handle_adj_mult(p1,p2,p3,p4);
                      break;
                      case  7 :
                        op_name = "ADJACENT_MULTIPLE_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                        handle_adj_mult(p1,p2,p3,p4);
                      break;

                      case  3 :
                        op_name = "EMERGENCY_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                        handle_emergency( p1, p2, p3);
                      break;
                      case  4 :
                        op_name = "AFFILIATION_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                        handle_affiliation( p1, p2, p3);
                      break;

                      case  17 :
                        op_name = "SKIP_TG_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                      break;
                      case  16 :
                        op_name = "HDU_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                        handle_hdu(p1,p2);
                      break;
                      case  5 :
                        op_name = "ENDCALL_EVT";
                        addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d\r\nEND-CALL", op, op_name, p1, p2, p3, p4) ); 
                        handle_endcall();
                      break;

                      case  2 :
                        switch( p1 ) {
                          case  0x3a  :
                            op_name = "\r\nSITE_EVT";
                            addP25Event(op_name);
                            //op_name = "SITE_EVT_PRIMARY";
                            //addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, 0x%02x, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                            handle_site( p1, p2); //primary
                          break;
                          case  0x3b  :
                            op_name = "\r\nSITE_EVT";
                            addP25Event(op_name);
                            //op_name = "SITE_EVT_PRIMARY";
                            //addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, 0x%02x, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                            handle_site( p1, p2); //primary
                          break;
                          case  0x39  :
                            op_name = "\r\nSITE_EVT";
                            addP25Event(op_name);
                            //op_name = "SITE_EVT_SECONDARY";
                            //addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, 0x%02x, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                            handle_site( p1, p2); //secondary
                          break;
                          case  0x3c  :
                            op_name = "\r\nSITE_EVT";
                            addP25Event(op_name);
                            //op_name = "SITE_EVT_ADJACENT";
                            //addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, 0x%02x, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                            handle_site( p1, p2); //adjacent
                          break;
                        }
                      break;
                    }
                  }
                }
                else if(skip_bytes>0 && rx_state==8) {
                  skip_bytes--;

                  if(skip_bytes>=0) pcm_bytes[pcm_idx++] = b[i];

                  if(skip_bytes==0) {
                    rx_state=0;
                    pcm_idx=0;

                    ByteBuffer p25_info = ByteBuffer.wrap(pcm_bytes);
                    p25_info.order(ByteOrder.LITTLE_ENDIAN);

                    //System.out.println("P25_INF");
                    //
                    on_control_freq = 1;

                    int p25_wacn = p25_info.getInt();
                    int p25_sys_id = (int) p25_info.getShort();
                    int p25_nac = (int) p25_info.getShort();
                    double p25_freq = (double) p25_info.getDouble()*1e6;
                    int p25_site_id = (int) p25_info.getShort();
                    int p25_rfss_id = (int) p25_info.getShort();

                    byte rssi_b = p25_info.get();
                    byte tsbk_ps_b = p25_info.get();
                    byte sa_b = p25_info.get();
                    byte ue_b = p25_info.get();
                    short tgzone_s = p25_info.getShort();

                    int crc = p25_info.getInt();
                    int crc1 = crc32.crc32_range(pcm_bytes, 26);

                    //System.out.println(crc+" "+crc1);
                    if(crc1!=crc) {
                      System.out.println("p25_inf frame dropped due to crc32");
                      return; //drop invalid 
                    }

                    p25_status_timeout=6000;

                    current_tgzone=tgzone_s;
                    update_zones();

                    blks_per_sec = tsbk_ps_b;

                    //this is done in the constellation data too
                    /*
                    try {
                      if(on_control_freq==1) {
                        update_rssi( (int) rssi_b );
                      }
                    } catch(Exception e) {
                    }
                    */


                    current_wacn_id = p25_wacn;
                    current_sys_id = p25_sys_id;

                    //good idea?
                    if(prev_p25_freq!=p25_freq) {
                      sys_info_count=0; 
                      if(is_dmr_mode==0) src_uid=0;
                      is_enc=0;
                      current_nco_off=0.0f;
                      did_freq_update=0;
                      did_metadata=0;
                      traffic_wacn_id=-1;
                      traffic_sys_id=-1;

                      current_wacn_id = 0; 
                      current_sys_id = 0; 

                      clear_sys_info();
                      prev_p25_freq=p25_freq;
                    }

                    try {
                      if(ue_b==0) {
                        bluetooth_error=0;
                      }
                      if(ue_b==1) {
                        if(bluetooth_error==0 && bluetooth_streaming==1) setStatus("Bluetooth Comm Error Detected.");
                        bluetooth_error=1;
                      }


                      if(sa_b==0) {
                        if(bluetooth_streaming==1 && bluetooth_error==0) setStatus("Bluetooth Audio Streaming Stopped");
                        bluetooth_streaming=0;
                          //is_phase1=1;
                          //is_phase2=0;
                      }
                      if(sa_b==1) {
                        if(bluetooth_streaming==0 && bluetooth_error==0) setStatus("Bluetooth Audio Streaming Started");
                        bluetooth_streaming=1;
                        bluetooth_streaming_timer=60000;
                          //is_phase1=1;
                          //is_phase2=0;
                      }
                    } catch(Exception e) {
                    }


                    try {
                      //System.out.println("p25_inf: wacn  "+p25_wacn);
                      //System.out.println("p25_inf: sys_id  "+p25_sys_id);
                      //System.out.println("p25_inf: nac  "+p25_nac);
                      //System.out.println("p25_inf: freq  "+p25_freq);

                      /*
                      try {
                        String freq_str= String.format( "%3.6f", p25_freq/1e6);
                        freq.setText("Freq: "+freq_str);
                        current_freq = new Double(p25_freq).doubleValue();
                        //System.out.println("p25_inf: freq  "+p25_freq);
                      } catch(Exception e) {
                      }
                      */

                      int wacnval = p25_wacn; 
                      if(wacnval!=0) {
                        wacn.setText("WACN: "+String.format("0x%05X", wacnval));
                      }
                      else {
                        wacn.setText("");
                      }
                      int siteidval = p25_site_id; 
                      if(siteidval!=0) {
                        siteid.setText("SITE ID: "+String.format("%03d", siteidval));
                      }
                      else {
                        siteid.setText("");
                      }
                      int rfidval = p25_rfss_id; 
                      if(rfidval!=0) {
                        rfid.setText("RFSS ID: "+String.format("%03d", rfidval));
                      }
                      else {
                        rfid.setText("");
                      }

                      int nacval = p25_nac; 
                      if(nacval!=0) {
                        nac.setText("NAC: "+String.format("0x%03X", nacval));
                      }
                      else {
                        if(nacval==0) nac.setText("");
                      }

                      try {
                        double d = p25_freq/1e6; 
                        if(d>20.0) cc_freq = d;
                      } catch(Exception e) {
                      }
                      int sys_id = p25_sys_id; 
                      if(sys_id!=0) {
                        sysid.setText("SYS_ID: "+String.format("0x%03X", sys_id));
                      }
                      else {
                        sysid.setText("");
                      }
                    } catch(Exception e) {
                    }
                  }
                }
                else if(skip_bytes>0 && rx_state==5) {
                  skip_bytes--;

                  if(const_idx<417) constellation_bytes[const_idx++] = b[i];

                  if(skip_bytes==0) {
                    rx_state=0;
                    const_idx=0;
                    cpanel.addData( constellation_bytes, do_synced );
                  }
                }
                else if(skip_bytes>0 && rx_state==7) {
                  skip_bytes--;
                    //System.out.println("bext_sb: "+skip_bytes);

                  if(pcm_idx<12) {
                    incoming_bytes[pcm_idx++] = b[i]; 
                    if(pcm_idx==12) {
                      ByteBuffer bbcrc = ByteBuffer.wrap(incoming_bytes,0,12);
                      bbcrc.order(ByteOrder.LITTLE_ENDIAN);

                      btext_len = bbcrc.getInt();
                      btext_crc32 = bbcrc.getInt();
                      btext_acktime = bbcrc.getInt();

                      bbcrc = ByteBuffer.wrap(incoming_bytes,0,12);
                      bbcrc.order(ByteOrder.LITTLE_ENDIAN);
                      bbcrc.putInt(btext_acktime);

                      //System.out.println("len "+btext_len+" acktime "+btext_acktime+"  crc32 "+ btext_crc32);

                      skip_bytes=btext_len;
                      if(skip_bytes<=0) {
                        rx_state=0;
                        pcm_idx=0;
                        skip_bytes=0;
                        continue;
                      }
                      if(skip_bytes>321) skip_bytes=321;
                      continue;
                    }
                  }
                  else {
                    incoming_bytes[pcm_idx++-8] = b[i]; 
                  }

                  if(skip_bytes==0 || pcm_idx>320+16) {

                    pcm_idx=0;
                    rx_state=0;

                    //crc is data area only 
                    int crc1 = crc32.crc32_range(incoming_bytes, btext_len+4);

                    //frame was corrupted. drop it and wait for a re-send
                    if(crc1!=btext_crc32) {
                      System.out.println("btext crc doesn't match. dropping "+String.format("0x%08x", crc1));
                      rx_state=0;
                      pcm_idx=0;
                      return;
                    }

                    //ackowledge that we received the frame
                    b_send_ack_crc(crc1);

                    //did we already receive this frame?
                    if( is_dup_crc(crc1) ) {
                      System.out.println("btext crc is duplicate. dropping "+String.format("0x%08x", crc1));
                      rx_state=0;
                      pcm_idx=0;
                      return;
                    }

                    //add new crc to the list
                    btext_crc[btext_crc_idx++] = crc1;
                    if(btext_crc_idx==8) btext_crc_idx=0;

                    String bstr = new String(incoming_bytes,4,btext_len-1);
                    if(bstr!=null && bstr.length()>0) addTextConsole(bstr); 

                    bstr=null; //garbage collect
                  }
                }
                else if(skip_bytes>0 && rx_state==4) {
                  skip_bytes--;

                  if(pcm_idx<332) {
                    incoming_bytes[pcm_idx++] = b[i]; //audio data + ack_time
                  }
                  else if(pcm_idx<336) {
                    pcm_crc[pcm_idx++-332] = b[i]; //crc
                  }

                  if(skip_bytes==0) {


                    for(int n=0;n<320;n++) {
                      pcm_bytes[n] = incoming_bytes[n];
                    }
                      
                    //crc includes a ms timestamp so crc is unique over time regardless of data (i.e. all zeros)
                    int crc1 = crc32.crc32_range(incoming_bytes, 332);
                    ByteBuffer bbcrc = ByteBuffer.wrap(pcm_crc);
                    bbcrc.order(ByteOrder.LITTLE_ENDIAN);
                    int crc2 = bbcrc.getInt();

                    ByteBuffer bba = ByteBuffer.wrap(incoming_bytes);
                    bba.order(ByteOrder.LITTLE_ENDIAN);

                    int ack_time = bba.getInt(320);
                    int system_uid = bba.getInt(324);
                    int tg = (int) (bba.getShort(328)&0xffff);
                    int wacn = (system_uid >> 12)&0xfffff;
                    int sysid = (system_uid & 0xfff);
                    int nacval = (int) (bba.getShort(330)&0xffff);


                    //System.out.println("talk group: "+tg);


                    //frame was corrupted. drop it and wait for a re-send
                    if(crc1!=crc2) {
                      System.out.println("voice audio crc doesn't match. dropping "+String.format("0x%08x", crc1));
                      rx_state=0;
                      pcm_idx=0;
                      return;
                    }

                    p25_status_timeout=6000;

                    if(nacval!=0) {
                      nac.setText("NAC: "+String.format("0x%03X", nacval));
                    }
                    else {
                      if(nacval==0) nac.setText("");
                    }

                    traffic_wacn_id = wacn;
                    traffic_sys_id = sysid;
                    //update_tgid( tg ); 


                    /*
                    //ackowledge that we received the frame
                    send_ack_crc(crc1);

                    //did we already receive this frame?
                    if( is_dup_crc(crc1) ) {
                      System.out.println("voice audio crc is duplicate. dropping "+String.format("0x%08x", crc1));
                      rx_state=0;
                      pcm_idx=0;
                      return;
                    }
                    */


                    //add new crc to the list
                    voice_crc[voice_crc_idx++] = crc1;
                    if(voice_crc_idx==8) voice_crc_idx=0;


                    //System.out.println("read voice");
                    try {
                      start_time = new java.util.Date().getTime();
                          tg_indicator.setBackground(java.awt.Color.yellow);
                          tg_indicator.setForeground(java.awt.Color.yellow);
                          tg_indicator.setEnabled(true);
                      if(aud!=null ) {
                        if(aud!=null) aud.playBuf(pcm_bytes);
                        cpanel.addAudio(pcm_bytes);
                        do_audio_tick=0;
                        audio_tick_start = new java.util.Date().getTime();
                      }
                    } catch(Exception e) {
                      e.printStackTrace();
                    }
                    rx_state=0;
                    pcm_idx=0;

                    if(enable_mp3.isSelected() || en_rdio.isSelected() || en_broadcastify_calls.isSelected() ) {
                      String fs =  System.getProperty("file.separator");
                      try {
                        if(aud_archive!=null) 
                          aud_archive.addAudio( pcm_bytes, current_talkgroup, 
                            home_dir+fs+sys_mac_id, current_wacn_id, current_sys_id, 1, 0, current_freq, broadcastify_calls_dir, sys_mac_id );
                      } catch(Exception e) {
                        e.printStackTrace();
                      }
                    }

                    if(tglog_e!=null && tglog_e.tg_trig_vaudio.isSelected()) do_meta();
                  }

                }
                else if(skip_bytes>0) {
                  skip_bytes--; //handle unknown state
                  if(skip_bytes==0) rx_state=0;
                }

                if(skip_bytes==0) {

                  //b2 5f 9c 71
                  //b2 4b f2 e5
                  if(rx_state==0 && b[i]==(byte) 0xb2) {
                    rx_state=1;
                  }
                  else if( (rx_state==1 && b[i]==(byte) 0x4b) || (rx_state==1 && b[i]==(byte) 0x4a) || (rx_state==1 && b[i]==(byte) 0x5f) || (rx_state==1 && b[i]==(byte) 0x5b) || (rx_state==1 && b[i]==(byte) 0x59) || (rx_state==1 && b[i]==(byte) 0x98) || (rx_state==1 && b[i]==(byte) 0x51) ) {
                    rx_state=2;
                  }
                  else if( (rx_state==2 && b[i]==(byte) 0xf2) || (rx_state==2 && b[i]==(byte) 0x76) || (rx_state==2 && b[i]==(byte) 0x9c) || (rx_state==2 && b[i]==(byte) 0x12) || (rx_state==2 && b[i]==(byte) 0xef) || (rx_state==2 && b[i]==(byte) 0x72) || (rx_state==2 && b[i]==(byte) 0x70) || (rx_state==2 && b[i]==(byte) 0xfe)) {
                    rx_state=3;
                  }
                  else if( (rx_state==3 && b[i]==(byte) 0xe5) || (rx_state==3 && b[i]==(byte) 0x0f) || (rx_state==3 && b[i]==(byte) 0x71) || (rx_state==3 && b[i]==(byte) 0xe4) || (rx_state==3 && b[i]==(byte) 0x72) || (rx_state==3 && b[i]==(byte) 0x31) || (rx_state==3 && b[i]==(byte) 0x15) || (rx_state==3 && b[i]==(byte) 0xb6)) {
                    //addTextConsole("\r\nfound voice header");

                    if(b[i]==(byte) 0x71) {
                      skip_bytes=320+1+4+4+8;
                      rx_state=4;
                      pcm_idx=0;
                      //System.out.println("do voice");
                    }
                    else if(b[i]==(byte) 0xe5) {
                      skip_bytes=21+1;
                      rx_state=9;
                      pcm_idx=0;
                      //System.out.println("p25_evt");
                    }
                    else if(b[i]==(byte) 0xb6) {
                      //skip_bytes=320+1+8+4;
                      //rx_state=7;
                      //pcm_idx=0;
                      rx_state=0;
                      //System.out.println("btext");
                    }
                    else if(b[i]==(byte) 0xe4) {
                      skip_bytes=417+1;
                      rx_state=5;
                      //System.out.println("do const");
                    }
                    else if(b[i]==(byte) 0x0f) {
                      pcm_idx=0;
                      skip_bytes=30+1;
                      rx_state=8;
                      //System.out.println("p25_inf");
                    }
                    else if(b[i]==(byte) 0x72) {
                      skip_bytes=256+1;
                      rx_state=6;
                      //System.out.println("do tdma");
                    }
                    else if(b[i]==(byte) 0x15) {
                      skip_bytes=148+1;
                      rx_state=0;
                      //System.out.println("do sysinfo");
                    }
                    else if(b[i]==(byte) 0x31) {
                      //audio flush
                      //System.out.println("\r\naudio flush");

                      rx_state=0;
                      skip_bytes=0;
                    }
                    else {
                      rx_state=0;
                      skip_bytes=0;
                    }
                  }
                  else {
                    //rx_state=0;  dont do this

                    if(rx_state==0 && skip_bytes==0 ) {
                      int isprint=1;

                      if( b[i]>=(byte) 0x00 && b[i]<=(byte)0x1f && b[i]!=(byte)0x0a && b[i]!=(byte)0x0d) {
                        isprint=0;
                        do_print=0;
                      }
                      if((byte)b[i]<0) {
                        isprint=0;
                        do_print=0;
                      }

                      if(isprint==1) str_b[str_idx++] = b[i];
                    }
                    else {
                      rx_state=0;
                      skip_bytes=0;
                    }
                  }
                }
              }

              if(str_idx>0 && do_print==1) {
                addTextConsole( new String(str_b,0,str_idx) );
                str_idx=0;
              }


            } catch(Exception e) {
              e.printStackTrace();
            }
          }

        }
        if( do_disconnect==1) {
          do_disconnect=0;
          is_connected=0;
          if(serial_port!=null) serial_port.closePort();
          serial_port=null;
        }

        tick_mod++;

        long ctime = new java.util.Date().getTime();


        //look for follow change
         if(tg_follow_blink != p25_follow ) {
          tg_follow_blink = p25_follow; 
          aud_archive.set_follow( tg_follow_blink );
         }

        if( tg_follow_blink>0 && ctime-tg_blink_time>2000) {


          tg_blink_time=ctime;

          tg_indicator.setEnabled(true);
          tg_blink^=0x01;
          if(tg_blink==1) {
            tg_indicator.setBackground(java.awt.Color.yellow);
            tg_indicator.setForeground(java.awt.Color.yellow);
          }
          else {
            tg_indicator.setBackground(java.awt.Color.black);
            tg_indicator.setForeground(java.awt.Color.black);
          }
        }
        else if(tg_follow_blink==0) {
          //tg_blink=0x01;
          //tg_indicator.setBackground(java.awt.Color.black);
          //tg_indicator.setForeground(java.awt.Color.black);
        }

        if(bluetooth_error==0 && bluetooth_streaming==1 && tick_mod%500==0) {
          bt_indicator.setEnabled(true);

          bluetooth_blink^=1;
          if(bluetooth_blink==1) {
            bt_indicator.setBackground(java.awt.Color.blue);
            bt_indicator.setForeground(java.awt.Color.blue);
          }
          else {
            bt_indicator.setBackground(java.awt.Color.black);
            bt_indicator.setForeground(java.awt.Color.black);
          }
        }
        else if(bluetooth_error==0 && bluetooth_streaming==0) {
          bt_indicator.setEnabled(true);
          bt_indicator.setBackground(java.awt.Color.black);
          bt_indicator.setForeground(java.awt.Color.black);
        }
        else if(bluetooth_error==1 && bluetooth_streaming==1) { //module is turned off
          bt_indicator.setEnabled(true);
          bt_indicator.setBackground(java.awt.Color.black);
          bt_indicator.setForeground(java.awt.Color.black);
        }


      } catch(Exception e) {
        e.printStackTrace(System.out);
      }

      long time = new java.util.Date().getTime();
      Boolean isWindows = System.getProperty("os.name").startsWith("Windows");
      int stop_time=50;
      //if(isWindows || is_mac_osx==1) stop_time=50;
      if(isWindows ) stop_time=50;
        else stop_time=500; 
      if(time-start_time>stop_time) {
        //if(aud!=null) aud.playStop();
        if(tg_follow_blink==0) {
          tg_indicator.setBackground(java.awt.Color.black);
          tg_indicator.setForeground(java.awt.Color.black);
        }
      }


    }
}

java.lang.Runtime rt;
int do_audio_tick=0;
int do_zipsearch=0;
int do_zipsearch2=0;
Boolean isWindows=true;
java.util.Timer utimer;
java.util.Timer iztimer;
int do_update_firmware;
int do_update_firmware2;
int do_disconnect;
int do_update_talkgroups;
int do_update_roaming=0;
int do_read_talkgroups;
static BTFrame parent;
SerialPort serial_port=null;
int firmware_checked=0;
int is_connected=0;
float current_evm_percent;
int do_connect=0;
String serial_port_name="";
char keydata[];
int sleep_factor=0;
int keyindex;
int command_input=0;
int status_timeout=1;
rssimeter rssim1;
rssimeter rssim2;
String console_line;
int sig_meter_timeout=1;
javax.swing.JLabel l1;
javax.swing.JLabel l2;
javax.swing.JLabel l3;
int do_restore_tg=0;
int do_restore_tg_csv=0;
int did_tg_backup=1;  //don't do backup on startup
int bluetooth_streaming=0;
int bluetooth_error=0;
int bluetooth_blink;
int tg_follow_blink=0;
int tg_blink=0;
int wd_count=0;
int tick_mod;
int rx_state=0;
int skip_bytes=0;
byte[] incoming_bytes;
byte[] pcm_bytes;
byte[] pcm_crc;
byte[] constellation_bytes;
byte[] tdma_bytes;
int pcm_idx=0;
int const_idx=0;
int tdma_idx=0;
LameEncoder encoder=null;
byte[] mp3_buffer;
String current_date=null;
String home_dir=null;
FileOutputStream fos_mp3;
FileOutputStream fos_meta;
FileOutputStream fos_conlog;
FileOutputStream fos_tdma;
File mp3_file=null;
File meta_file=null;
File conlog_file=null;
File tdma_file=null;
java.text.SimpleDateFormat formatter_date;
java.text.SimpleDateFormat time_format;
float current_nco_off=0.0f;
int current_sys_id = 0;
int current_wacn_id = 0; 
int traffic_sys_id = -1;
int traffic_wacn_id = -1; 
int do_toggle_record=1;
int did_metadata=0;
int meta_count=0;
int skip_header=1;
private Dimension parentSize;
int do_read_config=1;
int do_write_config=0;
audio aud = null; 
long start_time;
String freq_str="";
SYSConfig sys_config;
Roaming roaming_tests;
Preferences prefs;
int do_agc_update=0;
int system_crc=0;
int do_talkgroup_backup=0;
TGConfig tg_config=null;
zipsearch zs=null;
int do_test_freqs=0;
int do_roam_freq=0;
int do_read_roaming=0;
int do_roaming_backup=0;
int tg_update_pending=0;
int do_erase_roaming=0;
int do_backup_roaming=0;
int do_restore_roaming=0;
int do_append_roaming=0;
int do_console_output=0;
int do_write_roaming_flash_only=0;
int did_read_talkgroups=0;
int is_mac_osx=0;
int is_linux=0;
int is_windows=0;
int is_dmr_mode=0;
int tsbk_ps_i=0;
int bluetooth_streaming_timer=0;
int p25_status_timeout=1;
Hashtable rid_hash;
Hashtable lat_lon_hash1;
Hashtable lat_lon_hash2;
Hashtable supergroup_hash;
Hashtable no_loc_freqs;
Boolean do_tdma_messages=false;
ConstPlotPanel cpanel;
Boolean do_mini_const=false;
boolean do_synced;
double current_freq=0.0;
long audio_tick_start=0;
int command_input_timeout=0;
long tg_blink_time=0;
String current_talkgroup="";
int reset_session=0;
java.text.SimpleDateFormat mp3_time_format = new java.text.SimpleDateFormat( "HH:mm:ss" );
String mp3_time ="";
int tg_pri=0;
int do_select_home_dir=0;
String sys_mac_id="";
long wdog_time=0;
int did_freq_tests=0;
int sys_info_count=0;
int src_uid=0;
int prev_uid=0;
int is_enc=0;
Alias alias;
String current_alias;
int do_alias_import=0;
int do_alias_export=0;
byte[] b;
byte[] str_b;
int str_idx=0;
int avail=0;
int is_phase1=1;
int is_phase2=0;
int is_tdma_cc=0;
String tsbk_ps="";

int is_fast_mode=0;
double v_freq=0.0f;
double cc_freq=0.0f;
long status_time;
int bl_p1=0;
int current_tgzone=0;
int global_sync_count;
audio_archive aud_archive;
String document_dir="";
logger logger_out;
sysinfo si;
String talkgroup_name;
String freqval="";
String rssi="";

BigText bt1;
BigText bt2;
BigText bt3;
BigText bt4;
BigText bt5;

displayframe_edit dframe;

String src_uid_str="";
String rf_channel="";

aliasEntry alias_dialog;

int sel_freq=0;

int[] voice_crc;
int voice_crc_idx=0;
int[] btext_crc;
int btext_crc_idx=0;
displayframe_popout dvout;

String con_str="";


JFontChooser jfc;
int tg_font_size=14;
int tg_font_style = Font.PLAIN;
String tg_font_name="Monospaced";
public Color tg_font_color;


int btext_len = 0; 
int btext_acktime = 0; 
int btext_crc32 = 0; 
int cc_lcn=0;
int tdma_slot=0;
float erate=0.0f;

int save_iq_len=0;
int iq_out=0;

int fw_completed=0;
int blks_per_sec=0;

int demod_type=0;

int tg_zone=0;
String tg_zone_alias="";

tglog_editor tglog_e;

FileOutputStream fos_iq;
JFileChooser chooser;
freqConfiguration button_config;

double prev_p25_freq=0.0;
int on_control_freq=1;
int did_freq_update=0;
int send_const_count=0;
int roaming_toggle=0;
String prev_p25_evt="";
byte p25_follow=0;

public String broadcastify_calls_dir=null;

  ///////////////////////////////////////////////////////////////////
    public BTFrame(String[] args) {


      initComponents();


      freemem.setVisible(false);

      rt = Runtime.getRuntime();

      button_config = new freqConfiguration(this);

      chooser = new JFileChooser();

      DefaultCaret caret = (DefaultCaret) log_ta.getCaret();
      caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

      jfc = new JFontChooser();
      jfc.setSize(1024,800);

      voice_crc = new int[8];
      btext_crc = new int[8];

      edit_display_view.setEnabled(false);

      bt1 = new BigText(" ", 192, new Color(128,0,128) );
      bt2 = new BigText(" ", 128, Color.white);
      bt3 = new BigText(" ", 128, Color.red);
      bt4 = new BigText(" ", 128, Color.cyan);
      bt5 = new BigText(" ",128, Color.yellow);

      dframe = new displayframe_edit(this, bt1,bt2,bt3,bt4,bt5);

      display_frame.add(bt1);
      display_frame.add(bt2);
      display_frame.add(bt3);
      display_frame.add(bt4);
      display_frame.add(bt5);

      si = new sysinfo();

      logger_out = new logger(this);
      aud_archive = new audio_archive(this);

      autoscale_const.setVisible(false);
      nsymbols.setVisible(false);
      jLabel9.setVisible(false);
      if_slide.setVisible(false);

      audio_prog.setBackground( Color.black );
      audio_prog.setForeground( Color.green );


      formatter_date = new java.text.SimpleDateFormat( "yyyy-MM-dd" );
      String ndate = formatter_date.format(new java.util.Date() );
      String fdate=new String(ndate);  //date changed
      String exe_path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath().toString();
      exe_path = exe_path.replace("BTConfig.exe", "");
      System.out.println("log file path: "+exe_path+"p25rx_conlog_"+fdate+".txt");

      try {
        conlog_file = new File(exe_path+"p25rx_conlog_"+fdate+".txt");
        fos_conlog = new FileOutputStream( conlog_file, true ); 
      } catch(Exception e) {
        e.printStackTrace();
      }


      b = new byte[256000];
      str_b = new byte[256000];

      supergroup_hash = new Hashtable();
      rid_hash = new Hashtable();


      //jPanel25.remove(const_panel);
      cpanel = new ConstPlotPanel(this);
      const_panel.add(cpanel, java.awt.BorderLayout.CENTER);
      //jPanel25.add(cpanel);
      //

      if(zs==null) zs = new zipsearch(this);

      if(args.length>0) {
        for(int i=0;i<args.length;i++) {
          if(args[i].equals("-fastmode")) {
            System.out.println("enable fast mode");
            is_fast_mode=1;
          }
          if(args[i].equals("-console")) {
            do_console_output=1;
            System.out.println("enable console output");
          }
          if(args[i].equals("-tdma")) {
            do_tdma_messages=true;
            System.out.println("enable tdma / phase 2 messages");
          }
          if(args[i].equals("-mac")) {
            is_mac_osx=1;
            System.out.println("\r\nenabling MAC_OSX option");
          }
          if(args[i].equals("-miniconst")) {
            do_mini_const=true;
          }
          if(args[i].equals("-bl_p1")) {
            System.out.println("enable p25rx-1 bootloader support");
            bl_p1=1;
          }
        }
      }

      roaming_tests = new Roaming();

      inc_p25.setVisible(false);
      macid.setVisible(false);

      //agc_gain.setVisible(false); //hide agc slider related
      //jLabel3.setVisible(false);
      //agc_level_lb.setVisible(false);

      inc_dmr.setVisible(false);

      fw_ver.setVisible(false);
      fw_installed.setVisible(false);

      pcm_bytes = new byte[320];
      incoming_bytes = new byte[512];
      pcm_crc = new byte[4];
      constellation_bytes = new byte[417];
      tdma_bytes = new byte[256];

      write_config.setEnabled(false);
      disconnect.setEnabled(false);

      isWindows=false;

      if( System.getProperty("os.name").startsWith("Windows") ) {
        isWindows=true;
          System.out.println("\r\nenabling Windows option");
          os_string.setText("OS: Windows");
        is_windows=1;
      }

      //Mac OSX
      if( System.getProperty("os.name").toLowerCase().contains("mac os x") ) {
          is_mac_osx=1;
          System.out.println("\r\nenabling MAC_OSX option");
          os_string.setText("OS: Mac OSX");
      }

      if( System.getProperty("os.name").toLowerCase().contains("linux") ) {
          is_linux=1;
          System.out.println("\r\nenabling Linux option");
          os_string.setText("OS: Linux");
      }


      read_config.setVisible(false);  //read config button


      check_firmware.setEnabled(false);
      check_firmware.setVisible(false);

      record_to_mp3.setEnabled(false);
      record_to_mp3.setVisible(false);

      macid.setText("");
      wacn.setText("");
      sysid.setText("");
      nac.setText("");
      freq.setText("");
      rfid.setText("");
      siteid.setText("");


      record_to_mp3.setSelected(true);


      formatter_date = new java.text.SimpleDateFormat( "yyyy-MM-dd" );
      time_format = new java.text.SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );



      fw_ver.setText("Latest Avail: FW Date: 202206080812");
      release_date.setText("Release: 2022-06-30 15:31");
      fw_installed.setText("   Installed FW: ");

      setProgress(-1);

      jTable1.setShowHorizontalLines(true);
      jTable1.setShowVerticalLines(true);

      freq_table.setShowHorizontalLines(true);
      freq_table.setShowVerticalLines(true);

      alias_table.setShowHorizontalLines(true);
      alias_table.setShowVerticalLines(true);

      keydata = new char[4096];
      keyindex=0;
      jTextArea1.getCaret().setVisible(true);
      jTextArea1.getCaret().setBlinkRate(250);

      setIconImage(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/iconsmall.gif")).getImage()); // NOI18N
      setTitle("BlueTail Technologies P25RX-II Configuration Software (c) 2021,22");

      rssim1 = new rssimeter();
      rssim2 = new rssimeter();
      //rssim1.setValue(-90,true);
      //rssim2.setValue(-20,false);

      l1 = new javax.swing.JLabel();
      l2 = new javax.swing.JLabel();
      l3 = new javax.swing.JLabel();
      l1.setText("RF Sig Level");
      l2.setText("Sig Quality");
      l1.setForeground(java.awt.Color.white);
      l2.setForeground(java.awt.Color.white);
      l3.setForeground(java.awt.Color.white);
      l3.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N

      l3.setText("NO SIG");
      desc_panel.add(l3);


      level_panel.add( l1 ); 
      level_panel.add(rssim1); 

      //level_panel.add( l2 ); 
      //level_panel.add(rssim2); 

      l1.setVisible(true);
      l2.setVisible(true);
      l3.setVisible(true);
      rssim1.setVisible(true);
      rssim2.setVisible(true);
      rssim1.setValue(-130,false);
      rssim2.setValue(-130,false);
      p25_status_timeout=1;
      l3.setText("");


      /*
      alias_table.setAutoCreateRowSorter(true);
      alias_table.getRowSorter().addRowSorterListener(new RowSorterListener() {
       public void sorterChanged(RowSorterEvent rse) {
        if (rse.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
            java.util.List<? extends RowSorter.SortKey> sortKeys = alias_table.getRowSorter().getSortKeys();
            if(sortKeys.get(0).getSortOrder() == SortOrder.ASCENDING) {
              alias_table.getRowSorter().toggleSortOrder(0);
            }
          }
        }
      });
      */


      /*
      freq_table.setAutoCreateRowSorter(true);

      freq_table.getRowSorter().addRowSorterListener(new RowSorterListener() {
       public void sorterChanged(RowSorterEvent rse) {
        if (rse.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
          java.util.List<? extends RowSorter.SortKey> sortKeys = freq_table.getRowSorter().getSortKeys();
          if(sortKeys.get(0).getSortOrder() == SortOrder.ASCENDING) {
            freq_table.getRowSorter().toggleSortOrder(0);
          }
        }
       }
      });
      */

      DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
      rightRenderer.setHorizontalAlignment(JLabel.LEFT);
      jTable1.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
      jTable1.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
      jTable1.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
      jTable1.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
      jTable1.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
      jTable1.setAutoCreateRowSorter(true);

      jTable1.getRowSorter().addRowSorterListener(new RowSorterListener() {
       public void sorterChanged(RowSorterEvent rse) {
        if (rse.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
          if(tg_config!=null) {
            //System.out.println(rse);
            //tg_config.postSort(parent);
            java.util.List<? extends RowSorter.SortKey> sortKeys = jTable1.getRowSorter().getSortKeys();
            if(sortKeys.get(0).getSortOrder() == SortOrder.ASCENDING) {
              //jTable1.getRowSorter().setSortKeys(null);
              jTable1.getRowSorter().toggleSortOrder(0);
            }
          }
        }
       }
      });

      level_panel.remove(sq_lb);
      level_panel.remove(tg_lb);
      level_panel.remove(bt_lb);

      level_panel.remove(bt_indicator);
      level_panel.remove(bt_indicator);
      level_panel.remove(tg_indicator);
      level_panel.remove(sq_indicator);

      sq_lb.setText("   SIG");
      level_panel.add(sq_lb);
      level_panel.add(sq_indicator);
      level_panel.add(tg_lb);
      level_panel.add(tg_indicator);
      level_panel.add(bt_lb);
      level_panel.add(bt_indicator);

      sq_indicator.setBackground(java.awt.Color.black);
      sq_indicator.setForeground(java.awt.Color.black);
      bt_indicator.setBackground(java.awt.Color.black);
      bt_indicator.setForeground(java.awt.Color.black);
      tg_indicator.setBackground(java.awt.Color.black);
      tg_indicator.setForeground(java.awt.Color.black);


      //parentSize = Toolkit.getDefaultToolkit().getScreenSize();
      //setSize(new Dimension((int) (parentSize.width * 0.75), (int) (parentSize.height * 0.8)));

        //jTabbedPane1.remove( buttong_config);

      InputMap inputMap = jTable1.getInputMap(javax.swing.JComponent.WHEN_FOCUSED);
      ActionMap actionMap = jTable1.getActionMap();
      String deleteAction = "delete";
      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0),
                deleteAction);
      actionMap.put(deleteAction, new AbstractAction()
        {
          public void actionPerformed(java.awt.event.ActionEvent deleteEvent)
          {
              delete_talkgroup_rows();
          }
        });


      InputMap inputMap2 = freq_table.getInputMap(javax.swing.JComponent.WHEN_FOCUSED);
      ActionMap actionMap2 = freq_table.getActionMap();
      String deleteAction2 = "delete";
      inputMap2.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0),
                deleteAction2);
      actionMap2.put(deleteAction2, new AbstractAction()
        {
          public void actionPerformed(java.awt.event.ActionEvent deleteEvent)
          {
              delete_roaming_rows();
          }
        });


      InputMap inputMap3 = sig_cmd.getInputMap(javax.swing.JComponent.WHEN_FOCUSED);
      ActionMap actionMap3 = sig_cmd.getActionMap();
      String deleteAction3 = "cmd";
      inputMap3.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
                deleteAction3);
      actionMap3.put(deleteAction3, new AbstractAction()
        {
          public void actionPerformed(java.awt.event.ActionEvent deleteEvent)
          {
            //System.out.println("got here action3");
            String cmd= sig_cmd.getText().trim()+"\r\n"; 
            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
            System.out.println(cmd);
          }
        });

      do_connect();

      //do this last
      utimer = new java.util.Timer();
      utimer.schedule( new updateTask(), 100, 1);
      setSize(1200,800);
    }
  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  void delete_roaming_rows() {
      int[] rows = freq_table.getSelectedRows();
      int flash_recs=0;

      if(did_freq_tests==1) return;

      for(int i=0;i<250;i++) {
        String str1 = (String) freq_table.getModel().getValueAt(i, 3);
        if(str1!=null) {
          flash_recs++;
        }
      }

      if(rows.length>0) {
        for(int i=0;i<rows.length;i++) {
          freq_table.getModel().setValueAt(null,freq_table.convertRowIndexToModel(rows[i]),0);
          freq_table.getModel().setValueAt(null,freq_table.convertRowIndexToModel(rows[i]),1);
          freq_table.getModel().setValueAt(null,freq_table.convertRowIndexToModel(rows[i]),2);
          freq_table.getModel().setValueAt(null,freq_table.convertRowIndexToModel(rows[i]),3);
          freq_table.getModel().setValueAt(null,freq_table.convertRowIndexToModel(rows[i]),4);
          freq_table.getModel().setValueAt(null,freq_table.convertRowIndexToModel(rows[i]),5);
          freq_table.getModel().setValueAt(null,freq_table.convertRowIndexToModel(rows[i]),6);
          freq_table.getModel().setValueAt(null,freq_table.convertRowIndexToModel(rows[i]),7);
          freq_table.getModel().setValueAt(null,freq_table.convertRowIndexToModel(rows[i]),8);
          freq_table.getModel().setValueAt(null,freq_table.convertRowIndexToModel(rows[i]),9);
          freq_table.getModel().setValueAt(null,freq_table.convertRowIndexToModel(rows[i]),10);
        } 
      }
      if(flash_recs>0) do_write_roaming_flash_only=1;
  }
  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  void delete_talkgroup_rows() {
      int[] rows = jTable1.getSelectedRows();
      if(rows.length>0) {
        for(int i=0;i<rows.length;i++) {
          jTable1.getModel().setValueAt(null,jTable1.convertRowIndexToModel(rows[i]),0);
          jTable1.getModel().setValueAt(null,jTable1.convertRowIndexToModel(rows[i]),1);
          jTable1.getModel().setValueAt(null,jTable1.convertRowIndexToModel(rows[i]),2);
          jTable1.getModel().setValueAt(null,jTable1.convertRowIndexToModel(rows[i]),3);
          jTable1.getModel().setValueAt(null,jTable1.convertRowIndexToModel(rows[i]),4);
          jTable1.getModel().setValueAt(null,jTable1.convertRowIndexToModel(rows[i]),5);
          jTable1.getModel().setValueAt(null,jTable1.convertRowIndexToModel(rows[i]),6);
          jTable1.getModel().setValueAt(null,jTable1.convertRowIndexToModel(rows[i]),7);
        } 
      }

    jTable1.getRowSorter().toggleSortOrder(0);
    jTable1.getRowSorter().toggleSortOrder(0);

    do_update_talkgroups=1;
    do_read_talkgroups=1;
    jTable1.setRowSelectionInterval(0,0);
  }

  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  public SerialPort find_serial_port()
  {

    int n=0;

    SerialPort[] ports = SerialPort.getCommPorts();

    for(int i=0; i<ports.length; i++) {
        String isopen="";
        if( ports[i].isOpen() ) isopen=" open";
          else isopen="  closed";


        System.out.println("\r\n["+i+"]  found device on : "+
          ports[i].getSystemPortName()+"  "+
          ports[i].getDescriptivePortName()+"  "+
          ports[i].getPortDescription()+"  "+
          ports[i].toString()+isopen);
    }


    for(int i=0; i<ports.length; i++) {
      //setStatus("\r\nport: "+ports[i]+" on " + ports[i].getSystemPortName());

      String bl_search_str = "BlueTail-P25RX-II";
      if(bl_p1==1) bl_search_str="BlueTail-P";  //allow p25rx bootloader for first proto

      if( ports[i].toString().startsWith(bl_search_str) ) {

        if(!ports[i].isOpen()) {
          if( ports[i].openPort(200) ) {
            ports[i].closePort();
            System.out.println("using ["+i+"]  "+ports[i]);

            String fastmode_str="";
            if(is_fast_mode==1) fastmode_str="  FAST MODE ON";
            ser_dev.setText("PORT: "+ports[i].getSystemPortName()+fastmode_str);
            return ports[i];
          }
          else {
              System.out.println("attempting to close locked port "+ports[i]);
              ports[i].closePort();
              if( ports[i].openPort(200) ) {
                System.out.println("using ["+i+"]  "+ports[i]);
                return ports[i];
              }
          }
        }
      }
    }

    return null;
  }

/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
void update_rssi(int rssi_b) {

  rssi = String.format("%03d", rssi_b);
  p25_status_timeout=6000;

  if( rssim1.getValue()<-127 && blks_per_sec==0) {
    l3.setText("NO SIG");
  }
  else {

    if(on_control_freq==1) {
      //FORMAT ON CC
      String fmt = status_format_cc.getText(); 
      String cc_str = dframe.do_subs(fmt,false);
      l3.setText(cc_str);
    }
    else {
      //FORMAT ON VOICE
      String fmt = status_format_voice.getText(); 
      String voice_str = dframe.do_subs(fmt,false);
      l3.setText(voice_str);
    }


    reset_session=1;
    if(system_alias.getText()!=null && system_alias.getText().length()>0 ) {
      status.setVisible(true);
    }
    //if( is_dmr_mode==1 ) blks_per_sec*=2;

    /*
    if( blks_per_sec > 20) {
      sq_indicator.setForeground( java.awt.Color.green );
      sq_indicator.setBackground( java.awt.Color.green );
    }
    else if( blks_per_sec > 10) {
      sq_indicator.setForeground( java.awt.Color.blue );
      sq_indicator.setBackground( java.awt.Color.blue );
    }
    else if( blks_per_sec > 0) {
      sq_indicator.setForeground( java.awt.Color.red );
      sq_indicator.setBackground( java.awt.Color.red );
    }
    else {
      sq_indicator.setForeground( java.awt.Color.black );
      sq_indicator.setBackground( java.awt.Color.black );
    }
    */
  }


  rssim1.setValue( Integer.valueOf(rssi_b).intValue(),true );
  sig_meter_timeout=20000;

  try {
    //if( did_freq_update==0 ) {
      dframe.update_colors();
     // did_freq_update=1;
    //}
  } catch(Exception e) {
  }
}


    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    public byte[] encode_mp3(byte[] pcm) {

      int len=0;
      byte[] b=null;


      try {

        if(encoder==null) {
          //AudioFormat inputFormat = new AudioFormat( new AudioFormat.Encoding("PCM_SIGNED"), 8000.0f, 16, 1, 160, 50, false);
          AudioFormat inputFormat = new AudioFormat( 8000.0f, 16, 1, true, false);  //booleans are signed, big-endian
          //encoder = new LameEncoder(inputFormat, 256, MPEGMode.MONO, Lame.QUALITY_LOWEST, false);
          encoder = new LameEncoder(inputFormat, 32, MPEGMode.MONO, Lame.QUALITY_LOWEST, true);
          //ByteArrayOutputStream mp3 = new ByteArrayOutputStream();
          mp3_buffer = new byte[encoder.getPCMBufferSize()];

          //int bytesToTransfer = Math.min(buffer.length, pcm.length);
          //int bytesWritten;
          //int currentPcmPosition = 0;
          //while (0 < (bytesWritten = encoder.encodeBuffer(pcm, currentPcmPosition, bytesToTransfer, buffer))) {
           // currentPcmPosition += bytesToTransfer;
            //bytesToTransfer = Math.min(buffer.length, pcm.length - currentPcmPosition);

            //mp3.write(buffer, 0, bytesWritten);
          //}

          //encoder.close();
          //return mp3.toByteArray();
        }
        else {
          len = encoder.encodeBuffer(pcm, 0, 320, mp3_buffer);
          //addTextConsole("\r\nencoder: "+len);
        }

        if(len==0) return null;

        b = new byte[len];

        if(len>0) {
          for(int i=0;i<len;i++) {
            b[i] = mp3_buffer[i];
          }
        }
      } catch(Exception e) {
        //e.printStackTrace();
        e.printStackTrace();
      }

      return b;
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    void addTableObject(Object obj, int row, int col) {
      jTable1.getModel().setValueAt(obj,row,col);
    }
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    Object getTableObject(int row, int col) {
      return jTable1.getModel().getValueAt(row,col);
    }
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    void addAliasObject(Object obj, int row, int col) {
      alias_table.getModel().setValueAt(obj,row,col);
    }
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    Object getAliasObject(int row, int col) {
      return alias_table.getModel().getValueAt(row,col);
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    void addP25Event(String str) {
      try {
        if( str.equals(prev_p25_evt) ) {
          if( en_evt_output.isSelected() &&  !str.contains("SITE_EVT")) {
            addTextConsole(".");
          }
        } 
        else {
          if( en_evt_output.isSelected() ) {
            addTextConsole(str);
          }
          prev_p25_evt=str;
        }


        if( en_evt_output.isSelected() ) {
          try {
            String date = dframe.getDateTimeStr(); 
            String fs =  System.getProperty("file.separator");
            String wacn_out = String.format("%05X", current_wacn_id);
            String sysid_out = String.format("%03X", current_sys_id);
            String hdir = document_dir+fs+sys_mac_id+fs+"p25rx_eventlog_"+current_date+".txt";
            String header = "\r\n#EVT: op, p1, p2, p3, p4";
            if( logger_out!=null ) logger_out.write_log( "\r\n"+date+","+str.trim(), hdir, header );
            console_line = "";
          } catch(Exception e) {
          }
        }
      } catch(Exception e) {
      }
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    void addTextConsole(String str) {

      if(command_input==1) return;

   try {

       /*
      if( (!con_str.contains("\r\n") && !con_str.equals(".") && command_input_timeout==0)) {
        return;
      }
       */

      if(str!=null && do_console_output==1) System.out.println(str.trim());


      String talkgroup="";
      //freqval="";
      tsbk_ps="";

      if(console_line==null) console_line = new String("");
      console_line = console_line.concat(str);


      if( console_line.contains("signal found") || console_line.contains("FOUND P25") || console_line.contains("FOUND DMR") ) {
        StringTokenizer st = new StringTokenizer(console_line,"\r\n");
        String st1 = ""; 

        int cnt=0;
        while(st.hasMoreTokens() && cnt++<15) {
          st1 = st.nextToken().trim();
          if(st1.startsWith("signal found") || st1.contains("FOUND") ) {
            try {
              //String date = time_format.format(new java.util.Date() );
              String date = dframe.getDateTimeStr(); 
              String rec = ""; 
              if(st1.startsWith("signal found")) {
                rec = "\r\n"+date+","+st1; 
              }
              else {
                rec = " "+st1; 
              }
              String fs =  System.getProperty("file.separator");
              String wacn_out = String.format("%05X", current_wacn_id);
              String sysid_out = String.format("%03X", current_sys_id);
              String hdir = document_dir+fs+sys_mac_id+fs+"p25rx_cc_scan_"+current_date+".txt";
              String header = "\r\n#cc_search output";
              if( logger_out!=null ) logger_out.write_log( rec, hdir, header );
              console_line = "";
              break;
            } catch(Exception e) {
            }
          }
        }
      }

       /////////////////////////////////////////////////////////////////////////////////////////////////////////
       //         EVENT SECTION
       /////////////////////////////////////////////////////////////////////////////////////////////////////////





      if(console_line.contains("P25_PII_CC") ) {
        is_tdma_cc=1;
        p25_status_timeout=6000;
      }

      if(console_line.contains("$TDMA") ) {
        p25_status_timeout=6000;
      }


      if(console_line.contains("VGA_CAL:") && console_line.contains("$") ) {
        StringTokenizer st = new StringTokenizer(console_line,"\r\n$");
        String st1 = ""; 
        int cnt=0;
        while(st.hasMoreTokens() && cnt++<25) {
          st1 = st.nextToken();
          if(st1!=null && st1.contains("VGA_CAL:")) {
            try {
              setStatus(st1);
            } catch(Exception e) {
              e.printStackTrace();
            }
          }
        }
      }


       try {
          console_line = new String("");
        } catch(Exception e) {
            e.printStackTrace();
          console_line = new String("");
        }

      if( jTextArea1.getText().length() > 128000 ) {
        jTextArea1.replaceRange("",0,64000);
      }

        if(str.length()>0 ) {

          //String date = time_format.format(new java.util.Date() );
          String date = dframe.getDateTimeStr(); 
          //str = str.replaceAll("DATA_SYNC",date+" "+"DATA_SYNC");
          //str = str.replaceAll("found DMR_BS_VOICE_SYNC",date+" "+"found DMR_BS_VOICE_SYNC");

          jTextArea1.append(str);
          jTextArea1.setCaretPosition(jTextArea1.getText().length());

          jTextArea1.getCaret().setVisible(true);
          jTextArea1.getCaret().setBlinkRate(250);
        }
     } catch(Exception e) {
       e.printStackTrace();
     }
    }


/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         TG PRI EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_tg_pri(int p1, int p2, int p3, int p4) {

  String tg_id = Integer.valueOf(p1).toString(); 

  if(tg_id!=null) {
    current_talkgroup = tg_id;
  }
  tg_pri=1;
  src_uid=0;
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         ADJACENT MULT GRPS EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_adj_mult(int p1, int p2, int p3, int p4) {
  String active_tg= String.format("Adjacent Active Talk Groups: %d, %d", p2, p4);
  setStatus(active_tg);
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         ADJACENT SINGLE GRPS EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_adj_single(int p1, int p2, int p3, int p4) {
  String active_tg= String.format("Adjacent Active Talk Groups: %d", p1);
  setStatus(active_tg);
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 RID EVENT 
  //case  0x3a  :
  //  handle_site( op, p2); //primary
  //break;
  //case  0x3b  :
  //  handle_site( op, p2); //primary
  //break;
  //case  0x39  :
  //  handle_site( op, p2); //secondary
  //break;
  //case  0x3c  :
  //  handle_site( op, p2); //adjacent
  //break;
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_site( int op, int freq_hz) {

    String rec=", ";

    double f = Double.valueOf( ((double) freq_hz) / 1e6 );
    switch(op) {
      case  0x3a  :
        roaming_tests.addPrimary( String.format("%3.5f", f) );
        rec = rec.concat("PRIMARY,");
      break;
      case  0x3b  :
        roaming_tests.addPrimary( String.format("%3.5f", f) );
        rec = rec.concat("PRIMARY,");
      break;
      case  0x39  :
        roaming_tests.addSecondary( String.format("%3.5f", f) );
        rec = rec.concat("SECONDARY,");
      break;
      case  0x3c  :
        roaming_tests.addAdjacent( String.format("%3.5f", f) );
        rec = rec.concat("NEIGHBOR,");
      break;
    }
    rec = rec.concat( String.format("%3.6f", f) );

    try {
      String date = dframe.getDateTimeStr(); 
      String fs =  System.getProperty("file.separator");
      String wacn_out = String.format("%05X", current_wacn_id);
      String sysid_out = String.format("%03X", current_sys_id);
      String hdir = document_dir+fs+sys_mac_id+fs+"p25rx_eventlog_"+current_date+".txt";
      String header = "\r\n#EVT: op, p1, p2, p3, p4";
      if( logger_out!=null ) logger_out.write_log( rec, hdir, header );
      console_line = "";
    } catch(Exception e) {
    }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 HDU EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_hdu(int p1, int p2 ) {
  try {
  } catch(Exception e) {
  }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 ENDCALL EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_endcall( ) {
  try {
    int silent_time = new Integer( parent.end_call_silence.getText() ).intValue();
    if(aud_archive!=null && silent_time>0) {
      String fs =  System.getProperty("file.separator");
      aud_archive.addSilence( silent_time, current_talkgroup, home_dir+fs+sys_mac_id, current_wacn_id, current_sys_id );

      try {
        aud_archive.close_all_rdio();
      } catch(Exception e) {
      }
      try {
        aud_archive.close_all_bcalls(0);
      } catch(Exception e) {
      }
    }
  } catch(Exception e) {
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 EMERGENCY EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_emergency( int p1, int p2, int p3) {

  try {
    double fr = Double.valueOf((double) p1 / 1e6);
    int ga = Integer.valueOf(p2);
    int sa = Integer.valueOf(p3);

    String date = dframe.getDateTimeStr(); 
    String rec = String.format("\r\nEMRG_RESP(0x27),%s,%3.6f,%d,%d", date, fr, ga, sa);

    String fs =  System.getProperty("file.separator");
    String wacn_out = String.format("%05X", current_wacn_id);
    String sysid_out = String.format("%03X", current_sys_id);
    String hdir = document_dir+fs+sys_mac_id+fs+"p25rx_emergency_"+current_date+"-"+wacn_out+"-"+sysid_out+".txt";
    String header = "OPCODE,TIME,Frequency,TGroup,RADIO_ID";
    if( logger_out!=null && current_wacn_id!=0 && current_sys_id!=0) logger_out.write_log( rec, hdir, header );
  } catch(Exception e) {
  }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 AFFILIATION EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_affiliation( int p1, int p2, int p3) {
  try {
    double fr = Double.valueOf((double) p1 / 1e6);
    int ga = Integer.valueOf(p2);
    int sa = Integer.valueOf(p3);

    String date = dframe.getDateTimeStr(); 
    String rec = String.format("\r\nGRP_AFF_RESP(0x28),%s,%3.6f,%d,%d", date, fr, ga, sa);

    String fs =  System.getProperty("file.separator");
    String wacn_out = String.format("%05X", current_wacn_id);
    String sysid_out = String.format("%03X", current_sys_id);
    String hdir = document_dir+fs+sys_mac_id+fs+"p25rx_affiliation_"+current_date+"-"+wacn_out+"-"+sysid_out+".txt";
    String header = "OPCODE,TIME,Frequency,TGroup,RADIO_ID";
    if( logger_out!=null && current_wacn_id!=0 && current_sys_id!=0) logger_out.write_log( rec, hdir, header );
  } catch(Exception e) {
  }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 PATCH EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_patch(int p1, int p2, int p3, int p4) {
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 ENC EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_encryption(int p1, int p2, int p3, int p4) {
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 RID EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_dmr_vgrant(int group, int freq1, int rid, int freq2) {
  //handle_rid( rid, group);
  src_uid=rid;
  cc_freq = (double) (freq1/1e6);
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 RID EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_rid( int src, int grp) {

    try {
      if(tglog_e!=null && tglog_e.tg_trig_nzrid.isSelected() && src_uid!=0) do_meta();
      if(src_uid!=0) src_uid_str = new Integer(src_uid).toString();

      if(tglog_e!=null && tglog_e.tg_trig_nzrid.isSelected() && src_uid!=0) do_meta();
        else if(tglog_e!=null && tglog_e.tg_trig_anyrid.isSelected() ) do_meta();

    } catch(Exception e) {
      e.printStackTrace();
    }

    String ridstr = Integer.valueOf(src).toString();

    if(src!=0 && src != prev_uid) {
      did_metadata=0;
    }
    src_uid = src;

    try {
      if(alias!=null && src!=0) alias.addRID(this, ridstr);

      try {
          dframe.update_colors();
      } catch(Exception e) {
      }

    } catch(Exception e) {
    }


    if(ridstr!=null && ridstr.length()>0 && current_alias!=null) {
      status.setText(system_alias.getText()+", RID: "+ridstr+", "+current_alias);
    }
    else if(ridstr!=null && ridstr.length()>0 ) {
      status.setText(system_alias.getText()+", RID: "+ridstr);
    }
    else {
      status.setText(system_alias.getText());
    }

}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void update_follow(byte b) {
  if( b!=0 && !hold1.isSelected() ) {
    hold1.setSelected(true);
    hold1.setBackground( Color.green );
  }
  else if( b==0 && hold1.isSelected() ) {
    hold1.setSelected(false);
    hold1.setBackground( Color.gray );
  }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void update_roaming(byte b) {
  if( b==1 && !pause_roaming.isSelected() ) {
    pause_roaming.setSelected(true);
    pause_roaming.setBackground( Color.green );
    next_freq.setEnabled(false);
    prev_freq.setEnabled(false);
    freq_ch_lb.setEnabled(false);
  }
  else if( b==0 && pause_roaming.isSelected() ) {
    pause_roaming.setSelected(false);
    pause_roaming.setBackground( Color.gray );
    next_freq.setEnabled(true);
    prev_freq.setEnabled(true);
    freq_ch_lb.setEnabled(true);
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void update_tgid(int tg_id) {

  if(current_wacn_id==0 || current_wacn_id!=traffic_wacn_id) {
    return;
  }
  if(current_sys_id==0 || current_sys_id!=traffic_sys_id) {
    return;
  }


    if(tg_id==0) return;
    if(current_wacn_id==0) return;
    if(current_sys_id==0) return;

    if( on_control_freq==0 && current_sys_id!=0 && current_wacn_id!=0 && send_const_count>3) {
      tg_config.addUknownTG(parent, Integer.valueOf(tg_id).toString(), Integer.valueOf(current_sys_id).toString(), Integer.valueOf(current_wacn_id).toString() ); 
    }

    tg_zone = tg_config.find_tg_zone(this, Integer.valueOf(tg_id).toString() ); 
    if(tg_zone>0 && tg_zone<=16) {
      if(tg_zone==1 && prefs!=null) tg_zone_alias = prefs.get("zone1_alias","");
      if(tg_zone==2 && prefs!=null) tg_zone_alias = prefs.get("zone2_alias","");
      if(tg_zone==3 && prefs!=null) tg_zone_alias = prefs.get("zone3_alias","");
      if(tg_zone==4 && prefs!=null) tg_zone_alias = prefs.get("zone4_alias","");
      if(tg_zone==5 && prefs!=null) tg_zone_alias = prefs.get("zone5_alias","");
      if(tg_zone==6 && prefs!=null) tg_zone_alias = prefs.get("zone6_alias","");
      if(tg_zone==7 && prefs!=null) tg_zone_alias = prefs.get("zone7_alias","");
      if(tg_zone==8 && prefs!=null) tg_zone_alias = prefs.get("zone8_alias","");
      if(tg_zone==9 && prefs!=null) tg_zone_alias = prefs.get("zone9_alias","");
      if(tg_zone==10 && prefs!=null) tg_zone_alias = prefs.get("zone10_alias","");
      if(tg_zone==11 && prefs!=null) tg_zone_alias = prefs.get("zone11_alias","");
      if(tg_zone==12 && prefs!=null) tg_zone_alias = prefs.get("zone12_alias","");
    }
}



    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    void setAlias(String alias) {
      current_alias=alias;
    }
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    void setProgress(int pcomplete) {
      if(pcomplete<0) {
        progbar.setVisible(false);
        progbar.setValue(0);
        progress_label.setVisible(false);
      }
      else {
        progbar.setVisible(true);
        progress_label.setVisible(true);
        progbar.setValue(pcomplete);
      }
      repaint();
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    void setStatus(String str) {
      if(str==null) return;
      status.setVisible(true);
      status.setText("Status: "+str);
      status_timeout=1600;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        buttonGroup9 = new javax.swing.ButtonGroup();
        buttonGroup10 = new javax.swing.ButtonGroup();
        buttonGroup11 = new javax.swing.ButtonGroup();
        buttonGroup12 = new javax.swing.ButtonGroup();
        buttonGroup13 = new javax.swing.ButtonGroup();
        buttonGroup14 = new javax.swing.ButtonGroup();
        buttonGroup15 = new javax.swing.ButtonGroup();
        buttonGroup16 = new javax.swing.ButtonGroup();
        buttonGroup17 = new javax.swing.ButtonGroup();
        buttonGroup18 = new javax.swing.ButtonGroup();
        buttonGroup19 = new javax.swing.ButtonGroup();
        buttonGroup20 = new javax.swing.ButtonGroup();
        buttonGroup21 = new javax.swing.ButtonGroup();
        bottom_panel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        status_panel = new javax.swing.JPanel();
        status = new javax.swing.JLabel();
        tiny_const = new javax.swing.JPanel();
        jPanel57 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        z1 = new javax.swing.JToggleButton();
        z2 = new javax.swing.JToggleButton();
        z3 = new javax.swing.JToggleButton();
        z4 = new javax.swing.JToggleButton();
        z5 = new javax.swing.JToggleButton();
        z6 = new javax.swing.JToggleButton();
        z7 = new javax.swing.JToggleButton();
        z8 = new javax.swing.JToggleButton();
        z9 = new javax.swing.JToggleButton();
        z10 = new javax.swing.JToggleButton();
        z11 = new javax.swing.JToggleButton();
        z12 = new javax.swing.JToggleButton();
        hold1 = new javax.swing.JButton();
        skip1 = new javax.swing.JButton();
        mute = new javax.swing.JToggleButton();
        edit_alias1 = new javax.swing.JButton();
        jPanel70 = new javax.swing.JPanel();
        freq_ch_lb = new javax.swing.JLabel();
        prev_freq = new javax.swing.JButton();
        next_freq = new javax.swing.JButton();
        pause_roaming = new javax.swing.JButton();
        add_rid = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        progress_label = new javax.swing.JLabel();
        progbar = new javax.swing.JProgressBar();
        jSeparator2 = new javax.swing.JSeparator();
        meter_panel = new javax.swing.JPanel();
        desc_panel = new javax.swing.JPanel();
        level_panel = new javax.swing.JPanel();
        sq_lb = new javax.swing.JLabel();
        sq_indicator = new javax.swing.JToggleButton();
        tg_lb = new javax.swing.JLabel();
        tg_indicator = new javax.swing.JToggleButton();
        bt_lb = new javax.swing.JLabel();
        bt_indicator = new javax.swing.JToggleButton();
        jPanel50 = new javax.swing.JPanel();
        audio_prog = new javax.swing.JProgressBar();
        jTabbedPane1 = new javax.swing.JTabbedPane();

        p25rxconfigpanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        discover = new javax.swing.JButton();
        disconnect = new javax.swing.JButton();
        check_firmware = new javax.swing.JButton();
        write_config = new javax.swing.JButton();
        fw_ver = new javax.swing.JLabel();
        fw_installed = new javax.swing.JLabel();
        read_config = new javax.swing.JButton();
        en_bluetooth_cb = new javax.swing.JCheckBox();
        allow_unknown_tg_cb = new javax.swing.JCheckBox();
        enable_leds = new javax.swing.JCheckBox();
        frequency_tf1 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        no_voice_panel = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        no_voice_secs = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        roaming = new javax.swing.JCheckBox();
        freq_label = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        op_mode = new javax.swing.JComboBox<>();
        controlchannel = new javax.swing.JRadioButton();
        conventionalchannel = new javax.swing.JRadioButton();
        os_string = new javax.swing.JLabel();
        jPanel60 = new javax.swing.JPanel();
        status_format_cc = new javax.swing.JTextField();
        jPanel66 = new javax.swing.JPanel();
        status_format_voice = new javax.swing.JTextField();
        show_keywords = new javax.swing.JButton();
        jPanel67 = new javax.swing.JPanel();
        jLabel58 = new javax.swing.JLabel();
        audio_agc_max = new javax.swing.JTextField();
        jLabel59 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        system_alias = new javax.swing.JTextField();
        jPanel71 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        lineout_vol_slider = new javax.swing.JSlider();
        volume_label = new javax.swing.JLabel();
        jPanel72 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        bt_vol_slider1 = new javax.swing.JSlider();
        volume_label1 = new javax.swing.JLabel();
        jPanel73 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        vtimeout = new javax.swing.JComboBox<>();
        jPanel74 = new javax.swing.JPanel();
        jLabel51 = new javax.swing.JLabel();
        demod = new javax.swing.JComboBox<>();
        jSeparator49 = new javax.swing.JSeparator();
        p2_lsm = new javax.swing.JCheckBox();
        jPanel25 = new javax.swing.JPanel();
        jPanel26 = new javax.swing.JPanel();
        jPanel29 = new javax.swing.JPanel();
        dmr_cc_en1 = new javax.swing.JCheckBox();
        jSeparator4 = new javax.swing.JSeparator();
        dmr_lcn1_label = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        lcn1_freq = new javax.swing.JTextField();
        jSeparator38 = new javax.swing.JSeparator();
        dmr_clear_freqs = new javax.swing.JButton();
        jPanel31 = new javax.swing.JPanel();
        dmr_cc_en2 = new javax.swing.JCheckBox();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel36 = new javax.swing.JLabel();
        jSeparator7 = new javax.swing.JSeparator();
        lcn2_freq = new javax.swing.JTextField();
        jPanel32 = new javax.swing.JPanel();
        dmr_cc_en3 = new javax.swing.JCheckBox();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel37 = new javax.swing.JLabel();
        jSeparator9 = new javax.swing.JSeparator();
        lcn3_freq = new javax.swing.JTextField();
        jSeparator39 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        dmr_sys_id = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jPanel33 = new javax.swing.JPanel();
        dmr_cc_en4 = new javax.swing.JCheckBox();
        jSeparator10 = new javax.swing.JSeparator();
        jLabel38 = new javax.swing.JLabel();
        jSeparator11 = new javax.swing.JSeparator();
        lcn4_freq = new javax.swing.JTextField();
        jPanel34 = new javax.swing.JPanel();
        dmr_cc_en5 = new javax.swing.JCheckBox();
        jSeparator12 = new javax.swing.JSeparator();
        jLabel39 = new javax.swing.JLabel();
        jSeparator13 = new javax.swing.JSeparator();
        lcn5_freq = new javax.swing.JTextField();
        jPanel35 = new javax.swing.JPanel();
        dmr_cc_en6 = new javax.swing.JCheckBox();
        jSeparator14 = new javax.swing.JSeparator();
        jLabel40 = new javax.swing.JLabel();
        jSeparator15 = new javax.swing.JSeparator();
        lcn6_freq = new javax.swing.JTextField();
        jPanel36 = new javax.swing.JPanel();
        dmr_cc_en7 = new javax.swing.JCheckBox();
        jSeparator16 = new javax.swing.JSeparator();
        jLabel41 = new javax.swing.JLabel();
        jSeparator17 = new javax.swing.JSeparator();
        lcn7_freq = new javax.swing.JTextField();
        jPanel37 = new javax.swing.JPanel();
        dmr_cc_en8 = new javax.swing.JCheckBox();
        jSeparator18 = new javax.swing.JSeparator();
        jLabel42 = new javax.swing.JLabel();
        jSeparator19 = new javax.swing.JSeparator();
        lcn8_freq = new javax.swing.JTextField();
        jPanel38 = new javax.swing.JPanel();
        dmr_cc_en9 = new javax.swing.JCheckBox();
        jSeparator20 = new javax.swing.JSeparator();
        jLabel43 = new javax.swing.JLabel();
        jSeparator21 = new javax.swing.JSeparator();
        lcn9_freq = new javax.swing.JTextField();
        jPanel39 = new javax.swing.JPanel();
        dmr_cc_en10 = new javax.swing.JCheckBox();
        jSeparator22 = new javax.swing.JSeparator();
        jLabel44 = new javax.swing.JLabel();
        jSeparator23 = new javax.swing.JSeparator();
        lcn10_freq = new javax.swing.JTextField();
        jPanel40 = new javax.swing.JPanel();
        dmr_cc_en11 = new javax.swing.JCheckBox();
        jSeparator24 = new javax.swing.JSeparator();
        jLabel45 = new javax.swing.JLabel();
        jSeparator25 = new javax.swing.JSeparator();
        lcn11_freq = new javax.swing.JTextField();
        jPanel41 = new javax.swing.JPanel();
        dmr_cc_en12 = new javax.swing.JCheckBox();
        jSeparator26 = new javax.swing.JSeparator();
        jLabel46 = new javax.swing.JLabel();
        jSeparator27 = new javax.swing.JSeparator();
        lcn12_freq = new javax.swing.JTextField();
        jPanel42 = new javax.swing.JPanel();
        dmr_cc_en13 = new javax.swing.JCheckBox();
        jSeparator28 = new javax.swing.JSeparator();
        jLabel47 = new javax.swing.JLabel();
        jSeparator29 = new javax.swing.JSeparator();
        lcn13_freq = new javax.swing.JTextField();
        jPanel43 = new javax.swing.JPanel();
        dmr_cc_en14 = new javax.swing.JCheckBox();
        jSeparator30 = new javax.swing.JSeparator();
        jLabel48 = new javax.swing.JLabel();
        jSeparator31 = new javax.swing.JSeparator();
        lcn14_freq = new javax.swing.JTextField();
        jPanel44 = new javax.swing.JPanel();
        dmr_cc_en15 = new javax.swing.JCheckBox();
        jSeparator32 = new javax.swing.JSeparator();
        jLabel49 = new javax.swing.JLabel();
        jSeparator33 = new javax.swing.JSeparator();
        lcn15_freq = new javax.swing.JTextField();
        jPanel46 = new javax.swing.JPanel();
        dmr_conplus = new javax.swing.JRadioButton();
        dmr_conventional = new javax.swing.JRadioButton();
        jSeparator36 = new javax.swing.JSeparator();
        dmr_slot1 = new javax.swing.JCheckBox();
        dmr_slot2 = new javax.swing.JCheckBox();
        jSeparator37 = new javax.swing.JSeparator();
        jPanel30 = new javax.swing.JPanel();
        dmr_backup = new javax.swing.JButton();
        jSeparator34 = new javax.swing.JSeparator();
        dmr_restore = new javax.swing.JButton();
        jSeparator35 = new javax.swing.JSeparator();
        dmr_write_config = new javax.swing.JButton();
        jPanel47 = new javax.swing.JPanel();
        audiopanel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        enable_mp3 = new javax.swing.JCheckBox();
        enable_audio = new javax.swing.JCheckBox();
        mp3_separate_files = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        audio_dev_list = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        select_home = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        home_dir_label = new javax.swing.JLabel();
        do_mp3 = new javax.swing.JRadioButton();
        do_wav = new javax.swing.JRadioButton();
        audio_hiq = new javax.swing.JRadioButton();
        audio_lowq = new javax.swing.JRadioButton();
        jLabel28 = new javax.swing.JLabel();
        jPanel59 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        end_call_silence = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        separate_rid = new javax.swing.JCheckBox();
        en_rdio = new javax.swing.JCheckBox();
        jLabel64 = new javax.swing.JLabel();
        rdio_mask = new javax.swing.JTextField();
        en_broadcastify_calls = new javax.swing.JCheckBox();
        jPanel13 = new javax.swing.JPanel();
        freqdb_panel = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        freq_table = new javax.swing.JTable();
        jPanel16 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        inc_bus = new javax.swing.JCheckBox();
        inc_gov = new javax.swing.JCheckBox();
        inc_trunked_only = new javax.swing.JCheckBox();
        inc_p25 = new javax.swing.JCheckBox();
        inc_dmr = new javax.swing.JCheckBox();
        jPanel18 = new javax.swing.JPanel();
        inc_vhf = new javax.swing.JCheckBox();
        inc_400mhz = new javax.swing.JCheckBox();
        inc_700mhz = new javax.swing.JCheckBox();
        inc_800mhz = new javax.swing.JCheckBox();
        inc_900mhz = new javax.swing.JCheckBox();
        inc_dup_freq = new javax.swing.JCheckBox();
        jPanel14 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        search_radius = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        zipcode = new javax.swing.JTextField();
        freq_search = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel16 = new javax.swing.JLabel();
        city = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        state = new javax.swing.JTextField();
        freq_search2 = new javax.swing.JButton();
        jPanel19 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        testfreqs = new javax.swing.JButton();
        append_cc = new javax.swing.JButton();
        use_freq_primary = new javax.swing.JButton();
        gen_sys_info = new javax.swing.JButton();
        read_roaming = new javax.swing.JButton();
        monitor_sel = new javax.swing.JButton();
        jPanel20 = new javax.swing.JPanel();
        restore_roam = new javax.swing.JButton();
        backup_roam = new javax.swing.JButton();
        erase_roaming = new javax.swing.JButton();
        delete_roaming = new javax.swing.JButton();
        jPanel62 = new javax.swing.JPanel();
        add_primary = new javax.swing.JCheckBox();
        add_secondaries = new javax.swing.JCheckBox();
        add_neighbors = new javax.swing.JCheckBox();
        talkgroup_panel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel22 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        enable_table_rows = new javax.swing.JButton();
        disable_table_rows = new javax.swing.JButton();
        read_tg = new javax.swing.JButton();
        send_tg = new javax.swing.JButton();
        backup_tg = new javax.swing.JButton();
        tg_edit_del = new javax.swing.JButton();
        set_zones = new javax.swing.JButton();
        jPanel23 = new javax.swing.JPanel();
        restore_tg = new javax.swing.JButton();
        import_csv = new javax.swing.JButton();
        auto_flash_tg = new javax.swing.JCheckBox();
        disable_encrypted = new javax.swing.JCheckBox();
        auto_pop_table = new javax.swing.JCheckBox();
        jPanel69 = new javax.swing.JPanel();
        consolePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jPanel51 = new javax.swing.JPanel();
        en_evt_output = new javax.swing.JCheckBox();
        logpanel = new javax.swing.JPanel();
        tg_scroll_pane = new javax.swing.JScrollPane();
        log_ta = new javax.swing.JTextArea();
        tgfontpanel = new javax.swing.JPanel();
        tglog_font = new javax.swing.JButton();
        tglog_color = new javax.swing.JButton();
        tglog_edit = new javax.swing.JButton();
        buttong_config = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        button_write_config = new javax.swing.JButton();
        jPanel27 = new javax.swing.JPanel();
        single_click_opt1 = new javax.swing.JRadioButton();
        single_click_opt2 = new javax.swing.JRadioButton();
        single_click_opt3 = new javax.swing.JRadioButton();
        single_click_opt4 = new javax.swing.JRadioButton();
        single_click_opt5 = new javax.swing.JRadioButton();
        single_click_opt6 = new javax.swing.JRadioButton();
        jPanel28 = new javax.swing.JPanel();
        double_click_opt1 = new javax.swing.JRadioButton();
        double_click_opt2 = new javax.swing.JRadioButton();
        double_click_opt3 = new javax.swing.JRadioButton();
        double_click_opt4 = new javax.swing.JRadioButton();
        double_click_opt5 = new javax.swing.JRadioButton();
        double_click_opt6 = new javax.swing.JRadioButton();
        jPanel45 = new javax.swing.JPanel();
        triple_click_opt1 = new javax.swing.JRadioButton();
        triple_click_opt2 = new javax.swing.JRadioButton();
        triple_click_opt3 = new javax.swing.JRadioButton();
        triple_click_opt4 = new javax.swing.JRadioButton();
        triple_click_opt5 = new javax.swing.JRadioButton();
        triple_click_opt6 = new javax.swing.JRadioButton();
        jPanel48 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        skip_tg_to = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jSeparator40 = new javax.swing.JSeparator();
        roaming_ret_to_cc = new javax.swing.JCheckBox();
        jLabel33 = new javax.swing.JLabel();
        jPanel49 = new javax.swing.JPanel();
        quad_click_opt1 = new javax.swing.JRadioButton();
        quad_click_opt2 = new javax.swing.JRadioButton();
        quad_click_opt3 = new javax.swing.JRadioButton();
        quad_click_opt4 = new javax.swing.JRadioButton();
        quad_click_opt5 = new javax.swing.JRadioButton();
        quad_click_opt6 = new javax.swing.JRadioButton();
        alias_panel = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        alias_table = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        import_alias = new javax.swing.JButton();
        advancedpanel = new javax.swing.JPanel();
        adv_write_config = new javax.swing.JButton();
        en_encout = new javax.swing.JCheckBox();
        en_p2_tones = new javax.swing.JCheckBox();
        p25_tone_vol = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        en_zero_rid = new javax.swing.JCheckBox();
        enc_mode = new javax.swing.JCheckBox();
        allow_tg_pri_int = new javax.swing.JCheckBox();
        process_rid_alias = new javax.swing.JCheckBox();
        en_tg_int_tone = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        jPanel75 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        vga_target = new javax.swing.JTextField();
        jLabel53 = new javax.swing.JLabel();
        jSeparator44 = new javax.swing.JSeparator();
        jLabel62 = new javax.swing.JLabel();
        vga_step = new javax.swing.JTextField();
        jLabel63 = new javax.swing.JLabel();
        jSeparator45 = new javax.swing.JSeparator();
        jSeparator47 = new javax.swing.JSeparator();
        save_iq = new javax.swing.JButton();
        jPanel76 = new javax.swing.JPanel();
        jLabel60 = new javax.swing.JLabel();
        jSeparator48 = new javax.swing.JSeparator();
        jPanel53 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        rfgain = new javax.swing.JComboBox<>();
        jSeparator42 = new javax.swing.JSeparator();
        jLabel35 = new javax.swing.JLabel();
        mixgain = new javax.swing.JComboBox<>();
        jSeparator43 = new javax.swing.JSeparator();
        jLabel29 = new javax.swing.JLabel();
        vga_gain = new javax.swing.JComboBox<>();
        jSeparator46 = new javax.swing.JSeparator();
        jLabel56 = new javax.swing.JLabel();
        rf_hyst = new javax.swing.JComboBox<>();
        reset_defaults = new javax.swing.JButton();
        jPanel63 = new javax.swing.JPanel();
        jPanel64 = new javax.swing.JPanel();
        jLabel57 = new javax.swing.JLabel();
        p1_sync_thresh = new javax.swing.JTextField();
        jLabel65 = new javax.swing.JLabel();
        jPanel65 = new javax.swing.JPanel();
        jLabel50 = new javax.swing.JLabel();
        p2_sync_thresh = new javax.swing.JTextField();
        jLabel66 = new javax.swing.JLabel();
        jPanel54 = new javax.swing.JPanel();
        usb_slow = new javax.swing.JRadioButton();
        usb_med = new javax.swing.JRadioButton();
        usb_fast = new javax.swing.JRadioButton();
        jPanel56 = new javax.swing.JPanel();
        jLabel52 = new javax.swing.JLabel();
        enc_timeout = new javax.swing.JTextField();
        jLabel54 = new javax.swing.JLabel();
        enc_count = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        vrep = new javax.swing.JCheckBox();
        signalinsightpanel = new javax.swing.JPanel();
        const_panel = new javax.swing.JPanel();
        jPanel78 = new javax.swing.JPanel();
        jPanel55 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        autoscale_const = new javax.swing.JCheckBox();
        nsymbols = new javax.swing.JComboBox<>();
        jSeparator41 = new javax.swing.JSeparator();
        jPanel68 = new javax.swing.JPanel();
        si_cpu_high = new javax.swing.JRadioButton();
        si_cpu_normal = new javax.swing.JRadioButton();
        si_cpu_low = new javax.swing.JRadioButton();
        si_cpu_battery_saving = new javax.swing.JRadioButton();
        si_cpu_off = new javax.swing.JRadioButton();
        vga_cal = new javax.swing.JButton();
        freemem = new javax.swing.JButton();
        jPanel58 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        if_slide = new javax.swing.JSlider();
        jLabel27 = new javax.swing.JLabel();
        sig_cmd = new javax.swing.JTextField();
        displayviewmain_border = new javax.swing.JPanel();
        display_frame = new javax.swing.JPanel();
        jPanel61 = new javax.swing.JPanel();
        hold = new javax.swing.JButton();
        skip = new javax.swing.JButton();
        edit_alias = new javax.swing.JButton();
        edit_display_view = new javax.swing.JButton();
        dvpopout = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        logo_panel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        wacn = new javax.swing.JLabel();
        sysid = new javax.swing.JLabel();
        nac = new javax.swing.JLabel();
        freq = new javax.swing.JLabel();
        siteid = new javax.swing.JLabel();
        rfid = new javax.swing.JLabel();
        macid = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        minimize = new javax.swing.JToggleButton();
        record_to_mp3 = new javax.swing.JToggleButton();
        release_date = new javax.swing.JLabel();
        ser_dev = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        bottom_panel.setLayout(new javax.swing.BoxLayout(bottom_panel, javax.swing.BoxLayout.Y_AXIS));

        jPanel9.setLayout(new java.awt.BorderLayout());

        status_panel.setBackground(new java.awt.Color(0, 0, 0));
        status_panel.setMinimumSize(new java.awt.Dimension(99, 33));
        status_panel.setPreferredSize(new java.awt.Dimension(1004, 33));
        status_panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 7));

        status.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        status.setForeground(new java.awt.Color(255, 255, 255));
        status.setText("Status: Idle");
        status.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        status_panel.add(status);

        jPanel9.add(status_panel, java.awt.BorderLayout.CENTER);

        tiny_const.setBackground(new java.awt.Color(0, 0, 0));
        tiny_const.setPreferredSize(new java.awt.Dimension(33, 33));
        tiny_const.setRequestFocusEnabled(false);
        jPanel9.add(tiny_const, java.awt.BorderLayout.EAST);

        jPanel57.setBackground(new java.awt.Color(0, 0, 0));

        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("Zones");
        jPanel57.add(jLabel23);

        z1.setBackground(new java.awt.Color(204, 204, 204));
        z1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z1.setText("1");
        z1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z1ActionPerformed(evt);
            }
        });
        jPanel57.add(z1);

        z2.setBackground(new java.awt.Color(204, 204, 204));
        z2.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z2.setText("2");
        z2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z2ActionPerformed(evt);
            }
        });
        jPanel57.add(z2);

        z3.setBackground(new java.awt.Color(204, 204, 204));
        z3.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z3.setText("3");
        z3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z3ActionPerformed(evt);
            }
        });
        jPanel57.add(z3);

        z4.setBackground(new java.awt.Color(204, 204, 204));
        z4.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z4.setText("4");
        z4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z4ActionPerformed(evt);
            }
        });
        jPanel57.add(z4);

        z5.setBackground(new java.awt.Color(204, 204, 204));
        z5.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z5.setText("5");
        z5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z5ActionPerformed(evt);
            }
        });
        jPanel57.add(z5);

        z6.setBackground(new java.awt.Color(204, 204, 204));
        z6.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z6.setText("6");
        z6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z6ActionPerformed(evt);
            }
        });
        jPanel57.add(z6);

        z7.setBackground(new java.awt.Color(204, 204, 204));
        z7.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z7.setText("7");
        z7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z7ActionPerformed(evt);
            }
        });
        jPanel57.add(z7);

        z8.setBackground(new java.awt.Color(204, 204, 204));
        z8.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z8.setText("8");
        z8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z8ActionPerformed(evt);
            }
        });
        jPanel57.add(z8);

        z9.setBackground(new java.awt.Color(204, 204, 204));
        z9.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z9.setText("9");
        z9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z9ActionPerformed(evt);
            }
        });
        jPanel57.add(z9);

        z10.setBackground(new java.awt.Color(204, 204, 204));
        z10.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z10.setText("10");
        z10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z10ActionPerformed(evt);
            }
        });
        jPanel57.add(z10);

        z11.setBackground(new java.awt.Color(204, 204, 204));
        z11.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z11.setText("11");
        z11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z11ActionPerformed(evt);
            }
        });
        jPanel57.add(z11);

        z12.setBackground(new java.awt.Color(204, 204, 204));
        z12.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z12.setText("12");
        z12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z12ActionPerformed(evt);
            }
        });
        jPanel57.add(z12);

        hold1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        hold1.setText("H");
        hold1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hold1ActionPerformed(evt);
            }
        });
        jPanel57.add(hold1);

        skip1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        skip1.setText("S");
        skip1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skip1ActionPerformed(evt);
            }
        });
        jPanel57.add(skip1);

        mute.setBackground(new java.awt.Color(204, 204, 204));
        mute.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mute.setText("MUTE");
        mute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                muteActionPerformed(evt);
            }
        });
        jPanel57.add(mute);

        edit_alias1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        edit_alias1.setText("Edit Alias");
        edit_alias1.setEnabled(false);
        edit_alias1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edit_alias1ActionPerformed(evt);
            }
        });
        jPanel57.add(edit_alias1);

        jPanel9.add(jPanel57, java.awt.BorderLayout.EAST);

        bottom_panel.add(jPanel9);

        jPanel70.setBackground(new java.awt.Color(0, 0, 0));
        jPanel70.setForeground(new java.awt.Color(255, 255, 255));
        jPanel70.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        freq_ch_lb.setForeground(new java.awt.Color(255, 255, 255));
        freq_ch_lb.setText("FREQ");
        jPanel70.add(freq_ch_lb);

        prev_freq.setText("<");
        prev_freq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prev_freqActionPerformed(evt);
            }
        });
        jPanel70.add(prev_freq);

        next_freq.setText(">");
        next_freq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                next_freqActionPerformed(evt);
            }
        });
        jPanel70.add(next_freq);

        pause_roaming.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        pause_roaming.setText("R");
        pause_roaming.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pause_roamingActionPerformed(evt);
            }
        });
        jPanel70.add(pause_roaming);

        add_rid.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        add_rid.setText("Add RID");
        add_rid.setEnabled(false);
        add_rid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add_ridActionPerformed(evt);
            }
        });
        jPanel70.add(add_rid);

        bottom_panel.add(jPanel70);

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        progress_label.setForeground(new java.awt.Color(255, 255, 255));
        progress_label.setText("Working...");
        jPanel2.add(progress_label);

        progbar.setBackground(new java.awt.Color(204, 204, 204));
        progbar.setForeground(new java.awt.Color(102, 102, 102));
        progbar.setToolTipText("");
        progbar.setDoubleBuffered(true);
        progbar.setPreferredSize(new java.awt.Dimension(320, 14));
        progbar.setStringPainted(true);
        jPanel2.add(progbar);

        jSeparator2.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator2.setEnabled(false);
        jSeparator2.setPreferredSize(new java.awt.Dimension(250, 0));
        jPanel2.add(jSeparator2);

        bottom_panel.add(jPanel2);

        meter_panel.setBackground(new java.awt.Color(0, 0, 0));
        meter_panel.setForeground(new java.awt.Color(255, 255, 255));
        meter_panel.setLayout(new javax.swing.BoxLayout(meter_panel, javax.swing.BoxLayout.LINE_AXIS));

        desc_panel.setBackground(new java.awt.Color(0, 0, 0));
        desc_panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        meter_panel.add(desc_panel);

        level_panel.setBackground(new java.awt.Color(0, 0, 0));
        level_panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        sq_lb.setForeground(new java.awt.Color(255, 255, 255));
        sq_lb.setText("SIG");
        level_panel.add(sq_lb);

        sq_indicator.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        sq_indicator.setText("SQ");
        sq_indicator.setToolTipText("");
        sq_indicator.setBorderPainted(false);
        sq_indicator.setFocusPainted(false);
        level_panel.add(sq_indicator);

        tg_lb.setForeground(new java.awt.Color(255, 255, 255));
        tg_lb.setText("TG");
        level_panel.add(tg_lb);

        tg_indicator.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        tg_indicator.setText("TG");
        tg_indicator.setToolTipText("");
        tg_indicator.setBorderPainted(false);
        tg_indicator.setFocusPainted(false);
        level_panel.add(tg_indicator);

        bt_lb.setForeground(new java.awt.Color(255, 255, 255));
        bt_lb.setText("BT");
        level_panel.add(bt_lb);

        bt_indicator.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        bt_indicator.setText("BT");
        bt_indicator.setToolTipText("");
        bt_indicator.setBorderPainted(false);
        bt_indicator.setFocusPainted(false);
        level_panel.add(bt_indicator);

        meter_panel.add(level_panel);

        bottom_panel.add(meter_panel);

        jPanel50.setBackground(new java.awt.Color(0, 0, 0));
        jPanel50.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        audio_prog.setBackground(new java.awt.Color(0, 0, 0));
        audio_prog.setForeground(new java.awt.Color(0, 255, 0));
        audio_prog.setPreferredSize(new java.awt.Dimension(1100, 10));
        jPanel50.add(audio_prog);

        bottom_panel.add(jPanel50);

        getContentPane().add(bottom_panel, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(1115, 659));
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPane1MouseClicked(evt);
            }
        });

        p25rxconfigpanel.setMinimumSize(new java.awt.Dimension(1110, 554));
        p25rxconfigpanel.setPreferredSize(new java.awt.Dimension(1110, 554));
        p25rxconfigpanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        discover.setText("Connect To P25RX");
        discover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discoverActionPerformed(evt);
            }
        });
        jPanel12.add(discover);

        disconnect.setText("Disconnect");
        disconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectActionPerformed(evt);
            }
        });
        jPanel12.add(disconnect);

        check_firmware.setText("Install Latest Firmware");
        check_firmware.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                check_firmwareActionPerformed(evt);
            }
        });
        jPanel12.add(check_firmware);

        write_config.setText("Write Config To P25RX");
        write_config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                write_configActionPerformed(evt);
            }
        });
        jPanel12.add(write_config);

        fw_ver.setText("FW Date: 2020-03-04 16:00 ");
        jPanel12.add(fw_ver);

        fw_installed.setText("FW currently installed:");
        jPanel12.add(fw_installed);

        read_config.setText("Read Config From P25RX");
        read_config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                read_configActionPerformed(evt);
            }
        });
        jPanel12.add(read_config);

        jPanel1.add(jPanel12);

        p25rxconfigpanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 444, 1110, 70));

        en_bluetooth_cb.setSelected(true);
        en_bluetooth_cb.setText("Enable Bluetooth On Power-Up");
        en_bluetooth_cb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_bluetooth_cbActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(en_bluetooth_cb, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 220, -1, -1));

        allow_unknown_tg_cb.setSelected(true);
        allow_unknown_tg_cb.setText("Allow Unknown Talkgroups");
        allow_unknown_tg_cb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allow_unknown_tg_cbActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(allow_unknown_tg_cb, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 330, -1, -1));

        enable_leds.setSelected(true);
        enable_leds.setText("Enable Status LEDS");
        p25rxconfigpanel.add(enable_leds, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 360, -1, -1));

        frequency_tf1.setColumns(10);
        frequency_tf1.setToolTipText("Optionally, Use The <Search DB For Nearby Control Channels> Tab To Configure The Primary Control Channel.");
        p25rxconfigpanel.add(frequency_tf1, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 40, -1, 30));

        jLabel7.setText("MHz");
        p25rxconfigpanel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 50, -1, -1));

        jLabel2.setText("<primary>");
        p25rxconfigpanel.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 50, -1, -1));

        jLabel25.setText("Switch Control Channel After");
        no_voice_panel.add(jLabel25);

        no_voice_secs.setColumns(5);
        no_voice_secs.setText("180");
        no_voice_panel.add(no_voice_secs);

        jLabel24.setText("Secs Of Inactivity ( 0=USE CC BUSY STATUS )");
        no_voice_panel.add(jLabel24);

        p25rxconfigpanel.add(no_voice_panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 390, 720, 40));

        roaming.setText("Enable Roaming");
        roaming.setToolTipText("This option is intended for mobile operation.  Disable if you itend to listen to a single local control channel.");
        roaming.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                roamingActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(roaming, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 390, -1, -1));

        freq_label.setText("Control Channel Frequency");
        p25rxconfigpanel.add(freq_label, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 50, -1, -1));

        jLabel6.setText("Mode");
        p25rxconfigpanel.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 90, -1, -1));

        op_mode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "P25 I/II", "DMR Con+", "TDMA CC", "NXDN4800" }));
        op_mode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_modeActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(op_mode, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 80, 140, 30));

        buttonGroup7.add(controlchannel);
        controlchannel.setSelected(true);
        controlchannel.setText("Control Channel");
        controlchannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controlchannelActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(controlchannel, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 90, -1, -1));

        buttonGroup7.add(conventionalchannel);
        conventionalchannel.setText("Conventional");
        conventionalchannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conventionalchannelActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(conventionalchannel, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 90, -1, -1));

        os_string.setText("OS: ");
        p25rxconfigpanel.add(os_string, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jPanel60.setBorder(javax.swing.BorderFactory.createTitledBorder("Status Format On CC"));

        status_format_cc.setColumns(50);
        status_format_cc.setText("CC $P25_MODE$ B/SEC $BLKS_SEC$  $WACN$-$SYS_ID$-$NAC$, $FREQ$ MHz");
        status_format_cc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                status_format_ccActionPerformed(evt);
            }
        });
        jPanel60.add(status_format_cc);

        p25rxconfigpanel.add(jPanel60, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 120, 610, 80));

        jPanel66.setBorder(javax.swing.BorderFactory.createTitledBorder("Status Format Voice"));

        status_format_voice.setColumns(50);
        status_format_voice.setText("$P25_MODE$ $V_FREQ$ MHz, TG $TG_ID$, $TG_NAME$");
        jPanel66.add(status_format_voice);

        p25rxconfigpanel.add(jPanel66, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 220, 610, 80));

        show_keywords.setText("Show KeyWords");
        show_keywords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                show_keywordsActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(show_keywords, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 310, -1, -1));

        jPanel67.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel58.setText("Audio AGC Max Gain");
        jPanel67.add(jLabel58);

        audio_agc_max.setColumns(5);
        audio_agc_max.setText("0.7");
        jPanel67.add(audio_agc_max);

        jLabel59.setText("(default 0.7)");
        jPanel67.add(jLabel59);

        p25rxconfigpanel.add(jPanel67, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 290, 380, 40));

        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel11.setText("System Alias");
        jPanel7.add(jLabel11);

        system_alias.setPreferredSize(new java.awt.Dimension(250, 25));
        jPanel7.add(system_alias);

        p25rxconfigpanel.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 250, 420, 40));

        jPanel71.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel5.setText("Line Out Volume");
        jPanel71.add(jLabel5);

        lineout_vol_slider.setPaintLabels(true);
        lineout_vol_slider.setPaintTicks(true);
        lineout_vol_slider.setToolTipText("This option control the audio line-out level for driving powered speakers or line-in on an external device.");
        lineout_vol_slider.setValue(100);
        lineout_vol_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lineout_vol_sliderStateChanged(evt);
            }
        });
        jPanel71.add(lineout_vol_slider);

        volume_label.setText("1.0 (Def 0.5)");
        jPanel71.add(volume_label);

        p25rxconfigpanel.add(jPanel71, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 130, 460, 40));

        jPanel72.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel13.setText("Bluetooth Volume");
        jPanel72.add(jLabel13);

        bt_vol_slider1.setPaintLabels(true);
        bt_vol_slider1.setPaintTicks(true);
        bt_vol_slider1.setToolTipText("This option control the audio line-out level for driving powered speakers or line-in on an external device.");
        bt_vol_slider1.setValue(100);
        bt_vol_slider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bt_vol_slider1StateChanged(evt);
            }
        });
        jPanel72.add(bt_vol_slider1);

        volume_label1.setText("1.0 (Def 1.0)");
        jPanel72.add(volume_label1);

        p25rxconfigpanel.add(jPanel72, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 170, 490, 40));

        jLabel34.setText("Talk Group Timeout");
        jPanel73.add(jLabel34);

        vtimeout.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "100ms", "250ms", "500ms", "1sec", "1.5sec", "2sec", "3sec", "5sec", "10sec", "30sec" }));
        vtimeout.setToolTipText("The time since the last activity on a talk group before the receiver will follow a different talk group.");
        jPanel73.add(vtimeout);

        p25rxconfigpanel.add(jPanel73, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 310, 250, 50));

        jPanel74.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel51.setText("P25 Modulation");
        jPanel74.add(jLabel51);

        demod.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "LSM (simulcast)", "CQPSK/C4FM" }));
        jPanel74.add(demod);

        jSeparator49.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel74.add(jSeparator49);

        p25rxconfigpanel.add(jPanel74, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 40, 570, 40));

        p2_lsm.setSelected(true);
        p2_lsm.setText("P25 P2 Always Uses LSM Demod");
        p25rxconfigpanel.add(p2_lsm, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 80, -1, -1));

        jTabbedPane1.addTab("P25RX Config", p25rxconfigpanel);

        jPanel25.setLayout(new java.awt.BorderLayout());

        jPanel26.setLayout(new java.awt.GridLayout(16, 0));

        jPanel29.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en1.setText("Control Channel");
        jPanel29.add(dmr_cc_en1);

        jSeparator4.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator4.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel29.add(jSeparator4);

        dmr_lcn1_label.setText("LCN1 Frequency");
        jPanel29.add(dmr_lcn1_label);

        jSeparator5.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator5.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel29.add(jSeparator5);

        lcn1_freq.setColumns(15);
        jPanel29.add(lcn1_freq);

        jSeparator38.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator38.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel29.add(jSeparator38);

        dmr_clear_freqs.setText("Clear All Frequencies");
        dmr_clear_freqs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dmr_clear_freqsActionPerformed(evt);
            }
        });
        jPanel29.add(dmr_clear_freqs);

        jPanel26.add(jPanel29);

        jPanel31.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en2.setText("Control Channel");
        jPanel31.add(dmr_cc_en2);

        jSeparator6.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator6.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel31.add(jSeparator6);

        jLabel36.setText("LCN2 Frequency");
        jPanel31.add(jLabel36);

        jSeparator7.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator7.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel31.add(jSeparator7);

        lcn2_freq.setColumns(15);
        jPanel31.add(lcn2_freq);

        jPanel26.add(jPanel31);

        jPanel32.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en3.setText("Control Channel");
        jPanel32.add(dmr_cc_en3);

        jSeparator8.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator8.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel32.add(jSeparator8);

        jLabel37.setText("LCN3 Frequency");
        jPanel32.add(jLabel37);

        jSeparator9.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator9.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel32.add(jSeparator9);

        lcn3_freq.setColumns(15);
        jPanel32.add(lcn3_freq);

        jSeparator39.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator39.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel32.add(jSeparator39);

        jLabel8.setText("Sys ID");
        jPanel32.add(jLabel8);

        dmr_sys_id.setColumns(5);
        dmr_sys_id.setText("1");
        jPanel32.add(dmr_sys_id);

        jLabel26.setText("Decimal");
        jPanel32.add(jLabel26);

        jPanel26.add(jPanel32);

        jPanel33.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en4.setText("Control Channel");
        jPanel33.add(dmr_cc_en4);

        jSeparator10.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator10.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel33.add(jSeparator10);

        jLabel38.setText("LCN4 Frequency");
        jPanel33.add(jLabel38);

        jSeparator11.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator11.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel33.add(jSeparator11);

        lcn4_freq.setColumns(15);
        jPanel33.add(lcn4_freq);

        jPanel26.add(jPanel33);

        jPanel34.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en5.setText("Control Channel");
        jPanel34.add(dmr_cc_en5);

        jSeparator12.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator12.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel34.add(jSeparator12);

        jLabel39.setText("LCN5 Frequency");
        jPanel34.add(jLabel39);

        jSeparator13.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator13.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel34.add(jSeparator13);

        lcn5_freq.setColumns(15);
        jPanel34.add(lcn5_freq);

        jPanel26.add(jPanel34);

        jPanel35.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en6.setText("Control Channel");
        jPanel35.add(dmr_cc_en6);

        jSeparator14.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator14.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel35.add(jSeparator14);

        jLabel40.setText("LCN6 Frequency");
        jPanel35.add(jLabel40);

        jSeparator15.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator15.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel35.add(jSeparator15);

        lcn6_freq.setColumns(15);
        jPanel35.add(lcn6_freq);

        jPanel26.add(jPanel35);

        jPanel36.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en7.setText("Control Channel");
        jPanel36.add(dmr_cc_en7);

        jSeparator16.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator16.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel36.add(jSeparator16);

        jLabel41.setText("LCN7 Frequency");
        jPanel36.add(jLabel41);

        jSeparator17.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator17.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel36.add(jSeparator17);

        lcn7_freq.setColumns(15);
        jPanel36.add(lcn7_freq);

        jPanel26.add(jPanel36);

        jPanel37.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en8.setText("Control Channel");
        jPanel37.add(dmr_cc_en8);

        jSeparator18.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator18.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel37.add(jSeparator18);

        jLabel42.setText("LCN8 Frequency");
        jPanel37.add(jLabel42);

        jSeparator19.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator19.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel37.add(jSeparator19);

        lcn8_freq.setColumns(15);
        jPanel37.add(lcn8_freq);

        jPanel26.add(jPanel37);

        jPanel38.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en9.setText("Control Channel");
        jPanel38.add(dmr_cc_en9);

        jSeparator20.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator20.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel38.add(jSeparator20);

        jLabel43.setText("LCN9 Frequency");
        jPanel38.add(jLabel43);

        jSeparator21.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator21.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel38.add(jSeparator21);

        lcn9_freq.setColumns(15);
        jPanel38.add(lcn9_freq);

        jPanel26.add(jPanel38);

        jPanel39.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en10.setText("Control Channel");
        jPanel39.add(dmr_cc_en10);

        jSeparator22.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator22.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel39.add(jSeparator22);

        jLabel44.setText("LCN10 Frequency");
        jPanel39.add(jLabel44);

        jSeparator23.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator23.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel39.add(jSeparator23);

        lcn10_freq.setColumns(15);
        jPanel39.add(lcn10_freq);

        jPanel26.add(jPanel39);

        jPanel40.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en11.setText("Control Channel");
        jPanel40.add(dmr_cc_en11);

        jSeparator24.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator24.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel40.add(jSeparator24);

        jLabel45.setText("LCN11 Frequency");
        jPanel40.add(jLabel45);

        jSeparator25.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator25.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel40.add(jSeparator25);

        lcn11_freq.setColumns(15);
        jPanel40.add(lcn11_freq);

        jPanel26.add(jPanel40);

        jPanel41.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en12.setText("Control Channel");
        jPanel41.add(dmr_cc_en12);

        jSeparator26.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator26.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel41.add(jSeparator26);

        jLabel46.setText("LCN12 Frequency");
        jPanel41.add(jLabel46);

        jSeparator27.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator27.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel41.add(jSeparator27);

        lcn12_freq.setColumns(15);
        jPanel41.add(lcn12_freq);

        jPanel26.add(jPanel41);

        jPanel42.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en13.setText("Control Channel");
        jPanel42.add(dmr_cc_en13);

        jSeparator28.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator28.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel42.add(jSeparator28);

        jLabel47.setText("LCN13 Frequency");
        jPanel42.add(jLabel47);

        jSeparator29.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator29.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel42.add(jSeparator29);

        lcn13_freq.setColumns(15);
        jPanel42.add(lcn13_freq);

        jPanel26.add(jPanel42);

        jPanel43.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en14.setText("Control Channel");
        jPanel43.add(dmr_cc_en14);

        jSeparator30.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator30.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel43.add(jSeparator30);

        jLabel48.setText("LCN14 Frequency");
        jPanel43.add(jLabel48);

        jSeparator31.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator31.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel43.add(jSeparator31);

        lcn14_freq.setColumns(15);
        jPanel43.add(lcn14_freq);

        jPanel26.add(jPanel43);

        jPanel44.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        dmr_cc_en15.setText("Control Channel");
        jPanel44.add(dmr_cc_en15);

        jSeparator32.setMinimumSize(new java.awt.Dimension(150, 0));
        jSeparator32.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel44.add(jSeparator32);

        jLabel49.setText("LCN15 Frequency");
        jPanel44.add(jLabel49);

        jSeparator33.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator33.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel44.add(jSeparator33);

        lcn15_freq.setColumns(15);
        jPanel44.add(lcn15_freq);

        jPanel26.add(jPanel44);

        jPanel46.setPreferredSize(new java.awt.Dimension(885, 50));

        buttonGroup6.add(dmr_conplus);
        dmr_conplus.setSelected(true);
        dmr_conplus.setText("Con+ Control");
        dmr_conplus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dmr_conplusActionPerformed(evt);
            }
        });
        jPanel46.add(dmr_conplus);

        buttonGroup6.add(dmr_conventional);
        dmr_conventional.setText("Conventional");
        dmr_conventional.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dmr_conventionalActionPerformed(evt);
            }
        });
        jPanel46.add(dmr_conventional);

        jSeparator36.setPreferredSize(new java.awt.Dimension(100, 0));
        jPanel46.add(jSeparator36);

        dmr_slot1.setSelected(true);
        dmr_slot1.setText("Enable Slot1");
        jPanel46.add(dmr_slot1);

        dmr_slot2.setSelected(true);
        dmr_slot2.setText("Enable Slot2");
        jPanel46.add(dmr_slot2);

        jSeparator37.setPreferredSize(new java.awt.Dimension(100, 0));
        jPanel46.add(jSeparator37);

        jPanel26.add(jPanel46);

        jPanel25.add(jPanel26, java.awt.BorderLayout.CENTER);

        dmr_backup.setText("Backup To File");
        dmr_backup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dmr_backupActionPerformed(evt);
            }
        });
        jPanel30.add(dmr_backup);

        jSeparator34.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel30.add(jSeparator34);

        dmr_restore.setText("Restore From File");
        dmr_restore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dmr_restoreActionPerformed(evt);
            }
        });
        jPanel30.add(dmr_restore);

        jSeparator35.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel30.add(jSeparator35);

        dmr_write_config.setText("Write Configuration");
        dmr_write_config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dmr_write_configActionPerformed(evt);
            }
        });
        jPanel30.add(dmr_write_config);

        jPanel25.add(jPanel30, java.awt.BorderLayout.SOUTH);

        jPanel47.setPreferredSize(new java.awt.Dimension(50, 100));
        jPanel25.add(jPanel47, java.awt.BorderLayout.WEST);

        jTabbedPane1.addTab("DMR", jPanel25);

        audiopanel.setLayout(new java.awt.BorderLayout());

        jPanel11.setEnabled(false);
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        enable_mp3.setSelected(true);
        enable_mp3.setText("Enable audio file generation");
        enable_mp3.setToolTipText("This option will generate mp3 files in the p25rx directory located in the user home directory.  ~/p25rx on Linux and Documents/p25rx on Windows.");
        enable_mp3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enable_mp3ActionPerformed(evt);
            }
        });
        jPanel11.add(enable_mp3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, -1, -1));

        enable_audio.setSelected(true);
        enable_audio.setText("Enable PC Audio Output (PC Speakers)");
        enable_audio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enable_audioActionPerformed(evt);
            }
        });
        jPanel11.add(enable_audio, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 310, -1, -1));

        mp3_separate_files.setText("Separate files by talk group");
        mp3_separate_files.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mp3_separate_filesActionPerformed(evt);
            }
        });
        jPanel11.add(mp3_separate_files, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, -1, -1));

        jScrollPane3.setAutoscrolls(true);

        audio_dev_list.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Default" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        audio_dev_list.setSelectedIndex(0);
        audio_dev_list.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                audio_dev_listValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(audio_dev_list);

        jPanel11.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 40, 510, 240));

        jLabel3.setText("PC Output Audio Device Selection");
        jPanel11.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 20, -1, -1));

        select_home.setText("Select");
        select_home.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_homeActionPerformed(evt);
            }
        });
        jPanel11.add(select_home, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, -1, -1));

        jLabel10.setText("Audio Output Dir:");
        jPanel11.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, -1));

        home_dir_label.setText("/home/p25rx");
        jPanel11.add(home_dir_label, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, -1));

        buttonGroup16.add(do_mp3);
        do_mp3.setSelected(true);
        do_mp3.setText("MP3");
        do_mp3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                do_mp3ActionPerformed(evt);
            }
        });
        jPanel11.add(do_mp3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 130, -1, -1));

        buttonGroup16.add(do_wav);
        do_wav.setText("WAV");
        do_wav.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                do_wavActionPerformed(evt);
            }
        });
        jPanel11.add(do_wav, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 130, -1, -1));

        buttonGroup18.add(audio_hiq);
        audio_hiq.setSelected(true);
        audio_hiq.setText("High");
        audio_hiq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                audio_hiqActionPerformed(evt);
            }
        });
        jPanel11.add(audio_hiq, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 160, -1, -1));

        buttonGroup18.add(audio_lowq);
        audio_lowq.setText("VBR (variable bit rate)");
        audio_lowq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                audio_lowqActionPerformed(evt);
            }
        });
        jPanel11.add(audio_lowq, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 160, -1, -1));

        jLabel28.setText("MP3 Quality");
        jPanel11.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 160, -1, 20));

        jLabel4.setText("End-Of-Call Silence");
        jPanel59.add(jLabel4);

        end_call_silence.setColumns(5);
        end_call_silence.setText("0");
        jPanel59.add(end_call_silence);

        jLabel32.setText("ms");
        jPanel59.add(jLabel32);

        jPanel11.add(jPanel59, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 270, 230, 40));

        separate_rid.setText("Separate files by RID");
        separate_rid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                separate_ridActionPerformed(evt);
            }
        });
        jPanel11.add(separate_rid, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 230, -1, -1));

        en_rdio.setText("Rdio-Scanner support (DirWatch)");
        en_rdio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_rdioActionPerformed(evt);
            }
        });
        jPanel11.add(en_rdio, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 350, -1, -1));

        jLabel64.setText("Cut/Paste This Mask");
        jPanel11.add(jLabel64, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 380, -1, -1));

        rdio_mask.setText("TG_#TG_#DATE_#TIME_#SYS_#HZ_");
        rdio_mask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdio_maskActionPerformed(evt);
            }
        });
        jPanel11.add(rdio_mask, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 380, 460, -1));

        en_broadcastify_calls.setText("Enable Broadcastify Calls Output");
        jPanel11.add(en_broadcastify_calls, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 300, -1, -1));

        audiopanel.add(jPanel11, java.awt.BorderLayout.CENTER);

        jPanel13.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        audiopanel.add(jPanel13, java.awt.BorderLayout.PAGE_START);

        jTabbedPane1.addTab("PC Audio", audiopanel);

        freqdb_panel.setLayout(new java.awt.BorderLayout());

        freq_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));

        freq_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [8000][11],
            new String [] {
                "LICENSE", "GRANTEE", "ENTITY(GOV,BUS)", "CC FREQ", "MODE", "RESULTS", "INFLASH", "MODULATION", "CITY", "STATE", "EMISSION"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,

            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        freq_table.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                freq_tableKeyTyped(evt);
            }
        });
        jScrollPane8.setViewportView(freq_table);

        freqdb_panel.add(jScrollPane8, java.awt.BorderLayout.CENTER);

        jPanel16.setLayout(new javax.swing.BoxLayout(jPanel16, javax.swing.BoxLayout.Y_AXIS));

        jPanel17.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        inc_bus.setSelected(true);
        inc_bus.setText("Include Businesses");
        inc_bus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inc_busActionPerformed(evt);
            }
        });
        jPanel17.add(inc_bus);

        inc_gov.setSelected(true);
        inc_gov.setText("Include Government");
        jPanel17.add(inc_gov);

        inc_trunked_only.setSelected(true);
        inc_trunked_only.setText("Trunked Only");
        jPanel17.add(inc_trunked_only);

        inc_p25.setSelected(true);
        inc_p25.setText("Include P25");
        inc_p25.setEnabled(false);
        inc_p25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inc_p25ActionPerformed(evt);
            }
        });
        jPanel17.add(inc_p25);

        inc_dmr.setText("Include DMR");
        inc_dmr.setEnabled(false);
        jPanel17.add(inc_dmr);

        jPanel16.add(jPanel17);

        jPanel18.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        inc_vhf.setSelected(true);
        inc_vhf.setText("Include 150-300 MHz");
        jPanel18.add(inc_vhf);

        inc_400mhz.setSelected(true);
        inc_400mhz.setText("Include 400 MHz");
        jPanel18.add(inc_400mhz);

        inc_700mhz.setSelected(true);
        inc_700mhz.setText("Include 700 MHz");
        jPanel18.add(inc_700mhz);

        inc_800mhz.setSelected(true);
        inc_800mhz.setText("Include 800 MHz");
        jPanel18.add(inc_800mhz);

        inc_900mhz.setSelected(true);
        inc_900mhz.setText("Include 900 MHz");
        jPanel18.add(inc_900mhz);

        inc_dup_freq.setText("Include Duplicate Frequencies");
        inc_dup_freq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inc_dup_freqActionPerformed(evt);
            }
        });
        jPanel18.add(inc_dup_freq);

        jPanel16.add(jPanel18);

        jPanel14.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel14.setText("Search Distance: ");
        jPanel14.add(jLabel14);

        search_radius.setColumns(3);
        search_radius.setText("30");
        jPanel14.add(search_radius);

        jLabel15.setText("miles   ");
        jPanel14.add(jLabel15);

        zipcode.setColumns(6);
        zipcode.setText("99352");
        jPanel14.add(zipcode);

        freq_search.setText("Zipcode Search");
        freq_search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freq_searchActionPerformed(evt);
            }
        });
        jPanel14.add(freq_search);

        jSeparator3.setPreferredSize(new java.awt.Dimension(30, 0));
        jPanel14.add(jSeparator3);

        jLabel16.setText("City  ");
        jPanel14.add(jLabel16);

        city.setColumns(16);
        city.setText("Richland");
        jPanel14.add(city);

        jLabel17.setText("  State  ");
        jPanel14.add(jLabel17);

        state.setColumns(3);
        state.setText("Wa");
        jPanel14.add(state);

        freq_search2.setText("City/State Search");
        freq_search2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freq_search2ActionPerformed(evt);
            }
        });
        jPanel14.add(freq_search2);

        jPanel16.add(jPanel14);

        freqdb_panel.add(jPanel16, java.awt.BorderLayout.PAGE_START);

        jPanel19.setLayout(new javax.swing.BoxLayout(jPanel19, javax.swing.BoxLayout.Y_AXIS));

        jPanel15.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        testfreqs.setText("Test Selected Frequencies");
        testfreqs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testfreqsActionPerformed(evt);
            }
        });
        jPanel15.add(testfreqs);

        append_cc.setText("Write Selected To Roaming Flash");
        append_cc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                append_ccActionPerformed(evt);
            }
        });
        jPanel15.add(append_cc);

        use_freq_primary.setText("Use Selected On Power-Up");
        use_freq_primary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                use_freq_primaryActionPerformed(evt);
            }
        });
        jPanel15.add(use_freq_primary);

        gen_sys_info.setText("Gen Sys Info");
        gen_sys_info.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gen_sys_infoActionPerformed(evt);
            }
        });
        jPanel15.add(gen_sys_info);

        read_roaming.setText("Read Roaming Flash");
        read_roaming.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                read_roamingActionPerformed(evt);
            }
        });
        jPanel15.add(read_roaming);

        monitor_sel.setText("Monitor Selected");
        monitor_sel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monitor_selActionPerformed(evt);
            }
        });
        jPanel15.add(monitor_sel);

        jPanel19.add(jPanel15);

        jPanel20.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        restore_roam.setText("Restore From Backup");
        restore_roam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restore_roamActionPerformed(evt);
            }
        });
        jPanel20.add(restore_roam);

        backup_roam.setText("Make Backup");
        backup_roam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backup_roamActionPerformed(evt);
            }
        });
        jPanel20.add(backup_roam);

        erase_roaming.setText("Erase Roaming Flash");
        erase_roaming.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                erase_roamingActionPerformed(evt);
            }
        });
        jPanel20.add(erase_roaming);

        delete_roaming.setText("Delete Selected Rows");
        delete_roaming.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delete_roamingActionPerformed(evt);
            }
        });
        jPanel20.add(delete_roaming);

        jPanel62.setBorder(javax.swing.BorderFactory.createTitledBorder("P25 Sites ( active only when roaming is disabled )"));

        add_primary.setText("Add Primary");
        jPanel62.add(add_primary);

        add_secondaries.setText("Add Secondaries");
        jPanel62.add(add_secondaries);

        add_neighbors.setText("Add Neighbors");
        jPanel62.add(add_neighbors);

        jPanel20.add(jPanel62);

        jPanel19.add(jPanel20);

        freqdb_panel.add(jPanel19, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.addTab("Search DB", freqdb_panel);

        talkgroup_panel.setLayout(new java.awt.BorderLayout());

        jTable1.setDoubleBuffered(true);
        jTable1.setEditingColumn(1);
        jTable1.setEditingRow(1);
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object[6554][8],
            new String [] {
                "Enabled", "SYS_ID(HEX)", "Priority", "TGRP", "AlphaTag", "Description", "WACN(HEX)", "Zone"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTable1KeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);

        talkgroup_panel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel22.setLayout(new javax.swing.BoxLayout(jPanel22, javax.swing.BoxLayout.Y_AXIS));

        jPanel3.setBackground(new java.awt.Color(0, 0, 0));

        enable_table_rows.setText("Enable Selected");
        enable_table_rows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enable_table_rowsActionPerformed(evt);
            }
        });
        jPanel3.add(enable_table_rows);

        disable_table_rows.setText("Disable Selected");
        disable_table_rows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disable_table_rowsActionPerformed(evt);
            }
        });
        jPanel3.add(disable_table_rows);

        read_tg.setText("Read Talk Groups");
        read_tg.setToolTipText("reads talk groups from P25RX Device");
        read_tg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                read_tgActionPerformed(evt);
            }
        });
        jPanel3.add(read_tg);

        send_tg.setText("Write Talk Groups");
        send_tg.setToolTipText("writes talkgroups to P25RX device");
        send_tg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                send_tgActionPerformed(evt);
            }
        });
        jPanel3.add(send_tg);

        backup_tg.setText("Export TGP/CSV");
        backup_tg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backup_tgActionPerformed(evt);
            }
        });
        jPanel3.add(backup_tg);

        tg_edit_del.setText("DEL Selected");
        tg_edit_del.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tg_edit_delActionPerformed(evt);
            }
        });
        jPanel3.add(tg_edit_del);

        set_zones.setText("Set Selected Zones");
        set_zones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set_zonesActionPerformed(evt);
            }
        });
        jPanel3.add(set_zones);

        jPanel22.add(jPanel3);

        jPanel23.setBackground(new java.awt.Color(0, 0, 0));
        jPanel23.setForeground(new java.awt.Color(255, 255, 255));

        restore_tg.setText("Restore From TGP");
        restore_tg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restore_tgActionPerformed(evt);
            }
        });
        jPanel23.add(restore_tg);

        import_csv.setText("Import CSV");
        import_csv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                import_csvActionPerformed(evt);
            }
        });
        jPanel23.add(import_csv);

        auto_flash_tg.setForeground(new java.awt.Color(255, 255, 255));
        auto_flash_tg.setSelected(true);
        auto_flash_tg.setText("AUTO FLASH");
        auto_flash_tg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auto_flash_tgActionPerformed(evt);
            }
        });
        jPanel23.add(auto_flash_tg);

        disable_encrypted.setForeground(new java.awt.Color(255, 255, 255));
        disable_encrypted.setSelected(true);
        disable_encrypted.setText("AUTO DISABLE ENCRYPTED");
        disable_encrypted.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disable_encryptedActionPerformed(evt);
            }
        });
        jPanel23.add(disable_encrypted);

        auto_pop_table.setForeground(new java.awt.Color(255, 255, 255));
        auto_pop_table.setSelected(true);
        auto_pop_table.setText("AUTO POPULATE TABLE");
        auto_pop_table.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auto_pop_tableActionPerformed(evt);
            }
        });
        jPanel23.add(auto_pop_table);

        jPanel22.add(jPanel23);

        jPanel69.setBackground(new java.awt.Color(0, 0, 0));
        jPanel69.setForeground(new java.awt.Color(255, 255, 255));
        jPanel22.add(jPanel69);

        talkgroup_panel.add(jPanel22, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.addTab("TG Editor", talkgroup_panel);

        consolePanel.setPreferredSize(new java.awt.Dimension(963, 500));
        consolePanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                consolePanelFocusGained(evt);
            }
        });
        consolePanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new java.awt.Color(0, 0, 0));
        jTextArea1.setColumns(120);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        jTextArea1.setForeground(new java.awt.Color(255, 255, 255));
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(40);
        jTextArea1.setText("\nBlueTail Technologies P25RX Console\n\n$ ");
        jTextArea1.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextArea1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextArea1FocusGained(evt);
            }
        });
        jTextArea1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextArea1KeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(jTextArea1);

        consolePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        en_evt_output.setSelected(true);
        en_evt_output.setText("Display And Log Events");
        en_evt_output.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_evt_outputActionPerformed(evt);
            }
        });
        jPanel51.add(en_evt_output);

        jPanel6.add(jPanel51);

        consolePanel.add(jPanel6, java.awt.BorderLayout.PAGE_END);

        jTabbedPane1.addTab("Console", consolePanel);

        logpanel.setBackground(new java.awt.Color(0, 0, 0));
        logpanel.setPreferredSize(new java.awt.Dimension(963, 500));
        logpanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                logpanelFocusGained(evt);
            }
        });
        logpanel.setLayout(new java.awt.BorderLayout());

        log_ta.setEditable(false);
        log_ta.setBackground(new java.awt.Color(0, 0, 0));
        log_ta.setColumns(120);
        log_ta.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        log_ta.setForeground(new java.awt.Color(255, 255, 255));
        log_ta.setRows(40);
        log_ta.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        log_ta.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                log_taFocusGained(evt);
            }
        });
        log_ta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                log_taKeyTyped(evt);
            }
        });
        tg_scroll_pane.setViewportView(log_ta);

        logpanel.add(tg_scroll_pane, java.awt.BorderLayout.CENTER);

        tgfontpanel.setBackground(new java.awt.Color(0, 0, 0));
        tgfontpanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        tglog_font.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        tglog_font.setText("Font");
        tglog_font.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglog_fontActionPerformed(evt);
            }
        });
        tgfontpanel.add(tglog_font);

        tglog_color.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        tglog_color.setText("Color");
        tglog_color.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglog_colorActionPerformed(evt);
            }
        });
        tgfontpanel.add(tglog_color);

        tglog_edit.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        tglog_edit.setText("Edit");
        tglog_edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglog_editActionPerformed(evt);
            }
        });
        tgfontpanel.add(tglog_edit);

        logpanel.add(tgfontpanel, java.awt.BorderLayout.PAGE_END);

        jTabbedPane1.addTab("TG Log", logpanel);

        buttong_config.setLayout(new java.awt.BorderLayout());

        jPanel21.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel18.setText("Single Click");
        jPanel21.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, -1, -1));

        jLabel19.setText("Double Click");
        jPanel21.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 140, -1, 20));

        jLabel20.setText("Triple Click");
        jPanel21.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 220, -1, -1));

        button_write_config.setText("Write Config");
        button_write_config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_write_configActionPerformed(evt);
            }
        });
        jPanel21.add(button_write_config, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 390, -1, -1));

        jPanel27.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonGroup11.add(single_click_opt1);
        single_click_opt1.setSelected(true);
        single_click_opt1.setText("Follow TG");
        jPanel27.add(single_click_opt1);

        buttonGroup11.add(single_click_opt2);
        single_click_opt2.setText("Bluetooth Pairing");
        jPanel27.add(single_click_opt2);

        buttonGroup11.add(single_click_opt3);
        single_click_opt3.setText("Enable/Disable Status Leds");
        jPanel27.add(single_click_opt3);

        buttonGroup11.add(single_click_opt4);
        single_click_opt4.setText("Skip TG");
        jPanel27.add(single_click_opt4);

        buttonGroup11.add(single_click_opt5);
        single_click_opt5.setText("Enable/Disable Unknown TG");
        jPanel27.add(single_click_opt5);

        buttonGroup11.add(single_click_opt6);
        single_click_opt6.setText("Enable/Disable Roaming");
        jPanel27.add(single_click_opt6);

        jPanel21.add(jPanel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 80, 1010, 40));

        jPanel28.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonGroup10.add(double_click_opt1);
        double_click_opt1.setText("Follow TG");
        jPanel28.add(double_click_opt1);

        buttonGroup10.add(double_click_opt2);
        double_click_opt2.setSelected(true);
        double_click_opt2.setText("Bluetooth Pairing");
        jPanel28.add(double_click_opt2);

        buttonGroup10.add(double_click_opt3);
        double_click_opt3.setText("Enable/Disable Status Leds");
        jPanel28.add(double_click_opt3);

        buttonGroup10.add(double_click_opt4);
        double_click_opt4.setText("Skip TG");
        jPanel28.add(double_click_opt4);

        buttonGroup10.add(double_click_opt5);
        double_click_opt5.setText("Enable/Disable Unknown TG");
        jPanel28.add(double_click_opt5);

        buttonGroup10.add(double_click_opt6);
        double_click_opt6.setText("Enable/Disable Roaming");
        jPanel28.add(double_click_opt6);

        jPanel21.add(jPanel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 153, -1, 70));

        jPanel45.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonGroup9.add(triple_click_opt1);
        triple_click_opt1.setText("Follow TG");
        jPanel45.add(triple_click_opt1);

        buttonGroup9.add(triple_click_opt2);
        triple_click_opt2.setText("Bluetooth Pairing");
        jPanel45.add(triple_click_opt2);

        buttonGroup9.add(triple_click_opt3);
        triple_click_opt3.setSelected(true);
        triple_click_opt3.setText("Enable/Disable Status Leds");
        jPanel45.add(triple_click_opt3);

        buttonGroup9.add(triple_click_opt4);
        triple_click_opt4.setText("Skip TG");
        triple_click_opt4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                triple_click_opt4ActionPerformed(evt);
            }
        });
        jPanel45.add(triple_click_opt4);

        buttonGroup9.add(triple_click_opt5);
        triple_click_opt5.setText("Enable/Disable Unknown TG");
        jPanel45.add(triple_click_opt5);

        buttonGroup9.add(triple_click_opt6);
        triple_click_opt6.setText("Enable/Disable Roaming");
        jPanel45.add(triple_click_opt6);

        jPanel21.add(jPanel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 230, 1040, 40));

        jLabel21.setText("Skip TG Timeout ");
        jPanel48.add(jLabel21);

        skip_tg_to.setColumns(5);
        skip_tg_to.setText("60");
        jPanel48.add(skip_tg_to);

        jLabel22.setText("Minutes");
        jPanel48.add(jLabel22);

        jSeparator40.setMinimumSize(new java.awt.Dimension(75, 10));
        jSeparator40.setPreferredSize(new java.awt.Dimension(75, 0));
        jPanel48.add(jSeparator40);

        roaming_ret_to_cc.setSelected(true);
        roaming_ret_to_cc.setText("Roaming Return To Primary On Disable");
        jPanel48.add(roaming_ret_to_cc);

        jPanel21.add(jPanel48, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 350, 870, 50));

        jLabel33.setText("Quad Click");
        jPanel21.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 290, -1, -1));

        jPanel49.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonGroup13.add(quad_click_opt1);
        quad_click_opt1.setText("Follow TG");
        jPanel49.add(quad_click_opt1);

        buttonGroup13.add(quad_click_opt2);
        quad_click_opt2.setText("Bluetooth Pairing");
        jPanel49.add(quad_click_opt2);

        buttonGroup13.add(quad_click_opt3);
        quad_click_opt3.setText("Enable/Disable Status Leds");
        jPanel49.add(quad_click_opt3);

        buttonGroup13.add(quad_click_opt4);
        quad_click_opt4.setText("Skip TG");
        quad_click_opt4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quad_click_opt4ActionPerformed(evt);
            }
        });
        jPanel49.add(quad_click_opt4);

        buttonGroup13.add(quad_click_opt5);
        quad_click_opt5.setText("Enable/Disable Unknown TG");
        jPanel49.add(quad_click_opt5);

        buttonGroup13.add(quad_click_opt6);
        quad_click_opt6.setSelected(true);
        quad_click_opt6.setText("Enable/Disable Roaming");
        jPanel49.add(quad_click_opt6);

        jPanel21.add(jPanel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 300, 1030, -1));

        buttong_config.add(jPanel21, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Button CFG", buttong_config);

        alias_panel.setLayout(new java.awt.BorderLayout());

        alias_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object[16000][2],
            new String [] {
                "Radio ID", "Alias_And_Comments"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane5.setViewportView(alias_table);

        alias_panel.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        import_alias.setText("Import CSV");
        import_alias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                import_aliasActionPerformed(evt);
            }
        });
        jPanel5.add(import_alias);

        alias_panel.add(jPanel5, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.addTab("Alias", alias_panel);

        advancedpanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        adv_write_config.setText("Write Config");
        adv_write_config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adv_write_configActionPerformed(evt);
            }
        });
        advancedpanel.add(adv_write_config, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 340, -1, -1));

        en_encout.setText("Enable Encrypted Audio Output");
        advancedpanel.add(en_encout, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 30, -1, -1));

        en_p2_tones.setSelected(true);
        en_p2_tones.setText("Enable Phase II Tone Output");
        advancedpanel.add(en_p2_tones, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 60, -1, -1));

        p25_tone_vol.setColumns(5);
        p25_tone_vol.setText("1.0");
        p25_tone_vol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                p25_tone_volActionPerformed(evt);
            }
        });
        advancedpanel.add(p25_tone_vol, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 100, -1, -1));

        jLabel12.setText("P25 Phase II Tone Volume");
        advancedpanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 100, -1, -1));

        en_zero_rid.setSelected(true);
        en_zero_rid.setText("Allow Logging Of RID = 0 (some transmissions report SRC ID of 0)");
        en_zero_rid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_zero_ridActionPerformed(evt);
            }
        });
        advancedpanel.add(en_zero_rid, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 130, -1, -1));

        enc_mode.setText("Return To Control Control Channel And Skip TG for 30 Sec If Encrypted");
        enc_mode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enc_modeActionPerformed(evt);
            }
        });
        advancedpanel.add(enc_mode, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 170, -1, -1));

        allow_tg_pri_int.setSelected(true);
        allow_tg_pri_int.setText("Allow Talk Group Priority Interrupts");
        allow_tg_pri_int.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allow_tg_pri_intActionPerformed(evt);
            }
        });
        advancedpanel.add(allow_tg_pri_int, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 240, -1, -1));

        process_rid_alias.setSelected(true);
        process_rid_alias.setText("Process RID / Alias (no need to write config)");
        process_rid_alias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                process_rid_aliasActionPerformed(evt);
            }
        });
        advancedpanel.add(process_rid_alias, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 330, -1, -1));

        en_tg_int_tone.setSelected(true);
        en_tg_int_tone.setText("Enable 440 Hz tone on Talk Group priority interrupt event");
        advancedpanel.add(en_tg_int_tone, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 270, -1, -1));

        jPanel8.setLayout(new javax.swing.BoxLayout(jPanel8, javax.swing.BoxLayout.Y_AXIS));

        jPanel75.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel30.setText("AGC Target");
        jPanel75.add(jLabel30);

        vga_target.setColumns(3);
        vga_target.setText("-10");
        vga_target.setToolTipText("Higher values = More Linear, Lower values=More Sensitive");
        vga_target.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vga_targetActionPerformed(evt);
            }
        });
        jPanel75.add(vga_target);

        jLabel53.setText("dB (default -10 dB)");
        jPanel75.add(jLabel53);

        jSeparator44.setPreferredSize(new java.awt.Dimension(10, 0));
        jPanel75.add(jSeparator44);

        jLabel62.setText("  AGC Step Time");
        jPanel75.add(jLabel62);

        vga_step.setColumns(3);
        vga_step.setText("25");
        vga_step.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vga_stepActionPerformed(evt);
            }
        });
        jPanel75.add(vga_step);

        jLabel63.setText("ms");
        jPanel75.add(jLabel63);

        jSeparator45.setPreferredSize(new java.awt.Dimension(10, 0));
        jPanel75.add(jSeparator45);

        jSeparator47.setPreferredSize(new java.awt.Dimension(10, 0));
        jPanel75.add(jSeparator47);

        save_iq.setText("Save I/Q File");
        save_iq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_iqActionPerformed(evt);
            }
        });
        jPanel75.add(save_iq);

        jPanel8.add(jPanel75);

        jPanel76.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel60.setText("Suggest 0 to -20 dB AGC Target");
        jPanel76.add(jLabel60);

        jSeparator48.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel76.add(jSeparator48);

        jPanel8.add(jPanel76);

        advancedpanel.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 60, 790, 60));

        jPanel53.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel31.setText("LNA Gain");
        jPanel53.add(jLabel31);

        rfgain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "2 dB", "4 dB", "6 dB", "8 dB", "10 dB", "12 dB", "14 dB", "16 dB", "18 dB", "20 dB", "22 dB", "24 dB", "26 dB", "28 dB", "30 dB", "32 dB" }));
        jPanel53.add(rfgain);

        jSeparator42.setPreferredSize(new java.awt.Dimension(20, 0));
        jPanel53.add(jSeparator42);

        jLabel35.setText("Mixer Gain");
        jPanel53.add(jLabel35);

        mixgain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "1.125 dB", "2.25 dB", "3.375 dB", "4.5 dB", "5.625 dB", "6.75 dB", "7.875 dB", "9 dB", "10.125 dB", "11.25 dB", "12.375 dB", "13.5 dB", "14.625 dB", "15.75 dB", "16.875", "18 dB" }));
        jPanel53.add(mixgain);

        jSeparator43.setPreferredSize(new java.awt.Dimension(20, 0));
        jPanel53.add(jSeparator43);

        jLabel29.setText("BB VGA Gain");
        jPanel53.add(jLabel29);

        vga_gain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "0 dB", "3 dB", "6 dB", "9 dB", "12 dB", "15 dB", "18 dB", "21 dB", "24 dB", "27 dB", "30 dB", "33 dB", "36 dB", "39 dB", "42 dB", "45 dB" }));
        jPanel53.add(vga_gain);

        jSeparator46.setPreferredSize(new java.awt.Dimension(20, 0));
        jPanel53.add(jSeparator46);

        jLabel56.setText("Hysteresis");
        jPanel53.add(jLabel56);

        rf_hyst.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "3 dB", "6 dB", "10 dB", "16 dB", "22 dB" }));
        jPanel53.add(rf_hyst);

        advancedpanel.add(jPanel53, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 20, 770, 40));

        reset_defaults.setText("Reset To Defaults");
        reset_defaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reset_defaultsActionPerformed(evt);
            }
        });
        advancedpanel.add(reset_defaults, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 300, -1, -1));

        jPanel63.setLayout(new java.awt.GridLayout(2, 1));

        jPanel64.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel57.setText("P1 Sync Thresh");
        jPanel64.add(jLabel57);

        p1_sync_thresh.setColumns(3);
        p1_sync_thresh.setText("2");
        jPanel64.add(p1_sync_thresh);

        jLabel65.setText("default=2, (max err allowed)");
        jPanel64.add(jLabel65);

        jPanel63.add(jPanel64);

        jPanel65.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel50.setText("P2 Sync Thresh");
        jPanel65.add(jLabel50);

        p2_sync_thresh.setColumns(3);
        p2_sync_thresh.setText("0");
        jPanel65.add(p2_sync_thresh);

        jLabel66.setText("default=0 (max err allowed)");
        jPanel65.add(jLabel66);

        jPanel63.add(jPanel65);

        advancedpanel.add(jPanel63, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 150, 220, 110));

        jPanel54.setBorder(javax.swing.BorderFactory.createTitledBorder("USB interface speed"));

        buttonGroup19.add(usb_slow);
        usb_slow.setSelected(true);
        usb_slow.setText("Slow (default)");
        jPanel54.add(usb_slow);

        buttonGroup19.add(usb_med);
        usb_med.setText("Medium");
        jPanel54.add(usb_med);

        buttonGroup19.add(usb_fast);
        usb_fast.setText("Fast");
        jPanel54.add(usb_fast);

        advancedpanel.add(jPanel54, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 305, 470, 70));

        jPanel56.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel52.setText("ENC Timeout ms");
        jPanel56.add(jLabel52);

        enc_timeout.setColumns(9);
        enc_timeout.setText("30000");
        jPanel56.add(enc_timeout);

        jLabel54.setText("ENC Verify Count");
        jPanel56.add(jLabel54);

        enc_count.setColumns(3);
        enc_count.setText("2");
        jPanel56.add(enc_count);

        jLabel55.setText("(0 to 10)");
        jPanel56.add(jLabel55);

        advancedpanel.add(jPanel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 200, 480, 30));

        vrep.setText("Enable voice frame repeats");
        advancedpanel.add(vrep, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 263, -1, 30));

        jTabbedPane1.addTab("Advanced", advancedpanel);

        signalinsightpanel.setLayout(new java.awt.BorderLayout());

        const_panel.setBackground(new java.awt.Color(0, 0, 0));
        const_panel.setMaximumSize(new java.awt.Dimension(1512, 1512));
        const_panel.setMinimumSize(new java.awt.Dimension(512, 512));
        const_panel.setPreferredSize(new java.awt.Dimension(512, 512));
        const_panel.setLayout(new java.awt.BorderLayout());

        jPanel78.setBackground(new java.awt.Color(0, 0, 0));
        const_panel.add(jPanel78, java.awt.BorderLayout.EAST);

        signalinsightpanel.add(const_panel, java.awt.BorderLayout.CENTER);

        jPanel55.setLayout(new javax.swing.BoxLayout(jPanel55, javax.swing.BoxLayout.Y_AXIS));

        jPanel24.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        autoscale_const.setSelected(true);
        autoscale_const.setText("Auto Scale");
        autoscale_const.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoscale_constActionPerformed(evt);
            }
        });
        jPanel24.add(autoscale_const);

        nsymbols.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "256 Symbols", "512 Symbols", "1024 Symbols", "2048 Symbols", "4096 Symbols", "8192 Symbols" }));
        nsymbols.setEnabled(false);
        nsymbols.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nsymbolsActionPerformed(evt);
            }
        });
        jPanel24.add(nsymbols);
        jPanel24.add(jSeparator41);

        jPanel68.setBorder(javax.swing.BorderFactory.createTitledBorder("CPU Usage"));

        buttonGroup20.add(si_cpu_high);
        si_cpu_high.setText("High");
        si_cpu_high.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                si_cpu_highActionPerformed(evt);
            }
        });
        jPanel68.add(si_cpu_high);

        buttonGroup20.add(si_cpu_normal);
        si_cpu_normal.setSelected(true);
        si_cpu_normal.setText("Normal");
        si_cpu_normal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                si_cpu_normalActionPerformed(evt);
            }
        });
        jPanel68.add(si_cpu_normal);

        buttonGroup20.add(si_cpu_low);
        si_cpu_low.setText("Low");
        si_cpu_low.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                si_cpu_lowActionPerformed(evt);
            }
        });
        jPanel68.add(si_cpu_low);

        buttonGroup20.add(si_cpu_battery_saving);
        si_cpu_battery_saving.setText("Battery Saving");
        si_cpu_battery_saving.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                si_cpu_battery_savingActionPerformed(evt);
            }
        });
        jPanel68.add(si_cpu_battery_saving);

        buttonGroup20.add(si_cpu_off);
        si_cpu_off.setText("Off");
        si_cpu_off.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                si_cpu_offActionPerformed(evt);
            }
        });
        jPanel68.add(si_cpu_off);

        jPanel24.add(jPanel68);

        vga_cal.setText("Find Min VGA gain");
        vga_cal.setEnabled(false);
        vga_cal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vga_calActionPerformed(evt);
            }
        });
        jPanel24.add(vga_cal);

        freemem.setText("Free Memory");
        freemem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freememActionPerformed(evt);
            }
        });
        jPanel24.add(freemem);

        jPanel55.add(jPanel24);

        jPanel58.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel9.setText("IF FREQ TEST");
        jLabel9.setEnabled(false);
        jPanel58.add(jLabel9);

        if_slide.setMaximum(459000);
        if_slide.setMinimum(454000);
        if_slide.setEnabled(false);
        if_slide.setPreferredSize(new java.awt.Dimension(400, 16));
        if_slide.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if_slideStateChanged(evt);
            }
        });
        jPanel58.add(if_slide);

        jLabel27.setText("CMD");
        jPanel58.add(jLabel27);

        sig_cmd.setColumns(40);
        sig_cmd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sig_cmdKeyReleased(evt);
            }
        });
        jPanel58.add(sig_cmd);

        jPanel55.add(jPanel58);

        signalinsightpanel.add(jPanel55, java.awt.BorderLayout.NORTH);

        jTabbedPane1.addTab("Signal Insights", signalinsightpanel);

        displayviewmain_border.setBackground(new java.awt.Color(0, 0, 0));
        displayviewmain_border.setLayout(new java.awt.BorderLayout());

        display_frame.setBackground(new java.awt.Color(0, 0, 0));
        display_frame.setLayout(new java.awt.GridLayout(5, 1));
        displayviewmain_border.add(display_frame, java.awt.BorderLayout.CENTER);

        jPanel61.setBackground(new java.awt.Color(0, 0, 0));
        jPanel61.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        hold.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        hold.setText("H");
        hold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                holdActionPerformed(evt);
            }
        });
        jPanel61.add(hold);

        skip.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        skip.setText("S");
        skip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skipActionPerformed(evt);
            }
        });
        jPanel61.add(skip);

        edit_alias.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        edit_alias.setText("Edit Alias");
        edit_alias.setEnabled(false);
        edit_alias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edit_aliasActionPerformed(evt);
            }
        });
        jPanel61.add(edit_alias);

        edit_display_view.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        edit_display_view.setText("EDIT Display");
        edit_display_view.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edit_display_viewActionPerformed(evt);
            }
        });
        jPanel61.add(edit_display_view);

        dvpopout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/rightarrow.png"))); // NOI18N
        dvpopout.setText(" ");
        dvpopout.setBorderPainted(false);
        dvpopout.setContentAreaFilled(false);
        dvpopout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dvpopoutActionPerformed(evt);
            }
        });
        jPanel61.add(dvpopout);

        displayviewmain_border.add(jPanel61, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.addTab("Display View", displayviewmain_border);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setLayout(new java.awt.BorderLayout());

        logo_panel.setBackground(new java.awt.Color(255, 255, 255));
        logo_panel.setMinimumSize(new java.awt.Dimension(877, 10));
        logo_panel.setName(""); // NOI18N
        logo_panel.setPreferredSize(new java.awt.Dimension(1150, 80));
        logo_panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jSeparator1.setMaximumSize(new java.awt.Dimension(32767, 0));
        jSeparator1.setMinimumSize(new java.awt.Dimension(40, 0));
        jSeparator1.setPreferredSize(new java.awt.Dimension(40, 0));
        logo_panel.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 50, -1, -1));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/btlogo_small.gif"))); // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);
        jButton1.setFocusable(false);
        jButton1.setRequestFocusEnabled(false);
        jButton1.setRolloverEnabled(false);
        jButton1.setVerifyInputWhenFocusTarget(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        logo_panel.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 5, -1, -1));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        jLabel1.setText("P25RX-II");
        logo_panel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 10, -1, -1));

        wacn.setText("WACN:");
        logo_panel.add(wacn, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 40, -1, -1));

        sysid.setText("SYS_ID:");
        logo_panel.add(sysid, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 40, -1, -1));

        nac.setText("NAC:");
        logo_panel.add(nac, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 40, -1, -1));

        freq.setText("Freq:");
        logo_panel.add(freq, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 40, -1, -1));

        siteid.setText("SITE ID:");
        logo_panel.add(siteid, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 60, -1, -1));

        rfid.setText("RFSS ID:");
        logo_panel.add(rfid, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 60, -1, -1));

        macid.setText("MAC ID:");
        logo_panel.add(macid, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 60, -1, -1));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        minimize.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        minimize.setText("MON");
        minimize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimizeActionPerformed(evt);
            }
        });
        jPanel4.add(minimize);

        record_to_mp3.setText("REC");
        record_to_mp3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                record_to_mp3ActionPerformed(evt);
            }
        });
        jPanel4.add(record_to_mp3);

        release_date.setText("V: ");
        jPanel4.add(release_date);

        logo_panel.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 0, 360, -1));

        ser_dev.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        ser_dev.setText("PORT:");
        logo_panel.add(ser_dev, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, -1, -1));

        jPanel10.add(logo_panel, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel10, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void discoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discoverActionPerformed
      do_connect();

      //if(serial_port!=null) serial_port.closePort();
      //is_connected=0;
      //discover.setEnabled(true);
    }//GEN-LAST:event_discoverActionPerformed

    public void do_connect() {
      do_connect=1;
      do_read_config=1;
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextArea1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea1KeyTyped
    // TODO add your handling code here:
      char c = evt.getKeyChar();

      //System.out.println("got key");

      //if(skip_bytes==0 && do_update_firmware==0) {
      if(do_update_firmware==0) {
        if(c=='\n') {
          String str = String.copyValueOf(keydata,0,keyindex);
          str = str.trim()+"\r\n";
            //handle str 
            byte b[] = new byte[4096];
            for(int i=0;i<keyindex+2;i++) {
              //b[i] = (byte) keydata[i];
              b = str.getBytes();
            }
            if(serial_port!=null) serial_port.writeBytes(b,keyindex+2);
          keyindex=0;
          command_input=0;
        } else if(c=='\b') {
          if(keyindex>0) {
            keyindex--;
            String str = jTextArea1.getText();
            if(str.length()>0) jTextArea1.setText( str.substring(0, str.length()-1) );
          }
        } else {
          if(command_input==0) addTextConsole("\r\n$ ");
          command_input=1;
          command_input_timeout=5000;
          keydata[keyindex++] = c;
          jTextArea1.append( new Character(c).toString() );
        }
      }
    }//GEN-LAST:event_jTextArea1KeyTyped

    private void jTextArea1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea1FocusGained
      if(jTabbedPane1.getSelectedIndex()==5) jTextArea1.requestFocus();
    }//GEN-LAST:event_jTextArea1FocusGained

    private void consolePanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_consolePanelFocusGained
      if(jTabbedPane1.getSelectedIndex()==5) jTextArea1.requestFocus();
    }//GEN-LAST:event_consolePanelFocusGained

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
      if(jTabbedPane1.getSelectedIndex()==5) jTextArea1.requestFocus();
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void send_tgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_send_tgActionPerformed
      do_update_talkgroups=1;
    }//GEN-LAST:event_send_tgActionPerformed

    private void read_tgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_read_tgActionPerformed
      do_read_talkgroups=1;
    }//GEN-LAST:event_read_tgActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
      resizeColumns();
      resizeColumns2();
      resizeColumns3();
      //if(minimize.isSelected()) {
       // setSize(1054,192);
      //}
      save_position();
    }//GEN-LAST:event_formComponentResized

    private void disconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectActionPerformed
      do_disconnect=1;
      firmware_checked=0;
      do_update_firmware=0;
    }//GEN-LAST:event_disconnectActionPerformed

    private void check_firmwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_check_firmwareActionPerformed
      do_update_firmware=1;
      if(is_connected==0) do_connect=1;
    }//GEN-LAST:event_check_firmwareActionPerformed

    private void minimizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimizeActionPerformed
      if(minimize.isSelected()) {
        if(isWindows ) {
          setSize(1200,200+18+37);
        }
        else  {
          setSize(1200,185+18+37);  //linux and Mac
        }
      }
      else {
        setSize(1200,800+10);
        //parentSize = Toolkit.getDefaultToolkit().getScreenSize();
        //setSize(new Dimension((int) (parentSize.width * 0.75), (int) (parentSize.height * 0.8)));
      }
    }//GEN-LAST:event_minimizeActionPerformed

    private void restore_tgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restore_tgActionPerformed
      if(is_connected==0) do_connect();
      do_restore_tg=1;
    }//GEN-LAST:event_restore_tgActionPerformed

    private void record_to_mp3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_record_to_mp3ActionPerformed
      do_toggle_record=1;
    }//GEN-LAST:event_record_to_mp3ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      if(serial_port!=null) {
          SLEEP(100);
        String cmd= new String("en_voice_send 0\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
          SLEEP(100);
        cmd= new String("logging 0\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      }
          try {
            if( usb_slow.isSelected() ) prefs.put("usb_speed", "slow");
            if( usb_med.isSelected() ) prefs.put("usb_speed", "med");
            if( usb_fast.isSelected() ) prefs.put("usb_speed", "fast");
          } catch(Exception e) {
            e.printStackTrace();
          }

          try {
            prefs.put("sys_zipcode", zipcode.getText());
          } catch(Exception e) {
            e.printStackTrace();
          }
          try {
            prefs.put("sys_city", city.getText());
          } catch(Exception e) {
            e.printStackTrace();
          }
          try {
            prefs.put("sys_state", state.getText());
          } catch(Exception e) {
            e.printStackTrace();
          }

          try {
            prefs.put("end_call_silence", end_call_silence.getText() ); 
          } catch(Exception e) {
            e.printStackTrace();
          }

          try {
            parent.prefs.put("status_format_cc", status_format_cc.getText() ); 
          } catch(Exception e) {
            e.printStackTrace();
          }

          try {
            parent.prefs.put("status_format_voice", status_format_voice.getText() ); 
          } catch(Exception e) {
            e.printStackTrace();
          }

      save_position();
      if(alias!=null) {
        alias.save_alias();
        int cnt=0;
        while( alias.do_save_alias==1 && cnt<100) {
          SLEEP(10);
          cnt++;
        }
      }

    }//GEN-LAST:event_formWindowClosing

    public void clear_sys_info() {
      wacn.setText("");
      sysid.setText("");
      nac.setText("");
      nac.setText("");
      rfid.setText("");
      siteid.setText("");

    }

    private void en_bluetooth_cbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_bluetooth_cbActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_en_bluetooth_cbActionPerformed

    private void read_configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_read_configActionPerformed
      do_read_config=1;
    }//GEN-LAST:event_read_configActionPerformed

    private void write_configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_write_configActionPerformed
      do_read_config=1;
      do_write_config=1;
      cpanel.reset_ref_est();


      //current_sys_id = 0;
      //current_wacn_id = 0; 

      clear_sys_info();

      if(prefs!=null) prefs.put( "system_alias", system_alias.getText() );

      try {
        if(prefs!=null) parent.prefs.put("status_format_cc", status_format_cc.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        if(prefs!=null) parent.prefs.put("status_format_voice", status_format_voice.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        save_position();
      } catch(Exception e) {
      }

    }//GEN-LAST:event_write_configActionPerformed

    private void backup_tgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backup_tgActionPerformed
      did_tg_backup=0;
      do_read_talkgroups=1;
      do_talkgroup_backup=1;
    }//GEN-LAST:event_backup_tgActionPerformed

    private void lineout_vol_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lineout_vol_sliderStateChanged
      volume_label.setText( String.format( "%3.2f", (float) lineout_vol_slider.getValue() / 100.0f ) );
    }//GEN-LAST:event_lineout_vol_sliderStateChanged

    private void disable_table_rowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disable_table_rowsActionPerformed
      int[] rows = jTable1.getSelectedRows();
      if(rows.length>0) {
        for(int i=0;i<rows.length;i++) {
          jTable1.getModel().setValueAt(false,jTable1.convertRowIndexToModel(rows[i]),0);
          System.out.println("row "+i);
        } 
      }
    }//GEN-LAST:event_disable_table_rowsActionPerformed

    private void enable_table_rowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enable_table_rowsActionPerformed
      int[] rows = jTable1.getSelectedRows();
      if(rows.length>0) {
        for(int i=0;i<rows.length;i++) {
          jTable1.getModel().setValueAt(true,jTable1.convertRowIndexToModel(rows[i]),0);
          System.out.println("row "+i);
        } 
      }
    }//GEN-LAST:event_enable_table_rowsActionPerformed

    private void log_taFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_log_taFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_log_taFocusGained

    private void log_taKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_log_taKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_log_taKeyTyped

    private void logpanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_logpanelFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_logpanelFocusGained

    private void jTable1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyTyped
    }//GEN-LAST:event_jTable1KeyTyped

    private void jTabbedPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane1MouseClicked
      //System.out.println("evt tab");
      if(jTabbedPane1.getSelectedIndex()==5) {
        //System.out.println("evt tab");
        jTextArea1.requestFocus();
      }
    }//GEN-LAST:event_jTabbedPane1MouseClicked

    private void enable_mp3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enable_mp3ActionPerformed
      if(prefs!=null) prefs.putBoolean("enable_mp3", enable_mp3.isSelected());
    }//GEN-LAST:event_enable_mp3ActionPerformed

    private void enable_audioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enable_audioActionPerformed
      if(prefs!=null) prefs.putBoolean("enable_audio", enable_audio.isSelected());
    }//GEN-LAST:event_enable_audioActionPerformed

    private void freq_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq_searchActionPerformed
      do_zipsearch=1;
    }//GEN-LAST:event_freq_searchActionPerformed

    private void freq_search2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq_search2ActionPerformed
      do_zipsearch2=1;
    }//GEN-LAST:event_freq_search2ActionPerformed

    private void inc_busActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inc_busActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inc_busActionPerformed

    private void inc_p25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inc_p25ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inc_p25ActionPerformed

    private void testfreqsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testfreqsActionPerformed
      if(is_connected==0) do_connect();
      do_test_freqs=1;
    }//GEN-LAST:event_testfreqsActionPerformed

    private void append_ccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_append_ccActionPerformed
      if(is_connected==0) do_connect();
      do_append_roaming=1;
    }//GEN-LAST:event_append_ccActionPerformed

    private void use_freq_primaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_use_freq_primaryActionPerformed
          try {
              for(int i=0;i<250;i++) {
               freq_table.getModel().setValueAt( null, i, 5); 
              }
          } catch(Exception e) {
            e.printStackTrace();
          }
      if(is_connected==0) do_connect();
      do_roam_freq=1;
    }//GEN-LAST:event_use_freq_primaryActionPerformed

    private void inc_dup_freqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inc_dup_freqActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inc_dup_freqActionPerformed

    private void restore_roamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restore_roamActionPerformed
      if(is_connected==0) do_connect();
      do_restore_roaming=1;
    }//GEN-LAST:event_restore_roamActionPerformed

    private void backup_roamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backup_roamActionPerformed
      if(is_connected==0) do_connect();
      do_backup_roaming=1;
    }//GEN-LAST:event_backup_roamActionPerformed

    private void button_write_configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_write_configActionPerformed
      do_read_config=1;
      do_write_config=1;
      cpanel.reset_ref_est();

      //current_sys_id = 0;
      //current_wacn_id = 0; 
      wacn.setText("");
      sysid.setText("");
      nac.setText("");
    }//GEN-LAST:event_button_write_configActionPerformed

    private void erase_roamingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_erase_roamingActionPerformed
      if(is_connected==0) do_connect();
      do_erase_roaming=1;
    }//GEN-LAST:event_erase_roamingActionPerformed

    private void auto_flash_tgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auto_flash_tgActionPerformed
      if(prefs!=null) prefs.putBoolean( "tg_auto_flash", auto_flash_tg.isSelected());
    }//GEN-LAST:event_auto_flash_tgActionPerformed

    private void disable_encryptedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disable_encryptedActionPerformed
      if(prefs!=null) prefs.putBoolean( "enc_auto_flash", disable_encrypted.isSelected());
    }//GEN-LAST:event_disable_encryptedActionPerformed

    private void roamingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_roamingActionPerformed
      if( roaming.isSelected() ) {
        no_voice_panel.setVisible(true);
      }
      else {
        no_voice_panel.setVisible(false);
      }
    }//GEN-LAST:event_roamingActionPerformed

    private void freq_tableKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_freq_tableKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_freq_tableKeyTyped


    private void autoscale_constActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoscale_constActionPerformed
      if(prefs!=null) prefs.putBoolean("autoscale_const", autoscale_const.isSelected());
    }//GEN-LAST:event_autoscale_constActionPerformed

    private void nsymbolsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nsymbolsActionPerformed
       if(prefs!=null) prefs.putInt("nsymbols", nsymbols.getSelectedIndex());
    }//GEN-LAST:event_nsymbolsActionPerformed

    private void dmr_backupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dmr_backupActionPerformed
      try {

        FileNameExtensionFilter filter = new FileNameExtensionFilter( "dmr backup file", "dmr");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showDialog(parent, "Export DMR Backup .dmr file");

        ObjectOutputStream oos;

        if(returnVal == JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          oos = new ObjectOutputStream( new FileOutputStream(file) );

          oos.writeUTF(lcn1_freq.getText());
          oos.writeUTF(lcn2_freq.getText());
          oos.writeUTF(lcn3_freq.getText());
          oos.writeUTF(lcn4_freq.getText());
          oos.writeUTF(lcn5_freq.getText());
          oos.writeUTF(lcn6_freq.getText());
          oos.writeUTF(lcn7_freq.getText());
          oos.writeUTF(lcn8_freq.getText());
          oos.writeUTF(lcn9_freq.getText());
          oos.writeUTF(lcn10_freq.getText());
          oos.writeUTF(lcn11_freq.getText());
          oos.writeUTF(lcn12_freq.getText());
          oos.writeUTF(lcn13_freq.getText());
          oos.writeUTF(lcn14_freq.getText());
          oos.writeUTF(lcn15_freq.getText());

          oos.writeBoolean(dmr_cc_en1.isSelected());
          oos.writeBoolean(dmr_cc_en2.isSelected());
          oos.writeBoolean(dmr_cc_en3.isSelected());
          oos.writeBoolean(dmr_cc_en4.isSelected());
          oos.writeBoolean(dmr_cc_en5.isSelected());
          oos.writeBoolean(dmr_cc_en6.isSelected());
          oos.writeBoolean(dmr_cc_en7.isSelected());
          oos.writeBoolean(dmr_cc_en8.isSelected());
          oos.writeBoolean(dmr_cc_en9.isSelected());
          oos.writeBoolean(dmr_cc_en10.isSelected());
          oos.writeBoolean(dmr_cc_en11.isSelected());
          oos.writeBoolean(dmr_cc_en12.isSelected());
          oos.writeBoolean(dmr_cc_en13.isSelected());
          oos.writeBoolean(dmr_cc_en14.isSelected());
          oos.writeBoolean(dmr_cc_en15.isSelected());

          oos.writeBoolean(dmr_conplus.isSelected());
          oos.writeBoolean(dmr_conventional.isSelected());

          oos.writeBoolean(dmr_slot1.isSelected());
          oos.writeBoolean(dmr_slot2.isSelected());

          oos.writeUTF(dmr_sys_id.getText());

          oos.flush();
          oos.close();

          parent.setStatus("DMR backup completed.");

        }

      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_dmr_backupActionPerformed

    private void dmr_restoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dmr_restoreActionPerformed
      try {

        FileNameExtensionFilter filter = new FileNameExtensionFilter( "DMR restore backup file", "dmr");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showDialog(parent, "DMR restore backup file (.dmr) file");


        if(returnVal == JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          FileInputStream fis = new FileInputStream(file);
          ObjectInputStream ois = new ObjectInputStream(fis);

          lcn1_freq.setText( ois.readUTF() );
          lcn2_freq.setText( ois.readUTF() );
          lcn3_freq.setText( ois.readUTF() );
          lcn4_freq.setText( ois.readUTF() );
          lcn5_freq.setText( ois.readUTF() );
          lcn6_freq.setText( ois.readUTF() );
          lcn7_freq.setText( ois.readUTF() );
          lcn8_freq.setText( ois.readUTF() );
          lcn9_freq.setText( ois.readUTF() );
          lcn10_freq.setText( ois.readUTF() );
          lcn11_freq.setText( ois.readUTF() );
          lcn12_freq.setText( ois.readUTF() );
          lcn13_freq.setText( ois.readUTF() );
          lcn14_freq.setText( ois.readUTF() );
          lcn15_freq.setText( ois.readUTF() );

          dmr_cc_en1.setSelected( ois.readBoolean() );
          dmr_cc_en2.setSelected( ois.readBoolean() );
          dmr_cc_en3.setSelected( ois.readBoolean() );
          dmr_cc_en4.setSelected( ois.readBoolean() );
          dmr_cc_en5.setSelected( ois.readBoolean() );
          dmr_cc_en6.setSelected( ois.readBoolean() );
          dmr_cc_en7.setSelected( ois.readBoolean() );
          dmr_cc_en8.setSelected( ois.readBoolean() );
          dmr_cc_en9.setSelected( ois.readBoolean() );
          dmr_cc_en10.setSelected( ois.readBoolean() );
          dmr_cc_en11.setSelected( ois.readBoolean() );
          dmr_cc_en12.setSelected( ois.readBoolean() );
          dmr_cc_en13.setSelected( ois.readBoolean() );
          dmr_cc_en14.setSelected( ois.readBoolean() );
          dmr_cc_en15.setSelected( ois.readBoolean() );

          dmr_conplus.setSelected( ois.readBoolean() );
          dmr_conventional.setSelected( ois.readBoolean() );

          dmr_slot1.setSelected( ois.readBoolean() );
          dmr_slot2.setSelected( ois.readBoolean() );

          dmr_sys_id.setText( ois.readUTF() );

          parent.setStatus("DMR Backup imported.");

          //initiate write config
            do_read_config=1;
            do_write_config=1;

            op_mode.setSelectedIndex(1);

            cpanel.reset_ref_est();


            //current_sys_id = 0;
            //current_wacn_id = 0; 
            wacn.setText("");
            sysid.setText("");
            nac.setText("");

        }

      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_dmr_restoreActionPerformed

    private void dmr_write_configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dmr_write_configActionPerformed
      do_read_config=1;
      do_write_config=1;

      op_mode.setSelectedIndex(1);

      cpanel.reset_ref_est();


      //current_sys_id = 0;
      //current_wacn_id = 0; 
      wacn.setText("");
      sysid.setText("");
      nac.setText("");
    }//GEN-LAST:event_dmr_write_configActionPerformed

    private void dmr_conplusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dmr_conplusActionPerformed
      update_dmr_lcn1_label();
    }//GEN-LAST:event_dmr_conplusActionPerformed

    private void dmr_clear_freqsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dmr_clear_freqsActionPerformed
      lcn1_freq.setText("");
      lcn2_freq.setText("");
      lcn3_freq.setText("");
      lcn4_freq.setText("");
      lcn5_freq.setText("");
      lcn6_freq.setText("");
      lcn7_freq.setText("");
      lcn8_freq.setText("");
      lcn9_freq.setText("");
      lcn10_freq.setText("");
      lcn11_freq.setText("");
      lcn12_freq.setText("");
      lcn13_freq.setText("");
      lcn14_freq.setText("");
      lcn15_freq.setText("");
    }//GEN-LAST:event_dmr_clear_freqsActionPerformed

    private void op_modeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_modeActionPerformed
      if( op_mode.getSelectedIndex() == 0) {
        controlchannel.setVisible(true);
        conventionalchannel.setVisible(true);
      }
      else {
        controlchannel.setVisible(false);
        conventionalchannel.setVisible(false);
      }

      //update_op_mode( op_mode.getSelectedIndex() );
      int mode = op_mode.getSelectedIndex();
      if( mode == 0) {
        freq_label.setText("P25 Frequency");
      }
      if( mode == 1) {
        freq_label.setText("Frequency");
      }

    }//GEN-LAST:event_op_modeActionPerformed

    public void update_op_mode(int mode) {

      if( mode == 1) {
        freq_label.setText("P25 Frequency");
        jTabbedPane1.setEnabledAt(3, true);
      }
      else {
        freq_label.setText("Frequency");
        //jTabbedPane1.setEnabledAt(3, false);
      }
    }

    private void controlchannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_controlchannelActionPerformed
      freq_label.setText("Control Channel Frequency");
    }//GEN-LAST:event_controlchannelActionPerformed

    private void conventionalchannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conventionalchannelActionPerformed
      freq_label.setText("Conventional Channel Frequency");
    }//GEN-LAST:event_conventionalchannelActionPerformed

    private void dmr_conventionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dmr_conventionalActionPerformed
      update_dmr_lcn1_label();
    }//GEN-LAST:event_dmr_conventionalActionPerformed

    private void import_csvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_import_csvActionPerformed
      if(is_connected==0) do_connect();
      do_restore_tg_csv=1;
    }//GEN-LAST:event_import_csvActionPerformed

    private void mp3_separate_filesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mp3_separate_filesActionPerformed
     if(prefs!=null) prefs.putBoolean("mp3_separate_files", mp3_separate_files.isSelected());
    }//GEN-LAST:event_mp3_separate_filesActionPerformed

    private void audio_dev_listValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_audio_dev_listValueChanged
      try {
        String str = (String) audio_dev_list.getSelectedValue();
        if(str!=null) {
          if(prefs!=null) prefs.put("audio_output_device", str); 
          if(aud!=null) aud.dev_changed();
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_audio_dev_listValueChanged

    private void select_homeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_homeActionPerformed
      get_home_dir();
    }//GEN-LAST:event_select_homeActionPerformed

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
      save_position();
    }//GEN-LAST:event_formComponentMoved

    private void import_aliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_import_aliasActionPerformed
      do_alias_import=1;
    }//GEN-LAST:event_import_aliasActionPerformed

    private void triple_click_opt4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_triple_click_opt4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_triple_click_opt4ActionPerformed

    private void quad_click_opt4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quad_click_opt4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_quad_click_opt4ActionPerformed

    private void auto_pop_tableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auto_pop_tableActionPerformed
      if(prefs!=null) prefs.putBoolean( "tg_auto_pop_table", auto_pop_table.isSelected());
    }//GEN-LAST:event_auto_pop_tableActionPerformed


    ///////////////////////////////////////
    ///////////////////////////////////////
    public void do_follow() {
      try {
        String cmd= new String("f\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    ///////////////////////////////////////
    ///////////////////////////////////////
    public void do_skip() {
      try {
        String cmd= new String("s \r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    private void muteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_muteActionPerformed
      if(mute.isSelected()) {
        mute.setBackground(java.awt.Color.green);
      }
      else {
        mute.setBackground(java.awt.Color.gray);
      }
    }//GEN-LAST:event_muteActionPerformed

    private void delete_roamingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delete_roamingActionPerformed
      delete_roaming_rows();
    }//GEN-LAST:event_delete_roamingActionPerformed

    private void tg_edit_delActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tg_edit_delActionPerformed
      delete_talkgroup_rows();
    }//GEN-LAST:event_tg_edit_delActionPerformed

    private void process_rid_aliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_process_rid_aliasActionPerformed
        if(prefs!=null) prefs.putBoolean("process_rid_alias", process_rid_alias.isSelected());
    }//GEN-LAST:event_process_rid_aliasActionPerformed

    private void allow_tg_pri_intActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allow_tg_pri_intActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_allow_tg_pri_intActionPerformed

    private void enc_modeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enc_modeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_enc_modeActionPerformed

    private void en_zero_ridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_zero_ridActionPerformed
        if(prefs!=null) prefs.putBoolean("en_zero_rid", en_zero_rid.isSelected());
    }//GEN-LAST:event_en_zero_ridActionPerformed

    private void p25_tone_volActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_p25_tone_volActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_p25_tone_volActionPerformed

    private void adv_write_configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adv_write_configActionPerformed
        do_read_config=1;
        do_write_config=1;
        cpanel.reset_ref_est();

        //current_sys_id = 0;
        //current_wacn_id = 0;
        wacn.setText("");
        sysid.setText("");
        nac.setText("");
    }//GEN-LAST:event_adv_write_configActionPerformed

    private void if_slideStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_if_slideStateChanged
      String cmd= new String("if_freq "+if_slide.getValue()+"\r\n");
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      System.out.println(cmd);
    }//GEN-LAST:event_if_slideStateChanged

    private void sig_cmdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sig_cmdKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_sig_cmdKeyReleased

    private void do_mp3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_do_mp3ActionPerformed
        // TODO add your handling code here:
     if(prefs!=null) prefs.putBoolean("do_mp3", do_mp3.isSelected());
     if(prefs!=null) prefs.putBoolean("do_wav", !do_mp3.isSelected());
    }//GEN-LAST:event_do_mp3ActionPerformed

    private void do_wavActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_do_wavActionPerformed
        // TODO add your handling code here:
     if(prefs!=null) prefs.putBoolean("do_wav", do_wav.isSelected());
     if(prefs!=null) prefs.putBoolean("do_mp3", !do_wav.isSelected());
    }//GEN-LAST:event_do_wavActionPerformed

    private void audio_hiqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_audio_hiqActionPerformed
        // TODO add your handling code here:
     if(prefs!=null) prefs.putBoolean("audio_hiq", audio_hiq.isSelected());
     if(prefs!=null) prefs.putBoolean("audio_lowq", !audio_hiq.isSelected());
    }//GEN-LAST:event_audio_hiqActionPerformed

    private void audio_lowqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_audio_lowqActionPerformed
        // TODO add your handling code here:
     if(prefs!=null) prefs.putBoolean("audio_lowq", audio_lowq.isSelected());
     if(prefs!=null) prefs.putBoolean("audio_hiq", !audio_lowq.isSelected());
    }//GEN-LAST:event_audio_lowqActionPerformed

    private void gen_sys_infoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gen_sys_infoActionPerformed
      if( si !=null) {
        si.setVisible(true);
        si.addInfo(wacn.getText(), sysid.getText(), nac.getText(), siteid.getText(), rfid.getText(), roaming_tests.getSites());
      }
    }//GEN-LAST:event_gen_sys_infoActionPerformed

    private void edit_display_viewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edit_display_viewActionPerformed
      dframe.setVisible(true);
    }//GEN-LAST:event_edit_display_viewActionPerformed

    private void vga_stepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vga_stepActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_vga_stepActionPerformed

    private void edit_aliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edit_aliasActionPerformed
      edit_alias();
    }//GEN-LAST:event_edit_aliasActionPerformed

    private void read_roamingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_read_roamingActionPerformed
      do_read_roaming=1;
              for(int i=0;i<roaming_tests.MAXRECS;i++) {
               freq_table.getModel().setValueAt( null, i, 0); 
               freq_table.getModel().setValueAt( null, i, 1); 
               freq_table.getModel().setValueAt( null, i, 2); 
               freq_table.getModel().setValueAt( null, i, 3); 
               freq_table.getModel().setValueAt( null, i, 4); 
               freq_table.getModel().setValueAt( null, i, 5); 
               freq_table.getModel().setValueAt( null, i, 6); 
               freq_table.getModel().setValueAt( null, i, 7); 
               freq_table.getModel().setValueAt( null, i, 8); 
               freq_table.getModel().setValueAt( null, i, 9); 
               freq_table.getModel().setValueAt( null, i, 10); 
              }
            freq_table.setRowSelectionInterval(0,0);
    }//GEN-LAST:event_read_roamingActionPerformed

    private void holdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_holdActionPerformed
      do_follow();
    }//GEN-LAST:event_holdActionPerformed

    private void skipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skipActionPerformed
      do_skip();
    }//GEN-LAST:event_skipActionPerformed

    private void dvpopoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dvpopoutActionPerformed
      do_dv_popout();
    }//GEN-LAST:event_dvpopoutActionPerformed

    public void do_dv_popout() {
      try {
        if(do_read_talkgroups==1) return;

        displayviewmain_border.remove(display_frame);
        dvout.split_top.add(display_frame, java.awt.BorderLayout.CENTER);
        displayviewmain_border.repaint();

        logpanel.remove(tg_scroll_pane);
        dvout.split_bottom.add(tg_scroll_pane, java.awt.BorderLayout.CENTER);

        dvout.setVisible(true);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    private void tglog_colorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglog_colorActionPerformed
      Color color = JColorChooser.showDialog(parent, "TG Font Color", tg_font_color); 
      if(color!=null) tg_font_color=color;
      if( parent.prefs!=null && color!=null) {
        parent.prefs.putInt("tg_font_color",  tg_font_color.getRGB() );
        log_ta.setForeground(color);
      }
    }//GEN-LAST:event_tglog_colorActionPerformed

    private void tglog_fontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglog_fontActionPerformed

      jfc.setSelectedFontFamily(tg_font_name);
      jfc.setSelectedFontSize(tg_font_size);
      jfc.setSelectedFontStyle(tg_font_style);


      int result = jfc.showDialog(this);
      if( result == JFontChooser.OK_OPTION ) {
        tg_font_name = jfc.getSelectedFontFamily();
        tg_font_style = jfc.getSelectedFontStyle();
        tg_font_size = jfc.getSelectedFontSize();
        log_ta.setFont(new java.awt.Font(tg_font_name, tg_font_style, tg_font_size)); 
      }
      if(parent.prefs!=null) {
        parent.prefs.put("tg_font_name", jfc.getSelectedFontFamily() );
        parent.prefs.putInt("tg_font_style", jfc.getSelectedFontStyle() );
        parent.prefs.putInt("tg_font_size", jfc.getSelectedFontSize() );
      }


    }//GEN-LAST:event_tglog_fontActionPerformed

    private void tglog_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglog_editActionPerformed
      tglog_e.setVisible(true);
    }//GEN-LAST:event_tglog_editActionPerformed

    private void reset_defaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reset_defaultsActionPerformed
      String cmd= new String("factory\r\n");
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      setStatus("Reset config to factory defaults");
    }//GEN-LAST:event_reset_defaultsActionPerformed

    private void save_iqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_iqActionPerformed
      try {

        FileNameExtensionFilter filter = new FileNameExtensionFilter( "BTT IQ file", "biq");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showDialog(parent, "Save BTT .iq file");


        if(returnVal == JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          fos_iq = new FileOutputStream(file);
          save_iq_len = 5242880;
          iq_out=0;

          int avail = serial_port.bytesAvailable();
          byte[] b = new byte[avail];
          int len = serial_port.readBytes(b, avail);

          String cmd= new String("send_iq 30\r\n");
          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
          setStatus("Saving IQ File For 30 Seconds");
        }

      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_save_iqActionPerformed

    private void hold1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hold1ActionPerformed
      do_follow();
    }//GEN-LAST:event_hold1ActionPerformed

    private void skip1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skip1ActionPerformed
      do_skip();
      setStatus("skipping current TG");
    }//GEN-LAST:event_skip1ActionPerformed

    private void show_keywordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_show_keywordsActionPerformed
        // TODO add your handling code here:
      dframe.show_help();
    }//GEN-LAST:event_show_keywordsActionPerformed

    private void edit_alias1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edit_alias1ActionPerformed
      edit_alias();
    }//GEN-LAST:event_edit_alias1ActionPerformed

    private void si_cpu_highActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_si_cpu_highActionPerformed
        if(prefs!=null) prefs.put("si_cpu", "high");
    }//GEN-LAST:event_si_cpu_highActionPerformed

    private void si_cpu_normalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_si_cpu_normalActionPerformed
        if(prefs!=null) prefs.put("si_cpu", "normal");
    }//GEN-LAST:event_si_cpu_normalActionPerformed

    private void si_cpu_lowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_si_cpu_lowActionPerformed
        if(prefs!=null) prefs.put("si_cpu", "low");
    }//GEN-LAST:event_si_cpu_lowActionPerformed

    private void si_cpu_battery_savingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_si_cpu_battery_savingActionPerformed
        if(prefs!=null) prefs.put("si_cpu", "battery");
    }//GEN-LAST:event_si_cpu_battery_savingActionPerformed

    private void si_cpu_offActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_si_cpu_offActionPerformed
        if(prefs!=null) prefs.put("si_cpu", "off");
    }//GEN-LAST:event_si_cpu_offActionPerformed

    public void update_zones() {
      if( (current_tgzone&0x01)>0) z1.setSelected(true);
        else z1.setSelected(false);
      if( (current_tgzone&0x02)>0) z2.setSelected(true);
        else z2.setSelected(false);
      if( (current_tgzone&0x04)>0) z3.setSelected(true);
        else z3.setSelected(false);
      if( (current_tgzone&0x08)>0) z4.setSelected(true);
        else z4.setSelected(false);
      if( (current_tgzone&0x10)>0) z5.setSelected(true);
        else z5.setSelected(false);
      if( (current_tgzone&0x20)>0) z6.setSelected(true);
        else z6.setSelected(false);
      if( (current_tgzone&0x40)>0) z7.setSelected(true);
        else z7.setSelected(false);
      if( (current_tgzone&0x80)>0) z8.setSelected(true);
        else z8.setSelected(false);

      if( (current_tgzone&0x100)>0) z9.setSelected(true);
        else z9.setSelected(false);
      if( (current_tgzone&0x200)>0) z10.setSelected(true);
        else z10.setSelected(false);
      if( (current_tgzone&0x400)>0) z11.setSelected(true);
        else z11.setSelected(false);
      if( (current_tgzone&0x800)>0) z12.setSelected(true);
        else z12.setSelected(false);

      if(z1.isSelected()) {
        z1.setBackground(java.awt.Color.green);
      }
      else {
        z1.setBackground(java.awt.Color.gray);
      }
      if(z2.isSelected()) {
        z2.setBackground(java.awt.Color.green);
      }
      else {
        z2.setBackground(java.awt.Color.gray);
      }
      if(z3.isSelected()) {
        z3.setBackground(java.awt.Color.green);
      }
      else {
        z3.setBackground(java.awt.Color.gray);
      }
      if(z4.isSelected()) {
        z4.setBackground(java.awt.Color.green);
      }
      else {
        z4.setBackground(java.awt.Color.gray);
      }
      if(z5.isSelected()) {
        z5.setBackground(java.awt.Color.green);
      }
      else {
        z5.setBackground(java.awt.Color.gray);
      }
      if(z6.isSelected()) {
        z6.setBackground(java.awt.Color.green);
      }
      else {
        z6.setBackground(java.awt.Color.gray);
      }
      if(z7.isSelected()) {
        z7.setBackground(java.awt.Color.green);
      }
      else {
        z7.setBackground(java.awt.Color.gray);
      }
      if(z8.isSelected()) {
        z8.setBackground(java.awt.Color.green);
      }
      else {
        z8.setBackground(java.awt.Color.gray);
      }
      if(z9.isSelected()) {
        z9.setBackground(java.awt.Color.green);
      }
      else {
        z9.setBackground(java.awt.Color.gray);
      }
      if(z10.isSelected()) {
        z10.setBackground(java.awt.Color.green);
      }
      else {
        z10.setBackground(java.awt.Color.gray);
      }
      if(z11.isSelected()) {
        z11.setBackground(java.awt.Color.green);
      }
      else {
        z11.setBackground(java.awt.Color.gray);
      }
      if(z12.isSelected()) {
        z12.setBackground(java.awt.Color.green);
      }
      else {
        z12.setBackground(java.awt.Color.gray);
      }
    }

    private void z1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z1ActionPerformed

      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(1);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone1_alias", zone_alias); 
        z1.setToolTipText(zone_alias);
        return;
      }

      if(z1.isSelected()) current_tgzone |= 0x01;
        else current_tgzone &= (~0x01)&0xffff; 

      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

      update_zones();
    }//GEN-LAST:event_z1ActionPerformed

    private void z2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z2ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(2);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone2_alias", zone_alias); 
        z2.setToolTipText(zone_alias);
        return;
      }
      if(z2.isSelected()) current_tgzone |= 0x02;
        else current_tgzone &= (~0x02)&0xffff; 

      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      update_zones();
    }//GEN-LAST:event_z2ActionPerformed

    private void z3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z3ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(3);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone3_alias", zone_alias); 
        z3.setToolTipText(zone_alias);
        return;
      }
      if(z3.isSelected()) current_tgzone |= 0x04;
        else current_tgzone &= (~0x04)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      update_zones();
    }//GEN-LAST:event_z3ActionPerformed

    private void z4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z4ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(4);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone4_alias", zone_alias); 
        z4.setToolTipText(zone_alias);
        return;
      }
      if(z4.isSelected()) current_tgzone |= 0x08;
        else current_tgzone &= (~0x08)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      update_zones();
    }//GEN-LAST:event_z4ActionPerformed

    private void z5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z5ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(5);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone5_alias", zone_alias); 
        z5.setToolTipText(zone_alias);
        return;
      }
      if(z5.isSelected()) current_tgzone |= 0x10;
        else current_tgzone &= (~0x10)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      update_zones();
    }//GEN-LAST:event_z5ActionPerformed

    private void z6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z6ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(6);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone6_alias", zone_alias); 
        z6.setToolTipText(zone_alias);
        return;
      }
      if(z6.isSelected()) current_tgzone |= 0x20;
        else current_tgzone &= (~0x20)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      update_zones();
    }//GEN-LAST:event_z6ActionPerformed

    private void z7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z7ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(7);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone7_alias", zone_alias); 
        z7.setToolTipText(zone_alias);
        return;
      }
      if(z7.isSelected()) current_tgzone |= 0x40;
        else current_tgzone &= (~0x40)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      update_zones();
    }//GEN-LAST:event_z7ActionPerformed

    private void z8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z8ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(8);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone8_alias", zone_alias); 
        z8.setToolTipText(zone_alias);
        return;
      }
      if(z8.isSelected()) current_tgzone |= 0x80;
        else current_tgzone &= (~0x80)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      update_zones();
    }//GEN-LAST:event_z8ActionPerformed

    private void z9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z9ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(9);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone9_alias", zone_alias); 
        z9.setToolTipText(zone_alias);
        return;
      }
      if(z9.isSelected()) current_tgzone |= 0x100;
        else current_tgzone &= (~0x100)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      update_zones();
    }//GEN-LAST:event_z9ActionPerformed

    private void z10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z10ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(10);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone10_alias", zone_alias); 
        z10.setToolTipText(zone_alias);
        return;
      }
      if(z10.isSelected()) current_tgzone |= 0x200;
        else current_tgzone &= (~0x200)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      update_zones();
    }//GEN-LAST:event_z10ActionPerformed

    private void z11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z11ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(11);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone11_alias", zone_alias); 
        z11.setToolTipText(zone_alias);
        return;
      }
      if(z11.isSelected()) current_tgzone |= 0x400;
        else current_tgzone &= (~0x400)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      update_zones();
    }//GEN-LAST:event_z11ActionPerformed

    private void z12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z12ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(12);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone12_alias", zone_alias); 
        z12.setToolTipText(zone_alias);
        return;
      }
      if(z12.isSelected()) current_tgzone |= 0x800;
        else current_tgzone &= (~0x800)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      update_zones();
    }//GEN-LAST:event_z12ActionPerformed

    private void set_zonesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set_zonesActionPerformed
      int zone = Integer.parseInt( JOptionPane.showInputDialog((JFrame) this,
        "Zone # (1-8)",
        "[Zone Number?]",
        JOptionPane.INFORMATION_MESSAGE) );

      int[] rows = jTable1.getSelectedRows();
      if(rows.length>0) {
        for(int i=0;i<rows.length;i++) {
          jTable1.getModel().setValueAt(new Integer(zone),jTable1.convertRowIndexToModel(rows[i]),7);
        } 
      }
    }//GEN-LAST:event_set_zonesActionPerformed

    private void bt_vol_slider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bt_vol_slider1StateChanged
      volume_label1.setText( String.format( "%3.2f", (float) bt_vol_slider1.getValue() / 100.0f ) );
    }//GEN-LAST:event_bt_vol_slider1StateChanged

    private void allow_unknown_tg_cbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allow_unknown_tg_cbActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_allow_unknown_tg_cbActionPerformed

    private void vga_calActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vga_calActionPerformed
      try {
        String cmd= new String("vga_gain -2\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_vga_calActionPerformed

    private void vga_targetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vga_targetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_vga_targetActionPerformed

    private void status_format_ccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_status_format_ccActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_status_format_ccActionPerformed

    private void freememActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freememActionPerformed
      if(rt!=null) rt.gc();
    }//GEN-LAST:event_freememActionPerformed

    private void separate_ridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separate_ridActionPerformed
     if(prefs!=null) prefs.putBoolean("separate_rid", separate_rid.isSelected());
    }//GEN-LAST:event_separate_ridActionPerformed

    private void monitor_selActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monitor_selActionPerformed
      int[] rows = freq_table.getSelectedRows();
      monitor_freq( rows[0] );
    }//GEN-LAST:event_monitor_selActionPerformed

    private void prev_freqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prev_freqActionPerformed
      sel_freq--;
      if(sel_freq<0) sel_freq=0;
      monitor_freq( sel_freq );

      freq_table.setRowSelectionInterval(sel_freq,sel_freq);
    }//GEN-LAST:event_prev_freqActionPerformed

    private void next_freqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_next_freqActionPerformed
      do_next_freq();
    }//GEN-LAST:event_next_freqActionPerformed

    /////////////////////////////////
    /////////////////////////////////
    public void do_next_freq() {
      sel_freq++;
      monitor_freq( sel_freq );
      freq_table.setRowSelectionInterval(sel_freq,sel_freq);
    }

    private void add_ridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add_ridActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_add_ridActionPerformed

    private void pause_roamingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pause_roamingActionPerformed
      try {
        int state = 1;
        if( pause_roaming.isSelected() ) state=0;

        String cmd= new String("roaming "+state+"\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        if(state==1) {
          setStatus("roaming on");
          pause_roaming.setBackground( Color.green );
        }
        if(state==0) {
          setStatus("roaming off");
          pause_roaming.setBackground( Color.gray );
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_pause_roamingActionPerformed

    private void en_evt_outputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_evt_outputActionPerformed
     if(prefs!=null) prefs.putBoolean("en_evt_output", en_evt_output.isSelected());
    }//GEN-LAST:event_en_evt_outputActionPerformed

    private void en_rdioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_rdioActionPerformed
        try {
            prefs.putBoolean("en_rdio", en_rdio.isSelected());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_en_rdioActionPerformed

    private void rdio_maskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdio_maskActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rdio_maskActionPerformed

///////////////////////////////////////////////////
///////////////////////////////////////////////////
void monitor_freq(int row) {
  String freq_to_use = (String) freq_table.getModel().getValueAt(row,3);
  String demod_str = (String) freq_table.getModel().getValueAt(row,7);

  try {
    double d = new Double(freq_to_use).doubleValue();
  } catch(Exception e) {
    sel_freq=0;
    row = 0;
    freq_to_use = (String) freq_table.getModel().getValueAt(row,3);
    demod_str = (String) freq_table.getModel().getValueAt(row,7);
  }

  setStatus("Monitoring "+freq_to_use+" MHz");

  int demod_mode = 0;
  if(demod_str.contains("LSM")) demod_mode=0; 
  if(demod_str.contains("CQPSK")) demod_mode=1; 

  try {
    clear_sys_info();
    String cmd= new String("freq "+freq_to_use+"\r\n");
    serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
    SLEEP(10);
    cmd= new String("demod "+demod_mode+"\r\n");
    serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
  } catch(Exception e) {
    e.printStackTrace();
  }
}


///////////////////////////////////////////////////
///////////////////////////////////////////////////
public void edit_alias() {
  if(alias_dialog==null) alias_dialog = new aliasEntry(alias);

  if(alias_dialog!=null && src_uid!=0) {
    alias_dialog.setRID( src_uid );
    alias_dialog.setVisible(true);
  }
  else if(alias_dialog!=null && prev_uid!=0) {
    alias_dialog.setRID( prev_uid );
    alias_dialog.setVisible(true);
  }
}

    public void enable_voice() {
      frequency_tf1.setEnabled(false);
      roaming.setSelected(false);
      roaming.setEnabled(false);
    }

    public void enable_cc() {
      frequency_tf1.setEnabled(true);
      roaming.setSelected(true);
      roaming.setEnabled(true);
    }

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void update_dmr_lcn1_label() {
  if(dmr_conventional.isSelected()) {
    dmr_lcn1_label.setText("DMR Conventional Frequency");
  }
  else {
    dmr_lcn1_label.setText("LCN1 Frequency");
  }
}
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void do_meta() {

  if(current_talkgroup==null || Integer.valueOf(current_talkgroup)==0) {
    //did_metadata=1;
    return;
  }
  if(current_wacn_id==0 || current_wacn_id!=traffic_wacn_id) {
    //did_metadata=1;
    return;
  }
  if(current_sys_id==0 || current_sys_id!=traffic_sys_id) {
    //did_metadata=1;
    return;
  }

  if(is_enc==0) {

    if(did_metadata==1) return;

    if( is_dmr_mode==0 && src_uid==0 && !en_zero_rid.isSelected() ) {
      did_metadata=1;
      return;
    }
  }

  //is_enc==1 always gets here
  String enc="";
  if(is_enc==1) enc="ENCRYPTED,";

  is_enc=0;

    try {

        String log_format = tglog_e.getFormat();

        String log_str = "\r\n"+dframe.do_subs(log_format,true)+","+enc;

        String date = formatter_date.format(new java.util.Date() );
        current_date=new String(date);  //date changed

        try {
          if(fos_meta!=null) fos_meta.close();
        } catch(Exception e) {
          e.printStackTrace();
        }

        try {
          String fs =  System.getProperty("file.separator");
          meta_file = new File(document_dir+fs+sys_mac_id+fs+"p25rx_recmeta_"+current_date+".txt");
          fos_meta = new FileOutputStream( meta_file, true ); 
        } catch(Exception e) {
          e.printStackTrace();
        }


      try {
        //fos_meta.write(log_str.getBytes(),0,log_str.length());  //write int num records
        log_str = log_str.trim()+"\r\n";
        fos_meta.write(log_str.getBytes(),0,log_str.length());  //write int num records
        fos_meta.flush();
        fos_meta.close();
      } catch(Exception e) {
        e.printStackTrace();
      }

      log_str = "\r\n"+log_str.trim();
      String text = log_ta.getText().trim();

      log_ta.setText(text.concat( new String(log_str.getBytes()) ).trim()+"\n");

      /*
      if( log_ta.getText().length() > 16000 ) {
        String new_text = text.substring(8000,text.length()-1);
        log_ta.setText(new_text.trim()+"\n");
      }
      */
      if( log_ta.getText().length() > 64000 ) {
        log_ta.replaceRange("",0,32000);
      }


      log_ta.setCaretPosition(log_ta.getText().length());
      log_ta.getCaret().setVisible(true);
      log_ta.getCaret().setBlinkRate(250);

      tg_scroll_pane.getHorizontalScrollBar().setValue(0);

      did_metadata=1;
      tg_pri=0;

      prev_uid = src_uid;
      if(is_dmr_mode==0) src_uid=0;
    } catch(Exception e) {
      e.printStackTrace();
    }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void open_audio_output_files() {
  try {
    String fs =  System.getProperty("file.separator");

    Path path = Paths.get(home_dir+fs+sys_mac_id);
    Files.createDirectories(path);

    home_dir_label.setText(home_dir);

    String date = formatter_date.format(new java.util.Date() );
    current_date=new String(date);  //date changed

    meta_file = new File(document_dir+fs+sys_mac_id+fs+"p25rx_recmeta_"+current_date+".txt");
    String exe_path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath().toString();
    exe_path = exe_path.replace("BTConfig.exe", "");
    System.out.println("log file path: "+exe_path+"p25rx_conlog_"+current_date+".txt");
    fos_meta = new FileOutputStream( meta_file, true ); 

  } catch(Exception e) {
    e.printStackTrace();
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    JOptionPane.showMessageDialog(this, sw.toString(), "ok", JOptionPane.OK_OPTION);
  }


  try {
    if(aud==null && parent!=null) {
      aud = new audio(parent);
    }
    if(aud!=null) aud.dev_changed();
    if(aud!=null) aud.audio_tick();
  } catch(Exception e) {
    e.printStackTrace();
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    JOptionPane.showMessageDialog(this, sw.toString(), "ok", JOptionPane.OK_OPTION);
  }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void update_prefs() {

  try {
    //if(prefs==null) prefs = Preferences.userRoot().node(this.getClass().getName()+"_"+sys_mac_id);
    System.out.println("sys_mac_id: "+sys_mac_id);
    if(prefs==null) prefs = Preferences.userRoot().node(sys_mac_id);

    if( !prefs.getBoolean("did_new_agc1", false) ) {
      prefs.putInt("agc_gain", 50);
      prefs.putBoolean("did_new_agc1", true);
    }
    //agc_gain.setValue(65);
    do_agc_update=1;

    if(prefs!=null) {
      int i = prefs.getInt("audio_buffer_system",1);

      try {
        if(dframe!=null) dframe.update_colors();
      } catch(Exception e) {
      }
      try {
        tglog_e = new tglog_editor(this);
      } catch(Exception e) {
      }

      en_evt_output.setSelected( prefs.getBoolean("en_evt_output", true) );
      auto_flash_tg.setSelected( prefs.getBoolean("tg_auto_flash", false) );
      auto_pop_table.setSelected( prefs.getBoolean("tg_auto_pop_table", true) );
      disable_encrypted.setSelected( prefs.getBoolean("enc_auto_flash", false) );
      autoscale_const.setSelected( prefs.getBoolean("autoscale_const", true) );
      nsymbols.setSelectedIndex( prefs.getInt("nsymbols", 0) );


      system_alias.setText( prefs.get("system_alias", "") );

      zipcode.setText( prefs.get("sys_zipcode", "99352") );
      city.setText( prefs.get("sys_city", "Richland") );
      state.setText( prefs.get("sys_state", "Wa") );

          try {
            String usbspeed = prefs.get("usb_speed", "slow");
            if(usbspeed.equals("slow")) usb_slow.setSelected(true);
            if(usbspeed.equals("med")) usb_med.setSelected(true);
            if(usbspeed.equals("fast")) usb_fast.setSelected(true);
          } catch(Exception e) {
            e.printStackTrace();
          }

      en_zero_rid.setSelected( prefs.getBoolean("en_zero_rid", true) );

      process_rid_alias.setSelected( prefs.getBoolean("process_rid_alias", true) );

      enable_mp3.setSelected( prefs.getBoolean("enable_mp3", true) ); 
      enable_audio.setSelected( prefs.getBoolean("enable_audio", true) ); 
      mp3_separate_files.setSelected( prefs.getBoolean("mp3_separate_files", false) );
      separate_rid.setSelected( prefs.getBoolean("separate_rid", false) );

      do_mp3.setSelected( prefs.getBoolean("do_mp3", true) );
      do_wav.setSelected( prefs.getBoolean("do_wav", false) );
      audio_hiq.setSelected( prefs.getBoolean("audio_hiq", false) );
      audio_lowq.setSelected( prefs.getBoolean("audio_lowq", true) );

      end_call_silence.setText( prefs.get("end_call_silence", "0") );

      int constellation = prefs.getInt("const_select", 1);
    }

      JFileChooser chooser = new JFileChooser();
      File file = chooser.getCurrentDirectory();  //better for windows to do it this way
      String fs =  System.getProperty("file.separator");
      String home_dir_str = file.getAbsolutePath()+fs;

      document_dir = home_dir_str+"p25rx";

      home_dir = prefs.get("p25rx_home_dir", home_dir_str+"p25rx");
      home_dir_label.setText(home_dir);
      System.out.println("home_dir: "+home_dir);

        try {
          Path path = Paths.get(new File(home_dir+fs+sys_mac_id).getAbsolutePath() );
          Files.createDirectories(path);
        } catch(Exception e) {
          e.printStackTrace();
        }

        try {
          Path path = Paths.get(new File(document_dir+fs+sys_mac_id).getAbsolutePath() );
          Files.createDirectories(path);
        } catch(Exception e) {
          e.printStackTrace();
        }

        try {
          Path path = Paths.get(new File(home_dir+fs+"broadcastify_calls").getAbsolutePath() );
          Files.createDirectories(path);
          broadcastify_calls_dir = path.toString();
        } catch(Exception e) {
          e.printStackTrace();
        }

      restore_position();

      tg_font_name = parent.prefs.get("tg_font_name", "Monospaced" );
      tg_font_style = parent.prefs.getInt("tg_font_style", Font.PLAIN );
      tg_font_size = parent.prefs.getInt("tg_font_size", 14 );
      log_ta.setFont(new java.awt.Font(tg_font_name, tg_font_style, tg_font_size)); 

      tg_font_color = new Color( parent.prefs.getInt("tg_font_color", new Color(255,255,255).getRGB() ) );
      log_ta.setForeground( tg_font_color ); 

      status_format_cc.setText( parent.prefs.get("status_format_cc", "CC $P25_MODE$ B/SEC $BLKS_SEC$  $WACN$-$SYS_ID$-$NAC$, $FREQ$ MHz") );
      status_format_voice.setText( parent.prefs.get("status_format_voice", "$P25_MODE$  $TG_NAME$ ($TG_ID$)  $RID_ALIAS$ [$RID$] $V_FREQ$ MHz") );

      if(prefs!=null) {
        String sicpu = prefs.get("si_cpu", "normal");
        if(sicpu.equals("normal")) si_cpu_normal.setSelected(true);
        if(sicpu.equals("high")) si_cpu_high.setSelected(true);
        if(sicpu.equals("low")) si_cpu_low.setSelected(true);
        if(sicpu.equals("battery")) si_cpu_battery_saving.setSelected(true);
        if(sicpu.equals("off")) si_cpu_off.setSelected(true);
      }

        try {
          en_rdio.setSelected( prefs.getBoolean("en_rdio", false) ); 
        } catch(Exception e) {
            e.printStackTrace();
        }

      dframe.update_prefs();

      z1.setToolTipText( prefs.get("zone1_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z2.setToolTipText( prefs.get("zone2_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z3.setToolTipText( prefs.get("zone3_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z4.setToolTipText( prefs.get("zone4_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z5.setToolTipText( prefs.get("zone5_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z6.setToolTipText( prefs.get("zone6_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z7.setToolTipText( prefs.get("zone7_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z8.setToolTipText( prefs.get("zone8_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z9.setToolTipText( prefs.get("zone9_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z10.setToolTipText( prefs.get("zone10_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z11.setToolTipText( prefs.get("zone11_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z12.setToolTipText( prefs.get("zone12_alias","Press Shift While Pressing Button To Set Zone Alias") );

  } catch(Exception e) {
    e.printStackTrace();
  }


      if(dvout==null) {
        dvout = new displayframe_popout(this, prefs);
        dvout.setTitle("Display View");
        dvout.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/dispview.png")).getImage()); // NOI18N
      }

      try {

        int x = prefs.getInt("dvout_form_x",50);
        int y = prefs.getInt("dvout_form_y",50);
        int width = prefs.getInt("dvout_form_width", 1200);
        int height = prefs.getInt("dvout_form_height",750+10+27);

        if(height < 100) height = 770;
        if(width < 100) width=800; 

        if(x > 3200) x = 50;
        if(y > 3200) y = 50;
          //setSize(1054,750);
        Rectangle r = new Rectangle(x,y,width,height);
        dvout.setBounds(r);

      } catch(Exception e) {
        e.printStackTrace();
      }

    try {
      en_broadcastify_calls.setSelected( prefs.getBoolean("en_bcalls", false) );
    } catch(Exception e) {
      e.printStackTrace();
    }

}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
String get_home_dir() {
  JFileChooser chooser = new JFileChooser();

  chooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);


  int returnVal = chooser.showDialog(parent, "Select Home Directory/Folder");

  if(returnVal == JFileChooser.APPROVE_OPTION) {

    String fs =  System.getProperty("file.separator");
    File file = chooser.getSelectedFile();  //better for windows to do it this way
    System.out.println("file:"+file);


    Path path = Paths.get(new File(home_dir+fs+sys_mac_id).getAbsolutePath() );
    try {
      Files.createDirectories(path);
    } catch(Exception e) {
      e.printStackTrace();
    }
    System.out.println("path:"+path);

    path = Paths.get(file.getAbsolutePath());
    home_dir_label.setText(path.toString());
    home_dir = home_dir_label.getText();
    prefs.put("p25rx_home_dir", home_dir); 

    alias = new Alias(parent, parent.sys_mac_id, document_dir);
  }

  return home_dir_label.getText();
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void save_position() {
  try {
    if(prefs==null) return;

    Rectangle r = getBounds();

    prefs.putInt("form_x", r.x);
    prefs.putInt("form_y", r.y);
    prefs.putInt("form_width", r.width);
    prefs.putInt("form_height", r.height);

    Boolean b = minimize.isSelected();
    prefs.putBoolean("form_min", b);

  } catch(Exception e) {
    e.printStackTrace();
  }
}
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void restore_position() {
  try {
    if(prefs==null) return;

    Boolean b = prefs.getBoolean("form_min", false);


    int x = prefs.getInt("form_x",50);
    int y = prefs.getInt("form_y",50);
    int width = prefs.getInt("form_width", 1200);
    int height = prefs.getInt("form_height",770);

    if(height < 100) height = 770;
    if(width < 800) width=1200; 

    if(x > 1600) x = 50;
    if(y > 1600) y = 50;

      //setSize(1054,750);
    Rectangle r = new Rectangle(x,y,width,height);
    setBounds(r);

    if(height==200) b = true;
    if(height==185) b = true;
    if(height>=750) b = false;

    minimize.setSelected(b);

  } catch(Exception e) {
    e.printStackTrace();
  }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
void toggle_recording(Boolean isrec) {

  boolean is_recording=!isrec;

  String recnow="";
  if(is_recording) recnow="1";  //toggle
  if(!is_recording) recnow="0"; //toggle

  //enable binary voice output for mp3 recording
  for(int i=0;i<99;i++) {
    serial_port.writeBytes( new String("en_voice_send "+recnow+"\r\n").getBytes(), 17, 0);
    try {
      SLEEP(50);
      if(serial_port.bytesAvailable()>29) break;
    } catch(Exception e) {
      //e.printStackTrace();
      e.printStackTrace();
    }
    byte[] b = new byte[32];
    int len = serial_port.readBytes(b, 30);
    if(len>0) {
      String s = new String(b);
      if(s.contains("en_voice_send "+recnow)) {
        is_recording = !isrec; 
        break;
      };
    }
  }

  if(is_recording) {
    record_to_mp3.setSelected(true);
    record_to_mp3.setBackground(java.awt.Color.red);
    record_to_mp3.setForeground(java.awt.Color.black);
  }
  else {
    record_to_mp3.setSelected(false);
    record_to_mp3.setBackground(java.awt.Color.white);
    record_to_mp3.setForeground(java.awt.Color.black);
  }
}
    
//SUMS 1
float[] columnWidthPercentage = {.075f, .10f, .075f, .075f, .25f, .325f, 0.1f, 0.05f };
private void resizeColumns() {
  // Use TableColumnModel.getTotalColumnWidth() if your table is included in a JScrollPane
  //int tW = jTable1.getWidth();
  int tW = jTable1.getColumnModel().getTotalColumnWidth();
  TableColumn column;
  TableColumnModel jTableColumnModel = jTable1.getColumnModel();
  int cantCols = jTableColumnModel.getColumnCount();
  for (int i = 0; i < cantCols; i++) {
    column = jTableColumnModel.getColumn(i);
    int pWidth = Math.round(columnWidthPercentage[i] * tW);
    column.setPreferredWidth(pWidth);
  }
}

//SUMS 1
float[] columnWidthPercentage2 = {0.08f, 0.2f, .05f, 0.12f, 0.05f, 0.05f, 0.058f, 0.1f, 0.1f, 0.035f, 0.13f };
private void resizeColumns2() {
  int tW = freq_table.getColumnModel().getTotalColumnWidth();
  TableColumn column;
  TableColumnModel jTableColumnModel = freq_table.getColumnModel();
  int cantCols = jTableColumnModel.getColumnCount();
  for (int i = 0; i < cantCols; i++) {
    column = jTableColumnModel.getColumn(i);
    int pWidth = Math.round(columnWidthPercentage2[i] * tW);
    column.setPreferredWidth(pWidth);
  }
}

//SUMS 1
float[] columnWidthPercentage3 = {0.1f, 0.9f};
private void resizeColumns3() {
  int tW = freq_table.getColumnModel().getTotalColumnWidth();
  TableColumn column;
  TableColumnModel jTableColumnModel = alias_table.getColumnModel();
  int cantCols = jTableColumnModel.getColumnCount();
  for (int i = 0; i < cantCols; i++) {
    column = jTableColumnModel.getColumn(i);
    int pWidth = Math.round(columnWidthPercentage3[i] * tW);
    column.setPreferredWidth(pWidth);
  }
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void SLEEP_US(long val) {
  try {

    long NS_PER_US = 1; 
    long DELAY_TARGET_MS;

    sleep_factor=100;

    if(is_linux==1) {
      DELAY_TARGET_MS = NS_PER_US*sleep_factor*(val); 
    }
    else if(is_windows==1) {
      DELAY_TARGET_MS = NS_PER_US*sleep_factor*(val); 
    }
    else if(is_mac_osx==1) {
      DELAY_TARGET_MS = NS_PER_US*sleep_factor*(val); 
    }
    else {
      DELAY_TARGET_MS = NS_PER_US*sleep_factor*(val); 
    }

     long t0 = System.nanoTime(); 
     while (System.nanoTime() < t0+DELAY_TARGET_MS) {
       try {
         Thread.sleep(0, 1000);
       } catch(Exception e) {
       }
     }

  } catch(Exception e) {
    e.printStackTrace();
  }
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void SLEEP(long val) {
  try {

    long NS_PER_US = 1000; 
    long DELAY_TARGET_MS;

    if(sleep_factor<100) sleep_factor=1000;
    if(is_mac_osx==1) sleep_factor=1000;

    if( usb_slow.isSelected() ) sleep_factor=1000;
    if( usb_med.isSelected() ) sleep_factor=600;
    if( usb_fast.isSelected() ) sleep_factor=200;

    if(is_linux==1) {
      DELAY_TARGET_MS = NS_PER_US*sleep_factor*(val); 
    }
    else if(is_windows==1) {
      DELAY_TARGET_MS = NS_PER_US*sleep_factor*(val); 
    }
    else if(is_mac_osx==1) {
      DELAY_TARGET_MS = NS_PER_US*sleep_factor*(val); 
    }
    else {
      DELAY_TARGET_MS = NS_PER_US*sleep_factor*(val); 
    }

     long t0 = System.nanoTime(); 
     while (System.nanoTime() < t0+DELAY_TARGET_MS) {
       try {
         Thread.sleep(0, 1000);
       } catch(Exception e) {
       }
     }

  } catch(Exception e) {
    e.printStackTrace();
  }
}


  ////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////
  public Boolean is_dup_crc(int crc_test) {
    for(int i=0;i<8;i++) {
      if(voice_crc[i]==crc_test) return true;
    }
    for(int i=0;i<8;i++) {
      if(btext_crc[i]==crc_test) return true;
    }
    return false;
  } 

  ////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////
  public void b_send_ack_crc(int crc) {
    if(true) return;

    byte[] out_buffer = new byte[48]; //size of bl_op
    ByteBuffer bb = ByteBuffer.wrap(out_buffer);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    //uint32_t magic;
    //uint32_t op;
    //uint32_t addr;
    //uint32_t len;
    //uint8_t  data[32]; 

    bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
    bb.putInt( (int) Long.parseLong("12", 10) ); //ack crc op
    bb.putInt( crc ); //crc
    bb.putInt( (int) Long.parseLong("0", 10) ); //len

    for(int i=0;i<32;i++) {
      bb.put((byte)0);
    }

    serial_port.writeBytes( out_buffer, 48, 0);
   }
  ////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////
  public void send_ack_crc(int crc) {
    if(true) return;

    byte[] out_buffer = new byte[48]; //size of bl_op
    ByteBuffer bb = ByteBuffer.wrap(out_buffer);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    //uint32_t magic;
    //uint32_t op;
    //uint32_t addr;
    //uint32_t len;
    //uint8_t  data[32]; 

    bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
    bb.putInt( (int) Long.parseLong("11", 10) ); //ack crc op
    bb.putInt( crc ); //crc
    bb.putInt( (int) Long.parseLong("0", 10) ); //len

    for(int i=0;i<32;i++) {
      bb.put((byte)0);
    }

    serial_port.writeBytes( out_buffer, 48, 0);
   }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(BTFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BTFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BTFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BTFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
              parent = new BTFrame(args);
              parent.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBox add_neighbors;
    public javax.swing.JCheckBox add_primary;
    private javax.swing.JButton add_rid;
    public javax.swing.JCheckBox add_secondaries;
    private javax.swing.JButton adv_write_config;
    private javax.swing.JPanel advancedpanel;
    private javax.swing.JPanel alias_panel;
    public javax.swing.JTable alias_table;
    public javax.swing.JCheckBox allow_tg_pri_int;
    public javax.swing.JCheckBox allow_unknown_tg_cb;
    public javax.swing.JButton append_cc;
    public javax.swing.JTextField audio_agc_max;
    public javax.swing.JList<String> audio_dev_list;
    public javax.swing.JRadioButton audio_hiq;
    public javax.swing.JRadioButton audio_lowq;
    public javax.swing.JProgressBar audio_prog;
    private javax.swing.JPanel audiopanel;
    public javax.swing.JCheckBox auto_flash_tg;
    public javax.swing.JCheckBox auto_pop_table;
    public javax.swing.JCheckBox autoscale_const;
    public javax.swing.JButton backup_roam;
    private javax.swing.JButton backup_tg;
    private javax.swing.JPanel bottom_panel;
    private javax.swing.JToggleButton bt_indicator;
    private javax.swing.JLabel bt_lb;
    public javax.swing.JSlider bt_vol_slider1;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup10;
    private javax.swing.ButtonGroup buttonGroup11;
    private javax.swing.ButtonGroup buttonGroup12;
    private javax.swing.ButtonGroup buttonGroup13;
    private javax.swing.ButtonGroup buttonGroup14;
    private javax.swing.ButtonGroup buttonGroup15;
    private javax.swing.ButtonGroup buttonGroup16;
    private javax.swing.ButtonGroup buttonGroup17;
    private javax.swing.ButtonGroup buttonGroup18;
    private javax.swing.ButtonGroup buttonGroup19;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup20;
    private javax.swing.ButtonGroup buttonGroup21;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    private javax.swing.ButtonGroup buttonGroup9;
    private javax.swing.JButton button_write_config;
    private javax.swing.JPanel buttong_config;
    private javax.swing.JButton check_firmware;
    public javax.swing.JTextField city;
    private javax.swing.JPanel consolePanel;
    private javax.swing.JPanel const_panel;
    public javax.swing.JRadioButton controlchannel;
    public javax.swing.JRadioButton conventionalchannel;
    private javax.swing.JButton delete_roaming;
    public javax.swing.JComboBox<String> demod;
    private javax.swing.JPanel desc_panel;
    public javax.swing.JCheckBox disable_encrypted;
    private javax.swing.JButton disable_table_rows;
    private javax.swing.JButton disconnect;
    private javax.swing.JButton discover;
    public javax.swing.JPanel display_frame;
    public javax.swing.JPanel displayviewmain_border;
    private javax.swing.JButton dmr_backup;
    public javax.swing.JCheckBox dmr_cc_en1;
    public javax.swing.JCheckBox dmr_cc_en10;
    public javax.swing.JCheckBox dmr_cc_en11;
    public javax.swing.JCheckBox dmr_cc_en12;
    public javax.swing.JCheckBox dmr_cc_en13;
    public javax.swing.JCheckBox dmr_cc_en14;
    public javax.swing.JCheckBox dmr_cc_en15;
    public javax.swing.JCheckBox dmr_cc_en2;
    public javax.swing.JCheckBox dmr_cc_en3;
    public javax.swing.JCheckBox dmr_cc_en4;
    public javax.swing.JCheckBox dmr_cc_en5;
    public javax.swing.JCheckBox dmr_cc_en6;
    public javax.swing.JCheckBox dmr_cc_en7;
    public javax.swing.JCheckBox dmr_cc_en8;
    public javax.swing.JCheckBox dmr_cc_en9;
    private javax.swing.JButton dmr_clear_freqs;
    public javax.swing.JRadioButton dmr_conplus;
    public javax.swing.JRadioButton dmr_conventional;
    public javax.swing.JLabel dmr_lcn1_label;
    private javax.swing.JButton dmr_restore;
    public javax.swing.JCheckBox dmr_slot1;
    public javax.swing.JCheckBox dmr_slot2;
    public javax.swing.JTextField dmr_sys_id;
    private javax.swing.JButton dmr_write_config;
    public javax.swing.JRadioButton do_mp3;
    public javax.swing.JRadioButton do_wav;
    public javax.swing.JRadioButton double_click_opt1;
    public javax.swing.JRadioButton double_click_opt2;
    public javax.swing.JRadioButton double_click_opt3;
    public javax.swing.JRadioButton double_click_opt4;
    public javax.swing.JRadioButton double_click_opt5;
    public javax.swing.JRadioButton double_click_opt6;
    private javax.swing.JButton dvpopout;
    public javax.swing.JButton edit_alias;
    public javax.swing.JButton edit_alias1;
    private javax.swing.JButton edit_display_view;
    public javax.swing.JCheckBox en_bluetooth_cb;
    public javax.swing.JCheckBox en_broadcastify_calls;
    public javax.swing.JCheckBox en_encout;
    private javax.swing.JCheckBox en_evt_output;
    public javax.swing.JCheckBox en_p2_tones;
    public javax.swing.JCheckBox en_rdio;
    public javax.swing.JCheckBox en_tg_int_tone;
    public javax.swing.JCheckBox en_zero_rid;
    public javax.swing.JCheckBox enable_audio;
    public javax.swing.JCheckBox enable_leds;
    public javax.swing.JCheckBox enable_mp3;
    private javax.swing.JButton enable_table_rows;
    public javax.swing.JTextField enc_count;
    public javax.swing.JCheckBox enc_mode;
    public javax.swing.JTextField enc_timeout;
    public javax.swing.JTextField end_call_silence;
    public javax.swing.JButton erase_roaming;
    private javax.swing.JButton freemem;
    public javax.swing.JLabel freq;
    private javax.swing.JLabel freq_ch_lb;
    public javax.swing.JLabel freq_label;
    private javax.swing.JButton freq_search;
    private javax.swing.JButton freq_search2;
    public javax.swing.JTable freq_table;
    private javax.swing.JPanel freqdb_panel;
    public javax.swing.JTextField frequency_tf1;
    public javax.swing.JLabel fw_installed;
    public javax.swing.JLabel fw_ver;
    private javax.swing.JButton gen_sys_info;
    private javax.swing.JButton hold;
    private javax.swing.JButton hold1;
    public javax.swing.JLabel home_dir_label;
    private javax.swing.JSlider if_slide;
    private javax.swing.JButton import_alias;
    private javax.swing.JButton import_csv;
    public javax.swing.JCheckBox inc_400mhz;
    public javax.swing.JCheckBox inc_700mhz;
    public javax.swing.JCheckBox inc_800mhz;
    public javax.swing.JCheckBox inc_900mhz;
    public javax.swing.JCheckBox inc_bus;
    public javax.swing.JCheckBox inc_dmr;
    public javax.swing.JCheckBox inc_dup_freq;
    public javax.swing.JCheckBox inc_gov;
    public javax.swing.JCheckBox inc_p25;
    public javax.swing.JCheckBox inc_trunked_only;
    public javax.swing.JCheckBox inc_vhf;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    public javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel46;
    private javax.swing.JPanel jPanel47;
    private javax.swing.JPanel jPanel48;
    private javax.swing.JPanel jPanel49;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel50;
    private javax.swing.JPanel jPanel51;
    private javax.swing.JPanel jPanel53;
    private javax.swing.JPanel jPanel54;
    private javax.swing.JPanel jPanel55;
    private javax.swing.JPanel jPanel56;
    private javax.swing.JPanel jPanel57;
    private javax.swing.JPanel jPanel58;
    private javax.swing.JPanel jPanel59;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel60;
    private javax.swing.JPanel jPanel61;
    private javax.swing.JPanel jPanel62;
    private javax.swing.JPanel jPanel63;
    private javax.swing.JPanel jPanel64;
    private javax.swing.JPanel jPanel65;
    private javax.swing.JPanel jPanel66;
    private javax.swing.JPanel jPanel67;
    private javax.swing.JPanel jPanel68;
    private javax.swing.JPanel jPanel69;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel70;
    private javax.swing.JPanel jPanel71;
    private javax.swing.JPanel jPanel72;
    private javax.swing.JPanel jPanel73;
    private javax.swing.JPanel jPanel74;
    private javax.swing.JPanel jPanel75;
    private javax.swing.JPanel jPanel76;
    private javax.swing.JPanel jPanel78;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    public javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private javax.swing.JSeparator jSeparator17;
    private javax.swing.JSeparator jSeparator18;
    private javax.swing.JSeparator jSeparator19;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator20;
    private javax.swing.JSeparator jSeparator21;
    private javax.swing.JSeparator jSeparator22;
    private javax.swing.JSeparator jSeparator23;
    private javax.swing.JSeparator jSeparator24;
    private javax.swing.JSeparator jSeparator25;
    private javax.swing.JSeparator jSeparator26;
    private javax.swing.JSeparator jSeparator27;
    private javax.swing.JSeparator jSeparator28;
    private javax.swing.JSeparator jSeparator29;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator30;
    private javax.swing.JSeparator jSeparator31;
    private javax.swing.JSeparator jSeparator32;
    private javax.swing.JSeparator jSeparator33;
    private javax.swing.JSeparator jSeparator34;
    private javax.swing.JSeparator jSeparator35;
    private javax.swing.JSeparator jSeparator36;
    private javax.swing.JSeparator jSeparator37;
    private javax.swing.JSeparator jSeparator38;
    private javax.swing.JSeparator jSeparator39;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator40;
    private javax.swing.JSeparator jSeparator41;
    private javax.swing.JSeparator jSeparator42;
    private javax.swing.JSeparator jSeparator43;
    private javax.swing.JSeparator jSeparator44;
    private javax.swing.JSeparator jSeparator45;
    private javax.swing.JSeparator jSeparator46;
    private javax.swing.JSeparator jSeparator47;
    private javax.swing.JSeparator jSeparator48;
    private javax.swing.JSeparator jSeparator49;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane1;
    public javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    public javax.swing.JTextField lcn10_freq;
    public javax.swing.JTextField lcn11_freq;
    public javax.swing.JTextField lcn12_freq;
    public javax.swing.JTextField lcn13_freq;
    public javax.swing.JTextField lcn14_freq;
    public javax.swing.JTextField lcn15_freq;
    public javax.swing.JTextField lcn1_freq;
    public javax.swing.JTextField lcn2_freq;
    public javax.swing.JTextField lcn3_freq;
    public javax.swing.JTextField lcn4_freq;
    public javax.swing.JTextField lcn5_freq;
    public javax.swing.JTextField lcn6_freq;
    public javax.swing.JTextField lcn7_freq;
    public javax.swing.JTextField lcn8_freq;
    public javax.swing.JTextField lcn9_freq;
    private javax.swing.JPanel level_panel;
    public javax.swing.JSlider lineout_vol_slider;
    private javax.swing.JTextArea log_ta;
    private javax.swing.JPanel logo_panel;
    public javax.swing.JPanel logpanel;
    public javax.swing.JLabel macid;
    private javax.swing.JPanel meter_panel;
    private javax.swing.JToggleButton minimize;
    public javax.swing.JComboBox<String> mixgain;
    private javax.swing.JButton monitor_sel;
    public javax.swing.JCheckBox mp3_separate_files;
    public javax.swing.JToggleButton mute;
    public javax.swing.JLabel nac;
    private javax.swing.JButton next_freq;
    public javax.swing.JPanel no_voice_panel;
    public javax.swing.JTextField no_voice_secs;
    public javax.swing.JComboBox<String> nsymbols;
    public javax.swing.JComboBox<String> op_mode;
    private javax.swing.JLabel os_string;
    public javax.swing.JTextField p1_sync_thresh;
    public javax.swing.JTextField p25_tone_vol;
    private javax.swing.JPanel p25rxconfigpanel;
    public javax.swing.JCheckBox p2_lsm;
    public javax.swing.JTextField p2_sync_thresh;
    private javax.swing.JButton pause_roaming;
    private javax.swing.JButton prev_freq;
    public javax.swing.JCheckBox process_rid_alias;
    private javax.swing.JProgressBar progbar;
    private javax.swing.JLabel progress_label;
    public javax.swing.JRadioButton quad_click_opt1;
    public javax.swing.JRadioButton quad_click_opt2;
    public javax.swing.JRadioButton quad_click_opt3;
    public javax.swing.JRadioButton quad_click_opt4;
    public javax.swing.JRadioButton quad_click_opt5;
    public javax.swing.JRadioButton quad_click_opt6;
    public javax.swing.JTextField rdio_mask;
    private javax.swing.JButton read_config;
    private javax.swing.JButton read_roaming;
    private javax.swing.JButton read_tg;
    private javax.swing.JToggleButton record_to_mp3;
    private javax.swing.JLabel release_date;
    private javax.swing.JButton reset_defaults;
    public javax.swing.JButton restore_roam;
    private javax.swing.JButton restore_tg;
    public javax.swing.JComboBox<String> rf_hyst;
    public javax.swing.JComboBox<String> rfgain;
    public javax.swing.JLabel rfid;
    public javax.swing.JCheckBox roaming;
    public javax.swing.JCheckBox roaming_ret_to_cc;
    private javax.swing.JButton save_iq;
    private javax.swing.JTextField search_radius;
    private javax.swing.JButton select_home;
    private javax.swing.JButton send_tg;
    public javax.swing.JCheckBox separate_rid;
    private javax.swing.JLabel ser_dev;
    private javax.swing.JButton set_zones;
    private javax.swing.JButton show_keywords;
    public javax.swing.JRadioButton si_cpu_battery_saving;
    public javax.swing.JRadioButton si_cpu_high;
    public javax.swing.JRadioButton si_cpu_low;
    public javax.swing.JRadioButton si_cpu_normal;
    public javax.swing.JRadioButton si_cpu_off;
    private javax.swing.JTextField sig_cmd;
    private javax.swing.JPanel signalinsightpanel;
    public javax.swing.JRadioButton single_click_opt1;
    public javax.swing.JRadioButton single_click_opt2;
    public javax.swing.JRadioButton single_click_opt3;
    public javax.swing.JRadioButton single_click_opt4;
    public javax.swing.JRadioButton single_click_opt5;
    public javax.swing.JRadioButton single_click_opt6;
    public javax.swing.JLabel siteid;
    private javax.swing.JButton skip;
    private javax.swing.JButton skip1;
    public javax.swing.JTextField skip_tg_to;
    public javax.swing.JToggleButton sq_indicator;
    private javax.swing.JLabel sq_lb;
    public javax.swing.JTextField state;
    private javax.swing.JLabel status;
    private javax.swing.JTextField status_format_cc;
    private javax.swing.JTextField status_format_voice;
    private javax.swing.JPanel status_panel;
    public javax.swing.JLabel sysid;
    public javax.swing.JTextField system_alias;
    private javax.swing.JPanel talkgroup_panel;
    public javax.swing.JButton testfreqs;
    private javax.swing.JButton tg_edit_del;
    private javax.swing.JToggleButton tg_indicator;
    private javax.swing.JLabel tg_lb;
    public javax.swing.JScrollPane tg_scroll_pane;
    private javax.swing.JPanel tgfontpanel;
    private javax.swing.JButton tglog_color;
    private javax.swing.JButton tglog_edit;
    private javax.swing.JButton tglog_font;
    private javax.swing.JPanel tiny_const;
    public javax.swing.JRadioButton triple_click_opt1;
    public javax.swing.JRadioButton triple_click_opt2;
    public javax.swing.JRadioButton triple_click_opt3;
    public javax.swing.JRadioButton triple_click_opt4;
    public javax.swing.JRadioButton triple_click_opt5;
    public javax.swing.JRadioButton triple_click_opt6;
    public javax.swing.JRadioButton usb_fast;
    public javax.swing.JRadioButton usb_med;
    public javax.swing.JRadioButton usb_slow;
    public javax.swing.JButton use_freq_primary;
    private javax.swing.JButton vga_cal;
    public javax.swing.JComboBox<String> vga_gain;
    public javax.swing.JTextField vga_step;
    public javax.swing.JTextField vga_target;
    public javax.swing.JLabel volume_label;
    public javax.swing.JLabel volume_label1;
    public javax.swing.JCheckBox vrep;
    public javax.swing.JComboBox<String> vtimeout;
    public javax.swing.JLabel wacn;
    private javax.swing.JButton write_config;
    public javax.swing.JToggleButton z1;
    public javax.swing.JToggleButton z10;
    public javax.swing.JToggleButton z11;
    public javax.swing.JToggleButton z12;
    public javax.swing.JToggleButton z2;
    public javax.swing.JToggleButton z3;
    public javax.swing.JToggleButton z4;
    public javax.swing.JToggleButton z5;
    public javax.swing.JToggleButton z6;
    public javax.swing.JToggleButton z7;
    public javax.swing.JToggleButton z8;
    public javax.swing.JToggleButton z9;
    public javax.swing.JTextField zipcode;
    // End of variables declaration//GEN-END:variables
}
