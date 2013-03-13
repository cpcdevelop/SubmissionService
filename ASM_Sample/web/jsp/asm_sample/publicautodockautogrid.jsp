
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:resourceURL var="resURL" />
<portlet:renderURL var="rURL" />
<portlet:actionURL var="uploadURL" />
<portlet:resourceURL var="ajaxURL" />
<portlet:resourceURL var="uploadStatusURL" >
    <portlet:param name="sid" value="2778" />
    <portlet:param name="uploadStatus" value="" />
</portlet:resourceURL>
<portlet:defineObjects/>

<script type="text/javascript">

    function Validate(oFormID) {
        var oForm = document.getElementById(oFormID);
        var arrInputs = oForm.getElementsByTagName("input");
        writeTopMessage("Validating inputs..");
        for (var i = 0; i < arrInputs.length; i++) {
            var oInput = arrInputs[i];
            writeTopMessage(i+" Validating inputs :"+oInput.name);
            if (oInput.type == "text") {
                var textValue = oInput.value;
                if (oInput.name =="task_name"){
                    if (!checkTextValueIsAlphaNumeric(textValue)){
                        window.alert("Validation Error! Please use only A..Z and 0..9 characters in "+oInput.name+" text field, replace the "+oInput.value);
                        oInput.focus();
                        return false;
                    }
                }
                if (oInput.name =="docking_wu"){
                    if (!checkTextValueIsNumeric(textValue)){
                        window.alert("Validation Error! Please use only 0..9 characters in "+oInput.name+" text field, replace the "+oInput.value);
                        oInput.focus();
                        return false;
                    }
                }

                if (oInput.name =="best_result"){
                    if (!checkTextValueIsNumeric(textValue)){
                        window.alert("Validation Error! Please use only 0..9 characters in "+oInput.name+" text field, replace the "+oInput.value);
                        oInput.focus();
                        return false;
                    }
                }
                if (!checkTextValueIsAlphaNumeric(textValue)){
                    window.alert("Validation Error! Please use only A..Z and 0..9 characters in "+oInput.name+" text field, replace the "+oInput.value);
                    oInput.focus();
                    return false;
                }
            }

            if (oInput.type == "file") {
                var sFileName = oInput.value;
                var blnValid = false;
                if (sFileName.length > 0) {
                    var blnValid = false;
                    if (oInput.name =="receptor"){
                        if (sFileName.substr(sFileName.length - 4, 4).toLowerCase() == ".pdb"){
                            blnValid = true;
                        }else{
                            ;
                        }
                    }
                    if (oInput.name =="gpfparameter"){
                        if (sFileName.substr(sFileName.length - 4, 4).toLowerCase() == ".gpf"){
                            blnValid = true;
                        }else{
                            ;
                        }
                    }
                    if (oInput.name =="dpfparameter"){
                        if (sFileName.substr(sFileName.length - 4, 4).toLowerCase() == ".dpf"){
                            blnValid = true;
                        }else{
                            ;
                        }
                    }
                    if (oInput.name =="ligand"){
                        if (sFileName.substr(sFileName.length - 4, 4).toLowerCase() == ".pdb"){
                            blnValid = true;
                        }else{
                            ;
                        }
                    }
                    if (!blnValid) {
                        alert("Sorry, " + sFileName + " is invalid, please select the proper file type for "+oInput.name);
                        oInput.focus();
                        return false;
                    }
                } else{
                    alert("Sorry, " + oInput.name + " file is empty. Please select and upload file with content.");
                    oInput.focus();
                    return false;
                }
            }
        }
        var bestnr=parseInt(document.getElementById("best_result").value);
        var dockingnr=parseInt(document.getElementById("docking_wu").value);
       if (bestnr > dockingnr){
              window.alert("Validation Error! Please make sure best result ("+bestnr+") < docking number WU ("+dockingnr+")");
             return false;
        }

        writeTopMessage("Input Validation Success. Uploading, please wait for response..");
        return true;
    }
</script>

<portlet:actionURL var="pURL" portletMode="VIEW" />




<h1><strong>AutoDock4 - Random blind docking requiring pdb input files </strong></h1>

This application requires pdb input files and docks a small ligand molecule on a larger receptor molecule structure in a Monte-Carlo simulation. The workflow uses version 4.2.3 of the AutoDock docking simulation package.
For more information on required input parameters please see the window below.
<br>
For generic help on AutoDock please see <A href="http://autodock.scripps.edu/">Autodock usage</a>

<hr/>
<br>


<div id="divSubmit" name="divSubmit" style="background-color:LightSteelBlue; text-align:left; border:2px solid black">
    <form method="post" action="${pURL}" enctype="multipart/form-data" name="vinaSubmit" id="vinaSubmit" onsubmit="return Validate('vinaSubmit');">
        <input type="hidden" name="docking_type" id="docking_type" value="${docking_type}">
        <input type="hidden" name="action" id="action" value="doUploadLigands">
        <table width="100%" border="0" style="padding:10px;">
            <tr style="border:solid 1px black" align="center">
                <td style="border:solid 1px black" align="center" >
                    <strong><i>Autodock - specify the inputs</i></strong>
                </td>
                <td></td>
                <td style="border:solid 1px black" align="center">
                    <i>
                    Field description
                    </i>
                </td>

            </tr>


            <tr title="Select the pdbqt file from your computer, which will be the receptor for the docking. The format should be an pdbqt file." >
                <td align="left">
                    <strong>Receptor file (*.pdb)</strong>
                </td>
                <td>
                    <input type="file" name="receptor"  accept="*.pdb" style="width: 300px;" >
                </td>

                <td>
                    Select and upload the Receptor file for docking.
                    <a href="http://shiwa-repo.cpc.wmin.ac.uk/shiwa-repo/download?appid=2955&filename=receptor.pdb"> Sample receptor download. </a>
                </td>

            </tr>


            <tr title="Select the gpf file from your computer, the spatial attributes of the grid maps." >
                <td align="left">
                    <strong>Autogrid map file (*.gpf)</strong>
                </td>
                <td>
                    <input type="file" name="gpfparameter" accept="*.gpf" style="width: 300px;" >
                </td>

                <td>
                    Select and upload the Autogrid parameter file. Specifies the spatial attributes of the grid maps.
                    <a href="http://shiwa-repo.cpc.wmin.ac.uk/shiwa-repo/download?appid=2955&filename=docking.gpf"> Sample  map file  download. </a>
                </td>

            </tr>


            <tr title="Select the dpf file from your computer, which will be the parameters for the docking.">
                <td align="left">
                    <strong>Docking parameter file (*.dpf)</strong>
                </td>
                <td>
                    <input type="file" name="dpfparameter" accept="*.dpf" style="width: 300px;" >
                </td>

                <td>
                    Select and upload the Docking parameter file. Specifies the files and parameters for the docking.
                    <a href="http://shiwa-repo.cpc.wmin.ac.uk/shiwa-repo/download?appid=2955&filename=docking.dpf"> Sample parameter file download. </a>
                </td>
            </tr>

            <tr title="Select the ligand file from your computer.">
                <td align="left">
                    <strong>Ligand molecule file (*.pdb)</strong>
                </td>
                <td>
                    <input type="file" name="ligand"  style="width: 300px;" >
                </td>

                <td>
                    Select and upload the ligand file.
                    <a href="http://shiwa-repo.cpc.wmin.ac.uk/shiwa-repo/download?appid=2955&filename=ligand.pdb"> Sample ligands download. </a>
                </td>
            </tr>

            <tr title="Give the number of the docking. WorkUnit numbers">
                <td align="left">
                    <strong>Number of docking</strong>
                </td>
                <td>
                    <input type="text" id="docking_wu" name="docking_wu" value="10" style="width: 60px;" />
                </td>
                <td>
                    Specify the number of experiments you want to carry out. Default value: 10
                </td>
            </tr>

            <tr title="Give the number of the best results, ordered by the docking energy level">
                <td align="left">
                    <strong>Best result number</strong>
                </td>
                <td>
                    <input type="text" id="vina_best_result" name="best_result" value="5" style="width: 60px;" />
                </td>
                <td>
                   Specify the number of the lowest Autodock score results you want to keep. Default value: 5
                </td>
            </tr>

            <tr title="Give a name for your experiment">
                <td align="left">
                    <strong>Task name</strong>
                </td>
                <td>
                    <input type="text" id="content" name="task_name" value="Task1" style="width: 200px;" />
                </td>
                <td>
                    Specify the name of current task, use only letters or numbers.
                </td>
            </tr>

        </table>
        <input type="submit" value="Run task" title="Start the docking job" />
    </form>
</div>
