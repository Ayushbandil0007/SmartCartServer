package dbhandler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by Ayush Bandil on 12/2/2020.
 */
public class ConnectionUtils {
    private static String url = "jdbc:sqlserver://localhost:1434;databaseName=SmartCart;";
    private static String username = "mapsuser";
    private static String password = "mapsuser";
    private static Gson gson = new Gson();
    private static SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

    public static void main(String[] args) {
        String itemId = "item01";
        // 1 Item Details
        HashMap<String, ItemDetails> itemDetailsjson = getItemDetails(itemId);

        // 2 Insert purchase info and update inventory
        String phoneNo = "9999999999";
        String userName = "AyushNew";
        HashMap<String, Integer> collection = new HashMap<>();
        collection.put("item01", 1);
        collection.put("item02", 3);

        UpdatePurchaseInfo(userName, phoneNo, collection);
        updateInventory(collection);

        // 3 inventory report
        ArrayList<Inventory> inventory = getInventoryDetails();
        HashMap<String, HashMap<String, Integer>> inventoryReport = generateInventoryReport(inventory);
        gson.toJson(inventoryReport);

        // 4 get all/perticular user purchase history
        ArrayList<PurchaseInfo> purchaseHistory = getUserPurchaseHistory(userName);
        ArrayList<PurchaseInfo> purchaseHistoryAll = getUserPurchaseHistory(null);
        gson.toJson(purchaseHistoryAll);

        // 5 get user analytics report
        HashMap<String, UserAnalyticsInfo> analyticsReport = getUserAnalyticsReport();
    }

    public static HashMap<String, UserAnalyticsInfo> getUserAnalyticsReport() {
        ArrayList<PurchaseInfo> purchaseHistoryAll = getUserPurchaseHistory(null);
        HashMap<String, ItemDetails> priceMap = getItemDetails(null);
        HashMap<String, UserAnalyticsInfo> analMap = new HashMap<>();
        for (PurchaseInfo purchaseInfo : purchaseHistoryAll) {
            String userName = purchaseInfo.userName;
            if (!analMap.containsKey(userName)) {
                Double totalMoneySpent = getCartValue(purchaseInfo.getCollection(), priceMap);
                Integer totalItemsPurchased = getCartItemsCount(purchaseInfo.getCollection());
                Double activeSince = ((double) new Date().getTime() / 1000 - purchaseInfo.getTime()) / 86400;
                Integer totalVisits = 1;
                Date lastVisit = new Date(purchaseInfo.getTime() * 1000);
                analMap.put(userName, new UserAnalyticsInfo(userName, purchaseInfo.phoneNo, totalMoneySpent,
                        totalVisits, totalItemsPurchased, totalMoneySpent / totalItemsPurchased,
                        (int) Math.round(activeSince), lastVisit, ((double) totalVisits) / activeSince, null,
                        null));
            } else {
                UserAnalyticsInfo prev = analMap.get(userName);
                Double totalMoneySpent = prev.getTotalMoneySpent() + getCartValue(purchaseInfo.getCollection(), priceMap);
                prev.setTotalMoneySpent(totalMoneySpent);
                Integer totalVisits = prev.getTotalVisits() + 1;
                prev.setTotalVisits(totalVisits);
                Integer totalItemsPurchased = getCartItemsCount(purchaseInfo.getCollection()) + prev.getTotalItemsPurchased();
                prev.setTotalItemsPurchased(totalItemsPurchased);
                prev.setAvgMoneySpentPerItem(totalMoneySpent / totalItemsPurchased);
                Double activeSince = Math.max(((double) new Date().getTime() / 1000 - purchaseInfo.getTime()) / 86400, (double) prev.activeSince);
                Date lastVisit = new Date(purchaseInfo.getTime() * 1000).before(prev.getLastVisit()) ? new Date(purchaseInfo.getTime() * 1000) : prev.getLastVisit();
                analMap.put(userName, new UserAnalyticsInfo(userName, purchaseInfo.phoneNo, totalMoneySpent,
                        totalVisits, totalItemsPurchased, totalMoneySpent / totalItemsPurchased,
                        (int) Math.round(activeSince), lastVisit, ((double) totalVisits) / activeSince, null,
                        null));
            }
        }
        for (Map.Entry<String, UserAnalyticsInfo> entry : analMap.entrySet()) {
            UserAnalyticsInfo value = entry.getValue();
            HashMap<String, Integer> totalPurchaseMap = getTotalPurchaseMapForUser(purchaseHistoryAll, entry.getKey());
            value.setMostPurchasedItem(getMostPurchasedItem(totalPurchaseMap, entry.getKey()));
            value.setMostMoneySpentOnItem(getMostMoneySpentItem(totalPurchaseMap, entry.getKey(), priceMap));
        }
        return analMap;
    }

    private static HashMap<String, Integer> getTotalPurchaseMapForUser(ArrayList<PurchaseInfo> purchaseHistoryAll, String userName) {
        HashMap<String, Integer> totalPurchaseMap = new HashMap<>();
        for (PurchaseInfo purchaseInfo : purchaseHistoryAll) {
            if (purchaseInfo.getUserName().equals(userName)) {
                HashMap<String, Integer> collection = purchaseInfo.getCollection();
                collection.forEach((k, v) -> {
                    if (totalPurchaseMap.containsKey(k)) {
                        totalPurchaseMap.put(k, v + totalPurchaseMap.get(k));
                    } else {
                        totalPurchaseMap.put(k, v);
                    }
                });
            }
        }
        return totalPurchaseMap;
    }

    private static String getMostMoneySpentItem(HashMap<String, Integer> totalPurchaseMap, String key, HashMap<String, ItemDetails> priceMap) {
        String item = "";
        Double maxItemPrice = 0d;
        for (Map.Entry<String, Integer> entry : totalPurchaseMap.entrySet()) {
            if (maxItemPrice < entry.getValue() * priceMap.get(entry.getKey()).getPrice()) {
                maxItemPrice = entry.getValue() * priceMap.get(entry.getKey()).getPrice();
                item = entry.getKey();
            }
        }
        return item;
    }

    private static String getMostPurchasedItem(HashMap<String, Integer> totalPurchaseMap, String username) {
        String item = "";
        Integer maxItemCount = 0;
        for (Map.Entry<String, Integer> entry : totalPurchaseMap.entrySet()) {
            if (entry.getValue() > maxItemCount) {
                maxItemCount = entry.getValue();
                item = entry.getKey();
            }
        }
        return item;
    }

    private static Integer getCartItemsCount(HashMap<String, Integer> collection) {
        Integer value = 0;
        for (Map.Entry<String, Integer> entry : collection.entrySet()) {
            value += entry.getValue();
        }
        return value;
    }

    private static Double getCartValue(HashMap<String, Integer> collection, HashMap<String, ItemDetails> priceMap) {
        double value = 0d;
        for (Map.Entry<String, Integer> entry : collection.entrySet()) {
            Integer qty = entry.getValue();
            double price = priceMap.get(entry.getKey()).getPrice();
            value += price * qty;
        }
        return value;
    }

    public static ArrayList<PurchaseInfo> getUserPurchaseHistory(String userName) {
        ArrayList<PurchaseInfo> toReturn = new ArrayList<>();
        String query = "SELECT * FROM PURCHASE_INFO WHERE USER_NAME = '<USER_NAME>' order by time desc";
        if (userName == null || userName.length() == 0 || userName.equalsIgnoreCase("all")) {
            query = query.replace("WHERE USER_NAME = '<USER_NAME>' ", "");
        } else {
            query = query.replace("<USER_NAME>", userName);
        }
        Connection con = ConnectionUtils.getConnection();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String phoneNo = rs.getString("PHONE_NUMBER").trim();
                String purchaseJson = rs.getString("PURCHASE_JSON").trim();
                Integer time = rs.getInt("TIME");
                String userNameDb = rs.getString("USER_NAME").trim();
                toReturn.add(new PurchaseInfo(phoneNo, userNameDb, gson.fromJson(purchaseJson, new TypeToken<HashMap<String, Integer>>() {
                }.getType()), time));
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public static void updateInventory(HashMap<String, Integer> collection) {
        ArrayList<Inventory> inventory = getLatestInventoryDetails();
        HashMap<String, Integer> invMap = new HashMap<>();
        for (Inventory item : inventory) {
            invMap.put(item.getItemId(), item.getQuantity());
        }
        for (Map.Entry<String, Integer> purchase : collection.entrySet()) {
            invMap.put(purchase.getKey(), invMap.get(purchase.getKey()) - purchase.getValue());
        }
        updateInventoryTable(invMap);
        System.out.println("  updated inventory, time: " + new java.util.Date());
    }

    private static void updateInventoryTable(HashMap<String, Integer> invMap) {
        String queryDefault = "INSERT INTO [INVENTORY]\n" +
                "           ([ITEM_ID]\n" +
                "           ,[QUANTITY]\n" +
                "           ,[LAST_UPDATE])\n" +
                "     VALUES\n" +
                "           ('<ITEM_ID>'\n" +
                "           ,<QUANTITY>\n" +
                "           ,<LAST_UPDATE>)";

        String query = null;
        for (Map.Entry<String, Integer> entry : invMap.entrySet()) {
            query = queryDefault.replace("<ITEM_ID>", entry.getKey());
            query = query.replace("<QUANTITY>", entry.getValue() + "");
            query = query.replace("<LAST_UPDATE>", new Date().getTime() / 1000 + "");
            executeJdbcQuery(query);
        }
    }

    private static ArrayList<String> getAllItemIds() {
        String query = "select DISTINCT item_id from inventory";
        Connection con = ConnectionUtils.getConnection();
        PreparedStatement ps = null;
        ArrayList<String> toReturn = new ArrayList<>();
        try {
            ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String itemId = rs.getString("ITEM_ID").trim();
                toReturn.add(itemId);
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public static ArrayList<Inventory> getInventoryDetails() {
        String query = "select * from INVENTORY order by last_update";
        Connection con = ConnectionUtils.getConnection();
        PreparedStatement ps;
        ArrayList<Inventory> inventoryCollection = new ArrayList<Inventory>();
        try {
            ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String itemId = rs.getString("ITEM_ID").trim();
                Integer quantity = rs.getInt("QUANTITY");
                Long lastUpdate = rs.getLong("LAST_UPDATE");
                Inventory inventory = new Inventory(itemId, quantity, lastUpdate);
                inventoryCollection.add(inventory);
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventoryCollection;
    }

    private static ArrayList<Inventory> getLatestInventoryDetails() {
        ArrayList<String> itemIds = getAllItemIds();
        String queryDefault = "select top 1 * from INVENTORY   where item_id = '<ITEM_ID>' order by LAST_update desc";
        String query;
        Connection con = ConnectionUtils.getConnection();
        PreparedStatement ps;
        ResultSet rs = null;
        ArrayList<Inventory> inventoryCollection = new ArrayList<>();
        try {
            for (String itemId : itemIds) {
                query = queryDefault.replace("<ITEM_ID>", itemId);
                ps = con.prepareStatement(query);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Integer quantity = rs.getInt("QUANTITY");
                    Long lastUpdate = rs.getLong("LAST_UPDATE");
                    Inventory inventory = new Inventory(itemId, quantity, lastUpdate);
                    inventoryCollection.add(inventory);
                }
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventoryCollection;
    }

    public static HashMap<String, HashMap<String, Integer>> generateInventoryReport(ArrayList<Inventory> inventoryCollection) {
        HashMap<String, HashMap<String, Integer>> invMap = new LinkedHashMap<>();
        HashMap<String, Integer> prevDateEntries = new HashMap<>();
        for (Inventory inventory : inventoryCollection) {
            String entryDate = sdf.format(new Date(inventory.getLastUpdate() * 1000));
            if (invMap.containsKey(entryDate)) {
                HashMap<String, Integer> dayEntries = invMap.get(entryDate);
                dayEntries.put(inventory.getItemId(), inventory.getQuantity());
                prevDateEntries = dayEntries;
            } else {
                HashMap<String, Integer> value = new HashMap<>();
                value = (HashMap<String, Integer>) prevDateEntries.clone();
                value.put(inventory.getItemId(), inventory.getQuantity());
                invMap.put(entryDate, value);
            }
        }
        return invMap;
    }

    public static void UpdatePurchaseInfo(String username, String phoneNo, HashMap<String, Integer> collection) {
        String query = "INSERT INTO [dbo].[PURCHASE_INFO]\n" +
                "           ([USER_NAME]\n" +
                "           ,[PHONE_NUMBER]\n" +
                "           ,[TIME]\n" +
                "           ,[PURCHASE_JSON])\n" +
                "     VALUES\n" +
                "           ('<USER_NAME>'\n" +
                "           ,'<PHONE_NUMBER>'\n" +
                "           ,<TIME>\n" +
                "           ,'<PURCHASE_JSON>')";
        query = query.replace("<USER_NAME>", username);
        query = query.replace("<PHONE_NUMBER>", phoneNo);
        query = query.replace("<PURCHASE_JSON>", gson.toJson(collection));
        query = query.replace("<TIME>", "" + (new java.util.Date().getTime() / 1000));
        executeJdbcQuery(query);
        System.out.println("  inserted purchase information, user: " + username + ", time: " + new java.util.Date());
    }

    public static HashMap<String, ItemDetails> getItemDetails(String itemId) {
        String query = "select * from ITEM_DETAILS where ITEM_ID = '<ITEM_ID>'";
        if (itemId == null || itemId.length() == 0) {
            query = query.replace(" where ITEM_ID = '<ITEM_ID>'", "");
        } else {
            query = query.replace("<ITEM_ID>", itemId);
        }
        HashMap<String, ItemDetails> toReturn = new HashMap<>();
        Connection con = ConnectionUtils.getConnection();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String itemIdDB = rs.getString("ITEM_ID").trim();
                String itemName = rs.getString("ITEM_NAME").trim();
                String itemCategory = rs.getString("ITEM_CATEGORY").trim();
                Double price = rs.getDouble("PRICE");
                ItemDetails details = new ItemDetails(itemName, itemCategory, price, itemIdDB);
                toReturn.put(itemIdDB, details);
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public static void executeJdbcQuery(String query) {
        Connection con = ConnectionUtils.getConnection();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(query);
            ps.execute();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            if (connection != null) {
                return connection;
            } else {
                throw new SQLException("Could not establish connection");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double executeJdbcSingleOutputQuery(String query) {
        Connection con = ConnectionUtils.getConnection();
        Double toReturn = 0d;
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                toReturn = rs.getDouble("VALUE");
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toReturn;
    }
}
