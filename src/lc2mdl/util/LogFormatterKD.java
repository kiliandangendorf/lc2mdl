package lc2mdl.util;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogFormatterKD extends Formatter{
	@Override
	public String format(LogRecord record){
		StringBuilder sb=new StringBuilder();
		if(record.getLevel()==Level.WARNING || record.getLevel()==Level.SEVERE)sb.append(record.getLevel()+": ");
		sb
			.append(record.getMessage())
//			.append(" ("+record.getSourceClassName()+"."+record.getSourceMethodName()+")")
			.append(System.lineSeparator());
		return sb.toString();
	}
}
