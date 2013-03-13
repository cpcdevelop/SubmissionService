/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hu.sztaki.lpds.pgportal.portlets.asm;

import hu.sztaki.lpds.pgportal.portlets.asm.MoleculeStore;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.taglibs.standard.tag.common.core.CatchTag;

/**
 *
 * @author user
 */
public class MoleculeStore {
    private static int Maximum_Visualize=100;

    private String filePath="";
    private String fileName = "";

    private double energyLevel = 0;
    private String energyLevelUnit ="kcal/mol";
    private String moleculeTitle = "";
    private String moleculeInfo=""; // mol weight, formula, etc;


    MoleculeStore(String filePath,String fileName)
    {
        ASM_SamplePortlet.writeLog(Thread.currentThread().getStackTrace()[1].getMethodName()+" #new Molecule Constr. "+filePath+" "+fileName, 1);
        this.filePath=filePath;
        this.fileName=fileName;

        try{
            acquireEnergyLevel();
            this.moleculeTitle=getMoleculeTitle(filePath+fileName);
        }catch(Exception ex)
        {
            String s="a";
        }
    }

    MoleculeStore(String fileAbsolutePath)
    {
        this(new File(fileAbsolutePath).getParent()+"/",new File(fileAbsolutePath).getName());
    }

  // merge the ligand file to the receptor.
  public static String MergeMolecules(String receptorFile, String ligandFile)
  {
        try {
            ASM_SamplePortlet.writeLog(Thread.currentThread().getStackTrace()[1].getMethodName()+" # "+receptorFile+" "+ligandFile, 1);
            FileInputStream fstream = new FileInputStream(ligandFile);

            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            PrintWriter pw = new PrintWriter(new FileWriter(receptorFile,true));
            while ((strLine = br.readLine()) != null) {
                strLine=strLine.replace("MODEL", "REMARK by MODEL");
                strLine=strLine.replace("ROOT","REMARK by ROOT");
                pw.println(strLine);
            }
            pw.close();
            fstream.close();
        } catch (Exception ex) {
            ASM_SamplePortlet.writeLog("MergeMolecules() "+ex.getMessage(), 0);
        }
		return receptorFile+ligandFile;
  }

  public void mvMoleculeToScriptDir(String toPath) throws IOException{
    mvMoleculeToScriptDir(toPath,fileName);
  }

  public void mvMoleculeToScriptDir(String toPath,boolean newUUIDname) throws IOException{
    UUID idOne = UUID.randomUUID();
    String fileExtension;
    if (!fileName.contains(".")) { fileExtension=".pdb";}else{
     fileExtension=fileName.substring(fileName.lastIndexOf("."));
    }
    mvMoleculeToScriptDir(toPath,idOne+"."+fileExtension);
  }


  public void mvMoleculeToScriptDir(String toPath,String toFileName) throws IOException{
    ASM_SamplePortlet.writeLog(Thread.currentThread().getStackTrace()[1].getMethodName()+" #mv. "+toPath+" "+toFileName, 1);
    File to=new File(toPath+toFileName);
    File from=new File(filePath+fileName);
    FileChannel sourceChannel = null;
    FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(from).getChannel();
            destChannel = new FileOutputStream(to).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }catch(Exception ex){
            ASM_SamplePortlet.writeLog(Thread.currentThread().getStackTrace()[1].getMethodName()+" EXCEPTION "+ex.getMessage()+" "+toPath+" "+toFileName, 0);
        }finally{
               sourceChannel.close();
               destChannel.close();
       }
    this.filePath=toPath;
    this.fileName=toFileName;
  }


    // multi PDBQT
    public static MoleculeStore[] splitMultiMolecule(String filePathPDBQT,String splitDir){
        ArrayList<MoleculeStore> ret=new ArrayList<MoleculeStore>();
        String strLine;
        int count=0;
        try {
            FileInputStream fstream = new FileInputStream(filePathPDBQT);

            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            String fileExtension=filePathPDBQT.substring(filePathPDBQT.lastIndexOf("."));
            if (fileExtension.equals("")) fileExtension=".pdb";

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String pdbqtNewName="";

            //Read File Line By Line
            int countRes=0;
            FileOutputStream fos=null;
            DataOutputStream out=null;
            BufferedWriter bw=null;

            PrintWriter pw=null;
            ASM_SamplePortlet.writeLog(Thread.currentThread().getStackTrace()[1].getMethodName()+" #split: "+filePathPDBQT, 1);

            while ((strLine = br.readLine()) != null) {
                // MODEL        1
                if (strLine.startsWith("MODEL")&&strLine.contains("1")) {
                    try{
                        count++;
                        ASM_SamplePortlet.writeLog("splitMultiMolecule() no "+count+" Molecule found ", 1);
                        UUID idOne = UUID.randomUUID();
                        pdbqtNewName=idOne+fileExtension;
                        pw = new PrintWriter(new FileOutputStream(splitDir+pdbqtNewName));
                        ASM_SamplePortlet.writeLog(Thread.currentThread().getStackTrace()[1].getMethodName()+" #new mol "+splitDir+pdbqtNewName, 1);
                    }catch (Exception exp) {
                        String s="a";
                    }
                }

                // copy lines to new file;
                if (pw!=null){
                    try{
                     pw.println(strLine);
                     pw.flush();
                    }catch (Exception exp) {
                        String s="a";
                    }
                    if (strLine.startsWith("ENDMDL")) {
                    try{
                        pw.close();
                       ret.add(new MoleculeStore(splitDir, pdbqtNewName));
                       ASM_SamplePortlet.writeLog(Thread.currentThread().getStackTrace()[1].getMethodName()+" #Molecule end "+splitDir+pdbqtNewName, 1);
                        if (count>=Maximum_Visualize)
                        {
                            break;
                        }

                    }catch (Exception exp) {
                        String s="a";
                    }

                    }
                }
            }
        } catch (Exception ex) {
            ASM_SamplePortlet.writeLog(Thread.currentThread().getStackTrace()[1].getMethodName()+" EXCEPTION "+ex.getMessage()+" path:"+filePathPDBQT, 0);
        }
        return ret.toArray(new MoleculeStore[ret.size()]);
    }

    private static BufferedWriter newMolecule(String fileName) throws FileNotFoundException
    {
       FileOutputStream fos = new FileOutputStream(fileName);
       DataOutputStream out= new DataOutputStream(fos);
       BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
       return bw;
    }

    // ONLY IN PDBQT files!!!
	private void acquireEnergyLevel() throws IOException
	{
        FileInputStream fstream=null;
        DataInputStream in=null;
        BufferedReader br=null;
        try {
             fstream = new FileInputStream(filePath+fileName);
             in = new DataInputStream(fstream);
             br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            int countRes=0;
            while ((strLine = br.readLine()) != null) {

                // PDBQT Vina
                if (strLine.contains("VINA RESULT")) {
                    //matches to the whitespace chars
                    String[] rows = strLine.split("\\s+");
                    energyLevel= Double.parseDouble(rows[3]);
                    break;
                }
                // PDB Autodock
                // USER    Estimated Free Energy of Binding    =
                if (strLine.startsWith("USER") && strLine.contains("Estimated Free Energy of Binding")&&strLine.contains("=")) {
                    //matches to the whitespace chars
                    String[] rowsEq = strLine.split("=");
                    // right side of =
                    String[] rows = rowsEq[1].split("\\s+");
                    energyLevel=Double.parseDouble(rows[1]);
                    energyLevelUnit= rows[2];
                    break;
                }

            }
        } catch (Exception ex) {
            ASM_SamplePortlet.writeLog(Thread.currentThread().getStackTrace()[1].getMethodName()+" EXCEPTION "+ex.getMessage(), 0);
        }
        finally{
            br.close();
            in.close();
            fstream.close();
        }
	}

    //energy_Level_unit
// ONLY IN PDB files !!!
// exist ~ in 3 line in the file
   	private String getMoleculeTitle(String filePathPDB) throws IOException
	{
        String retTitle ="";
        FileInputStream fstream=null;
        DataInputStream in=null;
        BufferedReader br=null;
        try {
             fstream= new FileInputStream(filePathPDB);
             in = new DataInputStream(fstream);
             br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            int titleCounter = 0;
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains("formula")) {
                    String[] rows = strLine.split("\\s+");
                    moleculeInfo += rows[2]+",  ";
                }
                if (strLine.contains("mol_weight")){
                    moleculeInfo += strLine.substring(20).trim()+" ";
                }
                // TITLE should be continously in the following lines
                if (strLine.contains("TITLE")) {
                    retTitle += strLine.substring(5).trim();
                    titleCounter++;
                }else{
                    if (titleCounter>0){
                        return retTitle;
                    }
                }
                if (strLine.contains("name")) {
                    retTitle += strLine.substring(13).trim();
                }
                if (titleCounter >= 3) {
                    return retTitle;
                }
            }
        } catch (Exception ex) {
            ASM_SamplePortlet.writeLog(Thread.currentThread().getStackTrace()[1].getMethodName()+" EXCEPTION "+ex.getMessage(), 0);
        }
        finally{
            br.close();
            in.close();
            fstream.close();
        }
        return retTitle;
	}

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the energyLevel
     */
    public double getEnergyLevel() {
        return energyLevel;
    }

    /**
     * @return the moleculeTitle
     */
    public String getMoleculeTitle() {
        return moleculeTitle;
    }

    /**
     * @return the moleculeInfo
     */
    public String getMoleculeInfo() {
        return moleculeInfo;
    }

    /**
     * @return the energy_Level_unit
     */
    public String getEnergyLevelUnit() {
        return energyLevelUnit;
    }
    /**
     * @return the Maximum_Visualize
     */
    public static int getMaximum_Visualize() {
        return Maximum_Visualize;
    }

    /**
     * @param aMaximum_Visualize the Maximum_Visualize to set
     */
    public static void setMaximum_Visualize(int aMaximum_Visualize) {
        Maximum_Visualize = aMaximum_Visualize;
    }


}
