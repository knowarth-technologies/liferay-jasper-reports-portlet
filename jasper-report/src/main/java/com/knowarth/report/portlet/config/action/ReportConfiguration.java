package com.knowarth.report.portlet.config.action;
import aQute.bnd.annotation.metatype.Meta;
@Meta.OCD(
		id = "com.knowarth.report.portlet.config.action.ReportConfiguration"
	)
public interface ReportConfiguration {
	
	@Meta.AD(required = false)
	public String roles();

	@Meta.AD(required = false)
	public String definitions();
	
	@Meta.AD(required = false)
	public String reportName();
	
}
