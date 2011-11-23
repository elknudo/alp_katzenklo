package alpv_ws1112.ub1.webradio.webradio;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.jdesktop.application.SingleFrameApplication;

import alpv_ws1112.ub1.webradio.proto.PacketProtos.Message.Chat;
import alpv_ws1112.ub1.webradio.ui.ClientUI;


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
public class UIClient implements ClientUI,Runnable  {
    private JPanel topPanel;
    private JEditorPane chatmessages;
    private JButton sendChatMessage;
    private JTextField chatMessageField;
    private String username;
    private JButton muteButton;
    private ClientImpl2 client;
    public  JFrame jframe;
    
    public UIClient(String username2, ClientImpl2 clientImpl2) {
		this.username=username2;
		this.client = clientImpl2;
	}

    //ActionListener to send chat Messages
    private class SendActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				client.sendChatMessage(chatMessageField.getText());
			} catch (IOException e) {
				
			}
			chatMessageField.setText("");
		}
	}
    
    //initialize GUI
	protected void startup() {
        System.out.println("starting");jframe = new JFrame();
		topPanel = new JPanel();
        topPanel.setPreferredSize(new java.awt.Dimension(600, 450));
        {
        	//here will the messages be displayed
        	chatmessages = new JEditorPane();
        	topPanel.add(chatmessages);
        	chatmessages.setName("chatmessages");
        	chatmessages.setPreferredSize(new java.awt.Dimension(454, 234));
        	chatmessages.setFocusable(false);
        	chatmessages.setBorder(new LineBorder(new java.awt.Color(0,0,0), 1, false));
        }
        {
        	//here you will be able to enter messages; enter to send
        	chatMessageField = new JTextField();
        	topPanel.add(chatMessageField);
        	chatMessageField.setName("chatMessageField");
        	chatMessageField.setPreferredSize(new java.awt.Dimension(305, 25));
        	chatMessageField.addActionListener(new SendActionListener());
        }
        {
        	//button to send
        	sendChatMessage = new JButton();
        	topPanel.add(sendChatMessage);
        	sendChatMessage.setName("sendChatMessage");
        	sendChatMessage.setText("Send");
        	sendChatMessage.addActionListener(new SendActionListener());
        }
        {
        	//mute music
        	muteButton = new JButton();
        	topPanel.add(muteButton);
        	muteButton.setName("muteButton");
        	muteButton.setText("mute");
        	muteButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					client.mute();
					if(muteButton.getText().equals("mute"))
						muteButton.setText("unmute");
					else
						muteButton.setText("mute");
				}
			});
        }
        try{
        	jframe.add(topPanel);
        	jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        	jframe.setPreferredSize(new Dimension(500, 400));
        	jframe.pack();
        	jframe.setVisible(true);
        	
        }catch (Exception e){
        
        }
        
    }

 
    //not needed but had to be implemented
	public String getUserName() {
		return username;
	}

	//write message to the chatmessage field
	public void pushChatMessage(String message) {
		chatmessages.setText(chatmessages.getText()+message+"\n");
	}

	public void run() {
		try{
			startup();
		}catch(Exception e){
			
		}
	}

	//print messages from Chat
	public void pushChat(Chat c) {
		for(int i = 0;i< c.getMessageCount();i++){
			pushChatMessage(c.getUsername(i) + " : " + c.getMessage(i));
		}
	}
	
	
}
