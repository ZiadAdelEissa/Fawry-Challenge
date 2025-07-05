package Src;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Product {
    String name;
    double price;
    int quantity;
    boolean canExpire;
    LocalDate expiryDate;
    boolean requiresShipping;
    double weight;

    public Product(String name, double price, int quantity, boolean canExpire, 
                  LocalDate expiryDate, boolean requiresShipping, double weight) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.canExpire = canExpire;
        this.expiryDate = expiryDate;
        this.requiresShipping = requiresShipping;
        this.weight = weight;
    }

    public boolean isExpired() {
        return canExpire && LocalDate.now().isAfter(expiryDate);
    }
}

class CartItem {
    Product product;
    int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return product.price * quantity;
    }
}

class Customer {
    String name;
    double balance;
    List<CartItem> cart = new ArrayList<>();

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public void addToCart(Product product, int quantity) {
        if (quantity <= 0) {
            System.out.println("Quantity must be positive");
            return;
        }
        if (quantity > product.quantity) {
            System.out.println("Not enough stock for " + product.name);
            return;
        }
        cart.add(new CartItem(product, quantity));
        System.out.println(quantity + " " + product.name + "(s) added to cart");
    }
}

interface ShippingService {
    void shipItems(List<Product> items);
}

class SimpleShippingService implements ShippingService {
    @Override
    public void shipItems(List<Product> items) {
        System.out.println("\nShipping these items:");
        for (Product item : items) {
            System.out.printf("- %s (Weight: %.2f kg)\n", item.name, item.weight);
        }
    }
}

class CheckoutService {
    private ShippingService shippingService;
    private static final double BASE_SHIPPING = 5.0;
    private static final double PER_KG_RATE = 2.0;

    public CheckoutService(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    public void checkout(Customer customer) {
        if (customer.cart.isEmpty()) {
            System.out.println("Cannot checkout - your cart is empty");
            return;
        }

        for (CartItem item : customer.cart) {
            Product p = item.product;
            
            if (item.quantity > p.quantity) {
                System.out.println("Checkout failed - " + p.name + " is out of stock");
                return;
            }
            
            if (p.isExpired()) {
                System.out.println("Checkout failed - " + p.name + " has expired");
                return;
            }
        }

        double subtotal = customer.cart.stream()
            .mapToDouble(CartItem::getTotalPrice)
            .sum();
        
        List<Product> shippableItems = new ArrayList<>();
        double totalWeight = 0;
        
        for (CartItem item : customer.cart) {
            if (item.product.requiresShipping) {
                shippableItems.add(item.product);
                totalWeight += item.product.weight * item.quantity;
            }
        }
        
        double shippingFee = BASE_SHIPPING + (PER_KG_RATE * totalWeight);
        double total = subtotal + shippingFee;

        if (total > customer.balance) {
            System.out.println("Checkout failed - insufficient balance");
            return;
        }

        customer.balance -= total;

        for (CartItem item : customer.cart) {
            item.product.quantity -= item.quantity;
        }

        if (!shippableItems.isEmpty()) {
            shippingService.shipItems(shippableItems);
        }

        printReceipt(customer, subtotal, shippingFee, total);
        customer.cart.clear();
    }

    private void printReceipt(Customer customer, double subtotal, double shippingFee, double total) {
        System.out.println("\n=== RECEIPT ===");
        System.out.println("Customer: " + customer.name);
        System.out.println("Items Purchased:");
        for (CartItem item : customer.cart) {
            System.out.printf("- %d x %s: $%.2f\n", 
                item.quantity, 
                item.product.name, 
                item.getTotalPrice());
        }
        System.out.printf("Subtotal: $%.2f\n", subtotal);
        System.out.printf("Shipping: $%.2f\n", shippingFee);
        System.out.printf("Total: $%.2f\n", total);
        System.out.printf("Remaining balance: $%.2f\n", customer.balance);
        System.out.println("===============");
    }
}

public class InteractiveECommerce {
    public static void main(String[] args) {
       
        List<Product> products = new ArrayList<>();
        products.add(new Product("TV", 499.99, 10, false, null, true, 15.5));
        products.add(new Product("Cheese", 5.99, 20, true, LocalDate.now().plusDays(7), true, 0.5));
        products.add(new Product("Mobile Card", 10.0, 100, false, null, false, 0));
        products.add(new Product("Biscuits", 3.50, 15, true, LocalDate.now().plusDays(30), true, 0.3));

        // Create customer
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your balance: ");
        double balance = scanner.nextDouble();
        Customer customer = new Customer(name, balance);

        // Create services
        ShippingService shippingService = new SimpleShippingService();
        CheckoutService checkoutService = new CheckoutService(shippingService);

       
        while (true) {
            System.out.println("\n===== MENU =====");
            System.out.println("1. View Products");
            System.out.println("2. Add to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Checkout");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            
            switch (choice) {
                case 1: 
                    System.out.println("\nAvailable Products:");
                    for (int i = 0; i < products.size(); i++) {
                        Product p = products.get(i);
                        System.out.printf("%d. %s - $%.2f (Qty: %d)", 
                            i+1, p.name, p.price, p.quantity);
                        if (p.canExpire) {
                            System.out.printf(" - Expires: %s", p.expiryDate);
                        }
                        if (p.requiresShipping) {
                            System.out.printf(" - Weight: %.2fkg", p.weight);
                        }
                        System.out.println();
                    }
                    break;
                    
                case 2: 
                    System.out.println("\nAvailable Products:");
                    for (int i = 0; i < products.size(); i++) {
                        System.out.printf("%d. %s\n", i+1, products.get(i).name);
                    }
                    System.out.print("Select product number: ");
                    int productNum = scanner.nextInt();
                    if (productNum < 1 || productNum > products.size()) {
                        System.out.println("Invalid product number");
                        break;
                    }
                    Product selectedProduct = products.get(productNum-1);
                    System.out.print("Enter quantity: ");
                    int quantity = scanner.nextInt();
                    customer.addToCart(selectedProduct, quantity);
                    break;
                    
                case 3: 
                    if (customer.cart.isEmpty()) {
                        System.out.println("Your cart is empty");
                    } else {
                        System.out.println("\nYour Cart:");
                        for (CartItem item : customer.cart) {
                            System.out.printf("- %d x %s: $%.2f\n", 
                                item.quantity, 
                                item.product.name, 
                                item.getTotalPrice());
                        }
                        double subtotal = customer.cart.stream()
                            .mapToDouble(CartItem::getTotalPrice)
                            .sum();
                        System.out.printf("Subtotal: $%.2f\n", subtotal);
                    }
                    break;
                    
                case 4: 
                    checkoutService.checkout(customer);
                    break;
                    
                case 5:
                    System.out.println("Thank you for shopping with us!");
                    scanner.close();
                    System.exit(0);
                    
                default:
                    System.out.println("Invalid choice");
            }
        }
    }
}
