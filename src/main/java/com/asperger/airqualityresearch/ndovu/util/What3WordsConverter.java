package com.asperger.airqualityresearch.ndovu.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mapbox.geojson.Point;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class What3WordsConverter {

    public static Point fromStringLocationToPoint(String location) {
        try {
            String address = String.join("/", Arrays.asList(location.split(
                    "\\.")));
            URI uri = new URI("http://localhost/words/" + address + "/details"
                    + ".json");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(uri).method(
                    "GET", HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Experiencing server " +
                        "technicalities: " + response.statusCode());
            }
            JsonObject json =
                    JsonParser.parseString(response.body()).getAsJsonObject();
            return Point.fromLngLat(json.get("coordinates").getAsJsonObject().get("lng").getAsDouble(), json.get("coordinates").getAsJsonObject().get("lat").getAsDouble());
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return null;
    }
}

