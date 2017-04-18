import java.net.Authenticator;
import java.net.PasswordAuthentication;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;

/**
 *
 * @author pratik
 */
public class NewBrowser extends Application {
    private Stage stage;
    
    @Override
    public void start(Stage stage) throws Exception {
        this.stage=stage;
        Parent root = FXMLLoader.load(getClass().getResource("first_screen.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {   
            public void handle(WindowEvent we) {
                if(null != Constants.CURR_OBJ) {
                    Constants.CURR_OBJ.dht.leave();        
                    Constants.CURR_OBJ.peerServer.stop();
                    System.out.println("bye bye");
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
