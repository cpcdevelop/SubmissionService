<!--
Workfflow details main page
-->

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:resourceURL var="ajaxURL" />
<script>
    var ajaxURL="${ajaxURL}";
    var loadingconf_txt='<msg:getText key="portal.config.loadingconfig" />';
</script>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/asmlog.css?v=1"
      type="text/css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/facebox/facebox.css?v=1"
      type="text/css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/facebox/facebox.js?v=1"></script>

<div id="rwlist" style="position:relative;">


<portlet:defineObjects/>
<portlet:actionURL var="pURL" portletMode="VIEW" />


<table class="portlet-pane" cellspacing="1" cellpadding="1" border="0" width="100%" >
<tr><td>
    <form method="post" action="${pURL}">
        <input type="hidden" name="user_selected_instance" id="user_selected_Instance" value="${selected_Instance}">
        <input type="hidden" name="guse" id="action">
        <input type="hidden" name="action" id="action" value="doDetails">
    <input type="submit" value="Refresh" class="portlet-form-button">
    </form>

    <form method="post" action="${pURL}">
     <input type="hidden" name="guse" id="action">
     <input type="hidden" name="action" id="action" value="doGoBack">
     <input type="submit" value="Back" class="portlet-form-button">
    </form>
    <table width="100%" class="kback">
        <!--
        <tr>
	    
	    <td width="100%" colspan="4" style="border-bottom:solid 1px #ffffff;">
		<b><msg:getText key="text.wrkinst.instancename" /></b> ${rtid} </td>
	</tr>	    
        -->
        <c:forEach var="job" items="${workflow_details.jobs}" varStatus="ln">
	    <c:choose>
		<c:when test="${(ln.index%2)==1}">
	    	    <c:set var="color" value="kline1" />
		</c:when>
		<c:otherwise>
	    	    <c:set var="color" value="kline0" />
		</c:otherwise>
	    </c:choose>

		<tr>
		    <td class="${color}"> ${job.name} </td>
		    <td colspan="2"  class="${color}">
			<table width="100%" class="kback">
			<c:forEach var="overviewedstat" items="${job.statisticsBean.overviewedstatuses}">
			 
			    <tr>
			
                                <td width="25%" bgcolor="${statuscolors.statuscolors[statusconstants.statuses[overviewedstat.statuscode]]}">${statusconstants.statuses[overviewedstat.statuscode]} </td>
				<td class="${color}" width="25%">${overviewedstat.numberofinstances}</td>




                                <td> 
                     <input type="button" value="Show Instances" onclick="javascript:document.getElementById('instances_${job.name}').style.display='block';return false;"/>
                                </td>
                           </tr>
                           <tr>
                               <td>
                                <div id="instances_${job.name}" style="display:none;">
                                    <input type="button" value="Hide Instance Details" class="portlet-form-button" onClick="javascript:
					document.getElementById('instances_${job.name}').style.display='none';"/>
                                    <table>
                                        <tr>
                                            <td>ID</td>
                                            <td>Resource</td>
                                            <td>Status</td>
                                            <c:if test="${(bjob.status==6) || (bjob.status==7)|| (bjob.status==9)}">
                                                <td colspan="3">Actions</td>
                                            </c:if>
                                        </tr>
                                <c:forEach var="bjob" items="${job.instances}" varStatus="ln">
                                        <c:choose>
                                            <c:when test="${(ln.index%2)==1}">
                                                <c:set var="color" value="kline1" />
                                            </c:when>
                                            <c:otherwise>
                                                <c:set var="color" value="kline0" />
                                            </c:otherwise>
                                        </c:choose>
                                        <tr>
                                            <td class="${color}" width="10%">${bjob.id} </td>
                                            <td class="${color}" width="30%">${bjob.usedResource}</td>
					<td width="25%" bgcolor="${statuscolors.statuscolors[statusconstants.statuses[bjob.status]]}">${statusconstants.statuses[bjob.status]} </td>
                                            <td>
                                                <c:if test="${(bjob.status==6) || (bjob.status==7)|| (bjob.status==9)}">
                                                <table>
                                                <tr>
                                                    <td>
                                                        <input type="button" value="Show Std. Output" onclick="jQuery.facebox({ div: '#output_${job.name}${bjob.id}' });">
                                                        <div id="output_${job.name}${bjob.id}" style="display:none;">
                                                            <p> ${bjob.outputText}
                                    <!--                    <input type="button" class="portlet-form-button" onclick="javascript:hide('output_${job.name}${bjob.id}')" value="<msg:getText key='button.cancel' />" /> -->
                                                               </p>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <input type="button" value="Show Std. Error" onclick="jQuery.facebox({ div: '#error_${job.name}${bjob.id}' });">
                                                        <div id="error_${job.name}${bjob.id}" style="display:none;">
                                                            ${bjob.errorText}
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <input type="button" value="Show System Log" onclick="jQuery.facebox({ div: '#logbook_${job.name}${bjob.id}' });">
                                                        <div id="logbook_${job.name}${bjob.id}" style="display:none;">
                                                        ${bjob.logbookText}
                                                        </div>
                                                    </td>

                                                </tr>
                                                </table>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </table>
                                   </div>
                                    </td>
                            </tr>
                               
			</c:forEach>
			</table>
		    </td>
		</tr>
		<tr>
		    <td colspan="4">
			<div id="jobinsttatus_${job.name}"></div>
		    </td>
		</tr>
	</c:forEach>    
    </table>

</td></tr>
</table>    
</div>
