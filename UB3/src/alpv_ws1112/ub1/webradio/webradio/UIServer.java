package alpv_ws1112.ub1.webradio.webradio;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListDataListener;

import org.jdesktop.application.SingleFrameApplication;

import alpv_ws1112.ub1.webradio.ui.ServerUI;
import alpv_ws1112.ub1.webradio.webradio.ServerImpl2.JukeBox;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
/**
 * 
 */
public class UIServer implements ServerUI{
    private JPanel topPanel;
    private JList songList;
    private JFileChooser jFileChooser1;
    private JButton jButton2;
    private JButton jButton1;
    private JButton jButton3;
    private JEditorPane list;
	private JukeBox jukebox;
	public JFrame jframe;

    public UIServer(JukeBox jukeBox2) {
		this.jukebox = jukeBox2;
	}

	protected void startup() {
		{
			jframe = new JFrame();
			jframe.setSize(513, 373);
		}
        topPanel = new JPanel();
        topPanel.setPreferredSize(new java.awt.Dimension(300, 350));
        {
        	jFileChooser1 = new JFileChooser();
        	jFileChooser1.setPreferredSize(new java.awt.Dimension(600, 400));
        	jFileChooser1.setMultiSelectionEnabled(true);
        	
        }
        {
        	//list of files
        	list = new JEditorPane("txt","");
        	topPanel.add(list);
        	list.setPreferredSize(new java.awt.Dimension(273, 301));
        	list.setBorder(new LineBorder(new java.awt.Color(0,0,0), 1, false));
        	list.setFocusable(false);
        	list.setText("staticx.wav\n");
        }
        
        {
        	//button to mute the stream
        	jButton2 = new JButton();
        	topPanel.add(jButton2);
        	jButton2.setText("end stream");
        	jButton2.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					if(jButton2.getText().equals("end stream")){
						jukebox.stop();
						jButton2.setText("start stream");
					}
					else{
						jukebox.start();
						jButton2.setText("end stream");
					}
					
				}
			});
        }
        {
        	//button to add a file
        	jButton1 = new JButton();
        	topPanel.add(jButton1);
        	jButton1.setText("add File");
        	jButton1.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					int returnVal = jFileChooser1.showOpenDialog(list);
			       	if(returnVal == jFileChooser1.APPROVE_OPTION){
			       		File[] files = jFileChooser1.getSelectedFiles();
			       		for(File f : files){
			       			list.setText(list.getText()+f.getName()+"\n");
			       			jukebox.addFile(f);
			       		}
			       	}
					
				}
			});
        }
        {
        	//button to play next song
        	jButton3 = new JButton();
        	topPanel.add(jButton3);
        	jButton3.setName("jButton3");
        	jButton3.setText("skip song");
        	jButton3.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					jukebox.nextSong();
				}
			});
        }

        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jframe.add(topPanel);
        jframe.pack();
        jframe.setVisible(true);
    }

	public void run() {
		startup();
	}
	
	//close
	public void shutdown(){
		jframe.setVisible(false);
		jukebox.close();
		
	}

}
