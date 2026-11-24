package swing.db;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * MySQL 8.0 JDBC 연결 관리 및 스키마/시드 데이터 자동 초기화 클래스
 */
public class DatabaseManager {

    private static volatile boolean driverLoaded = false;
    @SuppressWarnings("unused")
    private static URLClassLoader fallbackDriverClassLoader;

    private DatabaseConfig config;

    public DatabaseManager(DatabaseConfig config) {
        this.config = config;
        loadDriver();
    }

    private synchronized void loadDriver() {
        if (driverLoaded) {
            return;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
            return;
        } catch (ClassNotFoundException ignored) {
            // IDE Run 버튼 실행 시 lib JAR가 -cp에 아직 반영되지 않은 경우 lib/ 폴더에서 자동 로드 시도
        }

        Path libDir = Paths.get("lib");
        if (Files.isDirectory(libDir)) {
            try (Stream<Path> files = Files.list(libDir)) {
                Path jarPath = files
                        .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".jar"))
                        .filter(p -> p.getFileName().toString().toLowerCase().contains("mysql-connector"))
                        .findFirst()
                        .orElse(null);

                if (jarPath != null) {
                    URLClassLoader loader = new URLClassLoader(
                            new URL[]{jarPath.toUri().toURL()},
                            DatabaseManager.class.getClassLoader());
                    Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.Driver", true, loader);
                    Driver realDriver = (Driver) driverClass.getDeclaredConstructor().newInstance();
                    DriverManager.registerDriver(new DriverShim(realDriver));
                    fallbackDriverClassLoader = loader;
                    driverLoaded = true;
                    System.out.println("[DB] lib 폴더에서 MySQL JDBC 드라이버 자동 로드 완료: " + jarPath.toAbsolutePath());
                    return;
                }
            } catch (Exception e) {
                throw new IllegalStateException(
                        "lib 폴더의 MySQL JDBC 드라이버 자동 로드 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }

        throw new IllegalStateException(
                "MySQL JDBC 드라이버(com.mysql.cj.jdbc.Driver)를 찾을 수 없습니다. "
                        + "lib/mysql-connector-j-8.4.0.jar 파일이 존재하는지 확인하세요.");
    }

    /**
     * URLClassLoader로 동적 로드된 JDBC Driver를 DriverManager가 인식할 수 있도록 감싸는 래퍼
     */
    private static final class DriverShim implements Driver {
        private final Driver delegate;

        DriverShim(Driver delegate) {
            this.delegate = delegate;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return delegate.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return delegate.acceptsURL(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return delegate.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return delegate.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return delegate.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return delegate.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return delegate.getParentLogger();
        }
    }

    public DatabaseConfig getConfig() {
        return config;
    }

    public void updateConfig(DatabaseConfig newConfig) {
        this.config = newConfig;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getDatabaseUrl(), config.getUser(), config.getPassword());
    }

    /**
     * 주어진 설정으로 연결을 테스트합니다. 성공하면 null, 실패하면 오류 메시지를 반환합니다.
     */
    public String testConnection(DatabaseConfig candidate) {
        String dbName = sanitizeIdentifier(candidate.getDbName());
        if (dbName.isEmpty()) {
            return "DB 이름이 올바르지 않습니다. 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.";
        }
        try (Connection conn = DriverManager.getConnection(
                candidate.getServerUrl(), candidate.getUser(), candidate.getPassword())) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT 1");
            }
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /**
     * 데이터베이스(pims_tdd), 테이블(roles, users, jobs, logs), 초기 시드 데이터를 자동 생성합니다.
     */
    public void initializeDatabase() throws SQLException {
        String safeDbName = sanitizeIdentifier(config.getDbName());
        if (safeDbName.isEmpty()) {
            throw new SQLException("유효하지 않은 데이터베이스 이름입니다: " + config.getDbName());
        }

        // 1. 데이터베이스 생성 (없을 경우)
        try (Connection serverConn = DriverManager.getConnection(
                config.getServerUrl(), config.getUser(), config.getPassword());
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + safeDbName + "` "
                    + "DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci");
        }

        // 2. 테이블 및 시드 데이터 초기화
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS roles ("
                            + "role_name VARCHAR(20) PRIMARY KEY, "
                            + "description VARCHAR(100) NOT NULL"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users ("
                            + "id INT AUTO_INCREMENT PRIMARY KEY, "
                            + "name VARCHAR(100) NOT NULL, "
                            + "email VARCHAR(150) NOT NULL UNIQUE, "
                            + "password VARCHAR(255) NOT NULL, "
                            + "role VARCHAR(20) NOT NULL, "
                            + "phone_number VARCHAR(30) NULL, "
                            + "birth_day VARCHAR(20) NULL, "
                            + "address VARCHAR(255) NULL, "
                            + "status VARCHAR(30) NULL, "
                            + "CONSTRAINT fk_users_role FOREIGN KEY (role) REFERENCES roles(role_name)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS jobs ("
                            + "id INT AUTO_INCREMENT PRIMARY KEY, "
                            + "job_name VARCHAR(100) NOT NULL, "
                            + "description VARCHAR(255) NULL, "
                            + "status VARCHAR(30) NOT NULL, "
                            + "start_date_time DATETIME NOT NULL, "
                            + "end_date_time DATETIME NULL, "
                            + "caller_phone_number VARCHAR(30) NULL, "
                            + "user_id INT NULL, "
                            + "agent_id INT NOT NULL, "
                            + "target_agent_id INT NULL, "
                            + "CONSTRAINT fk_jobs_user FOREIGN KEY (user_id) REFERENCES users(id), "
                            + "CONSTRAINT fk_jobs_agent FOREIGN KEY (agent_id) REFERENCES users(id), "
                            + "CONSTRAINT fk_jobs_target_agent FOREIGN KEY (target_agent_id) REFERENCES users(id)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS logs ("
                            + "id INT AUTO_INCREMENT PRIMARY KEY, "
                            + "job_id INT NULL, "
                            + "agent_id INT NULL, "
                            + "user_id INT NULL, "
                            + "log_name VARCHAR(100) NOT NULL, "
                            + "log_description VARCHAR(500) NULL, "
                            + "created_at DATETIME NOT NULL, "
                            + "CONSTRAINT fk_logs_job FOREIGN KEY (job_id) REFERENCES jobs(id), "
                            + "CONSTRAINT fk_logs_agent FOREIGN KEY (agent_id) REFERENCES users(id), "
                            + "CONSTRAINT fk_logs_user FOREIGN KEY (user_id) REFERENCES users(id)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            // 기존에 NOT NULL로 생성된 logs 테이블도 임의검색(NULL) 저장이 가능하도록 마이그레이션
            stmt.executeUpdate("ALTER TABLE logs MODIFY COLUMN job_id INT NULL");
            stmt.executeUpdate("ALTER TABLE logs MODIFY COLUMN agent_id INT NULL");

            // 5. 상담사 업무기록 테이블 (로그인, 로그아웃, 업무중, 자리비움)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS agent_work_logs ("
                            + "id INT AUTO_INCREMENT PRIMARY KEY, "
                            + "agent_id INT NOT NULL, "
                            + "category VARCHAR(50) NOT NULL, "
                            + "detail VARCHAR(50) NOT NULL, "
                            + "recorded_at DATETIME NOT NULL, "
                            + "CONSTRAINT fk_agent_work_logs_agent FOREIGN KEY (agent_id) REFERENCES users(id)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            // 역할 시드
            stmt.executeUpdate(
                    "INSERT IGNORE INTO roles (role_name, description) VALUES "
                            + "('상담사', '고객센터(CS) 상담사'), "
                            + "('사용자', '등록된 일반 회원'), "
                            + "('일반', '비회원 또는 일반 사용자')");

            // 상담사 시드 (비밀번호: 1234) — 업무 중 / 자리 비움 / 업무중: 전화 3가지 상태 포함
            stmt.executeUpdate(
                    "INSERT IGNORE INTO users (name, email, password, role, phone_number, birth_day, address, status) VALUES "
                            + "('첼시', 'chelsea@pims.com', '1234', '상담사', '02-1000-0001', '950310', '서울시 중구 고객센터', '업무 중'), "
                            + "('미키', 'mickey@pims.com', '1234', '상담사', '02-1000-0002', '930822', '서울시 중구 고객센터', '자리 비움'), "
                            + "('루시', 'lucy@pims.com', '1234', '상담사', '02-1000-0003', '970105', '서울시 중구 고객센터', '업무 중'), "
                            + "('제니', 'jenny@pims.com', '1234', '상담사', '02-1000-0004', '960618', '서울시 중구 고객센터', '업무중: 전화')");

            // 회원 시드
            stmt.executeUpdate(
                    "INSERT IGNORE INTO users (name, email, password, role, phone_number, birth_day, address, status) VALUES "
                            + "('John Doe', 'john@example.com', 'pass1234', '사용자', '010-user-xxxx', '99011x', '부산시 해운대구 xx동', '정상'), "
                            + "('김철수', 'chulsoo@example.com', 'pass1234', '사용자', '010-1234-5678', '920515', '서울시 강남구 역삼동 101호', '정상'), "
                            + "('이영희', 'younghee@example.com', 'pass1234', '사용자', '010-2222-3333', '951103', '부산시 수영구 광안동 202호', '정상'), "
                            + "('박민수', 'minsoo@example.com', 'pass1234', '사용자', '010-5555-7777', '880720', '대전시 서구 둔산동 303호', '정상')");
        }
    }

    private String sanitizeIdentifier(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        return trimmed.matches("[A-Za-z0-9_]+") ? trimmed : "";
    }
}
