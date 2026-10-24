package program.bprogram;

import job.Job;
import log.Log;
import users.Users;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * [antigravity(google.com)활용]
 * Aprogram의 깊은 if-else 중첩 구조를 보호 구문(Guard Clauses / Early Return)과
 * 단계별 메서드로 리팩토링하고, 전체 상담 및 본인확인 프로세스가 실제로 동작하도록 완성한 클래스입니다.
 */
public class BProgram {

    static LocalDate today;  // 오늘 날짜
    //getTimeNow()  현재시간은 getNowTime()로 가져오면 됨.

    static boolean bonilhwakinpilyoTF = true; // 본인확인 필요 여부
    static boolean answerganneunghamyeonTF = true; // 대답 가능 여부
    static boolean phoneTF = false; // 폰 번호 체크 여부

    public static void main(String[] args) {

        // 오늘 날짜를 가져오는 로직
        today = getToday();

        // 상담사와 사용자를 생성 ([antigravity(google.com)활용] 실제 프로세스 검증을 위한 사용자 데이터 초기화)
        Users user = new Users("Jone Doe", "사용자", "user-phone-1234", "서울시 강남구 역삼동 xxx-xx"); // user, 사용자
        Users user2 = new Users("첼시", "상담사");     // user2, 상담사

        // 검색을 위한 모든 사용자 정보를 가져오는 로직
        Users[] user_all = new Users[]{ user };
        // 폰으로 검색된 사용자 정보
        Users userPhone = null;

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
        Log log1 = new Log(user2, job1, "user-phone-1234", "상담사 : 걸려온 폰 번호의 체크", today, getNowTime());

        // 사용자 : 문의를 시작
        user.call("x23-4567-8901", "안녕하세요. ㅇㅇ때문에 그런데요.");

        // 상담사가 답변을 해줌 or not.
        // 상담사의 판단

        // [antigravity(google.com)활용]
        // 깊은 중첩 if-else 구조를 보호 구문(Guard Clauses / Early Return)과 단계별 메서드로 분리하여 개선함.
        //본인 확인이 필요하다면...
        if (bonilhwakinpilyoTF) {
            userPhone = findUserByPhone(user, user_all);
            if (verifyIdentity(user, user2, job1, userPhone)) {
                // 본인확인 완료 후 상담 답변 진행
                handleCounselorAnswer(user2);
            }
        } else {
            // 본인확인 없이 대답이 가능하다면...
            handleCounselorAnswer(user2);
        }

        user2.endCall("x23-4567-8901");
    }

    // [antigravity(google.com)활용]
    // 1단계: 폰 번호 존재 여부 확인 및 등록 사용자 검색 (Early Return 적용)
    static Users findUserByPhone(Users user, Users[] user_all) {
        //폰 번호가 없다면...
        if (user.getPhoneNumber() == null) {
            return null;
        }

        //폰 번호가 있다면...
        for (Users user_all1 : user_all) {
            if (user.getPhoneNumber().equals(user_all1.getPhoneNumber())) {
                //폰 번호가 있는 경우.
                phoneTF = true;
                return user_all1;  // 폰 번호로 검색된 사용자 정보
            }
        }
        return null;
    }

    // [antigravity(google.com)활용]
    // 2단계: 보호 구문(Guard Clauses)을 통해 조건 불충족 시 즉시 반환하여 if-else 중첩 제거
    static boolean verifyIdentity(Users user, Users user2, Job job1, Users userPhone) {
        if (!phoneTF || userPhone == null) {
            user2.answer("x23-4567-8901", "본인확인이 필요합니다. 죄송합니다만 등록된 본인 폰으로 걸어주세요.");
            return false;
        }

        if (!verifyUserName(user, user2, job1, userPhone)) {
            user2.answer("x23-4567-8901", "본인확인이 되지 않았습니다.");
            return false;
        }

        if (!verifyUserAddress(user, user2, job1, userPhone)) {
            user2.answer("x23-4567-8901", "본인확인이 되지 않았습니다.");
            return false;
        }

        user2.answer("x23-4567-8901", "본인확인이 완료되었습니다. 무엇을 도와드릴까요?");
        // 상담사: 본인확인 완료. 상담시작... ... .
        return true;
    }

    // [antigravity(google.com)활용]
    // 3단계: 사용자 이름 확인 로직 분리
    static boolean verifyUserName(Users user, Users user2, Job job1, Users userPhone) {
        user2.answer("x23-4567-8901", "아, 그렇군요. 잠시만 기다려주세요. 본인확인필요합니다. 이름을 알려주세요.");
        user.call("x23-4567-8901", "Jone Doe입니다.");
        // 상담사 :사용자 이름을 체크
        job1.setUserName("Jone Doe");
        Log log2 = new Log(user2, job1, "Jone Doe", "상담사 :사용자 이름을 체크", today, getNowTime());

        Object userName = job1.getUserName("Jone Doe");
        return userName != null && userName.equals(userPhone.getName());
    }

    // [antigravity(google.com)활용]
    // 4단계: 사용자 주소 확인 로직 분리
    static boolean verifyUserAddress(Users user, Users user2, Job job1, Users userPhone) {
        user2.answer("x23-4567-8901", "이번에는 주소로 본인확인할게요. 주소를 알려주세요.");
        user.call("x23-4567-8901", "서울시 강남구 역삼동 xxx-xx");
        //상담사 :사용자 주소를 체크
        job1.setUserAddress("서울시 강남구 역삼동 xxx-xx");
        Log log3 = new Log(user2, job1, "서울시 강남구 역삼동 xxx-xx", "상담사 :사용자 주소를 체크", today, getNowTime());

        Object userAddress = job1.getUserAddress("서울시 강남구 역삼동 xxx-xx");
        return userAddress != null && userAddress.equals(userPhone.getAddress());
    }

    // [antigravity(google.com)활용]
    // 상담사 답변 처리 로직 분리
    static void handleCounselorAnswer(Users user2) {
        if (answerganneunghamyeonTF) {
            user2.answer("x23-4567-8901", "아, 그렇군요. 그것은 이렇게 됨니다.");
        } else {
            user2.answer("x23-4567-8901", "잘 모르는데, 여기에 문의해보세요. ㅇㅇㅇㅇ-ㅇㅇㅇㅇ");
        }
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
