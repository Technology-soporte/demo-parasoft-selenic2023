package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONManager {

    public static String readJSONFile(String filePath) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            try {
                throw new IOException("Error reading JSON file: " + e.getMessage());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        return content.toString();
    }

    public static String modifyJSONField(String jsonStr, String fieldToModify, String newValue) {
        try {
            // Convertir el string JSON en un objeto JSONObject
            JSONObject jsonObject = new JSONObject(jsonStr);

            // Modificar el valor del campo especificado
            jsonObject.put(fieldToModify, newValue);

            // Devolver el JSON modificado como string
            return jsonObject.toString();
        } catch (Exception e) {
            System.err.println("Error modifying JSON: " + e.getMessage());
            return jsonStr; // Devolver el JSON original en caso de error
        }
    }

    public static String modifyJSONField(String jsonStr, String fieldToModify, JSONArray newValue) {
        try {
            // Convertir el string JSON en un objeto JSONObject
            JSONObject jsonObject = new JSONObject(jsonStr);

            // Modificar el valor del campo especificado
            jsonObject.put(fieldToModify, newValue);

            // Devolver el JSON modificado como string
            return jsonObject.toString();
        } catch (Exception e) {
            System.err.println("Error modifying JSON: " + e.getMessage());
            return jsonStr; // Devolver el JSON original en caso de error
        }
    }

    public static String beautifyJSON(String json) {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            Object jsonObject = mapper.readValue(json, Object.class);
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            return json;
        }
    }

}