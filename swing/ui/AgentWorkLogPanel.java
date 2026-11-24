package swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import swing.entity.AgentWorkLog;
import swing.entity.User;
import swing.service.AgentWorkLogService;

/**
 * 우측 탭 4: 현재 로그인한 상담사의 업무기록(로그인, 로그아웃, 업무중, 자리비움) 조회 패널
 */
public class AgentWorkLogPanel extends JPanel {

    private final User currentAgent;
    private final AgentWorkLogService workLogService;
    private final JLabel summaryLabel;
    private final JComboBox<String> detailFilterCombo;
    private final DefaultTableModel tableModel;

    public AgentWorkLogPanel(User currentAgent, AgentWorkLogService workLogService) {
        super(new BorderLayout(8, 8));
        this.currentAgent = currentAgent;
        this.workLogService = workLogService;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout(8, 4));
        topPanel.setOpaque(false);

        summaryLabel = new JLabel();
        summaryLabel.setFont(UiTheme.FONT_BOLD);
        summaryLabel.setForeground(UiTheme.PRIMARY_DARK);
        topPanel.add(summaryLabel, BorderLayout.WEST);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        filterBar.setOpaque(false);

        detailFilterCombo = new JComboBox<>(new String[]{"전체", "로그인", "로그아웃", "업무중", "업무중: 전화", "자리비움"});
        JButton refreshBtn = new JButton("새로고침");

        detailFilterCombo.addActionListener(e -> refreshWorkLogs());
        refreshBtn.addActionListener(e -> refreshWorkLogs());

        filterBar.add(new JLabel("상세 내용 필터:"));
        filterBar.add(detailFilterCombo);
        filterBar.add(refreshBtn);
        topPanel.add(filterBar, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "담당상담사", "항목", "상세 내용", "기록시각"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshWorkLogs();
    }

    public void refreshWorkLogs() {
        try {
            String filter = (String) detailFilterCombo.getSelectedItem();
            List<AgentWorkLog> logs = workLogService.findMyWorkLogsByDetail(currentAgent.getId(), filter);
            tableModel.setRowCount(0);
            for (AgentWorkLog log : logs) {
                String agentName = (log.getAgentName() != null) ? log.getAgentName() : currentAgent.getName();
                tableModel.addRow(new Object[]{
                        log.getId(),
                        agentName,
                        log.getCategory(),
                        log.getDetail(),
                        log.getFormattedRecordedAt()
                });
            }
            summaryLabel.setText("로그인 상담사: " + currentAgent.getName()
                    + "  |  현재 상태: " + (currentAgent.getStatus() != null ? currentAgent.getStatus() : "-")
                    + "  |  조회 건수: " + logs.size() + "건");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "상담사 업무기록 조회 오류:\n" + ex.getMessage(),
                    "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
