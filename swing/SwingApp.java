package swing;

import javax.swing.SwingUtilities;

import swing.db.DatabaseConfig;
import swing.db.DatabaseManager;
import swing.repository.AgentWorkLogRepository;
import swing.repository.JobRepository;
import swing.repository.LogRepository;
import swing.repository.UserRepository;
import swing.repository.impl.JdbcAgentWorkLogRepository;
import swing.repository.impl.JdbcJobRepository;
import swing.repository.impl.JdbcLogRepository;
import swing.repository.impl.JdbcUserRepository;
import swing.service.AgentWorkLogService;
import swing.service.AuthService;
import swing.service.JobService;
import swing.service.LogService;
import swing.service.UserService;
import swing.ui.LoginFrame;
import swing.ui.UiTheme;

/**
 * PIMS Swing 고객상담 회원정보시스템 실행 진입점
 */
public class SwingApp {

    public static void main(String[] args) {
        UiTheme.applyGlobalLookAndFeel();

        DatabaseConfig config = DatabaseConfig.load();
        DatabaseManager dbManager = new DatabaseManager(config);

        boolean dbReady = false;
        String initialDbError = null;
        try {
            dbManager.initializeDatabase();
            dbReady = true;
            System.out.println("[DB] MySQL 초기화 완료: " + config.getHost() + ":" + config.getPort() + "/" + config.getDbName());
        } catch (Exception e) {
            initialDbError = e.getMessage();
            System.err.println("[DB] 초기 연결 실패 (로그인 화면의 [DB 연결 설정]에서 접속 정보를 입력하세요): " + initialDbError);
        }

        // Repository & Service 의존성 조립 (DI)
        UserRepository userRepository = new JdbcUserRepository(dbManager);
        JobRepository jobRepository = new JdbcJobRepository(dbManager);
        LogRepository logRepository = new JdbcLogRepository(dbManager);
        AgentWorkLogRepository agentWorkLogRepository = new JdbcAgentWorkLogRepository(dbManager);

        AuthService authService = new AuthService(userRepository);
        UserService userService = new UserService(userRepository);
        JobService jobService = new JobService(jobRepository);
        LogService logService = new LogService(logRepository);
        AgentWorkLogService agentWorkLogService = new AgentWorkLogService(agentWorkLogRepository);

        final boolean ready = dbReady;
        final String err = initialDbError;

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(
                    dbManager, authService, userService, jobService, logService, agentWorkLogService, ready, err);
            loginFrame.setVisible(true);
        });
    }
}
