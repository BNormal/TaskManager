package Mule;

import org.dreambot.api.wrappers.items.Item;

public class OwnedItem {

	String owner;
	Item item;
	
	public OwnedItem(String owner, Item item) {
		this.owner = owner;
		this.item = item;
	}
	
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public Item getItem() {
		return item;
	}
	public void setItem(Item item) {
		this.item = item;
	}

}
