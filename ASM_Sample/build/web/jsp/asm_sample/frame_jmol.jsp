
<script language="JavaScript">
  var inMoleculaVisualizeSize=parent.MoleculaVisualizeSize;
  var inLigandUrlPath=parent.showLigandUrlPath;
</script>

  <script src="${pageContext.request.contextPath}/script/jmol/Jmol.js" type="text/javascript">
  </script>

  <script type="text/javascript">
		jmolInitialize("${pageContext.request.contextPath}/script/jmol");
        jmolApplet(inMoleculaVisualizeSize,   "load ${pageContext.request.contextPath}/script/jmol/model/"+inLigandUrlPath+"");
  </script>
   <a href="" id="moleculelink">Download</a>

  <script>
    document.getElementById('moleculelink').href = '${pageContext.request.contextPath}/script/jmol/model/'+inLigandUrlPath;
  </script>



