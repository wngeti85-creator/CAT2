package inventory.Controllers;

import inventory.Models.Part;
import inventory.Models.Inventory;
import inventory.Models.Product;
import inventory.Properties;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class AddProductController implements Initializable{
    
    //FXML Labels
    @FXML private Label partErrorLabel;
    //FXML TextFields
    @FXML private TextField partQueryTextField, idTextField, nameTextField, minTextField, maxTextField, 
                            invTextField, priceTextField;
    //FXML TableViews
    @FXML private TableView<Part> partTable, selectedPartTable;
    //FXML partTable Columns
    @FXML private TableColumn<Part,Integer> partTableIdColumn;
    @FXML private TableColumn<Part,String> partTableNameColumn;
    @FXML private TableColumn<Part,Integer> partTableInventoryColumn;
    @FXML private TableColumn<Part,Double> partTablePriceColumn;
    //FXML selectedPartTable Columns
    @FXML private TableColumn<Part,Integer> selectedPartTableIdColumn;
    @FXML private TableColumn<Part,String> selectedPartTableNameColumn;
    @FXML private TableColumn<Part,Integer> selectedPartTableInventoryColumn;
    @FXML private TableColumn<Part,Double> selectedPartTablePriceColumn;
    
    /**
     * @param location
     * @param resources 
     * Usage: Called when the scene is loaded.
     *        Initializes the scene.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        partTableIdColumn.setCellValueFactory(new PropertyValueFactory<Part, Integer>("partId"));
        partTableNameColumn.setCellValueFactory(new PropertyValueFactory<Part, String>("name"));
        partTableInventoryColumn.setCellValueFactory(new PropertyValueFactory<Part, Integer>("inv"));
        partTablePriceColumn.setCellValueFactory(new PropertyValueFactory<Part, Double>("price"));
        
        partTable.getItems().setAll(populatePartsTable());
        //Part table doesn't need its placeholder text set because the user will not be able to access this scene if they have no parts.
        selectedPartTable.setPlaceholder(new Label(Properties.getTABLE_PLACEHOLDER_MESSAGE()));
        idTextField.setText(String.valueOf(Inventory.getProducts().size()+1));
        partErrorLabel.setVisible(false);
    }

    /**
     * @param event
     * @throws IOException if the resources are not available when accessed.
     * Usage: Called when the user presses the cancel button.
     *        If the user confirms, the user's changes are lost.
     */
    @FXML
    public void onCancelButtonPress(ActionEvent event) throws IOException{
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(Properties.getAPPLICATION_NAME_AND_VERSION());
        alert.setHeaderText(Properties.getDIALOG_HEADER());
        alert.setContentText(Properties.getCANCEL_CONFIRMATION_PROMPT());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            goToMainScene(event);
        }
    }
    
    /**
     * @param event 
     * Usage: Called when the user presses the Add button.
     *        If the user has a part selected, it is added to the selected part table.
     */
    @FXML 
    public void onAddButtonPress(ActionEvent event){
        if(partTable.getSelectionModel().getSelectedItem() != null){
            Part selectedPart = partTable.getSelectionModel().getSelectedItem();
            addToSelectedPartsTable(selectedPart);
            
            selectedPartTable.refresh();
        }else{
            partErrorLabel.setText(Properties.getTELL_USER_TO_SELECT_ELEMENT_MESSAGE());
            partErrorLabel.setVisible(true);
        }
    }
    
    /**
     * @param event 
     * Usage: Called when the user presses the delete button.
     *        If the user selected a part, the part is deleted from the selected part table.
     */
    @FXML void onDeleteButtonPress(ActionEvent event){
        if(selectedPartTable.getSelectionModel().getSelectedItem() != null){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(Properties.getAPPLICATION_NAME_AND_VERSION());
            alert.setHeaderText(Properties.getDIALOG_HEADER());
            alert.setContentText(Properties.getDELETE_CONFIRMATION_PROMPT());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                Part selectedPart = selectedPartTable.getSelectionModel().getSelectedItem();           
                partTable.refresh();
                selectedPartTable.getItems().remove(selectedPart);
                selectedPartTable.refresh();
            }
        }else{
            partErrorLabel.setText(Properties.getTELL_USER_TO_SELECT_ELEMENT_MESSAGE());
            partErrorLabel.setVisible(true);
        }
    }
    
    /**
     * @param event
     * @throws IOException if the resources are not available when accessed.
     * Usage: Called when the user presses the save button.
     *        If the entered values are valid, the product is added to the inventory.
     */
    @FXML 
    public void onSaveButtonPress(ActionEvent event) throws IOException{
        
        try{
            if(nameTextField.getText().isEmpty() 
            || invTextField.getText().isEmpty()
            || priceTextField.getText().isEmpty()     
            || minTextField.getText().isEmpty()     
            || maxTextField.getText().isEmpty()){
                partErrorLabel.setText("ERROR: Please populate all fields.");
                partErrorLabel.setVisible(true);
            }else{
                int id = Integer.parseInt(idTextField.getText());
                String name = nameTextField.getText();
                int min = Integer.parseInt(minTextField.getText());
                int max = Integer.parseInt(maxTextField.getText());
                int inv = Integer.parseInt(invTextField.getText());
                double price = Double.parseDouble(priceTextField.getText());
                ObservableList<Part> parts = selectedPartTable.getItems();
        
                if(parts.isEmpty()){
                    partErrorLabel.setText("ERROR: You must associate at least one Part with the Product.");
                    partErrorLabel.setVisible(true); 
                 }else{
                    double totalPartsPrice = 0.00;
                    for(int i = 0; i < parts.size(); i++){
                        totalPartsPrice+=parts.get(i).getPrice();
                    }
                    if(price < totalPartsPrice){
                        partErrorLabel.setText("ERROR: The Product's price must be at least the sum of it's Parts ("+totalPartsPrice+").");
                        partErrorLabel.setVisible(true); 
                    }else{
                        Product newProduct = new Product(id,name,min,max,inv,price,parts);
                        Inventory.addProduct(newProduct);
                        goToMainScene(event);
                    }   
                }
            }
        } catch (Exception e){
            partErrorLabel.setText(Properties.getINVALID_DATA_MESSAGE());
            partErrorLabel.setVisible(true);
        }
    }
    
    /**
     * @return an arraylist of parts.
     * Usage: Populates the part table with parts from the inventory.
     */
    private List<Part> populatePartsTable() {
        ArrayList<Part> list = new ArrayList<Part>();
        for(int i = 0; i < Inventory.getParts().size(); i++){
            list.add(Inventory.getParts().get(i));
        }
        return list;
    }
    
    /**
     * @param part 
     * Usage: Adds the part to the selected parts table.
     */
    private void addToSelectedPartsTable(Part part){
        selectedPartTableIdColumn.setCellValueFactory(new PropertyValueFactory<Part, Integer>("partId"));
        selectedPartTableNameColumn.setCellValueFactory(new PropertyValueFactory<Part, String>("name"));
        selectedPartTableInventoryColumn.setCellValueFactory(new PropertyValueFactory<Part, Integer>("inv"));
        selectedPartTablePriceColumn.setCellValueFactory(new PropertyValueFactory<Part, Double>("price"));
        
        selectedPartTable.getItems().add(part);
    }
    
    /**
     * Usage: Called when the user presses the part search button.
     *        Filters the part table based on the user's query.
     */
    @FXML public void onPartSearchButtonPress(){
        String query = partQueryTextField.getText();
        if(query.isEmpty()){
            partTable.getItems().setAll(populatePartsTable());
        }else{
            ObservableList<Part>  queryMatches =  FXCollections.observableArrayList();
            for(int i = 0; i < Inventory.getParts().size(); i++){
                if(Inventory.getParts().get(i).getName().toLowerCase().contains(query.toLowerCase())){
                    queryMatches.add(Inventory.getParts().get(i));
                }
            }
            partTable.getItems().setAll(queryMatches);
        }
    }
    
    /**
     * @param event
     * @throws IOException if the resources are not available when accessed.
     * Usage: Changes the scene to the main scene.
     */
    private void goToMainScene(ActionEvent event) throws IOException{
        Parent MainParent = FXMLLoader.load(getClass().getResource("/inventory/Views/Main.fxml"));
        Scene MainScene = new Scene(MainParent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(MainScene);
        MainScene.getRoot().requestFocus();
        window.show();
    }
}
