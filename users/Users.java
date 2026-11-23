package users;

import java.util.ArrayList;
import java.util.List;

public class Users {

    private static final List<Users> registeredUsers = new ArrayList<>();

    private int id;
    private String name;
    private String email;
    private String password;
    private Role role;

    // [antigravity(google.com)활용] 본인확인 프로세스 동작을 위한 전화번호 및 주소 필드 추가
    private String phoneNumber;
    private String address;
    private String birthDay; // [antigravity(google.com)활용] 생년월일 필드 추가

    private String status; // 상담사 Role일때에, 상담사 상황을 나타내는 필드 추가. 업무 중, 자리 비움

    public Users() {  
    }
    
    public Users(String name, String role) {
        this.name = name; // [antigravity(google.com)활용] 이름 조회(getName)를 위해 name에도 저장
        this.email = name;
        this.role = new Role(Role.Name.valueOf(role));
        registeredUsers.add(this);
        //this.id = findIdByEmail(email);
        //... 
    }

    // [antigravity(google.com)활용] BProgram에서 전화번호와 주소까지 함께 초기화하기 위한 생성자 추가
    public Users(String name, String role, String phoneNumber, String address) {
        this.name = name;
        this.email = name;
        this.role = new Role(Role.Name.valueOf(role));
        this.phoneNumber = phoneNumber;
        this.address = address;
        registeredUsers.add(this);
    }

    private int findIdByEmail(String email2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findIdByEmail'");
    }

    Users(int id, String name, String email,String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = new Role(Role.Name.valueOf(role));
    }
     
    Users(int id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = new Role(Role.Name.valueOf(role));
    }

    public void call(String string) {
        // TODO Auto-generated method stub
       // throw new UnsupportedOperationException("Unimplemented method 'call'");
        // [antigravity(google.com)활용] 통화 연결 출력
        System.out.println("[" + this.name + " -> " + string + "] 전화 발신");
    }

    public void answer(String callNumber, String string2) {
        String message = (string2 == null) ? "전화를 받았습니다." : string2;
        //Role에 따라 상담사와 사용자 구분하여 출력
        if (this.role.getName() == Role.Name.상담사) {
            // [antigravity(google.com)활용] 상담사 응답 출력
            System.out.println("[상담사 " + this.name + " (" + callNumber + ")] " + message);
        } else if (this.role.getName() == Role.Name.사용자) {
            // [antigravity(google.com)활용] 사용자 응답 출력
            System.out.println("[사용자 " + this.name + " (" + callNumber + ")] " + message);
        }
        
    }

    public void call(String callNumber, String string2) {
        // [antigravity(google.com)활용] 사용자 발화 출력
        System.out.println("[사용자 " + this.name + " (" + callNumber + ")] " + string2);
    }

    public Object getName(String string) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'getName'");
        return this.name;
    }

    public Object getPhoneNumber() {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'getPhoneNumber'");
        return this.phoneNumber;
    }

    public Object getPhoneNumber(String string) {
        // TODO Auto-generated method stub
       // throw new UnsupportedOperationException("Unimplemented method 'getPhoneNumber'");
       return this.phoneNumber;
    }

    public void endCall(String string) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'endCall'");
        // [antigravity(google.com)활용] 통화 종료 출력
        System.out.println("[" + this.name + " (" + string + ")] 통화 종료");
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        // TODO Auto-generated method stub
        // [antigravity(google.com)활용] 주소 반환 구현
        return this.address;
    }

    // [antigravity(google.com)활용] 전화번호 및 주소 Setter 추가
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Users checkPhoneNumber(String string) {
        for (Users user : registeredUsers) {
            if (user.getPhoneNumber() != null && user.getPhoneNumber().equals(string)) {
                System.out.println("[상담사 " + this.name + " (" + string + ")] 회원 확인 완료: " + user.getName());
                return user; // 전화번호가 일치하는 사용자 반환
            }
        }
       
        System.out.println("[상담사 " + this.name + " (" + string + ")] 해당 사용자가 없습니다.");
        return null; // 전화번호가 일치하지 않음
       
    }

    public void setStatus(String string) {
        this.status = string;
        String roleLabel = (this.role != null) ? this.role.getName().name() + " " : "";
        System.out.println("[" + roleLabel + this.name + "] 상태 설정: " + this.status);
    }

    public String getStatus() {
        return this.status;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String string) {
        this.birthDay = string;
    }
}
