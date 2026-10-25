package job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import log.Log;

public class Job {

    private int id;
    private String name;
    private String description;
    private Status status;
    private LocalDateTime startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private String userPhoneNumber;
    private int[] userIds;
    private Log[] logs;

    private String userName; // 사용자 이름을 저장하는 필드 추가
    private String userAddress; // [antigravity(google.com)활용] 사용자 주소를 저장하는 필드 추가

    //new Job(1, "상담", "상담 업무");
    public Job (int id, String name, String description, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        if(status == "INIT") {this.status = new Status(Status.Name.valueOf("INIT"));}
        this.startDate = nowDateTime();
        this.endDate = null;
        this.userIds = new int[0];
    }

    public Job (int id, String name, String description, String status, Log log) {
        this.id = id;
        this.name = name;
        this.description = description;
        if(status == "INIT") {this.status = new Status(Status.Name.valueOf("INIT"));}
        this.startDate = nowDateTime();
        this.endDate = null;
        this.userIds = new int[0];
         
    }

    private LocalDateTime nowDateTime() {
        //지금 날짜와 시간을 리턴
        return LocalDateTime.now();
    }

    Job (int id, String name, String description, String status, LocalDateTime startDate, LocalDate endDate, int[] userIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = new Status(Status.Name.valueOf(status));
        this.startDate = startDate;
        this.endDate = endDate;
        this.userIds = userIds;
    }

    public void setUserPhoneNumber(String phoneNumber) {
        this.userPhoneNumber = phoneNumber;
    }

    // [antigravity(google.com)활용] 걸려온 전화번호 조회 Getter 추가
    public String getUserPhoneNumber() {
        return this.userPhoneNumber;
    }

    public void setStatus(String string) {
         
        this.status = new Status(Status.Name.valueOf(string));
    }

    public void setStartDate(LocalDate today) {
        this.startDate = today.atStartOfDay();
    }

    public void setStartTime(LocalTime nowTime) {
        
        this.startTime = nowTime;
    }

    public void setUserName(String string) {
        this.userName = string; // 사용자 이름을 설정하는 메서드
    }

    public Object getUserName(String string) {
        return this.userName; // 사용자 이름을 반환
    }

    public void setUserAddress(String string) {
       
        this.userAddress = string;
    }

    public Object getUserAddress(String string) {
        
        return this.userAddress;
    }
}
