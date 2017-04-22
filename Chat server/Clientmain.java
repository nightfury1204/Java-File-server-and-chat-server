import javax.swing.JFrame;
import javax.swing.JOptionPane;
public class Clientmain {
	public static void main(String[] args) {
		Client obj;
                String id=JOptionPane.showInputDialog(new JFrame("User"),"Enter Username:");
                if(id==null)
                    System.exit(0);
		obj = new Client("127.0.0.1",id);
		obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		obj.startRunning();
	}
}