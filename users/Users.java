package users;

public class Users {

    private int id;
    private String name;
    private String email;
    private String password;
    private Role role;

    // [antigravity(google.com)활용] 본인확인 프로세스 동작을 위한 전화번호 및 주소 필드 추가
    private String phoneNumber;
    private String address;

    public Users() {  
    }
    
    public Users(String email, String role) {
        this.name = email; // [antigravity(google.com)활용] 이름 조회(getName)를 위해 name에도 저장
        this.email = email;
        this.role = new Role(Role.Name.valueOf(role));
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
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'answer'");
        // [antigravity(google.com)활용] 상담사 응답 출력
        System.out.println("[상담사 " + this.name +"]"+ " (" + callNumber + ")] " + string2);
    }

    public void call(String callNumber, String string2) {
        // TODO Auto-generated method stub
       // throw new UnsupportedOperationException("Unimplemented method 'call'");
        // [antigravity(google.com)활용] 사용자 발화 출력
        System.out.println("[사용자 " + this.name +"]"+ " (" + callNumber + ")] " + string2);
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

    public Object getAddress() {
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
        Users[] user_all = new Users[]{ this }; // 현재 사용자 객체를 배열로 생성
        for (Users user : user_all) {
            if (user.getPhoneNumber() != null && user.getPhoneNumber().equals(string)) {
                return user; // 전화번호가 일치하는 사용자 반환
            }
        }
       
        System.out.println("[" + this.name + " (" + string + ")] 해당 사용자가 없습니다.");
        return null; // 전화번호가 일치하지 않음
       
    }
}
