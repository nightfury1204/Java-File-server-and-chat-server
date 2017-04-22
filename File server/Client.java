import java.io.*;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

public class Client extends JFrame{
	
	private Socket connection;
        private Socket connection1;
        private ObjectOutputStream output;
	private ObjectInputStream input;
        DataInputStream d_input;
        DataOutputStream dos;
        private String myid;
	private JList fileData;
        private DefaultListModel fileDataModel = new DefaultListModel();
        private JTextField searchBar;
        private JButton searchButton;
        private JButton uploadButton;
        private JButton downButton;
        private JTextArea logWindow;
        private JTextField uploadFileDir;
        private JButton browserButton;
        private JProgressBar uploadProgress;
        private JProgressBar downProgress;
        private JFileChooser Filechooser;
        private JButton showFileButton;
        private String fileLocation;
        private int upFileSize;
        private String fileStorageDir="E:\\UserFileStorage";
	public Client(String client) {
             super("User_id: "+client);
             myid=client;
             fileStorageDir+="\\"+myid;
             this.setLayout(null);
             Filechooser = new JFileChooser();
             fileData = new JList(fileDataModel);fileData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
             searchBar = new JTextField();
             searchButton = new JButton("Search");
             uploadButton = new JButton("Upload");
             downButton = new JButton("Download");
             showFileButton = new JButton("Show All Files");
             logWindow = new JTextArea();
             JScrollPane log = new JScrollPane(logWindow);
             JScrollPane file_s = new JScrollPane(fileData);
             uploadFileDir = new JTextField();
             browserButton = new JButton("Select");
             uploadProgress = new JProgressBar();uploadProgress.setBackground(Color.WHITE);
             downProgress = new JProgressBar();downProgress.setBackground(Color.WHITE);
             getContentPane().add(searchBar);
             searchBar.setBounds(15,15,200,40);
             getContentPane().add(searchButton);
             searchButton.setBounds(225,15,100,40);             
             getContentPane().add(file_s);
             file_s.setBounds(15,70,310,460);
             getContentPane().add(showFileButton);
             showFileButton.setBounds(100,550,150,30);
             getContentPane().add(uploadFileDir);
             uploadFileDir.setBounds(340,70,250, 23);
             getContentPane().add(browserButton);
             browserButton.setBounds(600,70,80,23);             
             getContentPane().add(uploadProgress);
             uploadProgress.setBounds(340,100,338,18);
             getContentPane().add(uploadButton);
             uploadButton.setBounds(450,135,90,40);
             getContentPane().add(downProgress);
             downProgress.setBounds(340,200,338,18);
             getContentPane().add(downButton);
             downButton.setBounds(450,235,90,40);
             getContentPane().add(log);
             log.setBounds(340,300, 335,230);
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
             searchBar.addActionListener(
				new ActionListener(){
				public void actionPerformed(ActionEvent event){
					sendMessage("S"+event.getActionCommand());
				}
			}
		);
              searchButton.addActionListener(
				new ActionListener(){
				public void actionPerformed(ActionEvent event){
                                        String message=searchBar.getText();
                                        if(message.length()>0)
					sendMessage("S"+message);
				}
			}
	     );
              browserButton.addActionListener(
				new ActionListener(){
				public void actionPerformed(ActionEvent event){
				    int returnVal;
                                    returnVal = Filechooser.showOpenDialog(rootPane);
                                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                                        File file = Filechooser.getSelectedFile();
                                       String loc=file.getAbsolutePath();
                                       uploadFileDir.setText(loc);                                        
                                    }
				}
			}
		);
              uploadButton.addActionListener(
				new ActionListener(){
				public void actionPerformed(ActionEvent event){
                                       String loc=valid_path(uploadFileDir.getText());
                                       String filename=findFileName(loc);
                                       File f_ch=new File(loc);
                                        if(f_ch.exists()==true){
                                            String message="U"+filename+"-";
                                            message+=String.valueOf(f_ch.length());
                                            sendMessage(message);
                                            fileLocation=loc;
                                            upFileSize=(int)f_ch.length();
                                           // sendFile(loc,(int)f_ck.length());
                                        }
                                        else showMessage("\nselect a valid file..");
				
				}
			}
	     );
             downButton.addActionListener(
				new ActionListener(){
				public void actionPerformed(ActionEvent event){
                                        String message=fileData.getSelectedValue().toString();
                                        if(message.length()>0)
					sendMessage("D"+message);
                                        else showMessage("\n select a file..");
				}
			}
	     );
             showFileButton.addActionListener(
				new ActionListener(){
				public void actionPerformed(ActionEvent event){
					sendMessage("A");
				}
			}
	     );
             this.setSize(700,635);
             this.setVisible(true);
             this.setResizable(false);
             this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // viewBar();
	}
 
	void setConection(String host,int port)
        {
            // File fil=new File(file);
              // System.out.println(fil.length());
		try {
                        
			connection = new Socket(host, port);
                        connection1 = new Socket(host,port);   
			//sendFile(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
        }
        void startrunning(String host,int port)
        {
             
             try{
                         File dir=new File(fileStorageDir);
                        if(dir.exists()==false)
                        {
                            if(dir.mkdirs()==false)
                            {
                                System.out.println("can not create directory");
                            }
                        }
			setConection(host, port);
                        setupStream(host,port);
			whileChatting();
		}
              finally{
			closeConnection();
		}
        }
        void setupStream(String host,int port)
        {
                try{
                   
                    output = new ObjectOutputStream(connection.getOutputStream());
                    output.flush();
                    input = new ObjectInputStream(connection.getInputStream());
                    //showMessage("\n The streams are now set up! \n");
                    output.writeObject(myid);
                    output.flush(); 
                    dos = new DataOutputStream(connection1.getOutputStream());
                    d_input = new DataInputStream(connection1.getInputStream());
                }
                catch(Exception ex){
                    
                }
        }
        void whileChatting(){
            String message="";
            while(true){
                try {
                    message=(String)input.readObject();
                   // System.out.println(message);
                    if(message.charAt(0)=='D'){
                        message=message.substring(1);
                        saveFile(Integer.parseInt(message));
                    }
                    else if(message.charAt(0)=='Y'){
                        sendFile(fileLocation, upFileSize);
                    }
                    else if(message.charAt(0)=='N'){
                        fileDataModel.removeAllElements();
                        showMessage("\nNo File Found");
                    }
                    else if(message.charAt(0)=='S')
                    {
                        final String item=message.substring(1);
                        SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
                                       fileDataModel.removeAllElements();
                                       fileDataModel.addElement(item);
				}
			}
		);
                       
                        //fileData.setModel(fileDataModel);
                    }
                    else if(message.charAt(0)=='A')
                    {
                        //System.out.println("1111");
                        message=message.substring(1);
                        final String message1=message;
                        SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
                                       String item="";
                        int l=message1.length();
                        fileDataModel.removeAllElements();
                        for(int i=0;i<l;i++)
                        {
                            if(message1.charAt(i)=='-'||i==l-1)
                            {
                               // System.out.println(item);
                                if(i==l-1)
                                    item+=message1.charAt(i);
                                if(item.length()>1){
                                    fileDataModel.addElement(item);
                                  //  System.out.println("-->"+item);
                                }
                                item="";
                            }
                            else{
                                item+=message1.charAt(i);
                            }
                        } 
				}
			}
		);
                       
                       // fileData.setModel(fileDataModel);
                    }
                } catch (Exception ex) {
                    //error;
                    System.out.println(ex);
                }
            }
        }
        //Close connection
	private void closeConnection(){
		showMessage("\n Closing the connection!");
		try{
			output.close();
			input.close();
			connection.close();
                        connection1.close();
                        d_input.close();
                        dos.close();
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
        public void saveFile(int filesize){
             try{
                 synchronized(this){
                 String filename= fileData.getSelectedValue().toString();
                
		FileOutputStream fos = new FileOutputStream(fileStorageDir+"//"+filename);
		byte[] buffer = new byte[4096];                
                int read = 0;
		long totalRead = 0;
		int remaining = filesize;  
                downProgress.setStringPainted(true);
                downProgress.setForeground(Color.decode("#00AA00"));
                // System.out.println("file sz: "+filesize);
		while((read = d_input.read(buffer, 0, Math.min(buffer.length, remaining))) > 0&&totalRead<filesize) {
			totalRead += read;
			remaining -= read;
			//System.out.println("read " + totalRead + " bytes.");
			fos.write(buffer, 0, read);                    
                        downProgress.setValue((int) ((totalRead*100)/(long)filesize));
                      // System.out.println(((totalRead*100)/filesize));
		}		
		fos.close();
               
              //  d_input.close();
               // downProgress.setValue(100);
               JOptionPane.showMessageDialog(this,"DownLoad Complete");
               downProgress.setValue(0);
               downProgress.setStringPainted(false);
                showMessage("\n"+filename+" downloaded successfully");
                
              }
           }
           catch(Exception ex)
           {
               //Error;
               System.out.println(ex);
           }
        }
	public void sendFile(String file,int filesize){
		try{
                   synchronized(this){
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int size=0,total=filesize;
                    long totalRead=0; 
                    uploadProgress.setStringPainted(true);
                    uploadProgress.setForeground(Color.decode("#00AA00"));
                    while ((size=fis.read(buffer)) > 0&&total>0) {
                            dos.write(buffer);
                          // System.out.println(size);
                            total-=size;
                            totalRead+=size;                   
                            uploadProgress.setValue((int)((totalRead*100)/(long)filesize));
                           // System.out.println(((totalRead*100)/(long)filesize));
                    }
                    //System.out.println(total);
                    fis.close();
                    JOptionPane.showMessageDialog(this,"UpLoad Complete");
                    uploadProgress.setValue(0);
                    uploadProgress.setStringPainted(false);
                    showMessage("\n"+findFileName(file)+" uploaded successfully");
                   }
                }catch(Exception ex)
                {
                    //error;
                    System.out.println(ex);
                }		
			
	}
	String valid_path(String path){
            String dir="";
            int l=path.length();
            for(int i=0;i<l;i++)
            {
                if(path.charAt(i)=='\\'){
                    dir+=path.charAt(i);
                }
                 dir+=path.charAt(i);
            }
            return dir;
        }
        String findFileName(String nam){
            String file="";
            int l=nam.length();
            for(int i=0;i<l;i++){
                if(nam.charAt(i)=='\\'){
                file="";
                }else{
                    file=file+nam.charAt(i);  
                }
            }
            return file;
        }
        private void sendMessage(String message){
		try{
			output.writeObject(message);
			output.flush();
		}catch(IOException ioException){
			showMessage("\n Oops! Something went wrong!");
		}
	}
	
	//update chat window
	private void showMessage(final String message){
            //type=1,system; type=2,to me; type=3,from me;
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
                                       logWindow.append(message);
				}
			}
		);
	}
	
}