import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 主窗口：表格显示图书 + 一排按钮（刷新/添加/修改/删除）。
 * 界面只负责"摆控件、响应点击"，数据库操作全部交给 BookDao。
 */
public class MainFrame extends JFrame {

    private final BookDao dao = new BookDao();
    private final BookTableModel model = new BookTableModel();
    private final JTable table = new JTable(model);
    private final JLabel status = new JLabel("正在加载...");

    public MainFrame() {
        setTitle("图书管理系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 480);
        setLocationRelativeTo(null); // 窗口居中
        initUi();
        refresh();                   // 打开窗口就去数据库取数据
    }

    /** 摆界面 */
    private void initUi() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- 上面：按钮工具栏 ----
        JButton refreshButton = new JButton("刷新");
        JButton addButton = new JButton("添加图书");
        JButton editButton = new JButton("修改选中");
        JButton deleteButton = new JButton("删除选中");

        refreshButton.addActionListener(e -> refresh());
        addButton.addActionListener(e -> onAdd());
        editButton.addActionListener(e -> onEdit());
        deleteButton.addActionListener(e -> onDelete());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.add(refreshButton);
        toolbar.add(addButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);

        // ---- 中间：表格 ----
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

        // 双击某一行 = 修改这一行
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEdit();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table); // 带滚动条的表格

        // ---- 下面：状态栏 ----
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.add(status);

        root.add(toolbar, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);
    }

    /** 重新从数据库查一遍，刷新表格 */
    public void refresh() {
        List<Book> books = dao.listAll();
        model.setBooks(books);
        status.setText("共 " + books.size() + " 本书");
    }

    /** 点"添加图书" */
    private void onAdd() {
        Book book = BookDialog.showAdd(this);
        if (book == null) {
            return; // 用户点了取消
        }
        int id = dao.add(book);
        if (id > 0) {
            refresh();
            status.setText("已添加《" + book.getTitle() + "》，编号 " + id + "，共 " + model.getRowCount() + " 本书");
        } else {
            JOptionPane.showMessageDialog(this, "添加失败，看看 MySQL 是不是没启动", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 点"修改选中"（或双击某行） */
    private void onEdit() {
        Book selected = getSelectedBook();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "先在表格里点一行，再点修改", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Book edited = BookDialog.showEdit(this, selected);
        if (edited == null) {
            return;
        }
        if (dao.update(edited)) {
            refresh();
            status.setText("已修改《" + edited.getTitle() + "》");
        } else {
            JOptionPane.showMessageDialog(this, "修改失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 点"删除选中" */
    private void onDelete() {
        Book selected = getSelectedBook();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "先在表格里点一行，再点删除", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "确定删除《" + selected.getTitle() + "》吗？删了就找不回来了。",
                "删除确认", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        if (dao.delete(selected.getId())) {
            refresh();
            status.setText("已删除《" + selected.getTitle() + "》");
        } else {
            JOptionPane.showMessageDialog(this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 取表格里当前选中的那本书；没选返回 null */
    private Book getSelectedBook() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        // 注意：开了排序后，界面上的行号和真实数据行号可能不一致，要转换
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getBookAt(modelRow);
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
