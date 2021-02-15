package lc2mdl.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class ConvertAndFormatMethods{
	
	/**
	 * Converts given image-String for file of kind JPEG, PNG, BMP, WEBMP or GIF into a svg-String. 
	 * @param imagePath: Path to image-file
	 * @return SVG-String containing base64-encoded image-data
	 * @throws IOException 
	 */
	public static String convertImagePathIntoSvgString(String imageAbsPath) throws IOException{
		if(imageAbsPath==null)throw new IOException();
		
		String base64DataOfImage=null;

		File file=new File(imageAbsPath);
		//only for getting measurements of images
		BufferedImage im=ImageIO.read(file);
		if(im==null)throw new IOException();
		int width=im.getWidth();
		int height=im.getHeight();
		
		String mimetype=URLConnection.guessContentTypeFromName(file.getName());
		//TODO: Check Mime-Type for null 
		
		FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] bytes = new byte[(int)file.length()];
        fileInputStreamReader.read(bytes);
		base64DataOfImage=Base64.getEncoder().encodeToString(bytes);
		fileInputStreamReader.close();
		
		
		//build svg string
		String svgHeader="<svg enable-background=\"new 0 0 "+width+" "+height+"\" height=\""+height+"\" viewBox=\"0 0 "+width+" "+height+"\" width=\""+width+"\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";
		String imageHeader="<image height=\""+height+"\" width=\""+width+"\" xlink:href=\"data:"+mimetype+";base64,";
		String imageFooter="\" />";
		String svgFooter="</svg>";
		
		String imageString=imageHeader+base64DataOfImage+imageFooter;
		return svgHeader+imageString+svgFooter;
	}
	
	
	/**
	 * Determines if giving String is an path to an image file of kind JPEG, PNG, BMP, WEBMP or GIF.
	 */
	public static boolean isImagePath(String pathString){
		if(pathString==null)return false;
		
		//at least one "." before image-suffix
		if(!pathString.contains("."))return false;
		//try to read as an image
		File pretendedFile=new File(pathString);
		try{
			BufferedImage pretendedImage=ImageIO.read(pretendedFile);
			if(pretendedImage!=null)return true;
		}catch(IOException e){
			//go on with returning false
		}
		return false;
	}
	
	/**
	 * Returns String-representation of given dom.Node.
	 * @return eg. <img src="..." />
	 */
	public static String getNodeString(Node node){
		//from: https://dzone.com/articles/java-dom-printing-content-node
		String xmlString=null;
		try{
			// Set up the output transformer
			TransformerFactory transfac=TransformerFactory.newInstance();
			Transformer trans=transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
			trans.setOutputProperty(OutputKeys.INDENT,"yes");
			// Print the DOM node
			StringWriter sw=new StringWriter();
			StreamResult result=new StreamResult(sw);
			DOMSource source=new DOMSource(node);
			trans.transform(source,result);
			xmlString=sw.toString();
		}catch(TransformerException e){
			//returning null
		}
		return xmlString;
	}
	
	/**
	 * Converts double into String with decimal point (".") as decimal seperator and 7 digits after decimal point. 
	 */
	public static String double2StackString(Double d){
		DecimalFormat df = new DecimalFormat("#0.0000000");
	    DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
	    dfs.setDecimalSeparator('.');
	    df.setDecimalFormatSymbols(dfs);
		return df.format(d);
	}


	/**
	 *
	 * @param text   String with round parentheses
	 * @param start  start position
	 * @return       int end position
	 */
	public static int findMatchingParentheses(String text, int start){
		return (findMatchingParentheses(text, start, true));
	}

	/**
	 *
	 * @param text   String with round or curly parentheses
	 * @param start  start position
	 * @param round  true if round, false if curly
	 *
	 * @return       int end position (or -1 if nothing found)
	 */
	public static int findMatchingParentheses(String text,int start,boolean round){
		if(round)return findMatchingParentheses(text,start,'(',')');
		else return findMatchingParentheses(text,start,'{','}');
	}
	
	/**
	 * @param text   String with parentheses
	 * @param start  start position
	 * @param charOpen e.g. '('
	 * @param charClose e.g. ')'
	 *
	 * @return       int end position (or -1 if nothing found)
	 */
	public static int findMatchingParentheses(String text,int start,char charOpen, char charClose){		
		int end=start;
//		char charOpen,charClose;
//		if(round){
//			charOpen='(';
//			charClose=')';
//		}else{
//			charOpen='{';
//			charClose='}';
//		}

		int bracketCount=1;
		while(end<text.length()){
			if(text.charAt(end)==charOpen){
				end++;
				break;
			}
			end++;
		}

		while((bracketCount>0)&&(end<text.length()-1)){
			if(text.charAt(end)==charOpen) bracketCount++;
			if(text.charAt(end)==charClose) bracketCount--;
			end++;
		}

		if(bracketCount!=0){
			return(-1);
		}else{
			return(end);
		}
	}

	/**
	 *  Removes all CR / new lines in
	 * @param text
	 * @return
	 */
	public static String removeCR(String text){
		String newText = text.replaceAll("\\n"," ");
		newText = newText.replaceAll("\\r", " ");
		return (newText);
	}
	
	/**
	 * Returns the start index of a regex match in a given text from a given startIndex
	 * @param regex to match in text
	 * @param text to be searched
	 * @param startIndex starting search at that index
	 * @return first index of match, -1 if there's no match  
	 */
	public static int getRegexStartIndexInText(String regex, String text, int startIndex){
	    Matcher matcher = Pattern.compile(regex).matcher(text);
	    if(matcher.find(startIndex)){
	        return matcher.start();
	    }
	    return -1;
	}
	/**
	 * Returns the start index of a regex match in a given text (starting at char 0)
	 * @param regex to match in text
	 * @param text to be searched
	 * @return first index of match, -1 if there's no match  
	 */
	public static int getRegexStartIndexInText(String regex, String text){
	    return getRegexStartIndexInText(regex, text, 0);
	}
	/**
	 * Returns the start and end index of a regex match in a given text from a given startIndex <br/>
	 * Example:<br/>
	 * 		regex="oob", text="foobar"<br/>
	 * 		return is [1,3]
	 * @param regex to match in text
	 * @param text to be searched
	 * @return first start and end index of match, [-1,-1] if there's no match  
	 */
	public static int[] getRegexStartAndEndIndexInText(String regex, String text, int startIndex){
	    Matcher matcher = Pattern.compile(regex).matcher(text);
	    if(matcher.find(startIndex)){
	        return new int[] {matcher.start(), matcher.end()-1};
	    }
	    return new int[]{-1,-1};
	}
	
	/**
	 * Replaces sequence between from and to in the text by replacement
	 * @param text where replacement should be done
	 * @param from index of first char that should be replaced (incl.)
	 * @param to index of last char that should be replaced (incl.)
	 * @param replacement string
	 * @return modified text
	 */
	public static String replaceSubsequenceInText(String text, int from, int to, String replacement){
		//String.replace was not save, because it would replace any occurance of the replacement string 
		// (eg. elsif(condition) matches if(condition)
		return text.substring(0,from)+replacement+text.substring(to+1,text.length());

	}

	/**
	 * Escapes almost all double quotes (") in given string with (\") not escaped yet 
	 * @param text
	 * @param isInQuotes if true, first and last character of given string will not be checked
	 * @return
	 */
	public static String escapeUnescapedDoubleQuotesInString(String text, boolean isInQuotes){
		//escape quotes in string (potential generated multilang-tags)
		if(isInQuotes){
	        char firstChar=text.charAt(0);
	        char lastChar=text.charAt(text.length()-1);
	        String midString=text.substring(1,text.length()-1);
	        midString=escapeUnescapedDoubleQuotesInWholeString(midString);
	        text=firstChar+midString+lastChar;
		}else{
			text=escapeUnescapedDoubleQuotesInWholeString(text);
		}
        return text;

	}
	/**
	 * Escapes ALL double quotes (") in given string with (\") not escaped yet
	 * @param text
	 * @return
	 */
	public static String escapeUnescapedDoubleQuotesInWholeString(String text){
//		return text.replaceAll("\"","\\\\\"");
		return text.replaceAll("(?<!\\\\)\"","\\\\\"");
	}

}
