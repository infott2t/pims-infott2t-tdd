package swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import swing.db.DatabaseManager;
import swing.entity.Job;
import swing.entity.JobStatus;
import swing.entity.Log;
import swing.entity.User;
import swing.service.AuthService;
import swing.service.JobService;
import swing.service.LogService;
import swing.service.UserService;

/**
 * 메인 상담사 업무 콘솔 창
 * 상단(상담사 상태/동료 현황) + 좌측(전화 수신/대화 타임라인) + 우측(회원검색·시나리오1~4 / Job DB / Log DB)
 */
public class MainConsoleFrame extends JFrame
        implements IncomingCallPanel.CallActionListener,
        ConsultationPanel.ScenarioActionListener,
        JobHistoryPanel.JobHistoryListener {

    private static final String CENTER_NUMBER = "x23-4567-8901";

    private final User currentAgent;
    private final DatabaseManager dbManager;
    private final AuthService authService;
    private final UserService userService;
    private final JobService jobService;
    private final LogService logService;

    private final JLabel peerAgentsLabel;
    private final IncomingCallPanel callPanel;
    private final ConsultationPanel consultationPanel;
    private final JobHistoryPanel jobHistoryPanel;
    private final LogHistoryPanel logHistoryPanel;
    private final JTabbedPane rightTabs;

    // 현재 진행 중인 통화/업무 세션 상태
    private Job activeJob;
    private User activeCustomer;
    private String activeCallerPhone;
    private boolean skipNextLogAutoRefresh = false;

    public MainConsoleFrame(User currentAgent,
                            DatabaseManager dbManager,
                            AuthService authService,
                            UserService userService,
                            JobService jobService,
                            LogService logService) {
        super("PIMS 고객상담 회원정보시스템 — 상담사: " + currentAgent.getName());
        this.currentAgent = currentAgent;
        this.dbManager = dbManager;
        this.authService = authService;
        this.userService = userService;
        this.jobService = jobService;
        this.logService = logService;

        this.peerAgentsLabel = new JLabel();
        this.callPanel = new IncomingCallPanel(this);
        this.consultationPanel = new ConsultationPanel(userService, this);
        this.jobHistoryPanel = new JobHistoryPanel(jobService, this);
        this.logHistoryPanel = new LogHistoryPanel(logService);
        this.rightTabs = new JTabbedPane();

        initUI();
        refreshAgentsInfo();

        callPanel.appendLine("[" + currentAgent.getRole() + " " + currentAgent.getName() + "] 로그인 완료 (상태: " + currentAgent.getStatus() + ")");
        callPanel.appendLine("좌측에서 [전화 수신(벨 울림)] 버튼을 눌러 상담을 시작하세요.");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());

        // 1. 상단 헤더 바
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(UiTheme.HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel titleLabel = new JLabel("PIMS 고객상담 콘솔  |  상담사: " + currentAgent.getName()
                + " (" + currentAgent.getRole() + ", " + currentAgent.getEmail() + ")");
        titleLabel.setFont(UiTheme.FONT_SUBTITLE);
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        peerAgentsLabel.setForeground(new Color(203, 213, 225));
        rightControls.add(peerAgentsLabel);

        JLabel statusTitle = new JLabel("내 상태:");
        statusTitle.setForeground(Color.WHITE);
        rightControls.add(statusTitle);

        JComboBox<String> myStatusCombo = new JComboBox<>(new String[]{"업무 중", "자리 비움"});
        myStatusCombo.setSelectedItem(currentAgent.getStatus());
        myStatusCombo.addActionListener(e -> {
            String newStatus = (String) myStatusCombo.getSelectedItem();
            if (newStatus != null) {
                authService.updateAgentStatus(currentAgent, newStatus);
                callPanel.appendLine("[상담사 " + currentAgent.getName() + "] 상태 변경 -> " + newStatus);
                refreshAgentsInfo();
            }
        });
        rightControls.add(myStatusCombo);

        JButton logoutBtn = new JButton("로그아웃");
        logoutBtn.addActionListener(e -> handleLogout());
        rightControls.add(logoutBtn);

        header.add(rightControls, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // 2. 중앙 JSplitPane (좌: 통화 제어 및 대화창 / 우: 3개 탭)
        rightTabs.setFont(UiTheme.FONT_BOLD);
        rightTabs.addTab("1. 회원 검색 & 상담 시나리오(1~4)", consultationPanel);
        rightTabs.addTab("2. 업무(Job) DB 조회", jobHistoryPanel);
        rightTabs.addTab("3. 로그(Log) DB 조회", logHistoryPanel);

        // 3번 탭(Log DB 조회) 클릭 시 관리자 전용 가상 시나리오 안내 팝업 표시
        rightTabs.addChangeListener(e -> {
            if (rightTabs.getSelectedIndex() == 2) {
                logHistoryPanel.setLogsVisible(false);
                JOptionPane.showMessageDialog(
                        MainConsoleFrame.this,
                        "가상시나리오로 작동되며, Log DB탭의 경우 관리자의 경우에 열어서 볼 수 있습니다.\n"
                                + "현재는 가상으로 작동되어 볼 수 있습니다.",
                        "관리자 전용 안내 (가상 시나리오)",
                        JOptionPane.INFORMATION_MESSAGE);
                if (!skipNextLogAutoRefresh) {
                    logHistoryPanel.refreshLogs();
                } else {
                    skipNextLogAutoRefresh = false;
                }
                logHistoryPanel.setLogsVisible(true);
            } else {
                logHistoryPanel.setLogsVisible(false);
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, callPanel, rightTabs);
        splitPane.setDividerLocation(420);
        splitPane.setResizeWeight(0.33);
        root.add(splitPane, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void refreshAgentsInfo() {
        List<User> others = authService.getOtherAgents(currentAgent.getId());
        consultationPanel.setOtherAgents(others);
        String summary = others.stream()
                .map(a -> a.getName() + "(" + a.getStatus() + ")")
                .collect(Collectors.joining("  |  "));
        peerAgentsLabel.setText("동료 상담사: " + (summary.isEmpty() ? "없음" : summary));
    }

    private void refreshAllDbViews() {
        jobHistoryPanel.refreshJobs();
        logHistoryPanel.refreshLogs();
        refreshAgentsInfo();
    }

    private boolean ensureActiveInProgressCall() {
        if (activeJob == null) {
            JOptionPane.showMessageDialog(this,
                    "진행 중인 통화가 없습니다.\n좌측에서 [전화 수신(벨 울림)] → [전화 받기]를 먼저 실행해 주세요.",
                    "통화 없음", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (activeJob.getStatus() == JobStatus.INIT) {
            JOptionPane.showMessageDialog(this,
                    "아직 전화를 받지 않았습니다.\n좌측의 [전화 받기] 버튼을 먼저 눌러주세요.",
                    "수신 대기 중", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (activeJob.getStatus() == JobStatus.COMPLETED) {
            JOptionPane.showMessageDialog(this,
                    "이미 종료(COMPLETED)된 상담입니다. 새 전화를 수신해 주세요.",
                    "종료된 상담", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    // ==================== CallActionListener 구현 ====================

    @Override
    public void onCallerPhoneChanged(String callerPhone) {
        // 전화 수신(벨 울림) 버튼 클릭 전에는 자동 검색을 실행하지 않음
    }

    @Override
    public void onSimulateIncomingCall(String callerPhone) {
        if (callerPhone == null || callerPhone.isBlank()) {
            JOptionPane.showMessageDialog(this, "발신 전화번호를 입력해 주세요.");
            return;
        }
        this.activeCallerPhone = callerPhone;
        Optional<User> found = userService.checkPhoneNumber(callerPhone);
        this.activeCustomer = found.orElse(null);

        String callerDisplay = (activeCustomer != null) ? activeCustomer.getName() : ("미등록(" + callerPhone + ")");
        callPanel.appendLine("--------------------------------------------------");
        callPanel.appendLine("[" + callerDisplay + " -> " + CENTER_NUMBER + "] 전화 발신");

        // Job 생성 (INIT)
        this.activeJob = jobService.jobsCallINIT("전화상담", null, "INIT", callerPhone, activeCustomer, currentAgent);
        callPanel.appendLine(activeJob.toString());

        // 회원 조회 결과 처리 및 자동 검색 반영
        consultationPanel.searchByIncomingPhone(callerPhone);
        if (activeCustomer != null) {
            callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + callerPhone + ")] 회원 확인 완료: " + activeCustomer.getName());
            Log log = logService.logsChkUser(currentAgent, activeJob, "회원 전화번호 확인", callerPhone, LocalDateTime.now());
            callPanel.appendLine(log.toString());
        } else {
            callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + callerPhone + ")] 해당 사용자가 없습니다. (비회원 — 단순상담만 가능)");
        }

        callPanel.setRingingState(activeJob, callerPhone);
        refreshAllDbViews();
    }

    @Override
    public void onAnswerCall() {
        if (activeJob == null) {
            return;
        }
        callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + CENTER_NUMBER + ")] 전화를 받았습니다.");
        jobService.setStatus(activeJob, JobStatus.IN_PROGRESS);
        callPanel.appendLine("[JOB #" + activeJob.getId() + " 상태] -> IN_PROGRESS");

        callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + CENTER_NUMBER + ")] 안녕하세요. ㅇㅇㅇ 상담사 "
                + currentAgent.getName() + "입니다. 무엇을 도와드릴까요?");
        String userLabel = (activeCustomer != null) ? activeCustomer.getName() : ("비회원(" + activeCallerPhone + ")");
        callPanel.appendLine("[사용자 " + userLabel + " (" + CENTER_NUMBER + ")] 안녕하세요. 문의사항이 있어서 전화드렸습니다.");

        callPanel.setInCallState(activeJob, activeCallerPhone);
        refreshAllDbViews();
    }

    @Override
    public void onEndCall() {
        if (activeJob == null) {
            callPanel.setIdleState();
            return;
        }
        if (activeJob.getStatus() != JobStatus.COMPLETED && activeJob.getStatus() != JobStatus.PENDING) {
            if (activeJob.getDescription() == null || activeJob.getDescription().isBlank()) {
                jobService.setDescription(activeJob, "상담 종료");
            }
            jobService.setStatus(activeJob, JobStatus.COMPLETED);
            callPanel.appendLine("[JOB #" + activeJob.getId() + " 상태] -> COMPLETED (종료시각: " + activeJob.getFormattedEndDateTime() + ")");
        }
        String callerName = (activeCustomer != null) ? activeCustomer.getName() : activeCallerPhone;
        callPanel.appendLine("[" + callerName + " (" + CENTER_NUMBER + ")] 통화 종료");

        callPanel.setCompletedState(activeJob);
        activeJob = null;
        refreshAllDbViews();
    }

    // ==================== ScenarioActionListener 구현 ====================

    @Override
    public void onCustomerSelected(User customer) {
        // 진행 중인 통화에 회원이 아직 연결되지 않았다면 선택 회원으로 보조 연결 가능
        if (activeJob != null && activeCustomer == null && customer != null) {
            this.activeCustomer = customer;
            jobService.attachCustomer(activeJob, customer);
            callPanel.appendLine("[회원 연결] Job #" + activeJob.getId() + "에 회원 '" + customer.getName() + "' 연결됨");
            refreshAllDbViews();
        }
    }

    @Override
    public void onArbitrarySearchExecuted(String keyword) {
        // Job ID·회원은 비우고(null), 담당상담사는 로그인한 상담사(currentAgent)로 "임의검색" 로그 기록
        Log log = logService.logArbitrarySearch(currentAgent, keyword, LocalDateTime.now());
        callPanel.appendLine(log.toString());
        logHistoryPanel.refreshLogs();
    }

    @Override
    public void onVerifyName(User selectedCustomer, String inputName) {
        if (!ensureActiveInProgressCall()) return;
        User target = (selectedCustomer != null) ? selectedCustomer : activeCustomer;
        if (target == null) {
            JOptionPane.showMessageDialog(this, "비회원은 본인확인 절차를 진행할 수 없습니다. (단순상담만 가능)");
            return;
        }
        boolean ok = userService.verifyName(target, inputName);
        consultationPanel.setNameVerifyResult(ok);

        callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + CENTER_NUMBER + ")] 성함이 어떻게 되시나요?");
        callPanel.appendLine("[사용자 " + target.getName() + " (" + CENTER_NUMBER + ")] " + inputName + "입니다.");

        String resultDetail = inputName + " (" + (ok ? "일치" : "불일치") + ")";
        Log log = logService.logChkName(currentAgent, activeJob, "회원 성함 확인", resultDetail, LocalDateTime.now());
        callPanel.appendLine(log.toString());
        refreshAllDbViews();
    }

    @Override
    public void onVerifyBirthDay(User selectedCustomer, String inputBirthDay) {
        if (!ensureActiveInProgressCall()) return;
        User target = (selectedCustomer != null) ? selectedCustomer : activeCustomer;
        if (target == null) {
            JOptionPane.showMessageDialog(this, "비회원은 본인확인 절차를 진행할 수 없습니다.");
            return;
        }
        boolean ok = userService.verifyBirthDay(target, inputBirthDay);
        consultationPanel.setBirthVerifyResult(ok);

        callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + CENTER_NUMBER + ")] 본인 생년월일이 어떻게 되시나요?");
        callPanel.appendLine("[사용자 " + target.getName() + " (" + CENTER_NUMBER + ")] " + inputBirthDay + "입니다.");

        String resultDetail = inputBirthDay + " (" + (ok ? "일치" : "불일치") + ")";
        Log log = logService.logChkBirthDay(currentAgent, activeJob, "회원 생년월일 확인", resultDetail, LocalDateTime.now());
        callPanel.appendLine(log.toString());
        refreshAllDbViews();
    }

    @Override
    public void onVerifyAddress(User selectedCustomer, String inputAddress) {
        if (!ensureActiveInProgressCall()) return;
        User target = (selectedCustomer != null) ? selectedCustomer : activeCustomer;
        if (target == null) {
            JOptionPane.showMessageDialog(this, "비회원은 본인확인 절차를 진행할 수 없습니다.");
            return;
        }
        boolean ok = userService.verifyAddress(target, inputAddress);
        consultationPanel.setAddressVerifyResult(ok);

        callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + CENTER_NUMBER + ")] 본인 주소가 어떻게 되시나요?");
        callPanel.appendLine("[사용자 " + target.getName() + " (" + CENTER_NUMBER + ")] " + inputAddress + "입니다.");

        String resultDetail = inputAddress + " (" + (ok ? "일치" : "불일치") + ")";
        Log log = logService.chkAddress(currentAgent, activeJob, "회원 주소 확인", resultDetail, LocalDateTime.now());
        callPanel.appendLine(log.toString());
        refreshAllDbViews();
    }

    @Override
    public void onCompleteScenario1(String description) {
        if (!ensureActiveInProgressCall()) return;
        String desc = (description == null || description.isBlank()) ? "본인확인 상담 완료" : description;
        jobService.setDescription(activeJob, desc);
        jobService.setStatus(activeJob, JobStatus.COMPLETED);

        callPanel.appendLine("[JOB #" + activeJob.getId() + " 설명] -> " + desc);
        callPanel.appendLine("[JOB #" + activeJob.getId() + " 상태] -> COMPLETED (종료시각: " + activeJob.getFormattedEndDateTime() + ")");
        String callerName = (activeCustomer != null) ? activeCustomer.getName() : activeCallerPhone;
        callPanel.appendLine("[" + callerName + " (" + CENTER_NUMBER + ")] 통화 종료");

        callPanel.setCompletedState(activeJob);
        activeJob = null;
        refreshAllDbViews();
    }

    @Override
    public void onExecuteScenario2(String anotherNumber) {
        if (!ensureActiveInProgressCall()) return;
        String phone = (anotherNumber == null || anotherNumber.isBlank()) ? "0123-another-number" : anotherNumber;

        callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + CENTER_NUMBER + ")] 이쪽으로 전화부탁드립니다. 번호는 " + phone + "입니다.");
        Log log = logService.logAnotherNumber(currentAgent, activeJob, "타 번호 안내", phone, LocalDateTime.now());
        callPanel.appendLine(log.toString());

        jobService.setDescription(activeJob, "타 번호 안내 완료 (" + phone + ")");
        jobService.setStatus(activeJob, JobStatus.COMPLETED);
        callPanel.appendLine("[JOB #" + activeJob.getId() + " 상태] -> COMPLETED (종료시각: " + activeJob.getFormattedEndDateTime() + ")");

        String callerName = (activeCustomer != null) ? activeCustomer.getName() : activeCallerPhone;
        callPanel.appendLine("[" + callerName + " (" + CENTER_NUMBER + ")] 통화 종료");

        callPanel.setCompletedState(activeJob);
        activeJob = null;
        refreshAllDbViews();
    }

    @Override
    public void onExecuteScenario3(User targetAgent) {
        if (!ensureActiveInProgressCall()) return;
        if (targetAgent == null) {
            JOptionPane.showMessageDialog(this, "연결할 다른 상담사를 선택해 주세요.");
            return;
        }

        callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + CENTER_NUMBER + ")] 그럼, " + targetAgent.getName() + " 상담사로 연결해 드릴게요. 잠시 기다려주세요.");
        jobService.setTargetAgent(activeJob, targetAgent);

        if ("업무 중".equals(targetAgent.getStatus())) {
            Log log = logService.logTransferAgent(currentAgent, activeJob, "상담사 연결 완료",
                    targetAgent.getName() + " (업무 중)", LocalDateTime.now());
            callPanel.appendLine(log.toString());
            jobService.setDescription(activeJob, targetAgent.getName() + " 상담사로 연결 완료");
            jobService.setStatus(activeJob, JobStatus.COMPLETED);
            callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + CENTER_NUMBER + ")] " + targetAgent.getName() + " 상담사에게 정상 인계되었습니다.");
            callPanel.appendLine("[JOB #" + activeJob.getId() + " 상태] -> COMPLETED (인계 완료)");
            callPanel.setCompletedState(activeJob);
            activeJob = null;
        } else {
            callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + CENTER_NUMBER + ")] "
                    + targetAgent.getName() + " 상담사가 현재 '" + targetAgent.getStatus() + "' 상태라 대기(PENDING)로 전환합니다.");
            Log log = logService.logTransferAgent(currentAgent, activeJob, "상담사 연결 대기",
                    targetAgent.getName() + " (" + targetAgent.getStatus() + ")", LocalDateTime.now());
            callPanel.appendLine(log.toString());
            jobService.setDescription(activeJob, "다른 상담사(" + targetAgent.getName() + ") 연결 대기");
            jobService.setStatus(activeJob, JobStatus.PENDING);
            callPanel.appendLine("[JOB #" + activeJob.getId() + " 상태] -> PENDING");
            callPanel.setPendingState(activeJob);
            activeJob = null;
        }
        refreshAllDbViews();
    }

    @Override
    public void onExecuteScenario4(String answerMessage) {
        if (!ensureActiveInProgressCall()) return;
        String msg = (answerMessage == null || answerMessage.isBlank())
                ? "해당 질문에 대해서는 안내드린 절차대로 진행하시면 됩니다."
                : answerMessage;

        callPanel.appendLine("[상담사 " + currentAgent.getName() + " (" + CENTER_NUMBER + ")] " + msg);
        Log log = logService.logSimpleAnswer(currentAgent, activeJob, "단순 문의 답변", msg, LocalDateTime.now());
        callPanel.appendLine(log.toString());

        jobService.setDescription(activeJob, "단순 문의 상담 완료");
        jobService.setStatus(activeJob, JobStatus.COMPLETED);
        callPanel.appendLine("[JOB #" + activeJob.getId() + " 상태] -> COMPLETED (종료시각: " + activeJob.getFormattedEndDateTime() + ")");

        String callerName = (activeCustomer != null) ? activeCustomer.getName() : activeCallerPhone;
        callPanel.appendLine("[" + callerName + " (" + CENTER_NUMBER + ")] 통화 종료");

        callPanel.setCompletedState(activeJob);
        activeJob = null;
        refreshAllDbViews();
    }

    @Override
    public void onExecuteGuestSimpleConsultation() {
        if (activeJob == null) {
            JOptionPane.showMessageDialog(this, "수신된 전화가 없습니다.");
            return;
        }
        jobService.setDescription(activeJob, "비회원 단순상담 완료");
        jobService.setStatus(activeJob, JobStatus.IN_PROGRESS);
        Log log = logService.logSimpleAnswer(currentAgent, activeJob, "비회원 단순상담", "미등록 번호 단순 안내 후 종료", LocalDateTime.now());
        callPanel.appendLine(log.toString());
        jobService.setStatus(activeJob, JobStatus.COMPLETED);
        callPanel.appendLine("[JOB #" + activeJob.getId() + " 상태] -> COMPLETED (비회원 단순상담)");
        callPanel.appendLine("[" + activeCallerPhone + " (" + CENTER_NUMBER + ")] 통화 종료");

        callPanel.setCompletedState(activeJob);
        activeJob = null;
        refreshAllDbViews();
    }

    @Override
    public void onRefreshAgentsRequested() {
        refreshAgentsInfo();
    }

    // ==================== JobHistoryListener 구현 ====================

    @Override
    public void onViewJobLogs(int jobId) {
        skipNextLogAutoRefresh = true;
        logHistoryPanel.filterByJobId(jobId);
        rightTabs.setSelectedIndex(2);
    }

    @Override
    public void onCompletePendingJob(Job job) {
        String note = JOptionPane.showInputDialog(this,
                "Job #" + job.getId() + " 대기 업무 완료 메모를 입력하세요:",
                "대기 업무 완료 처리", JOptionPane.PLAIN_MESSAGE);
        if (note == null) {
            return;
        }
        String finalDesc = note.isBlank() ? "대기 업무 후속 상담 완료" : note.trim();
        jobService.setDescription(job, finalDesc);
        jobService.setStatus(job, JobStatus.COMPLETED);
        Log log = logService.logSimpleAnswer(currentAgent, job, "대기 업무 완료", finalDesc, LocalDateTime.now());
        callPanel.appendLine("[PENDING 완료] Job #" + job.getId() + " -> COMPLETED (" + finalDesc + ")");
        callPanel.appendLine(log.toString());
        refreshAllDbViews();
    }

    private void handleLogout() {
        authService.updateAgentStatus(currentAgent, "자리 비움");
        LoginFrame loginFrame = new LoginFrame(
                dbManager, authService, userService, jobService, logService, true, null);
        loginFrame.setVisible(true);
        dispose();
    }
}
