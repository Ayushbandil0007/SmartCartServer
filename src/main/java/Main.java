import com.google.gson.Gson;
import dbhandler.*;
import javafx.util.Pair;
import org.webbitserver.*;
import org.webbitserver.netty.NettyWebServer;
import org.apache.commons.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Main {
    private static Gson gson = new Gson();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        WebServer webServer = new NettyWebServer(9992);
        final Rest rest = new Rest(webServer);
        rest.GET("/login", new LoginUser());
        rest.GET("/topics", new ChatTopics());
        rest.GET("/chatroom/{topic}", new ChatRoom());
        rest.GET("/register", new RegisterUser());
        rest.GET("/static/*", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest req, HttpResponse res, HttpControl ctl) {
                try{
                    String filepath = req.uri().replace("/static/", "");
                    InputStream inputStream = getClass()
                            .getClassLoader().getResourceAsStream(filepath);
                    byte[] byteArr = IOUtils.toByteArray(inputStream);

                    res.content(byteArr).end();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });

        rest.POST("/UpdatePurchaseInfo", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest req, HttpResponse res, HttpControl ctl) {
                String username = req.postParams("userName").get(0);
                String phoneNo = req.postParams("phoneNo").get(0);
                String collectionStr = req.postParams("collection").get(0);
                System.out.println("Received request, endpoint: UpdatePurchaseInfo, user: " + username);
                HashMap<String, Integer> collection = getCollectionFromString(collectionStr);
                ConnectionUtils.UpdatePurchaseInfo(username, phoneNo, collection);
                ConnectionUtils.updateInventory(collection);
                res.content("{\"code\":\"200\",\"status\":\"success\"}").end();
                System.out.println("Successfully processed, endpoint: UpdatePurchaseInfo, user: " + username);
            }
        });

        rest.GET("/GetInventoryReport", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest req, HttpResponse res, HttpControl ctl) {
                System.out.println("Received request, endpoint: GetInventoryReport, time: " + new Date());
                ArrayList<Inventory> inventory = ConnectionUtils.getInventoryDetails();
                HashMap<String, HashMap<String, Integer>> inventoryReport = ConnectionUtils.generateInventoryReport(inventory);
                res.content(gson.toJson(inventoryReport)).end();
                System.out.println("Successfully processed, endpoint: GetInventoryReport, time: " + new Date());
            }
        });

        rest.GET("/getItemDetails/{json}", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest req, HttpResponse res, HttpControl ctl) {
                String inputJson = req.uri().split("/")[2];
                System.out.println("Received request, endpoint: getItemDetails, item: " + inputJson);
                HashMap<String, ItemDetails> itemDetails = ConnectionUtils.getItemDetails(inputJson);
                String responseJson = gson.toJson(itemDetails.get(inputJson));
                res.content(responseJson).end();
                System.out.println("Response sent successfully, endpoint: getItemDetails, item: " + inputJson);
            }
        });

        rest.GET("/getUserPurchaseHistory/{json}", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest req, HttpResponse res, HttpControl ctl) {
                String inputJson = req.uri().split("/")[2];
                System.out.println("Received request, endpoint: getUserPurchaseHistory, user: " + inputJson);
                ArrayList<PurchaseInfo> purchaseHistory = ConnectionUtils.getUserPurchaseHistory(inputJson);
                if (purchaseHistory.size()==0){
                    res.content("No purchase history for the user").end();
                } else {
                    String responseJson = gson.toJson(purchaseHistory);
                    res.content(responseJson).end();
                }
                System.out.println("Response sent successfully, endpoint: getUserPurchaseHistory, item: " + inputJson);
            }
        });

        rest.GET("/getUserAnalyticsReport", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest req, HttpResponse res, HttpControl ctl) {
                System.out.println("Received request, endpoint: getUserAnalyticsReport");
                HashMap<String, UserAnalyticsInfo> analyticsReport = ConnectionUtils.getUserAnalyticsReport();
                if (analyticsReport.size()==0){
                    res.content("No Report to show").end();
                } else {
                    String responseJson = gson.toJson(analyticsReport);
                    res.content(responseJson).end();
                }
                System.out.println("Response sent successfully, endpoint: getUserAnalyticsReport");
            }
        });

        webServer.start().get();
        System.out.println("Try this: curl -i localhost:9992/login");
    }

    private static HashMap<String, Integer> getCollectionFromString(String collectionStr) {
        HashMap<String, Integer> toReturn = new HashMap<>();
        String[] itemCol = collectionStr.replaceAll("\\{","").replaceAll("\\}","")
                .replaceAll("\\[","").replaceAll("\\]","")
                .replaceAll("\"", "")
                .split(",");

        for (String s : itemCol) {
            String[] pair =  s.split(":");
            Integer prev = toReturn.containsKey(pair[0])? toReturn.get(pair[0]) : 0;
            toReturn.put(pair[0], prev + Integer.valueOf(pair[1]));
        }
        return toReturn;
    }
}
