package lc2mdl.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileFinder {



    public static String findFilesRecursively(File inputfolder,  String pathToFile, HashMap<String,String> files) throws FileNotFoundException {

        String[] pathSplit = pathToFile.split("/");
        String fileName = pathSplit[pathSplit.length-1];
        List<String> filesFound = searchFile(inputfolder,fileName);
        try {
            if (filesFound.isEmpty()) {
                throw (new FileNotFoundException("no file "+fileName+ " in path with parent directory"+inputfolder.getAbsolutePath()));
            }else{
                if (filesFound.size()>1){
                    String path=matchPath(pathToFile,filesFound);
                    if (!path.equals("")){ files.put(fileName,path);
                    } else {
                        throw (new FileNotFoundException("could not identify file "+fileName+ " in path with parent directory"+inputfolder.getAbsolutePath()));
                    }
                }else{
                    files.put(fileName,filesFound.get(0));
                }
            }
        } catch (FileNotFoundException e){
            throw e;
        }

        return fileName;
    }

    private static List<String> searchFile(File inputFolder, String fileName){
        List<String> filesFound = new ArrayList<String>();
        if (inputFolder.isDirectory()) {
            File[] fileList = inputFolder.listFiles();
            for (File f : fileList) {
                filesFound.addAll(searchFile(f, fileName));
            }
        }else{
                if (fileName.equals(inputFolder.getName())){
                    filesFound.add(inputFolder.getAbsolutePath());
                }
            }
        return filesFound;
    }

    private static String matchPath(String pathToFile, List<String> filesFound) {
        String path = "";

        if (filesFound.size()==1) {
            path = filesFound.get(0);
        }
        else{
            int maxindex = 0;
            String[] pathSplit = pathToFile.split("/");
            for (int depth=2; depth<pathSplit.length; depth++){
                int index=0;
                for (String f : filesFound) {
                    String[] fpathSplit = f.split("/");
                    if (pathSplit[pathSplit.length - depth].equals(fpathSplit[fpathSplit.length - depth])) {
                        maxindex=index;
                    }
                    index++;
                }

                depth++;
            }
            path = filesFound.get(maxindex);
            }

        return path;
    }
}
