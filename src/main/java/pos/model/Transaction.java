package pos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a completed transaction.
 * Stores transaction data for history and receipt generation.
 */
public class Transaction {
    private final int id;
    private final LocalDateTime timestamp;
    private final List<TransactionItem> items;
    private final double total;
    private String receiptPath;

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Transaction(int id, LocalDateTime timestamp, List<TransactionItem> items, double total) {
        this.id = id;
        this.timestamp = timestamp;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.receiptPath = null;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DISPLAY_FORMAT);
    }

    public List<TransactionItem> getItems() {
        return new ArrayList<>(items);
    }

    public int getItemCount() {
        return items.size();
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(TransactionItem::getQuantity).sum();
    }

    public double getTotal() {
        return total;
    }

    public String getReceiptPath() {
        return receiptPath;
    }

    public void setReceiptPath(String receiptPath) {
        this.receiptPath = receiptPath;
    }

    /**
     * Generates a receipt string for this transaction.
     *
     * @param storeName The store name for the receipt header
     * @param storeAddress The store address for the receipt header
     * @return Formatted receipt string
     */
    public String generateReceipt(String storeName, String storeAddress) {
        StringBuilder receipt = new StringBuilder();
        String separator = "================================\n";

        receipt.append(separator);
        receipt.append(String.format("        %s\n", centerText(storeName, 32)));
        receipt.append(String.format("      %s\n", centerText(storeAddress, 32)));
        receipt.append(separator);
        receipt.append(String.format("Date: %s\n", getFormattedTimestamp()));
        receipt.append("--------------------------------\n");

        for (TransactionItem item : items) {
            String itemLine = String.format("%s x %d", item.getProductName(), item.getQuantity());
            String priceLine = String.format("$%.2f", item.getSubtotal());
            receipt.append(String.format("%-24s%8s\n", itemLine, priceLine));
        }

        receipt.append("--------------------------------\n");
        receipt.append(String.format("%-24s%8s\n", "TOTAL:", String.format("$%.2f", total)));
        receipt.append(separator);
        receipt.append("      Thank You!\n");
        receipt.append(separator);

        return receipt.toString();
    }

    private String centerText(String text, int width) {
        if (text == null) {
            return "";
        }
        if (text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }

    @Override
    public String toString() {
        return String.format("Transaction #%d - %s - $%.2f (%d items)",
                id, getFormattedTimestamp(), total, items.size());
    }
}