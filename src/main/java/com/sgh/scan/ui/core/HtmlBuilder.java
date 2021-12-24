package com.sgh.scan.ui.core;

public class HtmlBuilder {
    private final String BLANK = "";

    String idProp = BLANK;
    String id = BLANK;
    String name = BLANK;
    String classs = BLANK;
    String style = BLANK;
    String value = BLANK;
    String displayValue = BLANK;
    String title = BLANK;
    String src = BLANK;
    String href = BLANK;
    String actions = BLANK;
    String colspan = BLANK;
    String rowspan = BLANK;
    String content = BLANK;
    String dataProperties = BLANK;
    String maxlength = BLANK;
    String rows = BLANK;
    String cols = BLANK;
    String autofocus = "autofocus";
    String checked = BLANK;
    String readonly = "readonly";
    String width = BLANK;
    String height = BLANK;
    String forId = BLANK;
    String icon = BLANK;
    Element el = Element.NONE;

    public static HtmlBuilder getInstance() { throw new UnsupportedOperationException("Current not supported"); }

    public static HtmlBuilder newInstance() { return new HtmlBuilder(); }
    
    private HtmlBuilder() {}

    void resetAllAttribute() {
        this.idProp = BLANK;
        this.id = BLANK;
        this.name = BLANK;
        this.classs = BLANK;
        this.style = BLANK;
        this.value = BLANK;
        this.displayValue = BLANK;
        this.title = BLANK;
        this.src = BLANK;
        this.href = BLANK;
        this.colspan = BLANK;
        this.rowspan = BLANK;
        this.actions = BLANK;
        this.maxlength = BLANK;
        this.content = BLANK;
        this.rows = BLANK;
        this.cols = BLANK;
        this.checked = BLANK;
        this.width = BLANK;
        this.height = BLANK;
        this.forId = BLANK;
        this.icon = BLANK;
        this.dataProperties = BLANK;
    }

    public HtmlBuilder id(String id) { this.id = id; this.idProp = id; return this; }
    public HtmlBuilder name(String name) { this.name = name; return this; }
    public HtmlBuilder classs(String classs) { this.classs = classs; return this; }
    public HtmlBuilder style(String style) { this.style = style; return this; }
    public HtmlBuilder value(String value) { this.value = value; return this; }
    public HtmlBuilder displayValue(String displayValue) { this.displayValue = displayValue; return this; }
    public HtmlBuilder title(String title) { this.title = title; return this; }
    public HtmlBuilder src(String src) { this.src = src; return this; }
    public HtmlBuilder href(String href) { this.href = href; return this; }
    public HtmlBuilder colspan(String colspan) { this.colspan = colspan; return this; }
    public HtmlBuilder rowspan(String rowspan) { this.rowspan = rowspan; return this; }
    public HtmlBuilder actions(String actions) { this.actions = actions; return this; }
    public HtmlBuilder maxlength(String maxlength) { this.maxlength = maxlength; return this; }
    public HtmlBuilder dataProperties(String dataProperties) { this.dataProperties = dataProperties; return this; }
    public HtmlBuilder content(String content) { this.content = content; return this; }
    public HtmlBuilder rows(String rows) { this.rows = rows; return this; }
    public HtmlBuilder cols(String cols) { this.cols = cols; return this; }
    public HtmlBuilder checked(String checked) { this.checked = checked; return this; }
    public HtmlBuilder autofocus() { return this; }
    public HtmlBuilder readonly() { return this; }
    public HtmlBuilder width(String width) { this.width = width; return this; }
    public HtmlBuilder height(String height) { this.height = height; return this; }
    public HtmlBuilder forId(String id) { this.forId = id; return this; }
    public HtmlBuilder icon(String icon) { this.icon = icon; return this; }
    public HtmlBuilder el(Element el) { this.el = el; resetAllAttribute(); return this; }

    public String build() {
        if (this.id != BLANK) { this.id = "id='" + this.id + "'"; }

        var output = "";
        switch(el) {
            case TEXT:
                output = HtmlTool.createTextTag(title, value, id + " class='" + classs + "' style='" + style + "' " + actions);
                break;
            case INPUT_TEXT_TITLE:
                output = HtmlTool.createInputTextWithTitleTag(title, id + " name='" + name + "' value='" + value + "' maxlength='" + maxlength + "' class='" + classs + "' style='" + style + "' " + actions);
                break;
            case INPUT_TEXT:
                output = HtmlTool.createInputTextTag(id + " name='" + name + "' value='" + value + "' maxlength='" + maxlength + "' class='" + classs + "' style='" + style + "' " + actions);
                break;
            case INPUT_PASSWORD:
                output = HtmlTool.createInputPasswordTag(id + " name='" + name + "' value='" + value + "' maxlength='" + maxlength + "' class='" + classs + "' style='" + style + "' " + actions);
                break;
            case SELECT:
                output = HtmlTool.createSelect(title, id + " name='" + name + "' class='" + classs + "' style='" + style + "' " + actions, value);
                break;
            case CHECK_BOX:
                output = HtmlTool.createCheckBoxTag(idProp, title, id + " name='" + name + "' value='" + value + "' class='" + classs + "' style='" + style + "' " + actions);
                break;
            case RADIO:
                output = HtmlTool.createRadioTag(id + " name='" + name + "' value='" + value + "' checked='" + checked + "' class='" + classs + "' style='" + style + "' " + actions);
                break;
            case BUTTON:
                output = HtmlTool.createButtonTag(value, id + " name='" + name + "' class='" + classs + "' style='" + style + "' " + actions);
                break;
            case BUTTON_ICON:
                output = HtmlTool.createButtonIconTag(value, icon, id + " name='" + name + "' class='" + classs + "' style='" + style + "' " + actions);
                break;
            case TEXT_AREA:
                output = HtmlTool.createTextAreaTag(title, value, id + " name='" + name + "' class='" + classs + "' style='" + style + "' rows='" + rows + "' cols='" + cols + "'" + actions);
                break;
            case HR:
                output = HtmlTool.createHrTag();
                break;
            case BR:
                output = HtmlTool.createBrTag();
                break;
            case TH:
                output = HtmlTool.createThTag(value, id + " class='" + classs + "' style='" + style + "' rowspan='" + rowspan + "' colspan='" + colspan + "'" + actions + " " + dataProperties);
                break;
            case TH_START:
                output = HtmlTool.createThTagStart(id + " class='" + classs + "' style='" + style + "' rowspan='" + rowspan + "' colspan='" + colspan + "'" + actions + " " + dataProperties);
                break;
            case TH_END:
                output = HtmlTool.createThTagEnd();
                break;
            case TR:
                output = HtmlTool.createTrTagStart(id + " class='" + classs + "' style='" + style + "' " + actions + " " + dataProperties);
                output += HtmlTool.createTrTagEnd();
                break;
            case TR_START:
                output = HtmlTool.createTrTagStart(id + " class='" + classs + "' style='" + style + "' " + actions + " " + dataProperties);
                break;
            case TR_END:
                output = HtmlTool.createTrTagEnd();
                break;
            case TD_START:
                output = HtmlTool.createTdTagStart(id + " class='" + classs + "' style='" + style + "' rowspan='" + rowspan + "' colspan='" + colspan + "' title='" + title + "'" + actions + " " + dataProperties);
                break;
            case TD_END:
                output = HtmlTool.createTdTagEnd();
                break;
            case TD:
                output = HtmlTool.createTdTag(value, id + " class='" + classs + "' style='" + style + "' rowspan='" + rowspan + "' colspan='" + colspan + "' title='" + title + "'" + actions + " " + dataProperties);
                break;
            case TABLE_START:
                output = HtmlTool.createTableTagStart(id + " class='" + classs + "' style='" + style + "' " + actions + " " + dataProperties);
                break;
            case TABLE_END:
                output = HtmlTool.createTableTagEnd();
                break;
            case IMG:
                output = HtmlTool.createImgTag(id + " class='" + classs + "' style='" + style + "' title='" + title + "' src='" + src + "' " + actions + " " + dataProperties);
                break;
            case LINK:
                output = HtmlTool.createLinkTag(value, id + " class='" + classs + "' style='" + style + "' title='" + title + "' href='" + href + "' " + actions);
                break;
            case DIV:
                output = HtmlTool.createDivTagStart(id + " class='" + classs + "' style='" + style + "' " + dataProperties);
                output += content;
                output += HtmlTool.createDivTagEnd();
                break;
            case DIV_START:
                output = HtmlTool.createDivTagStart(id + " class='" + classs + "' style='" + style + "' " + dataProperties);
                break;
            case DIV_END:
                output = HtmlTool.createDivTagEnd();
                break;
            case SPAN:
                output = HtmlTool.createSpanTag(value, id + " class='" + classs + "' style='" + style + "' " + dataProperties);
                break;
            case OL_START:
                output = HtmlTool.createOlTagStart(id + " class='" + classs + "' style='" + style + "' " + dataProperties);
                break;
            case OL_END:
                output = HtmlTool.createOlTagEnd();
                break;
            case LI:
                output = HtmlTool.createLiTag(value, id + " class='" + classs + "' style='" + style + "' " + actions + " " + dataProperties);
                break;
            case H2:
                output = HtmlTool.createH2Tag(value, id + " class='" + classs + "' style='" + style + "' " + actions + " " + dataProperties);
                break;
            case H3:
                output = HtmlTool.createH3Tag(value, id + " class='" + classs + "' style='" + style + "' " + actions + " " + dataProperties);
                break;
            case OPTION:
                output = HtmlTool.createSelectOption(value, displayValue);
                break;
            case P:
                output = HtmlTool.createPTag(value, id + " class='" + classs + "' style='" + style + "' " + dataProperties);
                break;
            case CANVAS:
                output = HtmlTool.createCanvasTag(id + " class='" + classs + "' style='" + style + "' width='" + width + "' height='" + height + "' " + dataProperties);
                break;
            case LEGEND:
                output = HtmlTool.createLegendTag(value);
                break;
            case LABEL:
                output = HtmlTool.createLabelTag(forId, value);
                break;
            case LABEL_START:
                output = HtmlTool.createLabelStartTag();
                break;
            case LABEL_END:
                output = HtmlTool.createLabelEndTag();
                break;
            case FORM_START:
                output = HtmlTool.createFormStartTag(id + "name='" + name + "' class='" + classs + "' style='" + style + "' " + actions);
                break;
            case FORM_END:
                output = HtmlTool.createFormEndTag();
                break;
            case FIELD_SET_START:
                output = HtmlTool.createFieldSetStartTag("class = '" + classs + "'");
                break;
            case FIELD_SET_END:
                output = HtmlTool.createFieldSetEndTag();
                break;
            case P_START:
                output = HtmlTool.createPStartTag(id + " class='" + classs + "' style='" + style + "' " + dataProperties);
                break;
            case P_END:
                output = HtmlTool.createPEndTag();
                break;
            case JSSCRIPT:
                output = HtmlTool.createJavaScriptTag(value);
                break;
            default:
                ;
        }

        output = output.replace("name=''", BLANK);
        output = output.replace("style=''", BLANK);
        output = output.replace("class=''", BLANK);
        output = output.replace("value=''", BLANK);
        output = output.replace("maxlength=''", BLANK);
        output = output.replace("colspan=''", BLANK);
        output = output.replace("rowspan=''", BLANK);
        output = output.replace("href=''", BLANK);
        output = output.replace("src=''", BLANK);
        output = output.replace("title=''", BLANK);
        output = output.replace("rows=''", BLANK);
        output = output.replace("cols=''", BLANK);
        output = output.replace("width=''", BLANK);
        output = output.replace("height=''", BLANK);
        output = output.replace("checked=''", BLANK);

        return output;
    }

    public enum Element {
        TEXT,
        INPUT_TEXT_TITLE,
        INPUT_TEXT,
        INPUT_PASSWORD,
        SELECT,
        CHECK_BOX,
        RADIO,
        BUTTON,
        TEXT_AREA,
        HR,
        BR,
        TR,
        TR_START,
        TR_END,
        TH,
        TH_START,
        TH_END,
        TD,
        TD_START,
        TD_END,
        TABLE_START,
        TABLE_END,
        IMG,
        LINK,
        DIV,
        DIV_START,
        DIV_END,
        SPAN,
        OL_START,
        OL_END,
        LI,
        H2,
        H3,
        OPTION,
        P,
        CANVAS,
        LABEL_START,
        LABEL_END,
        FORM_START,
        FORM_END,
        FIELD_SET_START,
        FIELD_SET_END,
        P_START,
        P_END,
        JSSCRIPT,
        LABEL,
        LEGEND,
        BUTTON_ICON,
        NONE
    }
}
