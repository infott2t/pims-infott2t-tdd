package swing.entity;

/**
 * 사용자 권한(역할) 엔티티 Enum
 * 기존 users.Role.Name(상담사, 사용자, 일반)을 계승합니다.
 */
public enum Role {
    상담사("고객센터(CS) 상담사"),
    사용자("등록된 일반 회원"),
    일반("비회원 또는 일반 사용자");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            return 일반;
        }
        return Role.valueOf(value.trim());
    }
}
