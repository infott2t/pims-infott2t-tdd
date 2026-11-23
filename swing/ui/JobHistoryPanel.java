package swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import swing.entity.Job;
import swing.entity.JobStatus;
import swing.service.JobService;

/**
 * 우측 탭 2: 업무(Job) DB 이력 조회 및 PENDING 업무 완료 처리 패널
 */
public class JobHistoryPanel extends JPanel {

    public interface JobHistoryListener {
        void onViewJobLogs(int jobId);
        void onCompletePendingJob(Job job);
    }

    private final JobService jobService;
    private final JobHistoryListener listener;
    private final JComboBox<String> statusFilterCombo;
    private final DefaultTableModel tableModel;
    private final JTable jobTable;
    private List<Job> displayedJobs = new ArrayList<>();

    public JobHistoryPanel(JobService jobService, JobHistoryListener listener) {
        super(new BorderLayout(8, 8));
        this.jobService = jobService;
        this.listener = listener;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(Color.WHITE);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.setOpaque(false);

        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "INIT", "IN_PROGRESS", "PENDING", "COMPLETED"});
        JButton refreshBtn = new JButton("새로고침");
        JButton viewLogsBtn = new JButton("선택 업무 로그 보기");
        JButton completePendingBtn = new JButton("선택 PENDING 업무 완료 처리");

        statusFilterCombo.addActionListener(e -> refreshJobs());
        refreshBtn.addActionListener(e -> refreshJobs());
        viewLogsBtn.addActionListener(e -> {
            Job selected = getSelectedJob();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "조회할 업무(Job) 행을 먼저 선택하세요.");
                return;
            }
            listener.onViewJobLogs(selected.getId());
        });
        completePendingBtn.addActionListener(e -> {
            Job selected = getSelectedJob();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "완료 처리할 업무(Job) 행을 먼저 선택하세요.");
                return;
            }
            if (selected.getStatus() != JobStatus.PENDING) {
                JOptionPane.showMessageDialog(this, "PENDING 상태인 업무만 완료 처리할 수 있습니다.");
                return;
            }
            listener.onCompletePendingJob(selected);
        });

        topBar.add(new JLabel("상태 필터:"));
        topBar.add(statusFilterCombo);
        topBar.add(refreshBtn);
        topBar.add(viewLogsBtn);
        topBar.add(completePendingBtn);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"Job ID", "업무명", "발신번호", "회원", "담당상담사", "연결상담사", "설명", "상태", "시작시각", "종료시각"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jobTable = new JTable(tableModel);
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jobTable.setRowHeight(24);
        add(new JScrollPane(jobTable), BorderLayout.CENTER);

        refreshJobs();
    }

    private Job getSelectedJob() {
        int row = jobTable.getSelectedRow();
        if (row >= 0 && row < displayedJobs.size()) {
            return displayedJobs.get(row);
        }
        return null;
    }

    public void refreshJobs() {
        try {
            String filter = (String) statusFilterCombo.getSelectedItem();
            if ("ALL".equals(filter) || filter == null) {
                displayedJobs = jobService.findAllJobs();
            } else {
                displayedJobs = jobService.findJobsByStatus(JobStatus.fromString(filter));
            }
            tableModel.setRowCount(0);
            for (Job j : displayedJobs) {
                tableModel.addRow(new Object[]{
                        j.getId(),
                        j.getJobName(),
                        j.getCallerPhoneNumber() != null ? j.getCallerPhoneNumber() : "-",
                        j.getUserName() != null ? j.getUserName() : "비회원",
                        j.getAgentName() != null ? j.getAgentName() : ("#" + j.getAgentId()),
                        j.getTargetAgentName() != null ? j.getTargetAgentName() : "-",
                        j.getDescription() != null ? j.getDescription() : "-",
                        j.getStatus().name(),
                        j.getFormattedStartDateTime(),
                        j.getFormattedEndDateTime()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "업무 목록 조회 오류:\n" + ex.getMessage(),
                    "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
