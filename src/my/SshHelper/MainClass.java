package my.SshHelper;



import java.io.IOException;
import com.jcraft.jsch.JSchException;

public class MainClass {

	private static String host = "192.168.242.135";
	private static String user ="g-o-d";
	private static String pass = "pkk211288";
	private static String response = "";
	
	public static void main(String[] args) throws JSchException, IOException, InterruptedException {
		
		SshHelper MySSH = new SshHelper(host, user, pass);
		
		response = MySSH.connect();
		if(response!="" && !MySSH.isERRORED()){
			response = MySSH.sendCmd("ls");
			System.out.println("response : \n" + response);
			
			response = MySSH.sendSudoCmd("sudo ls");
			System.out.println("response : \n" + response);
			
			response = MySSH.sendSudoCmd("sudo ls");
			System.out.println("response : \n" + response);
			
			MySSH.disconnect();	
		}		
		
	}

}
