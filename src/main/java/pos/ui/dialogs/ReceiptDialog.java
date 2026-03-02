package pos.ui.dialogs;

import pos.app.ThemeManager;
import pos.model.Transaction;
import pos.util.Config;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A dialog for viewing and printing receipts.
 */
public class ReceiptDialog extends JDialog {
    private final Transaction transaction;
    private final JTextArea receiptArea;

    public ReceiptDialog(Frame parent, Transaction transaction) {
        super(parent, "Receipt #" + transaction.getId(), true);
        this.transaction = transaction;
        this.receiptArea = new JTextArea();

        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBackground(ThemeManager.getInstance().getPanelBackgroundColor());

        // Receipt content
        String receiptContent = transaction.generateReceipt(
                Config.getInstance().getStoreName(),
                Config.getInstance().getStoreAddress()
        );

        receiptArea.setText(receiptContent);
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        receiptArea.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        receiptArea.setForeground(ThemeManager.getInstance().getTextColor());
        receiptArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setPreferredSize(new Dimension(450, 500));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());

        JButton printButton = new JButton("Print");
        printButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        printButton.setBackground(ThemeManager.getInstance().getSecondaryColor());
        printButton.setForeground(ThemeManager.getInstance().getTextColor());
        printButton.setFocusable(false);
        printButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        printButton.addActionListener(e -> printReceipt());

        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        saveButton.setBackground(ThemeManager.getInstance().getSecondaryColor());
        saveButton.setForeground(ThemeManager.getInstance().getTextColor());
        saveButton.setFocusable(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> saveReceipt());

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setBackground(ThemeManager.getInstance().getOrangeColor());
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusable(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(printButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Dialog settings
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(getParent());
    }

    private void printReceipt() {
        try {
            boolean printed = receiptArea.print();
            if (printed) {
                JOptionPane.showMessageDialog(this,
                        "Receipt sent to printer.",
                        "Print Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to print receipt: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveReceipt() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(
                "receipt_" + transaction.getId() + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(fileChooser.getSelectedFile())) {
                writer.write(receiptArea.getText());
                JOptionPane.showMessageDialog(this,
                        "Receipt saved to: " + fileChooser.getSelectedFile().getAbsolutePath(),
                        "Save Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to save receipt: " + e.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Shows the receipt dialog.
     *
     * @param parent The parent frame
     * @param transaction The transaction to display
     */
    public static void showReceipt(Frame parent, Transaction transaction) {
        ReceiptDialog dialog = new ReceiptDialog(parent, transaction);
        dialog.setVisible(true);
    }
}