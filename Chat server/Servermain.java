import javax.swing.JFrame;

public class Servermain {
	public static void main(String[] args) {
		Server obj = new Server();
		obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		obj.startRunning();
	}
}