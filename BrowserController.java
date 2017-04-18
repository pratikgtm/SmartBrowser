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
import javafx.scene.control.CheckBox;
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
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import javafx.application.*;
import javafx.scene.web.WebHistory;
import javafx.collections.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.lang.*;


/**
 * FXML Controller class
 *
 * @author Pratik
 */
public class BrowserController implements Initializable {
    @FXML
    private TextField txt, tv_username, tv_lastupdate, tv_status;
    @FXML
    private TextArea tv_all_links;
    @FXML
    private Button bt_online, bt_offline, show_caches, bt_fwd, bt_back;
    @FXML
    private AnchorPane anc; 
    @FXML
    private WebView webView;
    @FXML
    private CheckBox incognito_mode;
    
    WebEngine we = null;
    long timeOfQuery = 0;
    Boolean isGlobalData = false;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO        
        we=webView.getEngine();
        we.setJavaScriptEnabled(true);
        
        EventHandler<ActionEvent> enter= new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                setProxyAuth(); 
                isGlobalData = true;
                timeOfQuery = System.currentTimeMillis();               
                we.load(txt.getText());             
            }
        };
        txt.setOnAction(enter);
        tv_username.setText(Constants.USERNAME);
        bt_online.setOnAction(enter);
      
        we.locationProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                txt.setText(newValue);
                tv_lastupdate.setText("");
            }
        });        
        
        we.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>(){

            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if (newValue == Worker.State.SUCCEEDED) {
                    if(isGlobalData){
                        tv_status.setText("Global data, time taken: "+ (System.currentTimeMillis()-timeOfQuery)+"ms");
                        Document doc = we.getDocument();                
                        if(!incognito_mode.isSelected())
                            writeToFile(doc);
                    }
                }
            }         
        });
    }

    private void writeToFile(Document doc) {
        DOMSource source = new DOMSource(doc);
        String fileName = urlToFileName(doc.getDocumentURI());
        
        File dir = new File("caches");
        dir.mkdirs();	
        File tmp = new File(dir, fileName);
        try {
            tmp.createNewFile();
	    } catch (IOException e) {
            System.out.println("IOException "+e); 
        }
        
        FileWriter writer = null;
        try {
            writer = new FileWriter(tmp);
        } catch (IOException ex) {
            Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
        }
        StreamResult result = new StreamResult(writer);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();                  
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // writing to DHT    
        byte[] bytesArray = new byte[(int) tmp.length()];

        try {
            FileInputStream fis = new FileInputStream(tmp);
            try {
                fis.read(bytesArray); //read file into bytes[]
                fis.close();        
            } catch (IOException e) { 
                System.out.println("IOException "+e); 
            }
        } catch (FileNotFoundException e) { 
            System.out.println("FileNotFoundException "+e); 
        }
        System.out.println("putting in DHT : "+doc.getDocumentURI());

        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        Date date = new Date();
        String timestamp = dateFormat.format(date);

        Constants.CURR_OBJ.dht.put(doc.getDocumentURI(), new CustomCache(doc.getDocumentURI(), bytesArray, timestamp));

    }    
    
    private void setProxyAuth() {
        final String authUser = Constants.NET_AUTH_USER;
        final String authPassword = Constants.NET_AUTH_PASSWD;
        Authenticator.setDefault(
            new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                    authUser, authPassword.toCharArray());
                }
            }
        );
        System.setProperty("http.proxyHost",Constants.NET_PROXY_ADDR);
        System.setProperty("http.proxyPort",Constants.NET_PORT_NO);
        System.setProperty("https.proxyHost",Constants.NET_PROXY_ADDR);
        System.setProperty("https.proxyPort",Constants.NET_PORT_NO);
        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);            
    }

    @FXML
    private void handle(ActionEvent event) {}

    private String urlToFileName(String URL) {
        return Key.generate_Base16(URL, Constants.FILE_NAME_LENGTH);
    }

    private static String readFile(String path, Charset encoding) {
        try{
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        } catch(IOException e){
            return "<b>Error while reading local file !!!</b>";
        }
    }

    @FXML
    private void getOfflineData(ActionEvent event) {
        isGlobalData = false;
        timeOfQuery = System.currentTimeMillis();               

        String URL = txt.getText();
        System.out.println("url is "+URL);
        //getting data from local cache...
        File folder = new File("caches");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().equals(Key.generate_Base16(URL, Constants.FILE_NAME_LENGTH))) {
                String content = readFile(listOfFiles[i].getAbsolutePath(), StandardCharsets.UTF_8);
                we.loadContent(content);
                tv_lastupdate.setText("");
                tv_status.setText("local cache data, time taken: "+ (System.currentTimeMillis()-timeOfQuery)+"ms");
                return;
            } 
        }


        //getting data from DHT
        CustomCache recv_cache = (CustomCache)Constants.CURR_OBJ.dht.get(URL);
        byte[] data = recv_cache.data;

        if(null != data) {
            String str = null;
            try {
                str= new String(data, "UTF-8");
            } catch (Exception e) {
                System.out.println(e);
            }
            we.loadContent(str);
            tv_lastupdate.setText(recv_cache.timestamp);
            tv_status.setText("Chord cache data, time taken: " + (System.currentTimeMillis()-timeOfQuery)+"ms");
        } else 
            we.loadContent("<b>NO Cache File found !!!</b>");
    }

    @FXML
    private void showAllCache(ActionEvent event) {
        System.out.println("---> " + Constants.CURR_OBJ.dht.listAll().size());
        tv_all_links.setText("");        
        for(CustomCache curr : Constants.CURR_OBJ.dht.listAll()) {
            System.out.println("* "+curr);
            tv_all_links.appendText(curr.URL+"\n\n");
        }
    }

    @FXML
    private void goBackward(ActionEvent event) {
        final WebHistory history = we.getHistory();
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();

        Platform.runLater(() -> 
        {
            history.go(entryList.size() > 1 && currentIndex > 0 ? -1 : 0); 
        });   
    }

    @FXML
    private void goForward(ActionEvent event) {
        final WebHistory history = we.getHistory();   
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();

        Platform.runLater(() -> 
        {
            history.go(entryList.size() > 1 && currentIndex < entryList.size() - 1 ? 1 : 0); 
        });
    }
}    
    
