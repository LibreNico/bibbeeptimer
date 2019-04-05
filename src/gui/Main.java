package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{


        //Instantiating the BorderPane class
        BorderPane bPane = new BorderPane();

        //Setting the top, bottom, center, right and left nodes to the pane
        bPane.setTop(headerOptions());
        bPane.setBottom(bibList());
        //bPane.setLeft(new TextField("Left"));
        //bPane.setRight(new TextField("Right"));

        //TODO
        //https://gist.github.com/SatyaSnehith/167779aac353b4e79f8dfae4ed23cb85
        Label stopwatch = new Label("00:00:00");
        stopwatch.setFont(Font.font ("Verdana", 20));
        bPane.setCenter(stopwatch);

        //Creating a scene object
        Scene scene = new Scene(bPane);

        //Setting title to the Stage
        stage.setTitle("Bip bip timer");

        //Adding scene to the stage
        stage.setScene(scene);

        stage.setMinWidth(450);

        //Displaying the contents of the stage
        stage.show();


    }

    private Node bibList() {

        ScrollPane sp = new ScrollPane();


        ListView<String> list = new ListView<String>();
        ObservableList<String> items = FXCollections.observableArrayList (
                "Single", "Double", "Suite", "Family App", "Loollll" , "Loollll", "Loollll", "Loollll", "Loollll", "Loollll", "Loollll", "Loollll");
        list.setItems(items);

        list.setPrefWidth(100);
        list.setPrefHeight(100);
        return list;
    }

    private Parent headerOptions() {
        //Creating button1
        Button button1 = new Button("Start");

        Image image1 = new Image(getClass().getResourceAsStream("icons/media-play-outline.png"));
        button1.setGraphic(new ImageView(image1));

        //Creating button2
        Button button2 = new Button("Import");
        Image image2 = new Image(getClass().getResourceAsStream("icons/arrow-down.png"));
        button2.setGraphic(new ImageView(image2));

        //Creating button3
        Button button3 = new Button("Export");
        Image image3 = new Image(getClass().getResourceAsStream("icons/location-arrow-outline.png"));
        button3.setGraphic(new ImageView(image3));


        //Creating button4
        Button button5 = new Button("Setting");
        Image image5 = new Image(getClass().getResourceAsStream("icons/cog.png"));
        button5.setGraphic(new ImageView(image5));

        //Creating a Flow Pane
        FlowPane flowPane = new FlowPane();

        //Setting the horizontal gap between the nodes
        flowPane.setHgap(25);

        //Setting the margin of the pane
        flowPane.setMargin(button1, new Insets(20, 0, 20, 20));

        //Retrieving the observable list of the flow Pane
        ObservableList list = flowPane.getChildren();

        //Adding all the nodes to the flow pane
        list.addAll(button1, button2, button3,  button5);

        return flowPane;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
