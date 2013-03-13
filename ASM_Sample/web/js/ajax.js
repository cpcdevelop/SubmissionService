var formObj = null;
var formObjTyp = "";
var request=null;
var enabledlogg="false";
var lrequest=null;
//binaris adatok
var pgrid=null;
var vjob="";
var vjport="";
var vportold="";
//input field's event handlers
	var oldJobConf='';
	var slink;
	var prParams="";
//gridek lekerdezese

function getSID()
{
//    alert(document.cookie);
    var sid0=document.cookie.split(";");
    var sid;
    for(i=0;i<sid0.length;i++)
    {	
	sid=sid0[i].split("=");
//	alert(":::"+sid[0]+":"+sid[1]);		    
	if((sid[0]=="JSESSIONID")||(sid[0]==" JSESSIONID"))
	{
//	    alert(document.cookie+" -> SID:"+sid[1]);	    
	    return sid[1];
	}
//	else alert("KEY:"+sid[0]);	    
    }

}

function getUploadingstatus()
{
//	var url= storageID+'/upload?sid='+getSID()+confID;
	var url= uploadStatusURL;//storageID+'/upload?sid='+getSID()+confID;
	request=GetXmlHttpObject(uploading);
	request.open("GET",url, true);
	request.send("");
}

function sendFinalUploading()
{
//	var url= storageID+'/upload?endsid='+getSID()+confID;
//	request=GetXmlHttpObject(uploading);
//	request.open("GET",url, true);
//	request.send("");
	document.getElementById('jmsgreload').style.display="block";
	document.getElementById('jsmsg').style.display="none";
	document.getElementById('jsmsg0').style.display="block";
	
//        callDoConfigure();
}

function uploading()
{
    try
    {
        if((request.readyState == 4)&&(request.status == 200))
	{
	    var res=request.responseText;
	    var full=getElementNumber(res,"100 %");
	    var per=getElementNumber(res,"%");
//	    alert(""+full+"/"+per+"->"+res)	
  	    document.getElementById("statusforms").innerHTML=res;
	    if(((res.indexOf("100 %")==(-1))&&(res.substring(0,6)!="Upload"))||(full!=per)) 
	    {
//            alert("upload form"+sForm+":"+settimeout);
            var t=setTimeout("getUploadingstatus()",2000);
	    }
	    else
	    { 
            sForm++;
//            alert("upload form"+sForm+":"+formid);
            if(sForm<=formid)
            {
                submitallforms(sForm);
            }
            else if(res.substring(0,6)=="Upload")
            {
                sendFinalUploading();
//                sForm--;
            }
	    }
	}
//	else {alert("A problem occurred with communicating between the XMLHttpRequest object and the server program.");}
    } 
    catch (err) { alert("It does not appear that the server is available for this application. Please try again very soon. \nError: "+err.message);}
}

function getElementNumber(pSrc,pSub)
{
    var i=0;
    var st=0;
    while(pSrc.indexOf(pSub,st)!=(-1)){st=pSrc.indexOf(pSub,st)+1;i=i+1;}
    return i;
    
}


function setRemoteParam(pParam)
{
	var url= ajaxURL+'&'+pParam;
	request=GetXmlHttpObject(fake);
	request.open("POST",url, false);
	request.send("");
}
function setRemoteParamDOMValue(pParams)
{

	if(pParams=="null")pParams=prParams;
//	var t=pParams.replace(",",";");
	var p = new Array();
	p=pParams.split(",");
	
	var url= ajaxURL+"&m=SaveAllData";
	    for(k=0;k<p.length;k++)
	    {
		if(p[k]!="")
		{
		    try
		    {
    			if(document.getElementById(p[k]).type=="checkbox")
			{	
			    
			    if(document.getElementById(p[k]).checked==true)
				url=url+"&"+p[k]+"="+document.getElementById(p[k]).value;
			}
			else
			    url=url+"&"+p[k]+"="+escape(document.getElementById(p[k]).value);
		    }	
		    catch(ee){alert("exception:"+ee);alert("value:"+p[k]);alert("type: "+document.getElementById(p[k]));}
		}
	    }    
	request=GetXmlHttpObject(fake);
	request.open("POST",url, false);
	request.send("");
}

function setSaveWorkflow(pParams)
{
//	document.getElementById('jsmsg').innerHTML="Workflow is saved";
//	document.getElementById('jmsgreload').style.display="block";
//	document.getElementById('jcancel').style.display="none";

	var p=pParams.split(",");
	var url= ajaxURL+"&m=SendSavedData";
	for (k in p) 
	{
	    if(p[k]!="")
	    {
		try
		{
		    if(document.getElementById(p[k]).type=="radio")
    		    {
			if(document.getElementById(p[k]).checked==true)
			{
//			    url=url+"&"+p[x]+"="+escape(document.getElementById(p[x]).value);
			    url=url+"&"+p[k]+"=true";
			}
			else 
			{
			    url=url+"&"+p[k]+"=false";
			}
		    }
		    else
			url=url+"&"+p[k]+"="+escape(document.getElementById(p[k]).value);
		}
		catch(ee){alert(p[k]);}
	    }
	}
//	alert(url);
	request=GetXmlHttpObject(fake);
	request.open("POST",url, false);
	request.send("");
}

function fake()
{
    try
    {
        if((request.readyState == 4)&&(request.status == 200))
	{
	    document.getElementById("jsmsg").innerHTML=request.responseText;
	}
    } 
    catch (err) { alert("It does not appear that the server is available for this application. Please try again very soon. \nError: "+err.message);}
}

// Easy grid parameterek kezelese
function getRemoteSelectOptionsE(pParam)
{
	var url= ajaxURL+'&'+pParam;
	request=GetXmlHttpObject(setSelectOptionsE);
	request.open("POST",url, false);
	request.send("");
}
// Easy grid parameterek kezelese
function setSelectOptionsE()
{
    try
    {
        if((request.readyState == 4)&&(request.status == 200))
        {
            var resp =  request.responseText;
            if (resp != null)
    	    {
            	var opts=resp.split("::");
        		document.getElementById(opts[0]).options.length = 0;
                for(i=2;i<(opts.length-1);i++)
        		{
                    if(opts[i]!="")
        		    {
                    	document.getElementById(opts[0]).options[i-2] = new Option(opts[i],opts[i]);
            			document.getElementById(opts[0]).options[i-2].value=opts[i];
                        if(opts[i]==opts[1])document.getElementById(opts[0]).options[i-2].selected="true";
        		    }
                }
            }
        } 
    } 
    catch (err) { alert("It does not appear that the server is available for this application. Please try again very soon. \nError: "+err.message);}
    if(opts[opts.length-1]!="")getRemoteSelectOptionsE("m="+opts[opts.length-1]);

}// E





function getRemoteSelectOptions(pParam)
{
	var url= ajaxURL+'&'+pParam;
	request=GetXmlHttpObject(setSelectOptions);
	request.open("POST",url, false);
	request.send("");
}

function setSelectOptions()
{
    try
    {
        if((request.readyState == 4)&&(request.status == 200))
        {
            var resp =  request.responseText;
            if (resp != null)
    	    {
            	var opts=resp.split("::");
        		document.getElementById(opts[0]).options.length = 0;
                j=0;
                for(i=2;i<(opts.length-1);i++)
        		{
                    if(opts[i]!="")
                    {
                    	document.getElementById(opts[0]).options[j] = new Option(opts[i],opts[i]);
            			document.getElementById(opts[0]).options[j].value=opts[i];
                        if(opts[i]==opts[1])document.getElementById(opts[0]).options[j].selected="true";
                        j++;
        		    }
                }
                try
                {
                    if (opts[1]==""){
                        document.getElementById(opts[0]).options[0].selected="true";
                    }
                }catch (err) {}

                if(opts[0]=="job_grid")
                {
                    var job_middleware=document.getElementById('job_gridtype').value;
                    var job_grid=document.getElementById('job_grid').value;
                    getRemoteSelectOptions('mw='+job_middleware+'&j='+job_grid+'&m=GetResource');
                }
                
                if(opts[0]=="job_resource")
                {
                    var job_middleware=document.getElementById('job_gridtype').value;
                    var job_grid=document.getElementById('job_grid').value;
                    var job_resource=document.getElementById('job_resource').value;
                    getRemoteSelectOptions('cc=qq&mw='+job_middleware+'&g='+job_grid+'&j='+job_resource+'&m=GetData');
                }

            }
        } 
//	else {alert("A problem occurred with communicating between the XMLHttpRequest object and the server program.");}
    } 
    catch (err) { alert("It does not appear that the server is available for this application. Please try again very soon. \nError: "+err.message);}
//    if(opts[opts.length-1]!="")getRemoteSelectOptions("m="+opts[opts.length-1]);

}



function getRemoteTextareaOptions(pParam)
{
	var url= ajaxURL+'&'+pParam;
	request=GetXmlHttpObject(setDesc);
	request.open("POST",url, false);
	request.send("");
}

function setDesc()
{
    try
    {
        if((request.readyState == 4)&&(request.status == 200))
	{
            var resp =  request.responseText;
            if (resp != null)
	    {
		var opts=resp.split("::");
		document.getElementById('desc_txt').innerHTML=opts[0];
		document.getElementById('desc_value').value=opts[1];
            }
        } 
//	else {alert("A problem occurred with communicating between the XMLHttpRequest object and the server program.");}
    } 
    catch (err) { alert("It does not appear that the server is available for this application. Please try again very soon. \nError: "+err.message);}
}


function getNativeText(pParam)
{
	var url= pParam;
	request=GetXmlHttpObject(setText);
	request.open("POST",url, false);
	request.send("");
}

function setText()
{
    try
    {
        if((request.readyState == 4)&&(request.status == 200))
	{
            var resp =  request.responseText;
            if (resp != null)
	    {
		var opts=resp.split("::");
		document.getElementById(opts[0]).value=opts[1];
            }
        } 
//	else {alert("A problem occurred with communicating between the XMLHttpRequest object and the server program.");}
    } 
    catch (err) { alert("It does not appear that the server is available for this application. Please try again very soon. \nError: "+err.message);}
}

var comParams;
var comAction=true;
//behuzza a szukseges 
function getConfForm(pParam)
{
    TINY.box.show(loadingconf_txt,0,300,300,1);
        comParams=pParam;
//kommunikacio

	var url= ajaxURL+'&'+pParam;
	request=GetXmlHttpObject(openConf);
	request.open("POST",url, true);
	request.send("");

    
}

function openConf()
{
	try
	{
        var jjob=comParams.split("job=");
        if(jjob.length>1)
    	{
//            document.getElementById("gjob_"+jjob[1]).style.background="#ff0000";
            if(vjport!=""){document.getElementById("gport_"+vjport).style.background=vportold;}
            vjport="";
	    }
	}
	catch(err){}
    var jport=comParams.split("port=");
    var typee=comParams.split("=input");
	try
	{
	    if((jport.length>1)&&(jport[1]!=vjport))
	    {
	        document.getElementById("gport_"+jport[1]).style.background="#ff0000";
            if(vjport!=""){document.getElementById("gport_"+vjport).style.background=vportold;}
            if(typee.length>1)vportold="#92ee92";
            if(typee.length==1)vportold="#d2d2d2";
            vjport=jport[1];
	    }
	}
	catch(err){}

    try{
        if((request.readyState == 4)&&(request.status == 200)){
            var resp =  request.responseText;
            if (resp != null)
            {
        		var opts=resp.split("<!-- div id -->");
                if(opts.length==2){
    		    document.getElementById(opts[0]).innerHTML=opts[1];
                ReRunJS(opts[0]);
            }
            else{
            	document.getElementById("cdata").innerHTML=resp;
                ReRunJS("cdata");
            }
//		if((navigator.appName=="Opera")||(navigator.appName=="Microsoft Internet Explorer"))
    		if(navigator.appName!="Microsoft Internet Explorer")
        	{
                try{document.getElementById("cdata").style.top="0px";}
                catch (err) {}
    		}
        }
} 
//	else {alert("A problem occurred with communicating between the XMLHttpRequest object and the server program.");}
    } 
    catch (err) { alert("It does not appear that the server is available for this application. Please try again very soon. \nError: "+err.message);}
    TINY.box.hide();
}

function getHelpText(pKey)
{
	var url= ajaxURL+'&helptext='+pKey;
    request=GetXmlHttpObject(setHelpText);
    request.open("POST",url, true);
    request.send("");

}

function setHelpText()
{
    try
    {
        if((request.readyState == 4)&&(request.status == 200))
	{
            var resp =  request.responseText;
            if (resp != null)
	    {
		document.getElementById('helptext').innerHTML="<div style=\"float:right;\"><a href=\"#\" onclick=\"viewToolTip('helptext','')\"><img src=\""+webapp+"/img/close.gif\" /></a></div><br/>"+resp;
            }
        } 
//	else {alert("A problem occurred with communicating between the XMLHttpRequest object and the server program.");}
    } 
    catch (err) { alert("It does not appear that the server is available for this application. Please try again very soon. \nError: "+err.message);}
}


function getGLITEconfig()
{
    var x=document.getElementById("vo-name");
    var voname=x.options[x.selectedIndex].text;
	var url= ajaxURL+'&vo='+voname;
    request=GetXmlHttpObject(setGLITEconfig);
    request.open("POST",url, true);
    request.send("");
}

function setGLITEconfig()
{
    try
    {
        if((request.readyState == 4)&&(request.status == 200))
        {
            var resp =  request.responseText;
            if (resp != null)
            {
                document.getElementById('glite-config').innerHTML=resp;
            }
        }
//	else {alert("A problem occurred with communicating between the XMLHttpRequest object and the server program.");}
    }
    catch (err) { alert("It does not appear that the server is available for this application. Please try again very soon. \nError: "+err.message);}

}


function GetXmlHttpObject(handler)
{ 
    var objXmlHttp=null
    if (navigator.userAgent.indexOf("MSIE")>=0)
    { 
	var strName="Msxml2.XMLHTTP"
	if (navigator.appVersion.indexOf("MSIE 5.5")>=0)
	{strName="Microsoft.XMLHTTP"} 
	try
	{
	    objXmlHttp=new ActiveXObject(strName)
	    objXmlHttp.onreadystatechange=handler 
	    return objXmlHttp
	} 
	catch(e)
	{ 
	    alert("Error. Scripting for ActiveX might be disabled") 
	    return 
	} 
    } 
    else
//    if ((navigator.userAgent.indexOf("Mozilla")>=0)||(navigator.userAgent.indexOf("Opera")>=0))
    {
	objXmlHttp=new XMLHttpRequest()
	objXmlHttp.onload=handler
	objXmlHttp.onerror=handler 
	return objXmlHttp
    }
} 

		function openClosePanel(pDiv,pImg)
		{
		    var x=document.getElementById(pDiv);
		    if(x.style.display=="block")
		    {
			x.style.display="none";
			document.getElementById(pImg).src="/portal30/imgs/window/maximize.png";
		    }
		    else 
		    {
			x.style.display="block";
			document.getElementById(pImg).src="/portal30/imgs/window/minimize.png";
		    }
		}



