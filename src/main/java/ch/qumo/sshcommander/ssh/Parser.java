package ch.qumo.sshcommander.ssh;



public class Parser {


    // Used to know the correct EOL character
    public static String newlineChar = System.getProperty("line.separator");



    /** ------------------------------------------------------------------------------
     * @param name string to look for.
     * @param toParse string where to look for the "name".
     * @return "value" (string positioned after the string "name")
     *
     * This function returns the string positioned between the end of the string "name" and the next EOL, inside of the string "toParse".
     * ------------------------------------------------------------------------------
     */
    public static String getValueOf(String name, String toParse) {
        String value;

        //We look for the position of the "name" string.
        int beginIndex = toParse.indexOf(name);

        //We move the index at the end of the string.
        beginIndex = beginIndex + name.length();

        //Now we look for the next newlineChar after beginIndex
        int endIndex = toParse.indexOf(newlineChar, beginIndex);

        //We consider that the value is between beginIndex and endEndex.
        value = toParse.substring(beginIndex, endIndex);
        //System.out.println("TOPARSE get value of= "+name/*+" in "+toParse+" = "*/+value);
        return value;
    }//getValue()



    /** ------------------------------------------------------------------------------
     * @param text string to look for.
     * @param toParse string where to look for the "name".
     * @return "value" (string positioned after the string "name")
     *
     * This function returns the string positioned between the end of the string "name" and the next EOL, inside of the string "toParse".
     * -------------------------------------------------------------------------------
     */
    public static int getNumberOf(String text, String toParse) {
        int numberOf = 0;
        int fromIndex = 0;
        int previousIndex;

	    // As long as toParse.indexOf( text, fromIndex ) != -1, meaning that as long as
        // we find new occurrences of text in toParse, we increase the counter numberOf.
        while((previousIndex = toParse.indexOf(text, fromIndex)) != -1) {
            numberOf++;
            // Don't forget to move the index...
            fromIndex = previousIndex + text.length();
        }

        return numberOf;
    }//getNumberOf()

}//class Parser

