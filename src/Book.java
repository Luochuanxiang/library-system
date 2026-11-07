import java.math.BigDecimal;

/**
 * 图书实体类：只负责"装数据"，不负责连数据库。
 * 一个 Book 对象 = books 表里的一行。
 */
public class Book {

    private int id;                 // 编号（数据库自增，插入前一般是 0）
    private String title;           // 书名
    private String author;          // 作者
    private BigDecimal price = BigDecimal.ZERO; // 价格（用 BigDecimal 表示金额，避免小数误差）
    private int stock;              // 库存

    public Book() {
    }

    public Book(String title, String author, BigDecimal price, int stock) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    /** 打印一个 Book 对象时显示的格式 */
    @Override
    public String toString() {
        return "[编号" + id + "] 《" + title + "》 作者：" + author
                + "  价格：" + price + "  库存：" + stock;
    }
}
