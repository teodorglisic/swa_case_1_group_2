package ch.fhnw.students;

import org.camunda.bpm.client.ExternalTaskClient;
import org.json.JSONObject;

import java.sql.SQLException;
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


        client.subscribe("group2_droolsEngine").lockDuration(1000).handler(
                ((externalTask, externalTaskService) -> {
                    Long weight = (Long) externalTask.getVariable("weight");
                    String country = (String) externalTask.getVariable("deliveryCountry");

                    JSONObject courierAPIResult = ApiRequester.selectCourierAPI(country, weight);
                    System.out.println(courierAPIResult);
                    HashMap<String, Object> result = new HashMap<>();
                    int statusCode = courierAPIResult.getInt("statusCode");
                    if (statusCode == 202) {
                        result.put("destination", courierAPIResult.getString("destination"));
                        result.put("statusCode", statusCode);
                        result.put("deliveryType", courierAPIResult.getString("deliveryType"));
                        result.put("deliveryCheck", courierAPIResult.getString("deliveryType"));
                    } else {
                       result.put("statusCode", statusCode);
                       if (!Objects.equals(courierAPIResult.getString("destination"), "NOT_DEFINED")) {
                           result.put("destination", courierAPIResult.getString("destination"));
                       } else {
                           result.put("destination", courierAPIResult.getString("deliveryCountryManualCheck"));
                       }
                       result.put("deliveryCheck", courierAPIResult.getString("deliveryType"));

                    }

                    externalTaskService.complete(externalTask, result);

                })
        ).open();

        client.subscribe("group2_requestAPI").lockDuration(1000).handler(
                (externalTask, externalTaskService) -> {
                    String street = (String) externalTask.getVariable("deliveryAddressStreet");
                    Long houseNumber = (Long) externalTask.getVariable("deliveryAddressHouseNumber");
                    String postalCode = (String) externalTask.getVariable("deliveryPostalCode");
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


        // Service task für Protokollierung in SQL
        client.subscribe("group2_sqlLogging").lockDuration(1000).handler(
                (externalTask, externalTaskService) -> {

                    String orderID = (String) externalTask.getVariable("orderId");
                    String customerId = (String) externalTask.getVariable("customerId");
                    String destination = (String) externalTask.getVariable("destination");
                    Long weight = (Long) externalTask.getVariable("weight");
                    String deliveryType = (String) externalTask.getVariable("deliveryType");

                    System.out.println(orderID);
                    System.out.println(customerId);
                    System.out.println(destination);
                    System.out.println(weight);
                    System.out.println(deliveryType);


                    try {
                        DatabaseLogger.createConnection();
                        DatabaseLogger.dbInit();
                        DatabaseLogger.logging(orderID, customerId, destination, weight, deliveryType);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }


                    externalTaskService.complete(externalTask);
        }).open();
    }




}
