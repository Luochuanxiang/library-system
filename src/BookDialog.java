import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;

/**
 * 填一本书的小对话框（JDialog）。
 * 用法：BookDialog.showAdd(窗口) 添加；BookDialog.showEdit(窗口, 书) 修改。
 * 点了"确定"返回 Book 对象；点了"取消"返回 null。
 */
public class BookDialog extends JDialog {

    private final JTextField titleField = new JTextField(24);
    private final JTextField authorField = new JTextField(24);
    private final JTextField priceField = new JTextField(12);
    private final JTextField stockField = new JTextField(12);

    private Book result; // 用户点"确定"后装填的结果

    private BookDialog(Frame owner, String title) {
        super(owner, title, true); // 第三个参数 true = 模态：不开完这个窗口，主窗口点不动
        initUi();
    }

    /** 弹出"添加"对话框；取消返回 null */
    public static Book showAdd(Frame owner) {
        BookDialog dialog = new BookDialog(owner, "添加图书");
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true); // 这行会卡住，直到对话框关闭
        return dialog.result;
    }

    /** 弹出"修改"对话框，把原值填进去；取消返回 null */
    public static Book showEdit(Frame owner, Book book) {
        BookDialog dialog = new BookDialog(owner, "修改图书");
        dialog.titleField.setText(book.getTitle());
        dialog.authorField.setText(book.getAuthor());
        dialog.priceField.setText(book.getPrice() == null ? "" : book.getPrice().toPlainString());
        dialog.stockField.setText(String.valueOf(book.getStock()));
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        if (dialog.result != null) {
            dialog.result.setId(book.getId()); // 修改必须带上原来的编号
        }
        return dialog.result;
    }

    /** 摆界面：4 个输入框 + 确定/取消 */
    private void initUi() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.anchor = GridBagConstraints.WEST;

        addRow(form, c, 0, "书名：", titleField);
        addRow(form, c, 1, "作者：", authorField);
        addRow(form, c, 2, "价格：", priceField);
        addRow(form, c, 3, "库存：", stockField);

        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        okButton.addActionListener(e -> onOk());
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(okButton);
        buttons.add(cancelButton);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
    }

    /** 往表单里加一行：左边标签，右边输入框 */
    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }

    /** 点"确定"：先检查输入，通过了才生成 Book 并关闭 */
    private void onOk() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            warn("书名不能为空", titleField);
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceField.getText().trim());
            if (price.signum() < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            warn("价格要填数字，例如 23.50", priceField);
            return;
        }

        int stock;
        try {
            stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            warn("库存要填正整数，例如 5", stockField);
            return;
        }

        result = new Book(title, authorField.getText().trim(), price, stock);
        dispose(); // 关掉对话框
    }

    /** 弹提示并把光标放回出错的输入框 */
    private void warn(String message, JTextField field) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.WARNING_MESSAGE);
        field.requestFocus();
        field.selectAll();
    }
}
