import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/**
 * 主窗口：用一张表格显示 books 表里的所有书。
 * 本文件只负责"界面怎么摆"和"什么时候去取数据"，真正查数据库的是 BookDao。
 */
public class MainFrame extends JFrame {

    private final BookTableModel model = new BookTableModel();
    private final JTable table = new JTable(model);
    private final JLabel status = new JLabel("正在加载...");

    public MainFrame() {
        setTitle("图书管理系统 - 图书列表");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 460);
        setLocationRelativeTo(null); // 窗口居中
        initUi();
        refresh();                   // 打开窗口就去数据库取数据
    }

    /** 摆界面 */
    private void initUi() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 表格样式
        table.setRowHeight(30);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 一次只能选一行
        table.setAutoCreateRowSorter(true); // 点表头可以排序，JTable 自带功能

        // 各列宽度（让"编号/价格/库存"窄一点，书名宽一点）
        table.getColumnModel().getColumn(0).setPreferredWidth(60);   // 编号
        table.getColumnModel().getColumn(1).setPreferredWidth(240);  // 书名
        table.getColumnModel().getColumn(2).setPreferredWidth(160);  // 作者
        table.getColumnModel().getColumn(3).setPreferredWidth(90);   // 价格
        table.getColumnModel().getColumn(4).setPreferredWidth(70);   // 库存

        JScrollPane scroll = new JScrollPane(table); // 带滚动条的表格

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.add(status);

        root.add(scroll, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);
    }

    /** 重新从数据库查一遍，刷新表格 */
    public void refresh() {
        List<Book> books = new BookDao().listAll();
        model.setBooks(books);
        status.setText("共 " + books.size() + " 本书");
    }

    /** 程序入口 */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // 用 Windows 风格
        } catch (Exception ignore) {
            // 设置失败就用默认风格，不影响使用
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
}
