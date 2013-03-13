/* Copyright 2007-2011 MTA SZTAKI LPDS, Budapest

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.sztaki.lpds.pgportal.portlets.asm;

import hu.sztaki.lpds.pgportal.services.asm.constants.*;
import hu.sztaki.lpds.pgportal.services.asm.ASMService;
import hu.sztaki.lpds.pgportal.services.asm.ASMWorkflow;
import hu.sztaki.lpds.pgportal.services.asm.beans.ASMJobInstanceBean;
import hu.sztaki.lpds.pgportal.services.asm.beans.ASMRepositoryItemBean;
import hu.sztaki.lpds.pgportal.services.asm.beans.ASMResourceBean;
import hu.sztaki.lpds.pgportal.services.asm.beans.OverviewJobStatusBean;
import hu.sztaki.lpds.pgportal.services.asm.beans.RunningJobDetailsBean;
import hu.sztaki.lpds.pgportal.services.asm.beans.WorkflowInstanceBean;
import hu.sztaki.lpds.pgportal.services.asm.exceptions.general.NotMPIJobException;

import java.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.portlet.*;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import javax.activation.FileTypeMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItem;
import java.io.File;
import java.util.Properties;
import org.apache.james.mime4j.field.datetime.DateTime;
import org.apache.taglibs.standard.tag.common.core.CatchTag;

import org.openbabel.*;



/**
 * A simple key:value map reader populated by an external XML stream
 *
 * @author Jonathan Weatherhead
 */
 class SymbolMap {
    private Properties symbolmap;

    @SuppressWarnings("empty-statement")
    public SymbolMap(File file) {
        symbolmap = new Properties();

        try {
            //Populate the symbol map from the XML file
            symbolmap.loadFromXML( file.toURI().toURL().openStream() );
        }
        catch (Exception e) {
            System.out.println("processaction called...");
        }
    }

    //variable length arguments are packed into an array
    //which can be accessed and passed just like any array
    public String lookupSymbol(String symbol, String... variables) {
        //Retrieve the value of the associated key
        String message = symbolmap.getProperty(symbol);
        if(message == null)
            return "";

        //Interpolate parameters if necessary
        //and return the message
        return String.format(message, variables);
    }
}


/**
 *
 * @author Akos Balasko MTA SZTAKI
 */
public class ASM_SamplePortlet extends GenericPortlet {

    private String DISPLAY_PAGE = "/jsp/asm_sample/docking.jsp";
    private static final String DETAILS_PAGE =
            "/jsp/asm_sample/details.jsp";
    ASMService asm_service = null;

    static private int verbose_level=0;
    static private int verbose_level_log=0;
    private static String log_path="";
    private static String temp_dir="";
    private static int Maximum_WU=500;
    private static int Maximum_Visualize=100;
    private static int DOCKING_VINA=1;
    private static int DOCKING_AUTODOCKAUTOGRID=2;
    private static int DOCKING_AUTODOCKNO=3;

    private static String wf_type_vina="";
    private static String wf_type_autodockautogrid="";
    private static String wf_type_autodockno="";

    private String developerId="";

    public ASM_SamplePortlet() {
        asm_service = ASMService.getInstance();
    }

    public String resolveId(String symbol, SymbolMap symbolmap){
        try{
            String readWfType=symbolmap.lookupSymbol(symbol);
            if (readWfType.length()!=3) {
                throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" invalid XML configuration file! symbol found:"+readWfType, 1);
            }
            String readyType=readWfType;
            return readyType;
        }catch(Exception ex)
        {
            throwError("resolveId failed for:"+symbol+" "+ex.getMessage(),1);
        }
        return "";
    }

    public void readConfig(String contextPath)
    {
        String msgError = "";
        String msgUser = "";
        try{

            msgUser+=throwMessage(Thread.currentThread().getStackTrace()[1].getMethodName()+" ##### XML read ##### ",1);
            SymbolMap symbolmap = new SymbolMap(new File("../webapps/"+contextPath+"/WEB-INF/autodock_config.xml"));
            developerId=symbolmap.lookupSymbol("DeveloperId");

            wf_type_vina=resolveId("AutodockVina", symbolmap);
            wf_type_autodockautogrid=resolveId("Autodock4", symbolmap);
            wf_type_autodockno=resolveId("Autodock4noautogrid", symbolmap);

            log_path=symbolmap.lookupSymbol("LogFile");
            temp_dir=symbolmap.lookupSymbol("TempDir");

            if (temp_dir.endsWith("/")) {
                temp_dir= temp_dir.substring( temp_dir.length()-1)     ;
            }
            Maximum_Visualize=Integer.parseInt(symbolmap.lookupSymbol("MaximumVisualizeFiles"));
            Maximum_WU=Integer.parseInt(symbolmap.lookupSymbol("MaximumWU"));
            verbose_level=Integer.parseInt(symbolmap.lookupSymbol("VerboseLevel"));
            verbose_level_log=Integer.parseInt(symbolmap.lookupSymbol("VerboseLevelLog"));

            MoleculeStore.setMaximum_Visualize(Maximum_Visualize);
            msgUser+=throwMessage(Thread.currentThread().getStackTrace()[1].getMethodName()+" developerId "+developerId,1);

        }catch(Exception ex)
        {
            throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" "+ex.getMessage(),0);
        }
    }

    /**
     * Handling generic actions
     */
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException {
        String msgError="";
        String msgUser="";
        asm_service.init();
        String action = "";
        action = request.getParameter("action");

        boolean isMultipart = PortletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            doUploadLigands(request, response);
            action=null;
        }else{
            if ((request.getParameter("action") != null) && (!request.getParameter("action").equals(""))) {
                action = request.getParameter("action");
            }
            System.out.println("*************" + action + "::" + request.getParameter("action"));
            if (action != null) {
                try {
                    msgUser+=throwMessage(Thread.currentThread().getStackTrace()[1].getMethodName()+" call "+action,10);
                    if (Validate.htmlActionValid(action)){
                        Method method = this.getClass().getMethod(action, new Class[]{ActionRequest.class, ActionResponse.class});
                        method.invoke(this, new Object[]{request, response});
                        msgUser+=throwMessage(Thread.currentThread().getStackTrace()[1].getMethodName()+" "+action+" success",10);
                    }
                } catch (NoSuchMethodException e) {
                    msgError+=throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" No such method ",0);
                } catch (IllegalAccessException e) {
                    msgError+=throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" Illegal access ",0);
                } catch (InvocationTargetException e) {
                    msgError+=throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" Invoked function failed",0);
                }
            }else{
                 msgError+=throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" Wrong request format ",0);
                 msgError+=request.getParameter("errorMessage");
                 response.setRenderParameter("errorMessage", msgError );
            }
        }       
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

        String msgError="";
        String msgUser="";
        String userId = request.getRemoteUser();
        String selected_wf = request.getParameter("user_selected_instance");
        String jobport = "";
        String download_type=request.getParameter("file2download");
        Enumeration paramNames = request.getParameterNames();
        String selected_wf_download="";
        try{
            while (paramNames.hasMoreElements()) {
                String act_param = (String) paramNames.nextElement();
                if (act_param.startsWith("instance_download")) {
                    selected_wf_download = request.getParameter(act_param);
    //                selected_wf = request.getParameter(act_param);
                }
            }
            if (!Validate.wfInstanceValid(selected_wf)) throw new Exception(" invalid selected_wf"+selected_wf);

            if (DockingTypeFromSelectedInstance(selected_wf)==DOCKING_VINA){
                jobport="collector.sh@0@best.pdbqt";
            }
            if (DockingTypeFromSelectedInstance(selected_wf)==DOCKING_AUTODOCKAUTOGRID){
                if (download_type.equals("best")){
                    jobport="Collector@1@best.pdb";
                }
                if (download_type.equals("log.dlg")){
                    //jobport="AutoDock@2@log.dlg";
                    jobport="Collector@2@log.dlg.zip";
                }
               // should="Autodock@2@ log.dlg" = ren log.dlg.tar.gz
               // jobport="AutoGrid@1@best.pdb";
            }
            if (DockingTypeFromSelectedInstance(selected_wf)==DOCKING_AUTODOCKNO){
                if (download_type.equals("best")){
                    jobport="Collector@1@best.pdb";
                }
                if (download_type.equals("log.dlg")){
                    jobport="Collector@2@log.dlg.zip";
                    //jobport="AutoDock@2@log.dlg";
                }
                // should="Autodock@2@ log.dlg" = ren log.dlg.tar.gz
                //jobport="Generator@1@best.pdb";
            }
            
            String[] splitted = jobport.split("@");
            String selected_job = splitted[0];

            String file_port= splitted[2];
            selected_wf_download = selected_wf;

            msgUser+=throwMessage("Download result "+selected_wf+"/"+selected_job ,10);
            try {
                response.setContentType("application/zip");
                response.setProperty("Content-Disposition", "inline; filename=\"" + selected_wf_download + "_results.zip\"");

                asm_service.getFileStream(userId, selected_wf_download, selected_job, file_port, response);
                //asm_service.getFileStream(userId, selected_wf_download, selected_job, null, response);

                request.setAttribute("selected_wf",  selected_wf );
            } catch (Exception e) {
                msgError+=throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" "+e.getMessage(),0);
            }
        } catch (Exception e) {
            msgError+=throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" "+e.getMessage(),0);
        }
        request.setAttribute("userMessage",  msgUser);
        request.setAttribute("errorMessage",  msgError);
    }

    private final static Object sync_root= new Object();

    public String importWorkflow(String owner, String remoteUser, String impItemId, String instanceName) throws PortletException {
        String returnInsertedID="";
        try {
            synchronized(sync_root){
                String impWfType = RepositoryItemTypeConstants.Application;
                //String impItemId = request.getParameter("impItemId");
                //String owner = request.getParameter("rep_owner").toString();

                Calendar cal = Calendar.getInstance();
                //SimpleDateFormat udf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                //String projectName = impItemId + "_" + udf.format(cal.getTime());

                SimpleDateFormat udf = new SimpleDateFormat("SSS");
                instanceName += "_" + udf.format(cal.getTime());

                //impItemId = projectName;
                asm_service.ImportWorkflow(
                        remoteUser,
                        instanceName,
                        owner,
                        impWfType,
                        impItemId);
                ArrayList<ASMWorkflow> awf=asm_service.getASMWorkflows(remoteUser);
                int awf_size=awf.size();
                returnInsertedID=awf.get(awf_size-1).getWorkflowName();
            }
            throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" New wf instance imported # id "+returnInsertedID,1);

        } catch (Exception ex) {
            throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" "+ex.getMessage(),0);
            Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnInsertedID;
    }

    static private PrintWriter pw=null;
    public static void writeLog(String message,int prio)
    {
        if (pw==null){
            try {
                if (log_path.length()==0){
                    pw = new PrintWriter(new FileOutputStream("autodock.log",true));
                }else{
                    pw = new PrintWriter(new FileOutputStream(log_path,true));
                }
            } catch (Exception ex) {
                Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Date t=new Date();
        pw.println(t+" "+prio+" "+message);
        pw.flush();
        pw.close();
        pw=null;
    }

    public String throwError(String message,int prio)
    {
        System.out.print(prio+"# [ERROR] "+message);
        Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.ALL, message);
        if (prio<=verbose_level)
            return message;
//           errorMessage=message;
        if (prio<= verbose_level_log)
            writeLog(message,prio);
        return "";
    }

    public String throwMessage(String message,int prio)
    {
        if (prio<=verbose_level)
            return message;
//           errorMessage=message;
        if (prio<= verbose_level_log)
            writeLog("user:"+message,prio);
        return "";
    }

    @SuppressWarnings("empty-statement")
    public void doUploadLigands(ActionRequest request, ActionResponse response) {
        String errorMessage = "";
        String userMessage = "";
        String selected_wf = "";
        int docking_type = 0;
        try {
            // MULTIPART REQUEST
            ActionRequest temp_req = request;
            String userId = request.getRemoteUser();
            DiskFileItemFactory factory = new DiskFileItemFactory();
            PortletFileUpload pfu = new PortletFileUpload(factory);

            List fileItems = pfu.parseRequest(temp_req);

            // ligands
            Iterator iter = fileItems.iterator();
            FileItem ligandFileItem = null;
            FileItem receptorFileItem = null;
            FileItem configFileItem = null;
            FileItem inputsFileItem = null;
            String vina_configuration = "";
            String vina_best_result = "";
            String docking_wu = "";
            String task_name = "";
            FileItem gpfFileItem = null;
            FileItem dpfFileItem = null;
            throwMessage("Submitting started, processing inputs & files", 1);

            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (item.isFormField()) {
                    if (!Validate.textIsAlphaNumeric(item.getString()))
                        throw new Exception(" Input parameter string is not valid from FORM "+item.getFieldName()+", value:"+item.getString());
                    // TEXT FIELD
                    if (item.getFieldName().equals("vina_configuration")) {
                        vina_configuration = item.getString();
                    }
                    if (item.getFieldName().startsWith("docking_wu")) {
                        // number
                        docking_wu = item.getString();
                    }
                    if (item.getFieldName().startsWith("best_result")) {
                        vina_best_result = item.getString();
                    }
                    if (item.getFieldName().startsWith("task_name")) {
                        task_name = item.getString();
                    }
                    if (item.getFieldName().startsWith("docking_type")) {
                        docking_type = Integer.parseInt(item.getString());
                    }

                } else {
                    // FILE UPLOAD
                    String fieldName = item.getFieldName();
                    String fileName = item.getName();
                    long sizeInBytes = item.getSize();
                    // pdb or pdbqt
                    if (!Validate.fileNameValid(fileName))
                        throw new Exception(" Input parameter file is not valid from FORM "+fieldName);
                    // pdb or pdbqt
                    if (fieldName.equals("receptor")) {
                        receptorFileItem = item;
                    }
                    if (fieldName.equals("gpfparameter")) {
                        gpfFileItem = item;
                    }
                    if (fieldName.equals("dpfparameter")) {
                        dpfFileItem = item;
                    }
                    // .zip
                    if (fieldName.equals("ligands")) {
                        ligandFileItem = item;
                    }
                    // .pdb
                    if (fieldName.equals("ligand")) {
                        ligandFileItem = item;
                    }
                    // inputs.zip
                    if (fieldName.equals("inputs")) {
                        inputsFileItem = item;
                    }

                    if (fieldName.equals("vina_config_file")) {
                        configFileItem = item;
                    }
                }
            }

            throwMessage("Submitting started, checking inputs & uploading files", 1);
            String selected_wf_type = WfTypeFromDockingType(docking_type);

            if (!Validate.numberIsNaturalNumber(docking_wu, 1, Maximum_WU))
                    throw new Exception(" Input parameter docking_wu is not valid, value "+docking_wu+" Maximum_WU:"+Maximum_WU);
            if (!Validate.numberIsNaturalNumber(vina_best_result, 1, Integer.parseInt(docking_wu)))
                throw new Exception(" Input parameter best_result is not in valid range (1,"+ docking_wu+"), value: "+vina_best_result );

            //IMPORT NEW WORKFLOW INSTANCE
            String newWfinstanceID = selected_wf_type + "_" + task_name;
            selected_wf = importWorkflow(developerId, userId, selected_wf_type, newWfinstanceID);
            if (!Validate.wfInstanceValid(selected_wf))
                throw new Exception(" Input parameter  selected_wf is not valid, value "+selected_wf);

            String job_generator = "";
            String port_input_receptor = ""; //input-receptor.pdbqt
            String port_vina_config = "";  //vina-config.txt
            String port_input_ligands = ""; //input-ligands.zip
            String cmd_parameter_wus_nr = "" + docking_wu; // number of WUs

            String job_collector = "";
            String selected_job = "";
            String port_docking_gpf = "";
            String port_docking_dpf = "";
            String port_input_zip = "";


            // VINA
            if (docking_type == DOCKING_VINA) {
                job_generator = "Generator";
                port_input_receptor = "5"; //input-receptor.pdbqt
                port_vina_config = "4";  //vina-config.txt
                port_input_ligands = "3"; //input-ligands.zip
                job_collector = "collector.sh";
                selected_job = job_generator;
                //UPLOAD LIGAND ZIP
                File uploadedFile = asm_service.uploadFiletoPortalServer(ligandFileItem, userId, ligandFileItem.getName());
                asm_service.placeUploadedFile(userId, uploadedFile, selected_wf, selected_job, port_input_ligands);
                // UPLOAD RECEPTOR PDBQT FILE
                File receptorFile = asm_service.uploadFiletoPortalServer(receptorFileItem, userId, receptorFileItem.getName());
                asm_service.placeUploadedFile(userId, receptorFile, selected_wf, selected_job, port_input_receptor);
                // SETUP CONFIGURATION
                if (configFileItem == null) {
                    String content = vina_configuration;
                    asm_service.setInputText(userId, content, selected_wf, selected_job, port_vina_config);
                } else {
                    File configFile = asm_service.uploadFiletoPortalServer(configFileItem, userId, configFileItem.getName());
                    asm_service.placeUploadedFile(userId, configFile, selected_wf, selected_job, port_vina_config);
                }
            }
            if (docking_type == DOCKING_AUTODOCKAUTOGRID) {
                job_generator = "AutoGrid";
                port_input_receptor = "0"; //receptor.pdb
                port_docking_gpf = "1";
                String port_exec = "2";   //executables lol
                //port_vina_config = "2";  
                port_docking_dpf = "3"; // ligand.pdb
                port_input_ligands = "4"; // ligand.pdb
                job_collector = "Collector";
                selected_job = job_generator;

                //UPLOAD LIGAND
                File uploadedFile = asm_service.uploadFiletoPortalServer(ligandFileItem, userId, ligandFileItem.getName());
                asm_service.placeUploadedFile(userId, uploadedFile, selected_wf, selected_job, port_input_ligands);
                // UPLOAD RECEPTOR PDB FILE
                File receptorFile = asm_service.uploadFiletoPortalServer(receptorFileItem, userId, receptorFileItem.getName());
                asm_service.placeUploadedFile(userId, receptorFile, selected_wf, selected_job, port_input_receptor);
//
// SHOULD NOT BE IN PUBLIC VERSION!
//
                //UPLOAD EXEC
                //       File execFile = asm_service.uplloadFiletoPortalServer(execFileItem, userId, execFileItem.getName());
                //       asm_service.placeUploadedFile(userId, execFile , selected_wf, selected_job, port_exec);

                // UPLOAD GPF FILE
                File gpfFile = asm_service.uploadFiletoPortalServer(gpfFileItem, userId, gpfFileItem.getName());
                asm_service.placeUploadedFile(userId, gpfFile, selected_wf, selected_job, port_docking_gpf);
                // UPLOAD DPF FILE
                File dpfFile = asm_service.uploadFiletoPortalServer(dpfFileItem, userId, dpfFileItem.getName());
                asm_service.placeUploadedFile(userId, dpfFile, selected_wf, selected_job, port_docking_dpf);
            }
            if (docking_type == DOCKING_AUTODOCKNO) {
                job_generator = "Generator";
                port_input_zip = "1"; //inputs.zip
                port_docking_dpf = "3";
                job_collector = "Collector";
                selected_job = job_generator;

                //UPLOAD LIGAND
                File uploadedFile = asm_service.uploadFiletoPortalServer(inputsFileItem, userId, inputsFileItem.getName());
                asm_service.placeUploadedFile(userId, uploadedFile, selected_wf, selected_job, port_input_zip);

                // UPLOAD GPF FILE
                File dpfFile = asm_service.uploadFiletoPortalServer(dpfFileItem, userId, dpfFileItem.getName());
                asm_service.placeUploadedFile(userId, dpfFile, selected_wf, selected_job, port_docking_dpf);
            }

            // setup WUs Number
            // Generator
            asm_service.setCommandLineArg(userId, selected_wf, selected_job, cmd_parameter_wus_nr);
            // setup Best result number
            // at the collector job argument
            String best_content = vina_best_result;
            selected_job = job_collector;
            asm_service.setCommandLineArg(userId, selected_wf, selected_job, best_content);

            asm_service.submit(userId, selected_wf, "", "");

            throwMessage("Task successfully submitted! Docking_type:" + docking_type + " submitted", 1);
            userMessage += throwMessage("Task successfully submitted! Wait for run, and check eventually!", 1);
            response.setRenderParameter("userMessage", userMessage);


        } catch (Exception ex) {
            errorMessage += throwError(Thread.currentThread().getStackTrace()[1].getMethodName() + " " + ex.getMessage(), 1);
        }
            request.setAttribute("nextJSP", DISPLAY_PAGE);
            request.setAttribute("selected_wf", selected_wf);
            request.setAttribute("docking_type", docking_type);
            response.setRenderParameter("docking_type", "" + docking_type);

            response.setRenderParameter("errorMessage", errorMessage);
    }

    // delete molecule files from jmol/result
    private void runMoleculeCleanup(String contextPath)
    {
        // String contextPath=request.getContextPath();
        String pdbqtStorePath="../webapps"+contextPath+"/script/jmol/model/result/";
        String path = System.getProperty("user.dir")+"/";
        throwMessage("Cleanup # at: "+path+pdbqtStorePath,3);
        throwMessage("Cleanup # at: removing *.pdb and *.pdbqt",3);       
        //runSystemcmd("rm "+path+pdbqtStorePath+"*.pdb");
        //runSystemcmd("rm "+path+pdbqtStorePath+"*.pdbqt");
        throwMessage("Please cleanup manually",3);
    }

    public void writeWfID()
    {
                // write file WORKFLOW IDs
                //always give the path from root. This way it almost always works.
                String nameOfTextFile = "../webapps/wfids.txt";
                String str = "IMPORTED WORKFLOW IDs";
                try {
                    PrintWriter pw = new PrintWriter(new FileOutputStream(nameOfTextFile));
                    pw.println(str);

                //for (ASMWorkflow wf : asm_service.getASMWorkflows(userID)) {
                for (ASMWorkflow wf : asm_service.getASMWorkflows(developerId)) {
                    String wf_name=wf.getWorkflowName();
                    String wf_id=wf.getWorkflowID();
                    System.out.println(wf_name);

                    pw.println("name:" + wf_name + " id:"+ wf_id);

                }

                    pw.println(" --- REPOSITORY WFS --- (name ; id)");
                // for (ASMRepositoryItemBean asmb:asm_service.getWorkflowsFromRepository(userID, RepositoryItemTypeConstants.Application)){
                for (ASMRepositoryItemBean asmb:asm_service.getWorkflowsFromRepository(developerId, RepositoryItemTypeConstants.Application)){
                    String wf_type=asmb.getId()+"";
                    String logical_name=asmb.getItemID();
                    pw.println("name:" + logical_name + " ; id:"+ wf_type);
                }

                    pw.close();
                } catch(Exception ex) {
                     throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" "+ex.getMessage(),0);
                }
    }
  
    private String runSystemcmd(String[] cmd)
    {
        String string_error="";
        try{
                Process pr;
                if (cmd.length==1){
                    String command=cmd[0];
                    pr=Runtime.getRuntime().exec(command);
                }else{
                    pr=Runtime.getRuntime().exec(cmd);
                }
                //pr=null;
                throwMessage("systemCmd:"+cmd[0],10);
                BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                pr.waitFor();
                string_error=error.readLine();
                throwMessage("systemCmd error:"+string_error,5);
        } catch(Exception e) {
                throwError("CRITICAL "+Thread.currentThread().getStackTrace()[1].getMethodName()+" #" + cmd[0]+"  "+e.getMessage(),0);
                string_error=e.getMessage();
        }       
                return string_error;                
                
    }
    private String runSystemcmd(String cmd){
       return runSystemcmd(new String[]{cmd});
    }


    @SuppressWarnings("empty-statement")
    private ArrayList<String> getOutputfiles(String resultzipPath, String userId, String outputfileMask)
    {
        String temp_output = resultzipPath;
        String temp_filename = "wf_autodock_"+userId+"_out.zip";
        String temp_user_dir = "wf_autodock_"+userId+"_dir";
        String output_filename = outputfileMask;
        //"best.pdbqt";
        String string_error = "";
        ArrayList<String> resultPaths = new ArrayList<String>(1);
        try {
            runSystemcmd("rm -rf  "+ temp_dir+"/" + temp_user_dir);
            runSystemcmd("mkdir -p "+temp_dir+"/" + temp_user_dir);
            runSystemcmd("cp " + temp_output + " "+temp_dir+"/" + temp_filename);
            runSystemcmd("unzip -o "+temp_dir+"/" + temp_filename + " -d "+temp_dir+"/" + temp_user_dir);
            // MODIFIED TO SIMULATE MULTIPLE OUTPUT
            Process pr = Runtime.getRuntime().exec(new String[]{"find", temp_dir+"/" + temp_user_dir, "-name", output_filename});

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            String line = null;

            while ((line = input.readLine()) != null) {
                resultPaths.add(line);
            }

            string_error = error.readLine();
            throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" "+string_error,10);

        } catch (Exception ex) {
            throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" # "+ex.getMessage(),0);
            string_error = ex.getMessage();
        //throw new Exception("Grr");
        }
        return resultPaths;
    }

    @SuppressWarnings("empty-statement")

   public void doCheckStatus(ActionRequest request, ActionResponse response)
            throws PortletException, MalformedURLException, IOException
    {
        String msgError="";
        String msgUser="";

        String selected_job = "";
        String userId = request.getRemoteUser();
        String selected_wf=(String)request.getParameter("user_selected_instance");
        int selected_docking_type=0;
        try{
            if (!Validate.wfInstanceValid(selected_wf)) throw new Exception(" Invalid selected wf:"+selected_wf);
            selected_docking_type=DockingTypeFromSelectedInstance(selected_wf);
            response.setRenderParameter("docking_type", ""+selected_docking_type);

            String output_port="0";
            String status_str="";
            long errorNr=0;
            long finishedNr=0;
            long runningNr=0;

            try{
                status_str=asm_service.getWorkflowStatus(userId, selected_wf).getStatus();

                ArrayList<RunningJobDetailsBean> wfib= asm_service.getDetails(userId, selected_wf).getJobs();
///*
                for (RunningJobDetailsBean rjb : wfib) {
                    for (ASMJobInstanceBean ajib : rjb.getInstances()){
                        //String nr=ojb.getNumberofinstances();
                        /*
                         * INIT                  = "1";
                         * SUBMITTED             = "2";
                         * ACTIVE                = "14";
                         * WAITING               = "3";
                        statuses.put("5", "RUNNING");
                        statuses.put("6", "FINISHED");
                        statuses.put("7", "ERROR");
                         * */
                        // not show the local jobs (generator + collector)
                       // if (!ajib.getUsedResource().equals("dci-bridge host(64bit)"))
                        {
                            int statusNr = Integer.parseInt(ajib.getStatus());
                            throwMessage("RunningJobDetails Status:"+statusNr,10);
                            if ((statusNr <= 5)){
                                runningNr++;
                            }
                            if (statusNr == 6) {
                                finishedNr++;
                            }
                            if ((statusNr >=7)) {
                                errorNr++;
                            }
                        }
                    }
                }
            }catch(Exception ex)
            {
               msgError+=throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" "+ex.getMessage(), 10) ;
            }

            if (status_str.equals("FINISHED")){
                request.setAttribute("result_finished", "1");
                if (selected_docking_type==DOCKING_VINA){
                    selected_job = "collector.sh";
                }else{
                    selected_job = "Collector";
                }

                String msg="";
                String receptor_ref="",best_ref="";
                String temp_output = asm_service.getFiletoPortalServer(userId, selected_wf, selected_job, output_port);


                // EXTRACT THE OUTPUTVALUE FROM RESULTZIP FILE
                ArrayList<String> resultPaths=null;
                if (selected_docking_type==DOCKING_VINA){
                    resultPaths= getOutputfiles(temp_output, userId, "*.pdbqt");
                    receptor_ref="receptor.pdbqt";
                    best_ref="best.pdbqt";
                }
                if (selected_docking_type==DOCKING_AUTODOCKAUTOGRID){
                    resultPaths= getOutputfiles(temp_output, userId, "*");
                    //resultPaths= getOutputfiles(temp_output, userId, "*.pdb");
                    // receptor named 0
                    receptor_ref="inputs/0/0";
                    best_ref="best.pdb";
                }
                if (selected_docking_type==DOCKING_AUTODOCKNO){
                    resultPaths= getOutputfiles(temp_output, userId, "*.pdb");
                    // receptor and other inputs are zipped
                    receptor_ref="receptor.pdb";
                    best_ref="best.pdb";
                }

                // MOVE RESULTS to PDBQT dir

                String pdbqtStorePath="../webapps"+request.getContextPath()+"/script/jmol/model/result/";
                String path = System.getProperty("user.dir")+"/";

                ArrayList<String> pdbqtList=new ArrayList<String>(resultPaths.size());
                ArrayList<MoleculeStore> moleculeList=new ArrayList<MoleculeStore>(resultPaths.size());

                // if the Workflow produce 1 best molecule file, contains more..
                MoleculeStore receptorMolecule=null;
                MoleculeStore receptorMoleculeCombined=null;                
                // if (resultPaths.size()==2)
                {
                    String receptorFile="";
                    String ligandsFile="";
                    for(String filepath:resultPaths){
                        if (filepath.endsWith(receptor_ref)){
                              receptorFile=filepath;
                        }
                        if (filepath.endsWith( best_ref)){
                              ligandsFile=filepath;
                        }
                    }
                    // separate ligands
                    {
                        String temp_split_dir = temp_dir+"/wf_autodock_"+userId+"_dir/";
                        MoleculeStore[] bestMolecules=MoleculeStore.splitMultiMolecule(ligandsFile, temp_split_dir);

                        for(MoleculeStore m:bestMolecules)
                        {
                            m.mvMoleculeToScriptDir(path+pdbqtStorePath);
                            moleculeList.add(new MoleculeStore(pdbqtStorePath,m.getFileName()));
                        }
                    }
                    if (!ligandsFile.isEmpty()){
                    }
                    if (!receptorFile.isEmpty()){try{
                            // merge ligands with receptor
                            receptorMolecule=new MoleculeStore(receptorFile);
                            receptorMolecule.mvMoleculeToScriptDir(path+pdbqtStorePath, true);
                            // receptor -> best.pdbqt
                            MoleculeStore.MergeMolecules(receptorFile, ligandsFile);
                            receptorMoleculeCombined=new MoleculeStore(receptorFile);
                            receptorMoleculeCombined.mvMoleculeToScriptDir(path+pdbqtStorePath,true);
                        }catch(Exception ex){
                            msgError+=throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" Processing input Receptor file "+ex.getMessage(),1);
                        }
                    }
                }
                //throwMessage("found results: "+ moleculeList.size() +" #"+msg,10);
                if (Maximum_Visualize==moleculeList.size())                {
                    msgUser+=throwMessage(" Results number reached the Maximum Value:"+Maximum_Visualize+", ",1);
                }
                msgUser+=throwMessage("found results: #"+ moleculeList.size(),1);

                request.setAttribute("result_molecules", moleculeList);
                request.setAttribute("result_receptor", receptorMolecule);
                request.setAttribute("result_receptor_combined", receptorMoleculeCombined);

            }else{
                request.setAttribute("resultpdbqts", "");
            }
                request.setAttribute("jobs_errornr", errorNr+"");
                request.setAttribute("jobs_finishednr", finishedNr+"");
                request.setAttribute("jobs_runningnr", runningNr+"");
            // allow download logs
            //if (status_str.equals("ERROR"))
            {
                request.setAttribute("result_finished", "1");
            }
                if (status_str.contains("WORKFLOW_SUSPEND")){
                    asm_service.rescue(userId, selected_wf);
                }
        }catch(Exception ex)
        {
             msgError+=throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+ex.getMessage(),10);
        }
            request.setAttribute("selected_wf",  selected_wf );
            request.setAttribute("layerMoleculaVisualizeEnableVar","1");
            request.setAttribute("nextJSP",  DISPLAY_PAGE);
            response.setRenderParameter("userMessage",  msgUser);
            response.setRenderParameter("errorMessage",  msgError);
            String nextJSP= DISPLAY_PAGE;
            PortletRequestDispatcher dispatcher;
            dispatcher = getPortletContext().getRequestDispatcher(nextJSP);
            dispatcher.include(request, response);
    }


    @SuppressWarnings("empty-statement")
    public void notdoDownloadOutput(ResourceRequest request, ResourceResponse response) {
      String userId = request.getRemoteUser();
      String selected_wf = request.getParameter("user_selected_instance");
      response.setContentType("application/zip");
      response.setProperty("Content-Disposition", "inline; filename=\"" + selected_wf + "_enduser_outputs.zip\"");
      asm_service.getFileStream(userId, selected_wf, "collector.sh", null, response);
      request.setAttribute("selected_wf",  selected_wf );
    }



    @SuppressWarnings("empty-statement")
    public void doReSubmit(ActionRequest request, ActionResponse response)
            throws PortletException, MalformedURLException, IOException {
        String msgError = "";
        String msgUser = "";
        String selected_wf = "";
        int docking_type = 0;
        try {
            String userId = request.getRemoteUser();
            selected_wf = request.getParameter("user_selected_instance");
            if (!Validate.wfInstanceValid(selected_wf)) {
                throw new Exception(" Invalid selected workflow " + selected_wf);
            }
            docking_type = DockingTypeFromSelectedInstance(selected_wf);

            asm_service.submit(userId, selected_wf);
            throwMessage("Workflow re-started " + selected_wf, 10);
        } catch (Exception ex) {
            msgError += throwError(Thread.currentThread().getStackTrace()[1].getMethodName() + ex.getMessage(), 1);
        }
        request.setAttribute("selected_wf", selected_wf);
        response.setRenderParameter("errorMessage", msgError);
        response.setRenderParameter("userMessage", msgUser);
        response.setRenderParameter("docking_type", "" + docking_type);
    }

    @SuppressWarnings("empty-statement")
    public void doContinue(ActionRequest request, ActionResponse response)
            throws PortletException, MalformedURLException, IOException {
        String msgError = "";
        String msgUser = "";
        String selected_wf = "";
        int docking_type = 0;
        try {
            String userId = request.getRemoteUser();
            selected_wf = request.getParameter("user_selected_instance");
            if (!Validate.wfInstanceValid(selected_wf)) {
                throw new Exception(" Invalid selected workflow " + selected_wf);
            }
            docking_type = DockingTypeFromSelectedInstance(selected_wf);

            asm_service.rescue(userId, selected_wf);
            throwMessage("Workflow re-started " + selected_wf, 10);
        } catch (Exception ex) {
            msgError += throwError(Thread.currentThread().getStackTrace()[1].getMethodName() + ex.getMessage(), 1);
        }
        request.setAttribute("selected_wf", selected_wf);
        response.setRenderParameter("errorMessage", msgError);
        response.setRenderParameter("userMessage", msgUser);
        response.setRenderParameter("docking_type", "" + docking_type);
    }

    @SuppressWarnings("empty-statement")
    public void notdoChangeLigand(ActionRequest request, ActionResponse response)
            throws PortletException, MalformedURLException, IOException {
        ;
    }

    private String WfTypeFromDockingType(int docking_type)
    {
        String selected_wf_type="";
                switch(docking_type){
                    case 1: selected_wf_type=wf_type_vina; break;
                    case 2: selected_wf_type=wf_type_autodockautogrid; break;
                    case 3: selected_wf_type=wf_type_autodockno; break;
                }
        return selected_wf_type;
    }

    private int DockingTypeFromSelectedInstance(String selected_wf)throws Exception{
        int selected_docking_type=0;
            if (selected_wf.startsWith(wf_type_vina)) {selected_docking_type=DOCKING_VINA;};
            if (selected_wf.startsWith(wf_type_autodockautogrid)) {selected_docking_type=DOCKING_AUTODOCKAUTOGRID;};
            if (selected_wf.startsWith(wf_type_autodockno)) {selected_docking_type=DOCKING_AUTODOCKNO;};
            if (selected_docking_type==0){ throw new Exception("Docking type not recognised:"+selected_wf);}
         return selected_docking_type;
    }

    @SuppressWarnings("empty-statement")
    public void doChangeDockingType(ActionRequest request, ActionResponse response)
            throws PortletException, MalformedURLException, IOException {
        ;
               int docking_type=1;
               if (request.getParameter("docking_type") != null) {
                    request.setAttribute("docking_type", request.getParameter("docking_type"));
                    docking_type=Integer.parseInt(request.getParameter("docking_type"));
                }

                String selected_wf_type=WfTypeFromDockingType(docking_type);
                request.setAttribute("selected_wf_type",  selected_wf_type );
                response.setRenderParameter("docking_type", ""+docking_type);
    }

    /**
     * View user notify settings informations...
     */
    @SuppressWarnings("empty-statement")
    public void doView(RenderRequest request, RenderResponse response) throws PortletException {
        try {

            String userId = request.getRemoteUser();
            String selected_wf = request.getParameter("user_selected_instance");
            String userMessage="";
            int docking_type=1;
            try{
                docking_type =  Integer.parseInt(request.getParameter("docking_type"));
            }catch(Exception ex){
                    ;
            }
            String types= wf_type_vina + wf_type_autodockautogrid + wf_type_autodockno;
            //types="000";
            // probably this is the first init
            if ((types.length()<=3)|| (types.equals("000")))
            {
                writeWfID();
                readConfig(request.getContextPath());
                Validate.setWf_types(new String[]{wf_type_vina,wf_type_autodockautogrid,wf_type_autodockno});
                // do cleanup
                runMoleculeCleanup(request.getContextPath());
                userMessage+="Portlet configured and initialized.";
            }
            try {
                PortletSession portletSession = request.getPortletSession();

                request.setAttribute("owners", asm_service.getWorkflowDevelopers(RepositoryItemTypeConstants.Application));
                if (request.getParameter("owner") != null) {
                    String owner = request.getParameter("owner");
                    request.setAttribute("WorkflowList", asm_service.getWorkflowsFromRepository(owner, RepositoryItemTypeConstants.Application));
                    request.setAttribute("rep_owner", request.getParameter("owner"));
                }
                String workflowtobedetailed = (String) request.getParameter("getDetailsforWorkflow");
                if (workflowtobedetailed != null) {
                    try {
                        WorkflowInstanceBean wrkdetails = asm_service.getDetails(userId, workflowtobedetailed);
                        request.setAttribute("statusconstants", new StatusConstants());
                        request.setAttribute("statuscolors", new StatusColorConstants());
                        request.setAttribute("workflow_details", wrkdetails);
                        request.setAttribute("selected_Instance", workflowtobedetailed);

                    } catch (Exception ex) {
                        System.out.println("no RuntimeID");
                        ex.printStackTrace();
                    }
                }

                //ArrayList avail_wfs = asm_service.getWorkflows(userId);
                request.setAttribute("asm_instances", asm_service.getASMWorkflows(userId));
                request.setAttribute("portalID", asm_service.PORTAL);


                if (request.getParameter("userMessage")!=null) userMessage+=""+request.getParameter("userMessage");
                
                request.setAttribute("userMessage", userMessage);
                request.setAttribute("errorMessage", request.getParameter("errorMessage"));


               if (request.getParameter("docking_type") != null) {
                    request.setAttribute("docking_type", request.getParameter("docking_type"));
                    docking_type=Integer.parseInt(request.getParameter("docking_type"));
                }else{
                    request.setAttribute("docking_type", docking_type);
                }
                String selected_wf_type="";
               // errorMessage="";
               // userMessage="";
                switch(docking_type){
                    case 1: selected_wf_type=wf_type_vina; break;
                    case 2: selected_wf_type=wf_type_autodockautogrid; break;
                    case 3: selected_wf_type=wf_type_autodockno; break;
                }

                
                request.setAttribute("selected_wf_type",  selected_wf_type );
                //request.setAttribute("selected_wf",  selected_wf );

                request.setAttribute("userID", userId);
                request.setAttribute("storageurl", asm_service.STORAGE);
                if (request.getParameter("command_line") != null) {
                    request.setAttribute("command_line_text", request.getParameter("command_line"));
                    request.setAttribute("content", request.getParameter("command_line"));
                }
                if (request.getParameter("resourcebean") != null) {
                    request.setAttribute("dciresourcequeue", request.getParameter("resourcebean"));
                    request.setAttribute("content", request.getParameter("resourcebean"));
                }
                if (request.getParameter("remotepath") != null) {
                    request.setAttribute("remotepath", request.getParameter("remotepath"));
                    request.setAttribute("content", request.getParameter("remotepath"));
                }
                if (request.getParameter("nodeNumber") != null) {
                    request.setAttribute("content", request.getParameter("nodeNumber"));
                }
                if (request.getParameter("act_workflowID") != null) {
                    request.setAttribute("act_workflowID", request.getParameter("act_workflowID"));
                }

            } catch (Exception ex) {
                throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" # "+ex.getMessage(),0);
            }

            // Setting next page
            String nextJSP = (String) request.getParameter("nextJSP");
            if (nextJSP == null) {
                nextJSP = DISPLAY_PAGE;
            }

            PortletRequestDispatcher dispatcher;
            dispatcher = getPortletContext().getRequestDispatcher(nextJSP);
            dispatcher.include(request, response);
        } catch (IOException ex) {
            throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" "+ex.getMessage(),10);
            Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void notdoGetWorkflowsFromRepository(ActionRequest request, ActionResponse response) throws PortletException {
        String owner = request.getParameter("owner").toString();
        response.setRenderParameter("owner", owner);
    }

    public void notdoImportWorkflow(ActionRequest request, ActionResponse response) throws PortletException {
        try {
            String impWfType = RepositoryItemTypeConstants.Application;
            String impItemId = request.getParameter("impItemId");
            String owner = request.getParameter("rep_owner").toString();

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat udf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String projectName = impItemId + "_" + udf.format(cal.getTime());

           asm_service.ImportWorkflow(
                    request.getRemoteUser(),
                    projectName,
                    owner,
                    impWfType,
                    impItemId);
        } catch (Exception ex) {
            throwError(Thread.currentThread().getStackTrace()[1].getMethodName()+" # "+ex.getMessage(),0);
        }
    }

    public void doConfigure(ActionRequest request, ActionResponse response) throws PortletException {
        response.setRenderParameter("nextJSP", DISPLAY_PAGE);
        writeWfID();
        readConfig("../webapps/"+request.getContextPath());
        Validate.setWf_types(new String[]{wf_type_vina,wf_type_autodockautogrid,wf_type_autodockno});
    }


    public void notdoUpload(ActionRequest request, ActionResponse response) throws PortletException {
        try {
            String jobport = "";
            String selected_wf = "";
            //getting upload parameters
            ActionRequest temp_req = request;

            DiskFileItemFactory factory = new DiskFileItemFactory();
            PortletFileUpload pfu = new PortletFileUpload(factory);

            List fileItems = pfu.parseRequest(temp_req);

            Iterator iter = fileItems.iterator();
            FileItem file2upload = null;
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();

                // retrieve hidden parameters if item is a form field
                if (item.isFormField()) {
                    if (item.getFieldName().equals(new String("where2upload"))) {
                        jobport = item.getString();
                    }
                    if (item.getFieldName().startsWith(new String("instance_upload"))) {
                        selected_wf = item.getString();
                    }
                } else {
                    file2upload = item;
                }
            }

            String[] splitted = jobport.split("@");
            String selected_job = splitted[0];

            String selected_port = splitted[1];

            String userId = request.getRemoteUser();

            File uploadedFile = asm_service.uploadFiletoPortalServer(file2upload, userId, file2upload.getName());
            asm_service.placeUploadedFile(request.getRemoteUser(), uploadedFile, selected_wf, selected_job, selected_port);


        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    public void doDelete(ActionRequest request, ActionResponse response) throws PortletException {
        String msgError="";
        String msgUser="";
        try{
            msgUser+=throwMessage(Thread.currentThread().getStackTrace()[1].getMethodName()+"  ",10);
            String userID = (String) request.getRemoteUser();
            String selected_wf = request.getParameter("user_selected_instance");
            if (!Validate.wfInstanceValid(selected_wf)) throw new Exception(" instance invalid "+ selected_wf);
            int docking_type=Integer.parseInt(request.getParameter("docking_type"));
            response.setRenderParameter("docking_type", ""+docking_type);
            asm_service.DeleteWorkflow(userID, selected_wf);
            msgUser+=throwMessage(Thread.currentThread().getStackTrace()[1].getMethodName()+"  Ok ",10);
        }catch(Exception ex)
        {
            msgError+=throwError("DELETE "+ex.getMessage(), 1);
        }
        response.setRenderParameter("userMessage",  msgUser);
        response.setRenderParameter("errorMessage",  msgError);
    }

    public void notdoDetails(ActionRequest request, ActionResponse response)
            throws PortletException {

        String workflowID = request.getParameter("user_selected_instance");
        response.setRenderParameter("nextJSP", DETAILS_PAGE);
        response.setRenderParameter("getDetailsforWorkflow",workflowID);


        /*
        request.setAttribute("asm_instances",
                asm_service.getASMWorkflows(
                (String) request.getRemoteUser()));
        request.getPortletSession().setAttribute("nextJSP", DETAILS_PAGE);
        request.setAttribute("nextJSP", DETAILS_PAGE);
         */
        /*
         try {
         PortletRequestDispatcher dispatcher =
         getPortletContext().getRequestDispatcher(DETAILS_PAGE);
         response.setRenderParameter("nextJSP", DETAILS_PAGE);
         dispatcher.forward(request, response);
            
            
         } catch (IOException e) {
         Logger.getLogger(ASM_SamplePortlet.class.getName()).log(
         Level.WARNING,
         null,
         e);
         }*/
    }

    public void notdoGetInput(ActionRequest request, ActionResponse response) throws PortletException {
        String userID = (String) request.getRemoteUser();
        String selected_wf = request.getParameter("user_selected_instance");
        String selected_job = "add";


        String actual_command_line = asm_service.getCommandLineArg(userID, selected_wf, selected_job);
        response.setRenderParameter("command_line", actual_command_line);
        response.setRenderParameter("act_workflowID", selected_wf);
    }

    public void notdoGetResource(ActionRequest request, ActionResponse response) throws PortletException {
        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");
        String selectedJob = "add";


        ASMResourceBean resourcebean = asm_service.getResource(userID, selectedWf, selectedJob);
        response.setRenderParameter("resourcebean", resourcebean.getType() + "/" + resourcebean.getGrid() + "/" + resourcebean.getResource() + "/" + resourcebean.getQueue());
        response.setRenderParameter("act_workflowID", selectedWf);
    }

    public void notdoGetRemoteInputPath(ActionRequest request, ActionResponse response) throws PortletException {
        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");
        String selectedJob = "add";
        String selectedPort = "0";


        String remotepath = asm_service.getRemoteInputPath(userID, selectedWf, selectedJob, selectedPort);
        if (remotepath != null) {
            response.setRenderParameter("remotepath", remotepath);
        }
        response.setRenderParameter("act_workflowID", selectedWf);
    }

    public void notdoGetNodeNumber(ActionRequest request, ActionResponse response) throws PortletException {
        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");
        String selectedJob = "add";
        String nodeNumber = asm_service.getNodeNumber(userID, selectedWf, selectedJob);

        response.setRenderParameter("nodeNumber", nodeNumber);
        response.setRenderParameter("act_workflowID", selectedWf);

    }

    public void notdoSetNodeNumber(ActionRequest request, ActionResponse response) throws PortletException {

        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");
        String nodeNumber2Set = request.getParameter("content");
        String selectedJob = "add";
        try {
            int nodeNumber = Integer.parseInt(nodeNumber2Set);

            asm_service.setNodeNumber(userID, selectedWf, selectedJob, nodeNumber);
        } catch (NotMPIJobException notmpi) {
            System.out.println("Job is not an MPI application");
        } catch (NumberFormatException ex) {
        }


    }

    public void notdoGetRemoteOutputPath(ActionRequest request, ActionResponse response) throws PortletException {
        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");
        String selectedJob = "add";
        String selectedPort = "1";


        String remotepath = asm_service.getRemoteOutputPath(userID, selectedWf, selectedJob, selectedPort);
        if (remotepath != null) {
            response.setRenderParameter("remotepath", remotepath);
        }
        response.setRenderParameter("act_workflowID", selectedWf);
    }

    public void notdoSubmit(ActionRequest request, ActionResponse response) throws PortletException {
        try {
            String userID = (String) request.getRemoteUser();
            String selectedWf = request.getParameter("selected_workflow");
            String notifyText = request.getParameter("notifyText");
            String notifyType = request.getParameter("notifyType");
            System.out.println("Workflow: " + selectedWf + " notText:" + notifyText + " notifyType:" + notifyType);

            asm_service.submit(userID, selectedWf, notifyText, notifyType);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void notdoSetInput(ActionRequest request, ActionResponse response) throws PortletException {

        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");

        String actual_command_line = request.getParameter("content");
        String selectedJob = "add";
        asm_service.setCommandLineArg(userID, selectedWf, selectedJob, actual_command_line);
    }

    public void notdoSetRemoteInputPath(ActionRequest request, ActionResponse response) throws PortletException {

        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");

        String remotepath = request.getParameter("content");
        String selectedJob = "add";
        String selectedPort = "0";


        asm_service.setRemoteInputPath(userID, selectedWf, selectedJob, selectedPort, remotepath);

    }

    public void notdoSetRemoteOutputPath(ActionRequest request, ActionResponse response) throws PortletException {

        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");

        String remotepath = request.getParameter("content");
        String selectedJob = "add";
        String selectedPort = "1";


        asm_service.setRemoteOutputPath(userID, selectedWf, selectedJob, selectedPort, remotepath);

    }

    public void notdoSetResource(ActionRequest request, ActionResponse response) throws PortletException {

        String userID = (String) request.getRemoteUser();
        String selectedWF = request.getParameter("user_selected_instance");
        String selectedJob = "add";
        String dciresourcequeue = request.getParameter("content");
        String type = dciresourcequeue.split("/")[0];
        String grid = dciresourcequeue.split("/")[1];
        String resource = dciresourcequeue.split("/")[2];
        String queue = dciresourcequeue.split("/")[3];

        asm_service.setResource(userID, selectedWF, selectedJob, type, grid, resource, queue);

    }
}
