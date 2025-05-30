# ExtendedYoutubePlaylist

このアプリケーションを実行するにはjava-sdkが必要です。javafx-sdk-23.0.2で動作することを確認済みです。ダウンロードが完了したらターミナルからカレントディレクトリをこのフォルダまで移動させ、以下のコマンドを実行してください

```bash
javac -cp "lib/*" -d {output folder name here} src/extendedyoutubeplaylist/*.java --module-path {path to your sdk} --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web
```

以上のコマンドによってコンパイルされますが、必要なhtmlファイルは個別にコピーする必要があります。src/extendedyoutubeplaylist/VideoPlayer.htmlを(output folder name)/extendedyoutubeplaylistへとコピーしてください

以上のセットアップが終わったら、以下のコマンドによって実行することができます
```bash
java --module-path {path to your sdk} --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web --add-opens javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED --add-opens javafx.web/com.sun.javafx.webkit=ALL-UNNAMED -cp "lib/*:out" extendedyoutubeplaylist.Main
```
