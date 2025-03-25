package utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Base64;
import java.util.List;

public class Jobs {


    private static String host = Constants.CTP_BASE_URL;

    public static void setHost(String newHost){
        host = newHost;
    }

    public static String crearJob(String nombre, String testConfiguration, String owner){
        // Se verifica si ya existe un job con ese nombre, si existe se retorna el ID del job existente
        String jobId = obtenerJobId(nombre);
        if(jobId == null){
            // Se obtiene el body desde el archivo json
            String body = JSONManager.readJSONFile(Constants.BODY_POST_EM_JOBS);
            // Se actualiza el body con las variables correspondientes
            body = JSONManager.modifyJSONField(body, "name", nombre);
            body = JSONManager.modifyJSONField(body, "testConfiguration", testConfiguration);
            body = JSONManager.modifyJSONField(body, "owner", owner);
            // Se ejecuta POST
            RESTClient.activateHeaders(true);
            String username = Constants.CTP_USER;
            String password = Constants.CTP_PASSWORD;
            String credentials = username + ":" + password;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            RESTClient.addHeader("Authorization", "Basic " + encodedCredentials);
            String response = RESTClient.POST(host + "em/jobs", body);
            return new JSONObject(response).get("id").toString();
        }
        else return jobId;


    }

    public static String agregarTstAJob(String jobId, List<String> tstIds){
        // Se obtiene el body desde el archivo json
        String body = JSONManager.readJSONFile(Constants.BODY_PUT_EM_API_V3_JOBS_ID);

        // Se genera la estructura con N cantidad de tst Id's (JSON Array)
        JSONArray ja = new JSONArray();
        for(int i=0;i<tstIds.size();i++){
            JSONObject jo = new JSONObject();
            jo.put("testScenarioId", tstIds.get(i));
            ja.put(jo);
        }

        // Se modifica el campo con el nuevo JSON Array
        body = JSONManager.modifyJSONField(body, "testScenarioInstances", ja);

        // Se ejecuta PUT
        RESTClient.activateHeaders(true);
        String username = Constants.CTP_USER;
        String password = Constants.CTP_PASSWORD;
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        RESTClient.addHeader("Authorization", "Basic " + encodedCredentials);
        String response = RESTClient.PUT(host + "em/api/v3/jobs/" + jobId, body);
        return response;
    }
    public static String obtenerJobId(String jobName){
        // Se ejecuta GET
        RESTClient.activateHeaders(true);
        String username = Constants.CTP_USER;
        String password = Constants.CTP_PASSWORD;
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        RESTClient.addHeader("Authorization", "Basic " + encodedCredentials);
        String response = RESTClient.GET(host + "em/api/v3/jobs");

        // Se procesa el response y se obtienen los jobs y se guardan en una lista
        JSONArray ja = new JSONArray(new JSONObject(response).optJSONArray("jobs"));


        for(int i=0;i<ja.length();i++){
            if(ja.getJSONObject(i).getString("name").equals(jobName)){
                return ja.getJSONObject(i).get("id").toString();
            }

        }

        return null;
    }
}