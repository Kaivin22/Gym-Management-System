package com.gym;

import com.gym.config.HibernateConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Load login screen
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        
        Scene scene = new Scene(root, 1200, 800); 
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        
        stage.setTitle("Hệ thống Quản lý Phòng Gym");
        stage.setScene(scene);
        stage.setMinWidth(1024); 
        stage.setMinHeight(768);
        stage.setMaximized(true);
        stage.show();
    }
    
    public static void changeScene(String fxmlFile) throws Exception {
        Parent root = FXMLLoader.load(App.class.getResource("/fxml/" + fxmlFile));
        Scene scene = new Scene(root, 1200, 800); // FIX: Set kích thước
        scene.getStylesheets().add(App.class.getResource("/css/style.css").toExternalForm());
        primaryStage.setScene(scene);
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    @Override
    public void stop() {
        HibernateConfig.shutdown();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}