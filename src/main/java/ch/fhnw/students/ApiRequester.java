package ch.fhnw.students;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ApiRequester {

    public static void main(String[] args) {
        JSONObject object = apiRequester("Olten, Rigenbachstrasse 16", "hallo", "welt", 500L);
        System.out.println(object);
    }

    public static JSONObject apiRequester(String street, String customerId, String customerPhone, Long weight) {
        try (HttpClient client = HttpClient.newHttpClient()){


            JSONObject jsonBody = new JSONObject();

            jsonBody.put("destination", street);
            jsonBody.put("customerReference", customerId);
            jsonBody.put("recepientPhone", customerPhone);
            jsonBody.put("weight", weight);

            System.out.println(jsonBody);

            HttpRequest request = HttpRequest
                    .newBuilder()
                    .uri(new URI("http://192.168.111.5:8080/v1/consignment/request"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonBody)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 202) {
                JSONObject responseBody = new JSONObject(response.body());
                responseBody.put("statusCode", response.statusCode());
                return responseBody;
            } else {
                return new JSONObject().put("statusCode", response.statusCode());
            }

        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
