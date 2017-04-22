import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

public class FileServer extends JFrame{
	
	private ServerSocket server_soc;
	private String Directory="E:\\fileServer";
        JTextArea chatWindow;
        JList fileData;
        JList userData;
        DefaultListModel fileDataModel = new DefaultListModel();
        DefaultListModel userDataModel = new DefaultListModel();
	public FileServer() {
             super("File Server");
	     
             chatWindow=new JTextArea();
             JScrollPane scroll_pane=new JScrollPane(chatWindow);
             //chatWindow.setEditable(false);
             fileData=new JList(fileDataModel);
             //fileData.setBorder(new EmptyBorder(10,10, 10, 10));
             fileData.setFixedCellHeight(30);
             userData=new JList(userDataModel);
             userData.setFixedCellHeight(30);
             JPanel root_pane=new JPanel();
             JPanel pane1=new JPanel();
             JPanel pane2=new JPanel();
             JPanel pane3=new JPanel();
             pane1.setPreferredSize(new Dimension(250,pane1.getSize().height));
             pane3.setPreferredSize(new Dimension(170,pane1.getSize().height));
            // GridLayout rootL=new GridLayout(0,3,10,10);
             BorderLayout rootL=new BorderLayout(10,10);
             root_pane.setLayout(rootL);
            /// chatWindow.setPreferredSize(new Dimension(50,500));
             BorderLayout bL1=new BorderLayout(5,5);
             BorderLayout bL2=new BorderLayout(5,5);
             BorderLayout bL3=new BorderLayout(5,5);
             pane1.setLayout(bL1);
             pane2.setLayout(bL2);
             pane3.setLayout(bL3);
             pane2.add(new JLabel("Activity Log",JLabel.CENTER),bL2.NORTH);
             pane2.add(scroll_pane,bL2.CENTER);
             pane1.add(new JLabel("File List",JLabel.CENTER),bL1.NORTH);
             pane1.add(new JScrollPane(fileData),bL1.CENTER);
             pane3.add(new JLabel("User List",JLabel.CENTER),bL3.NORTH);
             pane3.add(new JScrollPane(userData),bL3.CENTER);
             root_pane.add(pane1,rootL.EAST);
             root_pane.add(pane2,rootL.CENTER);
             root_pane.add(pane3,rootL.WEST);
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
             this.add(root_pane);
             this.setSize(800, 600);
             this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             this.setVisible(true);
             this.setResizable(false);
             
        }
        void initServer(int port)
        {
            try {
                        File dir=new File(Directory);
                        if(dir.exists()==false)
                        {
                            if(dir.mkdirs()==false)
                            {
                                System.out.println("can not create directory");
                            }
                        }
			server_soc = new ServerSocket(port,100);
		} catch (IOException e) {
			e.printStackTrace();
		}
        }
        void updateFileData()
       {
           File dir_file=new File(Directory);
           String[] filelist=dir_file.list();
           int total=filelist.length;
           for(int i=0;i<total;i++)
           {
              fileDataModel.addElement(filelist[i]);
           }
       }
	public void server_run(int port) {
                initServer(port);
                updateFileData();
                int i=0;
		while (true) {
			try {
                                i++;
				Socket clientSock = server_soc.accept();				
                                if(i<100){
                                    FileWork obj=new FileWork(clientSock, i, this, Directory, server_soc);                                
                                     obj.start();
                                }
                                else{
                                    //sorry server is busy;
                                }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class FileWork extends Thread{
       
    private Socket clientSock;
    private Socket clientSock1;
    private FileServer root;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    DataInputStream d_input;
    DataOutputStream dos;
    static  FileWork[] thread=new FileWork[105];
    private int threadIdx;
    private String id;
    private FileWork oppsite;
    private int flag=-1;
    String homeDirectory;
    public FileWork(Socket con,int indx,FileServer obj,String home_dir, ServerSocket server_soc)
    {
        try {
            threadIdx=indx;
            thread[indx]=this;
            clientSock=con;
            root=obj;
            homeDirectory=home_dir;
            clientSock1= server_soc.accept();
            System.out.println("ttt");
        } catch (IOException ex) {
            Logger.getLogger(FileWork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void run()
    {
         while(true){
		 try{
        		setupStreams();
                       // if(flag==1)
                           whileOnline();
                        
	  	}catch(EOFException eofException){
                	showMessage("\n Server ended the connection! ");
		} catch (IOException ex) { 
                 Logger.getLogger(FileWork.class.getName()).log(Level.SEVERE, null, ex);
             } 
                finally{      
                       System.out.println("from here");
		        closeConnection(); //Changed the name to something more appropriate
                        break;
	       }
	}
    }
    
    void setupStreams()
    {
        try {
             dos = new DataOutputStream(clientSock1.getOutputStream());
             d_input = new DataInputStream(clientSock1.getInputStream());
            output = new ObjectOutputStream(clientSock.getOutputStream());
            output.flush();            
            input = new ObjectInputStream(clientSock.getInputStream());            
            showMessage("\nAsking for id?");
            id="";            
            do{// this will auto process
                    try{
                            id = (String) input.readObject();
                            //showMessage("\n" + message);
                            showMessage("\nUser Id: "+id);
                    }catch(ClassNotFoundException classNotFoundException){
                            showMessage("The user has sent an unknown object!");
                    }
            }while(id.length()==0);
                showMessage("\n User: <"+id+"> is now connected");
                root.userDataModel.addElement(id);
        } catch (IOException ex) {
            Logger.getLogger(FileWork.class.getName()).log(Level.SEVERE, null, ex);
            //Error
        }
    }
    private void whileOnline() throws IOException{
		String message;
		while(true){
			try{
				message = (String) input.readObject();
                                System.out.println("whileon-"+message);
				if(message.charAt(0)=='U')//upload
                                {
                                    String filename="";
                                    int filesize=0,i,j=message.length();
                                    for(i=1;i<j;i++)
                                    {
                                        if(message.charAt(i)=='-')
                                        {
                                            filesize=Integer.parseInt(message.substring(i+1));
                                            break;
                                        }
                                        else
                                        {
                                            filename+=message.charAt(i);
                                        }
                                    }
                                    showMessage("\n User: "+id+" uploading a file...");
                                    saveFile(filename, filesize);
                                }
                                else if(message.charAt(0)=='D')//download
                                {
                                    String filename=message.substring(1);
                                     showMessage("\n User: "+id+" downloading a file("+filename+")...");
                                    sendFile(filename);
                                }
                                else if(message.charAt(0)=='S')//search file
                                {
                                    showMessage("\n User: "+id+" is searching for "+message.substring(1));
                                    searchFile(message.substring(1),1);
                                }
                                else if(message.charAt(0)=='A')//all file
                                {
                                    showMessage("\n User: "+id+" is requesting for all file list");
                                    searchFile("A",2);
                                }
			}catch(ClassNotFoundException classNotFoundException){
				showMessage("The user has sent an unknown request!");
			}
		}
	}
    private void saveFile(String filename,int filesize){
           try{
               sendMessages("Y");
               // System.out.println("save--"+filename+" "+filesize);
               synchronized(this){
		FileOutputStream fos = new FileOutputStream(homeDirectory+"\\"+filename);
		byte[] buffer = new byte[4096];		
		//int filesize = 53608*1024; // Send file size in separate msg
		int read = 0;
		int totalRead = 0;
		int remaining = filesize;
		while((read = d_input.read(buffer, 0, Math.min(buffer.length, remaining))) > 0&&totalRead<filesize) {
			totalRead += read;
			remaining -= read;
			System.out.println("read " + totalRead + " bytes.");
			fos.write(buffer, 0, read);
		}
		
		fos.close();
                
               }
               // d_input.close();
                showMessage("\n"+filename+" uploaded successfully");
                root.fileDataModel.addElement(filename);
           }
           catch(Exception ex)
           {
               //Error;
           }
    }
    public void sendFile(String filename){
		try{ 
                    File file=new File(homeDirectory+"\\"+filename);
                    int filesize=(int) file.length();
                    sendMessages("D"+String.valueOf(filesize));
                    synchronized(this){ 
                    FileInputStream fis = new FileInputStream(homeDirectory+"\\"+filename);
                   
                    byte[] buffer = new byte[4096];
                    int size=0,total=filesize;
                    while ((size=fis.read(buffer)) > 0&&total>0) {
                            dos.write(buffer);
                            System.out.println(size);
                            total-=size;
                    }
                    //System.out.println(total);
                    fis.close();
                    showMessage("\n"+filename+" downloaded successfully by User: "+id);
                  }
                }catch(Exception ex)
                {
                    System.out.println(ex);
                }
		
			
	}
       void searchFile(String key,int type)
       {
           File dir_file=new File(homeDirectory);
           String[] filelist=dir_file.list();
           int total=filelist.length,flag=0;
           //System.out.println(total);
           for(int i=0;i<total;i++)
           {
               if(filelist[i].equals(key)&&type==1)
               {
                   if(type==1){
                     sendMessages("S"+key);
                     flag=1;
                     System.out.println("S"+key);
                       break;
                   }                   
               }
               else if(type==2)
               {
                       key+="-"+filelist[i];
                       flag=1;
               }
           }
           if(type==2&&flag==1)
               sendMessages(key);
           if(flag==0){
               sendMessages("N");
           }
           //System.out.println(key);
       }
        public void closeConnection(){
		showMessage("\nUser: <"+id+"> closing Connections... \n");
                root.userDataModel.removeElement(id);
		try{
                       dos.close();
                       d_input.close();
                       thread[threadIdx]=null;
			output.close(); //Closes the output path to the client
			input.close(); //Closes the input path to the server, from the client.
			clientSock.close(); //Closes the connection between you can the client
                        clientSock1.close();
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
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
       void sendMessages(String message)
       {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException ex) {
                //Logger.getLogger(FileWork.class.getName()).log(Level.SEVERE, null, ex);
                //Error;
            }
       }
}