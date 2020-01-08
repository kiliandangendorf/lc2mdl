package lc2mdl.mdl.quiz;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class QuestionEssay extends Question {
    private String graderinfo = "";
    private String responsetemplate = "";
    private String responserequired = "1";
    private String responsefieldlines = "15";
    // parameter for inline answer
    private String attachments = "0";
    private String attachmentsrequired = "0";
    private String responseformat="editor"; // or noinline
    private boolean file = false;


    @Override
    public Element exportToDom(Document dom) {
        Element e=super.exportToDom(dom);
        e.setAttribute("type", "essay");
        addElementAndContent(dom,e,"responseformat",responseformat);
        addElementAndContent(dom,e,"responserequired",responserequired);
        addElementAndContent(dom,e,"responsefieldlines",responsefieldlines);
        addElementAndContent(dom,e,"attachments",attachments);
        addElementAndContent(dom,e,"attachmentsrequired",attachmentsrequired);
        addElementAndTextContent(dom,e,"graderinfo",graderinfo);
        addElementAndTextContent(dom,e,"responsetemplate",responsetemplate);

        setCommentsInDom(dom,e);

        return e;
    }

    public void setParameterForFile(){
        attachments="-1";
        responseformat = "noinline";
        attachmentsrequired = "1";
        file = true;
    }

    public boolean isFile() { return file;   }
}
