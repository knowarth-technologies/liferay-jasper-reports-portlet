<%@ include file="init.jsp"%>
<script src="https://code.jquery.com/jquery-1.11.1.min.js"></script>
<script src="https://cdn.datatables.net/1.10.7/js/jquery.dataTables.min.js"></script>
<link rel="stylesheet" href="https://cdn.datatables.net/1.10.7/css/jquery.dataTables.css">

<c:if test="${ispreviewable=='False'}">
	Unable to Preview Report.Please Try Again Or Generate And Download Report!!
</c:if>

<table id="previewTable" class="display" cellspacing="0" width="100%">
	<thead>
		<tr>
			<c:forEach items="${ColumnList}" varStatus="i">
				<th>${ColumnList[i.index]}</th>
			</c:forEach>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${mapdata}" var="md">
			<tr>
				<c:forEach items="${md.value}" varStatus="ind">
					<td>${md.value[ind.index]}</td>
				</c:forEach>
			</tr>
		</c:forEach>
	</tbody>
</table>

<script>
	$(document).ready(function() {
		$('#previewTable').DataTable();
	});
</script>