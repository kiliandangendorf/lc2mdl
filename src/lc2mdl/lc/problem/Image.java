package lc2mdl.lc.problem;

import lc2mdl.mdl.quiz.Question;
import lc2mdl.mdl.quiz.QuestionStack;
import lc2mdl.util.FileFinder;
import lc2mdl.xml.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Image extends ProblemElement {

    private Element img;
    private String imgString="";
    private String height="";
    private String width="";

    public Image(Problem problem, Node node) {
        super(problem, node);
        img = (Element)node;
    }

   public Image(Problem problem, Node node, String LCimgString) {
        super(problem, node);
        try{
             Document dom= XMLParser.parseString2DOM(LCimgString);
             NodeList imgList = dom.getElementsByTagName("img");
             if (imgList.getLength()>1) {
                log.warning("--found more than one img element, handle only the first one");
             }
             img = (Element)imgList.item(0);
        }catch(Exception e){
				log.warning("--unable to read img-block.");
				e.printStackTrace();
			}
        img = (Element)node;
    }

    @Override
    public void consumeNode() {
        log.finer("image to show");

         // attributes
        if (img.hasAttribute("src")) {
            String src = img.getAttribute("src");
            if (src.charAt(0) == '$') {
                imgString = "{@ " + src.substring(1) + " @}";
            } else {
                String key = FileFinder.extractFileName(src);
                String svgString = problem.getImagesSVG().get(key);
                if (svgString==null){
                    log.warning("--did not find svg for image "+key);
                    svgString="";
                }
                imgString = svgString;
            }
            img.removeAttribute("src");
        }else{
            log.warning("no src in image");
        }

        if (img.hasAttribute("width")){
            width=img.getAttribute("width");
            img.removeAttribute("width");
            imgString = imgString.replaceFirst("width=\"[0-9]*px\"","width=\""+width+"px\"");
        }
        if (img.hasAttribute("height")){
            height=img.getAttribute("height");
            img.removeAttribute("height");
            imgString = imgString.replaceFirst("height=\"[0-9]*px\"","height=\""+height+"px\"");
        }
        removeAttributeIfExist(img,"TeXwrap");
        removeAttributeIfExist(img,"TeXwidth");
        removeAttributeIfExist(img,"TeXheight");
        removeAttributeIfExist(img,"alt");
        removeAttributeIfExist(img,"align");
        removeAttributeIfExist(img,"encrypturl");

        if (img.hasAttributes()){
            log.warning("-still unknown attributes in image.");
        }

    }

    @Override
    public void addToMdlQuestionStack(QuestionStack question) {
        addToMdlQuestion(question);
    }

    @Override
    public void addToMdlQuestion(Question question) {
        question.addToQuestionText(imgString);
    }
    public String getImgString() { return imgString; }
}
