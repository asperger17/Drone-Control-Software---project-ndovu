package com.asperger.airqualityresearch.ndovu.service;

import com.asperger.airqualityresearch.ndovu.models.NoFlyZone;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoFlyZoneService {

    public static List<NoFlyZone> getNoFlyZones(int port) {
        List<NoFlyZone> noFlyZones = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = new URI("http://localhost:" + port + "/buildings/no-fly"
                    + "-zones" + ".geojson");
            HttpRequest request = HttpRequest.newBuilder().uri(uri).method(
                    "GET", HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Server problems when getting the "
                        + "no fly zones info!");
            }
            List<Feature> features =
                    FeatureCollection.fromJson(response.body()).features();
            assert features != null;
            for (Feature feature : features) {
                NoFlyZone noFlyZone = new NoFlyZone(feature);
                noFlyZones.add(noFlyZone);
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return noFlyZones;
    }
}
