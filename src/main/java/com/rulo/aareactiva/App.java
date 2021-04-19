package com.rulo.aareactiva;

import com.rulo.aareactiva.controller.AppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.rulo.aareactiva.util.R;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(R.getUI("main.fxml"));
        loader.setController(new AppController());
        VBox padre = loader.load();
        Scene scene = new Scene(padre);
        stage.setScene(scene);
        stage.setTitle("Title");
        stage.setResizable(false);
        stage.show();
    }
}
