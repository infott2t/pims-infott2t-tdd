package program.cprogram;

import job.Job;
import log.Log;
import users.Users; 

public class C2Program {

    static Users chkUser = null; //상담사가 전화번호를 확인할 때 쓰는 함수. 회원정보에 전화번호가 있는지 없는지 체크. (예약상담 또는 단순상담 여부)
    static boolean needBoninHwackIn = true; //본인 확인 필요 여부 (true값 임시설정.)
    static boolean needAnotherNumber = true; // 다른 번호를 알려주는 경우 (true값 임시설정.)
    static boolean needAnotherAgent = true;  // 다른 상담사를 바꿔달라는 경우 (true값 임시설정.)
    static Log log1,log2,log3,log4 = null;  //업무중 로그기록들.

    static Job job1 = null;  //업무기록. 전화상담에대해 1번씩 기록. 상태, INIT, PENDING, IN_PROGRESS, COMPLETED
    public static void main(String[] args) {
        System.out.println("C2Program 실행");

        //대면 업무 전에 비대면 업무에 있어서, 서류신청에 준비물 등의 이야기. 전화상으로 업무가 완전히 끝나지는 않는다. 대면이 필요.
        // 구두 질의 응답. 대면 방문 예약 및 서류 안내만 가능. (지식 기반 인증, 단순 본인 추정)
        //이의 경우, 단순 상담, 분실 정지, 대면 예약 가능.
        // 5만원 이하의 상품권인 경우. 세금신고할 필요가 없어 개인정보 수집대상이 아니다. 
        // 5만원 이상인 경우, 소득세법(당첨금의 22% 제세공과금으로 신고 납부를 해야함)을 따라야하므로, 개인정보 수집이 필요. 대면이 필요.




        //사용자와 상담사 로직으로 구현 user and agent.
        Users user = new Users("John Doe", "사용자"); // user, 사용자
        user.setPhoneNumber("010-user-xxxx");       // 사용자 전화번호 설정
        user.setAddress("부산시 해운대구 xx동"); // 사용자 주소 설정


        Users agent = new Users("첼시", "상담사");     // agent, 고객센터(CS)상담사
        Users agent2 = new Users("미키", "상담사");    // agent, 고객센터(CS)상담사




        //*****************업무 진행 순서
        //회원(사용자)이 전화를 건다.
        user.call("x23-4567-8901");
        
        
        //상담사는 걸려온 전화번호를 확인한다.
        chkUser = agent.checkPhoneNumber("010-user-xxxx");
        //업무 객체가 실행됨.
        job1 = new Job(0, "전화상담", null, "INIT");
        //기록할 로그.
        log1 = null;
        if(chkUser!=null){
            //회원 정보가 있으므로, 계속 진행. 또, Log로 기록남김. Log(Users agent, Job job1, String phoneData)
            log1 = new Log(agent, job1, chkUser.getPhoneNumber()); 
        }else {
            //회원 정보가 없으므로, 단순 상담만 가능.
        }

        //상담사가 전화를 받는다.
        agent.answer("x23-4567-8901", null);

        //job. 업무상태가 IN_PROGRESS로 바뀐다.
        job1.setStatus("IN_PROGRESS");

        //상담사는 자신의 이름을 밝히고, 사용자(회원)에게 어떤 일로 상담을 하는지 물어본다.
        //안녕하세요. ㅇㅇㅇ 상담사 첼시입니다. 무엇을 도와드릴까요?
        agent.answer("x23-4567-8901", "안녕하세요. ㅇㅇㅇ 상담사 첼시입니다. 무엇을 도와드릴까요?");
        //사용자는 상담사에게 자신의 문의사항을 말한다.
        //안녕하세요. ㅇㅇ때문에 그런데요.
        user.answer("x23-4567-8901", "안녕하세요. ㅇㅇ때문에 그런데요.");
        //상담사는 사용자의 문의사항을 듣고, 상담 진행에 본인확인이 필요한지 판단하고, 상담을 진행한다.
        
        if(needBoninHwackIn){
        // 1. 본인확인이 필요한 경우 
        // 본인 확인이 필요한데요. 몇가지 여쭤보겠습니다. 
        // 성함이 어떻게 되시나요?
        // 본인 생년월일이 어떻게 되시나요?
        // 본인 주소가 어떻게 되시나요?
            agent.answer("x23-4567-8901", "본인 확인이 필요한데요. 몇가지 여쭤보겠습니다.");
            user.answer("x23-4567-8901", "네.");
            

            // public Log(Users agent, Job job1, String phoneData, String message, Users[] usersIds)
            agent.answer("x23-4567-8901", "성함이 어떻게 되시나요?");
            user.answer("x23-4567-8901", "ㅇㅇㅇ입니다.");
            log2 = new Log(agent, job1, (String) chkUser.getPhoneNumber(), "본인확인_이름", new Users[]{agent,chkUser});


            agent.answer("x23-4567-8901", "본인 생년월일이 어떻게 되시나요?");
            user.answer("x23-4567-8901", "99011x입니다.");
            log3 = new Log(agent, job1, (String) chkUser.getPhoneNumber(), "본인확인_생년월일", new Users[]{agent,chkUser});
            

            agent.answer("x23-4567-8901", "본인 주소가 어떻게 되시나요?");
            user.answer("x23-4567-8901", "부산시 해운대구 oo동 무슨아파트 몇동 몇호 입니다.");
            log4 = new Log(agent, job1, (String) chkUser.getPhoneNumber(), "본인확인_주소", new Users[]{agent,chkUser});    
        
            // 상담사가 상담을 마치고, 상담 종료를 한다.
            // job에 해당 상담업무에 대해 기록을 한다. ㅇ월 ㅇ일 몇시에 예약 완료, 단순상담등.
            // 이번 상담 일에 대해 기록을 남긴다.  
            // Job (int id, String name, String description, String status)
            job1.setDescription("상담종료_방문예약완료");
            job1.setStatus("COMPLETED");
          
        
        }else{
        // 2. 본인확인이 필요하지 않은 경우 - 궁금한 것에 대해 답변을 해주거나, 더 잘 알 수 있는 전화번호를 안내한다.
        //                                또, 다른 상담사에게 연결시켜준다.
            if(needAnotherNumber){
                 agent.answer("x23-4567-8901", "이쪽으로 전화부탁드림니다. 번호가.. 0123-anthoer-number입니다.");
                 //job의 상태가 COMPLETED로 바뀐다.
                 job1.setStatus("COMPLETED");
                }else{
                if(needAnotherAgent){
                     agent.answer("x23-4567-8901", "그럼, 해당 상담사를 바꿔드릴게요. 기다려주세요.");
                     //job의 상태가 PENDING으로 바뀐다.
                     job1.setStatus("PENDING");
                }else {
                     agent.answer("x23-4567-8901", "해당 질문의 대해서는 ... 게 하시면 됨니다.");
                     //job의 상태가 COMPLETED로 바뀐다.
                    job1.setStatus("COMPLETED");
                }
            } 
           
             
        }
        

        
    }
}