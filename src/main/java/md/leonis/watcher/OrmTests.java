package md.leonis.watcher;

import com.iciql.Db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.stage.Stage;
import md.leonis.watcher.domain.PageText;
import md.leonis.watcher.util.Config;
import md.leonis.watcher.util.JavaFxUtils;

public class OrmTests extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        https://www.jooq.org/download/
        https://mvnrepository.com/artifact/org.jooq

        https://stackoverflow.com/questions/452385/what-java-orm-do-you-prefer-and-why



        Config.loadProperties();

        http://iciql.com/
        iciql();

        http://ormlite.com/

        rawJdbc();
        //Config.loadProtectedProperties();
        JavaFxUtils.showMainPane(primaryStage);
    }

    private void iciql() {
        try (Db db = Db.open("jdbc:h2:mem:iciql")) {
            List<PageText> pageTextList = new ArrayList<>();
            pageTextList.add(new PageText("a", "b"));
            pageTextList.add(new PageText("as", "bd"));
            db.insertAll(pageTextList);
            PageText p = new PageText();
            List<PageText> restock = db.from(p).where(p.getLeft()).is("a").select();
            List<PageText> all = db.executeQuery(PageText.class, "select * from pagetext");
            List<String> names = db.from(p).selectDistinct(p.getLeft());
            System.out.println("i");
        }


    }

    private void rawJdbc() {
        try {
            Class.forName("org.h2.Driver");
            Connection con = DriverManager.getConnection("jdbc:h2:~/test", "sa", "" );
            Statement stmt = con.createStatement();
            //stmt.executeUpdate( "DROP TABLE table1" );
            stmt.executeUpdate( "CREATE TABLE IF NOT EXISTS table1 ( user varchar(50) )" );
            stmt.executeUpdate( "INSERT INTO table1 ( user ) VALUES ( 'Claudio' )" );
            stmt.executeUpdate( "INSERT INTO table1 ( user ) VALUES ( 'Bernasconi' )" );

            ResultSet rs = stmt.executeQuery("SELECT * FROM table1");
            while( rs.next() )
            {
                String name = rs.getString("user");
                System.out.println( name );
            }
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}