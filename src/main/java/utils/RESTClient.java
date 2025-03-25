package utils;

import com.parasoft.api.Application;
import com.parasoft.api.ExtensionToolContext;

import java.io.BufferedReader;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class RESTClient {

    private static boolean activateHeaders = false;
    private static List<String> headers = new ArrayList<String>();

    private static HttpURLConnection addHeader(HttpURLConnection conexion){
        for(String header:headers){
            String[] aux = header.split(",");
            conexion.addRequestProperty(aux[0], aux[1]);
        }
        return conexion;
    }

    public static void activateHeaders(boolean activate){
        activateHeaders = activate;
    }

    private static void resetHeaders(){
        headers = new ArrayList<String>();
    }

    public static void addHeader(String name,String value){
        headers.add(name + "," + value);
    }

    public static String formatURL(String url) {
        String new_url = "";

        String[] splitedUrl = url.split("\\?");

        for(int i = 1;i<splitedUrl.length;i++) {
            splitedUrl[i] = splitedUrl[i].replaceAll("/", "%2F");
            splitedUrl[i] = splitedUrl[i].replaceAll(" ", "%20");
        }

        for(int i = 0;i<splitedUrl.length;i++) {
            if(i == 0) new_url += splitedUrl[i];
            else new_url += "?" + splitedUrl[i];
        }

        return new_url;
    }



    public static String GET(Object input, ExtensionToolContext context) {

        String url = input.toString();

        StringBuilder respuesta = new StringBuilder();
        try {
            URL urlObj = new URL(url);
            Application.showMessage(url);
            HttpURLConnection conexion = (HttpURLConnection) urlObj.openConnection();
            conexion.setRequestMethod("GET");
            if(activateHeaders) addHeader(conexion);

            int responseCode = conexion.getResponseCode();
            BufferedReader lector;

            if (responseCode >= 200 && responseCode < 300) {
                lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            } else {
                lector = new BufferedReader(new InputStreamReader(conexion.getErrorStream()));
            }
            String linea;

            while ((linea = lector.readLine()) != null) {
                respuesta.append(linea);
            }

            lector.close();
        } catch (Exception e) {
            resetHeaders();
            e.printStackTrace();
        }
        resetHeaders();
        Application.showMessage(respuesta.toString());
        return respuesta.toString();
    }


    public static String GET(String url) {
        StringBuilder respuesta = new StringBuilder();
        try {
            URL urlObj = new URL(url);
            Application.showMessage("GET: " + url);
            HttpURLConnection conexion = (HttpURLConnection) urlObj.openConnection();
            conexion.setRequestMethod("GET");
            if(activateHeaders) addHeader(conexion);

            int responseCode = conexion.getResponseCode();
            BufferedReader lector;

            if (responseCode >= 200 && responseCode < 300) {
                lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            } else {
                lector = new BufferedReader(new InputStreamReader(conexion.getErrorStream()));
            }
            String linea;

            while ((linea = lector.readLine()) != null) {
                respuesta.append(linea);
            }

            lector.close();
        } catch (Exception e) {
            resetHeaders();
            e.printStackTrace();
        }
        resetHeaders();
        return respuesta.toString();
    }
    public static String DELETE(String url) {
        StringBuilder respuesta = new StringBuilder();
        try {
            URL urlObj = new URL(url);
            Application.showMessage("DELETE: " + url);
            HttpURLConnection conexion = (HttpURLConnection) urlObj.openConnection();
            conexion.setRequestMethod("DELETE");
            if(activateHeaders) addHeader(conexion);

            int responseCode = conexion.getResponseCode();
            BufferedReader lector;

            if (responseCode >= 200 && responseCode < 300) {
                lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            } else {
                lector = new BufferedReader(new InputStreamReader(conexion.getErrorStream()));
            }
            String linea;

            while ((linea = lector.readLine()) != null) {
                respuesta.append(linea);
            }

            lector.close();
        } catch (Exception e) {
            resetHeaders();
            e.printStackTrace();
        }
        resetHeaders();
        return respuesta.toString();
    }
    public static String POST(String url, String body) {
        StringBuilder respuesta = new StringBuilder();

        try {
            URL urlObj = new URL(url);
            Application.showMessage("POST: " + url);
            Application.showMessage("BODY: " + body);
            HttpURLConnection conexion = (HttpURLConnection) urlObj.openConnection();
            conexion.setRequestMethod("POST");
            if(activateHeaders) addHeader(conexion);
            conexion.setRequestProperty("Content-Type", "application/json");
            conexion.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(conexion.getOutputStream());
            outputStream.writeBytes(body);
            outputStream.flush();
            outputStream.close();

            int responseCode = conexion.getResponseCode();
            BufferedReader lector;

            if (responseCode >= 200 && responseCode < 300) {
                lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            } else {
                lector = new BufferedReader(new InputStreamReader(conexion.getErrorStream()));
            }

            String linea;

            while ((linea = lector.readLine()) != null) {
                respuesta.append(linea);
            }

            lector.close();
        } catch (Exception e) {
            resetHeaders();
            e.printStackTrace();
        }
        resetHeaders();
        return respuesta.toString();
    }

    public static String PUT(String url, String body) {
        StringBuilder respuesta = new StringBuilder();

        try {
            URL urlObj = new URL(url);
            Application.showMessage("PUT: " + url);
            Application.showMessage("BODY: " + body);
            HttpURLConnection conexion = (HttpURLConnection) urlObj.openConnection();
            conexion.setRequestMethod("PUT");
            if(activateHeaders) addHeader(conexion);
            conexion.setRequestProperty("Content-Type", "application/json");
            conexion.setDoOutput(true);

            OutputStream outputStream = conexion.getOutputStream();
            outputStream.write(body.getBytes());
            outputStream.flush();
            outputStream.close();

            BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            String linea;

            while ((linea = lector.readLine()) != null) {
                respuesta.append(linea);
            }

            lector.close();
        } catch (Exception e) {
            resetHeaders();
            Application.showMessage(e.getMessage());
            for(StackTraceElement ste:e.getStackTrace()) {

                Application.showMessage(ste.toString());
            }
        }
        resetHeaders();
        return respuesta.toString();
    }
}
