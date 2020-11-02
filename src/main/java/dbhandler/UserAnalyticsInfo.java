package dbhandler;

import java.util.Date;

/**
 * Created by Ayush Bandil on 15/3/2020.
 */
public class UserAnalyticsInfo {
    String userName;
    String phoneNo;
    Double totalMoneySpent;
    Integer totalVisits;
    Integer totalItemsPurchased;
    Double avgMoneySpentPerItem;
    Integer activeSince;    //days
    Date lastVisit;
    Double visitFreqency;   //visits/day
    String mostPurchasedItem;
    String mostMoneySpentOnItem;

    public UserAnalyticsInfo(String userName, String phoneNo, Double totalMoneySpent, Integer totalVisits, Integer totalItemsPurchased, Double avgMoneySpentPerItem, Integer activeSince, Date lastVisit, Double visitFreqency, String mostPurchasedItem, String mostMoneySpentOnItem) {
        this.userName = userName;
        this.phoneNo = phoneNo;
        this.totalMoneySpent = totalMoneySpent;
        this.totalVisits = totalVisits;
        this.totalItemsPurchased = totalItemsPurchased;
        this.avgMoneySpentPerItem = avgMoneySpentPerItem;
        this.activeSince = activeSince;
        this.lastVisit = lastVisit;
        this.visitFreqency = visitFreqency;
        this.mostPurchasedItem = mostPurchasedItem;
        this.mostMoneySpentOnItem = mostMoneySpentOnItem;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public Double getTotalMoneySpent() {
        return totalMoneySpent;
    }

    public void setTotalMoneySpent(Double totalMoneySpent) {
        this.totalMoneySpent = totalMoneySpent;
    }

    public Integer getTotalVisits() {
        return totalVisits;
    }

    public void setTotalVisits(Integer totalVisits) {
        this.totalVisits = totalVisits;
    }

    public Integer getTotalItemsPurchased() {
        return totalItemsPurchased;
    }

    public void setTotalItemsPurchased(Integer totalItemsPurchased) {
        this.totalItemsPurchased = totalItemsPurchased;
    }

    public Double getAvgMoneySpentPerItem() {
        return avgMoneySpentPerItem;
    }

    public void setAvgMoneySpentPerItem(Double avgMoneySpentPerItem) {
        this.avgMoneySpentPerItem = avgMoneySpentPerItem;
    }

    public Integer getActiveSince() {
        return activeSince;
    }

    public void setActiveSince(Integer activeSince) {
        this.activeSince = activeSince;
    }

    public Date getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(Date lastVisit) {
        this.lastVisit = lastVisit;
    }

    public Double getVisitFreqency() {
        return visitFreqency;
    }

    public void setVisitFreqency(Double visitFreqency) {
        this.visitFreqency = visitFreqency;
    }

    public String getMostPurchasedItem() {
        return mostPurchasedItem;
    }

    public void setMostPurchasedItem(String mostPurchasedItem) {
        this.mostPurchasedItem = mostPurchasedItem;
    }

    public String getMostMoneySpentOnItem() {
        return mostMoneySpentOnItem;
    }

    public void setMostMoneySpentOnItem(String mostMoneySpentOnItem) {
        this.mostMoneySpentOnItem = mostMoneySpentOnItem;
    }
}
