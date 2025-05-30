package extendedyoutubeplaylist;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

class Utils {
	public static StackPane getSvgStackPane(String content, Color color, double size) {
		double scaleFactor = size / 20;
		SVGPath svg = new SVGPath();
		svg.setContent(content);
		svg.setFill(color);
		svg.setScaleX(scaleFactor);
		svg.setScaleY(scaleFactor);
		StackPane svgStackPane = new StackPane(svg);
		svgStackPane.setMinWidth(size);
		svgStackPane.setMinHeight(size);
		// svgStackPane.setStyle("-fx-border-color: #FF0000"); // StackPaneの大きさ確認用
		svgStackPane.setAlignment(Pos.CENTER);
		
		return svgStackPane;
	}
	public static Button getAlignedButton(String content, Color color, double size, String text, int textSize) {
		StackPane stackPane = Utils.getSvgStackPane(content, color, size);
		
		Text buttonText = new Text(text);
		buttonText.setFont(Font.font(textSize));
		
		VBox vbox = new VBox();
		vbox.getChildren().add(stackPane);
		vbox.getChildren().add(buttonText);
		vbox.setAlignment(Pos.CENTER);
		
		Button button = new Button();
		button.setGraphic(vbox);
		
		return button;
	}
};