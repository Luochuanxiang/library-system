import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书 DAO（Data Access Object，数据访问对象）。
 * 专门负责 books 表的增、删、改、查。
 * 界面层以后只调用这里的方法，不直接写 SQL。
 */
public class BookDao {

    /** 增：返回新书的编号；失败返回 -1 */
    public int add(Book book) {
        String sql = "INSERT INTO books(title, author, price, stock) VALUES(?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setBigDecimal(3, book.getPrice());
            ps.setInt(4, book.getStock());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    book.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.out.println("添加失败：" + e.getMessage());
        }
        return -1;
    }

    /** 删：按编号删除，返回是否删掉了（删到 0 行就是没这条） */
    public boolean delete(int id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("删除失败：" + e.getMessage());
            return false;
        }
    }

    /** 改：按编号更新书名、作者、价格、库存（整本书覆盖） */
    public boolean update(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, price = ?, stock = ? WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setBigDecimal(3, book.getPrice());
            ps.setInt(4, book.getStock());
            ps.setInt(5, book.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("修改失败：" + e.getMessage());
            return false;
        }
    }

    /** 查：查全部图书，按编号排序 */
    public List<Book> listAll() {
        String sql = "SELECT id, title, author, price, stock FROM books ORDER BY id";
        List<Book> result = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(readBook(rs));
            }
        } catch (SQLException e) {
            System.out.println("查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 查：模糊搜索。书名或作者里"包含"关键字就查出来（SQL 里叫 LIKE 模糊匹配）。
     * 关键字是 null 或空白时，等于查全部。
     * 返回 List 不会为 null，界面可以直接用 size() 和 for 循环。
     */
    public List<Book> search(String keyword) {
        String kw = (keyword == null) ? "" : keyword.trim();
        if (kw.isEmpty()) {
            return listAll(); // 没输入关键字 = 查全部
        }

        String sql = "SELECT id, title, author, price, stock FROM books "
                + "WHERE title LIKE ? OR author LIKE ? ORDER BY id";
        // 前后各加一个 %，拼成 "%关键字%"：表示关键字前后可以有任何内容
        String pattern = "%" + escapeLike(kw) + "%";

        List<Book> result = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(readBook(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("搜索失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 把 LIKE 里的特殊字符转义：\、%、_。
     * 这些符号在 LIKE 里是"通配符"，用户想搜的是普通字符。
     * 例如用户输入 "%" 时，不转义会把整张表都搜出来；转义后只搜真正的百分号。
     */
    private String escapeLike(String text) {
        return text.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /** 查：按编号查一本；没有就返回 null */
    public Book findById(int id) {
        String sql = "SELECT id, title, author, price, stock FROM books WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return readBook(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("查询失败：" + e.getMessage());
        }
        return null;
    }

    /** 把 ResultSet 当前这一行，转成一个 Book 对象 */
    private Book readBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPrice(rs.getBigDecimal("price"));
        book.setStock(rs.getInt("stock"));
        return book;
    }
}
