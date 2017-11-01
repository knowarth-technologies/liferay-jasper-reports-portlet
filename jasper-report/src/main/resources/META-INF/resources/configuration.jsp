<%@include file="init.jsp"%>

<liferay-portlet:actionURL var="configurationActionURL"
	portletConfiguration="true" />
<c:choose>
	<c:when test="${SelectedDefinition ==''}">
		<c:set var="selectedDefinition" value="0"/>
	</c:when>
	<c:otherwise>
		<c:set var="selectedDefinition" value="${SelectedDefinition}" />
	</c:otherwise>
</c:choose> 

<form action="${configurationActionURL.toString()}" method="post"
	name="configurationForm">
	<input name="<portlet:namespace/>cmd" type="hidden" value="update" />
	<div class="form-group">
		<label>Select Role</label> <select name="<portlet:namespace/>roles"
			class="form-control" id="role-list" required="required">
			<c:forEach var="r" items="${RolesList}">
				<c:choose>
					<c:when test="${r.getRoleId()==SelectedRole}">
						<option value="${r.getRoleId()}" selected="true">${r.getName()}</option>
					</c:when>
					<c:otherwise>
						<option value="${r.getRoleId()}">${r.getName()}</option>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		</select>
	</div>
	<div class="form-group">
		<label>Select Definition</label> <select
			name="<portlet:namespace/>definitions" class="form-control"
			id="role-based-definition" required="required">
		</select>
	</div>
	<div class="form-group">
		<label>Report Name</label> <input
			name="<portlet:namespace/>reportName" class="form-control"
			id="report-name" required="required" type="text" value="${ReportName}">
		</select>
	</div>
	<button class="btn btn-primary" type="submit">Submit</button>
</form>
<liferay-portlet:resourceURL var="fetchDefinitionURL"
	id="FetchDefinitionByRole"
	portletName="com_knowarth_report_portlet_JasperReportPortlet">
</liferay-portlet:resourceURL>

<script>
$(document).ready(function(){
	var selectedDefId=${selectedDefinition};
	$('#role-list').on("load",getDefinitionList(selectedDefId));
	$('#role-list').on("change",function(){
		$('#role-based-definition option').remove();
		var roleId=$("#role-list").val();
		$.ajax({
	    	type: "POST",
	    	url: "${fetchDefinitionURL}",
	    	data: {
	    		role: roleId,
	    	},
	    	success: function(data){
	    		var jsonData = JSON.parse(data);
	            for (var i = 0; i < jsonData.jsonDefinition.length; i++) {
	                var def = jsonData.jsonDefinition[i];
	                var definitionName=def.name;
	                var definitionid=def.id;
	                $('#role-based-definition').append( '<option value='+definitionid+'>' + definitionName + '</option>' );
	            }
	        }
		}); 
	});
});
var getDefinitionList = function(id){
	var roleId=$("#role-list").val();
	$.ajax({
    	type: "POST",
    	url: "${fetchDefinitionURL}",
    	data: {
    		role: roleId,
    	},
    	success: function(data){
    		var jsonData = JSON.parse(data);
            for (var i = 0; i < jsonData.jsonDefinition.length; i++) {
                var def = jsonData.jsonDefinition[i];
                var definitionName=def.name;
                var definitionid=def.id;
                if(id==definitionid)
                	$('#role-based-definition').append( '<option value='+definitionid+' selected="true">' + definitionName + '</option>' );
                else
                	$('#role-based-definition').append( '<option value='+definitionid+'>' + definitionName + '</option>' );
            }
        }
	});
};
</script>

