package lc2mdl;

import lc2mdl.util.CmdReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main{
	public static Logger log=Logger.getLogger(Main.class.getName());

	public static void main(String[] args){
		// GET CLI-PARAMS
		CmdReader cmd=new CmdReader();
		try{
			cmd.readCmd(args);
		}catch(Exception e){
			System.err.println(e.getMessage());
			cmd.printUsage();
			return;
		}
		if(cmd.optionIsSet("-h")){
			cmd.printUsage();
			System.out.println(Prefs.WHAT_DO_I_DO);
			return;
		}
		final boolean removeTmpFiles=cmd.optionIsSet("-t");
		final boolean removeLogFiles=cmd.optionIsSet("-T");
		final boolean verbosity=cmd.optionIsSet("-v");
		final boolean findRecursively=cmd.optionIsSet("-r") || cmd.optionIsSet("-R");
		final boolean findRecursivelyForce=cmd.optionIsSet("-R");
		File inputfile=new File(cmd.getFrom());
		File outputfile=new File(cmd.getTo());

		Converter conv=new Converter(verbosity,removeTmpFiles,removeLogFiles);

		// if both are files do normal
		if(inputfile.isFile()){
			// create outputfile if not exists
			if(!outputfile.exists()){
				try{
					System.out.println("Create file: "+outputfile.getAbsolutePath());
					outputfile.createNewFile();
				}catch(IOException e){
					System.out.println("Could not create file: "+outputfile.getAbsolutePath());
					e.printStackTrace();
				}
			}
			if(outputfile.isFile()){
				conv.convertFile(inputfile,outputfile);
				return;
			}
		}

		// if folder do following for each problem-file in folder
		// if folder, copy inputfile-name and create new files according to
		// inputfile-name
		if(inputfile.isDirectory()){
			if(!outputfile.exists()){
				System.out.println("Create folder: "+outputfile.getAbsolutePath());
				outputfile.mkdirs();
			}
			if(outputfile.isDirectory()){
				final File inputfolder=inputfile;
				final File outputfolder=outputfile;
				HashMap<String,String> files=new HashMap<>();
				System.out.println("Looking for 'problem'-files...");
				findProblemFilesRecursively(inputfolder,outputfolder,files,findRecursively);

				for(String s:files.keySet()){
					System.out.println(s);
					System.out.println("->"+files.get(s));
				}
				
				//only ask if "you're not sure" ;)
				if(!findRecursivelyForce){
					Scanner scan=new Scanner(System.in);
					System.out.println("Do you really want to convert "+files.size()+" 'problem'-files? (y/n)");

					// if no return
					if(!scan.next().equals("y")){
						System.out.println("aborted.");
						scan.close();
						return;
					}
					scan.close();
				}

				int converted=0;
				int convertedFull=0;
				HashMap<String,String> abortedFiles=new HashMap<>();

				// if yes convert
				for(String filename:files.keySet()){
					inputfile=new File(filename);
					outputfile=new File(files.get(filename));

					// test if target folder exists and create if not
					File parentDir=new File(outputfile.getParent());
					if(!parentDir.exists()){
						parentDir.mkdirs();
						System.out.println("created folder: "+parentDir.getAbsolutePath());
					}

					System.out.println();
					System.out.println("#######################################################");
					System.out.println("FROMFILE: "+inputfile.getAbsolutePath());
					System.out.println("TOFILE:   "+outputfile.getAbsolutePath());
					int c=conv.convertFile(inputfolder,inputfile,outputfile);

					if(c==1) converted++;
					if(c==2) convertedFull++;
					if(c==0) abortedFiles.put(filename,files.get(filename));
				}

				System.out.println();
				System.out.println("########################################################");
				System.out.println("SUMMARY over all files:");
				System.out.println("Worked on "+files.size()+" files.");
				System.out.println("converted successfully: "+(converted+convertedFull));
				System.out.println("- partially:            "+converted);
				System.out.println("- full:                 "+convertedFull);
				int aborted=(files.size()-(converted+convertedFull));
				System.out.println("aborted:                "+aborted);
				if(aborted>0){
					System.out.println("aborted files (correct error and try again):");
					for(String abname:abortedFiles.keySet()){
						System.out.println("lc2mdl -v "+abname+" "+abortedFiles.get(abname));
					}
				}
				return;
			}
		}

		// else if not match
		System.out.println("FROMFILE and TOFILE doesn't match file or folder.");
		cmd.printUsage();
		return;
	}

	private static void findProblemFilesRecursively(final File inputfolder,final File outputfolder,
			HashMap<String,String> files,boolean findRecursively){
		for(final File curInputfile:inputfolder.listFiles()){
			final File curOutpufile;
			if(curInputfile.isDirectory()&&findRecursively){
				// add same named file in target-dir
				curOutpufile=new File(outputfolder.getAbsolutePath()+"/"+curInputfile.getName());

				findProblemFilesRecursively(curInputfile,curOutpufile,files,findRecursively);
			}else{
				String extension="";
				String basename=null;
				int i=curInputfile.getName().lastIndexOf('.');
				if(i>0){
					basename=curInputfile.getName().substring(0,i);
					extension=curInputfile.getName().substring(i+1);
				}
				if(extension.equals("problem")&&basename!=null){
					curOutpufile=new File(outputfolder.getAbsolutePath()+"/"+basename+".xml");// curInputfile.getName());
					files.put(curInputfile.getAbsolutePath(),curOutpufile.getAbsolutePath());
				}
			}
		}
	}

}