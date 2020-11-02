package dbhandler;

/**
 * Created by Ayush Bandil on 14/3/2020.
 */
public class ItemDetails {
    String itemName;
    String itemCategory;
    Double price;
    String itemId;

    public ItemDetails(String itemName, String itemCategory, Double price, String itemId) {
        this.itemName = itemName;
        this.itemCategory = itemCategory;
        this.price = price;
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}