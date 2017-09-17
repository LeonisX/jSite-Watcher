import domain.DiffStatus;
import domain.TextDiff;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import util.DiffUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static org.jsoup.helper.StringUtil.isBlank;

public class MainTableView extends Application {

    private static final ClassLoader classLoader = MainTableView.class.getClassLoader();

    private Label label = new Label();

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Table View Sample");

        Document doc = Jsoup.parse(new File(classLoader.getResource("page.html").getFile()), null);

        TableView<TextDiff> table = new TableView<>();

        //table.setSelectionModel(null);

        // Create column UserName (Data type of String).
        TableColumn<TextDiff, String> leftCol = new TableColumn<>("Old page (date...)");
        leftCol.prefWidthProperty().bind(table.widthProperty().divide(2));
        leftCol.setId("leftCol");
        leftCol.setCellFactory(cellFactory());

        //TODO show diff in text (in future)
        // Create column Email (Data type of String).
        TableColumn<TextDiff, String> rightCol = new TableColumn<>("New page (date...)");
        rightCol.prefWidthProperty().bind(table.widthProperty().divide(2));
        rightCol.setId("rightCol");
        rightCol.setCellFactory(cellFactory());

        leftCol.setCellValueFactory(new PropertyValueFactory<>("leftText"));
        rightCol.setCellValueFactory(new PropertyValueFactory<>("rightText"));

        Collection<String> leftList = walkTree(doc.body(), new ArrayList<>());

        //List<PageText> textList = mergeLists(leftList, leftList);

        List<String> left = Arrays.asList("aaa", "bbb", "ccc", "diff2", "When adding text, you can also set some of its properties.", "sea", "cae", "eee", "changed");
        List<String> right = Arrays.asList("000", "111", "aaa", "ccc", "diff", "When adding text, you can also set some of its", "ddd", "added1", "added2", "eee", "change");

        Map<Integer, String> leftMap = toIndexedMap(left);
        Map<Integer, String> rightMap = toIndexedMap(right);

        List<TextDiff> textList = DiffUtils.diff(leftMap, rightMap);

        // Display row data
        ObservableList<TextDiff> list = FXCollections.observableList(textList);

        table.setItems(list);

        table.getColumns().add(leftCol);
        table.getColumns().add(rightCol);

        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setBottom(label);
        primaryStage.setScene(new Scene(root, 600, 650));
        primaryStage.show();
    }

    private static Callback<TableColumn<TextDiff, String>, TableCell<TextDiff, String>> cellFactory() {
        return new Callback<TableColumn<TextDiff, String>, TableCell<TextDiff, String>>() {
            @Override
            public TableCell<TextDiff, String> call(TableColumn<TextDiff, String> param) {
                return new TableCell<TextDiff, String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        Text text = new Text();
                        text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
                        text.textProperty().bind(itemProperty());

                        setPrefHeight(Control.USE_COMPUTED_SIZE);
                        setGraphic(text);

                        if (getTableRow().getIndex() >= 0 && getTableRow().getIndex() < getTableView().getItems().size()) {
                            if (getTableView().getItems().get(getTableRow().getIndex()).getStatus() == DiffStatus.ADDED) {
                                //this.setStyle("-fx-background-color: rgb(200,255,200);");
                                //this.setStyle("-fx-text-fill: red;");
                                text.setFill(Color.GREEN);
                                this.setStyle("-fx-font-weight: bold; -fx-background-color: #F0FFF0;");
                            }
                            if (getTableView().getItems().get(getTableRow().getIndex()).getStatus() == DiffStatus.CHANGED) {
                                text.setFill(Color.BLUE);
                                this.setStyle("-fx-font-weight: bold; -fx-background-color: #F0F0FF;");
                            }
                            if (getTableView().getItems().get(getTableRow().getIndex()).getStatus() == DiffStatus.DELETED) {
                                text.setFill(Color.RED);
                                text.setStrikethrough(true);
                                this.setStyle("-fx-font-weight: bold; -fx-background-color: #FFF0F0;");
                            }
                            //this.setBackground();
                            //this.setStyle("-fx-background-color: #00FFFF;");
                            this.setText(empty ? "" : getItem());
                        }
                    }
                };
            }
        };
    }


    private static Map<Integer, String> toIndexedMap(List<String> stringList) {
        return IntStream.range(0, stringList.size())
                .boxed()
                .collect(toMap(idx -> idx, stringList::get));
    }

/*    private List<PageText> mergeLists(Collection<String> leftList, Collection<String> rightList) {
        return leftList.stream().map(s -> new PageText(s, s)).collect(Collectors.toList());
    }*/


    private static Collection<String> walkTree(Element element, Collection<String> collection) {
        if (!isBlank(element.ownText())) {
            collection.add(element.ownText());
        }
        element.children().forEach(e -> walkTree(e, collection));
        return collection;
    }

}
