
public class Diaper extends Product{
    private char size;
    private String style;

    public Diaper(
            int productID,
            String name,
            double price,
            int stockQuantity,
            String brand,
            char size,
            String style) {
        super(productID, name, price, stockQuantity, brand);
        this.size = size;
        this.style = style;
    }

    public char getSize() {
        return size;
    }

    public void setSize(char size) {
        this.size = size;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @Override
    public void display() {
        super.display();
        System.out.printf("Size: %c%n", size);
        System.out.printf("Style: %s%n", style);
    }

}
