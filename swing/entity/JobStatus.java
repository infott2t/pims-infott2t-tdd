package swing.entity;

/**
 * 상담 업무(Job) 상태 Enum
 */
public enum JobStatus {
    INIT("접수(INIT)"),
    IN_PROGRESS("진행중(IN_PROGRESS)"),
    PENDING("대기(PENDING)"),
    COMPLETED("완료(COMPLETED)");

    private final String label;

    JobStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static JobStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return INIT;
        }
        return JobStatus.valueOf(value.trim().toUpperCase());
    }
}
