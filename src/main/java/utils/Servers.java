package utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Servers {

    private static String host = Constants.CTP_BASE_URL;

    public static void setHost(String newHost){
        host = newHost;
    }
    /**
     * Obtiene una lista de tst de un servidor dado
     * @param serverName Nombre del servidor que queremos analizar.
     * @return Retorna un {@code List<String>} con el filepath de todos los tst correspondientes al servidor ingresado
     * concatenado junto con su id correspondiente separados por un ";" además de su nombre.
     *
     * Ejemplo:
     *
     * {@code "filepath;id;name"}
     */
    public static List<String> obtenerTstsDesdeServer(String serverName){
        // Mandar peticion GET, primero configurar las credenciales
        RESTClient.activateHeaders(true);
        String username = Constants.CTP_USER;
        String password = Constants.CTP_PASSWORD;
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        RESTClient.addHeader("Authorization", "Basic " + encodedCredentials);
        String response = RESTClient.GET(host + "em/api/v3/tests");

        // Se procesa el response y se obtienen los tst correspondientes al tst y se guardan en una lista
        JSONArray ja = new JSONArray(new JSONObject(response).optJSONArray("testScenarios"));

        List<String> list = new ArrayList<>();

        for(int i=0;i<ja.length();i++){
            if(ja.getJSONObject(i).getString("serverName").equals(serverName)){
                String filepath = ja.getJSONObject(i).getString("filepath");
                String id = ja.getJSONObject(i).get("id").toString();
                String name = ja.getJSONObject(i).get("name").toString();
                list.add(filepath + ";" + id + ";" + name);
            }

        }
        return list;
    }

    public static List<String> obtenerIds(List<String> names, String serverName){
        List<String> resultados = obtenerTstsDesdeServer(serverName);
        List<String> ids = new ArrayList<>();


        for(String resultado:resultados){
            if(names.contains(resultado.split(";")[2])){
                ids.add(resultado.split(";")[1]);
            }
        }

        return ids;
    }

}
