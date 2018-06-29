package ch.qumo.sshcommander.localcommands;



public class LocalCommands {

    
    private static final String SPECIAL_COMMAND_TAG = "==>";
    
    
    
    public static boolean isSpecialCommand(String command) {
        return command.startsWith(SPECIAL_COMMAND_TAG);
    }
    
    
    
    public static void executeSpecialCommand(String command) {
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
    
    
    public static String[] extractParams(String command) {
        String paramsStr = command.substring(command.lastIndexOf("(")+1, command.lastIndexOf(")"));
        return paramsStr.split(",");
    }
    
    
    
}
