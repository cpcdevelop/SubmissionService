<%-- 
    Document   : show_input_details
    Created on : 21-Aug-2012, 16:38:43
    Author     : sasvara
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
  
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>            
        <select id="where2upload" name="where2upload">                   

            <c:forEach var="inst" items="${asm_instances}">
                <c:if test="${ inst.workflowName eq 
                                   '112_20120821132020270_2012-08-21-132033'}">
                    <c:forEach var="jobs" items="${inst.jobs}">

                        <c:forEach var="ports" items="${jobs.value.input_ports}">
                            <option value="${jobs.key}@${ports.key}@${ports.value}">
                                ${jobs.key} job's ${ports.key} 's port ( ${ports.value} )
                            </option>
                        </c:forEach>

                    </c:forEach>
                </c:if>
            </c:forEach>
        </select>
    </body>
</html>
