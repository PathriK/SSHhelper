package my.SshHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshHelper{
	
	private static final String SUDO_PASS_PPROMPT = "[sudo] password";
	
	private Session session;
	private Channel channel;
	
	private InputStream in;
	private OutputStream out;
	private PrintStream ps;
	
	private String prompt;
	
	private Map<String,String> connectionParams = new HashMap<String,String>();
	
	private boolean ERRORED;
	
	private String getPrompt() {
		return prompt;
	}

	private void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public boolean isERRORED() {
		return ERRORED;
	}

	public void setERRORED(boolean eRRORED) {
		ERRORED = eRRORED;
	}

	private class connectionParam{
		public final static String HOST = "HOST";
		public final static String USER = "USER";
		public final static String PASSWD = "PASSWORD";
	}
	
	public SshHelper(String host,String user,String pass) throws JSchException, IOException {
		setERRORED(true);
		connectionParams.put(SshHelper.connectionParam.HOST, host);
		connectionParams.put(SshHelper.connectionParam.USER, user);
		connectionParams.put(SshHelper.connectionParam.PASSWD, pass);
		createConnection();
	}

	private void createConnection() throws JSchException, IOException{
		JSch jsch = new JSch();
		session = jsch.getSession(connectionParams.get(SshHelper.connectionParam.USER),connectionParams.get(SshHelper.connectionParam.HOST),22);
		session.setPassword(connectionParams.get(SshHelper.connectionParam.PASSWD));
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect();
		System.out.println("-*****-SSH Session Created for Host : " + connectionParams.get(SshHelper.connectionParam.HOST) + "\n");
		channel = session.openChannel("shell");
		
		out = channel.getOutputStream();
		ps = new PrintStream(out, true);
		in = channel.getInputStream();
		System.out.println("-*****-SSH Shell Ready to Connect\n");		
	}

	
	public String connect() throws JSchException, InterruptedException, IOException{
		channel.connect();
		System.out.println("-*****-SSH Shell Connection Established\n");
		String res = getResponse();
		setPrompt(getEndLine(res));
		System.out.println("-*****-Promt : " + getPrompt());
		setERRORED(false);
		return res;
	}
	
	public boolean verifyPrompt(String prompt){
		return((prompt==getPrompt()?true:false));
	}
	
	public String sendSudoCmd(String cmd) throws IOException, InterruptedException{
		return sendCmd(cmd,connectionParams.get(SshHelper.connectionParam.PASSWD),SUDO_PASS_PPROMPT);		
	}
	
	public String sendCmd(String cmd) throws IOException, InterruptedException{
		return sendCmd(cmd,"","");
	}
	
	public String sendCmd(String cmd,String passwd,String prompt) throws IOException, InterruptedException{
		String res = "\n";
		res = sendString(cmd);
		if(passwd!="")
			if(requiresPswd(res,prompt))
				res = sendString(passwd);
			else
				res = trimCmd(res,cmd);
		else
			res = trimCmd(res,cmd);
		if(ready4Next(res)){
			setERRORED(false);
			res = trimPrompt(res);
		}else{
			setERRORED(true);
		}
		return res.trim();
	}
	
	public void disconnect() throws IOException{
		ps.close();
		out.close();
		in.close();
		channel.disconnect();
		session.disconnect();
		System.out.println("-*****-Server Disconnected\n");
	}	
	
	private String trimCmd(String msg,String cmd){
		return(msg.substring(cmd.length()));
	}
	
	private String trimPrompt(String msg){
		return(msg.substring(0,msg.lastIndexOf("\n")-1));
	}
	private boolean ready4Next(String msg){
		return requiresPswd(msg, prompt);
	}
	private boolean requiresPswd(String msg,String prompt){
		msg = getEndLine(msg);
		if(msg.startsWith(prompt))
			return true;
		return false;
	}
	private String sendString(String cmd) throws IOException, InterruptedException{
		String res = "\n";
		out.write((cmd+"\n").getBytes());
		out.flush();
		res = getResponse();
		return res;
	}
	
	private String getEndLine(String msg){
		String res="";
		res = msg.substring(msg.lastIndexOf("\n")+1);
		return res;
	}

	private String getResponse() throws InterruptedException, IOException{
		Thread.sleep(1000);
		byte[] bt = new byte[1024];
		String result = "";
			while (in.available() > 0) {
				String str;
				int i = in.read(bt, 0, 1024);
				if (i < 0)
					break;
				str = new String(bt, 0, i);
				result = result + str;
				Thread.sleep(1000);
	}
			if(result != ""){
				System.out.println("-*****-Retrieved Result : " + result.substring(0, 6) + "...." + result.substring(result.length()-6) + "\n");
			}else{
				System.out.println("-*****-No Result to be Retrieved" + "\n");
			}
			return result;
	}


}