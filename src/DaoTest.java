import java.math.BigDecimal;
import java.util.List;

/** 测试类：在控制台验证 BookDao（增删改查）四个方法是否正常 */
public class DaoTest {

    public static void main(String[] args) {
        BookDao dao = new BookDao();

        System.out.println("=== 1. 新增三本书 ===");
        int id1 = dao.add(new Book("三体", "刘慈欣", new BigDecimal("23.00"), 5));
        int id2 = dao.add(new Book("活着", "余华", new BigDecimal("20.50"), 3));
        int id3 = dao.add(new Book("平凡的世界", "路遥", new BigDecimal("28.00"), 4));
        System.out.println("新增编号：" + id1 + "、" + id2 + "、" + id3);

        System.out.println();
        System.out.println("=== 2. 查询全部 ===");
        printList(dao.listAll());

        System.out.println();
        System.out.println("=== 3. 修改《三体》：价格改成 25.80，库存改成 8 ===");
        Book book = dao.findById(id1);
        if (book == null) {
            System.out.println("没找到编号 " + id1);
        } else {
            book.setPrice(new BigDecimal("25.80"));
            book.setStock(8);
            boolean ok = dao.update(book);
            System.out.println("修改结果：" + (ok ? "成功" : "失败"));
            printList(dao.listAll());
        }

        System.out.println();
        System.out.println("=== 4. 删除《活着》（编号 " + id2 + "）===");
        boolean deleted = dao.delete(id2);
        System.out.println("删除结果：" + (deleted ? "成功" : "失败"));
        printList(dao.listAll());

        System.out.println();
        System.out.println("=== 5. 删除一座不存在的编号 999 ===");
        System.out.println("删除结果：" + (dao.delete(999) ? "成功" : "失败（正常，本来就没有）"));
    }

    /** 打印一个图书列表 */
    private static void printList(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("(没有书)");
            return;
        }
        System.out.println("共 " + books.size() + " 本书：");
        for (Book b : books) {
            System.out.println("  " + b);
        }
    }
}
