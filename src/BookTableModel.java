import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 表格模型（TableModel）：把 List<Book> 变成 JTable 能显示的表。
 * JTable 自己不存数据，数据从这里的 getValueAt 一个个取。
 */
public class BookTableModel extends AbstractTableModel {

    /** 表头：5 列 */
    private static final String[] COLUMNS = {"编号", "书名", "作者", "价格", "库存"};

    private List<Book> books = new ArrayList<>();

    /** 换一批数据（界面刷新时调用） */
    public void setBooks(List<Book> books) {
        this.books = (books == null) ? new ArrayList<Book>() : books;
        fireTableDataChanged(); // 通知表格：数据变了，重新画
    }

    /** 取某一行对应的 Book 对象（后面做"修改/删除"要用） */
    public Book getBookAt(int row) {
        return books.get(row);
    }

    @Override
    public int getRowCount() {
        return books.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    /** 表格画某一格时，会来问这个值是什么 */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Book b = books.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return b.getId();
            case 1:
                return b.getTitle();
            case 2:
                return b.getAuthor();
            case 3:
                return b.getPrice() == null ? BigDecimal.ZERO : b.getPrice();
            case 4:
                return b.getStock();
            default:
                return "";
        }
    }
}
