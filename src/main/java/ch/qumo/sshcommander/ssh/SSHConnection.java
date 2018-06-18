package ch.qumo.sshcommander.ssh;



import java.util.Properties;
import com.jcraft.jsch.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;



public class SSHConnection {
	

	private static final int DEFAULT_SSH_PORT            = 22;
    private static final int SIZE_BYTE_CHAR_BUFF         = 32*4096;
    private static final int WAITING_TIME_MS             = 500;
    private static final int TIMEOUT_CONNECTION_MS       = 4000;
    private static final int CHANNEL_CONNECT_TIMEOUT_MS  = 10000;
    private static final String SSH_COMMAND_ERROR_TEXT = "Error while waiting for ssh response =";
    private static final String SPECIAL_COMMAND_TAG = "==>";
    
    
    private Session session;
	
	
    
	public void connect(String username, String host, String password) throws JSchException {
		JSch jsch = new JSch();
	
		System.out.println("getSession("+username+", "+host+", "+DEFAULT_SSH_PORT+")");
        
	    session = jsch.getSession(
	    		     username,
	    		     host,
	    		     DEFAULT_SSH_PORT
	    		   );
	    session.setPassword(password);
	
	    Properties properties = new Properties();
	    properties.put("StrictHostKeyChecking", "no");
	    session.setConfig(properties);
	    session.connect(TIMEOUT_CONNECTION_MS);
	}
	
	
	
	public void connect(String url) throws JSchException  {
		String username = url.substring(0, url.lastIndexOf(':'));
		String password = url.substring(url.lastIndexOf(':')+1, url.lastIndexOf('@'));
		String host = url.substring(url.lastIndexOf('@')+1);
	
		connect(username,
                host,
                password);
	}
	


    public void disconnect() {
  	  try{
  		  session.disconnect();
  	  } catch (Exception e) {
  		  e.printStackTrace();
  	  }
    }
    
    
    
   /**------------------------------------------------------------------------------
     * sendExecCommand()                                                                
     *------------------------------------------------------------------------------
     *           
     * @param command   String to send to the connected host.
     * @return [out]    buffString  response of the host.                           
     *                                                                              
     * Send the string "command" to the connected host. The size of buffer and the  
     * time to wait between each while loop can be changed. I recommend at least    
     * 4096 bytes for the chart and at least 500ms for the waiting time.            
     *------------------------------------------------------------------------------
     */
    public String sendExecCommand( String command ) {
        StringBuilder buffString = new StringBuilder();
        ChannelExec channel;
        
        try {
            channel = (ChannelExec)session.openChannel("exec");
            
            channel.setPty(true);
            channel.setCommand(command);
          
            channel.setInputStream(null);
            channel.setErrStream(System.err, true);
  
            InputStream in = channel.getInputStream();
            channel.connect(CHANNEL_CONNECT_TIMEOUT_MS);
            System.out.println("System connected...");
  
            // read buffer
            byte[] tmp = new byte[ SIZE_BYTE_CHAR_BUFF ];

            int i = 0;
            while(true){
                //  If there is something in the Stream "in", we wait and then we read.
                // Thread.sleep( WAITING_TIME_MS );

                while( in.available() > 0 ) {
                    i = in.read(
                        tmp,
                        0,
                        tmp.length
                      );

                    if( i < 0 ) {
                      break; 
                    }
                }//while

                // Convert the read buffer to string.
                String buffStringTmp = new String(tmp, 0, i);
                //System.out.print( buffStringTmp );
                buffString.append(buffStringTmp);

                if(channel.isClosed()) {
                    System.out.println( "exit-status: " + channel.getExitStatus() );
                    break;
                }

                Thread.sleep( WAITING_TIME_MS );

            }// while(true)
  
            channel.disconnect();
        } catch (JSchException e) {
        	System.out.println("Error while sending ssh Command"+e.toString());
            
        } catch(IOException e) {
            System.out.println("Error while sending ssh Command"+e.toString());
            
        } catch(InterruptedException e) {
            System.out.println(SSH_COMMAND_ERROR_TEXT + e.toString());
            Thread.currentThread().interrupt();
        }

        return buffString.toString();
    }// sendExecCommand()
    
    
    
    public String sendShellCommand( String[] commands ) {
        String output = "";
        try {
            Channel channel = session.openChannel("shell");// only shell
            
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            // channel.setOutputStream(System.out);
            channel.setOutputStream(os); 
            PrintStream shellStream = new PrintStream(channel.getOutputStream());// printStream for convenience 
            
            channel.connect();
            
            // TODO: Parse all commands first into a command object (to avoid parsing errors during exec and not start commands?)
            for(String command : commands) {
                if(isSpecialCommand(command)) {
                    executeSpecialCommand(command);
                } else {
                    shellStream.println(command);
                    shellStream.flush();
                }
            }
            
            while(true) {
                if(channel.isClosed()) {
                    System.out.println( "exit-status: " + channel.getExitStatus() );
                    break;
                }
                Thread.sleep( WAITING_TIME_MS );
            }
            
            output = os.toString("UTF8");
            shellStream.close();
            channel.disconnect();
            
        } catch (JSchException e) {
        	System.out.println(SSH_COMMAND_ERROR_TEXT + e.toString());
        } catch(IOException e) {
            System.out.println(SSH_COMMAND_ERROR_TEXT + e.toString());
        } catch(InterruptedException e) {
            System.out.println(SSH_COMMAND_ERROR_TEXT + e.toString());
            Thread.currentThread().interrupt();
        }
  
        return output;
    }// sendShellCommand()
    
    
    
    private boolean isSpecialCommand(String command) {
        return command.startsWith(SPECIAL_COMMAND_TAG);
    }
    
    
    
    private void executeSpecialCommand(String command) {
        String commandClean = command.substring(SPECIAL_COMMAND_TAG.length()).trim().toUpperCase();
        if(commandClean.contains("WAIT")) {
            int waitingTimeMs = Integer.parseInt(extractParams(command)[0]);
            try {
                Thread.sleep( waitingTimeMs );
            } catch(InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    private String[] extractParams(String command) {
        String paramsStr = command.substring(command.lastIndexOf("(")+1, command.lastIndexOf(")"));
        return paramsStr.split(",");
    }
    
    
}
