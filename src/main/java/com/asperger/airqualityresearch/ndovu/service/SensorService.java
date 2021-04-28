package com.asperger.airqualityresearch.ndovu.service;

import com.asperger.airqualityresearch.ndovu.models.Sensor;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorService {

    public static List<Sensor> getDailySensors(String date, String month,
                                               String year, int port) {
        List<Sensor> dailySensors = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri =
                    new URI("http://localhost:" + port + "/maps/" +
                            String.join("/", year,
                                    String.format("%02d",
                                            Integer.parseInt(month)),
                                    String.format("%02d",
                                            Integer.parseInt(date))) +
                            "/air-quality-data.json");
            HttpRequest request = HttpRequest.newBuilder().uri(uri).method(
                    "GET", HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Server problems when getting " +
                        "daily sensors");
            }
            JsonArray json =
                    JsonParser.parseString(response.body()).getAsJsonArray();
            for (int i = 0; i < json.size(); i++) {
                Sensor sensor = new Sensor(json.get(i).getAsJsonObject().get(
                        "location").getAsString());
                sensor.setBattery(Float.parseFloat(json.get(i).getAsJsonObject().get("battery").getAsString()));
                String reading =
                        json.get(i).getAsJsonObject().get("reading").getAsString();
                if (reading.equals("null") || reading.equals("NaN")) {
                    sensor.setReading(Float.NaN);
                } else {
                    sensor.setReading(Float.parseFloat(reading));
                }
                dailySensors.add(sensor);
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return dailySensors;
    }
}
