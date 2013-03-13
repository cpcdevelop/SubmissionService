var sdiv="";
var slin="";

// use getLayer
var ns = document.layers;
var moz = document.getElementById && !document.all;
var ie = document.all;
// use getLayer

function hideremotekey(pWrk)
{
    if(document.getElementById("remoting_"+pWrk).style.display=="none")
    {
	document.getElementById("remoting_"+pWrk).style.display="block";
	document.getElementById("remotingbutton_"+pWrk).value="hide";
    }
    else
    {
	document.getElementById("remoting_"+pWrk).style.display="none";
	document.getElementById("remotingbutton_"+pWrk).value="view key";
    }
}


function hidejobstatus(pJob,pJobStat)
{

    if(document.getElementById('jobbutton_'+pJob+pJobStat).value!="Hide")
    {
        document.getElementById('jobinsttatus0_'+pJob).style.display="none";
	getConfForm('m=GetJobInstance&j='+pJob+'&s='+pJobStat);
	document.getElementById('jobbutton_'+pJob+'all').value="Hide";
//	document.getElementById('jobinsttatus0_'+pJob).style.display="block";
    }
    else
    {
	document.getElementById('jobinsttatus_'+pJob).innerHTML="";
	document.getElementById('jobinsttatus0_'+pJob).style.display="none";
	document.getElementById('jobbutton_'+pJob+'all').value="View all content(s)";
    }
}
function getJobInstances(pJob,pJobStat,pfrom,prange,ptype)
{
	getConfForm('m=GetJobInstance&j='+pJob+'&s='+pJobStat+'&f='+pfrom+'&r='+prange+'&t='+ptype);
}


function hide(divID,pWorkflow,event)
{
    document.getElementById('workflow').value=pWorkflow;
    if(document.getElementById(divID).style.display=="none")
    {
	cx=event.clientX
	cy=event.clientY

	sx=event.pageX
	sy=event.pageY
//	    alert(":"+cx+"."+cy+":"+sx+"."+sy+":"+event.pageY);

	if((navigator.appName=="Opera")||(navigator.appName=="Microsoft Internet Explorer"))
	{
	    document.getElementById(divID+"_txt").style.top=window.event.y+" px";
	    if(window.event.x>300)
		document.getElementById(divID+"_txt").style.left=(window.event.x-500)+" px";
	    else document.getElementById(divID).style.left="10 px";
	}
	else
	{
    	    document.getElementById(divID+"_txt").style.top=(sy-250)+"px";
    	    if(sx>300)
		document.getElementById(divID+"_txt").style.left=(cx-250)+"px";
	    else
		document.getElementById(divID+"_txt").style.left=cx+"px";
//	    document.getElementById(divID+"_txt").style.left="500px";
//	    alert(":"+document.getElementById(divID+"_txt").style.top);
//	document.getElementById(divID+"_txt").style.left=(event.clientX-250)+" px";
//	document.getElementById(divID).style.left=window.event.x+" px";

	}
//	alert(navigator.appName+" Kliens X= " + cx + ",Kliens Y= " + cy +"\nPontos X= " + sx + ",Pontos Y= " + sy+"::");

	document.getElementById(divID).style.top="0 px";
	document.getElementById(divID).style.height=document.getElementById('rwlist').offsetHeight+"px";
	document.getElementById(divID).style.width=document.getElementById('rwlist').offsetWidth+"px";
	document.getElementById(divID).style.display="block";

    }
    else{document.getElementById(divID).style.display="none";}
}

function createInputElement(pType,pName,pValue)
{
    ctmp=document.createElement("input");
    ctmp.setAttribute("type",pType);
    ctmp.setAttribute("name",pName);
    ctmp.setAttribute("value",pValue);
    return ctmp;
}

function removeUploadView(pID)
{
    document.getElementById("uploadstatus").removeChild(document.getElementById(pID));
}

function addUploadView(pID,pTxt)
{
    tmp=document.createElement("div");
    tmp.setAttribute("id","upv"+pID);
    tmp.appendChild(document.createTextNode(pTxt));

    tmp0=document.createElement("div");
    tmp0.setAttribute("id","un"+pID);
    tmp0.style.display="block";
    tmp0.style.background="#fea602";
    tmp0.style.width="200px";
    tmp0.style.height="10px";
    tmp.appendChild(tmp0);
    document.getElementById("uploadstatus").appendChild(tmp);
}

function deletenode(id)
{
    var obj=document.getElementById(id);
    obj.parentNode.removeChild(obj);

}

function additem(pID,pNID,pJobID,pFile)
{
//    alert("additem("+pID+","+pNID+","+pJobID+","+pFile+")");
    try{
        if(document.getElementById(pFile).value!=""){
            if(document.getElementById(pFile).name.substring(0,5)!="file_"){
	    // alert("additem doit...");
        	    formid=formid+1;
                var sid2 = getSID()+confID;
        	    tmp=document.createElement("form");
                tmp.setAttribute("enctype","multipart/form-data");
                tmp.setAttribute("encoding","multipart/form-data");
                tmp.setAttribute("method","post");
        	    tmp.setAttribute("id","form"+formid);
                tmp.setAttribute("name","form"+formid);
                tmp.setAttribute("action",uploadURL);
        	    tmp.setAttribute("target","none");
        	    for(var ti=1;ti<formid;ti++){
            		if(document.getElementById("form"+ti)!=null){
                        if(jsIdCheck("form"+ti,pFile)){
                		    try{document.getElementById("hideforms").removeChild(document.getElementById("form"+ti));}
                            catch(ex){jsSubmitting("form"+ti);}
                        }
                    }
                }
        	    tmp0=document.getElementById(pFile);
                tmp0.setAttribute("name","file_"+sid2+"_"+get_random());
        	    tmp.appendChild(tmp0);

                tmp.appendChild(createInputElement("hidden","sfile",pNID));
        	    tmp.appendChild(createInputElement("hidden","confID",confID));
                tmp.appendChild(createInputElement("hidden","portalID",portalID));
        	    tmp.appendChild(createInputElement("hidden","userID",userID));
                tmp.appendChild(createInputElement("hidden","workflowID",workflowID));
        	    tmp.appendChild(createInputElement("hidden","jobID",pJobID));

                document.getElementById("hideforms").appendChild(tmp);
        	}
        }
    } catch (ex) {alert("ex:"+ex);}
}

//Easy upload
function addEitem(pID,pNID,pJobID,pFile,pstorageID,pportalID,puserID,pworkflowID)
{

    if(document.getElementById(pFile).value!="")
    {
	var sid2 = getSID()+confID;

	var otmp=document.getElementById("hideforms").childNodes;
	for(i=0;i<otmp.length;i++)
	{
	    if(otmp[i].hasChildNodes())
	    {
		var otmp0=otmp[i].childNodes;
    		var b=0;
		for(j=0;j<otmp0.length;j++)
	        {

		    if(otmp0[j].id==pFile)
		    {
			b=1;
		    }
		}
	    }
	    if(b==1)
	    {
		deletenode(otmp[i].id);
	    }
	}

    try
    {

//    alert("*"+docuement.getElementById(pID).value+"*");
	if(document.getElementById(pFile).value!="")
	{
    	    formid=formid+1;
//	    alert("formID"+formid);
//	    alert(i);
	    tmp=document.createElement("form");
	    tmp.setAttribute("enctype","multipart/form-data");
            tmp.setAttribute("encoding","multipart/form-data");
	    tmp.setAttribute("method","post");
	    tmp.setAttribute("id","form"+formid);
	    tmp.setAttribute("name","form"+formid);
	    tmp.setAttribute("action",uploadURL);

//	    alert("storageID > " + pstorageID);
	    tmp.setAttribute("target","none");

	    tmp0=document.getElementById(pFile);
//	    alert(sid[1])
	    tmp0.setAttribute("name","file_"+sid2+"_"+get_random());
	    tmp.appendChild(tmp0);

	    tmp.appendChild(createInputElement("hidden","sfile",pNID));
	    tmp.appendChild(createInputElement("hidden","confID",confID));
	    tmp.appendChild(createInputElement("hidden","portalID",pportalID));
	    tmp.appendChild(createInputElement("hidden","userID",puserID));
	    tmp.appendChild(createInputElement("hidden","workflowID",pworkflowID));
	    tmp.appendChild(createInputElement("hidden","jobID",pJobID));

	    document.getElementById("hideforms").appendChild(tmp);
//	    alert(document.getElementById("form"+i));
	    addUploadView(formid,tmp0.value);
	    document.getElementById('eparam_'+pID).value=document.getElementById('feparam_'+pID).value;
	    document.getElementById('vparam_'+pID).innerHTML=document.getElementById('feparam_'+pID).value;
	    document.getElementById("eupload_"+pID).innerHTML='<input type="button" value="Browse" onclick=javascript:removeEForm("'+pID+'",'+formid+')>';
	    document.getElementById("upload_"+pID).innerHTML='<input class="portlet-form-button" type="file" name="feparam_'+pID+'" id="feparam_'+pID+'" onchange="javascript:addEitem(\''+pID+'\',\''+pNID+'\',\''+pJobID+'\',\''+pFile+'\',\''+pstorageID+'\',\''+pportalID+'\',\''+puserID+'\',\''+pworkflowID+'\');" >';
	    document.getElementById('upload_'+pID).style.display="none";
//	    document.getElementById("upload_"+pID).innerHTML='<input type="text" disabled="true" value="'+document.getElementById(pFile).value+'"> <input type="button" value="Browse" onclick=javascript:removeEForm("'+pFile+'",'+i+')>';
	}
    }
    catch (ex) {alert("ex::"+ex);}
    }
}

    function removeEForm(pID,pFID)
    {
	document.getElementById("eupload_"+pID).innerHTML="";
	document.getElementById('upload_'+pID).style.display="block";
	document.getElementById("eparam_"+pID).value=document.getElementById("dparam_"+pID).value;
	document.getElementById("vparam_"+pID).innerHTML=document.getElementById("dparam_"+pID).value;
	document.getElementById("hideforms").removeChild(document.getElementById("form"+pFID));
	removeUploadView("upv"+pFID);
//	document.getElementById(pID).focus();
//	document.getElementById(pID).click();
//	document.getElementById(pID).select();
    }

function getLayer(layerID)
{
//    alert(ns+":"+moz+":"+"ie"+"layer->"+layerID);
    if (ns) return document.layers[layerID];
    else if (moz) return document.getElementById(layerID);
    else if (ie) return eval("document.all." + layerID);
    else return null;
}


function jsSubmitting(pValue)
{
    var x=document.getElementById(pValue).length;
    var elements=document.getElementById(pValue).getElementsByTagName("input");
    alert("form element size:"+x);
    for (var i=0;i<x;i++)
    {
        alert("Form elem:"+elements[i].name+"("+elements[i].id+")"+elements[i].value);
    }
}

function jsIdCheck(pValue,pId)
{
    var x=document.getElementById(pValue).length;
    var elements=document.getElementById(pValue).getElementsByTagName("input");

    for (var i=0;i<x;i++)
    {
//	alert(elements[i].id+":"+pId);
	if(elements[i].id==pId) return true;
    }
    return false;
}



function submitallforms(pNum)
{
    	if(navigator.appName=="Microsoft Internet Explorer")
        {
            try{document.getElementById("pgraf").style.display="none";}
            catch(de){}
            try{document.getElementById("pawkf").style.display="none";}
            catch(de){}
        }

        try
        {
            if(document.getElementById("form"+pNum)!=null)
            {
	    	// submitt elotti ellenorzes
                if( ((document.getElementById("form"+pNum)).getElementsByTagName("input")[0].name=="") || ((document.getElementById("form"+pNum)).getElementsByTagName("input")[0].value=="") )
                    fileUploadErrorFlag++;
// jsSubmitting("form"+pNum);
        		if (fileUploadErrorFlag == 0)
                {
    	    	    getLayer("form"+pNum).submit();
                    var t=setTimeout("getUploadingstatus()", 1000);
                }
// file upload error
                else
                {
            	    alert("File upload error !!! (" + (document.getElementById("form"+pNum)).getElementsByTagName("input")[0].id + ") All configuration data got lost !!!");
        		    document.getElementById('action').value='doConfigure';
                    getLayer("confform").submit();
                }
            }
            else
            {
                if((pNum+1)<=formid)
                {
                    submitallforms(pNum+1);
                    sForm++;
                }
                else
                {
        		    document.getElementById('jmsgreload').style.display="block";
                    document.getElementById('jsmsg').style.display="none";
        		    document.getElementById('jsmsg0').style.display="block";
        		    callflag++;
        		}
            }
        }
        catch (err) {alert("hiba:"+pNum+"::"+err);}
}

    function callDoConfigure()
    {
        try
	{
	    if ((callflag > 0) || (document.getElementById("statusforms").innerHTML.substring(0,6)=="Upload")) {
	        setSaveWorkflow('radioDeleteYes,radioDeleteNo,confIDparam');
	        // document.getElementById('disbl').style.display='none';
    	        document.getElementById('action').value='doConfigure';
    	        getLayer("confform").submit();
	    } else if ((callflag > 0) || (document.getElementById("statusforms").innerHTML.substring(0,5)=="Error")) {
                // document.getElementById('disbl').style.display='none';
	        document.getElementById('action').value='doConfigure';
	        getLayer("confform").submit();
	    }
	    else
	    {
	        var tdo=setTimeout("callDoConfigure()", 1000);
	    }
	}
	catch (err) {
	    alert("...");
	}
    }

    function callDoSaveEWorkflowParams()
    {
        try
	{
	    if ((callflag > 0) || (document.getElementById("statusforms").innerHTML.substring(0,6)=="Upload")) {
	        // document.getElementById('disbl').style.display='none';
    		document.getElementById('action').value='doSaveEWorkflowParams';
    		getLayer("confform").submit();
	    } else if ((callflag > 0) || (document.getElementById("statusforms").innerHTML.substring(0,5)=="Error")) {
	        // document.getElementById('disbl').style.display='none';
    		document.getElementById('action').value='doList';
    		getLayer("confform").submit();
	    } else
	    {
		var tdo=setTimeout("callDoSaveEWorkflowParams()", 1000);
	    }
	}
	catch (err) {
	    alert("...");
	}
    }

    function removeForm(pID,pFID)
    {
	document.getElementById("upload_"+pID).innerHTML="";
	document.getElementById("upload_"+pID).appendChild(document.getElementById(pID));
	document.getElementById("hideforms").removeChild(document.getElementById("form"+pFID));
	removeUploadView("upv"+pFID);
	document.getElementById(pID).focus();
	document.getElementById(pID).click();
	document.getElementById(pID).select();
    }



function get_random()
{
    var ranNum= Math.floor(Math.random()*1000000);
    return ranNum;
}


function viewToolTip(pDiv,pText)
{
    if(document.getElementById(pDiv)==null)
    {
//	Div(100,100,10,10,0,"#ffccdd",pDiv);
	Div(0,0,tempY,tempX+10,0,"#90CCFD",pDiv);
	document.getElementById(pDiv).innerHTML="<div style=\"float:right\"><a href=\"#\" title=\"close\" onclick=\"viewToolTip('"+pDiv+"','')\"><img src=\"/portal30/img/close.gif\" /></a></div><br/>"+pText;
    }
    else
    {
	document.getElementsByTagName('body')[0].removeChild(document.getElementById(pDiv));
	if(pText!="")
	{
	Div(0,0,tempY,tempX+10,0,"#90CCFD",pDiv);
	document.getElementById(pDiv).innerHTML="<div style=\"float:right\"><a href=\"#\" onclick=\"viewToolTip('"+pDiv+"','')\"><img src=\"/portal30/img/close.gif\" /></a></div><br/>"+pText;
	}
//	document.getElementsByTagName('body')[0].removeChild(document.getElementById(pDiv+"bugframe"));
    }
}



function Div(w,h,t,l,p,bg,id)
{
    // define object properties
    div=document.createElement('div');
    div.id=id
//    div.style.width='auto'; //w+'px';
//    div.style.height='auto'; //h+'px';
    div.style.position='absolute';
    div.style.top=t+'px';
    div.style.left=l+'px';
    div.style.padding='5px';
    div.style.border='1px solid #0C49B0';
    div.style.background=bg;

//    iframe=document.createElement('iframe');
//    iframe.id=id+"bugframe";
//    document.getElementsByTagName('body')[0].appendChild(iframe);
    document.getElementsByTagName('body')[0].appendChild(div);
}


function viewHelp(pDiv,pText)
{
    if(document.getElementById(pDiv)==null){
        HelpDiv(0,0,0,0,0,"#90CCFD",pDiv);
        getHelpText(pText);
    }
    else{
        document.getElementsByTagName('body')[0].removeChild(document.getElementById(pDiv));
        if(pText!=""){
            Div(0,0,tempY,tempX+10,0,"#90CCFD",pDiv);
            getHelpText(pText);
        }
    }
}



function HelpDiv(w,h,t,l,p,bg,id)
{
    // define object properties
    div=document.createElement('div');
    div.id=id
    div.style.width='99%'; //w+'px';
    div.style.height='99%'; //h+'px';
    div.style.overflow="auto";
    div.style.position='absolute';
    div.style.top=t+'px';
    div.style.left=l+'px';
    div.style.padding='5px';
    div.style.border='1px solid #0C49B0';
    div.style.background=bg;
    document.getElementsByTagName('body')[0].appendChild(div);
}






var IE = document.all?true:false
if (!IE) document.captureEvents(Event.MOUSEMOVE)
document.onmousemove = getMouseXY;
var tempX = 0
var tempY = 0

function getMouseXY(e)
{
  if (IE)
  { // grab the x-y pos.s if browser is IE
      tempX = event.clientX + document.body.scrollLeft
          tempY = event.clientY + document.body.scrollTop
	    } else {  // grab the x-y pos.s if browser is NS
	        tempX = e.pageX
		    tempY = e.pageY
		      }
		        // catch possible negative values in NS4
			  if (tempX < 0){tempX = 0}
			    if (tempY < 0){tempY = 0}
			      // show the position values in the form named Show
			        // in the text fields named MouseX and MouseY
				  //document.Show.MouseX.value = tempX
				//    document.Show.MouseY.value = tempY
				      return true
				      }



function inputEnabled(p,i)
{
    //alert(""+p+":"+i);
    if(i==0)
    {
        document.getElementById('input_'+p+'_file').disabled=false;
	document.getElementById('input_'+p+'_remote').disabled=true;
        document.getElementById('input_'+p+'_value').disabled=true;
	document.getElementById('input_'+p+'_sqlurl').disabled=true;
        document.getElementById('input_'+p+'_sqlselect').disabled=true;
	document.getElementById('input_'+p+'_sqluser').disabled=true;
        document.getElementById('input_'+p+'_sqlpass').disabled=true;
        document.getElementById('input_'+p+'_remotecopy').disabled=true;
        document.getElementById('input_'+p+'_clist').disabled=true;
        document.getElementById('input_'+p+'_cpropbutton').disabled=true;
        document.getElementById('input_'+p+'_cpropdiv').innerHTML='';
        document.getElementById('sqlblock_'+p).style.display="none";

	document.getElementById('input_'+p+'_remote').value='';
        document.getElementById('input_'+p+'_value').value='';
	document.getElementById('input_'+p+'_sqlurl').value='';
        document.getElementById('input_'+p+'_sqlselect').value='';
	document.getElementById('input_'+p+'_sqluser').value='';
        document.getElementById('input_'+p+'_sqlpass').value='';

    }

    if(i==1)
    {
        document.getElementById('input_'+p+'_file').disabled=true;
	document.getElementById('input_'+p+'_remote').disabled=false;
        document.getElementById('input_'+p+'_value').disabled=true;
	document.getElementById('input_'+p+'_sqlurl').disabled=true;
        document.getElementById('input_'+p+'_sqlselect').disabled=true;
	document.getElementById('input_'+p+'_sqluser').disabled=true;
        document.getElementById('input_'+p+'_sqlpass').disabled=true;
        document.getElementById('input_'+p+'_remotecopy').disabled=false;
        document.getElementById('input_'+p+'_clist').disabled=true;
        document.getElementById('input_'+p+'_cpropbutton').disabled=true;
        document.getElementById('input_'+p+'_cpropdiv').innerHTML='';
        document.getElementById('sqlblock_'+p).style.display="none";

        document.getElementById('input_'+p+'_value').value='';
	document.getElementById('input_'+p+'_sqlurl').value='';
        document.getElementById('input_'+p+'_sqlselect').value='';
	document.getElementById('input_'+p+'_sqluser').value='';
        document.getElementById('input_'+p+'_sqlpass').value='';
	if (document.getElementById('input_'+p+'_gout')!=null){//gemlca remote default input
		document.getElementById('input_'+p+'_remote').disabled=true;
		document.getElementById('input_'+p+'_remote').value='Default';
	}
    }

    if(i==2)
    {
        document.getElementById('input_'+p+'_file').disabled=true;
	document.getElementById('input_'+p+'_remote').disabled=true;
        document.getElementById('input_'+p+'_value').disabled=false;
	document.getElementById('input_'+p+'_sqlurl').disabled=true;
        document.getElementById('input_'+p+'_sqlselect').disabled=true;
	document.getElementById('input_'+p+'_sqluser').disabled=true;
        document.getElementById('input_'+p+'_sqlpass').disabled=true;
        document.getElementById('input_'+p+'_remotecopy').disabled=true;
        document.getElementById('input_'+p+'_clist').disabled=true;
        document.getElementById('input_'+p+'_cpropbutton').disabled=true;
        document.getElementById('input_'+p+'_cpropdiv').innerHTML='';
        document.getElementById('sqlblock_'+p).style.display="none";

	document.getElementById('input_'+p+'_remote').value='';
	document.getElementById('input_'+p+'_sqlurl').value='';
        document.getElementById('input_'+p+'_sqlselect').value='';
	document.getElementById('input_'+p+'_sqluser').value='';
        document.getElementById('input_'+p+'_sqlpass').value='';
    }

    if(i==3)
    {
        document.getElementById('input_'+p+'_file').disabled=true;
	document.getElementById('input_'+p+'_remote').disabled=true;
        document.getElementById('input_'+p+'_value').disabled=true;
	document.getElementById('input_'+p+'_sqlurl').disabled=false;
        document.getElementById('input_'+p+'_sqlselect').disabled=false;
	document.getElementById('input_'+p+'_sqluser').disabled=false;
        document.getElementById('input_'+p+'_sqlpass').disabled=false;
        document.getElementById('input_'+p+'_remotecopy').disabled=true;
        document.getElementById('input_'+p+'_clist').disabled=true;
        document.getElementById('input_'+p+'_cpropbutton').disabled=true;
        document.getElementById('input_'+p+'_clist').disabled=true;
        document.getElementById('input_'+p+'_cpropbutton').disabled=true;
        document.getElementById('input_'+p+'_cpropdiv').innerHTML='';
        document.getElementById('sqlblock_'+p).style.display="block";

	document.getElementById('input_'+p+'_remote').value='';
        document.getElementById('input_'+p+'_value').value='';
	document.getElementById('input_'+p+'_sqluser').value='';
        document.getElementById('input_'+p+'_sqlpass').value='';
    }

    if(i==4)
    {
        document.getElementById('input_'+p+'_file').disabled=true;
	document.getElementById('input_'+p+'_remote').disabled=true;
        document.getElementById('input_'+p+'_value').disabled=true;
	document.getElementById('input_'+p+'_sqlurl').disabled=true;
        document.getElementById('input_'+p+'_sqlselect').disabled=true;
	document.getElementById('input_'+p+'_sqluser').disabled=true;
        document.getElementById('input_'+p+'_sqlpass').disabled=true;
        document.getElementById('input_'+p+'_remotecopy').disabled=true;
        document.getElementById('input_'+p+'_clist').disabled=true;
        document.getElementById('input_'+p+'_cpropbutton').disabled=false;
        document.getElementById('sqlblock_'+p).style.display="none";

	document.getElementById('input_'+p+'_remote').value='';
        document.getElementById('input_'+p+'_value').value='';
	document.getElementById('input_'+p+'_sqluser').value='';
        document.getElementById('input_'+p+'_sqlpass').value='';
    }

    if(i==5)
    {
        document.getElementById('input_'+p+'_file').disabled=true;
	document.getElementById('input_'+p+'_remote').disabled=true;
        document.getElementById('input_'+p+'_value').disabled=true;
	document.getElementById('input_'+p+'_sqlurl').disabled=true;
        document.getElementById('input_'+p+'_sqlselect').disabled=true;
	document.getElementById('input_'+p+'_sqluser').disabled=true;
        document.getElementById('input_'+p+'_sqlpass').disabled=true;
        document.getElementById('input_'+p+'_remotecopy').disabled=true;
        document.getElementById('input_'+p+'_clist').disabled=false;
        document.getElementById('input_'+p+'_cpropbutton').disabled=true;
        document.getElementById('input_'+p+'_cpropdiv').innerHTML='';
        document.getElementById('sqlblock_'+p).style.display="none";

	document.getElementById('input_'+p+'_remote').value='';
        document.getElementById('input_'+p+'_value').value='';
	document.getElementById('input_'+p+'_sqluser').value='';
        document.getElementById('input_'+p+'_sqlpass').value='';
    }


}


function descriptionHandler()
{
    if(document.getElementById('desc_key').value=='')
    {
	document.getElementById('desc_value').disabled=true;
	document.getElementById('desc_butt').disabled=true;
    }
    else
    {
	document.getElementById('desc_value').disabled=false;
	document.getElementById('desc_butt').disabled=false;
    }
}

function gemlcaUI(isG,jID)
{
    if (isG) //gemlca felulet eng.
    {
	document.getElementById('div_wait').style.display='block';
	setRemoteParamDOMValue('job_gridtype,job_grid,job_resource,job_jobmanager');
	getConfForm('m=GetJobView&job='+jID);document.getElementById('cdata').style.display='block';
    }
    else
    {
	//document.getElementById('jobisbinarya').style.display='block';
	//document.getElementById('jobisbinaryag').style.display='block';
	//document.getElementById('jobisbinaryg').style.display='none';
	setRemoteParamDOMValue('job_gridtype');
	getConfForm('m=GetJobView&job='+jID);document.getElementById('cdata').style.display='block';
    }
}

// gemlca parameter check
function gpcheck(formID,defvalue)
{
    if (document.getElementById('job_gparam'+formID).value=='')
    {
	document.getElementById('job_gparam'+formID).value=defvalue;
    }
	i=0;
	newparam='';
	while (document.getElementById('job_gparam'+i)!=null)
	{
	    newparam+=document.getElementById('job_gparam'+i).value+' ';
	    i++;
	}
	document.getElementById('job_params').value=newparam;
//	alert(formID+' '+defvalue);
}

// easy gemlca parameter check
function egpcheck(eparamID,formID,defvalue)
{

    if (document.getElementById('job_gparam'+eparamID+formID).value=='')
    {
	document.getElementById('job_gparam'+eparamID+formID).value=defvalue;
    }
	i=0;
	newparam='';
	while (document.getElementById('job_gparam'+eparamID+i)!=null)
	{
	    newparam+=document.getElementById('job_gparam'+eparamID+i).value+' ';
	    i++;
	}
	//alert(eparamID+' '+formID+' '+defvalue+' new:'+newparam);
	document.getElementById('eparam_'+eparamID).value=newparam;
//	alert(formID+' '+defvalue);
}

// easy - clear property divs
function ecleardivs(){
	for (i=0;i<document.getElementById('easyParamssize').value;i++){
		if(document.getElementById('eparam_'+i+'div')!=null){
			document.getElementById('eparam_'+i+'div').innerHTML="";
		}
	}
}

// easy instance output
function getout(storageID,workflowID,einstanceID){
    document.getElementById('downloadform').action=storageID+"/download";
    document.getElementById('workflowID').value=workflowID;
    document.getElementById('downloadType').value="outputs_"+einstanceID;
    document.getElementById('downloadform').submit();
}

// user interface aktualizalo
function refreshui(jID) {
	//alert('rrrrrjobid:'+jID);
    if (document.getElementById('job_gridtype').value=='gemlca') {
	gemlcaUI(true,jID);
    } else if (document.getElementById('pgridtype').value=='gemlca'){
	gemlcaUI(false,jID);
    }
    // cancer grid ui frisitese
    if (document.getElementById('job_gridtype').value=='DesktopGrid') {
	document.getElementById('cg_bin').style.display='block';
	document.getElementById('er_bin').style.display='none';
	document.getElementById('upload_upload0').style.display='none';
    } else {
	if (document.getElementById('job_gridtype').value=='gUSE') {
	    document.getElementById('cg_bin').style.display='none';
	    document.getElementById('er_bin').style.display='block';
	    document.getElementById('upload_upload0').style.display='block';
	} else {
	    document.getElementById('cg_bin').style.display='none';
	    document.getElementById('er_bin').style.display='none';
	    document.getElementById('upload_upload0').style.display='block';
	}
    }
}

function closediv(div) {
    document.getElementById(div).style.display='none';
    document.getElementById(div).innerHTML='';

}

//jobkonfiguracio

	function getMiddlewarePanel(pParam)
	{
//kommunikacio
	var url= ajaxURL+'&m=GetMiddlewareConfigPanel&j='+pParam;
	request=GetXmlHttpObject(showMiddlewarePanel);
	request.open("POST",url, true);
	request.send("");
}
function showMiddlewarePanel()
{
    try
    {
        if((request.readyState == 4)&&(request.status == 200))
	{
            var resp =  request.responseText;
            if (resp != null)
	    {
            	document.getElementById("middleware-panel").innerHTML=resp;
		if(navigator.appName!="Microsoft Internet Explorer")
		{
		    try{document.getElementById("middleware-panel").style.top="0px";}
		    catch (err) {}
		}
            }
        }
//	else {alert("A problem occurred with communicating between the XMLHttpRequest object and the server program.");}
    }
    catch (err) { alert("It does not appear that the server is available for this application. Please try again very soon. \nError: "+err.message);}
}

        function showMidleware()
        {
            var x=document.getElementById("job_gridtype");
	    txt=x.options[x.selectedIndex].text;
	    getMiddlewarePanel(txt);
    	    document.getElementById("middleware-panel").style.display="block";
        }



