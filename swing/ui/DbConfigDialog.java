package swing.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import swing.db.DatabaseConfig;
import swing.db.DatabaseManager;

/**
 * MySQL 8.0 접속 정보를 GUI에서 입력·테스트·저장하는 다이얼로그
 */
public class DbConfigDialog extends JDialog {

    private final DatabaseManager dbManager;
    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField dbNameField;
    private final JTextField userField;
    private final JPasswordField passwordField;
    private boolean savedAndConnected = false;

    public DbConfigDialog(Frame owner, DatabaseManager dbManager) {
        super(owner, "MySQL DB 연결 설정", true);
        this.dbManager = dbManager;

        DatabaseConfig current = dbManager.getConfig();
        this.hostField = new JTextField(current.getHost(), 20);
        this.portField = new JTextField(current.getPort(), 20);
        this.dbNameField = new JTextField(current.getDbName(), 20);
        this.userField = new JTextField(current.getUser(), 20);
        this.passwordField = new JPasswordField(current.getPassword(), 20);

        initUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("로컬 MySQL 8.0 접속 정보를 입력하세요");
        title.setFont(UiTheme.FONT_SUBTITLE);
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        addRow(form, gbc, 0, "호스트 (Host):", hostField);
        addRow(form, gbc, 1, "포트 (Port):", portField);
        addRow(form, gbc, 2, "DB 이름:", dbNameField);
        addRow(form, gbc, 3, "사용자 (User):", userField);
        addRow(form, gbc, 4, "비밀번호 (Password):", passwordField);

        root.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton testBtn = new JButton("연결 테스트");
        JButton saveBtn = new JButton("저장 및 DB 초기화");
        JButton cancelBtn = new JButton("취소");

        testBtn.addActionListener(e -> handleTestConnection());
        saveBtn.addActionListener(e -> handleSaveAndInit());
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(testBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private DatabaseConfig buildCandidateConfig() {
        return new DatabaseConfig(
                hostField.getText().trim(),
                portField.getText().trim(),
                dbNameField.getText().trim(),
                userField.getText().trim(),
                new String(passwordField.getPassword())
        );
    }

    private void handleTestConnection() {
        DatabaseConfig candidate = buildCandidateConfig();
        String err = dbManager.testConnection(candidate);
        if (err == null) {
            JOptionPane.showMessageDialog(this,
                    "MySQL 서버 연결에 성공했습니다!",
                    "연결 성공", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "MySQL 연결 실패:\n" + err,
                    "연결 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSaveAndInit() {
        DatabaseConfig candidate = buildCandidateConfig();
        String err = dbManager.testConnection(candidate);
        if (err != null) {
            JOptionPane.showMessageDialog(this,
                    "MySQL 연결에 실패하여 저장할 수 없습니다:\n" + err,
                    "연결 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            dbManager.updateConfig(candidate);
            dbManager.initializeDatabase();
            candidate.save();
            this.savedAndConnected = true;
            JOptionPane.showMessageDialog(this,
                    "DB 연결 저장 및 테이블/시드 데이터 초기화가 완료되었습니다.\n"
                            + "DB: " + candidate.getHost() + ":" + candidate.getPort() + "/" + candidate.getDbName(),
                    "초기화 완료", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "DB 초기화 또는 설정 저장 중 오류가 발생했습니다:\n" + ex.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSavedAndConnected() {
        return savedAndConnected;
    }
}
