package com.sgh.scan.ui.pages;

import com.sgh.scan.ui.core.HtmlBuilder;
import com.sgh.scan.ui.core.HtmlBuilder.Element;
import com.sgh.scan.ui.core.HtmlTool;
import com.sgh.scan.ui.core.pages.MasterPage;

public class RepositoryRegistrationPage extends MasterPage {

    @Override
    protected String body() {
        var content = new StringBuilder();
        var htmlBuilder = HtmlBuilder.newInstance();

        content.append(HtmlTool.createCssFile("/css/repository.css"));
        content.append(HtmlTool.createJsFile("/js/repository.js"));

        content.append("<div id='divBody'>");
        content.append(htmlBuilder.el(Element.DIV_START).id("registrationForm").title("New Repository").build());
        content.append(htmlBuilder.el(Element.P).classs("validateTips").value("All form fields are required.").build());
        content.append(htmlBuilder.el(Element.FORM_START).build());

        content.append(htmlBuilder.el(Element.FIELD_SET_START).classs("formGroup").build());
        content.append(htmlBuilder.el(Element.LABEL).forId("branch").value("Branch").build());
        content.append(htmlBuilder.el(Element.INPUT_TEXT).id("branch").classs("text ui-widget-content ui-corner-all").build());
        content.append(htmlBuilder.el(Element.LABEL).forId("repoUrl").value("Repository Url").build());
        content.append(htmlBuilder.el(Element.INPUT_TEXT).id("repoUrl").classs("text ui-widget-content ui-corner-all").build());
        content.append(htmlBuilder.el(Element.FIELD_SET_END).build());

        content.append(htmlBuilder.el(Element.FIELD_SET_START).classs("transportType").build());
        content.append(htmlBuilder.el(Element.LEGEND).value("Authentication Type").build());
        content.append(htmlBuilder.el(Element.LABEL).forId("sshType").value("SSH").build());
        content.append(htmlBuilder.el(Element.RADIO).id("sshType").name("authType").value("SSH").checked("true").build());
        content.append(htmlBuilder.el(Element.LABEL).forId("httpType").value("HTTP").build());
        content.append(htmlBuilder.el(Element.RADIO).id("httpType").name("authType").value("HTTP").build());
        content.append(htmlBuilder.el(Element.FIELD_SET_END).build());

        content.append(htmlBuilder.el(Element.FIELD_SET_START).classs("sshPassPhase").build());
        content.append(htmlBuilder.el(Element.LABEL).forId("passPhase").value("PassPhase").build());
        content.append(htmlBuilder.el(Element.INPUT_PASSWORD).id("passPhase").classs("text ui-widget-content ui-corner-all").build());
        content.append(htmlBuilder.el(Element.FIELD_SET_END).build());

        content.append(htmlBuilder.el(Element.FIELD_SET_START).classs("httpAuthentication").build());
        content.append(htmlBuilder.el(Element.LABEL).forId("userName").value("Username").build());
        content.append(htmlBuilder.el(Element.INPUT_TEXT).id("userName").classs("text ui-widget-content ui-corner-all").build());
        content.append(htmlBuilder.el(Element.LABEL).forId("password").value("Password").build());
        content.append(htmlBuilder.el(Element.INPUT_PASSWORD).id("password").classs("text ui-widget-content ui-corner-all").build());
        content.append(htmlBuilder.el(Element.FIELD_SET_END).build());

        content.append(htmlBuilder.el(Element.FORM_END).build());
        content.append(htmlBuilder.el(Element.DIV_END).build());


        content.append(htmlBuilder.el(Element.DIV_START).id("registrationResult").title("Registration Result").build());
        content.append(htmlBuilder.el(Element.P).id("message").build());
        content.append(htmlBuilder.el(Element.DIV_END).build());

        content.append(htmlBuilder.el(Element.DIV_START).id("backgroundWaiting").build());
        content.append(htmlBuilder.el(Element.IMG).src("/css/images/loading-icon.jpg").build());
        content.append(htmlBuilder.el(Element.DIV_END).build());
        content.append("</div>");

        return content.toString();
    }
}
