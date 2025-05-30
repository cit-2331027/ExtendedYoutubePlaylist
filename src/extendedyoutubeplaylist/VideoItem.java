package extendedyoutubeplaylist;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

class VideoItem extends Item {
	private String videoId;
	private Image image;
	private Consumer<VideoItem> setSoleVideoOfVideoPlayer;
	
	VideoItem(String videoName, ZonedDateTime createdZonedDateTime, DateTimeFormatter createdZonedDateTimeFormatter, Runnable updateItemVBox, Consumer<Item> moveItemUp, Consumer<Item> moveItemDown, Consumer<Item> renameItem, Consumer<Item> deleteItem, String videoId, Consumer<VideoItem> setSoleVideoOfVideoPlayer) {
		super(videoName, createdZonedDateTime, createdZonedDateTimeFormatter, updateItemVBox, moveItemUp, moveItemDown, renameItem, deleteItem);
		this.videoId = videoId;
		this.loadImageAsync();
		this.setSoleVideoOfVideoPlayer = setSoleVideoOfVideoPlayer;
	}
	
	private void loadImageAsync() {
		String[] qualities = {
				"maxresdefault.jpg",
				"sddefault.jpg",
				"hqdefault.jpg",
				"mqdefault.jpg",
				"default.jpg"
		};
        Task<Image> loadImageTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
            	for (String q : qualities) {
            		Image i = new Image("https://img.youtube.com/vi/" + VideoItem.this.videoId + "/" + q);
            		if (i.isError() == false) return i;
            	}
            	return null;
            }
        };

        loadImageTask.setOnSucceeded(event -> {
            this.image = loadImageTask.getValue();
            this.getUpdateItemVBox().run();
        });

        new Thread(loadImageTask).start();
    }
	
	public String getVideoId() {
		return this.videoId;
	}
	@Override
	public HBox getElement() {
		return this.getElement(true, this.setSoleVideoOfVideoPlayer);
	}
	public HBox getElement(boolean hasButtons, Consumer<VideoItem> onClicked) {
		String svgContent = "M0 2a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2zm5.5 10a.5.5 0 0 0 .832.374l4.5-4a.5.5 0 0 0 0-.748l-4.5-4A.5.5 0 0 0 5.5 4z";
		StackPane stackPane = Utils.getSvgStackPane(svgContent, Color.RED, itemHboxHeight);
		
		ImageView imageView = new ImageView();
		imageView.setFitWidth(Item.itemHboxWidth);
		imageView.setFitHeight(Item.itemHboxHeight);
		if (this.image != null) {
			imageView.setImage(this.image);
			stackPane.getChildren().clear();
			stackPane.getChildren().add(imageView);
		}
		
		HBox hbox = this.getItemHBox(this.getName(), hasButtons, stackPane, Color.RED);
		hbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				onClicked.accept(VideoItem.this);
			}
		});
		
		return hbox;
	}
	public ObjectNode getObjectNode(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("type", "video");
		node.put("name", this.getName());
		node.put("createdZonedDateTime", this.getFormattedCreatedZonedDateTime());
		node.put("videoId", this.videoId);
		return node;
	}
}