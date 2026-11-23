package swing.entity;

/**
 * 회원 및 상담사 엔티티
 * 기존 users.Users 필드를 기반으로 DB 테이블(users)과 매핑됩니다.
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private String phoneNumber;
    private String birthDay;
    private String address;
    private String status; // 상담사 상태: "업무 중", "자리 비움" 등

    public User() {
    }

    public User(int id, String name, String email, String password, Role role,
                String phoneNumber, String birthDay, String address, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.birthDay = birthDay;
        this.address = address;
        this.status = status;
    }

    public User(String name, String email, String password, Role role,
                String phoneNumber, String birthDay, String address, String status) {
        this(0, name, email, password, role, phoneNumber, birthDay, address, status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        if (role == Role.상담사) {
            return name + " (" + (status != null ? status : "상태 없음") + ")";
        }
        return name + " (" + phoneNumber + ")";
    }
}
