import java.math.BigDecimal;
import java.util.List;

/**
 * 测试类：验证 BookDao.search 模糊搜索（按书名 / 作者）是否正常。
 * 做法：临时插入两本标题带「搜索测试」的书，测完删掉，不弄脏原来的数据。
 * 有测试没通过时以退出码 1 结束，命令行里可以用 echo $? 看到。
 */
public class SearchTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        BookDao dao = new BookDao();

        // 准备两条临时数据（标题带「搜索测试」，不容易和真实数据撞）
        int id1 = dao.add(new Book("搜索测试-银河帝国", "阿西莫夫", new BigDecimal("30.00"), 2));
        int id2 = dao.add(new Book("搜索测试-活着之上", "余华", new BigDecimal("26.00"), 3));
        if (id1 <= 0 || id2 <= 0) {
            System.out.println("准备测试数据失败，检查 MySQL 是否启动");
            System.exit(1);
        }

        try {
            // 1. 按书名的一部分搜（关键字在书名中间也算命中）
            List<Book> r1 = dao.search("银河");
            expect("按书名搜「银河」只找到 1 本", 1, r1.size());
            expect("找到的是《搜索测试-银河帝国》", id1, r1.isEmpty() ? -1 : r1.get(0).getId());

            // 2. 按作者搜
            List<Book> r2 = dao.search("阿西莫夫");
            expect("按作者搜「阿西莫夫」找到 1 本", 1, r2.size());

            // 3. 关键字只命中作者时，不该把别的书带出来
            List<Book> r3 = dao.search("余华");
            expect("按作者搜「余华」包含《搜索测试-活着之上》", true, containsId(r3, id2));
            expect("按作者搜「余华」不包含《搜索测试-银河帝国》", false, containsId(r3, id1));

            // 4. 公共关键字「搜索测试」应同时找到两本临时书
            List<Book> r4 = dao.search("搜索测试");
            expect("按公共前缀搜「搜索测试」至少找到 2 本", true, r4.size() >= 2);
            expect("两本临时书都在结果里", true, containsId(r4, id1) && containsId(r4, id2));

            // 5. 空关键字 = 查全部（数量和 listAll 一致）
            List<Book> r5 = dao.search("   ");
            expect("空关键字等于查全部", dao.listAll().size(), r5.size());

            // 6. 搜不存在的关键字 → 0 本，不报错
            List<Book> r6 = dao.search("绝对不存在的书名xyz123");
            expect("搜不存在的关键字返回 0 本", 0, r6.size());

            // 7. 关键字前后空格会自动去掉
            List<Book> r7 = dao.search("  银河  ");
            expect("关键字前后空格会被去掉", 1, r7.size());

            // 8. 通配符转义：输入 % 或 _ 应该当普通字符搜，而不是把全部书都搜出来
            expect("搜「%」不会把全部书搜出来", true, dao.search("%").size() < dao.listAll().size());
            expect("搜「_」不会把全部书搜出来", true, dao.search("_").size() < dao.listAll().size());
        } finally {
            // 9. 清理临时数据，别污染原来的表
            dao.delete(id1);
            dao.delete(id2);
        }

        System.out.println();
        System.out.println("结果：通过 " + passed + " 项，失败 " + failed + " 项");
        if (failed > 0) {
            System.out.println("有测试没通过，退出码 1");
            System.exit(1);
        }
        System.out.println("全部通过！");
    }

    /** 断言：实际值等于期望值时记一次通过，否则打印失败详情 */
    private static void expect(String name, Object expected, Object actual) {
        if (expected.equals(actual)) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name + "（期望：" + expected + "，实际：" + actual + "）");
        }
    }

    /** 列表里有没有指定编号的书 */
    private static boolean containsId(List<Book> books, int id) {
        for (Book b : books) {
            if (b.getId() == id) {
                return true;
            }
        }
        return false;
    }
}
