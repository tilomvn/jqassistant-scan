package com.sgh.scan.ui.core.pages;

import com.sgh.scan.ui.core.HtmlBuilder;
import com.sgh.scan.ui.core.HtmlTool;
import com.sgh.scan.ui.core.HtmlBuilder.Element;

public abstract class MasterPage {

    String htmlHeader(String pageTile) {
        var content = new StringBuilder();
        content.append(HtmlTool.createViewPortDevice() + HtmlTool.createMetaCharsetUtf8() +
                HtmlTool.createTitleTag(pageTile) + /*HtmlTool.createWebIconTag() +*/
                HtmlTool.createJQueryScriptTag());
        content.append(HtmlTool.createJsFile("/js/Chartjs-2.9.4.js"));
        content.append(HtmlTool.createJsFile("/js/jquery-ui.js"));
        content.append(HtmlTool.createJsFile("/js/clipboard.js"));
        content.append(HtmlTool.createCssFile("/css/jquery-ui.css"));
        content.append(HtmlTool.createCssFile("/css/datepicker.css"));
        content.append(HtmlTool.createJsFile("/js/common.js"));
        content.append(HtmlTool.createCssFile("/css/common.css"));
        content.append(HtmlTool.createJsFile("/js/navigationbar.js"));
        content.append(HtmlTool.createCssFile("/css/navigationbar.css"));
        content.append(HtmlTool.createCssFile("/css/body.css"));
        content.append(HtmlTool.createCssFile("/css/footer.css"));
        return content.toString();
    }

    String header() {
        var content = new StringBuilder();
        return content.toString();
    }

    String navigationBar() {
        var content = new StringBuilder();
        var builder = HtmlBuilder.newInstance();
        var builderElement = HtmlBuilder.newInstance();
        content.append(builder.el(Element.DIV_START).id("divHeader").build());
        content.append(builder.el(Element.DIV).classs("buttonBar").content(
                builderElement.el(Element.BUTTON_ICON).id("new-repository").classs("ui-button ui-widget ui-corner-all")
                        .icon("ui-icon ui-icon-plus")
                        .value("New Repository").build()
        ).build());
        content.append(builder.el(Element.HR).build());
        content.append(builder.el(Element.DIV_END).build());
        return content.toString();
    }

    protected abstract String body();

    String footer() {
        var content = new StringBuilder();
        var builder = HtmlBuilder.newInstance();
        content.append(builder.el(Element.DIV_START).id("divFooter").build());
        content.append(builder.el(Element.HR).build());
        content.append(builder.el(Element.P).value("Sgh © 2021").build());
        content.append(builder.el(Element.DIV_END).build());
        return content.toString();
    }

    public String getPage(String pageTitle) {
        var page = new StringBuilder();
        page.append("<!doctype html>");
        page.append("<html lang=\"en\">");
        page.append("<head>");
        page.append(htmlHeader(pageTitle));
        page.append("</head>");
        page.append("<body>");
        page.append(header());
        page.append(navigationBar());
        page.append(body());
        page.append(footer());
        page.append("</body>");
        page.append("</html>");
        return page.toString();
    }
}
