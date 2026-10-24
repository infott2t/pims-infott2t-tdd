package job;

import java.time.LocalDate;
import java.time.LocalTime;

public class Job {

    private int id;
    private String name;
    private String description;
    private Status status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private String userPhoneNumber;
    private int[] userIds;

    private String userName; // 사용자 이름을 저장하는 필드 추가

    //new Job(1, "상담", "상담 업무");
    public Job (int id, String name, String description, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        if(status == "INIT") {this.status = new Status(Status.Name.valueOf("INIT"));}
        this.startDate = null;
        this.endDate = null;
        this.userIds = new int[0];
    }

    Job (int id, String name, String description, String status, LocalDate startDate, LocalDate endDate, int[] userIds) {
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

    public void setStatus(String string) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'setStatus'");
    }

    public void setStartDate(LocalDate today) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'setStartDate'");
    }

    public void setStartTime(LocalTime nowTime) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'setStartTime'");
    }

    public void setUserName(String string) {
        this.userName = string; // 사용자 이름을 설정하는 메서드
    }

    public Object getUserName(String string) {
        return this.userName; // 사용자 이름을 반환
    }

    public void setUserAddress(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setUserAddress'");
    }

    public Object getUserAddress(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserAddress'");
    }
}