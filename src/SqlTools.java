import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @Description:
 * @Copyright: Dist
 * @Author: MengHui
 * @Date: 2019-07-09 ${MINUTF}
 * @Modified:
 * @Description:
 * @Date:
 */
public class SqlTools {

    private Connection connect = null;
    static Properties properties = new Properties();
    static String path = null;
        static {
            //获取文件外一层路径
            path = MainApp.class.getResource("/config.properties").toString().replaceAll("jar:file:/","")
                    .replaceAll("getsql.jar!/config.properties","");

            //加载数据驱动
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            //加载配置文件

            try {
                System.out.println(path);
                //properties.load(SqlTools.class.getClassLoader().getResourceAsStream("config.properties"));
                properties.load(new FileInputStream(new File(path+"config.properties")));

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        private void createCon(){
            //获取数据库连接
            try {
                connect =
                        DriverManager.getConnection(properties.getProperty("url"),
                                properties.getProperty("user"), properties.getProperty("password"));
            } catch (SQLException e) {
                System.out.println("获取配置的oracle数据库失败");
                e.printStackTrace();
            }

        }

        public Connection getConnect(){

            if(connect == null){
                createCon();
            }
            return connect;
        }






}
