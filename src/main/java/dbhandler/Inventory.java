package dbhandler;

/**
 * Created by Ayush Bandil on 14/3/2020.
 */
public class Inventory {
    String itemId;
    Integer quantity;
    long lastUpdate;

    public Inventory(String itemId, Integer quantity, long lastUpdate) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.lastUpdate = lastUpdate;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
