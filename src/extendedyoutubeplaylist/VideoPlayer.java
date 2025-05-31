package extendedyoutubeplaylist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Pair;
import netscape.javascript.JSObject;

public class VideoPlayer {
	private int videoWidth = 480;
	private int videoHeight = 270;
	private int videoPadding = 20;
	private boolean isExist;
	private boolean isPlaying;
	private boolean isLooping;
	private int videoIterator;
	private VideoItem currentVideo;
	private WebView webView;
	private ArrayList<Pair<VideoItem, HBox>> videoMap = new ArrayList<>();
	private Text title = new Text();
	private boolean isVideoPlayerReady;
	
	VideoPlayer() {
		this.isExist = false;
		this.isPlaying = false;
		this.videoIterator = 0;
		this.webView = new WebView();
		this.webView.setMouseTransparent(true);
		this.webView.getEngine().setOnError(e -> System.out.println(e.getMessage()));
		this.webView.getEngine().setOnAlert(e -> System.out.println(e.getData()));
		
		this.isVideoPlayerReady = false;
		
		Callback callback = new Callback(this);
		this.webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
			if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
				JSObject win = (JSObject)this.webView.getEngine().executeScript("window");
				win.setMember("callback", callback);
				this.isVideoPlayerReady = (boolean) this.webView.getEngine().executeScript("getIsYouTubeIframeAPIReady();");
			}
		});
		String url = getClass().getResource("VideoPlayer.html").toExternalForm();
		this.webView.getEngine().load(url);
		this.webView.setMaxHeight(this.videoHeight + 2 * this.videoPadding);
		
		this.isLooping = false;
	}
	
	private HBox getButtonField() {
		Button previousButton = this.getPreviousButton();
		Button skipStartButton = this.getSkipStartButton();
		Button playButton = this.getPlayButton();
		Button skipEndButton = this.getSkipEndButton();
		Button nextButton = this.getNextButton();
		Button loopButton = this.getLoopButton();
		
		HBox hbox = new HBox();
		hbox.getChildren().add(previousButton);
		hbox.getChildren().add(skipStartButton);
		hbox.getChildren().add(playButton);
		hbox.getChildren().add(skipEndButton);
		hbox.getChildren().add(nextButton);
		hbox.getChildren().add(loopButton);
		
		return hbox;
	}
	private Button getPreviousButton() {
		String svgContent = "M.5 3.5A.5.5 0 0 0 0 4v8a.5.5 0 0 0 1 0V8.753l6.267 3.636c.54.313 1.233-.066 1.233-.697v-2.94l6.267 3.636c.54.314 1.233-.065 1.233-.696V4.308c0-.63-.693-1.01-1.233-.696L8.5 7.248v-2.94c0-.63-.692-1.01-1.233-.696L1 7.248V4a.5.5 0 0 0-.5-.5";
		
		Button button = Utils.getAlignedButton(svgContent, Color.BLACK, 20, "Previous", 8);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				int nextIterator = (VideoPlayer.this.videoIterator + VideoPlayer.this.videoMap.size() - 1) % VideoPlayer.this.videoMap.size();
				VideoPlayer.this.setCurrentVideo(VideoPlayer.this.videoMap.get(nextIterator).getKey());
			}
		});
		
		return button;
	}
	private Button getSkipStartButton() {
		String svgContent = "M4 4a.5.5 0 0 1 1 0v3.248l6.267-3.636c.54-.313 1.232.066 1.232.696v7.384c0 .63-.692 1.01-1.232.697L5 8.753V12a.5.5 0 0 1-1 0z";
		
		Button button = Utils.getAlignedButton(svgContent, Color.BLACK, 20, "Back", 8);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				VideoPlayer.this.webView.getEngine().executeScript("skipStart(" + VideoPlayer.this.isPlaying + ")");
			}
		});
		
		return button;
	}
	private Button getPlayButton() {
		String svgPlayContent = "m11.596 8.697-6.363 3.692c-.54.313-1.233-.066-1.233-.697V4.308c0-.63.692-1.01 1.233-.696l6.363 3.692a.802.802 0 0 1 0 1.393";
		String svgPauseContent = "M5.5 3.5A1.5 1.5 0 0 1 7 5v6a1.5 1.5 0 0 1-3 0V5a1.5 1.5 0 0 1 1.5-1.5m5 0A1.5 1.5 0 0 1 12 5v6a1.5 1.5 0 0 1-3 0V5a1.5 1.5 0 0 1 1.5-1.5";
		
		Button button = Utils.getAlignedButton(svgPlayContent, Color.RED, 20, "Play", 8);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Button newButton;
				if (VideoPlayer.this.isPlaying) {
					VideoPlayer.this.webView.getEngine().executeScript("pauseVideo()");
					VideoPlayer.this.isPlaying = false;
					newButton = Utils.getAlignedButton(svgPlayContent, Color.RED, 20, "Play", 8);
				} else {
					VideoPlayer.this.webView.getEngine().executeScript("playVideo()");
					VideoPlayer.this.isPlaying = true;
					newButton = Utils.getAlignedButton(svgPauseContent, Color.RED, 20, "Pause", 8);
				}
				button.setGraphic(newButton.getGraphic());
			}
		});
		
		return button;
	}
	private Button getSkipEndButton() {
		String svgContent = "M12.5 4a.5.5 0 0 0-1 0v3.248L5.233 3.612C4.693 3.3 4 3.678 4 4.308v7.384c0 .63.692 1.01 1.233.697L11.5 8.753V12a.5.5 0 0 0 1 0z";
		
		Button button = Utils.getAlignedButton(svgContent, Color.BLACK, 20, "Forward", 8);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				VideoPlayer.this.webView.getEngine().executeScript("skipEnd(" + VideoPlayer.this.isPlaying + ")");
			}
		});
		
		return button;
	}
	private Button getNextButton() {
		String svgContent = "M15.5 3.5a.5.5 0 0 1 .5.5v8a.5.5 0 0 1-1 0V8.753l-6.267 3.636c-.54.313-1.233-.066-1.233-.697v-2.94l-6.267 3.636C.693 12.703 0 12.324 0 11.693V4.308c0-.63.693-1.01 1.233-.696L7.5 7.248v-2.94c0-.63.693-1.01 1.233-.696L15 7.248V4a.5.5 0 0 1 .5-.5";
		
		Button button = Utils.getAlignedButton(svgContent, Color.BLACK, 20, "Next", 8);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				VideoPlayer.this.setCurrentVideoToNextVideo();
			}
		});
		
		return button;
	}
	private Button getLoopButton() {
		String svgContent = "M11.534 7h3.932a.25.25 0 0 1 .192.41l-1.966 2.36a.25.25 0 0 1-.384 0l-1.966-2.36a.25.25 0 0 1 .192-.41m-11 2h3.932a.25.25 0 0 0 .192-.41L2.692 6.23a.25.25 0 0 0-.384 0L.342 8.59A.25.25 0 0 0 .534 9 M8 3c-1.552 0-2.94.707-3.857 1.818a.5.5 0 1 1-.771-.636A6.002 6.002 0 0 1 13.917 7H12.9A5 5 0 0 0 8 3M3.1 9a5.002 5.002 0 0 0 8.757 2.182.5.5 0 1 1 .771.636A6.002 6.002 0 0 1 2.083 9z";
		
		Button button = Utils.getAlignedButton(svgContent, Color.BLACK, 20, "Loop", 8);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				VideoPlayer.this.isLooping = !VideoPlayer.this.isLooping;
				VideoPlayer.this.webView.getEngine().executeScript("setIsLooping(" + VideoPlayer.this.isLooping + ")");
				if (VideoPlayer.this.isLooping) button.setStyle("-fx-background-color: #AAAAFF");
				else button.setStyle("");
			}
		});
		
		return button;
	}
	private void videoListVBoxUpdate() {
		for (Pair<VideoItem, HBox> p : this.videoMap) {
			VideoItem v = p.getKey();
			HBox hbox = p.getValue();
			if (v.equals(this.currentVideo)) hbox.setStyle("-fx-background-color: #AAAAFF");
			else hbox.setStyle(null);
		}
	}
	private void titleUpdate() {
		this.title.setText(this.currentVideo.getName());
		title.setFont(Font.font(24));
	}
	
	public boolean getIsExist() {
		return this.isExist;
	}
	public void addVideo(VideoItem video) {
		Consumer<VideoItem> onClicked = (v) -> {
			this.setCurrentVideo(v);
		};
		HBox hbox = video.getElement(false, onClicked);
		Pair<VideoItem, HBox> pair = new Pair<>(video, hbox);
		this.videoMap.add(pair);
	}
	public void setCurrentVideo(VideoItem video) {
		this.webView.getEngine().executeScript("setVideo('" + video.getVideoId() + "', " + this.videoWidth + ", " + this.videoHeight + "," + this.isPlaying +  ");");
		this.currentVideo = video;
		
		int index = 0;
		for (Pair<VideoItem, HBox> p : this.videoMap) {
			if (p.getKey().equals(video)) break;
			index++;
		}
		this.videoIterator = index;
		
		this.titleUpdate();
		this.videoListVBoxUpdate();
	}
	public void setCurrentVideoToFirst() {
		this.setCurrentVideo(this.videoMap.get(0).getKey());
	}
	public void setCurrentVideoToNextVideo() {
		int nextIterator = (VideoPlayer.this.videoIterator + 1) % VideoPlayer.this.videoMap.size();
		this.setCurrentVideo(VideoPlayer.this.videoMap.get(nextIterator).getKey());
	}
	public void onVideoLoaded() {
		if (this.isPlaying) this.webView.getEngine().executeScript("playVideo()");
	}
	public void shuffleVideoList() {
		Collections.shuffle(this.videoMap);
	}
	
	public void createStage() {
		if (this.isExist == true) return;
		
		int videoFieldWidth = this.videoWidth + 2 * this.videoPadding;
		
		this.webView.setPrefWidth(videoFieldWidth);
		
		HBox buttonField = this.getButtonField();
		buttonField.setAlignment(Pos.CENTER);
	
		this.title.setWrappingWidth(videoFieldWidth);
		this.titleUpdate();
		
		VBox videoInfoField = new VBox();
		videoInfoField.getChildren().add(this.webView);
		videoInfoField.getChildren().add(buttonField);
		videoInfoField.getChildren().add(this.title);
		
		VBox videoListVBox = new VBox();
		this.videoListVBoxUpdate();
		for (Pair<VideoItem, HBox> p : this.videoMap) {
			videoListVBox.getChildren().add(p.getValue());
		}
		
		ScrollPane videoListScrollPane = new ScrollPane();
		videoListScrollPane.setContent(videoListVBox);
		videoListScrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		videoListScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		
		HBox hbox = new HBox();
		hbox.getChildren().add(videoInfoField);
		hbox.getChildren().add(videoListScrollPane);
		hbox.setMinWidth(800);
		hbox.setMinHeight(400);
		
		Stage stage = new Stage();
		stage.setScene(new Scene(hbox));
		stage.setOnCloseRequest(event -> {
			this.isExist = false;
			this.videoMap.clear();
		});
		
		stage.show();
		if (!this.isVideoPlayerReady) stage.close();
		this.isExist = true;
	}
	public boolean getIsVideoPlayerReady() {
		return this.isVideoPlayerReady;
	}
	public static class Callback {
		private VideoPlayer videoPlayer;
		Callback(VideoPlayer videoPlayer) {
			this.videoPlayer = videoPlayer;
		}
		public void setCurrentVideoToNextVideo() {
			this.videoPlayer.setCurrentVideoToNextVideo();
		}
		public void onVideoLoaded() {
			this.videoPlayer.onVideoLoaded();
		}
		public void onYouTubeIframeAPIReady() {
			System.out.println("here");
		}
	}
}