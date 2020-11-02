package dbhandler;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ayush Bandil on 15/3/2020.
 */
public class PurchaseInfo {
    String phoneNo;
    String userName;
    HashMap<String, Integer> collection;
    long time;

    public PurchaseInfo(String phoneNo, String userName, HashMap<String, Integer> collection, long time) {
        this.phoneNo = phoneNo;
        this.userName = userName;
        this.collection = collection;
        this.time = time;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public HashMap<String, Integer> getCollection() {
        return collection;
    }

    public void setCollection(HashMap<String, Integer> collection) {
        this.collection = collection;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
