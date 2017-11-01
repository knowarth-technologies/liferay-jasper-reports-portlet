package com.knowarth.report.portlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import com.knowarth.report.api.JasperReportApi;
import com.knowarth.report.api.bean.ParameterDetail;
import com.knowarth.report.api.contants.JasperConstants;
import com.knowarth.report.portlet.config.action.ReportConfiguration;
import com.liferay.document.library.kernel.store.DLStoreUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.reports.engine.ReportFormat;
import com.liferay.portal.reports.engine.console.model.Entry;

@Component(
		configurationPid="com.knowarth.report.portlet.config.action.ReportConfiguration",
		immediate = true, 
		property = { 
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.instanceable=true", 
		"javax.portlet.display-name=Jasper Report Portlet",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"javax.portlet.name=com_knowarth_report_portlet_JasperReportPortlet",
		"javax.portlet.init-param.config-template=/configuration.jsp" }, service = Portlet.class)
public class JasperReportPortlet extends MVCPortlet {
	private volatile ReportConfiguration reportConfiguration;
	private static final Log _log = LogFactoryUtil.getLog(JasperReportPortlet.class);
	private long definitionId;
	private JasperReportApi jasperReportApi;

	@Reference
	public void setHrmsClientAPI(JasperReportApi jasperReportApi) {
		this.jasperReportApi = jasperReportApi;
	}

	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {
		String reportName = StringPool.BLANK;
		Map<String, List<ParameterDetail>> mapParameter = new HashMap<>();
		String defId = renderRequest.getPreferences().getValue(JasperConstants.DEFINITION_ID,StringPool.BLANK);
		if (Validator.isNotNull(defId)) {
			definitionId = Long.valueOf(defId);
			mapParameter=jasperReportApi.getDefinitionParameters(renderRequest, definitionId);
			reportName=renderRequest.getPreferences().getValue(JasperConstants.REPORT_NAME_CONFIG,StringPool.BLANK);
			renderRequest.setAttribute(JasperConstants.REPORT_NAME,reportName);
			renderRequest.setAttribute(JasperConstants.ERROR_MESSAGE,StringPool.BLANK);
			renderRequest.setAttribute(JasperConstants.REPORT_FORMAT, ReportFormat.values());
			renderRequest.setAttribute(JasperConstants.MAP_PARAMETER, mapParameter);
		}else{
			renderRequest.setAttribute(JasperConstants.ERROR_MESSAGE,JasperConstants.ERROR_MESSAGE_VALUE);
		}
		
		PermissionChecker permissionChecker = PermissionThreadLocal.getPermissionChecker();
		if(permissionChecker.isOmniadmin() || renderRequest.isUserInRole(RoleConstants.ADMINISTRATOR)){
			renderRequest.setAttribute(JasperConstants.IS_ADMIN,JasperConstants.TRUE);
		}else{
			renderRequest.setAttribute(JasperConstants.IS_ADMIN,JasperConstants.FALSE);	
		}
		super.render(renderRequest, renderResponse);
	}
	
	@ProcessAction(name = "previewReport")
	public void previewReport(ActionRequest actionRequest, ActionResponse actionResponse) {
		String format = "xml";
		String reportName= ParamUtil.getString(actionRequest,JasperConstants.REPORT_NAME);
		jasperReportApi.generateReport(actionRequest,format, definitionId,reportName,true);
	}

	@Override
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws IOException, PortletException {
		String action = ParamUtil.getString(resourceRequest, "action", StringPool.BLANK);
		ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
		HttpServletRequest httpServletRequest = PortalUtil.getHttpServletRequest(resourceRequest);
		HttpServletResponse httpServletResponse = PortalUtil.getHttpServletResponse(resourceResponse);
		
		if(action.equals("generateReport")){
			try{
				_log.info(":::: Generating Report .... ::::");
				String format = ParamUtil.getString(resourceRequest,JasperConstants.FORMAT);
				String reportName= ParamUtil.getString(resourceRequest,JasperConstants.REPORT_NAME);
				_log.info(":::: Generating Report in format ::::" + format);
				
				_log.info(":::: Generating Report in Name  ::::" + reportName);
				
				Entry entry=jasperReportApi.generateReport(resourceRequest,format, definitionId, reportName,false);
				_log.info(":::: Report generated successfully in format ::::" +format);
				reportName = reportName + StringPool.PERIOD + format;
				Thread.sleep(1000);
				_log.info(":::: Downloading report in format ::::" +format);
				String fileLocation = entry.getAttachmentsDir().concat(StringPool.SLASH).concat(reportName);
				String shortFileName = StringUtil.extractLast(fileLocation, StringPool.SLASH);
				InputStream inputStream = DLStoreUtil.getFileAsStream(themeDisplay.getCompanyId(),
						CompanyConstants.SYSTEM, fileLocation);
				String contentType = MimeTypesUtil.getContentType(fileLocation);
				
				ServletResponseUtil.sendFile(httpServletRequest, httpServletResponse, shortFileName, inputStream, contentType);
				_log.info(":::: Report downloaded successfully in format ::::");
			}catch(Exception e){
				_log.error(":::: Error while generating report .... ::::" + e);
			}
			
		} else {
			UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(resourceRequest);
			String roleId = uploadPortletRequest.getParameter(JasperConstants.ROLE);
			jasperReportApi.getDefinitionByRole(resourceResponse, Long.parseLong(roleId));
		}
		super.serveResource(resourceRequest, resourceResponse);
	}
	
	@Activate
	@Modified
	protected void activate(Map<Object, Object> properties) {
		reportConfiguration = ConfigurableUtil.createConfigurable(
			ReportConfiguration.class, properties);
	}
}