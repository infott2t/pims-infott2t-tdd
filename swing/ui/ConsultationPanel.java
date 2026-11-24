package swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import swing.entity.User;
import swing.service.UserService;

/**
 * 우측 탭 1: 회원정보 DB 검색 및 상담 시나리오 1~4 처리 패널
 */
public class ConsultationPanel extends JPanel {

    public interface ScenarioActionListener {
        void onVerifyName(User selectedCustomer, String inputName);
        void onVerifyBirthDay(User selectedCustomer, String inputBirthDay);
        void onVerifyAddress(User selectedCustomer, String inputAddress);
        void onCompleteScenario1(String description);
        void onExecuteScenario2(String anotherNumber);
        void onExecuteScenario3(User targetAgent);
        void onExecuteScenario4(String answerMessage);
        void onExecuteGuestSimpleConsultation();
        void onRefreshAgentsRequested();
        void onCustomerSelected(User customer);
        void onArbitrarySearchExecuted(String keyword);
    }

    private final UserService userService;
    private final ScenarioActionListener listener;

    // 회원 검색 UI
    private final JComboBox<String> searchFieldCombo;
    private final JTextField searchKeywordField;
    private final Timer autoSearchTimer;
    private boolean suppressAutoSearch = false;
    private final DefaultTableModel customerTableModel;
    private final JTable customerTable;
    private final JLabel selectedCustomerBanner;
    private List<User> displayedCustomers = new ArrayList<>();
    private User selectedCustomer;

    // 시나리오 1 UI (본인확인)
    private final JTextField s1NameField;
    private final JTextField s1BirthField;
    private final JTextField s1AddressField;
    private final JLabel s1NameResultLabel;
    private final JLabel s1BirthResultLabel;
    private final JLabel s1AddressResultLabel;
    private final JTextField s1DescField;

    // 시나리오 2 UI (타 전화번호 안내)
    private final JTextField s2PhoneField;

    // 시나리오 3 UI (다른 상담사 연결)
    private final JComboBox<User> s3AgentCombo;

    // 시나리오 4 UI (단순 질문 답변)
    private final JTextField s4AnswerField;

    public ConsultationPanel(UserService userService, ScenarioActionListener listener) {
        super(new BorderLayout(8, 8));
        this.userService = userService;
        this.listener = listener;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(UiTheme.SURFACE);

        // 1. 상단: 회원정보 DB 검색 영역
        JPanel searchSection = new JPanel(new BorderLayout(6, 6));
        searchSection.setBackground(Color.WHITE);
        searchSection.setBorder(BorderFactory.createTitledBorder("[검색] 회원정보 DB 검색 (이름 · 이메일 · 전화번호 · 생년월일 · 주소)"));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        searchBar.setOpaque(false);
        searchFieldCombo = new JComboBox<>(new String[]{"전화번호", "이름", "이메일"});
        searchKeywordField = new JTextField(18);
        JButton searchBtn = new JButton("검색");
        JButton clearBtn = new JButton("초기화");
        JButton addCustomerBtn = new JButton("+ 신규 회원 등록");

        // 검색 텍스트필드 변경 시 400ms 디바운스 후 자동 검색 + 임의검색 로그 기록
        autoSearchTimer = new Timer(400, e -> {
            if (suppressAutoSearch) {
                return;
            }
            String kw = searchKeywordField.getText().trim();
            if (kw.isEmpty()) {
                updateCustomerTable(Collections.emptyList());
                clearSelectedCustomer("전화 수신(벨 울림)을 누르거나 검색어를 입력하면 회원 정보가 조회됩니다.");
            } else {
                performSearch(true);
            }
        });
        autoSearchTimer.setRepeats(false);

        searchKeywordField.getDocument().addDocumentListener(new DocumentListener() {
            private void scheduleAutoSearch() {
                if (!suppressAutoSearch) {
                    autoSearchTimer.restart();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleAutoSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleAutoSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleAutoSearch();
            }
        });

        // 검색 기준 select 변경 시 텍스트필드에 값이 있으면 즉시 자동 검색 + 임의검색 로그 기록
        searchFieldCombo.addActionListener(e -> {
            if (suppressAutoSearch) {
                return;
            }
            autoSearchTimer.stop();
            if (!searchKeywordField.getText().trim().isEmpty()) {
                performSearch(true);
            }
        });

        searchBtn.addActionListener(e -> {
            autoSearchTimer.stop();
            performSearch(true);
        });
        searchKeywordField.addActionListener(e -> {
            autoSearchTimer.stop();
            performSearch(true);
        });
        clearBtn.addActionListener(e -> {
            autoSearchTimer.stop();
            suppressAutoSearch = true;
            try {
                searchKeywordField.setText("");
                searchFieldCombo.setSelectedItem("전화번호");
            } finally {
                suppressAutoSearch = false;
            }
            updateCustomerTable(Collections.emptyList());
            clearSelectedCustomer("전화 수신(벨 울림)을 누르거나 검색어를 입력하면 회원 정보가 조회됩니다.");
        });
        addCustomerBtn.addActionListener(e -> openRegisterCustomerDialog());

        searchBar.add(new JLabel("검색 기준:"));
        searchBar.add(searchFieldCombo);
        searchBar.add(searchKeywordField);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);
        searchBar.add(addCustomerBtn);
        searchSection.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"ID", "이름", "이메일", "전화번호", "생년월일", "주소", "권한"};
        customerTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(customerTableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.setRowHeight(24);
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = customerTable.getSelectedRow();
                if (row >= 0 && row < displayedCustomers.size()) {
                    selectCustomer(displayedCustomers.get(row));
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(customerTable);
        tableScroll.setPreferredSize(new Dimension(650, 145));
        searchSection.add(tableScroll, BorderLayout.CENTER);

        selectedCustomerBanner = new JLabel("선택된 회원: 없음 (전화 수신 또는 테이블에서 회원을 선택하세요)");
        selectedCustomerBanner.setFont(UiTheme.FONT_BOLD);
        selectedCustomerBanner.setForeground(UiTheme.PRIMARY_DARK);
        selectedCustomerBanner.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        searchSection.add(selectedCustomerBanner, BorderLayout.SOUTH);

        // 2. 하단: 시나리오 1~4 처리 탭
        JTabbedPane scenarioTabs = new JTabbedPane();
        scenarioTabs.setFont(UiTheme.FONT_BOLD);

        // --- 시나리오 1: 본인확인 필요 ---
        s1NameField = new JTextField(16);
        s1BirthField = new JTextField(16);
        s1AddressField = new JTextField(22);
        s1NameResultLabel = new JLabel("미확인");
        s1BirthResultLabel = new JLabel("미확인");
        s1AddressResultLabel = new JLabel("미확인");
        s1DescField = new JTextField("본인확인 상담 완료 (대면 방문 예약 안내)", 26);

        scenarioTabs.addTab("시나리오 1: 본인확인", wrapScenarioTab(buildScenario1Panel()));

        // --- 시나리오 2: 타 전화번호 안내 ---
        s2PhoneField = new JTextField("0123-another-number", 22);
        scenarioTabs.addTab("시나리오 2: 타 번호 안내", wrapScenarioTab(buildScenario2Panel()));

        // --- 시나리오 3: 다른 상담사 연결 ---
        s3AgentCombo = new JComboBox<>();
        scenarioTabs.addTab("시나리오 3: 상담사 연결", wrapScenarioTab(buildScenario3Panel()));

        // --- 시나리오 4: 단순 질문 답변 / 비회원 단순상담 ---
        s4AnswerField = new JTextField("해당 질문에 대해서는 안내드린 절차대로 진행하시면 됩니다.", 30);
        scenarioTabs.addTab("시나리오 4: 단순 답변", wrapScenarioTab(buildScenario4Panel()));

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchSection, scenarioTabs);
        verticalSplit.setDividerLocation(240);
        verticalSplit.setResizeWeight(0.38);
        verticalSplit.setBorder(null);
        add(verticalSplit, BorderLayout.CENTER);

        // 초기에는 전체 회원 목록을 노출하지 않고 비워둠 (전화 수신 또는 직접 검색 시 표시)
        clearSelectedCustomer("전화 수신(벨 울림)을 누르거나 검색어를 입력하면 회원 정보가 조회됩니다.");
    }

    private JScrollPane wrapScenarioTab(JPanel content) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(Color.WHITE);
        holder.add(content, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(holder);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel createScenarioHeader(String titleText, String descText) {
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 3));
        header.setBackground(new Color(241, 245, 249));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, UiTheme.PRIMARY),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(UiTheme.FONT_SUBTITLE);
        titleLabel.setForeground(UiTheme.HEADER_BG);

        JLabel descLabel = new JLabel(descText);
        descLabel.setFont(UiTheme.FONT_REGULAR);
        descLabel.setForeground(new Color(51, 65, 85));

        header.add(titleLabel);
        header.add(descLabel);
        return header;
    }

    private JPanel buildScenario1Panel() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        root.add(createScenarioHeader(
                "1. 본인확인이 필요한 경우",
                "고객에게 성함·생년월일·주소를 질의하여 DB 회원정보와 대조하고 각 항목의 확인 로그를 기록합니다."
        ), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // 성함 행
        JButton chkNameBtn = new JButton("성함 확인 & 로그");
        chkNameBtn.addActionListener(e -> listener.onVerifyName(selectedCustomer, s1NameField.getText().trim()));
        addVerifyRow(form, gbc, 0, "고객 응답 성함:", s1NameField, chkNameBtn, s1NameResultLabel);

        // 생년월일 행
        JButton chkBirthBtn = new JButton("생년월일 확인 & 로그");
        chkBirthBtn.addActionListener(e -> listener.onVerifyBirthDay(selectedCustomer, s1BirthField.getText().trim()));
        addVerifyRow(form, gbc, 1, "고객 응답 생년월일:", s1BirthField, chkBirthBtn, s1BirthResultLabel);

        // 주소 행
        JButton chkAddrBtn = new JButton("주소 확인 & 로그");
        chkAddrBtn.addActionListener(e -> listener.onVerifyAddress(selectedCustomer, s1AddressField.getText().trim()));
        addVerifyRow(form, gbc, 2, "고객 응답 주소:", s1AddressField, chkAddrBtn, s1AddressResultLabel);

        // 완료 설명 & 완료 버튼
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("업무 결과 메모:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        s1DescField.setMinimumSize(new Dimension(220, 28));
        form.add(s1DescField, gbc);

        JButton completeS1Btn = new JButton("본인확인 상담 완료 (COMPLETED)");
        completeS1Btn.setFont(UiTheme.FONT_BOLD);
        completeS1Btn.addActionListener(e -> listener.onCompleteScenario1(s1DescField.getText().trim()));
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(completeS1Btn, gbc);

        root.add(form, BorderLayout.CENTER);
        return root;
    }

    private void addVerifyRow(JPanel panel, GridBagConstraints gbc, int row,
                              String label, JTextField field, JButton button, JLabel resultLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setPreferredSize(new Dimension(130, 26));
        panel.add(fieldLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        field.setMinimumSize(new Dimension(180, 28));
        panel.add(field, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(button, gbc);

        gbc.gridx = 3;
        resultLabel.setFont(UiTheme.FONT_BOLD);
        resultLabel.setForeground(UiTheme.MUTED_TEXT);
        resultLabel.setPreferredSize(new Dimension(105, 24));
        panel.add(resultLabel, gbc);
    }

    private JPanel buildScenario2Panel() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        root.add(createScenarioHeader(
                "2. 본인확인 불필요 — 다른 담당 부서 전화번호 안내",
                "안내할 전화번호를 로그(logAnotherNumber)에 기록하고 업무 상태를 COMPLETED로 종료합니다."
        ), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(new JLabel("안내할 전화번호:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        s2PhoneField.setMinimumSize(new Dimension(200, 28));
        form.add(s2PhoneField, gbc);

        JButton executeBtn = new JButton("타 번호 안내 & 상담 완료 (COMPLETED)");
        executeBtn.setFont(UiTheme.FONT_BOLD);
        executeBtn.addActionListener(e -> listener.onExecuteScenario2(s2PhoneField.getText().trim()));
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(executeBtn, gbc);

        root.add(form, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildScenario3Panel() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        root.add(createScenarioHeader(
                "3. 본인확인 불필요 — 다른 상담사 연결",
                "대상 상담사가 '업무 중'이면 인계 완료(COMPLETED), '자리 비움'이면 대기 안내 후 PENDING 상태로 전환합니다."
        ), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(new JLabel("연결 대상 상담사:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        s3AgentCombo.setMinimumSize(new Dimension(180, 28));
        form.add(s3AgentCombo, gbc);

        JButton refreshBtn = new JButton("상태 새로고침");
        refreshBtn.addActionListener(e -> listener.onRefreshAgentsRequested());
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(refreshBtn, gbc);

        JButton transferBtn = new JButton("상담사 연결 요청");
        transferBtn.setFont(UiTheme.FONT_BOLD);
        transferBtn.addActionListener(e -> {
            User target = (User) s3AgentCombo.getSelectedItem();
            listener.onExecuteScenario3(target);
        });
        gbc.gridx = 3;
        form.add(transferBtn, gbc);

        root.add(form, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildScenario4Panel() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        root.add(createScenarioHeader(
                "4. 본인확인 불필요 — 단순 질문 답변 후 상담 종료",
                "답변 내용을 로그(logSimpleAnswer)로 남기고 업무를 COMPLETED로 종료합니다. (비회원 단순상담도 지원)"
        ), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(new JLabel("상담사 답변 내용:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        s4AnswerField.setMinimumSize(new Dimension(220, 28));
        form.add(s4AnswerField, gbc);

        JButton answerBtn = new JButton("답변 & 상담 완료 (COMPLETED)");
        answerBtn.setFont(UiTheme.FONT_BOLD);
        answerBtn.addActionListener(e -> listener.onExecuteScenario4(s4AnswerField.getText().trim()));
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(answerBtn, gbc);

        JButton guestBtn = new JButton("비회원 단순상담 즉시 완료");
        guestBtn.addActionListener(e -> listener.onExecuteGuestSimpleConsultation());
        gbc.gridx = 1;
        gbc.gridy = 1;
        form.add(guestBtn, gbc);

        root.add(form, BorderLayout.CENTER);
        return root;
    }

    public void performSearch() {
        performSearch(true);
    }

    public void performSearch(boolean recordArbitraryLog) {
        try {
            String field = (String) searchFieldCombo.getSelectedItem();
            String keyword = searchKeywordField.getText().trim();
            if (keyword.isEmpty()) {
                updateCustomerTable(Collections.emptyList());
                clearSelectedCustomer("검색어(전화번호 등)를 입력해 주세요. 전체 회원 목록은 표시되지 않습니다.");
                return;
            }

            if ("전화번호".equals(field)) {
                Optional<User> matched = userService.checkPhoneNumber(keyword);
                if (matched.isPresent()) {
                    User customer = matched.get();
                    updateCustomerTable(Collections.singletonList(customer));
                    customerTable.setRowSelectionInterval(0, 0);
                    selectCustomer(customer);
                } else {
                    updateCustomerTable(Collections.emptyList());
                    clearSelectedCustomer("미등록 전화번호 (" + keyword + ") — 일치하는 회원 정보 없음 (비회원 단순상담만 가능)");
                }
            } else {
                List<User> results = userService.searchCustomers(field, keyword);
                updateCustomerTable(results);
                if (results.size() == 1) {
                    customerTable.setRowSelectionInterval(0, 0);
                    selectCustomer(results.get(0));
                } else if (results.isEmpty()) {
                    clearSelectedCustomer("검색 결과가 없습니다: " + keyword);
                } else {
                    clearSelectedCustomer("검색 결과 " + results.size() + "건 — 테이블에서 대상 회원을 선택하세요.");
                }
            }

            if (recordArbitraryLog) {
                listener.onArbitrarySearchExecuted(keyword);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "회원 검색 오류:\n" + ex.getMessage(),
                    "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 전화 수신(벨 울림) 등 시스템 이벤트에 의해 발신번호와 정확히 일치하는 회원 1건만 표시합니다.
     * (임의검색 로그를 중복 발생시키지 않도록 suppressAutoSearch로 보호합니다.)
     */
    public void filterByExactPhone(String phoneNumber) {
        String trimmed = (phoneNumber == null) ? "" : phoneNumber.trim();
        autoSearchTimer.stop();
        suppressAutoSearch = true;
        try {
            searchFieldCombo.setSelectedItem("전화번호");
            searchKeywordField.setText(trimmed);
        } finally {
            suppressAutoSearch = false;
        }

        if (trimmed.isEmpty()) {
            updateCustomerTable(Collections.emptyList());
            clearSelectedCustomer("전화 수신(벨 울림)을 누르거나 검색어를 입력하면 회원 정보가 조회됩니다.");
            return;
        }

        try {
            Optional<User> matched = userService.checkPhoneNumber(trimmed);
            if (matched.isPresent()) {
                User customer = matched.get();
                updateCustomerTable(Collections.singletonList(customer));
                customerTable.setRowSelectionInterval(0, 0);
                selectCustomer(customer);
            } else {
                updateCustomerTable(Collections.emptyList());
                clearSelectedCustomer("미등록 전화번호 (" + trimmed + ") — 일치하는 회원 정보 없음 (비회원 단순상담만 가능)");
            }
        } catch (Exception ex) {
            updateCustomerTable(Collections.emptyList());
            clearSelectedCustomer("회원 조회 중 오류 발생: " + ex.getMessage());
        }
    }

    public void searchByIncomingPhone(String phoneNumber) {
        filterByExactPhone(phoneNumber);
    }

    private void updateCustomerTable(List<User> customers) {
        this.displayedCustomers = customers;
        customerTableModel.setRowCount(0);
        for (User u : customers) {
            customerTableModel.addRow(new Object[]{
                    u.getId(),
                    u.getName(),
                    u.getEmail(),
                    u.getPhoneNumber(),
                    u.getBirthDay(),
                    u.getAddress(),
                    u.getRole()
            });
        }
    }

    private void selectCustomer(User customer) {
        this.selectedCustomer = customer;
        selectedCustomerBanner.setForeground(UiTheme.PRIMARY_DARK);
        selectedCustomerBanner.setText(String.format(
                "선택된 회원: [ID:%d] %s | 이메일: %s | 전화: %s | 생년월일: %s | 주소: %s",
                customer.getId(), customer.getName(), customer.getEmail(),
                customer.getPhoneNumber(), customer.getBirthDay(), customer.getAddress()));

        // 시나리오 1 입력란에 빠른 테스트를 위한 힌트 자동 채우기
        s1NameField.setText(customer.getName());
        s1BirthField.setText(customer.getBirthDay() != null ? customer.getBirthDay() : "");
        s1AddressField.setText(customer.getAddress() != null ? customer.getAddress() : "");
        resetVerificationBadges();
        listener.onCustomerSelected(customer);
    }

    public void clearSelectedCustomer(String notice) {
        this.selectedCustomer = null;
        selectedCustomerBanner.setForeground(UiTheme.WARNING);
        selectedCustomerBanner.setText(notice);
        s1NameField.setText("");
        s1BirthField.setText("");
        s1AddressField.setText("");
        resetVerificationBadges();
    }

    public void resetVerificationBadges() {
        setBadge(s1NameResultLabel, "미확인", UiTheme.MUTED_TEXT);
        setBadge(s1BirthResultLabel, "미확인", UiTheme.MUTED_TEXT);
        setBadge(s1AddressResultLabel, "미확인", UiTheme.MUTED_TEXT);
    }

    public void setNameVerifyResult(boolean matched) {
        setBadge(s1NameResultLabel, matched ? "일치 (확인됨)" : "불일치", matched ? UiTheme.SUCCESS : UiTheme.DANGER);
    }

    public void setBirthVerifyResult(boolean matched) {
        setBadge(s1BirthResultLabel, matched ? "일치 (확인됨)" : "불일치", matched ? UiTheme.SUCCESS : UiTheme.DANGER);
    }

    public void setAddressVerifyResult(boolean matched) {
        setBadge(s1AddressResultLabel, matched ? "일치 (확인됨)" : "불일치", matched ? UiTheme.SUCCESS : UiTheme.DANGER);
    }

    private void setBadge(JLabel label, String text, Color color) {
        label.setText(text);
        label.setForeground(color);
    }

    public void setOtherAgents(List<User> agents) {
        s3AgentCombo.removeAllItems();
        for (User agent : agents) {
            s3AgentCombo.addItem(agent);
        }
    }

    private void openRegisterCustomerDialog() {
        JTextField nameF = new JTextField(16);
        JTextField emailF = new JTextField(16);
        JTextField phoneF = new JTextField(16);
        JTextField birthF = new JTextField(16);
        JTextField addrF = new JTextField(20);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 6));
        form.add(new JLabel("이름 (*):"));
        form.add(nameF);
        form.add(new JLabel("이메일 (*):"));
        form.add(emailF);
        form.add(new JLabel("전화번호 (*):"));
        form.add(phoneF);
        form.add(new JLabel("생년월일 (예: 950101):"));
        form.add(birthF);
        form.add(new JLabel("주소:"));
        form.add(addrF);

        int result = JOptionPane.showConfirmDialog(this, form, "신규 회원 DB 등록",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                User saved = userService.registerCustomer(
                        nameF.getText(), emailF.getText(), phoneF.getText(), birthF.getText(), addrF.getText());
                JOptionPane.showMessageDialog(this,
                        "회원이 DB에 등록되었습니다: " + saved.getName() + " (ID: " + saved.getId() + ")",
                        "등록 완료", JOptionPane.INFORMATION_MESSAGE);
                filterByExactPhone(saved.getPhoneNumber());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "회원 등록 실패:\n" + ex.getMessage(),
                        "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
