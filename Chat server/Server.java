import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Nahid
 */
public class Server extends JFrame {
	
	JTextField userText;
        public JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	private clientConection thred; 
        private int maxClient=100;
        private int threadIndex=0;
        Server root;
	//constructor
	public Server(){
                
		super("Awesome Messenger");
                root=this;
		userText = new JTextField();
		//userText.setEditable(false);
		userText.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){
				    thred=new clientConection(maxClient+1, null, null);
                                    String message=(String)event.getActionCommand();
                                    synchronized(this)
                                    {
                                        for(int i=0;i<maxClient;i++)
                                        {
                                            if(thred.thread[i]!=null)
                                            {
                                                try {
                                                    thred.thread[i].output.writeObject("<server> "+message);
                                                    thred.thread[i].output.flush();
                                                } catch (IOException ex) {
                                                    //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                                    thred.showMessage("\nError: Can not send message from server");
                                                }
                                            }
                                        }
                                    }
					userText.setText("");
				}
			}
		);
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
                chatWindow.setEditable(false);
		setSize(600, 450); //Sets the window size
		setVisible(true);
	}
//	public static void main(String[] args)
//        {
//            Server obj=new Server();
//            obj.startRunning();
//        }
	public void startRunning(){
		try{
			server = new ServerSocket(6789, 100); //6789 is a dummy port for testing, this can be changed. The 100 is the maximum people waiting to connect.
			while(true){
				try{
					//Trying to connect and have conversation
					waitForConnection();
				}catch(EOFException eofException){
					chatWindow.append("\n Server ended the connection! ");
				}
			}
		} catch (IOException ioException){
			ioException.printStackTrace();
		}
	}
	//wait for connection, then display connection information
	private void waitForConnection() throws IOException{
		//showMessage(" Waiting for someone to connect... \n");
		connection = server.accept();
		//showMessage(" Now connected to " + connection.getInetAddress().getHostName());
                if(maxClient>threadIndex)
                {
                    threadIndex++;
                    new clientConection(threadIndex-1,connection,root).start();
                    
                }
                else{
                  //  sorry server is busy
                }
	}
}
class clientConection extends Thread{
    
        ObjectOutputStream output;
	private ObjectInputStream input;
	//private ServerSocket server;
	private Socket connection;
        static clientConection[] thread=new clientConection[105];
        private Server root;
        private int threadIndx;
        private String id;
        private int Maxuser=100;

    public clientConection(int threadIndex,Socket con, Server root) {
        thread[threadIndex]=this;
        connection=con;
        this.root= root;
        threadIndx=threadIndex;
    }
    public void run()
    {
        while(true){
		 try{
        		setupStreams();
                        updateOnlineUser("@+"+id);
                        updateMyOnlineUser();
			whileChatting();
                       // closeConnection();
	  	}catch(EOFException eofException){
                	showMessage("\n Server ended the connection! ");
		} catch (IOException ex) { 
                  // Logger.getLogger(clientConection.class.getName()).log(Level.SEVERE, null, ex);
                   showMessage("\n Server ended the connection! ");
                } 
                finally{                                   
		        closeConnection(); //Changed the name to something more appropriate
                        break;
	       }
	}
    }
    //get stream to send and receive data
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		
		input = new ObjectInputStream(connection.getInputStream());
		
		//showMessage("\n Streams are now setup \n");
                showMessage("\nAsking for id??");
                id="";
                // this will auto process
                do{
			try{
				id = (String) input.readObject();
				//showMessage("\n" + message);
                                showMessage("\nUser Id: "+id);
			}catch(ClassNotFoundException classNotFoundException){
				showMessage("The user has sent an unknown object!");
			}
		}while(id.length()==0);
                showMessage("\n User: <"+id+"> is now connected");
                
	}
	
	//during the chat conversation
	private void whileChatting() throws IOException{
		String message = " You are now connected! ",receiver="";
		//sendMessage(message,"server");
		do{
			try{
				message = (String) input.readObject();
				//showMessage("\n" + message);
                                receiver="";
                                int i=0,j=0;
                                for(i=1,j=message.length();i<j;i++)
                                {
                                    if(message.charAt(i)=='>')
                                    {
                                        break;
                                    }                         
                                    receiver=receiver+message.charAt(i);
                                }
                                message=message.substring(i+1);
                                sendMessage(message, receiver);
			}catch(ClassNotFoundException classNotFoundException){
				showMessage("The user has sent an unknown object!");
			}
		}while(!message.equals("CLIENT - END"));
	}
	
	public void closeConnection(){
		showMessage("\nUser: <"+id+"> closing Connections... \n");
		try{
                       thread[threadIndx]=null;
                       updateOnlineUser("@-"+id);
			output.close(); //Closes the output path to the client
			input.close(); //Closes the input path to the server, from the client.
			connection.close(); //Closes the connection between you can the client
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	// who is here
        void updateOnlineUser(String OnUser)
        {
            synchronized(this)
            {
               for(int i=0;i<Maxuser;i++)
                {
                    if(thread[i]!=null)
                    {
                        try {
                            thread[i].output.writeObject(OnUser);
                            thread[i].output.flush();
                        } catch (IOException ex) {
                            showMessage("\nError: Can not update user list");
                        }
                    }
                }
                    
            }
        }
        void updateMyOnlineUser()
        {
            synchronized(this)
            {
               for(int i=0;i<Maxuser;i++)
                {
                    if(thread[i]!=null)
                    {
                        try {
                            output.writeObject("@+"+thread[i].id);
                            output.flush();
                        } catch (IOException ex) {
                            showMessage("\nError: Can not update user list");
                        }
                    }
                }
                    
            }
        }
	//Send a mesage to the client
	private void sendMessage(String message,String receiver){
            synchronized(this){
		
                int i=0;
                if(receiver.equals("server"))// for echo server
                {
                   showMessage("\n<"+id+"> "+message);
                   try{
                              output.writeObject("<server>" + message);
                              output.flush();
                             // showMessage("\n"+id+"-" + message);
                       }catch(IOException ioException){
                                    showMessage("\n ERROR: CANNOT SEND MESSAGE");
                       }
                }
                else
                for(i=0;i<Maxuser;i++)
                {
                    if(thread[i]!=null&&thread[i].id.equals(receiver))
                    {
                           try{
                              thread[i].output.writeObject("<"+id+">" + message);
                              thread[i].output.flush();
                             // showMessage("\n"+id+"-" + message);
                            }catch(IOException ioException){
                                    showMessage("\n ERROR: CANNOT SEND MESSAGE");
                            }
                        break;
                    }
                }
                if(i==Maxuser)
                {
                     showMessage("\n ERROR: CANNOT FIND USER");
                }
            }
	}
	
	//update chatWindow
	void showMessage(final String text){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
			         root.chatWindow.append(text);
                                // System.out.println(text);
				}
			}
		);
	}
}