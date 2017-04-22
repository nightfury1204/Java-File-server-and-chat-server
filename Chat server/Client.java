import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.text.StyledDocument;

public class Client extends JFrame{
	
	private JTextField userText;
	private JTextPane chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
        private String myid;
        private String[] userid=new String[]{"server"};
        private JComboBox<String> userList= new JComboBox<>(userid);
        private StringBuilder buildformat = new StringBuilder();
	
	//constructor
	public Client(String host,String id){
		super("User_id: "+id);
                this.setLayout(null);
                userText = new JTextField();
                chatWindow = new JTextPane();//chatWindow.setPreferredSize(new Dimension(150, (int) chatWindow.getSize().getHeight()));
                JScrollPane chatS=new JScrollPane(chatWindow);
                chatWindow.setEditable(false);
                chatWindow.setContentType("text/html");
                JLabel label = new JLabel("Who's Here",JLabel.CENTER);
                ((JLabel)userList.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
               // int defaultheight=(int) userText.getSize().getHeight();
                //userText.setSize(new Dimension(50,100)); 
                getContentPane().add(userText);
                userText.setBounds(15,15,450,50);
                getContentPane().add(chatS);
                chatS.setBounds(15,75,450,400);
                getContentPane().add(label);
                label.setBounds(490,15,100,50);
                getContentPane().add(userList);
                userList.setBounds(490,75,100,40);
                myid=id;
		serverIP = host;
                userText.setEditable(false);
		userText.addActionListener(
				new ActionListener(){
				public void actionPerformed(ActionEvent event){
					sendMessage(event.getActionCommand());
					userText.setText("");
				}
			}
		);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
		setSize(630, 550); //Sets the window size
		setVisible(true);
                this.setResizable(false);
	}
	
	//connect to server
	public void startRunning(){
		try{
			connectToServer();
			setupStreams();
			whileChatting();
		}catch(EOFException eofException){
			showMessage("\n Client terminated the connection","red",1);
		}catch(IOException ioException){
			ioException.printStackTrace();
		}finally{
			closeConnection();
		}
	}
	
	//connect to server
	private void connectToServer() throws IOException{
		//showMessage("Attempting connection... \n");
		connection = new Socket(InetAddress.getByName(serverIP), 6789);
		showMessage("Connection Established!","green",1);
	}
	
	//set up streams
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		//showMessage("\n The streams are now set up! \n");
                output.writeObject(myid);
                output.flush();
	}
	
	//while chatting with server
	private void whileChatting() throws IOException{
		ableToType(true);
		do{
			try{
				message = (String) input.readObject();
                                if(message.charAt(0)=='@')
                                {
                                    if(message.charAt(1)=='+')
                                    {
                                        message=message.substring(2);
                                        if(!message.equals(myid))
                                        userList.addItem(message);
                                    }
                                    else
                                    {
                                        message=message.substring(2);
                                        if(!message.equals(myid))
                                        userList.removeItem(message);
                                    }
                                }
                                else
                                {
                                    int i=0,j=message.length(),flag=0;
                                    String prefix="";
                                    for(i=0;i<j;i++)
                                    {
                                         if(message.charAt(i)=='>')
                                        {
                                            prefix=prefix;//+" to me:- ";
                                            break;
                                        }
                                         if(flag==1)
                                        {
                                            prefix+=message.charAt(i);
                                        }
                                        if(message.charAt(i)=='<')
                                        {
                                            flag=1;
                                        }
                                       
                                    }
                                    message=message.substring(i+1);
                                    showMessage("\n"+message,prefix,2);
                                }
			}catch(ClassNotFoundException classNotFoundException){
				showMessage("\nUnknown data received!","red",1);
			}
		}while(!message.equals("SERVER - END"));	
	}
	
	//Close connection
	private void closeConnection(){
		showMessage("\n Closing the connection!","red",1);
		ableToType(false);
		try{
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//send message to server
	private void sendMessage(String message){
		try{
                       String reciver=(String)userList.getSelectedItem();
			output.writeObject("<"+reciver+">"+message);
			output.flush();
			showMessage("\n"+message,reciver,3);
		}catch(IOException ioException){
			showMessage("\n Oops! Something went wrong!","red",1);
		}
	}
	
	//update chat window
	private void showMessage(final String message,String col,int type){
            //type=1,system; type=2,to me; type=3,from me;
              String pers="";
              if(type==2){
                    pers="<p style=\"font-size:15px;color: #FF9009;\">"+col+" to me </p>";
                }
              if(type==3){
                  pers="<p style=\"font-size:15px;color: #118C4E;\">me to "+col+"</p>";
              }
                if(type>1)
                    col = "#000000";
                final String color=col;
                final String person=pers;
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
                                        buildformat.append("<html><br><div style=\"color:"+color+";padding:5px;border: 2px solid #4080FF;\">" +person+ message + "</div><html>");
					chatWindow.setText(buildformat.toString());
				}
			}
		);
	}
	
	//allows user to type
	private void ableToType(final boolean tof){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					userText.setEditable(tof);
				}
			}
		);
	}
        //append in Textpane
        
}