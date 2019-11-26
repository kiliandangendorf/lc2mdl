package lc2mdl.lc.problem;

import lc2mdl.mdl.quiz.QuestionStack;
import lc2mdl.xml.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Gnuplot extends ProblemElement {

    private String plotString;
    private Element gnu;

    public Gnuplot(Problem problem, Node node){
        super(problem,node);
        gnu = (Element)node;
    }

    public Gnuplot(Problem problem, Node node, String LCgnuString){
        super(problem,node);
        try{
             Document dom= XMLParser.parseString2DOM(LCgnuString);
             NodeList gnuList = dom.getElementsByTagName("gnuplot");
             if (gnuList.getLength()>1) {
                log.warning("--found more than one gnuplot element, handle only the first one");
             }
             gnu = (Element)gnuList.item(0);
        }catch(Exception e){
				log.warning("--unable to read gnuplot-block.");
				e.printStackTrace();
			}
    }

    @Override
    public void consumeNode() {
        log.finer("gnuplot");

        String width= "400";
        String height = "300";
        if (gnu.hasAttribute("width")) {
            String s = gnu.getAttribute("width");
            if (!s.equals("")){
                if (s.charAt(0) == '$'){
                    s = s.substring(1);
                }
                width = s;
            }
            gnu.removeAttribute("width");
        }
        if (gnu.hasAttribute("height")) {
            String s = gnu.getAttribute("height");
            if (!s.equals("")){
                if (s.charAt(0) == '$'){
                    s = s.substring(1);
                }
                height = s;
            }
            gnu.removeAttribute("height");
        }
        String size = ",[size,"+width+","+height+"]";


        String color = "";
        if (gnu.hasAttribute("fgcolor")){
            String s = gnu.getAttribute("fgcolor");
            if (!s.equals("")){
                if (s.charAt(0) == '$'){
                    s = s.substring(1);
                }
                color = ",[color,"+s+"]";
            }
            gnu.removeAttribute("fgcolor");
        }

        String box = "";
        if (gnu.hasAttribute("border")){
            String s = gnu.getAttribute("border");
            if (s.equals("off")){
                box = ",[box,false]";
            }
            gnu.removeAttribute("border");
        }

        String align = "";
        if (gnu.hasAttribute("align")){
            String s = gnu.getAttribute("align");
            if (s.equals("left")||s.equals("right")){
                align = ",[plottags,false]";
            }
            gnu.removeAttribute("align");
        }

        // remove things that are not supported
        removeAttributeIfExist(gnu,"bgcolor");
        removeAttributeIfExist(gnu,"transparent");
        removeAttributeIfExist(gnu,"grid");
        removeAttributeIfExist(gnu,"gridlayer");
        removeAttributeIfExist(gnu,"box_border");
        removeAttributeIfExist(gnu,"font");
        removeAttributeIfExist(gnu,"fontface");
        removeAttributeIfExist(gnu,"samples");
        removeAttributeIfExist(gnu,"texwidth");
        removeAttributeIfExist(gnu,"texfont");
        removeAttributeIfExist(gnu,"plotcolor");
        removeAttributeIfExist(gnu,"pattern");
        removeAttributeIfExist(gnu,"solid");
        removeAttributeIfExist(gnu,"fillstyle");
        removeAttributeIfExist(gnu,"plottype");
        removeAttributeIfExist(gnu,"gridtype");
        removeAttributeIfExist(gnu,"lmargin");
        removeAttributeIfExist(gnu,"rmargin");
        removeAttributeIfExist(gnu,"tmargin");
        removeAttributeIfExist(gnu,"bmargin");
        removeAttributeIfExist(gnu,"boxwidth");
        removeAttributeIfExist(gnu,"major_ticscale");
        removeAttributeIfExist(gnu,"minor_ticscale");
        removeAttributeIfExist(gnu,"alttag");

        if (gnu.hasAttributes()){
            log.warning("There still undefined attributes in gnuplot");
        }

        String title = "";
        NodeList list = gnu.getElementsByTagName("title");
        if (list.getLength()>1) {
                log.warning("--found more than one title element, handle only the first one");
             }
        if (list.getLength()>=1) {
            Element e = (Element)list.item(0);
            title = "[legend,"+e.getNodeValue()+"],";
            removeNodeFromDOM(e);
        }

        String xminmax = "";
        String xmin="-10.0";
        String xmax= "10.0";
        String yminmax = "";
        String ymin="-10.0";
        String ymax= "10.0";
        list = gnu.getElementsByTagName("axis");
        if (list.getLength()>1) {
                log.warning("--found more than one axis element, handle only the first one");
             }
        if (list.getLength()>=1) {
            Element e = (Element)list.item(0);
            if (e.hasAttribute("xmin")) {
                xmin = e.getAttribute("xmin");
                e.removeAttribute("xmin");
            }
            if (e.hasAttribute("xmax")) {
                xmin = e.getAttribute("xmax");
                e.removeAttribute("xmax");
            }
            xminmax = ",[x,"+xmin+","+xmax+"],";
            if (e.hasAttribute("ymin")) {
                ymin = e.getAttribute("ymin");
                e.removeAttribute("ymin");
            }
            if (e.hasAttribute("ymax")) {
                ymin = e.getAttribute("ymax");
                e.removeAttribute("ymax");
            }
            yminmax = ",[y,"+ymin+","+ymax+"],";
            //removeNodeFromDOM(e);
        }

        String xlabel ="";
        list = gnu.getElementsByTagName("xlabel");
        if (list.getLength()>0){
            Element e = (Element)list.item(0);
            xlabel = ",[xlabel,\""+e.getTextContent()+"\"]";
            xlabel = xlabel.replaceAll("\\$","");
            removeNodeFromDOM(e);
        }
        String ylabel ="";
        list = gnu.getElementsByTagName("ylabel");
        if (list.getLength()>0){
            Element e = (Element)list.item(0);
            ylabel = ",[ylabel,\""+e.getTextContent()+"\"]";
            ylabel = ylabel.replaceAll("\\$","");
           removeNodeFromDOM(e);
        }

        String axes="";
        list=gnu.getElementsByTagName("axis");
        if (list.getLength()>0){
            Element e = (Element)list.item(0);
            // remove unused attributes
            removeAttributeIfExist(e,"color");
            if (e.hasAttribute("xformat")){
                if (e.getAttribute("xformat").equals("off")){
                    axes = ",[axes,false]";
                }
                e.removeAttribute("xformat");
            }
            if (e.hasAttribute("yformat")){
                if (e.getAttribute("yformat").equals("off")){
                    axes = ",[axes,false]";
                }
                e.removeAttribute("yformat");
            }

        }

        String fkt="[";
        list = gnu.getElementsByTagName("curve");
        for (int i=0; i<list.getLength();i++){
            Element e= (Element)list.item(i);
            NodeList fktlist = e.getElementsByTagName("function");

            for (int j=0; j<fktlist.getLength();j++){
                Element el= (Element)fktlist.item(j);
                String fktString = el.getTextContent();
                if (!fktString.equals("")){
                    if (fktString.charAt(0)=='$'){
                        fktString = fktString.substring(1);
                    }
                    if (j>0) {
                        fkt += ", ";
                    }
                    fkt += fktString;
                }
                removeNodeFromDOM(el);
            }
        }
        fkt += "]";

        plotString = "{@ plot("+fkt+axes;

        plotString += xlabel+ylabel+xminmax+yminmax+title+align+box+color+size+") @}";
    }

    @Override
    public void addToMdlQuestion(QuestionStack question) {
        question.addToQuestionText(plotString);
    }

    public String getPlotString(){ return plotString; }
}
