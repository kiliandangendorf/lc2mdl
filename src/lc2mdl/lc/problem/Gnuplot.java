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
                if (s.equals("x000000")){
                    s = "black";
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

        String alt = "";
        if (gnu.hasAttribute("alttag")){
            String a = gnu.getAttribute("alttag");
            if (!a.equals("")){
                    alt = ",[alt,"+a+"]";
            }
            gnu.removeAttribute("alttag");
        }

       String grid = ",grid2d";
        if (gnu.hasAttribute("grid")){
            String a = gnu.getAttribute("grid");
            if (a.equals("off")){
                    grid = "";
            }
            gnu.removeAttribute("grid");
        }

        // remove things that are not supported
        removeAttributeIfExist(gnu,"bgcolor");
        removeAttributeIfExist(gnu,"transparent");
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

        if (gnu.hasAttributes()){
            log.warning("There still undefined attributes in gnuplot");
        }

        String start="-10.0";
        String inc = "1";
        String end = "-10";
        String tics ="";
        NodeList list = gnu.getElementsByTagName("xtics");
        if (list.getLength()>=1) {
            Element e = (Element)list.item(0);
            if (e.hasAttribute("start")){
                String s= e.getAttribute("start");
                if (!e.equals("")){
                     start = s;
                }
            }
            if (e.hasAttribute("end")){
                String s= e.getAttribute("end");
                if (!e.equals("")){
                     end = s;
                }
            }
            if (e.hasAttribute("increment")){
                String s= e.getAttribute("increment");
                if (!e.equals("")){
                     inc = s;
                }
            }
           tics += ",[xtics,"+start+","+inc+","+end+"]";
           removeNodeFromDOM(e);
        }
        start="-10.0";
        inc = "1";
        end = "-10";
        list = gnu.getElementsByTagName("ytics");
        if (list.getLength()>=1) {
            Element e = (Element)list.item(0);
            if (e.hasAttribute("start")){
                String s= e.getAttribute("start");
                if (!e.equals("")){
                     start = s;
                }
            }
            if (e.hasAttribute("end")){
                String s= e.getAttribute("end");
                if (!e.equals("")){
                     end = s;
                }
            }
            if (e.hasAttribute("increment")){
                String s= e.getAttribute("increment");
                if (!e.equals("")){
                     inc = s;
                }
            }
           tics += ",[ytics,"+start+","+inc+","+end+"]";
           removeNodeFromDOM(e);
        }

        String title = "";
        list = gnu.getElementsByTagName("title");
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
                xmax = e.getAttribute("xmax");
                e.removeAttribute("xmax");
            }
            xminmax = ",[x,"+xmin+","+xmax+"]";
            if (e.hasAttribute("ymin")) {
                ymin = e.getAttribute("ymin");
                e.removeAttribute("ymin");
            }
            if (e.hasAttribute("ymax")) {
                ymax = e.getAttribute("ymax");
                e.removeAttribute("ymax");
            }
            yminmax = ",[y,"+ymin+","+ymax+"]";
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
                    axes = ",[axes,y]";
                }
                e.removeAttribute("xformat");
            }
            if (e.hasAttribute("yformat")){
                if (e.getAttribute("yformat").equals("off")){
                    if (!axes.equals("")) {
                        axes = ",[axes,false]";
                    }else{
                        axes = ",[axes,x]";
                    }
                }
                e.removeAttribute("yformat");
            }
            if (e.hasAttribute("xzero")){
                if (e.getAttribute("xzero").equals("on")){
                    axes = ",[axes,solid]";
                }
                e.removeAttribute("xzero");
            }
            if (e.hasAttribute("yzero")){
                if (e.getAttribute("yzero").equals("on")){
                    axes = ",[axes,solid]";
                }
                e.removeAttribute("yzero");
            }

        }

        String label ="";
        list = gnu.getElementsByTagName("label");
        for (int i=0; i<list.getLength();i++){
            Element e = (Element)list.item(i);
            String xpos="0";
            String ypos="0";
            String textlabel = e.getTextContent();
            if (!textlabel.equals("")){
                if (textlabel.charAt(0)=='$'){
                    textlabel = textlabel.substring(1);
                }else{
                    textlabel="\""+textlabel+"\"";
                }
            }
            if (e.hasAttribute("xpos")){
                String s = e.getAttribute("xpos");
                if (!s.equals("")){
                    if (s.charAt(0)=='$') s = s.substring(1);
                }
                xpos = s;
            }
            if (e.hasAttribute("ypos")){
                String s = e.getAttribute("ypos");
                if (!s.equals("")){
                    if (s.charAt(0)=='$') s = s.substring(1);
                }
                ypos = s;
            }
            if (i==0){
                label = ",[label,["+textlabel+","+xpos+","+ypos+"]";
            }else{
                label += ",["+textlabel+","+xpos+","+ypos+"]";
            }
            removeNodeFromDOM(e);
        }

        if (!label.equals("")){
            label += "]";
        }

        String fkt="[";
        String style = ",[style";
        String[] pointtypes = {"bullet", "circle", "plus", "times",
                "asterisk", "box", "square", "triangle", "delta",
                "wedge", "nabla", "diamond", "lozenge"};
        String point_type = "[point_type,";
        Boolean existColor=false;
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
                    if ((j>0)||(i>0)) {
                        fkt += ", ";
                    }
                    fkt += fktString;
                }
                e.setTextContent(null);
            }

            NodeList datalist = e.getElementsByTagName("data");
            log.finer("datalist length"+datalist.getLength());

            for (int j=0; j<datalist.getLength();j++){
                 Element el = (Element)datalist.item(j);
                String dataString = el.getTextContent();
                if (!dataString.equals("")){
                    if (dataString.charAt(0)=='@'){
                        dataString = dataString.substring(1);
                    }else{
                        dataString = dataString.replaceAll("$","");
                        dataString = "["+dataString+"]";
                    }
                    if (j==0){
                        if (fktlist.getLength()>0){
                            fkt += ", ";
                        }
                        fkt += "[discrete, "+ dataString;
                    }else{
                        fkt += ", "+dataString;
                    }
                }
                el.setTextContent(null);
            }

             if (datalist.getLength()>0){
                fkt += "]";
            }



            if (e.hasAttribute("color")){
                String c = e.getAttribute("color");
                if (!c.equals("")){
                    if (c.equals("x000000")) c = "black";
                   if (i==0){
                        color = "[color,"+c;
                        existColor = true;
                    }else
                    {
                        color+= ","+c;
                    }
                }
                e.removeAttribute("color");
            }

            if (e.hasAttribute("linestyle")){
                String s = e.getAttribute("linestyle");
                if (!s.equals("")){
                        style += ","+s;
                }
                e.removeAttribute("linestyle");
            }else{
                style += ",lines";
            }

            if (e.hasAttribute("pointtype")){
                int p = Integer.valueOf(e.getAttribute("pointtype"));
                point_type += ","+pointtypes[p];
                e.removeAttribute("pointtype");
            }else{
                point_type += ","+pointtypes[1];
            }

            if (e.hasAttribute("name")){
                String n = e.getAttribute("name");
                if (!n.equals("")){
                    if (title.equals("")){
                        title = ",[legend,\""+n+"\"]";
                    }else{
                        title = title.substring(0, title.length()-2)+" "+n+"\"]";
                    }
                }
                e.removeAttribute("name");
            }

            // remove not-supported attributes
            removeAttributeIfExist(e,"arrowangle");
            removeAttributeIfExist(e,"arrowhead");
            removeAttributeIfExist(e,"arrowlength");
            removeAttributeIfExist(e,"arrowstyle");
            removeAttributeIfExist(e,"limit");
            removeAttributeIfExist(e,"linetype");
            removeAttributeIfExist(e,"pointsize");
            removeAttributeIfExist(e,"arrowbackangle");
            removeAttributeIfExist(e,"linewidth");

        }
        fkt += "]";
        if (existColor) {
            color += "]";
        }
        style += "]";

        plotString = "{@ plot("+fkt+xminmax+yminmax+axes+style+tics+label;

        plotString += xlabel+ylabel+title+align+box+color+grid+size+alt+") @}";
    }

    @Override
    public void addToMdlQuestion(QuestionStack question) {
        question.addToQuestionText(plotString);
    }

    public String getPlotString(){ return plotString; }
}
