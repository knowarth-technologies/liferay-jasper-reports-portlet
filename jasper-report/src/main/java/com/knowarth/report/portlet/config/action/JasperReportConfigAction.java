package com.knowarth.report.portlet.config.action;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import com.knowarth.report.api.contants.JasperConstants;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;

@Component(
		configurationPid="com.knowarth.report.portlet.config.action.ReportConfiguration",
	    configurationPolicy = ConfigurationPolicy.OPTIONAL,
	    immediate = true,
	    property = {
	    		"javax.portlet.name=com_knowarth_report_portlet_JasperReportPortlet"
		    },
		    service = ConfigurationAction.class
		)
public class JasperReportConfigAction extends DefaultConfigurationAction {
	private volatile ReportConfiguration reportConfiguration;
	String definitionId=StringPool.BLANK;
	String roleId=StringPool.BLANK;
	String reportName=StringPool.BLANK;
	@Override
	public void include(PortletConfig portletConfig, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		List<Role> roleList=new ArrayList<>();
		roleList.addAll(RoleLocalServiceUtil.getRoles(themeDisplay.getCompanyId(), RoleConstants.TYPES_REGULAR_AND_SITE));

		roleList.remove(RoleLocalServiceUtil.getRole(themeDisplay.getCompanyId(), RoleConstants.ADMINISTRATOR));
		roleList.remove(RoleLocalServiceUtil.getRole(themeDisplay.getCompanyId(), RoleConstants.SITE_ADMINISTRATOR));
		roleList.remove(RoleLocalServiceUtil.getRole(themeDisplay.getCompanyId(), RoleConstants.SITE_OWNER));
		request.setAttribute(JasperConstants.ROLE_LIST, roleList);
		request.setAttribute(JasperConstants.SELECTED_ROLE, roleId);
		request.setAttribute(JasperConstants.SELECTED_DEFINITION, definitionId);
		request.setAttribute(JasperConstants.REPORT_NAME, reportName);
		super.include(portletConfig, request, response);
	}
	
	@Override
	public void processAction(PortletConfig portletConfig, ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {
		definitionId = ParamUtil.getString(actionRequest, JasperConstants.DEFINITIONS);
		roleId = ParamUtil.getString(actionRequest,JasperConstants.ROLES);
		reportName=ParamUtil.getString(actionRequest,JasperConstants.REPORT_NAME_CONFIG);
		setPreference(actionRequest,JasperConstants.DEFINITION_ID,definitionId);
		setPreference(actionRequest,JasperConstants.REPORT_NAME_CONFIG,reportName);
		super.processAction(portletConfig, actionRequest, actionResponse);
	}
	
	@Activate
	@Modified
	protected void activate(Map<Object, Object> properties) {
		reportConfiguration = ConfigurableUtil.createConfigurable(
			ReportConfiguration.class, properties);
	}
}	