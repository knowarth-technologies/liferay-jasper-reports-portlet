<%@ include file="init.jsp"%>

<input type="hidden" id="error" name="Error" value="${ErrorMessage}">

<portlet:resourceURL var="generateReportURL">
	<portlet:param name="action" value="generateReport" />
</portlet:resourceURL>

<portlet:actionURL name="previewReport" var="previewReportURL">
	<portlet:param name="mvcPath" value="/previewReport.jsp" />
</portlet:actionURL>

<c:if test="${ISADMIN=='False'}">
	Report Name : ${ReportName}
</c:if>

<aui:form name="jasperForm" method="post">
		<aui:input name="ReportName" value="${ReportName}" type="hidden">
					<aui:validator name="required"></aui:validator>
		</aui:input>
		<c:forEach items="${MapParameters}" var="mp">
			<c:forEach items="${mp.value}" var="p">
				<aui:input name="Key${p.name}" value="${p.name}" type="hidden">
					<aui:validator name="required"></aui:validator>
				</aui:input>
				<aui:input name="${p.name}${p.type}" value="${p.type}" type="hidden">
					<aui:validator name="required"></aui:validator>
				</aui:input>
				<c:choose>
					<c:when test="${p.type=='java.lang.String'}">
						<aui:input name="${p.name}" label="${p.name}" type="text">
							<aui:validator name="required"></aui:validator>
						</aui:input>
					</c:when>

					<c:when
						test="${p.type=='java.sql.Date' || p.type == 'java.util.Date' || p.type == 'java.sql.Time' || p.type == 'java.sql.Timestamp'}">
						<aui:input name="${p.name}" label="${p.name}" type="date">
							<aui:validator name="required"></aui:validator>
							<aui:validator name="date" errorMessage="Enter Only Date"></aui:validator>
						</aui:input>
					</c:when>
					<c:when test="${p.type=='java.lang.Boolean'}">
						<aui:input name="${p.name}" label="${p.name}" type="checkbox">
							<aui:validator name="required"></aui:validator>
						</aui:input>
					</c:when>

					<c:otherwise>
						<aui:input name="${p.name}" label="${p.name}" type="text"
							id="input-number">
							<aui:validator name="number"></aui:validator>
							<aui:validator name="required" />
						</aui:input>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		</c:forEach>

		<aui:select label="Report Format" name="format">

			<c:forEach items="${ReportFormat}" var="rf">

				<aui:option label="${rf.getValue()}" value="${rf.getValue()}" />

			</c:forEach>
		</aui:select>
		
		<aui:input name="recipient" label="email-recipient" type="text">
			<aui:validator name="email" />
		</aui:input>
		<aui:button-row>
			<button class="btn btn-primary" value="Generate Report" 
				name="<portlet:namespace />generateReport" id="<portlet:namespace />generateRreport" 
				onclick ='<%= renderResponse.getNamespace() + "submitJasperForm(\"submit\");" %>' >Download</button>
			<button class="btn btn-primary" value="Generate Report" 
				name="<portlet:namespace />previewReport" id="<portlet:namespace />previewRreport" 
				onclick ='<%= renderResponse.getNamespace() + "submitJasperForm(\"preview\");" %>' >Preview</button>
		</aui:button-row>
	</aui:form>

<c:choose>
	<c:when test="${Success=='False'}">
		<div style="color: red; font-size: 15px">Unable to generate
			Report. Please Try Again !!</div>
	</c:when>
	<c:when test="${ErrorMessage==''}">
	</c:when>
	<c:otherwise>
		<c:if test="${ISADMIN=='True'}">
			<div style="color: red; font-size: 15px">Please Select
				Definition From Configuration To Generate Report !!!</div>
		</c:if>
	</c:otherwise>
</c:choose>

<script>
	$(document).ready(function() {
		var err = document.getElementById("error").value;
		if (err != '') {
			$('#<portlet:namespace/>generateRreport').hide();
			$('#<portlet:namespace/>previewRreport').hide();
		}
	});
	<portlet:namespace />submitJasperForm = function(action){
		document.<portlet:namespace />jasperForm.target = '_blank';
		if(action === 'preview'){
			document.<portlet:namespace />jasperForm.action = '${previewReportURL}';	
		}else{
			document.<portlet:namespace />jasperForm.action = '${generateReportURL}';	
			
		}
		submitForm(<portlet:namespace />jasperForm);
	};
</script>