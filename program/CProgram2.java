package program;

import java.time.LocalDateTime;

import jobs.Job;
import jobs.JobsIF;
import logs.Log;
import logs.LogsIF;
import users.Users;

public class CProgram2 implements JobsIF, LogsIF {

    static Users chkUser = null;               // 상담사가 전화번호를 확인할 때 쓰는 변수. 회원정보에 전화번호가 있는지 없는지 체크. (예약상담 또는 단순상담 여부)
    static String chkNumber = "010-user-xxxx"; // 상담사가 확인한 사용자로부터 걸려온 전화번호 (기본값)
    static boolean needBoninHwackIn = true;    // 본인 확인 필요 여부 (true값 임시설정.)
    static boolean needAnotherNumber = true;   // 다른 번호를 알려주는 경우 (true값 임시설정.)
    static boolean needAnotherAgent = true;    // 다른 상담사를 바꿔달라는 경우 (true값 임시설정.)

    static LogsIF log1 = new CProgram2();      // 업무중 로그기록들.
    static JobsIF job1 = new CProgram2();      // 업무기록. 전화상담에대해 1번씩 기록. 상태: INIT, PENDING, IN_PROGRESS, COMPLETED

    public static void main(String[] args) {

        System.out.println("CProgram2 실행");

        // 대면 업무 전에 비대면 업무에 있어서, 서류신청에 준비물 등의 이야기. 전화상으로 업무가 완전히 끝나지는 않는다. 대면이 필요.
        // 구두 질의 응답. 대면 방문 예약 및 서류 안내만 가능. (지식 기반 인증, 단순 본인 추정)
        // 이의 경우, 단순 상담, 분실 정지, 대면 예약 가능.
        // 5만원 이하의 상품권인 경우. 세금신고할 필요가 없어 개인정보 수집대상이 아니다.
        // 5만원 이상인 경우, 소득세법(당첨금의 22% 제세공과금으로 신고 납부를 해야함)을 따라야하므로, 개인정보 수집이 필요. 대면이 필요.

        // ================= 초기 임시 설정 (기본값) =================
        System.out.println("\n================ [초기 임시 설정] ================");

        // 사용자 user
        Users user = new Users("John Doe", "사용자"); // user, 사용자
        user.setPhoneNumber("010-user-xxxx");         // 사용자 전화번호 설정
        user.setAddress("부산시 해운대구 xx동");         // 사용자 주소 설정
        user.setBirthDay("99011x");                   // 사용자 생년월일 설정

        // 상담사 agent
        Users agent = new Users("첼시", "상담사");      // agent, 고객센터(CS)상담사
        Users agent2 = new Users("미키", "상담사");     // agent2, 고객센터(CS)상담사

        // 상담자의 상황 (상황, 상태)
        agent.setStatus("업무 중");                    // 첼시는 자리에 있음
        agent2.setStatus("자리 비움");                 // 미키는 자리비움

        // 임시 설정 기본값 세팅
        chkNumber = "010-user-xxxx";
        chkUser = user;
        needBoninHwackIn = true;
        needAnotherNumber = true;
        needAnotherAgent = true;

        System.out.println("[임시 설정] chkUser=" + chkUser.getName()
                + ", chkNumber=" + chkNumber
                + ", needBoninHwackIn=" + needBoninHwackIn
                + ", needAnotherNumber=" + needAnotherNumber
                + ", needAnotherAgent=" + needAnotherAgent);

        // ================= 시나리오별 실행 확인 =================
        // 1. 본인확인이 필요한 경우
        needBoninHwackIn = true;
        needAnotherNumber = false;
        needAnotherAgent = false;
        runConsultation("시나리오 1: 본인확인이 필요한 경우", user, agent, agent2);

        // 2. 본인확인 불필요 - 다른 전화번호 안내
        needBoninHwackIn = false;
        needAnotherNumber = true;
        needAnotherAgent = false;
        runConsultation("시나리오 2: 본인확인 불필요 - 다른 전화번호 안내", user, agent, agent2);

        // 3. 본인확인 불필요 - 다른 상담사 연결 (agent2 자리 비움 -> PENDING)
        needBoninHwackIn = false;
        needAnotherNumber = false;
        needAnotherAgent = true;
        runConsultation("시나리오 3: 본인확인 불필요 - 다른 상담사 연결", user, agent, agent2);

        // 4. 본인확인 불필요 - 단순 질문 답변 후 종료
        needBoninHwackIn = false;
        needAnotherNumber = false;
        needAnotherAgent = false;
        runConsultation("시나리오 4: 본인확인 불필요 - 단순 질문 답변", user, agent, agent2);
    }

    private static void runConsultation(String scenarioTitle, Users user, Users agent, Users agent2) {
        System.out.println("\n================ [" + scenarioTitle + "] ================");
        System.out.println("[조건] needBoninHwackIn=" + needBoninHwackIn
                + ", needAnotherNumber=" + needAnotherNumber
                + ", needAnotherAgent=" + needAnotherAgent);

        // ***************** 업무 진행 순서
        // 회원(사용자)이 전화를 건다.
        user.call("x23-4567-8901");

        // 업무 객체가 실행됨.
        Job job_01 = job1.jobsCallINIT("전화상담", null, "INIT");

        // 상담사는 걸려온 전화번호를 확인한다.
        chkUser = agent.checkPhoneNumber(chkNumber);

        if (chkUser != null) {
            // 회원 정보가 있으므로, 계속 진행. 또, Log로 기록남김.
            log1.logsChkUser(agent, job_01, "회원 전화번호 확인", chkNumber, LocalDateTime.now());
        } else {
            // 회원 정보가 없으므로, 단순 상담만 가능.
            job1.setDescription(job_01, "단순상담");
            job1.setStatus(job_01, "IN_PROGRESS");
            // 사용자와 상담을 해주고, 상담을 종료한다.
            job1.setStatus(job_01, "COMPLETED");
            user.endCall("x23-4567-8901");
            return;
        }

        // 상담사가 전화를 받는다.
        agent.answer("x23-4567-8901", "전화를 받았습니다.");

        // job. 업무상태가 IN_PROGRESS로 바뀐다.
        job1.setStatus(job_01, "IN_PROGRESS");

        // 상담사는 자신의 이름을 밝히고, 사용자(회원)에게 어떤 일로 상담을 하는지 물어본다.
        agent.answer("x23-4567-8901", "안녕하세요. ㅇㅇㅇ 상담사 첼시입니다. 무엇을 도와드릴까요?");

        // 사용자는 상담사에게 자신의 문의사항을 말한다.
        user.answer("x23-4567-8901", "안녕하세요. ㅇㅇ때문에 그런데요.");

        // 상담사는 사용자의 문의사항을 듣고, 상담 진행에 본인확인이 필요한지 판단하고, 상담을 진행한다.
        if (needBoninHwackIn) {
            // 1. 본인확인이 필요한 경우
            agent.answer("x23-4567-8901", "본인 확인이 필요한데요. 몇가지 여쭤보겠습니다.");
            user.answer("x23-4567-8901", "네.");

            agent.answer("x23-4567-8901", "성함이 어떻게 되시나요?");
            user.answer("x23-4567-8901", user.getName() + "입니다.");
            log1.logChkName(agent, job_01, "회원 성함 확인", user.getName(), LocalDateTime.now());

            agent.answer("x23-4567-8901", "본인 생년월일이 어떻게 되시나요?");
            user.answer("x23-4567-8901", "99011x입니다.");
            user.setBirthDay("99011x");
            log1.logChkBirthDay(agent, job_01, "회원 생년월일 확인", user.getBirthDay(), LocalDateTime.now());

            agent.answer("x23-4567-8901", "본인 주소가 어떻게 되시나요?");
            user.answer("x23-4567-8901", "부산시 해운대구 oo동 무슨아파트 몇동 몇호 입니다.");
            user.setAddress("해운대구 oo동 무슨아파트 몇동 몇호");
            log1.chkAddress(agent, job_01, "회원 주소 확인", user.getAddress(), LocalDateTime.now());

            // 상담사가 상담을 마치고, 상담 종료를 한다.
            job1.setDescription(job_01, "본인확인 상담 완료");
            job1.setStatus(job_01, "COMPLETED");
            user.endCall("x23-4567-8901");

        } else {
            // 2. 본인확인이 필요하지 않은 경우
            //    - 궁금한 것에 대해 답변을 해주거나, 더 잘 알 수 있는 전화번호를 안내한다.
            //    - 또, 다른 상담사에게 연결시켜준다.
            if (needAnotherNumber) {
                String anotherNumber = "0123-another-number";
                agent.answer("x23-4567-8901", "이쪽으로 전화부탁드립니다. 번호가.. " + anotherNumber + "입니다.");

                log1.logAnotherNumber(agent, job_01, "타 번호 안내", anotherNumber, LocalDateTime.now());
                // job의 상태가 COMPLETED로 바뀐다. (endDateTime 자동 기록)
                job1.setDescription(job_01, "타 번호 안내 완료");
                job1.setStatus(job_01, "COMPLETED");
                user.endCall("x23-4567-8901");

            } else if (needAnotherAgent) {
                agent.answer("x23-4567-8901", "그럼, 해당 상담사를 바꿔드릴게요. 기다려주세요.");

                if ("업무 중".equals(agent2.getStatus())) {
                    log1.logTransferAgent(agent, job_01, "상담사 연결 완료", agent2.getName(), LocalDateTime.now());
                    job1.setDescription(job_01, agent2.getName() + " 상담사로 연결");
                    job1.setStatus(job_01, "IN_PROGRESS");
                } else {
                    agent.answer("x23-4567-8901", agent2.getName() + " 상담사가 현재 '" + agent2.getStatus() + "' 상태라 잠시 대기 부탁드립니다.");
                    log1.logTransferAgent(agent, job_01, "상담사 연결 대기", agent2.getName() + "(" + agent2.getStatus() + ")", LocalDateTime.now());
                    // job의 상태가 PENDING으로 바뀐다.
                    job1.setDescription(job_01, "다른 상담사 연결 대기");
                    job1.setStatus(job_01, "PENDING");
                }

            } else {
                String answerMsg = "해당 질문에 대해서는 ... 이렇게 하시면 됩니다.";
                agent.answer("x23-4567-8901", answerMsg);

                log1.logSimpleAnswer(agent, job_01, "단순 문의 답변", answerMsg, LocalDateTime.now());
                // job의 상태가 COMPLETED로 바뀐다. (endDateTime 자동 기록)
                job1.setDescription(job_01, "단순 문의 상담 완료");
                job1.setStatus(job_01, "COMPLETED");
                user.endCall("x23-4567-8901");
            }
        }
    }

    @Override
    public Job jobsCallINIT(String jobName, String description, String status) {
        return new Job(jobName, description, status);
    }

    @Override
    public Log logsChkUser(Users user, Job job, String logName, String logDescription, LocalDateTime startDateTime) {
        return new Log(user, job, logName, logDescription, startDateTime);
    }

    @Override
    public void setStatus(Job job, String string) {
        job.setStatus(string);
    }

    @Override
    public void logChkName(Users agent, Job job_01, String string, String name, LocalDateTime now) {
        new Log(agent, job_01, string, name, now);
    }

    @Override
    public void logChkBirthDay(Users agent, Job job_01, String string, String birthDay, LocalDateTime now) {
        new Log(agent, job_01, string, birthDay, now);
    }

    @Override
    public void chkAddress(Users agent, Job job_01, String string, String address, LocalDateTime now) {
        new Log(agent, job_01, string, address, now);
    }

    @Override
    public void logAnotherNumber(Users agent, Job job_01, String logName, String phoneNumber, LocalDateTime now) {
        new Log(agent, job_01, logName, phoneNumber, now);
    }

    @Override
    public void logTransferAgent(Users agent, Job job_01, String logName, String targetAgentInfo, LocalDateTime now) {
        new Log(agent, job_01, logName, targetAgentInfo, now);
    }

    @Override
    public void logSimpleAnswer(Users agent, Job job_01, String logName, String answer, LocalDateTime now) {
        new Log(agent, job_01, logName, answer, now);
    }

    @Override
    public void setDescription(Job job_01, String string) {
        job_01.setDescription(string);
    }
}
