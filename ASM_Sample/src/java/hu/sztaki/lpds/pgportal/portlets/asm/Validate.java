/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hu.sztaki.lpds.pgportal.portlets.asm;

/**
 *
 * @author user
 */
public class Validate {
    private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };
    private static String[] wf_types={"123","234","567"};

    public static boolean textIsAlphaNumeric(String value) {
        return value.matches("^[A-Za-z0-9]+$");
    }

    public static boolean textIsNumeric(String value) {
        return value.matches("^[0-9]+$");
    }
    public static boolean fileNameValid(String fileName)  {
       if (fileName.length()==0) return false;
       if (fileName.equals(" ")) return false;
       if (fileName.contains(new String(ILLEGAL_CHARACTERS))) return false;
       return true;
    }

    public static boolean numberIsNaturalNumber(String value,int rangeMin, int rangeMax)
    {
        int valueNumber = Integer.parseInt(value.toString());
        if (rangeMin>=rangeMax) return false;
            // throw new Exception(" rangeMin > rangeMax");
        if ((valueNumber >= rangeMin) && (valueNumber <= rangeMax)) {
            return true;
        }
        return false;
    }

    // sets the value inside the range
    public static String numberApplyRange(String value,int rangeMin, int rangeMax)
    {
        int valueNumber = Integer.parseInt(value.toString());
        if (rangeMin>valueNumber) value=""+rangeMin;
        if (rangeMax<valueNumber) value=""+rangeMax;
        return value;
    }

    public static boolean htmlActionValid(String actionName)
    {
       if (!actionName.startsWith("do")) return false;
       return true;
    }

    // should be Valid as filename, and start with wf type
    public static boolean wfInstanceValid(String selectedWf)
    {
        for (String type : wf_types) {
            if (selectedWf.startsWith(type)&&(fileNameValid(selectedWf))) return true;
        }
        return false;
    }

    /**
     * @param aWf_types the wf_types to set
     */
    public static void setWf_types(String[] aWf_types) {
        wf_types = aWf_types;
    }


}
