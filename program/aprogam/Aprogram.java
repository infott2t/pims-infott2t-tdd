package program.aprogam;
 
import job.Job;
import log.Log;
import users.Users;
import java.time.LocalDate;
import java.time.LocalTime;

 
public class Aprogram {
    
    static LocalDate today;  // 오늘 날짜
    //getTimeNow()  현재시간은 getNowTime()로 가져오면 됨.

    static boolean bonilhwakinpilyoTF; // 본인확인 필요 여부
    static boolean answerganneunghamyeonTF; // 대답 가능 여부
    static boolean phoneTF = false; // 폰 번호 체크 여부
    public static void main(String[] args) {
        
        // 오늘 날짜를 가져오는 로직
        today = getToday();


        // 상담사와 사용자를 생성
        Users user = new Users("John Doe", "사용자"); // user, 사용자
        Users user2 = new Users("첼시", "상담사");     // user2, 상담사
      
        // 검색을 위한 모든 사용자 정보를 가져오는 로직
        Users[] user_all = new Users[]{};

        // 상담사와 사용자가 통화를 시작
        user.call("x23-4567-8901");
        user2.answer("x23-4567-8901", "안녕하세요. ㅇㅇㅇ입니다. 저는 상담사 첼시입니다. 무엇을 도와드릴까요?");
       

        //전화가 걸리면 일에 기록...
        Job job1 = new Job(1, "상담", "상담 업무", "INIT");  // Job (int id, String name, String description, String status)
        job1.setStatus("IN_PROGRESS");
        job1.setStartDate(today);
        job1.setStartTime(getNowTime());


        // 상담사 : 걸려온 폰 번호의 체크
        job1.setUserPhoneNumber("user-phone-1234");
        Log log1 = new Log(user2, job1,"user-phone-1234", "상담사 : 걸려온 폰 번호의 체크", today, getNowTime());

        // 사용자 : 문의를 시작
        user.call("x23-4567-8901", "안녕하세요. ㅇㅇ때문에 그런데요.");
        
        

        // 상담사가 답변을 해줌 or not.
        // 상담사의 판단

        //본인 확인이 필요하다면...
        if(bonilhwakinpilyoTF){
           
            //폰 번호가 있다면...
            if(user.getPhoneNumber() != null){
                for(Users user_all1 : user_all){
                    if(user.getPhoneNumber().equals(user_all1.getPhoneNumber())){
                       //폰 번호가 있는 경우.
                        phoneTF = true;
                    }      
                }
                    if(phoneTF){
                        user2.answer("x23-4567-8901", "아, 그렇군요. 잠시만 기다려주세요. 본인확인필요합니다. 이름을 알려주세요.");
                
                    }else {
                        user2.answer("x23-4567-8901", "본인확인필요합니다. 죄송합니다만 등록된 본인 폰으로 걸어주세요.");
                    }
           
            //폰 번호가 없다면...
            }else{
                    user2.answer("x23-4567-8901", "본인확인필요합니다. 죄송합니다만 등록된 본인 폰으로 걸어주세요.");
            }


        // 본인확인 없이 대답이 가능하다면...
        if(answerganneunghamyeonTF){
            user2.answer("x23-4567-8901", "아, 그렇군요. 그것은 이렇게 됨니다.");
        }else{
            user2.answer("x23-4567-8901", "잘 모르는데, 여기에 문의해보세요. ㅇㅇㅇㅇ-ㅇㅇㅇㅇ");
        }
    }
        
        user2.endCall("x23-4567-8901");
    }

     
    static LocalDate getToday() {
        // 오늘 날짜를 가져오는 로직
        return LocalDate.now();
    }
    static LocalTime getNowTime() {
        // 현재 시간을 가져오는 로직
        return LocalTime.now();
    }
}