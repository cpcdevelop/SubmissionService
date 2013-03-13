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
import hu.sztaki.lpds.pgportal.services.asm.beans.ASMRepositoryItemBean;
import hu.sztaki.lpds.pgportal.services.asm.beans.ASMResourceBean;
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



class ClientError extends Exception {
}

interface UserInput {

    public void validate() throws ClientError;
}

class MentalRayInput implements UserInput {

    public MentalRayInput(String workunits,
            String firstFrame,
            String lastFrame,
            String mayaSceneFileName,
            FileItem projectZip,
            String imageFormat) {
        this.workunits = Integer.getInteger(workunits);
        this.firstFrame = Integer.getInteger(firstFrame);
        this.lastFrame = Integer.getInteger(lastFrame);
        this.mayaSceneFileName = mayaSceneFileName;
        this.projectZip = projectZip;
        this.imageFormat = imageFormat;
    }
    private int workunits;
    private int firstFrame;
    private int lastFrame;
    private String mayaSceneFileName;
    private FileItem projectZip;
    private String imageFormat;
    private static final String validImageFormats[] = {"SI", "SOFT", "SOFTIMAGE", "GIF", "RLA", "WAVE", "WAVEFRONT", "TIFF", "TIF", "TIFF16", "TIF16", "SGIRGB", "SGI16", "RGB16", "PIX", "IFF", "TDI", "EXPLOREMAYA", "JPEG", "JPG", "EPS", "MAYA16", "IFF16", "CINEON", "CIN", "FIDO", "QTL", "QUANTEL", "TGA", "TARGA", "BMP"};
    private static final int MAX_WU = 40;

    Boolean invalidFormat() {
        for (String format : validImageFormats) {
            if (format.equals(imageFormat.toUpperCase())) {
                return false;
            }
        }
        return true;
    }

    Boolean sceneIsNotPresentInProject() {


        return false;
    }

    public void validate() throws ClientError {
        if (firstFrame <= 0 || firstFrame > lastFrame
                || workunits < 0 || workunits > MAX_WU
                || invalidFormat() || sceneIsNotPresentInProject()) {
            throw new ClientError();
        }
    }
}



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

    private String DISPLAY_PAGE = "/jsp/asm_sample/asmsample.jsp";
    private static final String DETAILS_PAGE =
            "/jsp/asm_sample/details.jsp";
    ASMService asm_service = null;
    private String selected_wf="";
            //"122_20121026183313187_2012-10-26-183313";
    private String selected_wf_type="";
            //"122"
    private String errorMessage="";

    public ASM_SamplePortlet() {
        asm_service = ASMService.getInstance();

    }

    public void readConfig()
    {
        try{
            SymbolMap symbolmap = new SymbolMap(new File("../webapps/autodock_config.xml"));
            String readWfType=symbolmap.lookupSymbol("AutodockVina");
            selected_wf_type=readWfType.substring(0, 3);
            if (!readWfType.equals(selected_wf_type)) {
                throwError("invalid XML configuration file!", 1);
            }
        //String v2=symbolmap.lookupSymbol("Autodock4");
        //String v3=symbolmap.lookupSymbol("Autodock4noautogrid");
        }catch(Exception ex)
        {
            System.out.println("processaction called...");
        }
    }

    /**
     * Handling generic actions
     */
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException {
        asm_service.init();

        System.out.println("processaction called..."+selected_wf);
        String action = "";
        action = request.getParameter("action");

        boolean isMultipart = PortletFileUpload.isMultipartContent(request);
        if (isMultipart) {


            doUploadLigands(request, response);
     //       doUpload(request,response);
   /*
            // PARSE MULTIPART HTML POST
            Hashtable<String,String> itemParameters=new Hashtable<String, String>();
            List<FileItem> items=null;

            ActionRequest temp_req=request;
            try {
                items = new PortletFileUpload(new DiskFileItemFactory()).parseRequest(temp_req);
            } catch (FileUploadException ex) {
                Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (FileItem item : items) {
                if (item.isFormField()) {
                    String fieldname = item.getFieldName();
                    String fieldvalue = item.getString();
                    itemParameters.put(fieldname, fieldvalue);
                }
            }
            action=itemParameters.get("action");
            if (action.equals("doUpload"))
            {
               // doUpload(request,response);
            }
            if (action.equals("doUploadLigands"))
            {
               // doUploadLigands(request,response);
            }
        */
            action=null;

        }
            if ((request.getParameter("action") != null) && (!request.getParameter("action").equals(""))) {
                action = request.getParameter("action");
            }
            System.out.println("*************" + action + "::" + request.getParameter("action"));
            if (action != null) {
                try {
                    Method method = this.getClass().getMethod(action, new Class[]{ActionRequest.class, ActionResponse.class});
                    method.invoke(this, new Object[]{request, response});
                } catch (NoSuchMethodException e) {
                    System.out.println("-----------------------No such method");//+(""+request.getAttribute(SportletProperties.ACTION_EVENT)).split("=")[1]);
                } catch (IllegalAccessException e) {
                    System.out.println("----------------------Illegal access");
                } catch (InvocationTargetException e) {
                    System.out.println("-------------------Invoked function failed");
                    e.printStackTrace();
                }
            }else{
                System.out.println("[ERROR] Wrong request format");
            }

    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

        String userID = request.getRemoteUser();
        String jobport = request.getParameter("file2download");
        Enumeration paramNames = request.getParameterNames();
        String selected_wf = "";
        while (paramNames.hasMoreElements()) {
            String act_param = (String) paramNames.nextElement();
            if (act_param.startsWith("instance_download")) {
                selected_wf = request.getParameter(act_param);
            }
        }

        String[] splitted = jobport.split("@");
        String selected_job = splitted[0];
        try {
            response.setContentType("application/zip");
            response.setProperty("Content-Disposition", "inline; filename=\"" + selected_wf + "_enduser_outputs.zip\"");

            asm_service.getFileStream(userID, selected_wf, selected_job, null, response);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public String importWorkflow(String owner, String remoteUser, String impItemId, String instanceName) throws PortletException {
        String returnInsertedID="";
        try {
            String impWfType = RepositoryItemTypeConstants.Application;
            //String impItemId = request.getParameter("impItemId");
            //String owner = request.getParameter("rep_owner").toString();

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat udf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String projectName = impItemId + "_" + udf.format(cal.getTime());
            // request.getRemoteUser(),
            asm_service.ImportWorkflow(
                    remoteUser,
                    instanceName,
                    owner,
                    impWfType,
                    impItemId);
            ArrayList<ASMWorkflow> awf=asm_service.getASMWorkflows(owner);
            int awf_size=awf.size();
            returnInsertedID=awf.get(awf_size-1).getWorkflowName();

        } catch (Exception ex) {
            Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnInsertedID;
    }

    public void doChangeSelectedwf(ActionRequest request, ActionResponse response) throws PortletException, IOException
    {
        try{
            String sentSelectedWf=(String)request.getParameter("user_selected_instance");
            selected_wf=sentSelectedWf;
            doCheckStatus(request, response);
        }catch (Exception ex) {
            Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
    
    public void throwError(String message,int prio)
    {
        System.out.print(prio+"# [ERROR] "+message);
        Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.ALL, message);
        errorMessage=message;
    }


    @SuppressWarnings("empty-statement")
    public void doUploadLigands(ActionRequest request, ActionResponse response) {
    try{
            // MULTIPART REQUEST
            ActionRequest temp_req = request;

            DiskFileItemFactory factory = new DiskFileItemFactory();
            PortletFileUpload pfu = new PortletFileUpload(factory);

            List fileItems = pfu.parseRequest(temp_req);

            // ligands
            Iterator iter = fileItems.iterator();
            ArrayList<FileItem> file2upload = new ArrayList<FileItem>();
            FileItem receptorFileItem=null;
            FileItem configFileItem=null;
            String vina_configuration="";
            String vina_best_result="";
            String rep_owner="";
            String task_name="";

            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (item.isFormField()) {
                    // TEXT FIELD
                    if (item.getFieldName().equals("vina_configuration")) {
                        vina_configuration=item.getString();
                    }
                    if (item.getFieldName().startsWith("vina_best_result")) {
                        vina_best_result = item.getString();
                    }
                    if (item.getFieldName().startsWith("rep_owner")) {
                        rep_owner = item.getString();
                    }
                    if (item.getFieldName().startsWith("task_name")) {
                        task_name = item.getString();
                    }

                } else {
                    // FILE UPLOAD
                    String fieldName = item.getFieldName();
                    String fileName = item.getName();
                    long sizeInBytes = item.getSize();
                    if (fieldName.equals("receptor")){
                        receptorFileItem=item;
                    }
                    if (fieldName.equals("ligands")){
                        file2upload.add(item);
                    }
                    if (fieldName.equals("vina_config_file")){
                        configFileItem=item;
                    }
                }
            }

            //IMPORT NEW WORKFLOW INSTANCE
           // selected_wf_type="122";
            String newWfinstanceID=selected_wf_type+"_"+task_name;
            String ruser=request.getRemoteUser();
            rep_owner=ruser;
            String userId =rep_owner;
            String rowner=request.getParameter("owner");
            selected_wf=importWorkflow(rep_owner, ruser, selected_wf_type, newWfinstanceID);

            if (selected_wf.length()<3){
                throwError("Import "+newWfinstanceID+" workflow is failed", 1);
            }

            String job_generator = "Generator";

            String port_input_receptor = "5"; //input-receptor.pdbqt
            String port_vina_config = "4";  //vina-config.txt
            String port_input_ligands = "3"; //input-ligands.zip
            String cmd_parameter_wus_nr = ""; // number of WUs


            String job_collector = "collector.sh";

            String cmd_parameter_best = ""; // number of Best results

            // UPLOAD LIGAND ZIP FILE

            String selected_job=job_generator;
            String selected_port= port_input_ligands;
            

            for(FileItem f:file2upload){
                if (f.getName().endsWith(".zip"))
                {
                    File uploadedFile = asm_service.uploadFiletoPortalServer(f, userId, f.getName());
                    asm_service.placeUploadedFile(userId, uploadedFile , selected_wf, selected_job, selected_port);
                }
            }

/*
            // UPLOAD MULTIPLE PDBQT LIGAND FILE
            String ligandDir="/tmp/wf_dir_ligands";
            String zipName="upload.zip";

            Runtime.getRuntime().exec("rm -rf "+ligandDir+"/"+zipName);

            for(FileItem f:file2upload){
                File uploadedFile = asm_service.uploadFiletoPortalServer(f, userId, f.getName());
                System.out.println("jaj"+uploadedFile.getAbsolutePath());
                // PREPARE UPLOADED FILES to SUBMIT MOVE AND COMPRESS
                Runtime.getRuntime().exec("cp "+uploadedFile+" "+ligandDir);
                Runtime.getRuntime().exec("zip "+ligandDir+"/"+zipName+" "+ligandDir+"/"+uploadedFile.getName());
            }

            File ligandsFile=new File(ligandDir+"/"+zipName);
            selected_port = "0";
            asm_service.placeUploadedFile(userId, ligandsFile, selected_wf, selected_job, selected_port);
*/

            // UPLOAD RECEPTOR PDBQT FILE
            selected_port= port_input_receptor;

            File receptorFile =  asm_service.uploadFiletoPortalServer(receptorFileItem, userId, receptorFileItem.getName());
            asm_service.placeUploadedFile(userId, receptorFile, selected_wf, selected_job, selected_port);

            // SETUP CONFIGURATION
            if (configFileItem==null){
                String content=vina_configuration;
                selected_port= port_vina_config;
                asm_service.setInputText(userId, content, selected_wf, selected_job, selected_port);
            }else{
                selected_port= port_vina_config;
                File configFile =  asm_service.uploadFiletoPortalServer(configFileItem, userId, configFileItem.getName());
                asm_service.placeUploadedFile(userId, configFile, selected_wf, selected_job, selected_port);
            }
            // WUs number
            String wu_content="5";
            asm_service.setCommandLineArg(userId, selected_wf, selected_job, wu_content);
            // setup Best result number, at the collector job argument
            String best_content=vina_best_result;
            selected_job=job_collector;
            asm_service.setCommandLineArg(userId, selected_wf, selected_job, best_content);

            asm_service.submit(userId, selected_wf, "", "");

            request.setAttribute("nextJSP",  DISPLAY_PAGE);

            throwError("Wow! Submitted task! Success! Wait for run, and check eventually!", 10);
            String nextJSP= DISPLAY_PAGE;
            PortletRequestDispatcher dispatcher;
            dispatcher = getPortletContext().getRequestDispatcher(nextJSP);
            dispatcher.include(request, response);

    }catch(Exception ex)
    {
       throwError(ex.getMessage(),10);
    }
    }

    public void writeWfID(String userID)
    {
                // write file WORKFLOW IDs
                //always give the path from root. This way it almost always works.
                String nameOfTextFile = "../webapps/wfids.txt";
                String str = "IMPORTED WORKFLOW IDs";
                try {
                    PrintWriter pw = new PrintWriter(new FileOutputStream(nameOfTextFile));
                    pw.println(str);

                for (ASMWorkflow wf : asm_service.getASMWorkflows(userID)) {
                    String wf_name=wf.getWorkflowName();
                    String wf_id=wf.getWorkflowID();
                    System.out.println(wf_name);

                    pw.println("name:" + wf_name + " id"+ wf_id);

                }

                    pw.println(" --- REPOSITORY WFS --- ");
                for (ASMRepositoryItemBean asmb:asm_service.getWorkflowsFromRepository(userID, RepositoryItemTypeConstants.Application)){
                    String wf_type=asmb.getId()+"";
                    String logical_name=asmb.getItemID();
                    pw.println("name:" + logical_name + " id"+ wf_type);
                }
/*
                Vector<ASMRepositoryItemBean> asmr= asm_service.getWorkflowsFromRepository(userID, RepositoryItemTypeConstants.Application);
                String user=asmr.get(0).getUserID();
                ASMRepositoryItemBean asmb=asmr.get(0);
                String wf_type=asmb.getId()+"";
                String locigal_name=asmb.getItemID();
*/

                    pw.close();
                } catch(Exception e) {
                   System.out.println(e.getMessage());
                }
    }
  
    private String runSystemcmd(String[] cmd)
    {
        String string_error="";
        try{
                Process pr;
                pr=Runtime.getRuntime().exec(cmd);
                BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                pr.waitFor();
                string_error=error.readLine();
        } catch(Exception e) {
                string_error=e.getMessage();
        }       
                return string_error;                
                
    }

    private ArrayList<String> getOutputfiles(String resultzipPath, String outputfileMask)
    {
                String temp_output=resultzipPath;
                String temp_filename= "wf_out.zip";
                String temp_dir="wf_dir";
                String output_filename="*.pdbqt";
                //String output_filename="*.*";
                try{
                    runSystemcmd( );
                Process process;
                process=Runtime.getRuntime().exec("rm -rf  /tmp/"+temp_dir);
                process.waitFor();
                process=Runtime.getRuntime().exec("mkdir -p /tmp/"+temp_dir);
                process.waitFor();
                process=Runtime.getRuntime().exec("cp "+temp_output+" /tmp/"+temp_filename);
                process.waitFor();
                process=Runtime.getRuntime().exec("unzip -o /tmp/"+temp_filename+" -d /tmp/"+temp_dir);
                process.waitFor();
                // get the output_filename location
                // MODIFIED TO SIMULATE MULTIPLE OUTPUT
                
                Process pr = Runtime.getRuntime().exec(new String[]{"find","/tmp/"+temp_dir,"-name", output_filename});                

                BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

                String line=null;
                String string_error="";
                ArrayList<String> resultPaths=new ArrayList<String>(1);
                while((line=input.readLine()) != null) {
                    resultPaths.add(line);
                }

                string_error=error.readLine();
                // MOVE RESULTS to PDBQT dir

                String pdbqtStorePath="../webapps/"+request.getContextPath()+"/script/jmol/model/result/";
                ArrayList<String> pdbqtList=new ArrayList<String>(resultPaths.size());
                
                for(String result : resultPaths)
                {
                    UUID idOne = UUID.randomUUID();
                    File resultx=new File(result);
                    String pdbqtNewName=idOne +"-"+resultx.getName()+".pdbqt";
                    Runtime.getRuntime().exec("cp -f "+result+" "+pdbqtStorePath+pdbqtNewName);
                    pdbqtList.add(""+pdbqtNewName);
                }
                }catch(Exception ex)
                {
                    ;
                }
        
    }

    @SuppressWarnings("empty-statement")
    public void doCheckStatus(ActionRequest request, ActionResponse response)
            throws PortletException, MalformedURLException, IOException
    {
        try{
            String selected_job = "collector.sh";
            String selected_port = "0";
            String userId = request.getRemoteUser();

            String status_code="";
            String status="";
            String jobid="wtf";
            String output_port="0";

            String status_str="";
            try{
                status_str=asm_service.getWorkflowStatus(userId, selected_wf).getStatus();
            }catch(Exception ex)
            {
                ;
            }
            if (status_str.equals("FINISHED")){
                String temp_output = asm_service.getFiletoPortalServer(userId, selected_wf, selected_job, output_port);

                // EXTRACT THE OUTPUTVALUE FROM RESULTZIP FILE
                String temp_filename= "wf_out.zip";
                String temp_dir="wf_dir";
                String output_filename="*.pdbqt";
                //String output_filename="*.*";
                Process process;
                process=Runtime.getRuntime().exec("rm -rf  /tmp/"+temp_dir);
                process.waitFor();
                process=Runtime.getRuntime().exec("mkdir -p /tmp/"+temp_dir);
                process.waitFor();
                process=Runtime.getRuntime().exec("cp "+temp_output+" /tmp/"+temp_filename);
                process.waitFor();
                process=Runtime.getRuntime().exec("unzip -o /tmp/"+temp_filename+" -d /tmp/"+temp_dir);
                process.waitFor();
                // get the output_filename location
                // MODIFIED TO SIMULATE MULTIPLE OUTPUT
                
                Process pr = Runtime.getRuntime().exec(new String[]{"find","/tmp/"+temp_dir,"-name", output_filename});                

                BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

                String line=null;
                String string_error="";
                ArrayList<String> resultPaths=new ArrayList<String>(1);
                while((line=input.readLine()) != null) {
                    resultPaths.add(line);
                }

                string_error=error.readLine();
                // MOVE RESULTS to PDBQT dir

                String pdbqtStorePath="../webapps/"+request.getContextPath()+"/script/jmol/model/result/";
                ArrayList<String> pdbqtList=new ArrayList<String>(resultPaths.size());

                for(String result : resultPaths)
                {
                    UUID idOne = UUID.randomUUID();
                    File resultx=new File(result);
                    String pdbqtNewName=idOne +"-"+resultx.getName()+".pdbqt";
                    Runtime.getRuntime().exec("cp -f "+result+" "+pdbqtStorePath+pdbqtNewName);
                    pdbqtList.add(""+pdbqtNewName);
                }
  
/*
                String result =temp_output;
                UUID idOne = UUID.randomUUID();
                File resultx=new File(result);
                String pdbqtNewName=idOne +"-"+resultx.getName()+".pdbqt";
                Runtime.getRuntime().exec("cp -f "+result+" "+pdbqtStorePath+pdbqtNewName);
                pdbqtList.add(""+pdbqtNewName);

 */
                throwError("found results: "+resultPaths.size()+" e:"+string_error,10);
                request.setAttribute("resultpdbqts", pdbqtList);
            }else{
                request.setAttribute("resultpdbqts", "");
            }
//
        }catch(Exception ex)
        {
            throwError(ex.getMessage(),10);
        }
            request.setAttribute("layerMoleculaVisualizeEnableVar","1");
            request.setAttribute("nextJSP",  DISPLAY_PAGE);
            String nextJSP= DISPLAY_PAGE;
            PortletRequestDispatcher dispatcher;
            dispatcher = getPortletContext().getRequestDispatcher(nextJSP);
            dispatcher.include(request, response);
    }


    @SuppressWarnings("empty-statement")
    public void doDownloadOutput(ResourceRequest request, ResourceResponse response) {

      String ruser=request.getRemoteUser();
      response.setContentType("application/zip");
      response.setProperty("Content-Disposition", "inline; filename=\"" + selected_wf + "_enduser_outputs.zip\"");
      asm_service.getFileStream(ruser, selected_wf, "collector.sh", null, response);
    }


    @SuppressWarnings("empty-statement")
    public void doSubmitVina(ActionRequest request, ActionResponse response)
            throws PortletException, MalformedURLException, IOException {
        ;
    }

    @SuppressWarnings("empty-statement")
    public void doChangeLigand(ActionRequest request, ActionResponse response)
            throws PortletException, MalformedURLException, IOException {
        ;

    }
    

    /**
     * View user notify settings informations...
     */
    @SuppressWarnings("empty-statement")
    public void doView(RenderRequest req, RenderResponse res) throws PortletException {
        try {
            readConfig();
            String userID = req.getRemoteUser();
            writeWfID(userID);
            try {

                req.setAttribute("owners", asm_service.getWorkflowDevelopers(RepositoryItemTypeConstants.Application));
                if (req.getParameter("owner") != null) {
                    String owner = req.getParameter("owner");
                    req.setAttribute("WorkflowList", asm_service.getWorkflowsFromRepository(owner, RepositoryItemTypeConstants.Application));
                    req.setAttribute("rep_owner", req.getParameter("owner"));
                }
                String workflowtobedetailed = (String) req.getParameter("getDetailsforWorkflow");
                if (workflowtobedetailed != null) {
                    try {
                        WorkflowInstanceBean wrkdetails = asm_service.getDetails(userID, workflowtobedetailed);
                        req.setAttribute("statusconstants", new StatusConstants());
                        req.setAttribute("statuscolors", new StatusColorConstants());
                        req.setAttribute("workflow_details", wrkdetails);
                        req.setAttribute("selected_Instance", workflowtobedetailed);

                    } catch (Exception ex) {
                        System.out.println("no RuntimeID");
                        ex.printStackTrace();
                    }
                }

                //ArrayList avail_wfs = asm_service.getWorkflows(userID);
                req.setAttribute("asm_instances", asm_service.getASMWorkflows(userID));
                req.setAttribute("portalID", asm_service.PORTAL);
                                
                req.setAttribute("errorMessage", errorMessage);
                errorMessage="";
                req.setAttribute("selected_wf_type",  selected_wf_type );
                req.setAttribute("selected_wf",  selected_wf );

                req.setAttribute("userID", userID);
                req.setAttribute("storageurl", asm_service.STORAGE);
                if (req.getParameter("command_line") != null) {
                    req.setAttribute("command_line_text", req.getParameter("command_line"));
                    req.setAttribute("content", req.getParameter("command_line"));
                }
                if (req.getParameter("resourcebean") != null) {
                    req.setAttribute("dciresourcequeue", req.getParameter("resourcebean"));
                    req.setAttribute("content", req.getParameter("resourcebean"));
                }
                if (req.getParameter("remotepath") != null) {
                    req.setAttribute("remotepath", req.getParameter("remotepath"));
                    req.setAttribute("content", req.getParameter("remotepath"));
                }
                if (req.getParameter("nodeNumber") != null) {
                    req.setAttribute("content", req.getParameter("nodeNumber"));
                }
                if (req.getParameter("act_workflowID") != null) {
                    req.setAttribute("act_workflowID", req.getParameter("act_workflowID"));
                }

            } catch (Exception e) {
                throwError(e.getMessage(),10);
                e.printStackTrace();

            }

            // Setting next page
            String nextJSP = (String) req.getParameter("nextJSP");
            if (nextJSP == null) {
                nextJSP = DISPLAY_PAGE;
            }

            String next = (String) req.getPortletSession().getAttribute("nextJSP");
         //   if (next != null && next.equals(DETAILS_PAGE))
            {

            }

            PortletRequestDispatcher dispatcher;
            dispatcher = getPortletContext().getRequestDispatcher(nextJSP);
            dispatcher.include(req, res);
        } catch (IOException ex) {
            throwError(ex.getMessage(),10);
            Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void doGetWorkflowsFromRepository(ActionRequest request, ActionResponse response) throws PortletException {
        String owner = request.getParameter("owner").toString();
        response.setRenderParameter("owner", owner);
    }

    public void doImportWorkflow(ActionRequest request, ActionResponse response) throws PortletException {
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
            Logger.getLogger(ASM_SamplePortlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void doGoBack(ActionRequest request, ActionResponse response) throws PortletException {
        response.setRenderParameter("nextJSP", DISPLAY_PAGE);


    }


    public void doUpload(ActionRequest request, ActionResponse response) throws PortletException {
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
        String userID = (String) request.getRemoteUser();
        String selected_wf = request.getParameter("user_selected_instance");


        asm_service.DeleteWorkflow(userID, selected_wf);

    }

    public void doDetails(ActionRequest request, ActionResponse response)
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

    public void doGetInput(ActionRequest request, ActionResponse response) throws PortletException {
        String userID = (String) request.getRemoteUser();
        String selected_wf = request.getParameter("user_selected_instance");
        String selected_job = "add";


        String actual_command_line = asm_service.getCommandLineArg(userID, selected_wf, selected_job);
        response.setRenderParameter("command_line", actual_command_line);
        response.setRenderParameter("act_workflowID", selected_wf);
    }

    public void doGetResource(ActionRequest request, ActionResponse response) throws PortletException {
        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");
        String selectedJob = "add";


        ASMResourceBean resourcebean = asm_service.getResource(userID, selectedWf, selectedJob);
        response.setRenderParameter("resourcebean", resourcebean.getType() + "/" + resourcebean.getGrid() + "/" + resourcebean.getResource() + "/" + resourcebean.getQueue());
        response.setRenderParameter("act_workflowID", selectedWf);
    }

    public void doGetRemoteInputPath(ActionRequest request, ActionResponse response) throws PortletException {
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

    public void doGetNodeNumber(ActionRequest request, ActionResponse response) throws PortletException {
        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");
        String selectedJob = "add";
        String nodeNumber = asm_service.getNodeNumber(userID, selectedWf, selectedJob);

        response.setRenderParameter("nodeNumber", nodeNumber);
        response.setRenderParameter("act_workflowID", selectedWf);

    }

    public void doSetNodeNumber(ActionRequest request, ActionResponse response) throws PortletException {

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

    public void doGetRemoteOutputPath(ActionRequest request, ActionResponse response) throws PortletException {
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

    public void doSubmit(ActionRequest request, ActionResponse response) throws PortletException {
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

    public void doSetInput(ActionRequest request, ActionResponse response) throws PortletException {

        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");

        String actual_command_line = request.getParameter("content");
        String selectedJob = "add";
        asm_service.setCommandLineArg(userID, selectedWf, selectedJob, actual_command_line);
    }

    public void doSetRemoteInputPath(ActionRequest request, ActionResponse response) throws PortletException {

        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");

        String remotepath = request.getParameter("content");
        String selectedJob = "add";
        String selectedPort = "0";


        asm_service.setRemoteInputPath(userID, selectedWf, selectedJob, selectedPort, remotepath);

    }

    public void doSetRemoteOutputPath(ActionRequest request, ActionResponse response) throws PortletException {

        String userID = (String) request.getRemoteUser();
        String selectedWf = request.getParameter("user_selected_instance");

        String remotepath = request.getParameter("content");
        String selectedJob = "add";
        String selectedPort = "1";


        asm_service.setRemoteOutputPath(userID, selectedWf, selectedJob, selectedPort, remotepath);

    }

    public void doSetResource(ActionRequest request, ActionResponse response) throws PortletException {

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

