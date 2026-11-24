package swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import swing.db.DatabaseConfig;
import swing.db.DatabaseManager;
import swing.entity.AgentWorkLog;
import swing.entity.User;
import swing.service.AgentWorkLogService;
import swing.service.AuthService;
import swing.service.JobService;
import swing.service.LogService;
import swing.service.UserService;

/**
 * 상담사 전용 로그인 화면
 */
public class LoginFrame extends JFrame {

    private final DatabaseManager dbManager;
    private final AuthService authService;
    private final UserService userService;
    private final JobService jobService;
    private final LogService logService;
    private final AgentWorkLogService agentWorkLogService;

    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JLabel dbStatusLabel;
    private boolean dbReady;

    public LoginFrame(DatabaseManager dbManager,
                      AuthService authService,
                      UserService userService,
                      JobService jobService,
                      LogService logService,
                      AgentWorkLogService agentWorkLogService,
                      boolean dbReady,
                      String initialDbError) {
        super("PIMS 고객상담 정보시스템 - 상담사 로그인");
        this.dbManager = dbManager;
        this.authService = authService;
        this.userService = userService;
        this.jobService = jobService;
        this.logService = logService;
        this.agentWorkLogService = agentWorkLogService;
        this.dbReady = dbReady;

        this.emailField = new JTextField("chelsea@pims.com", 22);
        this.passwordField = new JPasswordField("1234", 22);
        this.dbStatusLabel = new JLabel();

        initUI();
        refreshDbStatusLabel(initialDbError);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(470, 390);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));
        root.setBackground(UiTheme.SURFACE);

        // 상단 타이틀
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("PIMS 고객상담 회원정보시스템", SwingConstants.CENTER);
        title.setFont(UiTheme.FONT_TITLE);
        title.setForeground(UiTheme.HEADER_BG);
        JLabel subtitle = new JLabel("상담사 계정(Role.상담사)으로 로그인하세요", SwingConstants.CENTER);
        subtitle.setForeground(UiTheme.MUTED_TEXT);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // 중앙 로그인 폼
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formCard.add(new JLabel("이메일:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formCard.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formCard.add(new JLabel("비밀번호:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formCard.add(passwordField, gbc);

        JButton loginBtn = new JButton("상담사 로그인");
        loginBtn.setFont(UiTheme.FONT_BOLD);
        loginBtn.addActionListener(e -> handleLogin());
        getRootPane().setDefaultButton(loginBtn);

        JButton dbConfigBtn = new JButton("DB 연결 설정");
        dbConfigBtn.addActionListener(e -> openDbConfigDialog());

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionRow.setOpaque(false);
        actionRow.add(dbConfigBtn);
        actionRow.add(loginBtn);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formCard.add(actionRow, gbc);

        // 테스트 계정 안내 (GridBagLayout 내 HTML 줄바꿈 깨짐 방지를 위해 일반 JLabel 사용)
        JPanel hintBox = new JPanel(new java.awt.GridLayout(3, 1, 0, 2));
        hintBox.setOpaque(false);
        JLabel h1 = new JLabel("테스트 상담사 계정 (비밀번호: 1234)");
        JLabel h2 = new JLabel("• 첼시: chelsea@pims.com  |  미키: mickey@pims.com  |  루시: lucy@pims.com");
        JLabel h3 = new JLabel("• 일반 회원(john@example.com)은 상담사 권한 체크로 로그인 차단");
        h1.setForeground(UiTheme.MUTED_TEXT);
        h2.setForeground(UiTheme.MUTED_TEXT);
        h3.setForeground(UiTheme.MUTED_TEXT);
        hintBox.add(h1);
        hintBox.add(h2);
        hintBox.add(h3);

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 6, 0, 6);
        formCard.add(hintBox, gbc);

        root.add(formCard, BorderLayout.CENTER);

        // 하단 DB 상태 바
        dbStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dbStatusLabel.setFont(UiTheme.FONT_BOLD);
        root.add(dbStatusLabel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void refreshDbStatusLabel(String errorMessage) {
        DatabaseConfig cfg = dbManager.getConfig();
        if (dbReady) {
            dbStatusLabel.setForeground(UiTheme.SUCCESS);
            dbStatusLabel.setText("● MySQL 연결 완료 (" + cfg.getHost() + ":" + cfg.getPort() + "/" + cfg.getDbName() + ")");
        } else {
            dbStatusLabel.setForeground(UiTheme.DANGER);
            String shortErr = (errorMessage != null && !errorMessage.isBlank())
                    ? " — [DB 연결 설정]에서 비밀번호를 확인하세요"
                    : " — [DB 연결 설정]을 눌러 연결하세요";
            dbStatusLabel.setText("● MySQL 미연결" + shortErr);
        }
    }

    private void openDbConfigDialog() {
        DbConfigDialog dialog = new DbConfigDialog(this, dbManager);
        dialog.setVisible(true);
        if (dialog.isSavedAndConnected()) {
            this.dbReady = true;
            refreshDbStatusLabel(null);
        }
    }

    private void handleLogin() {
        if (!dbReady) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "MySQL DB에 아직 연결되지 않았습니다.\n지금 [DB 연결 설정] 창을 열까요?",
                    "DB 연결 필요", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                openDbConfigDialog();
            }
            return;
        }

        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            User agent = authService.login(email, password);
            AgentWorkLog loginRecord = agentWorkLogService.recordLogin(agent);
            MainConsoleFrame console = new MainConsoleFrame(
                    agent, dbManager, authService, userService, jobService, logService, agentWorkLogService, loginRecord);
            console.setVisible(true);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "로그인 실패", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "로그인 처리 중 DB 오류가 발생했습니다:\n" + ex.getMessage(),
                    "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
