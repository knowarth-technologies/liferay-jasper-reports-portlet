package com.knowarth.report.service;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;

import com.knowarth.report.api.JasperReportApi;
import com.knowarth.report.api.bean.ParameterDetail;
import com.knowarth.report.api.contants.JasperConstants;
import com.liferay.document.library.kernel.store.DLStoreUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.xml.Attribute;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.reports.engine.console.model.Definition;
import com.liferay.portal.reports.engine.console.model.Entry;
import com.liferay.portal.reports.engine.console.service.DefinitionLocalServiceUtil;
import com.liferay.portal.reports.engine.console.service.EntryLocalServiceUtil;


/**
 * @author vishal.munjani
 */
@Component(
	immediate = true,
	property = {
	},
	service = JasperReportApi.class
)
public class JasperReportService implements JasperReportApi {
	private static final Log _log = LogFactoryUtil.getLog(JasperReportService.class);
	private List<ParameterDetail> copyParamList = new ArrayList<>();
	
	@Override
	public Map<String, List<ParameterDetail>> getDefinitionParameters(RenderRequest renderRequest, long definitionId) {
		Map<String, List<ParameterDetail>> mapParameter = new HashMap<>();
		List<ParameterDetail> paramAttributeList = new ArrayList<>();
		copyParamList.clear();
		try {
			Definition definition = DefinitionLocalServiceUtil.getDefinition(definitionId);
			String directoryName = definition.getAttachmentsDir();
			String fileName = definition.getReportName();
			fileName = fileName + StringPool.PERIOD + JasperConstants.JRXML;
			String fileLocation = directoryName.concat(StringPool.SLASH).concat(fileName);
			File file = DLStoreUtil.getFile(definition.getCompanyId(), CompanyConstants.SYSTEM, fileLocation);

			Document document = SAXReaderUtil.read(file);
			Element rootElement = document.getRootElement();
			List<Element> elementList = rootElement.elements();
			for (Element e : elementList) {
				if (e.getName().equals(JasperConstants.PARAMETER)) {
					List<Attribute> attrList = e.attributes();
					ParameterDetail p = new ParameterDetail();
					for (Attribute attribute : attrList) {
						if (attribute.getName().equals(JasperConstants.NAME))
							p.setName(attribute.getData().toString());
						else
							p.setType(attribute.getData().toString());
					}
					paramAttributeList.add(p);
					
					mapParameter.put(JasperConstants.MAP_PARAMETER_KEY, paramAttributeList);

					
					/*
					 * List<Element> paramList=e.elements(); for(Element
					 * e1:paramList){ _log.info("Paramter Element :"
					 * +e1.getName());
					 * if(e1.getName().equals("parameterDescription"))
					 * _log.info("Paramter Description Value :"
					 * +e1.getData().toString());
					 * if(e1.getName().equals("defaultValueExpression"))
					 * _log.info("Paramter Default Value Expression :"
					 * +e1.getData().toString()); }
					 */
					 
				}
			}
			copyParamList.addAll(paramAttributeList);
		} catch (PortalException e) {
			_log.error("Portal Error :" + e.getMessage());
		} catch (DocumentException e1) {
			_log.error("Document Error :" + e1.getMessage());
		}
		return mapParameter;
	}

	@Override
	public void getDefinitionByRole(ResourceResponse resourceResponse, long roleId) {
		try {
			JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
			List<Definition> definitionList = DefinitionLocalServiceUtil.getDefinitions(0,
					DefinitionLocalServiceUtil.getDefinitionsCount());
			List<Definition> roleDefinitionList = new ArrayList<>();
			for (Definition definition : definitionList) {
				boolean hasUserPermissions = ResourcePermissionLocalServiceUtil.hasResourcePermission(
						definition.getCompanyId(), Definition.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL,
						Long.toString(definition.getDefinitionId()), roleId, ActionKeys.VIEW);
				if (hasUserPermissions) {
					roleDefinitionList.add(definition);
				}
			}
			for (int i = 0; i < roleDefinitionList.size(); i++) {
				JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
				jsonObject.put(JasperConstants.ID, definitionList.get(i).getDefinitionId());
				jsonObject.put(JasperConstants.NAME, definitionList.get(i).getName(Locale.getDefault()));
				jsonArray.put(jsonObject);
			}
			PrintWriter writer = resourceResponse.getWriter();
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put(JasperConstants.JSON_DEFINITION, jsonArray);
			writer.print(jsonObject.toString());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			_log.error(e.getMessage());
		}
	}

	@Override
	public Entry generateReport(PortletRequest portletRequest, String format, long definitionId, String reportName,
			boolean isPreview) {
		Entry entry = null;
		List<ParameterDetail> lparam = new ArrayList<>();
		String portletId = PortalUtil.getPortletId(portletRequest);
		ThemeDisplay themeDisplay = (ThemeDisplay) portletRequest.getAttribute(WebKeys.THEME_DISPLAY);
		String url = StringPool.BLANK;
		for (int i = 0; i < copyParamList.size(); i++) {
			ParameterDetail p = new ParameterDetail();
			p.setName(ParamUtil.getString(portletRequest, JasperConstants.KEY_NAME + copyParamList.get(i).getName()));
			String paramType = ParamUtil.getString(portletRequest,
					copyParamList.get(i).getName() + copyParamList.get(i).getType());
			if ((paramType.equals(JasperConstants.SQL_DATE) || paramType.equals(JasperConstants.SQL_TIMESTAMP)
					|| paramType.equals(JasperConstants.UTIL_DATE))) {
				DateFormat dateFormat = new SimpleDateFormat(JasperConstants.REPORT_DATE_FORMAT);
				Date date = ParamUtil.getDate(portletRequest, copyParamList.get(i).getName(), dateFormat);
				p.setValue(dateFormat.format(date));
			} else
				p.setValue(ParamUtil.getString(portletRequest, copyParamList.get(i).getName()));
			p.setType(paramType);
			lparam.add(p);
		}
		JSONArray reportParametersJSONArray = JSONFactoryUtil.createJSONArray();
		for (int i = 0; i < lparam.size(); i++) {
			JSONObject entryReportParameterJSONObject = JSONFactoryUtil.createJSONObject();
			entryReportParameterJSONObject.put(JasperConstants.KEY, lparam.get(i).getName());
			entryReportParameterJSONObject.put(JasperConstants.VALUE, lparam.get(i).getValue());

			reportParametersJSONArray.put(entryReportParameterJSONObject);
		}
		String emailDelivery=ParamUtil.getString(portletRequest, "recipient");

		try {
			ServiceContext serviceContext = ServiceContextFactory.getInstance(Entry.class.getName(), portletRequest);
			entry = EntryLocalServiceUtil.addEntry(themeDisplay.getUserId(), themeDisplay.getScopeGroupId(),
					definitionId, format, false, null, null, false, null, null, emailDelivery, portletId, url, reportName,
					reportParametersJSONArray.toString(), serviceContext);
			portletRequest.setAttribute(JasperConstants.SUCCESS,JasperConstants.TRUE);
			if(isPreview){
				try {
					Thread.sleep(1000);
					previewReport(portletRequest,reportName,entry,format);
				} catch (InterruptedException e) {
					_log.info("Error :"+e.getMessage());
					
				}
			}
		} catch (PortalException e) {
			portletRequest.setAttribute(JasperConstants.SUCCESS,JasperConstants.FALSE);
			if(isPreview)
				portletRequest.setAttribute("ispreviewable",JasperConstants.FALSE);
			_log.error("Errror :" + e.getMessage());
		}
		return entry;
	}

	public void previewReport(PortletRequest portletRequest, String reportName, Entry entry, String format) {
		ThemeDisplay themeDisplay = (ThemeDisplay) portletRequest.getAttribute(WebKeys.THEME_DISPLAY);
		try {
			reportName = reportName + StringPool.PERIOD + format;
			String fileLocation = entry.getAttachmentsDir().concat(StringPool.SLASH).concat(reportName);
			File file = DLStoreUtil.getFile(themeDisplay.getCompanyId(),
					CompanyConstants.SYSTEM, fileLocation);
			Document document=SAXReaderUtil.read(file);
			Element rootElement=document.getRootElement();
			
			List<Element> originElements = rootElement.elements("origin");
			
			int columnHeaderInd = 0;
			int detailInd = 0;
			for (int i=0; i<originElements.size();i++) {
				Attribute bandAttr = originElements.get(i).attribute("band");
				String bandAttrData = bandAttr.getData().toString();
				if(bandAttrData.equals("columnHeader")){
					columnHeaderInd = i;
				}else if(bandAttrData.equals("detail")){
					detailInd = i;
				}
			}
			
			List<Element> pageElements = rootElement.elements("page");
			List<Element> textElements=pageElements.get(0).elements("text");
			List<String> columnList = new ArrayList<>();
			for (Element txtElement : textElements) {
				List<Element> subTxtElementsList = txtElement.elements();
				boolean columnEl = false;
				for (Element subTxtElement : subTxtElementsList) {
					Attribute attributeOrigin = subTxtElement.attribute("origin");
					if (Validator.isNotNull(attributeOrigin)
							&& attributeOrigin.getData().toString().equals(String.valueOf(columnHeaderInd))) {
						columnEl = true;
					} else if (subTxtElement.getName().equals("textContent") && columnEl) {
						String columnContent = subTxtElement.getData().toString();
						columnList.add(columnContent);
					}
				}
			}
			
			int startDetailSrcId=0;
			List<Element> subPageEles=pageElements.get(0).elements();
			for(Element subPgEle:subPageEles) {
				List<Element> li=subPgEle.elements();
				boolean detailEl = false;
				for(Element subEl:li){
					Attribute org = subEl.attribute("origin");
					Attribute src = subEl.attribute("srcId");
					if(Validator.isNotNull(org) && org.getData().toString().equals(String.valueOf(detailInd))){
						detailEl=true;
						startDetailSrcId=Integer.parseInt(src.getData().toString());
						break;
					}else{
						detailEl=false;
					}
				}
				if(detailEl){
					break;
				}
			}
			
			Map<Integer, List<String>> detailData = new HashMap<>();
			for (int j = 0; j < columnList.size(); j++) {
				List<String> datalist = new ArrayList<>();
				for (int i = 0; i < pageElements.size(); i++) {
					List<Element> textElementsList = pageElements.get(i).elements("text");
					for (Element textElement : textElementsList) {
						List<Element> subTextElements = textElement.elements();
						boolean detailEl = false;
						for (Element subTextElement : subTextElements) {
							Attribute attrOrigin = subTextElement.attribute("origin");
							Attribute attrSrcId = subTextElement.attribute("srcId");
							if (Validator.isNotNull(attrOrigin)
									&& attrOrigin.getData().toString().equals(String.valueOf(detailInd))
									&& Validator.isNotNull(attrOrigin)
									&& attrSrcId.getData().toString().equals(String.valueOf(startDetailSrcId))) {
								detailEl = true;
							} else if (subTextElement.getName().equals("textContent") && detailEl) {
								String dataContent = subTextElement.getData().toString();
								datalist.add(dataContent);
							}
						}
					}
				}
				detailData.put(j, datalist);
				startDetailSrcId++;
			}
			
			
			Set<Integer> keySet=detailData.keySet();
			
			Map<Integer, List<String>> reportData = new HashMap<>();
			for (int i = 0; i < detailData.get(0).size(); i++) {
				List<String> li=new ArrayList<>();
				Iterator<Integer> iterator=keySet.iterator();
				while (iterator.hasNext()) {
					List<String> data = detailData.get(iterator.next());
					li.add(data.get(i));
				}
				reportData.put(i, li);
			}
			portletRequest.setAttribute("ColumnList", columnList);
			portletRequest.setAttribute("mapdata", reportData);
			
		} catch (Exception e) {
			portletRequest.setAttribute("ispreviewable",JasperConstants.FALSE);
			_log.error("Error :" + e.getMessage());
		}
	}
}