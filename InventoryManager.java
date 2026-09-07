import java.util.ArrayList;
import java.io.*;

public class InventoryManager {
    private ArrayList<Product> inventoryList;

    public InventoryManager() {
        inventoryList = new ArrayList<>();
    }

    public ArrayList<Product> getInventoryList() {
        return inventoryList;
    }

    public ArrayList<Product> searchProductByName(String name) {
        ArrayList<Product> productList = new ArrayList<>();
        for(Product p : inventoryList) {
            if(p.getName().toLowerCase().contains(name.toLowerCase())) {
                productList.add(p);
            }
        }
        return productList;
    }

    public Product searchProductById(int id) {
        for (Product p : inventoryList) {
            if(p.getProductID() == id) {
                return p;
            }
        }
        return null;
    }

    public boolean addProduct(Product p) {
        if(checkIdExists(p.getProductID())) {
            return false;
        }
        if(checkStore(p.getName())){
            return false;
        }
        if(p.getPrice() < 0.0 || p.getStockQuantity() < 0){
            System.out.println("Error in adding product");
            return false;
        }
        inventoryList.add(p);
        java.util.Collections.sort(inventoryList);

        System.out.println("Success added: " + p.getName());
        saveToFile();
        return true;
    }

    public boolean removeProduct(int id) {
        Product p = searchProductById(id);
        if(p != null) {
            inventoryList.remove(p);
            System.out.println("Success removed: " + p.getName());
            saveToFile();
            return true;
        }
        return false;
    }

    public boolean checkIdExists(int id){
        for(Product p : inventoryList) {
            if(p.getProductID() == id) {
                System.out.println("ID already exists: " + p.getName());
                return true;
            }
        }
        return false;
    }

    public boolean updateProduct(int id, double newPrice, int newStockQuantity) {
        Product p = searchProductById(id);
        if(p != null) {
            if(newPrice < 0.0 || newStockQuantity < 0) {
                System.out.println("Error in updating product");
                return false;
            }
            else{
                p.setPrice(newPrice);
                p.setStockQuantity(newStockQuantity);
                System.out.println("Success updated: " + p.getName());
                saveToFile();
                return true;
            }
        }
        System.out.println("Product with id " + id + " not found");
        return false;
    }

    public String saleProduct(int id, int quantityBuy,String paymentMethod) {
        Product p = searchProductById(id);
        if(p == null) {
            return "Error: Product with id " + id + " not found";
        }

        if(quantityBuy <= 0) {
            return "Error: quantity buy should be at least 1";
        }

        if(p.getStockQuantity() < quantityBuy) {
            return String.format("[ERROR] Insufficient stock for '%s'! (Available: %d, Requested: %d)", p.getName(), p.getStockQuantity(), quantityBuy);
        }

        p.setStockQuantity(p.getStockQuantity() - quantityBuy);
        saveToFile();

        double totalAmount = quantityBuy * p.getPrice();
        saveTransactionRecord(p,quantityBuy,totalAmount,paymentMethod);

        return String.format(
                "=============================================\n" +
                        "                 MYBABY STORE                \n" +
                        "=============================================\n" +
                        "Item Sold : %s (ID: %d)\n" +
                        "Price/Unit: RM%.2f\n" +
                        "Quantity  : %d\n" +
                        "---------------------------------------------\n" +
                        "Total Paid: RM%.2f\n" +
                        "Payment   : %s\n" +
                        "=============================================\n\n",
                p.getName(), p.getProductID(), p.getPrice(), quantityBuy, totalAmount, paymentMethod
        );

    }

    private final String FILENAME = "inventory.txt";

    public void saveToFile() {
        try(PrintWriter writer = new PrintWriter(new FileWriter(FILENAME))){
            for(Product p : inventoryList) {
                if(p instanceof Diaper) {
                    Diaper d = (Diaper) p;
                    writer.printf("Diaper, %d, %s, %.2f, %d, %s, %c, %s\n",
                            d.getProductID(),d.getName(),d.getPrice(),d.getStockQuantity(),d.getBrand(),d.getSize(),d.getStyle());
                }
                else if(p instanceof MilkPowder) {
                    MilkPowder m = (MilkPowder) p;
                    writer.printf("Milk Powder, %d, %s, %.2f, %d, %s, %s, %s, %.2f\n",
                            m.getProductID(), m.getName(), m.getPrice(), m.getStockQuantity(), m.getBrand(), m.getModel(), m.getAge(), m.getWeight());
                }
                else if(p instanceof Toy) {
                    Toy t = (Toy) p;
                    writer.printf("Toy,%d,%s,%.2f,%d,%s,%d,%s\n",
                            t.getProductID(), t.getName(), t.getPrice(), t.getStockQuantity(), t.getBrand(), t.getMinimumAge(), t.getType());
                }
            }
            System.out.println("Data Saved Successfully");
        }catch(IOException e){
            System.err.println("File error：" + e.getMessage());
        }
    }

    public void loadFromFile() {
        File file = new File(FILENAME);
        if (!file.exists()) {
            System.out.println("No existing data file found. Starting fresh.");
            return;
        }

        inventoryList.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 7) continue;

                String type = parts[0].trim();
                int id = Integer.parseInt(parts[1].trim());
                String name = parts[2].trim();
                double price = Double.parseDouble(parts[3].trim());
                int stock = Integer.parseInt(parts[4].trim());
                String brand = parts[5].trim();

                //根据类型还原读取数据
                if (type.equals("Diaper")) {
                    char size = parts[6].trim().charAt(0);
                    String style = parts[7].trim();
                    inventoryList.add(new Diaper(id, name, price, stock, brand, size, style));
                } else if (type.equals("Milk Powder")) {
                    String model = parts[6].trim();
                    String age = parts[7].trim();
                    double weight = Double.parseDouble(parts[8].trim());
                    inventoryList.add(new MilkPowder(id, name, price, stock, brand, model, age, weight));
                } else if (type.equals("Toy")) {
                    int minAge = Integer.parseInt(parts[6].trim());
                    String toyType = parts[7].trim();
                    inventoryList.add(new Toy(id, name, price, stock, brand, minAge, toyType));
                }
            }
            java.util.Collections.sort(inventoryList);
            System.out.println("Data successfully loaded from file.");
        } catch (Exception e) {
            System.out.println("Error loading from file: " + e.getMessage());
        }
    }

    private void saveTransactionRecord(Product p, int quantity, double totalAmount, String paymentMethod) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("sales.txt", true))) {
            writer.printf("ID: %d | Product: %s | Qty: %d | Total: RM%.2f | Payment: %s\n",
                    p.getProductID(), p.getName(), quantity, totalAmount, paymentMethod);
        } catch (IOException e) {
            System.err.println("Error writing sales record: " + e.getMessage());
        }
    }

    public boolean checkStore(String name){
        for(Product p : inventoryList) {
            if(p.getName().equalsIgnoreCase(name.trim())){
                System.out.println("Product " + p.getName() + " already exists.");
                return true;
            }
        }
        return false;
    }

}
