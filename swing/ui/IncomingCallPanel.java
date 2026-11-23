package swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import swing.entity.Job;

/**
 * 좌측 패널: 걸려온 전화 시뮬레이션, 수신/종료 제어 및 실시간 대화·업무·로그 타임라인
 */
public class IncomingCallPanel extends JPanel {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public interface CallActionListener {
        void onCallerPhoneChanged(String callerPhone);
        void onSimulateIncomingCall(String callerPhone);
        void onAnswerCall();
        void onEndCall();
    }

    private static final class PhonePreset {
        private final String label;
        private final String phoneNumber; // null이면 "직접 입력"

        PhonePreset(String label, String phoneNumber) {
            this.label = label;
            this.phoneNumber = phoneNumber;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final JComboBox<PhonePreset> callerPhoneCombo;
    private final JTextField callerPhoneField;
    private final JButton simulateBtn;
    private final JButton answerBtn;
    private final JButton endCallBtn;
    private final JLabel callStatusLabel;
    private final JLabel activeJobLabel;
    private final JTextArea timelineArea;

    public IncomingCallPanel(CallActionListener listener) {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(UiTheme.SURFACE);

        // 상단: 전화 수신 제어 카드
        JPanel controlCard = new JPanel(new GridBagLayout());
        controlCard.setBackground(Color.WHITE);
        controlCard.setBorder(BorderFactory.createTitledBorder("[통화] 전화 수신 시뮬레이터 & 통화 제어"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        PhonePreset[] presets = {
                new PhonePreset("010-user-xxxx (John Doe - 회원)", "010-user-xxxx"),
                new PhonePreset("010-1234-5678 (김철수 - 회원)", "010-1234-5678"),
                new PhonePreset("010-2222-3333 (이영희 - 회원)", "010-2222-3333"),
                new PhonePreset("010-5555-7777 (박민수 - 회원)", "010-5555-7777"),
                new PhonePreset("010-9999-0000 (미등록 - 비회원)", "010-9999-0000"),
                new PhonePreset("직접 입력", null)
        };
        callerPhoneCombo = new JComboBox<>(presets);
        callerPhoneField = new JTextField(presets[0].getPhoneNumber(), 18);
        callerPhoneField.setMinimumSize(new Dimension(160, 28));

        // TextField 직접 입력 시 150ms 디바운스 후 발신번호 변경 이벤트 전달
        Timer phoneChangeTimer = new Timer(150, e -> listener.onCallerPhoneChanged(callerPhoneField.getText().trim()));
        phoneChangeTimer.setRepeats(false);

        callerPhoneField.getDocument().addDocumentListener(new DocumentListener() {
            private void scheduleNotify() {
                phoneChangeTimer.restart();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleNotify();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleNotify();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleNotify();
            }
        });

        callerPhoneCombo.addActionListener(e -> {
            PhonePreset selected = (PhonePreset) callerPhoneCombo.getSelectedItem();
            if (selected == null) {
                return;
            }
            if (selected.getPhoneNumber() == null) {
                callerPhoneField.setText("");
                phoneChangeTimer.stop();
                callerPhoneField.requestFocusInWindow();
                listener.onCallerPhoneChanged("");
            } else {
                callerPhoneField.setText(selected.getPhoneNumber());
                phoneChangeTimer.stop();
                listener.onCallerPhoneChanged(selected.getPhoneNumber());
            }
        });

        // 행 0: 발신번호 선택 (Select)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        controlCard.add(new JLabel("발신번호 선택:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        controlCard.add(callerPhoneCombo, gbc);

        // 행 1: 발신번호 직접 입력 (TextField)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        controlCard.add(new JLabel("발신번호 입력:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        controlCard.add(callerPhoneField, gbc);

        JLabel presetHint = new JLabel("※ 목록 선택 또는 입력창에 직접 입력 후 수신(Enter 가능)");
        presetHint.setForeground(UiTheme.MUTED_TEXT);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        controlCard.add(presetHint, gbc);

        simulateBtn = new JButton("전화 수신(벨 울림)");
        answerBtn = new JButton("전화 받기");
        endCallBtn = new JButton("통화 종료");

        simulateBtn.setFont(UiTheme.FONT_BOLD);
        answerBtn.setEnabled(false);
        endCallBtn.setEnabled(false);

        Runnable triggerIncoming = () -> {
            if (!simulateBtn.isEnabled()) {
                return;
            }
            String phone = callerPhoneField.getText().trim();
            listener.onSimulateIncomingCall(phone);
        };
        simulateBtn.addActionListener(e -> triggerIncoming.run());
        callerPhoneField.addActionListener(e -> triggerIncoming.run());
        answerBtn.addActionListener(e -> listener.onAnswerCall());
        endCallBtn.addActionListener(e -> listener.onEndCall());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);
        btnRow.add(simulateBtn);
        btnRow.add(answerBtn);
        btnRow.add(endCallBtn);

        gbc.gridy = 3;
        controlCard.add(btnRow, gbc);

        callStatusLabel = new JLabel("통화 상태: 대기 중");
        callStatusLabel.setFont(UiTheme.FONT_BOLD);
        callStatusLabel.setForeground(UiTheme.MUTED_TEXT);

        activeJobLabel = new JLabel("현재 업무: 없음");
        activeJobLabel.setForeground(UiTheme.PRIMARY_DARK);

        gbc.gridy = 4;
        controlCard.add(callStatusLabel, gbc);
        gbc.gridy = 5;
        controlCard.add(activeJobLabel, gbc);

        add(controlCard, BorderLayout.NORTH);

        // 중앙: 실시간 통화/업무/로그 타임라인
        JPanel timelinePanel = new JPanel(new BorderLayout(4, 4));
        timelinePanel.setBackground(Color.WHITE);
        timelinePanel.setBorder(BorderFactory.createTitledBorder("실시간 통화 & 업무/로그 출력 (String)"));

        timelineArea = new JTextArea();
        timelineArea.setEditable(false);
        timelineArea.setLineWrap(true);
        timelineArea.setWrapStyleWord(true);
        timelineArea.setFont(UiTheme.FONT_REGULAR);
        timelineArea.setBackground(new Color(252, 252, 253));

        JScrollPane scroll = new JScrollPane(timelineArea);
        timelinePanel.add(scroll, BorderLayout.CENTER);

        JButton clearBtn = new JButton("대화창 비우기");
        clearBtn.addActionListener(e -> timelineArea.setText(""));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        footer.setOpaque(false);
        footer.add(clearBtn);
        timelinePanel.add(footer, BorderLayout.SOUTH);

        add(timelinePanel, BorderLayout.CENTER);
    }

    public String getCurrentCallerPhone() {
        return callerPhoneField.getText().trim();
    }

    public void appendLine(String text) {
        String time = LocalTime.now().format(TIME_FMT);
        timelineArea.append("[" + time + "] " + text + "\n");
        timelineArea.setCaretPosition(timelineArea.getDocument().getLength());
    }

    private void setInputEnabled(boolean enabled) {
        callerPhoneCombo.setEnabled(enabled);
        callerPhoneField.setEnabled(enabled);
    }

    public void setIdleState() {
        setInputEnabled(true);
        simulateBtn.setEnabled(true);
        answerBtn.setEnabled(false);
        endCallBtn.setEnabled(false);
        callStatusLabel.setText("통화 상태: 대기 중");
        callStatusLabel.setForeground(UiTheme.MUTED_TEXT);
        activeJobLabel.setText("현재 업무: 없음");
    }

    public void setRingingState(Job job, String callerPhone) {
        setInputEnabled(false);
        simulateBtn.setEnabled(false);
        answerBtn.setEnabled(true);
        endCallBtn.setEnabled(true);
        callStatusLabel.setText("통화 상태: 벨 울림 (" + callerPhone + ")");
        callStatusLabel.setForeground(UiTheme.WARNING);
        activeJobLabel.setText("현재 업무: Job #" + job.getId() + " [" + job.getStatus() + "]");
    }

    public void setInCallState(Job job, String callerPhone) {
        setInputEnabled(false);
        simulateBtn.setEnabled(false);
        answerBtn.setEnabled(false);
        endCallBtn.setEnabled(true);
        callStatusLabel.setText("통화 상태: 통화 중 (" + callerPhone + ")");
        callStatusLabel.setForeground(UiTheme.SUCCESS);
        activeJobLabel.setText("현재 업무: Job #" + job.getId() + " [" + job.getStatus() + "]");
    }

    public void setPendingState(Job job) {
        setInputEnabled(true);
        simulateBtn.setEnabled(true);
        answerBtn.setEnabled(false);
        endCallBtn.setEnabled(false);
        callStatusLabel.setText("통화 상태: 상담사 연결 대기 (PENDING)");
        callStatusLabel.setForeground(UiTheme.WARNING);
        activeJobLabel.setText("최근 업무: Job #" + job.getId() + " [PENDING]");
    }

    public void setCompletedState(Job job) {
        setInputEnabled(true);
        simulateBtn.setEnabled(true);
        answerBtn.setEnabled(false);
        endCallBtn.setEnabled(false);
        callStatusLabel.setText("통화 상태: 통화 종료 (COMPLETED)");
        callStatusLabel.setForeground(UiTheme.PRIMARY);
        activeJobLabel.setText("최근 업무: Job #" + job.getId() + " [COMPLETED]");
    }
}
