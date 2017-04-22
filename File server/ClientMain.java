
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class ClientMain {
    public static void main(String[] args) {
                String id= JOptionPane.showInputDialog(new JFrame("User"),"Enter Username:");
                if(id==null)
                    System.exit(0);
		Client fc = new Client(id);
                fc.startrunning("127.0.0.1", 1900);
	}

}
