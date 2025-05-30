package extendedyoutubeplaylist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class Main extends Application {
	private FolderItem rootFolder;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		Text folderPath = new Text();
		
		AtomicReference<FolderItem> currentFolder = new AtomicReference<>();
		AtomicReference<VBox> itemVBox = new AtomicReference<>();
		itemVBox.set(new VBox());
		VideoPlayer videoPlayer = new VideoPlayer();
		DateTimeFormatter createdZonedDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z");
		
		// itemVBoxをkey(Item), value(VBox)で管理し、無駄なアップデートを消す
		Runnable updateItemVBox = () -> {
			itemVBox.get().getChildren().clear();
			VBox vbox = currentFolder.get().getElementOfItems();
			itemVBox.get().getChildren().setAll(vbox.getChildren());
		};
		Consumer<FolderItem> setCurrentFolder = (dstFolder) -> {
			currentFolder.set(dstFolder);
			updateItemVBox.run();
			folderPath.setText("Path: " + currentFolder.get().getFolderPath());
		};
		Consumer<Item> moveItemUp = (targetItem) -> {
			currentFolder.get().moveItemUp(targetItem);
		};
		Consumer<Item> moveItemDown = (targetItem) -> {
			currentFolder.get().moveItemDown(targetItem);
		};
		Consumer<Item> renameItem = (targetItem) -> {
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			
			Text text = new Text("New Item Name: ");
			VBox textWrapper = new VBox();
			textWrapper.getChildren().add(text);
			textWrapper.setAlignment(Pos.CENTER);
			textWrapper.setPadding(new Insets(0, 20, 0, 0));
			
			TextField textField = new TextField();
			textField.setText(targetItem.getName());
			HBox.setHgrow(textField, Priority.ALWAYS);
			
			HBox inputField = new HBox();
			inputField.getChildren().add(textWrapper);
			inputField.getChildren().add(textField);
			
			Button cancelButton = new Button("Cancel");
			cancelButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					stage.close();
				}
			});
			Button okButton = new Button("OK");
			okButton.setDefaultButton(true); // push Enter key to do as click okButton
			okButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					if (!textField.getText().equals(targetItem.getName()) && 
						currentFolder.get().hasItemOfDuplicatedName(textField.getText())
					) {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Duplicate Name");
						alert.setContentText("The item named \"" + textField.getText() + "\" is already exist. " +
								"Please input unique name."
								);
						alert.show();
					} else {
						targetItem.setName(textField.getText());
						updateItemVBox.run();
						stage.close();
					}
				}
			});
			
			HBox buttonField = new HBox();
			buttonField.getChildren().add(cancelButton);
			buttonField.getChildren().add(okButton);
			buttonField.setAlignment(Pos.BOTTOM_RIGHT);
			
			VBox vbox = new VBox();
			vbox.getChildren().add(inputField);
			vbox.getChildren().add(buttonField);
			vbox.setPadding(new Insets(30));
			
			stage.setTitle("Rename Item");
			stage.setWidth(500);
			stage.setHeight(150);
			stage.setScene(new Scene(vbox));
			stage.show();
		};
		Consumer<Item> deleteItem = (targetItem) -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete Confirmation");
			
			if (targetItem instanceof FolderItem) {
				alert.setContentText(
						"Folder \"" + targetItem.getName() + "\" has " +
						((FolderItem)targetItem).getTotalFolderItems() + " folders and " +
						((FolderItem)targetItem).getTotalVideoItems() + " videos. " +
						"Are you sure want to delete this folder?"
				);
			}
			if (targetItem instanceof VideoItem) {
				alert.setContentText("Are you sure want to delete this video, \"" + targetItem.getName() + "\" ?");
			}
			
			Optional<ButtonType> result = alert.showAndWait();
			if (!(result.isPresent() && result.get() == ButtonType.OK)) {
				return;
			}
			currentFolder.get().removeItem(targetItem);
		};
		Consumer<VideoItem> setSoleVideoOfVideoPlayer = (videoItem) -> {
			if (videoPlayer.getIsVideoPlayerReady() && !videoPlayer.getIsExist()) {
				videoPlayer.addVideo(videoItem);
				videoPlayer.setCurrentVideoToFirst();
				videoPlayer.createStage();
			}
		};
		
		TextField textField = new TextField();
		textField.setMaxHeight(50);
		
		Button addVideoButton = new Button("Add Video");
		addVideoButton.setMaxWidth(100);
		addVideoButton.setMaxHeight(50);
		addVideoButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String text = textField.getText();
				if (text == "") {
					Alert alert = new Alert(AlertType.INFORMATION);
	                alert.setTitle("Usage");
	                alert.setContentText("Click this button after input your new YouTube videoId");
	                alert.show();
				} else {
					try {
						URL url = new URL("https://www.youtube.com/watch?v=" + text);
						HttpURLConnection connection = (HttpURLConnection)url.openConnection();
						connection.setRequestMethod("GET");
						connection.connect();
						int responseCode = connection.getResponseCode();
						if (responseCode == HttpURLConnection.HTTP_OK) {
							BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				            String inputLine;
				            StringBuilder htmlContent = new StringBuilder();

				            while ((inputLine = in.readLine()) != null) {
				                htmlContent.append(inputLine);
				            }
				            in.close();
				            String html = htmlContent.toString();
				            Pattern pattern = Pattern.compile("<title>(.*?)</title>");
				            Matcher matcher = pattern.matcher(html);
				            String title;
				            if (matcher.find()) {
				            	title = matcher.group(1);
					            title = title.replace("- YouTube", "").trim();
				            } else {
				            	title = text;
				            }
				            
							currentFolder.get().addItem(new VideoItem(title, ZonedDateTime.now(), createdZonedDateTimeFormatter, updateItemVBox, moveItemUp, moveItemDown, renameItem, deleteItem, text, setSoleVideoOfVideoPlayer));
							updateItemVBox.run();
							textField.setText("");
						}
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		Button addFolderButton = new Button("Add Folder");
		addFolderButton.setMaxWidth(100);
		addFolderButton.setMaxHeight(50);
		addFolderButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String text = textField.getText().strip();
				if (text == "") {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Usage");
					alert.setContentText("Click this button after input your new folder name");
					alert.show();
				} else if (SymbolChecker.isFirstCharSymbol(text)) {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Invalid Name");
					alert.setContentText("Do not start the folder name with a symbol");
					alert.show();
				} else {
					boolean isItemAdded = currentFolder.get().addItem(new FolderItem(text, ZonedDateTime.now(), createdZonedDateTimeFormatter, updateItemVBox, moveItemUp, moveItemDown, renameItem, deleteItem, currentFolder.get(), setCurrentFolder));
					if (isItemAdded) {
						setCurrentFolder.accept(currentFolder.get());
						textField.setText("");
					} else {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Invalid Name");
						alert.setContentText("Item \"" + text + "\" is already exist. Input another name");
						alert.show();
					}
				}
			}
		});
		
		HBox inputField = new HBox();
		inputField.setAlignment(Pos.CENTER);
		inputField.getChildren().add(textField);
		inputField.getChildren().add(addVideoButton);
		inputField.getChildren().add(addFolderButton);
		inputField.setPadding(new Insets(30));
		HBox.setHgrow(textField, Priority.ALWAYS);
		
		ObjectMapper mapper = new ObjectMapper();
		File file = new File("data.json");
		if (file.exists()) {
			JsonNode root = mapper.readTree(file);
			if (root == null) {
				rootFolder = new FolderItem("root", ZonedDateTime.now(), createdZonedDateTimeFormatter, updateItemVBox, moveItemUp, moveItemDown, renameItem, deleteItem, null, setCurrentFolder);
			}
			else {
				try {
					rootFolder = new FolderItem(root.get("name").asText(), ZonedDateTime.parse(root.get("createdZonedDateTime").asText(), createdZonedDateTimeFormatter), createdZonedDateTimeFormatter, updateItemVBox, moveItemUp, moveItemDown, renameItem, deleteItem, null, setCurrentFolder);
					rootFolder.setNodeFromJson(root, setSoleVideoOfVideoPlayer);
				} catch (NullPointerException e) {
					rootFolder = new FolderItem("root", ZonedDateTime.now(), createdZonedDateTimeFormatter, updateItemVBox, moveItemUp, moveItemDown, renameItem, deleteItem, null, setCurrentFolder);
				}
			}
		} else {
			rootFolder = new FolderItem("root", ZonedDateTime.now(), createdZonedDateTimeFormatter, updateItemVBox, moveItemUp, moveItemDown, renameItem, deleteItem, null, setCurrentFolder);
		}
		currentFolder.set(rootFolder);
		
		setCurrentFolder.accept(rootFolder);
		
		ScrollPane itemGroupSp = new ScrollPane();
		itemGroupSp.setContent(itemVBox.get());
		itemGroupSp.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		itemGroupSp.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		itemGroupSp.setFitToWidth(true); // enable itemVBox.setAlignment(Pos.TOP_CENTER)
		VBox.setVgrow(itemGroupSp, Priority.ALWAYS);
		
		Button shufflePlayHereButton = new Button("Shuffle Play Here");
		shufflePlayHereButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (videoPlayer.getIsVideoPlayerReady() && !videoPlayer.getIsExist()) {
					currentFolder.get().addAllVideoHereToVideoPlayer(videoPlayer);
					videoPlayer.shuffleVideoList();
					videoPlayer.setCurrentVideoToFirst();
					videoPlayer.createStage();
				}
			}
		});
		Button shufflePlayUnderHereButton = new Button("Shuffle Play Under Here");
		shufflePlayUnderHereButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (videoPlayer.getIsVideoPlayerReady() && !videoPlayer.getIsExist()) {
					currentFolder.get().addAllVideoUnderHereToVideoPlayer(videoPlayer);
					videoPlayer.shuffleVideoList();
					videoPlayer.setCurrentVideoToFirst();
					videoPlayer.createStage();
				}
			}
		});
		HBox playButtonField = new HBox();
		playButtonField.getChildren().add(shufflePlayHereButton);
		playButtonField.getChildren().add(shufflePlayUnderHereButton);
		playButtonField.setAlignment(Pos.CENTER);
		
		VBox vbRoot = new VBox();
		vbRoot.setAlignment(Pos.TOP_LEFT);
		vbRoot.getChildren().add(inputField);
		vbRoot.getChildren().add(folderPath);
		vbRoot.getChildren().add(itemGroupSp);
		vbRoot.getChildren().add(playButtonField);
		
		primaryStage.setTitle("Extended Youtube Playlist");
		primaryStage.setWidth(600);
		primaryStage.setHeight(400);
		primaryStage.setScene(new Scene(vbRoot));
		primaryStage.show();
	}
	
	public static void main(String args[]) {
		launch(args);
	}
	
	@Override
	public void stop() {
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		File file = new File("data.json");
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(mapper.writeValueAsString(this.rootFolder.getObjectNode(mapper)));
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}