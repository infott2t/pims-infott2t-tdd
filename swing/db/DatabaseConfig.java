package swing.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * MySQL 8.0 접속 정보 관리 클래스
 * 기본 위치: swing/db/db.properties (환경변수 PIMS_DB_* 로도 오버라이드 가능)
 */
public class DatabaseConfig {

    public static final Path DEFAULT_CONFIG_PATH = Paths.get("swing", "db", "db.properties");

    private String host = "localhost";
    private String port = "3306";
    private String dbName = "pims_tdd";
    private String user = "root";
    private String password = "";

    public DatabaseConfig() {
    }

    public DatabaseConfig(String host, String port, String dbName, String user, String password) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.user = user;
        this.password = password;
    }

    public static DatabaseConfig load() {
        DatabaseConfig config = new DatabaseConfig();
        if (Files.exists(DEFAULT_CONFIG_PATH)) {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(DEFAULT_CONFIG_PATH)) {
                props.load(in);
                config.host = props.getProperty("db.host", config.host).trim();
                config.port = props.getProperty("db.port", config.port).trim();
                config.dbName = props.getProperty("db.name", config.dbName).trim();
                config.user = props.getProperty("db.user", config.user).trim();
                config.password = props.getProperty("db.password", config.password);
            } catch (IOException ignored) {
                // 기본값 사용
            }
        }
        // 환경변수가 설정된 경우 우선 적용
        String envHost = System.getenv("PIMS_DB_HOST");
        String envPort = System.getenv("PIMS_DB_PORT");
        String envName = System.getenv("PIMS_DB_NAME");
        String envUser = System.getenv("PIMS_DB_USER");
        String envPass = System.getenv("PIMS_DB_PASSWORD");
        if (envHost != null && !envHost.isBlank()) config.host = envHost.trim();
        if (envPort != null && !envPort.isBlank()) config.port = envPort.trim();
        if (envName != null && !envName.isBlank()) config.dbName = envName.trim();
        if (envUser != null && !envUser.isBlank()) config.user = envUser.trim();
        if (envPass != null) config.password = envPass;

        return config;
    }

    public void save() throws IOException {
        if (DEFAULT_CONFIG_PATH.getParent() != null) {
            Files.createDirectories(DEFAULT_CONFIG_PATH.getParent());
        }
        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", port);
        props.setProperty("db.name", dbName);
        props.setProperty("db.user", user);
        props.setProperty("db.password", password != null ? password : "");
        try (OutputStream out = Files.newOutputStream(DEFAULT_CONFIG_PATH)) {
            props.store(out, "PIMS Swing MySQL 8.0 Connection Settings");
        }
    }

    public String getServerUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/"
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
    }

    public String getDatabaseUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
