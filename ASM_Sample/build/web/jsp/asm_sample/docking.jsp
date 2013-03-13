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
    <portlet:param name="docking_type" value="${docking_type}" />
</portlet:resourceURL>


<script>
    <!--
    //   09-01-2013 #4 log
    //    user='root';
    var ajaxURL="${ajaxURL}";
    var uploadURL="${uploadURL}";
    var uploadStatusURL="${uploadStatusURL}";
    workflow='${wrkdata.workflowID}';
    sjob='';
    var sForm=1;
    var portalID="${portalID}";

    var userID="${userID}";
    var workflowID="${wrkdata.workflowID}";
    var confID="${confID}";
    var jobID="";
    var vJob="";
    var callflag=0;
    var fileUploadErrorFlag=0;
    var formid=0;
    var action=""

    //    var sid0=document.cookie.split("=");
    //-->


    var layerMoleculaVisualizeEnable="${layerMoleculaVisualizeEnableVar}";
    var MoleculaVisualizeSize=600;
    var showLigandUrlPath="${ligandUrlPath}";

    function hideLayer(ObHide)
    {
        document.getElementById(ObHide).style.visibility="hidden";
    }

    function showLayer(ObShow)
    {
        document.getElementById(ObShow).style.visibility="visible";
    }

    function closeLayer(ObHide)
    {
        document.getElementById(ObHide).style.display="none";
    }

    function openLayer(ObShow)
    {
        document.getElementById(ObShow).style.display="";
    }

    function getY( el ) {
        var ret = 0;
        while( el != null ) {
            ret += el.offsetTop;
            el = el.offsetParent;
        }
        return ret;
    }

    function setSubmitMenu(whichDiv,whichRef) {
        document.getElementById(whichDiv).style.top = getY(whichRef) - 50;
    }

</script>

<script type="text/javascript">
    var MouseDownX, MouseDownY, PrevContainerX, PrevContainerY, PrevContainerW, PrevContainerH;
    var MouseIsDown = false;
    function LayerEvents(e,layerName) {
        switch(e.type) {
            case 'mousemove':
                if(e.ctrlKey && MouseIsDown) {
                    w = PXValue(document.getElementById(layerName).style.width);
                    h = PXValue(document.getElementById(layerName).style.height);
                    document.getElementById(layerName).style.width = PrevContainerW+((e.clientX-PrevContainerX)-(MouseDownX-PrevContainerX))+'px';
                    document.getElementById(layerName).style.height =  PrevContainerH+((e.clientY-PrevContainerY)-(MouseDownY-PrevContainerY))+'px';
                } else if(MouseIsDown) {
                    document.getElementById(layerName).style.left = e.clientX-(MouseDownX-PrevContainerX)+'px';
                    document.getElementById(layerName).style.top = e.clientY-(MouseDownY-PrevContainerY)+'px';
                }
                break;
            case 'mousedown':
                MouseIsDown = true;
                MouseDownX = e.clientX;
                MouseDownY = e.clientY;
                PrevContainerX = PXValue(document.getElementById(layerName).style.left);
                PrevContainerY = PXValue(document.getElementById(layerName).style.top);
                PrevContainerW = PXValue(document.getElementById(layerName).style.width);
                PrevContainerH = PXValue(document.getElementById(layerName).style.height);
                break;
            case 'mouseup':
                MouseIsDown = false;
        }
    }
    function PXValue(s) {
        return parseInt(s.replace('px',''));
    }
</script>
<script>
    var winW = 630, winH = 460;
    var winSq = 460;
    function getWindowsSize(){
        if (document.body && document.body.offsetWidth) {
            winW = document.body.offsetWidth;
            winH = document.body.offsetHeight;
        }
        if (document.compatMode=='CSS1Compat' &&
            document.documentElement &&
            document.documentElement.offsetWidth ) {
            winW = document.documentElement.offsetWidth;
            winH = document.documentElement.offsetHeight;
        }
        if (window.innerWidth && window.innerHeight) {
            winW = window.innerWidth;
            winH = window.innerHeight;
        }
        winSq = winH;
        if (winH > winW){
            winSq = winW;
        }
    }
</script>


<style type="text/css">
    div.draggable {
        position: absolute;
        top: 300px;
        left: 400px;
        z-index:100;
        background-color: #ccc;
        border: 1px solid #000;
        opacity: 0.9;
        padding: 5px;
        filter: alpha(opacity=90);
        display: none;
    }
    div.draggable div {
        background-color: #ffa;
        z-index:101;
        opacity: 1.0;
        color: #000;
        padding: 5px;
        filter: alpha(opacity=100);
    }

    .pull{
        position: absolute;
        top: 10px;
        left: 10px;
        z-index:1001;
        background-color: #999999;
        border: 1px solid #000;

        filter: alpha(opacity=90);
        color: #000;
    }

</style>


<script type="text/javascript">

    function writeTopMessage(msg){
        document.getElementById('topMessage').innerHTML = msg;
    }

    function writeBottomMessage(msg){
        document.getElementById('bottomMessage').innerHTML = msg;
    }

    function checkTextValueIsAlphaNumeric(value) {
        return value.match(/^[A-Za-z0-9]+$/);
    }

    function checkTextValueIsNumeric(value) {
        return value.match(/^[0-9]+$/);
    }

    // Popup window code
    function newPopup(url) {
        popupWindow = window.open(
            url,'popUpWindow','height=700,width=800,left=10,top=10,resizable=yes,scrollbars=yes,toolbar=yes,menubar=no,location=no,directories=no,status=yes')
    }
</script>

<portlet:defineObjects/>

<portlet:actionURL var="pURL" portletMode="VIEW" />

<table>
    <tr>
        <td>
        </td>
        <td>
            <form method="post" action="${pURL}" >
                <input type="hidden" name="action" id="action" value="doChangeDockingType">
                <input type="hidden" name="docking_type" id="docking_type" value="1">
                <input type="submit" value="Vina" class="portlet-form-button">
                <c:if test="${docking_type == '1' }">
                    <
                </c:if>
            </form>
        </td>
        <td>
            <form method="post" action="${pURL}" >
                <input type="hidden" name="action" id="action" value="doChangeDockingType">
                <input type="hidden" name="docking_type" id="docking_type" value="2">
                <input type="submit" value="AutodockAutogrid" class="portlet-form-button">
                <c:if test="${docking_type == '2' }">
                    <
                </c:if>
            </form>
        </td>
        <td>
            <form method="post" action="${pURL}" >
                <input type="hidden" name="action" id="action" value="doChangeDockingType">
                <input type="hidden" name="docking_type" id="docking_type" value="3">
                <input type="submit" value="AutodockNo" class="portlet-form-button">
                <c:if test="${docking_type == '3' }">
                    <
                </c:if>
            </form>
        </td>
        <td>
            &nbsp;
        </td>
        <td align="right">
            <a href="JavaScript:newPopup('${pageContext.request.contextPath}/jsp/vinaportlet_help/vinaportlet_help.html');"> How to use docking - Help</a>    
        </td>
    </tr>
</table>

<c:if test="${fn:length(errorMessage) > 0}">
    <div id="messageErrorfromServer"  style="padding:40px; border:2px solid black;  opacity:0.9; background-color: red; position:absolute;  top: 300px; left: 400px; z-index:999" >
        <strong><h1>Warning!</h1></strong>

        ${errorMessage}
        <p align="center">
            <input type="button" name="ly1"   value="OK" onClick="hideLayer('messageErrorfromServer')">
        </p>
    </div>
</c:if>

<div style="position:fixed; ">
    <div style="position:relative; padding:2px; border:0px dashed #ccc; top:2px; left:2px; height:25; width:100%; color: #0900C4;" onmousemove="LayerEvents(event,'layerMoleculaVisualize')" onmousedown="LayerEvents(event,'layerMoleculaVisualize')" onmouseup="LayerEvents(event,'layerMoleculaVisualize')" >
        <div id="layerMoleculaVisualize" class="pull"  style="padding:2px; border:2px solid black; position:absolute; top:-350px; left:0px;">
            <table>
                <tr>
                    <td align="left">
                        <script>
                            function reloadMolecule(){
                                document.getElementById('frame_jmol').src = '${pageContext.request.contextPath}/jsp/asm_sample/frame_jmol.jsp';
                                document.getElementById('frame_jmol').width=MoleculaVisualizeSize+18;
                                document.getElementById('frame_jmol').height=MoleculaVisualizeSize+38;
                            }
                        </script>
                        <script>
                            function moveToTop(){
                                document.getElementById('layerMoleculaVisualize').style.left = "-50px";
                                document.getElementById('layerMoleculaVisualize').style.top = "-460px";
                            }
                        </script>

                        <div id="layerInSideMoleculaVisualize">
                            <iframe id="frame_jmol" frameborder="0" src="${pageContext.request.contextPath}/jsp/asm_sample/frame_jmol.jsp">
                            </iframe>
                        </div>

                    </td>
                    <td align="right" valign="top">
                        <br>
                        <input type="button" name="ly1"  style="height: 25px; width: 25px" value="X" title="Hide" onClick="hideLayer('layerMoleculaVisualize')">
                        <input type="button" name="ly1"  style="height: 25px; width: 25px" value="[]" title="Fit screen" onClick="getWindowsSize(); MoleculaVisualizeSize = winSq-30;
                            moveToTop();
                            reloadMolecule();
                           ">

                        <br>
                        <input type="button" name="ly1"  style="height: 25px; width: 25px"  value="+" title="Larger" onClick="MoleculaVisualizeSize = MoleculaVisualizeSize+100; reloadMolecule();">
                        <input type="button" name="ly1"  style="height: 25px; width: 25px"  value="-" title="Smaller" onClick="MoleculaVisualizeSize = MoleculaVisualizeSize-100; reloadMolecule(); ">

                        <br>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</div>


<p><b id='topMessage'>-</b> </p>

<hr/>

<c:set var="actionURL"  value="${pURL}" scope="session" />

<c:if test="${ docking_type=='1'}">
    <c:import url = "publicautodockvina.jsp">
    </c:import>
</c:if>
<c:if test="${ docking_type=='2'}">
    <c:import url = "publicautodockautogrid.jsp">
    </c:import>
</c:if>
<c:if test="${ docking_type=='3'}">
    <c:import url = "publicautodockno.jsp">
    </c:import>
</c:if>




<hr/>

    <table border="1" width="100%" cellspacing="10" style="padding:10px;">
        <tr style="border:1px solid black;">
            <td align="center">
                <strong><i>Task Name</i></strong>
            </td>
            <td align="center">
                <strong><i>Task Status</i></strong>
            </td>
            <td align="center" colspan="4">
                <strong><i>Actions</i></strong>
            </td>
        </tr>
        <c:forEach var="workflows" items="${asm_instances}">
            <c:if test="${fn:startsWith(workflows.workflowName,selected_wf_type)==true}">
                <c:set var="selectedcolor"  value=""/>
                <c:if test="${ workflows.workflowName eq selected_wf}">
                    <c:set var="selectedcolor"  value="LIGHTBLUE"/>
                </c:if>
                <tr bgcolor="${selectedcolor}" >
                    <td align="center" >
                        <c:set var="string2" value="${fn:split(workflows.workflowName, '_')}" />
                        ${string2[1]} <br>
                        ${string2[3]} <br>
                    </td>


                    <c:choose>
                        <c:when test="${workflows.statusbean.status == 'FINISHED'}">
                            <c:set var="statuscolor"  value="LIGHTGREEN"/>
                        </c:when>
                        <c:when test="${workflows.statusbean.status == 'ERROR'}">
                            <c:set var="statuscolor"  value="RED"/>
                        </c:when>
                        <c:when test="${workflows.statusbean.status == 'RUNNING'}">
                            <c:set var="statuscolor"  value="LIGHTBLUE"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="statuscolor"  value="WHITE"/>
                        </c:otherwise>
                    </c:choose>

                    <td bgcolor="${statuscolor}" align="center">
                        ${workflows.statusbean.status}
                        <c:if test="${ workflows.workflowName eq selected_wf}">
                            <c:if test="${jobs_errornr+jobs_finishednr+jobs_runningnr > 0}">
                                <br> (${jobs_errornr} error, ${jobs_finishednr} finished,  ${jobs_runningnr} running)
                            </c:if>
                        </c:if>
                    </td>


                    <td >
                        <table border="0">
                            <tr>
                                <td>
                                    <form method="post" action="${pURL}" >
                                        <input type="hidden" id="user_selected_instance" name="user_selected_instance" value="${workflows.workflowName}"/>
                                        <input type="hidden" name="action" id="action" value="doCheckStatus">
                                        <input type="hidden" name="docking_type" id="docking_type" value="${docking_type}">
                                        <input type="submit" value="Show results / refresh" class="portlet-form-button">
                                    </form>
                                </td>
                                <c:if test="${workflows.statusbean.status == 'WORKFLOW_SUSPENDED'}">
                                    <td>
                                        <form method="post" action="${pURL}" >
                                            <input type="hidden" id="user_selected_instance" name="user_selected_instance" value="${workflows.workflowName}"/>
                                            <input type="hidden" name="action" id="action" value="doContinue">
                                            <input type="hidden" name="docking_type" id="docking_type" value="${docking_type}">
                                            <input type="submit" value="Continue from Suspend" class="portlet-form-button">
                                        </form>
                                    </td>
                                </c:if>
                                <!--input type="button" value="Download Results" class="portlet-form-button"
                                        onClick="javascript:document.getElementById('instance_download_${workflows.workflowName}').value='${workflows.workflowName}';
                                                setSubmitMenu('div_download_${workflows.workflowName}',this);
                                                document.getElementById('div_download_${workflows.workflowName}').style.display='block';
                                        "/-->
                                <td>
                                    <form method="post" action="${pURL}" >
                                        <input type="hidden" id="user_selected_instance" name="user_selected_instance" value="${workflows.workflowName}"/>
                                        <input type="hidden" name="action" id="action" value="doDelete">
                                        <input type="hidden" name="docking_type" id="docking_type" value="${docking_type}">
                                        <input type="submit" value="Delete" class="portlet-form-button">
                                    </form>
                                </td>
                            </tr>
                        </table>
                    </td>


                </tr>
            </c:if>
        </c:forEach>
    </table>
    <br>


    <c:if test="${ result_finished=='1'}">

    </hr>
    <br>

    <div id="divResult" name="divResult" style="border:2px solid black; background-color:LightSteelBlue">
        <input type="hidden" name="action" id="action" value="doShowSelected">

  <table border="1" width="100%" cellspacing="10" style="padding:10px;">
        <tr style="border:1px solid black;" bgcolor="white">
            <td align="center">
                </td>
            <td align="center">
                    <h4> <i>  Results for task:</i>
                        <strong>
                            <c:set var="string2" value="${fn:split(selected_wf, '_')}" />
                                ${string2[1]} ${string2[3]}
                        </strong>
                    </h4>
                </td>

            </tr>
            <tr>
                <table>
                <tr>
                    <td>
                        <form method="post" action="${resURL}">
                            <input type="hidden" id="user_selected_instance" name="user_selected_instance" value="${selected_wf}"/>
                            <input type="hidden" id="file2download" name="file2download" value="best" >
                            <input type="hidden" name="docking_type" id="docking_type" value="${docking_type}">
                            <input type="submit" value="Download best outputs " class="portlet-form-button">
                        </form>
                    </td>
                    <c:if test="${ docking_type!='1'}">
                        <td>
                            <form method="post" action="${resURL}">
                                <input type="hidden" id="user_selected_instance" name="user_selected_instance" value="${selected_wf}"/>
                                <input type="hidden" id="file2download" name="file2download" value="log.dlg" >
                                <input type="hidden" name="docking_type" id="docking_type" value="${docking_type}">
                                <input type="submit" value="Download log.dlg tar.gz" class="portlet-form-button">
                            </form>
                        </td>
                    </c:if>
                    <td>
                        <form method="post" action="${pURL}" >
                            <input type="hidden" id="user_selected_instance" name="user_selected_instance" value="${selected_wf}"/>
                            <input type="hidden" name="action" id="action" value="doReSubmit">
                            <input type="hidden" name="docking_type" id="docking_type" value="${docking_type}">
                            <input type="submit" value="Re-compute task" class="portlet-form-button">
                        </form>
                    </td>
                </tr>
                </table>
            </tr>
            <c:if test="${fn:length(result_receptor.fileName) > 0}">
            <tr>
                <td>
                    Receptor file, with docked ligands:
                </td>
                <td>
                    Title:<strong>${result_receptor.moleculeTitle} </strong>
                    ${result.moleculeInfo}
                    <br>

                    Receptor:
                    <input type="button" value="Large view" onClick="
                        showLigandUrlPath='result/${result_receptor.fileName}';
                        showLayer('layerMoleculaVisualize')
                        reloadMolecule();
                ">
                    <script src="${pageContext.request.contextPath}/script/jmol/Jmol.js" type="text/javascript">   </script>

                    <script type="text/javascript">
                        jmolInitialize("${pageContext.request.contextPath}/script/jmol");
                        jmolApplet(200,   "load ${pageContext.request.contextPath}/script/jmol/model/result/${result_receptor.fileName}");
                    </script>
                    Receptor with ligands:
                    <input type="button" value="Large view" onClick="
                        showLigandUrlPath='result/${result_receptor_combined.fileName}';
                        showLayer('layerMoleculaVisualize')
                        reloadMolecule();
                ">


                    <script src="${pageContext.request.contextPath}/script/jmol/Jmol.js" type="text/javascript">   </script>
                    <script type="text/javascript">
                        jmolInitialize("${pageContext.request.contextPath}/script/jmol");
                        jmolApplet(200,   "load ${pageContext.request.contextPath}/script/jmol/model/result/${result_receptor_combined.fileName}");
                    </script>
                </td>
            </tr>
            </c:if>
        </table>

        <c:if test="${fn:length(result_molecules) > 0}">
            <br>
            <hr>

            <table width="100%" border="1" >
                <tr align="center" bgcolor="white">
                    <td>
                        <strong>Ligand molecule title</strong>
                    </td>
                    <td>
                        <strong>Ligand molecule info</strong>
                    </td>
                    <td>
                    </td>
                    <td>
                        <strong>Ligand molecule image</strong>
                    </td>
                </tr>

                <c:forEach var="result" items="${result_molecules}">
                    <tr style="border:1px solid black;">
                        <td>
                            Title:<strong>${result.moleculeTitle} </strong>
                        </td>
                        <td>
                            Autodock Score:<strong>${result.energyLevel} </strong> [${result.energyLevelUnit}]
                            <br>
                            ${result.moleculeInfo}
                            <br>
                            <a href="${pageContext.request.contextPath}/script/jmol/model/result/${result.fileName}">Download Molecule</a>
                        </td>
                        <td>

                        </td>
                        <td align="right">
                            <input type="button" value="Large view" onClick="
                                showLigandUrlPath='result/${result.fileName}';
                                showLayer('layerMoleculaVisualize')
                                reloadMolecule();
                           ">

                            <script src="${pageContext.request.contextPath}/script/jmol/Jmol.js" type="text/javascript">
                            </script>
                            <script type="text/javascript">
                                jmolInitialize("${pageContext.request.contextPath}/script/jmol");
                                jmolApplet(200,   "load ${pageContext.request.contextPath}/script/jmol/model/result/${result.fileName}");
                            </script>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </c:if>
</c:if>

<hr/>
<p><b id='bottomMessage'>-</b> </p>



<!--
<form method="post" action="${pURL}" >
 <input type="hidden" name="action" id="action" value="doCheckStatus">
 <input type="submit" value="Check Status" class="portlet-form-button">
</form>

<form method="post" action="${pURL}" >
 <input type="hidden" name="action" id="action" value="doSubmitVina">
 <input type="submit" value="sprobe" class="portlet-form-button">
</form>


<input type="button" name="ly1"  value="hide divResult" onClick="closeLayer('divResult')">
<input type="button" name="ly1"  value="hide layerMoleculaVisualize" onClick="hideLayer('layerMoleculaVisualize')">

<input type="button" name="ly1"  value="show divResult" onClick="openLayer('divResult')">
<input type="button" name="ly1"  value="show layerMoleculaVisualize" onClick="showLayer('layerMoleculaVisualize')">


05-02-2013 # UoW # CPC
-->
 <!--
<form method="post" action="${pURL}" >
   
    write workflow ids to file  ../webapps/wfids.txt
    and read the autodock config xml file
-->
 <!--
    <input type="hidden" name="action" id="action" value="doConfigure">
    <input type="submit" value="configure portlet" class="portlet-form-button">
</form>
-->

<script>

    <!--writeBottomMessage("selected_wf is:${selected_wf} type:${selected_wf_type}"); -->
    writeTopMessage("${userMessage}");

    if (layerMoleculaVisualizeEnable=="1"){
        //showLayer("layerMoleculaVisualize");
    }else{
        hideLayer("layerMoleculaVisualize")
    }
    hideLayer("layerMoleculaVisualize")
    hideLayer("layerMolSketch")
    if (selected_wf_type=="") writeTopMessage("!ERROR! Please contact the site administrator, to configure the ${pageContext.request.contextPath}/WEB-INF/autodock-config.xml file");
</script>