public class MilkPowder extends Product {
    private String model;
    private String age;
    private double weight;

    public MilkPowder(
            int productID,
            String name,
            double price,
            int stockQuantity,
            String brand,
            String model,
            String age,
            double weight) {
        super(productID, name, price, stockQuantity,  brand);
        this.model = model;
        this.age = age;
        this.weight = weight;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public void display() {
        super.display();
        System.out.printf("Model: %s%n", model);
        System.out.printf("Age: %s%n", age);
        System.out.printf("Weight: %.2f%n", weight);
    }

}
