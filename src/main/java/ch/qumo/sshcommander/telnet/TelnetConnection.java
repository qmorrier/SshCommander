package ch.qumo.sshcommander.telnet;



import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetInputListener;



public class TelnetConnection implements TelnetInputListener {
	

	private static final int DEFAULT_TELNET_PORT = 23;
    private static final int SIZE_BYTE_CHAR_BUFF = 32*4096;
    private static final int WAITING_TIME_MS     = 500;
 
	
	
    
	public String sendCommands(String url,
                               String[] commands) throws IOException {
        
		String username = url.substring(0, url.lastIndexOf(':'));
		String password = url.substring(url.lastIndexOf(':')+1, url.lastIndexOf('@'));
		String host = url.substring(url.lastIndexOf('@')+1);
        
        String res = "";
        
        TelnetClient telnet = null; 
        InputStream in = null; 
        PrintStream out = null; 
        
        telnet = new TelnetClient();
        
        System.out.println("Connecting to server: " + host + " on port: "+ DEFAULT_TELNET_PORT);
        telnet.connect(host, DEFAULT_TELNET_PORT);
        
        in = telnet.getInputStream();
        //ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        
        out = new PrintStream(telnet.getOutputStream());
        
        // Login
        write(out, username);
        write(out, password);
        
        
        // And then commands
        for(String command : commands) {
            write(out, command);
        }
        
        // read buffer
        StringBuilder buffString = new StringBuilder();
        byte[] tmp = new byte[ SIZE_BYTE_CHAR_BUFF ];
        int i = 0;
        try {
            while(true){
                //  If there is something in the Stream "in", we wait and then we read.
                //Thread.sleep( WAITING_TIME_MS );
                
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
                buffString.append(buffStringTmp);
                
                Thread.sleep( WAITING_TIME_MS );
                
                if(!(in.available() > 0)) {
                    if(!telnet.isConnected()) {
                        System.out.println("!telnet.isConnected()");
                        break;
                    }
                    if(!telnet.sendAYT( WAITING_TIME_MS )) {
                        System.out.println("!telnet.sendAYT( WAITING_TIME_MS )");
                        break;
                    }
                }
                
            }// while(true)

        } catch(InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            
        } catch(SocketException e) {
            e.printStackTrace();
            
        } finally {
            in.close();
            out.close();
            telnet.disconnect();
        }
        
        res = buffString.toString();
        System.out.println(res);
        
        return res;
	}
	
	
    
    private void write(PrintStream printStream, String value) { 
        printStream.println(value);
        printStream.flush(); 
    } 



    public void telnetInputAvailable() {
        System.out.println("telnetInputAvailable");
    }
}
