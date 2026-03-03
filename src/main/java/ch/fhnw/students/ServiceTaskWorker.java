package ch.fhnw.students;

import org.camunda.bpm.client.ExternalTaskClient;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ServiceTaskWorker {

    public static void main(String[] args) {
        ExternalTaskClient client = ExternalTaskClient
                .create()
                .baseUrl("http://group2:A0fBMV7M7qGbPh@192.168.111.3:8080/engine-rest")
                .asyncResponseTimeout(1000)
                .build();

        client.subscribe("group2_requestAPI").lockDuration(1000).handler(
                (externalTask, externalTaskService) -> {
                    String street = (String) externalTask.getVariable("deliveryAddressStreet");
                    Long houseNumber = (Long) externalTask.getVariable("deliveryAddressHouseNumber");
                    Long postalCode = (Long) externalTask.getVariable("deliveryPostalCode");
                    String city = (String) externalTask.getVariable("deliveryCity");
                    String country = (String) externalTask.getVariable("deliveryCountry");
                    String phone = (String) externalTask.getVariable("customerPhone");
                    String email = (String) externalTask.getVariable("customerEmail");
                    Long weight = (Long) externalTask.getVariable("weight");
                    String customerId = (String) externalTask.getVariable("customerId");

                    JSONObject apiResult = ApiRequester.apiRequester(
                            city + ", " + street + " " + houseNumber,
                            customerId,
                            phone,
                            weight);

                    HashMap<String, Object> result = new HashMap<>();
                    int statusCode = apiResult.getInt("statusCode");
                    if (statusCode == 202) {
                        result.put("trackingNumber", apiResult.getString("orderId"));

                        result.put("statusCode", statusCode);

                        LocalDate pickupLocalDate = LocalDate.parse(apiResult.getString("pickupdate"));
                        Date pickupDate = Date.from(pickupLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        result.put("dateOfPickup", pickupDate);

                        LocalDate deliveryLocalDate = LocalDate.parse(apiResult.getString("deliverydate"));
                        Date deliveryDate = Date.from(deliveryLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        result.put("expectedDeliveryDate", deliveryDate);
                    } else {
                        result.put("statusCode", statusCode);
                    }

                    externalTaskService.complete(externalTask, result);

        }).open();
    }




}
