import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * FXML Controller class
 *
 * @author Pratik
 */
public class FirstScreenController implements Initializable {
    @FXML
    private TextField home_username, home_proxy, home_port, home_bcast, 
            home_auth_username, home_auth_password, home_conn_username, home_conn_ip;
    @FXML
    private TextArea home_dashboard;
    @FXML
    private Button home_connect, home_next;
    @FXML
    private AnchorPane anc;     
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO        
    }
    
    private void setProxyAuth() {
        final String authUser = "edcguest";
        final String authPassword = "edcguest";
        Authenticator.setDefault(
            new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                    authUser, authPassword.toCharArray());
                }
            }
        );
        System.setProperty("http.proxyHost","172.31.102.29");
        System.setProperty("http.proxyPort","3128");
        System.setProperty("https.proxyHost","172.31.102.29");
        System.setProperty("https.proxyPort","3128");
        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);            
    }
    
    @FXML
    private void home_next_action(ActionEvent event) throws IOException {
        Stage stage; 
        Parent root;
        stage=(Stage) home_next.getScene().getWindow();
        //load up OTHER FXML document
        root = FXMLLoader.load(getClass().getResource("browser.fxml"));      
        //create a new scene with root and set the stage
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
    @FXML
    private void home_connect_action(ActionEvent event){
        String username = home_username.getText();
        String proxy_addr = home_proxy.getText();
        String bcast_addr = home_bcast.getText();
        String conn_username = home_conn_username.getText();
        String conn_ip = home_conn_ip.getText();
        String port = home_port.getText();
        String auth_user = home_auth_username.getText();
        String auth_pass = home_auth_password.getText();
        
        Constants.USERNAME = username;
        Constants.NET_PROXY_ADDR = proxy_addr;
        Constants.NET_PORT_NO = port;
        Constants.NET_AUTH_USER = auth_user;
        Constants.NET_AUTH_PASSWD = auth_pass;
        Constants.BROADCAST_ADDR = bcast_addr;
        Constants.CONNECT_USERNAME = conn_username;
        Constants.CONNECT_IP = conn_ip;
                
        Constants.CURR_OBJ = new BrowserUtil(Constants.USERNAME, home_dashboard); 
    }
}
    
