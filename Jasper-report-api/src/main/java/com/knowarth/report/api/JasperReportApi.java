package com.knowarth.report.api;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ResourceResponse;

import com.knowarth.report.api.bean.ParameterDetail;
import com.liferay.portal.reports.engine.console.model.Entry;

/**
 * @author vishal.munjani
 */
public interface JasperReportApi {
	public Map<String, List<ParameterDetail>> getDefinitionParameters(RenderRequest renderRequest, long definitionId);
	public void getDefinitionByRole(ResourceResponse resourceResponse, long roleId);
	public Entry generateReport(PortletRequest portletRequest, String format, long definitionId,
			String reportName, boolean isPreview);public void previewReport(PortletRequest portletRequest, String reportName, Entry entry, String format);
}