							Jasper report - Plug-in for Liferay DXP platform
				----------------------------------------------------------------------------
Jasper report is most popular open source reporting engine. Keeping that in mind, KNOWARTH has created “Jasper Report” a flexible plug-in that can be customized to meet various organizational requirements.

Problems:
	-Liferay’s Report admin portlet is not user-friendly because in this portlet, Uploading defination (jrxml file) to download report we have to navigate different pages.
	-In Liferay’s Report admin portlet we have to add parameter like key and type manually.
	-In Liferay’s Report admin portlet all definitions are not visible to based on permissions.

Solution:
	-Jasper report plug-in is configurable in which you can choose definition based on role’s view permission.
	 Parameters will be fetched automatically from the jrxml file and it will generate input elements based on selected jrxml so we don’t have to add key and type of parameters manually.
	-Jasper report plug-in is user-friendly because from single page we can generate, download and Preview report.
	-Jasper report plug-in is accessible to any user so only admin needs to add definitions(jrxml) from Liferay Report Admin Portlet.

Steps for easy Configuration:
	Step 1:Copy the Jars file in deploy folder of Tomcat Application Server.
		1.com.liferay.portal.reports.engine.console.api-2.0.0.jar
		2.com.liferay.portal.reports.engine.console.service-1.0.1.jar
		3.com.liferay.portal.reports.engine.console.web-1.0.1.jar
		4.com.liferay.portal.reports.engine.jasper-1.0.8.jar
	Step 2: Disable Expose global from Control Panel > System Setting  >  Foundation  > JavaScript Loader . 
	Step 3:Copy the Jasper Report Jars file in deploy folder of 		Tomcat Application Server.
		1. jasper-report-api.jar
		2. jasper-report-impl.jar
		3. jasper-report-web.jar
	Step 4 : Now add definition from Configuration > Report Admin Portlet.
	Step 5: Navigate to “Configuration” in Jasper Report plug-in , where you can select definition based on role’s view 	permission.
	Step 6:After Selecting definition you can download and preview report.
	NOTE : You can Preview report in case of only data not for charts and graphs.
	
Support:
	Please feel free to contact us on contact@knowarth.com for any issue/suggestions.