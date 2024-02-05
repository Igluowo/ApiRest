/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package usoApi;

import clases.Agente;
import clases.Arma;
import clases.Mapa;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import interfaces.Buscar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author erick
 */
public class BuscarApi {

    HttpURLConnection conexion;

    //Al crear el objeto con una URL se hace la conexion
    public BuscarApi(String url) {
        try {
            URI uri = new URI(url);
            URL urlApi = uri.toURL();
            conexion = (HttpURLConnection) urlApi.openConnection();
        } catch (URISyntaxException ex) {

        } catch (MalformedURLException ex) {

        } catch (IOException ex) {

        }
    }

    //Para obtener los datos
    public ArrayList<Object> obtenerDatos(String unico, String objeto) {
        ArrayList<Object> listaObjetos = new ArrayList<>();
        try {
            conexion.setRequestMethod("GET");
            conexion.connect();
            int codigoRespuesta = conexion.getResponseCode();
            if (codigoRespuesta != 200) { //Si la repuesta es diferente a 200, salta un error.
                throw new Exception("Ha ocurrido un error, el código es: " + codigoRespuesta);
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                StringBuilder respuesta = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    respuesta.append(line);
                }
                reader.close();
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(respuesta.toString(), JsonObject.class);
                JsonArray dataArray = null;
                //Condicional para crear solo uno o varios objetos
                if (!unico.isEmpty()) {
                    JsonObject dataObject = jsonObject.getAsJsonObject("data");
                    Object agente = crear(dataObject, objeto);
                    listaObjetos.add(agente);
                } else {
                    dataArray = jsonObject.getAsJsonArray("data");
                    // Iterar sobre los elementos del array "data"
                    for (int i = 0; i < dataArray.size(); i++) {
                        JsonObject dataObject = dataArray.get(i).getAsJsonObject();
                        Object agente = crear(dataObject, objeto);
                        listaObjetos.add(agente);
                    }
                }
            }
        } catch (ProtocolException ex) {

        } catch (IOException ex) {

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "No se ha podido encontrar el uuid", "Error 404", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        return listaObjetos;
    }

    private Object crear(JsonObject dataObject, String objeto) {
        String rol = "No tiene";
        String descripcion = "No tiene";
        String nombre = dataObject.get("displayName").getAsString();
        //Comprueba si el agente tiene rol
        if (dataObject.has("role") && dataObject.get("role").isJsonObject()) {
            JsonObject roleObject = dataObject.getAsJsonObject("role");
            rol = roleObject.get("displayName").getAsString();
        }
        if (dataObject.has("description")) {
            descripcion = dataObject.get("description").getAsString();
        } else if (dataObject.has("narrativeDescription")) {
            descripcion = dataObject.has("narrativeDescription") && !dataObject.get("narrativeDescription").isJsonNull()
                ? dataObject.get("narrativeDescription").getAsString() : "";
        }
        String uuid = dataObject.get("uuid").getAsString();
        String iconoUrl = dataObject.has("displayIcon") && !dataObject.get("displayIcon").isJsonNull()
                ? dataObject.get("displayIcon").getAsString() : "";
        Object objetoDevolver = null;
        if (objeto.equals("agents")) {
            objetoDevolver = new Agente(uuid, nombre, descripcion, rol, iconoUrl);
        } else if (objeto.equals("maps")) {
            objetoDevolver = new Mapa(uuid, nombre, descripcion, iconoUrl);
        } else {
            objetoDevolver = new Arma(uuid, nombre, iconoUrl);
        }
        return objetoDevolver;
    }

    public HttpURLConnection getConexion() {
        return conexion;
    }

    public void setConexion(HttpURLConnection conexion) {
        this.conexion = conexion;
    }
}
