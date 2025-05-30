package extendedyoutubeplaylist;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class FolderItem extends Item {
	private FolderItem parentFolder;
	private ArrayList<Item> items = new ArrayList<Item>();
	private Consumer<FolderItem> setCurrentFolder;
	
	FolderItem(String folderName, ZonedDateTime createdZonedDateTime, DateTimeFormatter createdZonedDateFormatter, Runnable updateItemVBox, Consumer<Item> moveItemUp, Consumer<Item> moveItemDown, Consumer<Item> renameItem, Consumer<Item> deleteItem, FolderItem parentFolder, Consumer<FolderItem> setCurrentFolder) {
		super(folderName, createdZonedDateTime, createdZonedDateFormatter, updateItemVBox, moveItemUp, moveItemDown, renameItem, deleteItem);
		this.parentFolder = parentFolder;
		this.setCurrentFolder = setCurrentFolder;
	}
	
	/**
	 * return element that name is folderName
	 * @param folderName
	 * @return
	 */
	private HBox getElement(String folderName, boolean hasButtons) {
		String svgContent = "M.54 3.87.5 3a2 2 0 0 1 2-2h3.672a2 2 0 0 1 1.414.586l.828.828A2 2 0 0 0 9.828 3h3.982a2 2 0 0 1 1.992 2.181l-.637 7A2 2 0 0 1 13.174 14H2.826a2 2 0 0 1-1.991-1.819l-.637-7a2 2 0 0 1 .342-1.31zM2.19 4a1 1 0 0 0-.996 1.09l.637 7a1 1 0 0 0 .995.91h10.348a1 1 0 0 0 .995-.91l.637-7A1 1 0 0 0 13.81 4zm4.69-1.707A1 1 0 0 0 6.172 2H2.5a1 1 0 0 0-1 .981l.006.139q.323-.119.684-.12h5.396z";
		StackPane svgStackPane = Utils.getSvgStackPane(svgContent, Color.BLACK, Item.itemHboxHeight);
		
		HBox hbox = this.getItemHBox(folderName, hasButtons, svgStackPane, Color.BLACK);
		
		hbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				setCurrentFolder.accept(FolderItem.this);
			}
		});
		
		return hbox;
	}
	private HBox getElement(String folderName) {
		return this.getElement(folderName, true);
	}
	private HBox getElementAsParent() {
		return this.getElement("..", false);
	}
	
	public String getFolderPath() {
		if (this.parentFolder == null) return "/";
		else if (this.parentFolder.getFolderPath().equals("/")) return "/" + this.getName();
		return this.parentFolder.getFolderPath() + "/" + this.getName();
	}
	@Override
	public HBox getElement() {
		return this.getElement(this.getName());
	}
	public VBox getElementOfItems() {
		VBox vbox = new VBox();
		if (this.parentFolder != null) {
			vbox.getChildren().add(this.parentFolder.getElementAsParent());
		}
		for (Item i : items) {
			vbox.getChildren().add(i.getElement());
		}
		return vbox;
	}
	/**
	 * If any items' name include item's name, add item to items
	 * @param item
	 * @return whether item added or not
	 */
	public boolean addItem(Item item) {
		for (Item i : this.items) {
			if (i.getName().equals(item.getName())) {
				return false;
			}
		}
		this.items.add(item);
		return true;
	}
	public void removeItem(Item item) {
		this.items.remove(item);
	}
	public void moveItemUp(Item item) {
		int index = items.indexOf(item);
		if (index > 0) {
			items.set(index, items.get(index - 1));
			items.set(index - 1, item);
		}
	}
	public void moveItemDown(Item item) {
		int index = items.indexOf(item);
		if (index + 1 < items.size()) {
			items.set(index, items.get(index + 1));
			items.set(index + 1, item);
		}
	}
	public void addAllVideoHereToVideoPlayer(VideoPlayer videoPlayer) {
		for (Item i : this.items) {
			if (i instanceof VideoItem) videoPlayer.addVideo((VideoItem)i);
		}
	}
	public void addAllVideoUnderHereToVideoPlayer(VideoPlayer videoPlayer) {
		for (Item i : this.items) {
			if (i instanceof VideoItem) videoPlayer.addVideo((VideoItem)i);
			if (i instanceof FolderItem) ((FolderItem)i).addAllVideoUnderHereToVideoPlayer(videoPlayer);
		}
	}
	public boolean isEmpty() {
		if (this.items.size() == 0) return true;
		return false;
	}
	public boolean hasItemOfDuplicatedName(String name) {
		for (Item i : this.items) {
			if (i.getName().equals(name)) return true;
		}
		return false;
	}
	public int getTotalFolderItems() {
		int count = 0;
		for (Item i : this.items) {
			if (i instanceof FolderItem) count++;
		}
		return count;
	}
	public int getTotalVideoItems() {
		int count = 0;
		for (Item i : this.items) {
			if (i instanceof VideoItem) count++;
		}
		return count;
	}
	public ObjectNode getObjectNode(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("type", "folder");
		node.put("name", this.getName());
		node.put("createdZonedDateTime", this.getFormattedCreatedZonedDateTime());
		ArrayNode itemsArrayNode = mapper.createArrayNode();
		for (Item i : this.items) {
			if (i instanceof FolderItem) itemsArrayNode.add(((FolderItem)i).getObjectNode(mapper));
			if (i instanceof VideoItem) itemsArrayNode.add(((VideoItem)i).getObjectNode(mapper));
		}
		node.set("items", itemsArrayNode);
		return node;
	}
	public void setNodeFromJson(JsonNode node, Consumer<VideoItem> setSoleVideoOfVideoPlayer) {
		for (JsonNode n : node.get("items")) {
			if (!(n.has("type") && n.has("name") && n.has("createdZonedDateTime"))) continue;
			
			String type = n.get("type").asText();
			
			if (type.equals("folder")) {
				FolderItem childFolder = new FolderItem(n.get("name").asText(), this.createdZonedDateTimeParse(n.get("createdZonedDateTime").asText()), this.getZonedDateTimeFormatter(), this.getUpdateItemVBox(), this.getMoveItemUp(), this.getMoveItemDown(), this.getRenameItem(), this.getDeleteItem(), this, setCurrentFolder);
				this.addItem(childFolder);
				childFolder.setNodeFromJson(n, setSoleVideoOfVideoPlayer);
			}
			if (type.equals("video")) {
				if (n.has("videoId")) this.addItem(new VideoItem(n.get("name").asText(), this.createdZonedDateTimeParse(n.get("createdZonedDateTime").asText()), this.getZonedDateTimeFormatter(), this.getUpdateItemVBox(), this.getMoveItemUp(), this.getMoveItemDown(), this.getRenameItem(), this.getDeleteItem(), n.get("videoId").asText(), setSoleVideoOfVideoPlayer));
			}
		}
	}
}
