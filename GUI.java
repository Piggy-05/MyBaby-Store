
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.collections.*;
import java.util.*;

public class GUI extends Application {
    //导入功能
    private InventoryManager manager =  new InventoryManager();

    @Override
    public void start(Stage primaryStage) throws Exception{

        manager.loadFromFile();

        //头页分类按钮&排列
        VBox dashboard = new VBox(30);
        dashboard.setPadding(new Insets(50));
        dashboard.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Baby Store");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold");

        //商品种类选择
        Button diaperBtn = new Button("Diaper");
        Button milkBtn = new Button("Milk Powder");
        Button toyBtn = new Button("Toy");

        String btnStyle = "-fx-font-size: 16px; -fx-min-width: 250px; -fx-min-height: 60px; -fx-cursor: hand;";
        diaperBtn.setStyle(btnStyle);
        milkBtn.setStyle(btnStyle);
        toyBtn.setStyle(btnStyle);

        //库存提醒功能
        Label alertTitle = new Label("Low Stock Warning!");
        alertTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red;");

        ListView<String> listView = new ListView<>();

        //限制窗口展示大小
        listView.setPrefHeight(150);
        listView.setMaxWidth(400);

        Runnable refreshAlert = ()->{
            listView.getItems().clear();

            boolean lowStock = false;

            //找低库存商品
            for(Product p : manager.getInventoryList()){
                if(p.getStockQuantity() < 10){
                    String type = p.getClass().getSimpleName();
                    String msg = String.format("[%s] ID: %d - %s (Only:%d)", type, p.getProductID(), p.getName(), p.getStockQuantity());
                    listView.getItems().add(msg);
                    lowStock = true;
                }
            }
            if(!lowStock){
                listView.getItems().add("Stock Quantity Enough");
            }
        };

        refreshAlert.run();

        diaperBtn.setOnAction(e -> {
            openDiaper(primaryStage);
            refreshAlert.run();
        });

        milkBtn.setOnAction(e -> {
            openMilk(primaryStage);
            refreshAlert.run();
        });

        toyBtn.setOnAction(e -> {
            openToy(primaryStage);
            refreshAlert.run();
        });

        dashboard.getChildren().addAll(titleLabel, diaperBtn, milkBtn, toyBtn);
        dashboard.getChildren().addAll(alertTitle, listView);

        //POS界面
        VBox posLayout = new VBox(15);
        posLayout.setPadding(new Insets(20));

        Label posTitle = new Label("Cashier System");
        posTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold");

        //制造接收框&结账按钮
        HBox posInput = new HBox(10);
        posInput.setAlignment(Pos.CENTER_LEFT);

        TextField posIdInput = new TextField();
        posIdInput.setPromptText("Enter Product ID");
        posIdInput.setPrefWidth(120);

        TextField posQtyInput = new TextField();
        posQtyInput.setPromptText("Enter Product Quantity");
        posQtyInput.setPrefWidth(135);

        ComboBox<String> paymentMethodBox = new ComboBox<>();
        paymentMethodBox.getItems().addAll("Cash", "Credit Card");
        paymentMethodBox.setValue("Cash");

        Button billBtn = new Button("Process Sale");
        billBtn.setStyle("-fx-background-color: #2ab7ca; -fx-text-fill: white; -fx-font-weight: bold;");

        posInput.getChildren().addAll(
                new Label("ID: "),posIdInput,
                new Label("Quantity: "),posQtyInput,
                new Label("Payment: "), paymentMethodBox,
                billBtn
        );

        //制造账单展示区域
        TextArea receiptArea = new TextArea();
        receiptArea.setEditable(false);
        receiptArea.setPrefHeight(300);
        receiptArea.setStyle("-fx-font-family: monospace;");
        receiptArea.setPromptText("[Receipt]");

        posLayout.getChildren().addAll(posTitle,posInput,receiptArea);

        //结账功能
        billBtn.setOnAction(billE->{
            try{
                int buyId = Integer.parseInt(posIdInput.getText().trim());
                int buyQty = Integer.parseInt(posQtyInput.getText().trim());
                String paymentType = paymentMethodBox.getValue();

                String resultMessage = manager.saleProduct(buyId, buyQty, paymentType);

                receiptArea.appendText(resultMessage);

                if (!resultMessage.startsWith("ERROR")) {
                    posIdInput.clear();
                    posQtyInput.clear();
                    refreshAlert.run();
                }

            }catch(NumberFormatException ex){
                receiptArea.appendText("Please enter a valid number for [product ID/quantity]\n");
            }
        });

        //制造分支页面
        TabPane rootTabPane = new TabPane();

        //第一分支页面
        Tab inventoryTab = new Tab("Inventory", dashboard);
        inventoryTab.setClosable(false);

        //第二分支页面
        Tab posTab = new Tab("Point of Sales (POS)", posLayout);
        posTab.setClosable(false);

        //分支合成
        rootTabPane.getTabs().addAll(inventoryTab, posTab);

        //制造头页
        Scene fScene = new Scene(rootTabPane,680,600);
        primaryStage.setScene(fScene);
        primaryStage.setTitle("Baby Store Management System");
        primaryStage.show();
    }

    private void openDiaper(Stage parentStage){
        Stage diaperStage = new Stage();
        diaperStage.setTitle("Diaper Management");

        //建表
        TableView<Diaper> table = new TableView<>();

        //表格列
        setupCommonColumns(table);

        TableColumn<Diaper, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));

        TableColumn<Diaper, String> styleCol = new TableColumn<>("Style");
        styleCol.setCellValueFactory(new PropertyValueFactory<>("style"));

        table.getColumns().addAll(sizeCol, styleCol);

        //获取数据
        ObservableList<Diaper> diaperData = FXCollections.observableArrayList();
        for (Product p : manager.getInventoryList()) {
            if (p instanceof Diaper) {
                diaperData.add((Diaper) p);
            }
        }
        table.setItems(diaperData);

        //功能按钮
        Button addBtn = new Button("Add Product");
        Button delBtn = new Button("Delete Product");
        Button updBtn = new Button("Update Product");

        delBtn.setOnAction(e -> {
            deleteProduct(table, diaperData);
        });

        updBtn.setOnAction(e -> {
            openUpdateDialog(table.getSelectionModel().getSelectedItem(), table);
        });

        //制造搜索框&按钮
        TextField searchInput = new TextField();
        searchInput.setPromptText("Search product...");
        Button searchBtn = new Button("Search");
        Button resetBtn = new Button("Reset");

        HBox searchBox = new HBox(10, searchInput, searchBtn, resetBtn);

        //搜索逻辑
        searchBtn.setOnAction(e -> {
            String keyword = searchInput.getText().trim().toLowerCase();
            if (keyword.isEmpty()) {
                table.setItems(diaperData);
            } else {
                ObservableList<Diaper> filtered = FXCollections.observableArrayList();

                if(keyword.matches("\\d+")) {
                    int searchID = Integer.parseInt(keyword);
                    Product p = manager.searchProductById(searchID);

                    if (p != null && p instanceof Diaper) {
                        filtered.add((Diaper) p);
                    }
                }else {
                    ArrayList<Product> searchResults = manager.searchProductByName(keyword);
                    for (Product p : searchResults) {
                        if (p instanceof Diaper) {
                            filtered.add((Diaper) p);
                        }
                    }
                }
                table.setItems(filtered);
            }
        });

        //重置清空逻辑
        resetBtn.setOnAction(e -> {
            searchInput.clear();
            table.setItems(diaperData);
        });

        //添加功能
        addBtn.setOnAction(e->{
            Stage addStage = new Stage();
            addStage.setTitle("Add New Diaper");

            TextField idInput = new TextField();
            idInput.setPromptText("ID");

            TextField nameInput = new TextField();
            nameInput.setPromptText("Name");

            TextField priceInput = new TextField();
            priceInput.setPromptText("Price");

            TextField stockInput = new TextField();
            stockInput.setPromptText("Stock Quantity");

            TextField brandInput = new TextField();
            brandInput.setPromptText("Brand");

            TextField sizeInput = new TextField();
            sizeInput.setPromptText("Size");

            TextField styleInput = new TextField();
            styleInput.setPromptText("Style");

            Button saveBtn = new Button("Save");

            saveBtn.setOnAction(saveE->{
                try {
                    int id = Integer.parseInt(idInput.getText());
                    String name = nameInput.getText();
                    double price = Double.parseDouble(priceInput.getText());
                    int stock = Integer.parseInt(stockInput.getText());
                    String brand = brandInput.getText();
                    char size = sizeInput.getText().charAt(0);
                    String style = styleInput.getText();

                    Diaper newDiaper = new Diaper(id, name, price, stock, brand, size, style);

                    if (manager.addProduct(newDiaper)) {
                        diaperData.add(newDiaper);
                        addStage.close();
                    }
                    else {
                        showErrorAlert("Failed! ID or Name already exists.");
                    }
                } catch (Exception ex) {
                    showErrorAlert("Invalid Input format!");
                }
            });

            VBox addLayout = new VBox(10);
            addLayout.setPadding(new Insets(20));
            addLayout.getChildren().addAll(
                    new Label("ID:"), idInput,
                    new Label("Name:"), nameInput,
                    new Label("Price:"), priceInput,
                    new Label("Stock:"), stockInput,
                    new Label("Brand:"), brandInput,
                    new Label("Size:"), sizeInput,
                    new Label("Style:"), styleInput,
                    saveBtn);

            addStage.setScene(new Scene(addLayout,300,520));
            addStage.initModality(Modality.APPLICATION_MODAL);
            addStage.show();
        });

        //纵向排列
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        Label header = new Label("Diaper Inventory");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        layout.getChildren().addAll(header,searchBox,new HBox(10,addBtn,delBtn,updBtn),table);

        Scene scene = new Scene(layout,600,400);
        diaperStage.setScene(scene);
        diaperStage.initModality(Modality.APPLICATION_MODAL);
        diaperStage.initOwner(parentStage);
        diaperStage.showAndWait();
    }

    private void openMilk(Stage parentStage){
        Stage milkStage = new Stage();
        milkStage.setTitle("Milk Powder Management");

        TableView<MilkPowder> table = new TableView<>();

        setupCommonColumns(table);

        TableColumn<MilkPowder, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<MilkPowder, String> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));

        TableColumn<MilkPowder, Double> weightCol = new TableColumn<>("Weight");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));

        table.getColumns().addAll(modelCol, ageCol, weightCol);

        ObservableList<MilkPowder> milkData = FXCollections.observableArrayList();
        for (Product p : manager.getInventoryList()) {
            if (p instanceof MilkPowder) {
                milkData.add((MilkPowder) p);
            }
        }
        table.setItems(milkData);

        //功能按钮
        Button addBtn = new Button("Add Product");
        Button delBtn = new Button("Delete Product");
        Button updBtn = new Button("Update Product");

        delBtn.setOnAction(e -> deleteProduct(table, milkData));
        updBtn.setOnAction(e -> openUpdateDialog(table.getSelectionModel().getSelectedItem(), table));

        //制造搜索框&按钮
        TextField searchInput = new TextField();
        searchInput.setPromptText("Search product...");
        Button searchBtn = new Button("Search");
        Button resetBtn = new Button("Reset");

        HBox searchBox = new HBox(10, searchInput, searchBtn, resetBtn);

        //搜索逻辑
        searchBtn.setOnAction(e -> {
            String keyword = searchInput.getText().trim().toLowerCase();
            if (keyword.isEmpty()) {
                table.setItems(milkData);
            } else {
                ObservableList<MilkPowder> filtered = FXCollections.observableArrayList();

                if(keyword.matches("\\d+")) {
                    int searchID = Integer.parseInt(keyword);
                    Product p = manager.searchProductById(searchID);

                    if (p != null && p instanceof MilkPowder) {
                        filtered.add((MilkPowder) p);
                    }
                }else {
                    ArrayList<Product> searchResults = manager.searchProductByName(keyword);
                    for (Product p : searchResults) {
                        if (p instanceof MilkPowder) {
                            filtered.add((MilkPowder) p);
                        }
                    }
                }
                table.setItems(filtered);
            }
        });

        //重置清空逻辑
        resetBtn.setOnAction(e -> {
            searchInput.clear();
            table.setItems(milkData);
        });

        //添加功能
        addBtn.setOnAction(e->{
            Stage addStage = new Stage();
            addStage.setTitle("Add New Milk Powder");

            TextField idInput = new TextField();
            idInput.setPromptText("ID");

            TextField nameInput = new TextField();
            nameInput.setPromptText("Name");

            TextField priceInput = new TextField();
            priceInput.setPromptText("Price");

            TextField stockInput = new TextField();
            stockInput.setPromptText("Stock Quantity");

            TextField brandInput = new TextField();
            brandInput.setPromptText("Brand");

            TextField modelInput = new TextField();
            modelInput.setPromptText("Model");

            TextField ageInput = new TextField();
            ageInput.setPromptText("Age");

            TextField weightInput = new TextField();
            weightInput.setPromptText("Weight");

            Button saveBtn = new Button("Save");

            saveBtn.setOnAction(saveE->{
                try {
                    int id = Integer.parseInt(idInput.getText());
                    String name = nameInput.getText();
                    double price = Double.parseDouble(priceInput.getText());
                    int stock = Integer.parseInt(stockInput.getText());
                    String brand = brandInput.getText();
                    String model = modelInput.getText();
                    String age = ageInput.getText();
                    double weight = Double.parseDouble(weightInput.getText());

                    MilkPowder newMilk = new MilkPowder(id, name, price, stock, brand, model, age, weight);

                    if (manager.addProduct(newMilk)) {
                        milkData.add(newMilk);
                        addStage.close();
                    }
                    else {
                        showErrorAlert("Failed! ID or Name already exists.");
                    }
                } catch (Exception ex) {
                    showErrorAlert("Invalid Input format!");
                }
            });

            VBox addLayout = new VBox(10);
            addLayout.setPadding(new Insets(20));
            addLayout.getChildren().addAll(
                    new Label("ID:"), idInput,
                    new Label("Name:"), nameInput,
                    new Label("Price:"), priceInput,
                    new Label("Stock:"), stockInput,
                    new Label("Brand:"), brandInput,
                    new Label("Model:"), modelInput,
                    new Label("Age:"), ageInput,
                    new Label("Weight:"), weightInput,
                    saveBtn);

            addStage.setScene(new Scene(addLayout,300,580));
            addStage.initModality(Modality.APPLICATION_MODAL);
            addStage.show();
        });

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        Label header = new Label("Milk Powder Inventory");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        layout.getChildren().addAll(header,searchBox,new HBox(10,addBtn,delBtn,updBtn),table);

        Scene scene = new Scene(layout,680,400);
        milkStage.setScene(scene);
        milkStage.initModality(Modality.APPLICATION_MODAL);
        milkStage.initOwner(parentStage);
        milkStage.showAndWait();
    }

    private void openToy(Stage parentStage) {
        Stage toyStage = new Stage();
        toyStage.setTitle("Toy Management");

        TableView<Toy> table = new TableView<>();

        setupCommonColumns(table);

        TableColumn<Toy, Integer> minAgeCol = new TableColumn<>("Minimum Age");
        minAgeCol.setCellValueFactory(new PropertyValueFactory<>("minimumAge"));

        TableColumn<Toy, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        table.getColumns().addAll(minAgeCol, typeCol);

        ObservableList<Toy> toyData = FXCollections.observableArrayList();
        for (Product p : manager.getInventoryList()) {
            if (p instanceof Toy) {
                toyData.add((Toy) p);
            }
        }
        table.setItems(toyData);

        //功能按钮
        Button addBtn = new Button("Add Product");
        Button delBtn = new Button("Delete Product");
        Button updBtn = new Button("Update Product");

        delBtn.setOnAction(e -> deleteProduct(table, toyData));
        updBtn.setOnAction(e -> openUpdateDialog(table.getSelectionModel().getSelectedItem(), table));

        //制造搜索框&按钮
        TextField searchInput = new TextField();
        searchInput.setPromptText("Search product...");
        Button searchBtn = new Button("Search");
        Button resetBtn = new Button("Reset");

        HBox searchBox = new HBox(10, searchInput, searchBtn, resetBtn);

        //搜索逻辑
        searchBtn.setOnAction(e -> {
            String keyword = searchInput.getText().trim().toLowerCase();
            if (keyword.isEmpty()) {
                table.setItems(toyData);
            } else {
                ObservableList<Toy> filtered = FXCollections.observableArrayList();

                if(keyword.matches("\\d+")) {
                    int searchID = Integer.parseInt(keyword);
                    Product p = manager.searchProductById(searchID);

                    if (p != null && p instanceof Toy) {
                        filtered.add((Toy) p);
                    }
                }else {
                    ArrayList<Product> searchResults = manager.searchProductByName(keyword);
                    for (Product p : searchResults) {
                        if (p instanceof Toy) {
                            filtered.add((Toy) p);
                        }
                    }
                }
                table.setItems(filtered);
            }
        });

        //重置清空逻辑
        resetBtn.setOnAction(e -> {
            searchInput.clear();
            table.setItems(toyData);
        });

        //添加功能
        addBtn.setOnAction(e -> {
            Stage addStage = new Stage();
            addStage.setTitle("Add New Toy");

            TextField idInput = new TextField();
            idInput.setPromptText("ID");

            TextField nameInput = new TextField();
            nameInput.setPromptText("Name");

            TextField priceInput = new TextField();
            priceInput.setPromptText("Price");

            TextField stockInput = new TextField();
            stockInput.setPromptText("Stock Quantity");

            TextField brandInput = new TextField();
            brandInput.setPromptText("Brand");

            TextField minAgeInput = new TextField();
            minAgeInput.setPromptText("Minimum Age");

            TextField typeInput = new TextField();
            typeInput.setPromptText("Type");

            Button saveBtn = new Button("Save");

            saveBtn.setOnAction(saveE -> {
                try {
                    int id = Integer.parseInt(idInput.getText());
                    String name = nameInput.getText();
                    double price = Double.parseDouble(priceInput.getText());
                    int stock = Integer.parseInt(stockInput.getText());
                    String brand = brandInput.getText();
                    int minAge = Integer.parseInt(minAgeInput.getText());
                    String type = typeInput.getText();

                    Toy newToy = new Toy(id, name, price, stock, brand, minAge, type);

                    if (manager.addProduct(newToy)) {
                        toyData.add(newToy);
                        addStage.close();
                    }
                    else {
                        showErrorAlert("Failed! ID or Name already exists.");
                    }
                } catch (Exception ex) {
                    showErrorAlert("Invalid Input format!");
                }
            });

            VBox addLayout = new VBox(10);
            addLayout.setPadding(new Insets(20));
            addLayout.getChildren().addAll(
                    new Label("ID:"), idInput,
                    new Label("Name:"), nameInput,
                    new Label("Price:"), priceInput,
                    new Label("Stock:"), stockInput,
                    new Label("Brand:"), brandInput,
                    new Label("Minimum Age:"), minAgeInput,
                    new Label("Type:"), typeInput,
                    saveBtn);

            addStage.setScene(new Scene(addLayout, 300, 520));
            addStage.initModality(Modality.APPLICATION_MODAL);
            addStage.show();
        });

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        Label header = new Label("Toy Inventory");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        layout.getChildren().addAll(header,searchBox,new HBox(10,addBtn,delBtn,updBtn),table);

        Scene scene = new Scene(layout,650,400);
        toyStage.setScene(scene);
        toyStage.initModality(Modality.APPLICATION_MODAL);
        toyStage.initOwner(parentStage);
        toyStage.showAndWait();
    }

    //表格
    private <T extends Product> void setupCommonColumns(TableView<T> table) {
        TableColumn<T, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productID"));

        TableColumn<T, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<T, Double> priceCol = new TableColumn<>("Price(RM)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<T, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        TableColumn<T, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));

        table.getColumns().addAll(idCol, nameCol, priceCol, stockCol, brandCol);
    }

    //删除功能
    private <T extends Product> void deleteProduct(TableView<T> table, ObservableList<T> data) {
        T selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            manager.removeProduct(selected.getProductID());
            data.remove(selected);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Successfully deleted product!",  ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    //更新功能
    private void openUpdateDialog(Product selected, TableView<?> table) {
        if (selected == null) return;
        Stage updStage = new Stage(); updStage.setTitle("Update Product");
        TextField priceInput = new TextField(String.valueOf(selected.getPrice()));
        TextField stockInput = new TextField(String.valueOf(selected.getStockQuantity()));
        Button saveBtn = new Button("Save");

        saveBtn.setOnAction(saveE -> {
            try {
                if (manager.updateProduct(selected.getProductID(), Double.parseDouble(priceInput.getText()), Integer.parseInt(stockInput.getText()))) {
                    table.refresh(); updStage.close();
                }
            } catch (Exception ex) {
                showErrorAlert("Invalid Input format!");
            }
        });

        VBox updLayout = new VBox(10, new Label("New Price: RM"), priceInput, new Label("New Stock: "), stockInput, saveBtn);
        updLayout.setPadding(new Insets(20));
        updStage.setScene(new Scene(updLayout, 250, 200)); updStage.initModality(Modality.APPLICATION_MODAL); updStage.show();
    }

    //错误提示框
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Error"); alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}



