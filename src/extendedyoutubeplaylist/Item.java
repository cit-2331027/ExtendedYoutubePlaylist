package extendedyoutubeplaylist;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class Item {
	protected static double itemHboxHeight = 100;
	protected static double itemHboxWidth = Item.itemHboxHeight * (double)16 / 9;
	
	private DateTimeFormatter createdZonedDateTimeFormatter;
	private String name;
	private ZonedDateTime createdZonedDateTime;
	private Runnable updateItemVBox;
	private Consumer<Item> moveItemUp;
	private Consumer<Item> moveItemDown;
	private Consumer<Item> renameItem;
	private Consumer<Item> deleteItem;
	
	Item() {
		this(null, null, null, null, null, null, null, null);
	}
	Item(String name, ZonedDateTime createdZonedDateTime, DateTimeFormatter createdZonedDateTimeFormatter, Runnable updateItemVBox, Consumer<Item> moveItemUp, Consumer<Item> moveItemDown, Consumer<Item> renameItem, Consumer<Item> deleteItem) {
		this.name = name;
		this.createdZonedDateTime = createdZonedDateTime;
		this.createdZonedDateTimeFormatter = createdZonedDateTimeFormatter;
		this.updateItemVBox = updateItemVBox;
		this.moveItemUp = moveItemUp;
		this.moveItemDown = moveItemDown;
		this.renameItem = renameItem;
		this.deleteItem = deleteItem;
	}
	
	public abstract HBox getElement();
	
	private Button getRenameItemButton() {
		String svgContent = "M12.854.146a.5.5 0 0 0-.707 0L10.5 1.793 14.207 5.5l1.647-1.646a.5.5 0 0 0 0-.708zm.646 6.061L9.793 2.5 3.293 9H3.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.207zm-7.468 7.468A.5.5 0 0 1 6 13.5V13h-.5a.5.5 0 0 1-.5-.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.5-.5V10h-.5a.5.5 0 0 1-.175-.032l-.179.178a.5.5 0 0 0-.11.168l-2 5a.5.5 0 0 0 .65.65l5-2a.5.5 0 0 0 .168-.11z";

		Button button = Utils.getAlignedButton(svgContent, Color.GREEN, 25, "Rename", 8);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				renameItem.accept(Item.this);
			}
		});
		
		return button;
	}
	private Button getDeleteItemButton() {
		String svgContent = "M11 1.5v1h3.5a.5.5 0 0 1 0 1h-.538l-.853 10.66A2 2 0 0 1 11.115 16h-6.23a2 2 0 0 1-1.994-1.84L2.038 3.5H1.5a.5.5 0 0 1 0-1H5v-1A1.5 1.5 0 0 1 6.5 0h3A1.5 1.5 0 0 1 11 1.5m-5 0v1h4v-1a.5.5 0 0 0-.5-.5h-3a.5.5 0 0 0-.5.5M4.5 5.029l.5 8.5a.5.5 0 1 0 .998-.06l-.5-8.5a.5.5 0 1 0-.998.06m6.53-.528a.5.5 0 0 0-.528.47l-.5 8.5a.5.5 0 0 0 .998.058l.5-8.5a.5.5 0 0 0-.47-.528M8 4.5a.5.5 0 0 0-.5.5v8.5a.5.5 0 0 0 1 0V5a.5.5 0 0 0-.5-.5";
		
		Button button = Utils.getAlignedButton(svgContent, Color.GRAY, 25, "Delete", 8);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				deleteItem.accept(Item.this);
				updateItemVBox.run();
			}
		});
		
		return button;
	}
	private Button getMoveUpButton() {
		String svgContent = "m7.247 4.86-4.796 5.481c-.566.647-.106 1.659.753 1.659h9.592a1 1 0 0 0 .753-1.659l-4.796-5.48a1 1 0 0 0-1.506 0z";
		
		Button button = Utils.getAlignedButton(svgContent, Color.BLACK, 10, null, 0);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				moveItemUp.accept(Item.this);
				updateItemVBox.run();
			}
		});
		return button;
	}
	private Button getMoveDownButton() {
		String svgContent = "M7.247 11.14 2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z";
		
		Button button = Utils.getAlignedButton(svgContent, Color.BLACK, 10, null, 0);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				moveItemDown.accept(Item.this);
				updateItemVBox.run();
			}
		});
		return button;
	}
	private VBox getMoveButtons() {
		VBox vbox = new VBox();
		vbox.getChildren().add(getMoveUpButton());
		vbox.getChildren().add(getMoveDownButton());
		return vbox;
	}
	protected String getFormattedCreatedZonedDateTime() {
		return this.createdZonedDateTime.format(this.createdZonedDateTimeFormatter);
	}
	protected DateTimeFormatter getZonedDateTimeFormatter() {
		return this.createdZonedDateTimeFormatter;
	}
	protected Runnable getUpdateItemVBox() {
		return this.updateItemVBox;
	}
	protected Consumer<Item> getMoveItemUp() {
		return this.moveItemUp;
	}
	protected Consumer<Item> getMoveItemDown() {
		return this.moveItemDown;
	}
	protected Consumer<Item> getRenameItem() {
		return this.renameItem;
	}
	protected Consumer<Item> getDeleteItem() {
		return this.deleteItem;
	}
	protected HBox getItemHBox(String itemName, boolean hasButtons, StackPane svgStackPane, Color svgColor) {
		svgStackPane.setMinWidth(Item.itemHboxWidth);
		
		Text nameField = new Text();
		nameField.setText(itemName);
		nameField.setFont(Font.font("MS Gothic", 30));
		
		Text createdZonedDateTimeText = new Text("created on:         " + this.getFormattedCreatedZonedDateTime());
		VBox createdZonedDateTimeField = new VBox(createdZonedDateTimeText);
		HBox.setHgrow(createdZonedDateTimeField, Priority.ALWAYS);
		
		VBox moveButtons = this.getMoveButtons();
		moveButtons.setAlignment(Pos.BOTTOM_LEFT);
		Button renameButton = this.getRenameItemButton();
		Button deleteButton = this.getDeleteItemButton();
		
		HBox buttonField = new HBox();
		//buttonField.getChildren().add(moveButtons);
		createdZonedDateTimeField.getChildren().add(moveButtons);
		buttonField.getChildren().add(renameButton);
		buttonField.getChildren().add(deleteButton);
		buttonField.setAlignment(Pos.BOTTOM_RIGHT);
		
		HBox hbox = new HBox();
		hbox.getChildren().add(createdZonedDateTimeField);
		if (hasButtons) hbox.getChildren().add(buttonField);
		VBox.setVgrow(hbox, Priority.ALWAYS);
		
		VBox infoField = new VBox();
		infoField.getChildren().add(nameField);
		infoField.getChildren().add(hbox);
		infoField.setPadding(new Insets(0, 0, 0, 10));
		HBox.setHgrow(infoField, Priority.ALWAYS);

		HBox itemHbox = new HBox();
		itemHbox.setMinHeight(Item.itemHboxHeight);
		itemHbox.getChildren().add(svgStackPane);
		itemHbox.getChildren().add(infoField);
		itemHbox.setStyle("-fx-border-color: #AAAAAA");
		
		return itemHbox;
	}
	
	public ZonedDateTime createdZonedDateTimeParse(String string) {
		return ZonedDateTime.parse(string, this.createdZonedDateTimeFormatter);
	}
	public String getName() {
		return this.name;
	}
 	public void setName(String name) {
		this.name = name;
	}
 	public ZonedDateTime getCreatedZonedDateTime() {
 		return this.createdZonedDateTime;
 	}
 	public void setCreatedZonedDateTime(ZonedDateTime createdZonedDateTime) {
 		this.createdZonedDateTime = createdZonedDateTime;
 	}
}
