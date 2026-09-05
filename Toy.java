public class Toy extends Product{
    private int minimumAge;
    private String type;

    public Toy(
            int productID,
            String name,
            double price,
            int stockQuantity,
            String brand,
            int minimumAge,
            String type){
        super(productID, name, price, stockQuantity, brand);
        this.minimumAge = minimumAge;
        this.type = type;
    }

    public int getMinimumAge() {
        return minimumAge;
    }

    public void setMinimumAge(int minimumAge) {
        this.minimumAge = minimumAge;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void display() {
        super.display();
        System.out.printf("Minimum age: %d%n", minimumAge);
        System.out.printf("Type: %s%n", type);
    }
}
