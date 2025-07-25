# ExtendedYoutubePlaylist

## ※このリポジトリは就職活動用に公開しており、Pull Request・Issueの受付は行っておりません。

　このアプリケーションを実行するにはjava-sdkが必要です。javafx-sdk-23.0.2で動作することを確認済みです。ダウンロードが完了したらターミナルからカレントディレクトリをこのフォルダまで移動させ、以下のコマンドを実行してください。なお、/path/to/your/java-sdkはJavaFX SDKのパスを、{output-folder-name}にはコンパイル先のフォルダの名前を指定してください。

```bash
javac -cp "lib/*" -d {output-folder-name} src/extendedyoutubeplaylist/*.java --module-path /path/to/your/java-sdk --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web
```

　以上のコマンドによってコンパイルされますが、必要なhtmlファイルは個別にコピーする必要があります。src/extendedyoutubeplaylist/VideoPlayer.htmlを{output-folder-name}/extendedyoutubeplaylistへとコピーしてください。

　以上のセットアップが終わったら、(output-folder-name)の親ディレクトリから以下のコマンドによって実行することができます
```bash
java --module-path /path/to/your/java-sdk --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web --add-opens javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED --add-opens javafx.web/com.sun.javafx.webkit=ALL-UNNAMED -cp "lib/*:{output-folder-name}" extendedyoutubeplaylist.Main
```

　始めてアプリケーションを開く時には、画面の上部にテキストボックスとボタンがある状態です。テキストボックスにYouTubeのVideoIDを入力してaddVideoボタンを押すことによって動画を追加することができます。また、テキストボックスに追加したいフォルダ名を入力し、addFolderボタンを押すことによってフォルダの追加を行うことができます。
 
　動画をクリックすることで動画の再生を、フォルダを押すことによってそのフォルダへの移動を行うことができます。フォルダに移動したら初期状態で".."というフォルダが存在していますが、これは前のフォルダに戻るための特殊なフォルダです。
 
　画面下部の"Shuffle Play Here"というボタンを押すと、カレントディレクトリに存在する動画をシャッフル再生することができます。また、"Shuffle Play Under Here"というボタンを押すと、カレントディレクトリから".."以外のフォルダを探索して、存在する動画全てを対象としてシャッフル再生を行います。
