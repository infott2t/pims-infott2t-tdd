package swing.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import swing.entity.Log;
import swing.service.LogService;

/**
 * 우측 탭 3: 로그(Log) DB 이력 조회 및 검색 패널 (관리자 전용 — 가상 시나리오 모드)
 */
public class LogHistoryPanel extends JPanel {

    private static final String CARD_LOCKED = "LOCKED";
    private static final String CARD_CONTENT = "CONTENT";

    private final LogService logService;
    private final CardLayout cardLayout;
    private final JTextField jobIdField;
    private final JTextField keywordField;
    private final DefaultTableModel tableModel;

    public LogHistoryPanel(LogService logService) {
        this.logService = logService;
        this.cardLayout = new CardLayout();
        setLayout(cardLayout);
        setBackground(Color.WHITE);

        // 1. 잠금(비노출) 카드 — 일반적으로 보이지 않도록 기본 상태
        JPanel lockedPanel = new JPanel(new BorderLayout());
        lockedPanel.setBackground(UiTheme.SURFACE);
        JLabel lockedLabel = new JLabel(
                "관리자 전용 영역입니다. (가상 시나리오 확인 후 열람 가능)",
                SwingConstants.CENTER);
        lockedLabel.setFont(UiTheme.FONT_SUBTITLE);
        lockedLabel.setForeground(UiTheme.MUTED_TEXT);
        lockedPanel.add(lockedLabel, BorderLayout.CENTER);
        add(lockedPanel, CARD_LOCKED);

        // 2. 실제 로그 DB 조회 카드
        JPanel contentPanel = new JPanel(new BorderLayout(8, 8));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        contentPanel.setBackground(Color.WHITE);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.setOpaque(false);

        jobIdField = new JTextField(6);
        keywordField = new JTextField(16);
        JButton searchJobBtn = new JButton("Job ID 조회");
        JButton searchKeywordBtn = new JButton("키워드 검색");
        JButton showAllBtn = new JButton("전체 새로고침");

        searchJobBtn.addActionListener(e -> filterByJobIdInput());
        searchKeywordBtn.addActionListener(e -> searchByKeyword());
        keywordField.addActionListener(e -> searchByKeyword());
        showAllBtn.addActionListener(e -> {
            jobIdField.setText("");
            keywordField.setText("");
            refreshLogs();
        });

        topBar.add(new JLabel("Job ID:"));
        topBar.add(jobIdField);
        topBar.add(searchJobBtn);
        topBar.add(new JLabel("항목/내용:"));
        topBar.add(keywordField);
        topBar.add(searchKeywordBtn);
        topBar.add(showAllBtn);
        contentPanel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Log ID", "Job ID", "담당상담사", "회원", "항목 (logName)", "상세 내용 (logDescription)", "기록시각"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable logTable = new JTable(tableModel);
        logTable.setRowHeight(24);
        contentPanel.add(new JScrollPane(logTable), BorderLayout.CENTER);

        add(contentPanel, CARD_CONTENT);

        // 기본적으로 비노출 상태 유지
        cardLayout.show(this, CARD_LOCKED);
    }

    public void setLogsVisible(boolean visible) {
        if (visible) {
            cardLayout.show(this, CARD_CONTENT);
        } else {
            cardLayout.show(this, CARD_LOCKED);
        }
    }

    public void filterByJobId(int jobId) {
        jobIdField.setText(String.valueOf(jobId));
        keywordField.setText("");
        try {
            updateTable(logService.findLogsByJobId(jobId));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "로그 조회 오류:\n" + ex.getMessage(),
                    "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterByJobIdInput() {
        String text = jobIdField.getText().trim();
        if (text.isEmpty()) {
            refreshLogs();
            return;
        }
        try {
            int jobId = Integer.parseInt(text);
            filterByJobId(jobId);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Job ID는 숫자로 입력해 주세요.");
        }
    }

    private void searchByKeyword() {
        try {
            updateTable(logService.searchLogs(keywordField.getText().trim()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "로그 검색 오류:\n" + ex.getMessage(),
                    "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshLogs() {
        try {
            updateTable(logService.findAllLogs());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "로그 목록 조회 오류:\n" + ex.getMessage(),
                    "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Log> logs) {
        tableModel.setRowCount(0);
        for (Log l : logs) {
            tableModel.addRow(new Object[]{
                    l.getId(),
                    l.getJobId(),
                    l.getAgentName() != null ? l.getAgentName() : ("#" + l.getAgentId()),
                    l.getUserName() != null ? l.getUserName() : "비회원",
                    l.getLogName(),
                    l.getLogDescription(),
                    l.getFormattedCreatedAt()
            });
        }
    }
}
