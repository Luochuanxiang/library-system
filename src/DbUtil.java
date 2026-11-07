import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库工具类：统一管理"怎么连数据库"。
 * 以后所有要连数据库的代码都调用 DbUtil.getConnection()，不用重复写地址密码。
 */
public class DbUtil {

    /** 连接地址：jdbc:mysql://主机:端口/库名?参数 */
    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/library_system"
            + "?useUnicode=true&characterEncoding=utf8"
            + "&serverTimezone=Asia/Shanghai"
            + "&useSSL=false&allowPublicKeyRetrieval=true";

    private static final String USER = "root";
    private static final String PASSWORD = "root1234";

    // 类第一次被使用时执行一次：把驱动（翻译官）加载进来
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("找不到 MySQL 驱动，检查 lib 目录和 classpath", e);
        }
    }

    /** 获取一个数据库连接（用完要关，用 try-with-resources 自动关） */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
