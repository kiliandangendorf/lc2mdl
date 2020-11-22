package lc2mdl.util;

import java.util.ArrayList;
import java.util.Arrays;

public class CmdReader{
	private class CmdOption{
		String option;
		String shortOption;
		boolean isset;
		String usage;

		public CmdOption(String option,String shortOption, boolean isset,String usage){
			this.option=option;
			this.shortOption=shortOption;
			this.isset=isset;
			this.usage=usage;
		}

		public String toString(){
			String string="";
			if(!option.equals(""))string+=option+",";
			else string+="   ";
			string+=shortOption+"\t"+usage;
			return string;
		}
	}

	private ArrayList<String> arguments=new ArrayList<>();
	private ArrayList<CmdOption> options=new ArrayList<>(Arrays.asList(
			new CmdOption("-h","--help",false,"shows usage"),
			new CmdOption("-v","--verbose",false,"verbose output"),
			new CmdOption("-r","--recursive",false,"find files recursively in folder (lists and ask for confirmation before converting)"),			
			new CmdOption("-R","--recnocon",false,"find files recursively in folder (starts converting without confirmation)"),			
			new CmdOption("-t","--rmtmp",false,"remove tmp-files (automatically done if empty)"),			
			new CmdOption("-T","--rmlog",false,"remove log-files (NOT RECOMMENDED)"),

			//convert options
			new CmdOption("-p","--prefercheckbox",false,"prefer checkbox, if only two options (optionresponse)"),

			//languages beta
			new CmdOption("","--de",false,"choose \"de\" as default language"),
			new CmdOption("","--en",false,"choose \"en\" as default language")
			));
	private String from;
	private String to;
	private String usage="Usage: lc2mdl [options] FROMFILE TOFILE"+System.lineSeparator()
			+"FROMFILE: LON-CAPA problem-file or folder."+System.lineSeparator()
			+"TOFILE:   Moodle-STACK xml-file or folder."+System.lineSeparator()
			+"FROMFILE and TOFILE must be both files or folders.";

	public boolean optionIsSet(String option){
		for(CmdOption o:options){
			if(o.shortOption.equals(option))return o.isset;
			if(o.option.equals(option))return o.isset;
		}
		return false;
	}
	public String getFrom(){
		return from;
	}
	public String getTo(){
		return to;
	}
	
	/**
	 * Reads args-Array used to be from CLI.
	 * If more or less than exact 2 Arguments IllegalArgumentException is thrown.
	 * No limits for options.
	 */
	public void readCmd(String[] args) throws Exception{// throws IllegalArgumentException{
		
		if(args.length==0)throw new IllegalArgumentException("Too few arguments.");

		for(int i=0;i<args.length;i++){
			if(args[i].charAt(0)=='-'){
				// too short
//				if(args[i].length()<2)throw new IllegalArgumentException("Invalid argument: "+args[i]);

				// double opt
				if(args[i].charAt(1)=='-'){
					// too short
					if(args[i].length()<3)throw new IllegalArgumentException("Invalid argument: "+args[i]);

					setOpt(args[i]); //maybe add boolean if option gets param... and add next arg if exists
				}else{
					// opt
					//for multiple opts eg. -vh
					String s=args[i].substring(1);//remove -
					for(char c:s.toCharArray())setOpt("-"+c);
//					setOpt(args[i]);
				}
			}else{
				// arg
				arguments.add(args[i]);
			}
		}
		if(optionIsSet("-h"))return;
		
		if(arguments.size()<2)throw new IllegalArgumentException("Too few arguments: "+arguments);
		if(arguments.size()>2)throw new IllegalArgumentException("Too much arguments: "+arguments);
		
		from=arguments.get(0);
		to=arguments.get(1);
	}

	private void setOpt(String opt){
		for(CmdOption o:options){
			if(o.option.equals(opt))o.isset=true;
			if(o.shortOption.equals(opt))o.isset=true;
		}
	}

	public void printUsage(){
		System.out.println(usage);
		for(CmdOption o:options)System.out.println(o.toString());
	}
}
