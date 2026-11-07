import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 第 3 步：最小连接测试
 * 目的：验证 Java -> JDBC 驱动 -> MySQL -> D:\MySQLData 整条链路是通的。
 */
public class DbTest {

    // 连接地址：协议 jdbc:mysql://主机:端口/库名?参数
    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/library_system"
            + "?useUnicode=true&characterEncoding=utf8"
            + "&serverTimezone=Asia/Shanghai"
            + "&useSSL=false&allowPublicKeyRetrieval=true";

    private static final String USER = "root";
    private static final String PASSWORD = "root1234";

    public static void main(String[] args) throws Exception {
        // 1. 加载驱动（把“翻译官”叫来上班）
        Class.forName("com.mysql.cj.jdbc.Driver");

        // 2. 建立连接
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("连接成功！MySQL 版本："
                    + conn.getMetaData().getDatabaseProductVersion());

            // 3. 查一下 books 表有几条数据
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM books")) {
                rs.next();
                System.out.println("books 表当前有 " + rs.getInt(1) + " 条数据");
            }

            // 4. 把书列出来（现在应该是空的）
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT title, author, price, stock FROM books LIMIT 5")) {
                System.out.println("--- 图书列表 ---");
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.println("《" + rs.getString("title") + "》 作者："
                            + rs.getString("author") + "  价格：" + rs.getBigDecimal("price")
                            + "  库存：" + rs.getInt("stock"));
                }
                if (!any) {
                    System.out.println("(空表，还没有书)");
                }
            }
        }

        System.out.println("整条链路 OK：Java -> JDBC 驱动 -> MySQL -> D:\\MySQLData");
    }
}
