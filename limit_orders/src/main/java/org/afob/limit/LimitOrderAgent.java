import org.afob.prices.PriceListener;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class LimitOrderAgent implements PriceListener {
    private final ExecutionClient executionClient;
    private final Map<String, Order> orders;

    public LimitOrderAgent(final ExecutionClient ec) {
        this.executionClient = ec;
        this.orders = new HashMap<>();
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {
        // Check if the product is IBM and if the price drops below $100
        if (productId.equals("IBM")) {
            if (price.compareTo(new BigDecimal("100")) < 0) {
                // If the conditions are met, add a buy order for 1000 shares of IBM
                addOrder(true, "IBM", 1000, new BigDecimal("100"));
            }
        }
        
        // Check if there is a pending order for the received product ID
        if (orders.containsKey(productId)) {
            Order order = orders.get(productId);
            // Check if the order is a buy order and if the price meets the limit
            if (order.isBuy() && price.compareTo(order.getLimit()) <= 0) {
                // Execute the buy order
                executionClient.buy(order.getProductId(), order.getAmount());
                // Remove the order from the pending orders map
                orders.remove(productId);
            }
        }
    }

    // Method to add a new order
    public void addOrder(boolean buy, String productId, int amount, BigDecimal limit) {
        orders.put(productId, new Order(buy, productId, amount, limit));
    }

    // Inner class representing an order
    private static class Order {
        private final boolean buy;
        private final String productId;
        private final int amount;
        private final BigDecimal limit;

        // Constructor
        public Order(boolean buy, String productId, int amount, BigDecimal limit) {
            this.buy = buy;
            this.productId = productId;
            this.amount = amount;
            this.limit = limit;
        }

        // Getter methods
        public boolean isBuy() {
            return buy;
        }

        public String getProductId() {
            return productId;
        }

        public int getAmount() {
            return amount;
        }

        public BigDecimal getLimit() {
            return limit;
        }

        // toString method to represent Order object as a string
        @Override
        public String toString() {
            return "Order{" +
                    "buy=" + buy +
                    ", productId='" + productId + '\'' +
                    ", amount=" + amount +
                    ", limit=" + limit +
                    '}';
        }
    }

    // toString method to represent LimitOrderAgent object as a string
    @Override
    public String toString() {
        return "LimitOrderAgent{" +
                "orders=" + orders +
                '}';
    }
}



*****************************************************************************************************************************************


To implement a basic trading strategy using the provided framework, you can create a class that implements the PriceListener interface. This class will receive price updates for different products and decide whether to execute buy or sell orders based on the received prices. Here's an example implementation:


package org.afob.strategy;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BasicTradingStrategy implements PriceListener {

    private final ExecutionClient executionClient;
    private final Map<String, BigDecimal> lastPrices;

    public BasicTradingStrategy(ExecutionClient executionClient) {
        this.executionClient = executionClient;
        this.lastPrices = new HashMap<>();
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {
        // Check if we have a previous price for this product
        if (lastPrices.containsKey(productId)) {
            BigDecimal previousPrice = lastPrices.get(productId);
            // Decide whether to buy, sell, or do nothing based on the price movement
            if (price.compareTo(previousPrice) > 0) {
                // Price increased, consider selling
                try {
                    executionClient.sell(productId, 1); // Sell 1 unit for simplicity
                    System.out.println("Sold 1 unit of " + productId + " at price: " + price);
                } catch (ExecutionException e) {
                    System.err.println("Failed to sell: " + e.getMessage());
                }
            } else if (price.compareTo(previousPrice) < 0) {
                // Price decreased, consider buying
                try {
                    executionClient.buy(productId, 1); // Buy 1 unit for simplicity
                    System.out.println("Bought 1 unit of " + productId + " at price: " + price);
                } catch (ExecutionException e) {
                    System.err.println("Failed to buy: " + e.getMessage());
                }
            }
        }
        // Update last price for the product
        lastPrices.put(productId, price);
    }
}


This BasicTradingStrategy class listens for price updates using the priceTick method. It compares the current price with the last known price for each product and decides whether to buy, sell, or do nothing based on the price movement. It then interacts with the ExecutionClient to execute buy or sell orders.

Now, you can use this BasicTradingStrategy class in your application. Here's an example of how you might set it up:


package org.afob;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;
import org.afob.strategy.BasicTradingStrategy;

public class Main {

    public static void main(String[] args) {
        ExecutionClient executionClient = new ExecutionClient();
        PriceListener tradingStrategy = new BasicTradingStrategy(executionClient);

        // Register the trading strategy as a price listener
        // Assuming you have some mechanism to receive price updates
        // For example, if you have a PriceFeed class that provides price updates,
        // you would register the trading strategy with it.
        // priceFeed.registerListener(tradingStrategy);
    }
}




    
