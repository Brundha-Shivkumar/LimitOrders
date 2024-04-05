package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.Map;

public class LimitOrderAgent implements PriceListener {

    private final ExecutionClient executionClient;
    private final Map<String, Order> orders = new HashMap<>();

    public LimitOrderAgent(final ExecutionClient ec) {
        this.executionClient = ec;
    }

    public void addOrder(boolean isBuy, String productId, int amount, BigDecimal limit) {
        // Validate order parameters (amount should be positive, limit should be non-null)
        if (amount <= 0) {
            throw new IllegalArgumentException("Order amount must be positive");
        }
        if (limit == null) {
            throw new IllegalArgumentException("Order limit cannot be null");
        }

        // Create order object
        Order order = new Order(isBuy, productId, amount, limit);
        orders.put(generateOrderId(productId, isBuy), order);
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {
        // Check if any orders for the product exist
        if (!orders.containsKey(productId)) {
            return;
        }

        // Get the order for the product
        Order order = orders.get(productId);

        // Check if the price meets the order condition (better than or equal to limit for buy, less than or equal to limit for sell)
        if ((order.isBuy && price.compareTo(order.getLimit()) <= 0) ||
                (!order.isBuy && price.compareTo(order.getLimit()) >= 0)) {
            // Execute the order
            executionClient.executeOrder(order.isBuy(), productId, order.getAmount());
            orders.remove(productId);
        }
    }

    private String generateOrderId(String productId, boolean isBuy) {
        // Implement your logic to generate a unique order ID based on product ID and buy/sell flag
        // This example uses a simple concatenation
        return productId + (isBuy ? "-buy" : "-sell");
    }

    private static class Order {
        private final boolean isBuy;
        private final String productId;
        private final int amount;
        private final BigDecimal limit;

        public Order(boolean isBuy, String productId, int amount, BigDecimal limit) {
            this.isBuy = isBuy;
            this.productId = productId;
            this.amount = amount;
            this.limit = limit;
        }

        public boolean isBuy() {
            return isBuy;
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
    }
}
