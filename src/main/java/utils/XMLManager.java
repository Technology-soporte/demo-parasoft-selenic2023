package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class XMLManager {
    public static String leerXML(String path){
        StringBuilder content = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    public static List<String> obtenerClasesDesdeXML(String path){
        String xmlContent = leerXML(path);
        List<String> clases = Regex.extractMatches("class name=\"([^\"]+)\"", xmlContent);

        for(int i=0;i<clases.size();i++){
            if(clases.get(i).contains(".")){
                String[] claseSplitted = clases.get(i).split("\\.");
                clases.set(i, claseSplitted[claseSplitted.length-1]);
            }
        }

        return clases;
    }
}
