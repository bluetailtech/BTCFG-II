
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
import javax.swing.JColorChooser;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.Color;
import java.awt.Font;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;

import java.io.*;
/**
 *
 * @author radioactive
 */
public class tglog_editor extends javax.swing.JFrame {
BTFrame parent;
JFileChooser chooser;
    /**
     * Creates new form tglog_editor
     */
    public tglog_editor(BTFrame p) {
      initComponents();
      parent = p;
      chooser = new JFileChooser();

      //would be better defaults
      //$DATE$ $TIME$, $TG_NAME$, TG_$TG_ID$, RID $RID$, $RID_ALIAS$, RSSI $RSSI$, VFREQ $V_FREQ$, CCFREQ $CC_FREQ$, SYS $WACN$-$SYS_ID$, NAC $NAC$, SITE $SITE_ID$, RFSS $RFSS_ID$, ERR_RATE $ERR_RATE$

      if( parent.prefs!=null) {
        log_format.setText( parent.prefs.get("tglog_format", "$P25_MODE$ $V_FREQ$ MHz,  TG $TG_ID$ ,  $TG_NAME$,$DATE$ $TIME$, $RSSI$ dbm,  cc_freq $CC_FREQ$ mhz, RID $RID$, $P25_MODE$, EVM  $EVM_P$%, ") );
      }
      if( parent.prefs!=null) {
        tg_trig_vgrant.setSelected(parent.prefs.getBoolean("tg_trig_vgrant", false)); 
      }
      if( parent.prefs!=null) {
        tg_trig_vaudio.setSelected(parent.prefs.getBoolean("tg_trig_vaudio_v2", true)); 
      }
      if( parent.prefs!=null) {
        tg_trig_nzrid.setSelected(parent.prefs.getBoolean("tg_trig_nzrid", false)); 
      }
      if( parent.prefs!=null) {
        tg_trig_anyrid.setSelected(parent.prefs.getBoolean("tg_trig_anyrid", true)); 
      }
      if( parent.prefs!=null) {
        tg_trig_enc.setSelected(parent.prefs.getBoolean("tg_trig_enc", true)); 
      }
    }

    public String getFormat() {
      return log_format.getText();
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
        jPanel1 = new javax.swing.JPanel();
        log_format = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        tg_trig_vgrant = new javax.swing.JCheckBox();
        tg_trig_vaudio = new javax.swing.JCheckBox();
        tg_trig_nzrid = new javax.swing.JCheckBox();
        tg_trig_anyrid = new javax.swing.JCheckBox();
        tg_trig_enc = new javax.swing.JCheckBox();
        te_import = new javax.swing.JButton();
        te_export = new javax.swing.JButton();
        help = new javax.swing.JButton();
        reset = new javax.swing.JButton();
        close = new javax.swing.JButton();
        save = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();

        setTitle("TG Log Editor");

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        log_format.setColumns(200);
        log_format.setText("$P25_MODE$ $V_FREQ$ MHz,  TG $TG_ID$ ,  $TG_NAME$,$DATE$ $TIME$, $RSSI$ dbm,  cc_freq $CC_FREQ$ mhz, RID $RID$, $P25_MODE$, EVM  $EVM_P$%, ");
        log_format.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                log_formatActionPerformed(evt);
            }
        });
        jPanel1.add(log_format);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Log Trigger"));

        tg_trig_vgrant.setSelected(true);
        tg_trig_vgrant.setText("V Grant");
        tg_trig_vgrant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tg_trig_vgrantActionPerformed(evt);
            }
        });
        jPanel4.add(tg_trig_vgrant);

        tg_trig_vaudio.setSelected(true);
        tg_trig_vaudio.setText("V Audio");
        tg_trig_vaudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tg_trig_vaudioActionPerformed(evt);
            }
        });
        jPanel4.add(tg_trig_vaudio);

        tg_trig_nzrid.setSelected(true);
        tg_trig_nzrid.setText("Non-Zero RID");
        tg_trig_nzrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tg_trig_nzridActionPerformed(evt);
            }
        });
        jPanel4.add(tg_trig_nzrid);

        tg_trig_anyrid.setSelected(true);
        tg_trig_anyrid.setText("Any RID");
        tg_trig_anyrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tg_trig_anyridActionPerformed(evt);
            }
        });
        jPanel4.add(tg_trig_anyrid);

        tg_trig_enc.setSelected(true);
        tg_trig_enc.setText("Encrypted V");
        tg_trig_enc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tg_trig_encActionPerformed(evt);
            }
        });
        jPanel4.add(tg_trig_enc);

        jPanel2.add(jPanel4);

        te_import.setText("Import");
        te_import.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                te_importActionPerformed(evt);
            }
        });
        jPanel2.add(te_import);

        te_export.setText("Export");
        te_export.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                te_exportActionPerformed(evt);
            }
        });
        jPanel2.add(te_export);

        help.setText("Show Keywords");
        help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });
        jPanel2.add(help);

        reset.setText("Reset To Defaults");
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetActionPerformed(evt);
            }
        });
        jPanel2.add(reset);

        close.setText("Close");
        close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeActionPerformed(evt);
            }
        });
        jPanel2.add(close);

        save.setText("Save");
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });
        jPanel2.add(save);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jLabel2.setText("Log File Output Format");
        jPanel3.add(jLabel2);

        getContentPane().add(jPanel3, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeActionPerformed
      setVisible(false);
    }//GEN-LAST:event_closeActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
      if( parent.prefs!=null) {
        parent.prefs.put("tglog_format", getFormat());
      }
    }//GEN-LAST:event_saveActionPerformed

    private void helpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpActionPerformed
      if(parent.dframe!=null) parent.dframe.show_help();
    }//GEN-LAST:event_helpActionPerformed

    private void log_formatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_log_formatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_log_formatActionPerformed

    private void resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetActionPerformed
        log_format.setText("$P25_MODE$ $V_FREQ$ MHz,  TG $TG_ID$ ,  $TG_NAME$,$DATE$ $TIME$, $RSSI$ dbm,  cc_freq $CC_FREQ$ mhz, RID $RID$, $P25_MODE$, EVM  $EVM_P$%, ");
    }//GEN-LAST:event_resetActionPerformed

    private void te_importActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_te_importActionPerformed
      try {

        FileNameExtensionFilter filter = new FileNameExtensionFilter( "TG log format file", "fmt");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showDialog(parent, "Import TG log fomrat file (.fmt) file");


        if(returnVal == JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          FileInputStream fis = new FileInputStream(file);
          ObjectInputStream ois = new ObjectInputStream(fis);
          log_format.setText( ois.readUTF() );

          if( parent.prefs!=null) {
            parent.prefs.put("tglog_format", getFormat());
          }
          parent.setStatus("TG log format imported.");

        }

      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_te_importActionPerformed

    private void te_exportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_te_exportActionPerformed
      try {

        FileNameExtensionFilter filter = new FileNameExtensionFilter( "tg format file", "fmt");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showDialog(parent, "Export TG Format Export .fmt file");

        ObjectOutputStream oos;

        if(returnVal == JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          oos = new ObjectOutputStream( new FileOutputStream(file) );

          oos.writeUTF(log_format.getText());

          oos.flush();
          oos.close();

          parent.setStatus("TG log format exported.");

        }

      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_te_exportActionPerformed

    private void tg_trig_vgrantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tg_trig_vgrantActionPerformed
      if( parent.prefs!=null) {
        parent.prefs.putBoolean("tg_trig_vgrant", tg_trig_vgrant.isSelected());
      }
    }//GEN-LAST:event_tg_trig_vgrantActionPerformed

    private void tg_trig_vaudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tg_trig_vaudioActionPerformed
      if( parent.prefs!=null) {
        parent.prefs.putBoolean("tg_trig_vaudio_v2", tg_trig_vaudio.isSelected());
      }
    }//GEN-LAST:event_tg_trig_vaudioActionPerformed

    private void tg_trig_nzridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tg_trig_nzridActionPerformed
      if( parent.prefs!=null) {
        parent.prefs.putBoolean("tg_trig_nzrid", tg_trig_nzrid.isSelected());
      }
    }//GEN-LAST:event_tg_trig_nzridActionPerformed

    private void tg_trig_anyridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tg_trig_anyridActionPerformed
      if( parent.prefs!=null) {
        parent.prefs.putBoolean("tg_trig_anyrid", tg_trig_anyrid.isSelected());
      }
    }//GEN-LAST:event_tg_trig_anyridActionPerformed

    private void tg_trig_encActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tg_trig_encActionPerformed
      if( parent.prefs!=null) {
        parent.prefs.putBoolean("tg_trig_enc", tg_trig_enc.isSelected());
      }
    }//GEN-LAST:event_tg_trig_encActionPerformed

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
            java.util.logging.Logger.getLogger(tglog_editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(tglog_editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(tglog_editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(tglog_editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new tglog_editor().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton close;
    private javax.swing.JButton help;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField log_format;
    private javax.swing.JButton reset;
    private javax.swing.JButton save;
    private javax.swing.JButton te_export;
    private javax.swing.JButton te_import;
    public javax.swing.JCheckBox tg_trig_anyrid;
    public javax.swing.JCheckBox tg_trig_enc;
    public javax.swing.JCheckBox tg_trig_nzrid;
    public javax.swing.JCheckBox tg_trig_vaudio;
    public javax.swing.JCheckBox tg_trig_vgrant;
    // End of variables declaration//GEN-END:variables
}
