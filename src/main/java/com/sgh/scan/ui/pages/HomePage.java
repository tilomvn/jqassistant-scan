package com.sgh.scan.ui.pages;

import com.sgh.scan.ui.core.HtmlTool;
import com.sgh.scan.ui.core.pages.MasterPage;

public class HomePage extends MasterPage {

    @Override
    protected String body() {
        var content = new StringBuilder();
        content.append(HtmlTool.createJsFile("/js/home.js"));
        content.append("<div id='divBody' style='font-size: 20pt; font-weight: bold;'><div>Welcome to project scan and analyze application</div></div>");
        return content.toString();
    }
}
