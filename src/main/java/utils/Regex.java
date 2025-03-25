package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

    public static List<String> extractMatches(String patron,String texto) {
        List<String> ids = new ArrayList<String>();

        Pattern pattern = Pattern.compile(patron);
        Matcher matcher = pattern.matcher(texto);

        while (matcher.find()) {
            ids.add(matcher.group(1));
        }

        return ids;
    }
}