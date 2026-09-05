
public class Product implements Comparable<Product>{
    private int productID;
    private String name;
    private double price;
    private int stockQuantity;
    private String brand;

    public Product(int productID, String name, double price, int stockQuantity,  String brand) {
        this.productID = productID;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.brand = brand;
    }

    public int getProductID(){
        return productID;
    }

    public void setProductID(int id){
        this.productID = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public double getPrice(){
        return price;
    }

    public void setPrice(double price){
        this.price = price;
    }

    public int getStockQuantity(){
        return stockQuantity;
    }

    public void setStockQuantity(int sQ){
        this.stockQuantity = sQ;
    }

    public String getBrand(){
        return brand;
    }

    public void setBrand(String brand){
        this.brand = brand;
    }

    public void display(){
        System.out.printf("Product ID: %d\n", productID);
        System.out.printf("Product Name: %s\n", name);
        System.out.printf("Product Price: RM%.2f\n", price);
        System.out.printf("Stock Quantity: %d\n", stockQuantity);
        System.out.printf("Brand: %s\n", brand);
    }

    @Override
    public int compareTo(Product o) {
        return Integer.compare(this.productID, o.productID);
    }
}
