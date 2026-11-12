package garaman.dao;

import garaman.exception.DataAccessException6;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class DAO6 implements AutoCloseable {
    protected final Connection con;
    protected final boolean owner;
    public DAO6() {
        Connection opened = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            opened = DriverManager.getConnection(setting("GARAMAN_DB_URL",
                "jdbc:mysql://127.0.0.1:3306/garaman6?connectionTimeZone=Asia/Ho_Chi_Minh&forceConnectionTimeZoneToSession=false&characterEncoding=UTF-8"),
                setting("GARAMAN_DB_USER", "garaman_app"), setting("GARAMAN_DB_PASSWORD", ""));
            try (var s = opened.createStatement()) { s.execute("SET time_zone = '+07:00'"); }
            verifyTestConnection(opened);
            con = opened;
            owner = true;
        } catch (SQLException | ClassNotFoundException e) {
            if (opened != null) try { opened.close(); } catch (SQLException closeError) { e.addSuppressed(closeError); }
            throw new DataAccessException6("Không thể kết nối CSDL", e);
        }
    }
    public DAO6(Connection con) {
        this.con = java.util.Objects.requireNonNull(con); owner = false;
        try{verifyTestConnection(con);}catch(SQLException e){throw new DataAccessException6("Từ chối kết nối ngoài schema test",e);}
    }
    private static void verifyTestConnection(Connection connection) throws SQLException {
        if(!"true".equals(setting("GARAMAN_TEST_MODE","false")))return;
        String expected=setting("GARAMAN_TEST_DATABASE","garaman6_v3_it");
        try(var ps=connection.prepareStatement("SELECT DATABASE()");var rs=ps.executeQuery()){
            rs.next();String actual=rs.getString(1);
            if(!expected.matches("garaman6[a-zA-Z0-9_]*_it")||!expected.equals(actual))throw new SQLException("Refuse writes outside the configured test schema");
        }
    }
    public Map<String,String> runtimeInfo() {
        try(var ps=con.prepareStatement("SELECT DATABASE(),@@session.time_zone,VERSION()");var rs=ps.executeQuery()){
            rs.next();return Map.of("database",rs.getString(1),"timeZone",rs.getString(2),"serverVersion",rs.getString(3));
        }catch(SQLException e){throw new DataAccessException6("Không đọc được thông tin runtime",e);}
    }
    @Override public void close() {
        if (owner) try { con.close(); } catch (SQLException e) { throw new DataAccessException6("Không đóng được kết nối", e); }
    }

    private static final Properties LOCAL=load();
    private static Properties load() {
        Properties p = new Properties();
        String file = System.getProperty("garaman.config", System.getenv().getOrDefault("GARAMAN_CONFIG", ".local/garaman.properties"));
        if (Files.isRegularFile(Path.of(file))) {
            try (Reader reader = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) { p.load(reader); }
            catch (IOException e) { throw new IllegalStateException("Không đọc được cấu hình local", e); }
        }
        return p;
    }
    public static String setting(String key, String fallback) {
        return System.getProperty(key, System.getenv().getOrDefault(key, LOCAL.getProperty(key, fallback)));
    }
    public static Integer number(String key, int fallback) { return Integer.parseInt(setting(key, String.valueOf(fallback))); }
    public static ZoneId zone() { return ZoneId.of(setting("GARAMAN_TIME_ZONE", "Asia/Ho_Chi_Minh")); }
}
