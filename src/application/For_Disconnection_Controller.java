package application;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class For_Disconnection_Controller {
	
	Stage stage;
	
    public void navigation(String switchPage, ActionEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(getClass().getResource(switchPage + ".fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(newRoot);
    }
    @FXML
    public void back(ActionEvent event) throws IOException {
        navigation("Homepage", event);
    }
    
}
