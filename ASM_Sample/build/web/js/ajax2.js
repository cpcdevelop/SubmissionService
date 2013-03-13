var dest="";
function requestFunction(a,m,p,f)
{ //address/method/parameters/function
    var xmlHttp;
    if(window.XMLHttpRequest){xmlHttp=new XMLHttpRequest();}else if(window.ActiveXObject){xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");}

    if(xmlHttp!=null)
    {

	xmlHttp.onreadystatechange=function()
        {
	    if(xmlHttp.readyState==4)
            {
		if(xmlHttp.status==200)
                {
		    var r=new String;
		    r=xmlHttp.responseText;
		    f(r);
		}
                else{alert('Hiba: '+xmlHttp.status+'\r\n'+xmlHttp.statusText);}
	    }
	}

  	switch(m)
        {
	    case 'GET':
		xmlHttp.open('GET',a,true);
		xmlHttp.send(null);
		break;
	    case 'POST':
		xmlHttp.open('POST',a,true);
		xmlHttp.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=utf-8');
		xmlHttp.send(p);
		break;
	}

    }
    else{alert('A folyamat megszakadt!\n- A bÃ¶ngÃ©szÅ‘ nem tÃ¡mogatja az XMLHTTP hasznÃ¡latÃ¡t -');}
}

function formsubmit(e,f)
{
    switch(e.tagName)
    {
	case 'A':
	    var h=e.getAttribute('href');
	    requestFunction(h+'&submit=link','GET','',f);
	    break;
	case 'FORM':
	    var a=e.getAttribute('action');

	    var I,S,T;
	    I=e.getElementsByTagName('input');
	    S=e.getElementsByTagName('select');
	    T=e.getElementsByTagName('textarea');

	    var j,i,s,t;
	    var p=''; //AJAX-al tovÃ¡bbkÃ¼ldÃ¶tt paramÃ©terek
	    for(var n=0; s=S[n]; n++){p+=s.getAttribute('name')+'='+s.value+'&';} //select

	//input
	    for(j=0; i=I[j]; j++)
	    {
		var type=i.getAttribute('type');
		if(type=='checkbox' || type=='radio'){if(i.checked){p+=i.getAttribute('name')+'='+i.value+'&';}}
		if(type=='hidden' || type=='password' || type=='text'|| type=='file'){p+=i.getAttribute('name')+'='+i.value+'&';}
	    }

	    for(var n=0; t=T[n]; n++){p+=t.getAttribute('name')+'='+t.value+'&';} //textarea
//	    alert(a+"?"+p);
	    requestFunction(a,'POST',p,f);
	    break;
	}
	return false;
}



function getValue(e,d)
{
    dest=d;
    document.getElementById(dest).disabled=false;
    return formsubmit(e,setvalue);
}

function getHTML(e,d)
{
    dest=d;
    document.getElementById(dest).style.display="block";
    return formsubmit(e,viewtext);
}

function viewtext(pValue)
{
    try
    {
        document.getElementById(dest).innerHTML=pValue;
        ReRunJS(dest);
    }
    catch(e){}
}

function setvalue(pValue)
{
    try{document.getElementById(dest).value=pValue;}
    catch(e){}
}

function ReRunJS(id)
{
    var ObjJS=document.getElementById(id).getElementsByTagName('script');
    for(var i=0; i<ObjJS.length; i++)
    {
        var TempSrc=ObjJS[i].getAttribute('src');
        var NewScript=document.createElement('script');
        NewScript.setAttribute('type','text/javascript');
        if(TempSrc){NewScript.setAttribute('src',TempSrc);}
        else{NewScript.text=ObjJS[i].text;}
        ObjJS[i].parentNode.replaceChild(NewScript,ObjJS[i]);
    }
}

    function fakeSave(pValue){}

    function saveGridJobData(pForm,pJobID)
    {
        formsubmit(pForm,fakeSave);
        openClosePanel('cdata','config-icon');
    	additem('upload0','binary',pJobID,'job_'+pJobID+'_binary');
        return false;
    }
    

function OnlyInteger(id)
{
	var Obj=id;//document.getElementById(id);
	var Text=Obj.value;
	var TempText='';
	var TempChar='';
	for(var i=0; i<Text.length; i++)
	{
        TempChar=Text.substr(i,1);
        if(/^[0-9]$/i.test(TempChar))
        {TempText+=TempChar;}
	}
	Obj.value=TempText;
}