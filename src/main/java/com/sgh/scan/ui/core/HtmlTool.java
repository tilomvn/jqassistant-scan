package com.sgh.scan.ui.core;

public class HtmlTool {
    public static String createTableTagStart(String properties) { return "<table cellpadding='2' cellspacing='0' " + properties + ">"; }
    public static String createTableTagEnd() { return "</table>"; }
    public static String createThTagStart(String properties) { return "<th " + properties + ">"; }
    public static String createThTagEnd() { return "</th>"; }
    public static String createThTag(String content, String properties) { return "<th " + properties + ">" + content + "</th>"; }
    public static String createTrTagStart(String properties) { return "<tr " + properties + ">"; }
    public static String createTrTagEnd() { return "</tr>"; }
    public static String createTdTagStart(String properties) { return "<td " + properties + ">"; }
    public static String createTdTagEnd() { return "</td>"; }
    public static String createTdTag(String content, String properties) { return "<td " + properties + ">" + content + "</td>"; }
    public static String createLinkTag(String displayValue, String properties) { return "<a href='#' " + properties + ">" + displayValue + "</a>"; }
    public static String createDivTagStart(String properties) { return "<div " + properties + ">"; }
    public static String createDivTagEnd() { return "</div>"; }
    public static String createH2Tag(String content, String properties) { return "<h2 " + properties + ">" + content + "</h2>"; }
    public static String createH3Tag(String content, String properties) { return "<h3 " + properties + ">" + content + "</h3>"; }
    public static String createBrTag() { return "<br/>"; }
    public static String createHrTag() { return "<hr/>"; }
    public static String createJQueryScriptTag() { return "<script type=\"application/javascript\" src='/js/jquery-3.5.1.min.js'></script>"; }
    public static String createJsFile(String url) { return "<script type=\"application/javascript\" src='" + url + "'></script>"; }
    public static String createCssFile(String url) { return "<link rel='stylesheet' href='" + url + "' />"; }
    public static String createCheckBoxTag(String lbId, String label, String properties) { return "<div style='padding: 2px;'><input type='checkbox' " + properties + "><label for='" + lbId + "'>" + label + "</label></div>"; }
    public static String createButtonTag(String label, String properties) { return "<input type='button' value='" + label + "' " + properties + "/>"; }
    public static String createTitleTag(String title) { return "<title>" + title + "</title>"; };
    public static String createJavaScriptTag(String jsContent) { return "<script type='text/javascript'>" + jsContent + "</script>"; }
    public static String createInputTextWithTitleTag(String title, String properties) { return "<p><span>" + title + " </span><input type='text' " + properties + "></p>"; }
    public static String createInputTextTag(String properties) { return "<input type='text' " + properties + "/>"; }
    public static String createInputPasswordTag(String properties) { return "<input type='password' " + properties + "/>"; }
    public static String createTextTag(String title, String value, String properties) { return "<p " + properties + "><span>" + title + " </span><span>" + value + "</span></p>"; }
    public static String createSpanTag(String content, String properties) { return "<span " + properties + ">" + content + "</span>"; }
    public static String createSelectOption(String content, String value) { return "<option value='" + value + "'>" + content + "</option>"; }
    public static String createSelect(String title, String properties, String options) { return "<p> "+ title + "<select " + properties + ">" + options + "</select></p>"; }
    public static String createImgTag(String properties) { return "<img " + properties + " />"; }
    public static String createOlTagStart(String properties) { return "<ol " + properties + ">"; }
    public static String createOlTagEnd() { return "<ol/>"; }
    public static String createLiTag(String content, String properties) { return "<li " + properties + ">" + content + "</li>"; }
    public static String createPTag(String content, String properties) { return "<p " + properties + ">" + content + "</p>"; }
    public static String createMetaCharsetUtf8() { return "<meta charset=\"utf-8\">"; }
    public static String createViewPortDevice() { return "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"; }
    public static String createWebIconTag() { return "<link rel=\"icon\" href=\"/favicon.ico\">"; }
    public static String createTextAreaTag(String title, String content, String properties) { return "<p><span>" + title + " </span><textarea " + properties + ">" + content + "</textarea></p>"; }
    public static String createSpaces(int num) {
        String HTML_BLANK = "&nbsp;";
        StringBuilder htmlSpace = new StringBuilder(HTML_BLANK);
        for (int i = 1; i < num; i++) {
            htmlSpace.append(HTML_BLANK);
        }
        return htmlSpace.toString();
    }
    public static String createCanvasTag(String properties) { return "<canvas " + properties + "></canvas>"; }
    public static String createLabelStartTag() { return "<label>"; }
    public static String createLabelEndTag() { return "</label>"; }
    public static String createFormStartTag(String properties) { return "<form " + properties + ">"; }
    public static String createFormEndTag() { return "</form>"; }
    public static String createPStartTag(String properties) { return "<p " + properties + ">"; }
    public static String createPEndTag() { return "</p>"; }
    public static String createRadioTag(String properties) { return "<input type='radio' " + properties + "/>"; }
    public static String createFieldSetStartTag(String properties) { return "<fieldset " + properties + ">"; }
    public static String createFieldSetEndTag() { return "</fieldset>"; }
    public static String createLabelTag(String forId, String value) { return "<label for='" + forId + "'>" + value + "</label>"; }
    public static String createLegendTag(String value) { return "<legend>" + value + "</legend>"; }
    public static String createButtonIconTag(String value, String icon, String properties) {
        return "<button " + properties + ">" +
                "<span class='" + icon + "'></span>" + value +
                "</button>";
    }
}
