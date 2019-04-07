package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import racetimer.RaceUtil;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Main extends Application {

    public static final String INIT_STOPWACHT = "00:00:00";
    public static final String APP_TITLE = "BibBeep - race timing system";
    private static final String COPYLEFT = "Bib scanning and timing software made by the Joggans club";
    private int mins = 0;
    private int secs = 0;
    private int millis = 0;
    private Label stopwatchLabel;
    private Timeline timeline;
    private final FileChooser fileChooser = new FileChooser();
    private static StringBuilder inputKeyBordBuffer = new StringBuilder();
    private ListView<Object> listFinisher;
    Set<String> alreadyBeeped = new HashSet<>();
    private boolean hasRaceStarted;


    @Override
    public void start(Stage stage)  {


        //Instantiating the VBox class
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(5);

        //retrieving the observable list of the VBox
        ObservableList list = vBox.getChildren();

        //Adding all the nodes to the observable list
        list.addAll(createHeaderOptions(stage), createStopWatch());

        BorderPane bPane = new BorderPane();
        bPane.setTop(vBox);
        bPane.setBottom(createAbout());
        //bPane.setLeft(new TextField("Left"));
        //bPane.setRight(new TextField("Right"));
        bPane.setCenter(createFinisherList());


        //Creating a scene object
        Scene scene = new Scene(bPane);

        handelKeyBord(scene);

        //Setting title to the Stage
        stage.setTitle(APP_TITLE);

        //Adding scene to the stage
        stage.setScene(scene);

        stage.setMinWidth(450);

        //Displaying the contents of the stage
        stage.show();


    }

    private Node createAbout() {
        HBox aboutBar = new HBox();
        /*ImageView imageCopyleft = new ImageView(new Image(getClass().getResourceAsStream("icons/copyleft.png")));
        imageCopyleft.setFitHeight(12);
        imageCopyleft.setFitWidth(12);*/

        Label copyleft = new Label(COPYLEFT);
        aboutBar.setSpacing(5);
        aboutBar.getChildren().addAll(/*imageCopyleft,*/ copyleft);
        return aboutBar;
    }

    private void handelKeyBord(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {

            if (hasRaceStarted) {

                if (key.getCode() == KeyCode.ENTER) {
                    String inputKeys = inputKeyBordBuffer.toString();

                    if (!RaceUtil.isNumeric(inputKeys)) {
                        RaceUtil.printError("Scanned input " + inputKeys + " is not numeric.");
                    } else if (alreadyBeeped.contains(inputKeys)) {
                        RaceUtil.printError("Scanned input " + inputKeys + " already scan.");
                    } else {
                        listFinisher.getItems().add(inputKeyBordBuffer + " - " + stopwatchLabel.getText());
                        alreadyBeeped.add(inputKeys);
                        RaceUtil.printInfo("Bib scan: " + inputKeyBordBuffer + ";" + stopwatchLabel.getText());
                        RaceUtil.tone(1000, 100);
                    }

                    inputKeyBordBuffer.setLength(0);


                } else {
                    inputKeyBordBuffer.append(key.getText());
                }

            }
        });
    }

    private Label createStopWatch() {
        //https://gist.github.com/SatyaSnehith/167779aac353b4e79f8dfae4ed23cb85
        stopwatchLabel = new Label(INIT_STOPWACHT);
        stopwatchLabel.setFont(Font.font("Verdana", 35));
        timeline = new Timeline(new KeyFrame(Duration.millis(1), event -> change(stopwatchLabel)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(false);
        return stopwatchLabel;
    }

    void change(Label text) {
        if (millis == 1000) {
            secs++;
            millis = 0;
        }
        if (secs == 60) {
            mins++;
            secs = 0;
        }
        text.setText((((mins / 10) == 0) ? "0" : "") + mins + ":"
                + (((secs / 10) == 0) ? "0" : "") + secs + ":"
                + (((millis / 10) == 0) ? "00" : (((millis / 100) == 0) ? "0" : "")) + millis++);
    }


    private Node createFinisherList() {
        listFinisher = new ListView<>();
      /*  ObservableList<String> items = FXCollections.observableArrayList (
                "Single", "Double", "Suite", "Family App", "Loollll" , "Loollll", "Loollll", "Loollll", "Loollll", "Loollll", "Loollll", "Loollll");
        listFinisher.setItems(items);*/
        listFinisher.setPrefWidth(100);
        listFinisher.setPrefHeight(100);
        return listFinisher;
    }

    private Parent createHeaderOptions(Stage stage) {
        //Creating button1
        Button buttonStartStop = new Button("Start");

        ImageView imageStart = new ImageView(new Image(getClass().getResourceAsStream("icons/media-play-outline.png")));
        ImageView imageStop = new ImageView(new Image(getClass().getResourceAsStream("icons/media-stop-outline.png")));
        buttonStartStop.setGraphic(imageStart);


        buttonStartStop.setOnAction(e -> {
            if (buttonStartStop.getText().equals("Start")) {
                hasRaceStarted = true;
                buttonStartStop.setText("Stop");
                buttonStartStop.setGraphic(imageStop);
                timeline.play();
            } else {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText("Stop the race");
                alert.setContentText("Are you sure you want to stop the timing?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    hasRaceStarted = false;
                    buttonStartStop.setText("Start");
                    buttonStartStop.setGraphic(imageStart);
                    timeline.pause();
                    stopwatchLabel.setText(INIT_STOPWACHT);
                    alreadyBeeped.clear();
                }

            }
        });


        //Creating button2
        Button buttonAddRunners = new Button("Runners");
        Image image2 = new Image(getClass().getResourceAsStream("icons/plus-outline.png"));
        buttonAddRunners.setGraphic(new ImageView(image2));


        buttonAddRunners.setOnAction(
                e -> {
                    configureFileChooser(fileChooser);
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information Dialog");
                        alert.setHeaderText(null);
                        alert.setContentText("I have a great message for you! " + file.getAbsolutePath());

                        alert.showAndWait();
                    }
                });

        //Creating button3
        Button button3 = new Button("Report");
        Image image3 = new Image(getClass().getResourceAsStream("icons/printer.png"));
        button3.setGraphic(new ImageView(image3));


        //Creating button4
        Button buttonBackup = new Button("Backup");
        Image image5 = new Image(getClass().getResourceAsStream("icons/folder.png"));
        buttonBackup.setGraphic(new ImageView(image5));

        //Creating a Flow Pane
        FlowPane flowPane = new FlowPane();

        //Setting the horizontal gap between the nodes
        flowPane.setHgap(25);

        //Setting the margin of the pane
        flowPane.setMargin(buttonStartStop, new Insets(10, 0, 10, 10));

        //Retrieving the observable list of the flow Pane
        ObservableList list = flowPane.getChildren();

        //Adding all the nodes to the flow pane
        list.addAll(buttonStartStop, buttonAddRunners, button3, buttonBackup);

        return flowPane;
    }

    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("View Pictures");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
    }


    public static void main(String[] args) {
        launch(args);
    }
}
