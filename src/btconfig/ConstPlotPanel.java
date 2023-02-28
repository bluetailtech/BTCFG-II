
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.nio.*;

public class ConstPlotPanel extends JPanel {

  double[] win160 = {
  -1.3877787807814457e-17, 0.0001406218328187242, 0.00056343689893088855, 0.0012712851196743924, 0.0022688704844587432, 0.0035627174386369115, 0.0051611102393743918, 0.0070740156892118988, 0.0093129897695918978, 0.011891068805869147, 0.01482264590055906, 0.018123333472120563, 0.021809812831784091, 0.025899671820209053, 0.030411231608524991, 0.035363363844038147, 0.040775299389091718, 0.046666429961814956, 0.053056104039388416, 0.059963418427666511, 0.067407006935238017, 0.075404827615059927, 0.083973950052490753, 0.093130344184774672, 0.10288867213373692, 0.11326208452065348, 0.12426202271002451, 0.13589802839744422, 0.14817756191610434, 0.16110583058694014, 0.17468562837933443, 0.18891718808298114,
   0.20379804711739433, 0.21932292802407771, 0.2354836345980561, 0.25226896452084907, 0.26966463925663758, 0.28765325186794216, 0.30621423329727177, 0.32532383754757432, 0.34495514607765349, 0.36507809160971832, 0.38565950142565963, 0.40666316010723874, 0.428049891553898, 0.44977765999110764, 0.47180168956281771, 0.49407460198441588, 0.51654657161835038, 0.53916549722397344, 0.56187718952687726, 0.58462557365172318, 0.60735290536690756, 0.62999999999999989, 0.65250647280027352, 0.6748109894493457, 0.69685152535344808, 0.71856563229156767, 0.73989071094301229, 0.76076428777620952, 0.78112429474797052, 0.80090935023930043, 0.82005903964023263, 0.83851419399220206,
   0.85621716510221979, 0.87311209555849734, 0.88914518210213733, 0.90426493084390036, 0.91842240285869003, 0.93157144874296605, 0.94366893078154745, 0.95467493143976245, 0.96455294697427729, 0.97327006504068414, 0.9807971252675457, 0.98710886186452806, 0.99218402743589085, 0.99600549727932275, 0.99856035356324213, 0.99983994889253114, 0.99983994889253114, 0.99856035356324213, 0.99600549727932275, 0.99218402743589085, 0.98710886186452818, 0.9807971252675457, 0.97327006504068425, 0.96455294697427729, 0.95467493143976245, 0.94366893078154745, 0.93157144874296616, 0.91842240285869026, 0.90426493084390058, 0.88914518210213755, 0.87311209555849745, 0.85621716510221979,
   0.83851419399220217, 0.82005903964023275, 0.80090935023930054, 0.78112429474797052, 0.76076428777620997, 0.73989071094301262, 0.718565632291568, 0.69685152535344863, 0.67481098944934581, 0.65250647280027385, 0.63000000000000023, 0.60735290536690789, 0.58462557365172374, 0.56187718952687749, 0.53916549722397378, 0.51654657161835082, 0.49407460198441622, 0.47180168956281821, 0.44977765999110775, 0.42804989155389833, 0.40666316010723913, 0.38565950142565997, 0.36507809160971877, 0.3449551460776536, 0.3253238375475741, 0.30621423329727154, 0.28765325186794205, 0.26966463925663758, 0.25226896452084918, 0.23548363459805619, 0.2193229280240776, 0.20379804711739424,
   0.18891718808298114, 0.17468562837933446, 0.16110583058694028, 0.14817756191610429, 0.13589802839744425, 0.12426202271002451, 0.11326208452065356, 0.10288867213373702, 0.093130344184774602, 0.083973950052490712, 0.075404827615059927, 0.067407006935238031, 0.059963418427666622, 0.053056104039388416, 0.046666429961814929, 0.040775299389091718, 0.035363363844038181, 0.030411231608525095, 0.025899671820209039, 0.021809812831784112, 0.018123333472120612, 0.014822645900559032, 0.011891068805869161, 0.0093129897695918978, 0.0070740156892118433, 0.0051611102393743918, 0.003562717438636967, 0.0022688704844587848, 0.0012712851196743646, 0.00056343689893088855, 0.0001406218328187242, -1.3877787807814457e-17
  };

    static int DATA_SIZE=0;

    int draw_mod_cnt;
   int draw_mod=0;

   static int[] plot_data;
    int[] scaled_data;
   int plot_idx=0;

   int xoff=0;
   int yoff=+96;
   double peak_mag=0.0;
   double peak_mag_i=0.0;
   double peak_mag_q=0.0;
   double scale=1.0;
   double scale_i=1.0;
   double scale_q=1.0;

   int vga_gain=0;
    int mixer_gain=-1; 

   int demod=0;
   int ch_flt=0;
   int freq_cc_lock=0;

   int gains_idx;
   static float[] gains = new float[256*3];

   int evms_idx;
   static float[] evms = new float[256*3];
   static float[] evms_ideal = new float[256*3];

   int sync_idx;
   static int[] syncs = new int[256*3];

   static String current_gain="";
   static String current_rf_gain="";
   static String err_hz="";
   static String est_hz="";
   static String sync_state="";
   static String ref_freq_est="";
   BTFrame parent;
   int last_sync_state=0;
   int data_init=0;
   float nco=0.0f;

   ByteBuffer bb;
   boolean do_synced=false;

   int est_ref_cnt=0;
   double[] est_ref_array = new double[2048];
   int est_ref_tot=0;
   double ref_freq_error=0.0;

   boolean do_display_ref_est=true;

   short[] audio_bytes;

   int paint_audio;
   int audio_frame_count;
   FastFourierTransform fft;

   double audio_in[];
   double audio_out[];
   boolean did_draw_audio_fft=true;
   float evm_db = -30.0f;
   float evm_percent = 0.0f; 
   String current_mixer_gain="";

   int repaint_amod=2;
   int repaint_vmod=4;

   float loop_freq;

   float ferr_alpha = 1/4.0f; 
   float ferr_beta = 1.0f - ferr_alpha; 
   float ferr_avg=0.0f;

   java.lang.Runtime rt;

   ///////////////////////////////////////////////////////////////////////////////////////
   ///////////////////////////////////////////////////////////////////////////////////////
   public ConstPlotPanel(BTFrame parent) {
     this.parent = parent;

     fft = new FastFourierTransform(256);
     audio_in = new double[256];
     audio_out = new double[256*2];

     rt = Runtime.getRuntime();

     plot_data = new int[8192*2];
     scaled_data = new int[ 8192*2 ];
     //DATA_SIZE = 36*2*4;
     //DATA_SIZE = 512;
     DATA_SIZE = 256;
     //DATA_SIZE = 128;

     paint_audio=0;

     audio_bytes = new short[160];

     for(int i=0;i<256*3;i++) {
       gains[i] = 1024.0f;
     }
     for(int i=0;i<256*3;i++) {
       syncs[i] = -1; 
     }
     for(int i=0;i<256*3;i++) {
       evms[i] = -30; 
       evms_ideal[i] = -30; 
     }
   }

   ///////////////////////////////////////////////////////////////////////////////////////
   ///////////////////////////////////////////////////////////////////////////////////////
   public void reset_ref_est() {
     est_ref_cnt=0;
     est_ref_array = new double[2048];
     est_ref_tot=0;
     ref_freq_error=0.0;
   }
   
   ///////////////////////////////////////////////////////////////////////////////////////
   ///////////////////////////////////////////////////////////////////////////////////////
   public void addAudio(byte[] pcm) {
     try {
       bb = ByteBuffer.wrap(pcm);
       bb.order(ByteOrder.LITTLE_ENDIAN);
       for(int i=0;i<256;i++) {
         if(i<160) {
           audio_bytes[i] = bb.getShort();
           //audio_in[i] = (double) audio_bytes[i] * win160[i];
           audio_in[i] = (double) audio_bytes[i];
         }
         else {
           audio_in[i] = 0.0;
         }
       }

       /*
       if(did_draw_audio_fft) {
         fft.applyReal( audio_in, 0, true, audio_out, 0);  //real in, complex out
         did_draw_audio_fft=false;
       }
       */

       paint_audio=15;
       audio_frame_count++;
       if(audio_frame_count%repaint_amod==0) repaint();
     } catch(Exception e) {
     }
   }
   ///////////////////////////////////////////////////////////////////////////////////////
   ///////////////////////////////////////////////////////////////////////////////////////
   public void addData( byte[] data , boolean do_synced) {
     int j=0;
     //System.out.println("add data");
     try {
       this.do_synced = do_synced;

       data_init=1;

       bb = ByteBuffer.wrap(data);
       bb.order(ByteOrder.LITTLE_ENDIAN);

       //DATA_SIZE = (int) java.lang.Math.pow( 2.0, parent.nsymbols.getSelectedIndex()+8 ); 

       for(int i=0;i<288/8;i++) {

         int ii = bb.getInt();
         int qq = bb.getInt();

         //System.out.println( String.format("%d, %d", ii, qq) );

         plot_data[plot_idx++] = ii;
         plot_data[plot_idx++] = qq;

         if(plot_idx>=DATA_SIZE) {
           plot_idx=0;
           //System.out.println("rollover");
         }
       }

       do_display_ref_est=false;

       /*
       if( parent.op_mode.getSelectedIndex()==1 && parent.dmr_conventional.isSelected()) {
       }
       */
       if( parent.op_mode.getSelectedIndex()==0 && parent.controlchannel.isSelected()) {
         do_display_ref_est=true;
       }


       
       bb = ByteBuffer.wrap(data);
       bb.order(ByteOrder.LITTLE_ENDIAN);

       float err_hz_f = bb.getFloat(300);
       float est_hz_f = bb.getFloat(304);
       int lna_gain = bb.getInt(308);
       float gain = bb.getFloat(312);
       nco = bb.getFloat(332);
       demod = bb.getInt(336);
       ch_flt = bb.getInt(340);
       freq_cc_lock = bb.getInt(344);
       loop_freq = bb.getFloat(348);


       //new values
       double cur_freq = bb.getDouble(352);
       byte rssi_b = bb.get(360);
       int c_tg = (int) (bb.getShort(361)&0xffff);

       byte[] desc_b = new byte[16];
       for(int i=0;i<16;i++) {
         desc_b[i] = bb.get(i+363);
       }

       float erate = bb.getFloat(379);

       byte on_control_b = bb.get(383);

       int cc_lcn_s = (int) (bb.getShort(384)&0xffff);

       byte mode_b = bb.get(386);
       byte slot_b = bb.get(387);

       int wacn = bb.getInt(388);
       int sysid = (int) (bb.getShort(392)&0xffff);

       //System.out.println("wacn: "+String.format("0x%05x", wacn));
       //system.out.println("sys_id: "+string.format("0x%03x", sysid));

       byte status_led_b = bb.get(394);

       int send_const_count = bb.getInt(395);

       byte is_roaming = bb.get(399);
       byte follow = bb.get(400);

       int dmr_tg = bb.getInt(401);

       int sync_count = bb.getInt(405);
       parent.global_sync_count = sync_count;


       int crc1 = bb.getInt(409);
       int crc = crc32.crc32_range(data, 409);

       //System.out.println("crc: "+crc+"  "+crc1);

       if(crc1!=crc) {
         System.out.println("dropping bad voice frame due to crc32");
         return; //drop it
       }


       if( parent.is_dmr_mode==1 ) c_tg = dmr_tg;


       parent.current_wacn_id = wacn;
       parent.current_sys_id = sysid;
       parent.send_const_count = send_const_count;

       try {
         parent.update_roaming(is_roaming);
       } catch(Exception e) {
       }

       parent.p25_follow = follow;
       try {
         parent.update_follow(follow);
       } catch(Exception e) {
       }

       parent.tdma_slot = slot_b;

       if(mode_b==0) {
         parent.is_phase1=1;
         parent.is_phase2=0;
         parent.is_dmr_mode=0;
       }
       if(mode_b==1) {
         parent.is_phase1=0;
         parent.is_phase2=1;
         parent.is_dmr_mode=0;
       }
       if(mode_b==2) {
         parent.is_phase1=0;
         parent.is_phase2=0;
         parent.is_dmr_mode=1;
       }

       parent.erate = erate;
       parent.on_control_freq = on_control_b;
       parent.current_talkgroup = String.format("%d", (int) c_tg); 
       parent.talkgroup_name = new String(desc_b);
       parent.current_freq = new Double(cur_freq).doubleValue()*1e6;

       if( parent.is_dmr_mode==1 && parent.src_uid!=0 ) {
          parent.handle_rid( parent.src_uid, c_tg);
       }

        try {
          String freq_str= String.format( "%3.6f", cur_freq); 
          parent.freq.setText("Freq: "+freq_str);
        } catch(Exception e) {
        }

       if(on_control_b==1) {
         parent.cc_freq = parent.current_freq/1e6;
         parent.cc_lcn = cc_lcn_s;
         parent.freqval = ""; 
       }
       else {
         parent.v_freq = parent.current_freq/1e6;
         parent.rf_channel = String.format("%d", cc_lcn_s);
         parent.freqval = String.format("3.6f", cur_freq);
       }

       parent.demod_type=demod;


       //keep these after all the above updates
       try {
         parent.update_rssi(rssi_b);
       } catch(Exception e) {
       }

       try {
         parent.update_tgid( (int) c_tg );
       } catch(Exception e) {
       }




       double ppb_est = 0.0; 
       double ppb2 = 0.0;
       if(parent.current_freq!=0.0) {
         ppb_est = est_hz_f / parent.current_freq;
         ppb_est *= 1e9;
         ppb2 = err_hz_f / parent.current_freq;
         ppb2 *= 1e9;
       }

       double rfreq = 0.0;
       int ref_correct = 0;
       double rfreq_cor=0.0;
       double est_ref_sum=0.0;

       //System.out.println("gain: "+java.lang.Math.log10(gain)*20.0f);
       current_gain = "AGC->target: "+String.format("%3.1f", java.lang.Math.log10(gain)*20.0f)+" dB";

       gains[gains_idx++] = (float) java.lang.Math.log10(gain)*20.0f;
       if(gains_idx==256*3) gains_idx=0;

       int synced = bb.getInt(316);
       if(synced>0) synced=1;
       sync_state = "sync state: "+synced;
       syncs[sync_idx++] = synced; 
       if(sync_idx==256*3) sync_idx=0;

       /*
        if( status_led_b==2) { 
          parent.sq_indicator.setForeground( java.awt.Color.green );
          parent.sq_indicator.setBackground( java.awt.Color.green );
        }
        else if( status_led_b==3) { 
          parent.sq_indicator.setForeground( java.awt.Color.blue );
          parent.sq_indicator.setBackground( java.awt.Color.blue );
        }
        else if( status_led_b==1) { 
          parent.sq_indicator.setForeground( java.awt.Color.red );
          parent.sq_indicator.setBackground( java.awt.Color.red );
        }
        else if( status_led_b==0) { 
          parent.sq_indicator.setForeground( java.awt.Color.black );
          parent.sq_indicator.setBackground( java.awt.Color.black );
        }
       */



       vga_gain = bb.getInt(320);
       evm_db = bb.getFloat(324);
       mixer_gain = bb.getInt(328);

       evms[evms_idx++] = evm_db;
       if(evms_idx==256*3) evms_idx=0;

       current_rf_gain = "LNA Gain: "+(lna_gain+1)*2+" dB";
       if(lna_gain==-1) current_rf_gain = "LNA GAIN: AUTO";

       if(mixer_gain==-1) current_mixer_gain = "MIXER GAIN: AUTO";
         else current_mixer_gain = "MIXER GAIN: "+(mixer_gain+1)*1.125+" dB";


       try {
         evm_percent = (float) java.lang.Math.pow( 10.0f, (evm_db/20.0f) ) * 100.0f;
         parent.current_evm_percent= evm_percent;
       } catch(Exception e) {
       }

       last_sync_state=synced;

       if(synced==1) {
          parent.sq_indicator.setForeground( java.awt.Color.green );
          parent.sq_indicator.setBackground( java.awt.Color.green );
          //parent.sq_indicator.invalidate();
       }
       else if(synced==-2) {
          parent.sq_indicator.setForeground( java.awt.Color.yellow );
          parent.sq_indicator.setBackground( java.awt.Color.yellow );
          //parent.sq_indicator.invalidate();
       }
       else if(synced==0) {
          parent.sq_indicator.setForeground( java.awt.Color.black );
          parent.sq_indicator.setBackground( java.awt.Color.black );
          //parent.sq_indicator.invalidate();
       }
       else {
          parent.sq_indicator.setForeground( java.awt.Color.black );
          parent.sq_indicator.setBackground( java.awt.Color.black );
          parent.sq_indicator.invalidate();
       }


       draw_mod_cnt++;
       if(draw_mod_cnt%repaint_vmod!=0) return; //not drawing this one 

       if(draw_mod_cnt%repaint_vmod==0) {
         invalidate();
         repaint();
         parent.jPanel24.invalidate();
         parent.jPanel24.repaint();
       }

     } catch(Exception e) {
       plot_idx=0;
       gains_idx=0;
       sync_idx=0;
       e.printStackTrace();
     }
   }
   
   ///////////////////////////////////////////////////////////////////////////////////////
   ///////////////////////////////////////////////////////////////////////////////////////
   public void paint(Graphics g){
     //super.paint(g);
     Graphics2D g2d = (Graphics2D) g;
     int text_xoff = 250+256;


     if( parent.si_cpu_high.isSelected()) {
       repaint_amod=1;
       repaint_vmod=1;
     }
     else if( parent.si_cpu_normal.isSelected()) {
       repaint_amod=2;
       repaint_vmod=4;
     }
     else if( parent.si_cpu_low.isSelected()) {
       repaint_amod=8;
       repaint_vmod=16;
     }
     else if( parent.si_cpu_battery_saving.isSelected()) {
       repaint_amod=32;
       repaint_vmod=64;
     }

     //Rectangle r = g2d.getClipBounds();
     Rectangle r = getBounds(); 
     g2d.setStroke( new BasicStroke(1.2f) );
      //clear to black
     g2d.setColor( Color.black ); 
     g2d.fill3DRect(r.x-250,r.y-250,r.width+500,r.height+500,false); 

       long total = rt.totalMemory();
       long free = rt.freeMemory();

       long total_mb = total / (1024*1024);
       long free_mb = free / (1024*1024);
       long used_mb = total_mb - free_mb; 

       g2d.setColor( Color.white ); 
       g2d.drawString( String.format("MEM: %d MB Used,   %d MB Free / %d MB Total Avail To Java", used_mb, free_mb, total_mb) , text_xoff,25 );

     if( parent.si_cpu_off.isSelected()) {
       return;
     }

     g2d.setColor( new java.awt.Color(128,128,128) ); 

     //+/-Q
     g2d.drawString("00", xoff+256-6,256-64-yoff-10);
     g2d.drawString("11", xoff+256-6,256+64-yoff+20);

     //+/-I
     g2d.drawString("01", xoff+128+64-20, 256-yoff+5);
     g2d.drawString("10", xoff+256+64+10, 256-yoff+5);

     g2d.setColor( Color.green ); 
     g2d.drawLine(xoff+128+64,256-yoff,xoff+256+64,256-yoff); 
     g2d.drawLine(xoff+256,256-64-yoff,xoff+256,256+64-yoff); 

     //draw ref circles
     g2d.setColor( new java.awt.Color(48,48,48) ); 
     g2d.drawOval(xoff+256-45,256-45-yoff,90,90); //peak 
     //g2d.drawOval(xoff+166,256-90-yoff,180,180);  //peak * 0.7


     //if( parent.off_const.isSelected() ) return;

     int j=0;
     int j2=0;
     for(int i=0;i<DATA_SIZE;i++) {

         int ii = (int) ((double) ((double) plot_data[j++]/4096.0f)*50.0f);
         int qq = (int) ((double) ((double) plot_data[j++]/4096.0f)*50.0f);


         scaled_data[j2++] = ii; 
         scaled_data[j2++] = qq; 
      }

     g2d.setColor( Color.white ); 
     g2d.drawString("I/Q Symbol Plot", 200,20);
     g2d.drawString("Pi / 4  Diff Constellation", 200,40);
     g2d.drawString("Alignment: On-Axis", 200,60);

     g2d.drawString("AGC->Target Gain", 10,350);
     g2d.drawString("Sync Status", 10,380);
     g2d.drawString("EVM", 10,410);

     g2d.setColor( new Color(96,96,96) ); 
     g2d.drawString("REF30", 30,420);

     //g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
     //draw x/y plot
     //g2d.setColor( new Color( Color.yellow.getRGB() | 0x10000000 , true) ); 

     g2d.setColor( Color.yellow ); 
     j=0;
     for(int i=0;i<DATA_SIZE/2;i++) {
       int ii = scaled_data[j++];
       int qq = scaled_data[j++];
       g2d.drawRoundRect(xoff+256+ii, 256+qq-yoff, 2, 2, 2, 2);
     }
     //g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

     int yoff2=-80;
     int xoff2=70;

     try {
       //draw agc gains
       g2d.setColor( Color.white ); 
       j=0;
       for(int i=0;i<256*3;i++) {
         g2d.drawRoundRect(i+xoff2, (int) (yoff2 + gains[j++]+445.0f ),1, 1, 1, 1);
         //System.out.println( "gain "+gains[j++]);
       }
      } catch(Exception e) {
      }
     
     
     j=0;
     for(int i=0;i<256*3;i++) {
       g2d.setColor( new Color(96,96,96) ); 
       g2d.drawRoundRect(i+xoff2, (int) (yoff2 + 525 + evms_ideal[i]),1, 1, 1, 1);

        float e_g = evms[i];
        if(e_g > -16.0f) {
           g2d.setColor( Color.red ); 
        }
        else if(e_g > -18.0f) {
           g2d.setColor( Color.yellow ); 
        }
        else { 
           g2d.setColor( Color.green ); 
        }
       g2d.drawRoundRect(i+xoff2, (int) (yoff2 + 525 + evms[i]),1, 1, 1, 1);

     }
     


     g2d.setColor( Color.white ); 
     g2d.drawString(current_gain, text_xoff,50);
     g2d.drawString(current_rf_gain, text_xoff,75);

     String current_vga_gain = ""; 
       current_vga_gain = "BB VGA Gain: "+String.format("%d", vga_gain*3)+" dB";

     if(vga_gain==-99) g2d.drawString("BB VGA Gain: Calibrating..." , text_xoff,125);
     else if(vga_gain>=0) g2d.drawString(current_vga_gain, text_xoff,125);
      else g2d.drawString("BB VGA Gain: AUTO", text_xoff,125);

     g2d.drawString(current_mixer_gain, text_xoff,100);

     try {
       //g2d.drawString(est_hz, text_xoff,125);
       //g2d.drawString(err_hz, text_xoff,150);
     } catch(Exception e) {
     }

     try {
        if( (int) ref_freq_error < 6 ) {
          g2d.setColor( Color.green ); 
        }
        else if( (int) ref_freq_error < 8 ) {
          g2d.setColor( Color.white ); 
        }
        else if( (int) ref_freq_error < 10 ) {
          g2d.setColor( Color.yellow ); 
        }
        else {
          g2d.setColor( Color.red ); 
        }

        if( do_display_ref_est )  {
          //if(est_ref_tot>2048 && ref_freq_est!=null )  g2d.drawString(ref_freq_est, text_xoff,175);
        }
     } catch(Exception e) {
     }

     //////////////////////////////////
     //audio plot
     //////////////////////////////////
     boolean b=true;
     if(paint_audio>0 || b) {

       int a_xoff = 850;
       int a_yoff = 75;

       g2d.setColor( Color.white ); 
       g2d.drawString("Audio Frame Count "+audio_frame_count, a_xoff+10, 125);
       //g2d.drawString("Audio FFT 0-4kHz", 860,375+a_yoff);

       int aval=0;
       int prev_aval=0;

       g2d.setColor( Color.green ); 
       for(int i=0;i<160-1;i++) {
         //g2d.drawLine(a_xoff++, audio_bytes[i]/200+132-50, a_xoff, audio_bytes[i+1]/200+132-50 );
         aval = audio_bytes[i]/200;
         if(i>1) {
           g2d.drawLine(a_xoff, prev_aval+132-50, a_xoff+1, aval+132-50 );
         }
         prev_aval=aval;
         a_xoff+=2;
       }

       /*

       a_xoff = 850;

       //draw grid 
       g2d.setColor( new Color(0.5f,0.5f,0.5f,0.3f ) ); 

       g2d.drawLine(a_xoff, 355+a_yoff, a_xoff, 350-251+a_yoff);
       g2d.drawLine(a_xoff+256, 355+a_yoff, a_xoff+256, 350-251+a_yoff);

       double g_ystep = 256.0/10.0; 
       double g_xstep = 256.0/10.0; 

       for(int i=0;i<11;i++) {
         g2d.drawLine(a_xoff, 99+a_yoff+(int)((double)i*g_ystep), a_xoff+256, 99+a_yoff+(int)((double)i*g_ystep));
       }
       for(int i=0;i<11;i++) {
         g2d.drawLine(a_xoff+(int)((double)i*g_xstep), 355+a_yoff, a_xoff+(int)((double)i*g_xstep), 350-251+a_yoff);
       }



       //draw audio fft 0-8 kHz
       g2d.setColor( Color.yellow ); 
       j=0;
       double prev_mag=350.0;

       if(paint_audio>12 && !did_draw_audio_fft) {



         for(int i=0;i<128-1;i++) {
           double ii = audio_out[j++];
           double qq = audio_out[j++];
           ii = ii*ii;
           qq = qq*qq;
           double mag = 40.0 * java.lang.Math.log10( java.lang.Math.pow(ii+qq, 0.5) );

           //if(i>0 && mag > 0 && prev_mag > 0 && mag < 300 && prev_mag < 300) {
             //g2d.drawLine(a_xoff++, 350-(int)prev_mag, a_xoff, 350-(int)mag);
           mag += 75;

           if(i>1 && mag>0 && prev_mag>0) {
             g2d.drawLine(a_xoff, 350-(int)prev_mag+a_yoff, a_xoff+1, 350-(int)mag+a_yoff);
           }
           prev_mag = mag;
           a_xoff+=2;
         }

         did_draw_audio_fft=true;
       }

       */
       paint_audio--;
       if(paint_audio==0) {
         audio_frame_count=0;
         for(int i=0;i<160;i++) {
           audio_bytes[i] = 0; 
         }
         did_draw_audio_fft=true;
       }
     }

     int sync_off=5;

     if(do_synced) {
       //draw sync status 
       g2d.setColor( Color.green ); 
       j=0;
       for(int i=0;i<256*3;i++) {
         if(syncs[j]==1) {
           g2d.setColor( Color.green ); 
           sync_off=5;
         }
         else if(syncs[j]==0)  {
           g2d.setColor( Color.red ); 
           sync_off=0;
         }
         else if(syncs[j]==-2)  {
           g2d.setColor( Color.yellow ); 
           sync_off=0;
         }
         else {
           g2d.setColor( Color.black ); 
           sync_off=0;
         }
         g2d.drawRoundRect(i+xoff2, (int) yoff2 + 470 - syncs[j++]*sync_off,1, 1, 1, 1);
       }

       if(last_sync_state==1) {
         g2d.setColor( Color.green ); 
         g2d.drawString(sync_state+" (Synced)", text_xoff,150);
       }
       else if(last_sync_state==0)  {
         g2d.setColor( Color.red ); 
         g2d.drawString(sync_state+" (No Sync)", text_xoff,150);
       }
       else if(last_sync_state==-2)  {
         g2d.setColor( Color.yellow ); 
         g2d.drawString(sync_state+" (TDU)", text_xoff,150);
       }
       else {
         g2d.setColor( Color.black ); 
         g2d.drawString(sync_state+" (No Signal)", text_xoff,150);
       }
     }

      if(evm_db > -16.0f) {
         g2d.setColor( Color.red ); 
      }
      else if(evm_db > -18.0f) {
         g2d.setColor( Color.yellow ); 
      }
      else { 
         g2d.setColor( Color.green ); 
      }


     if( data_init!=0 ) {
       g2d.drawString(String.format("Error Vector Mag (EVM): %02.0f dB", evm_db), text_xoff,175);
       g2d.drawString(String.format("Error Vector Mag (EVM): %01.0f", evm_percent)+" %", text_xoff,200);
       g2d.drawString(String.format("NCO: %01.3f", nco), text_xoff,225);

       String demod_str="";
       if(demod==0) demod_str="LSM (Simulcast)";
       if(demod==1) demod_str="CQPSK / C4FM";
       if(demod==3) demod_str="FMNB";
       g2d.drawString("DEMOD: "+demod_str , text_xoff,250);

       String ch_flt_str="";
       if(ch_flt==0) ch_flt_str="AUTO";
       if(ch_flt==1) ch_flt_str="8.7 kHz";
       if(ch_flt==2) ch_flt_str="15 kHz";
       if(ch_flt==3) ch_flt_str="10.4 kHz";
       g2d.drawString("Channel Filter: "+ch_flt_str , text_xoff,275);

       String freq_lock="";
       if(freq_cc_lock==0) {
         g2d.setColor( Color.yellow ); 
         freq_lock="NCO: UNLOCKED";
       }
       if(freq_cc_lock==1) {
         g2d.setColor( Color.green ); 
         freq_lock="NCO: LOCKED";
       }
       g2d.drawString(freq_lock , text_xoff+256,225);

       //float ferr_hz = (float) (loop_freq * (float) -7.7973e+03) + (float) 1.1271e+04 + 6000.0f;
       //ferr_hz /= 2.0f;
       //ferr_hz -= 14000.0f;
       //ferr_avg = ferr_avg * ferr_beta + ferr_hz * ferr_alpha;
       ferr_avg = ferr_avg * ferr_beta + loop_freq * ferr_alpha;
       g2d.drawString( String.format("Corr NCO Freq Error: %1.2f", ferr_avg) , text_xoff+256,250 );

     }



   }

}
