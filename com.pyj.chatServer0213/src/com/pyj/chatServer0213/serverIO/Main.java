package com.pyj.chatServer0213.serverIO;
	
import com.pyj.chatClient0213.serverIO.dao.DBManager;
import com.pyj.chatServer0213.serverIO.event.MainHandler;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
	MainHandler controller;
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pyj/chatServer0213/serverIO/layout/Server.fxml"));
			Parent root = loader.load();
			controller = loader.getController();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/com/pyj/chatServer0213/serverIO/layout/application.css").toExternalForm());
			primaryStage.setTitle("IIO방식 채팅 서버 v1.0");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void stop(){
		controller.stopServer();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
