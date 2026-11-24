package swing.test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import swing.db.DatabaseConfig;
import swing.db.DatabaseManager;
import swing.entity.AgentWorkLog;
import swing.entity.Job;
import swing.entity.JobStatus;
import swing.entity.Log;
import swing.entity.Role;
import swing.entity.User;
import swing.repository.AgentWorkLogRepository;
import swing.repository.JobRepository;
import swing.repository.LogRepository;
import swing.repository.UserRepository;
import swing.service.AgentWorkLogService;
import swing.service.AgentWorkLogServiceIF;
import swing.service.AuthService;
import swing.service.JobService;
import swing.service.JobsServiceIF;
import swing.service.LogService;
import swing.service.LogsServiceIF;
import swing.service.UserService;

/**
 * swing.* 서비스/인터페이스/시나리오 1~4 자동 검증 테스트 (In-Memory Repository 기반)
 */
public class SwingServiceTest {

    public static void main(String[] args) {
        System.out.println("=== SwingServiceTest 시작 ===");

        // 1. MySQL JDBC 드라이버 로드 확인
        DatabaseManager dbManager = new DatabaseManager(DatabaseConfig.load());
        assertTrue(dbManager.getConfig() != null, "DatabaseConfig 로드 확인");
        System.out.println("[PASS] MySQL JDBC 드라이버(com.mysql.cj.jdbc.Driver) 로드 확인");

        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        InMemoryJobRepository jobRepo = new InMemoryJobRepository();
        InMemoryLogRepository logRepo = new InMemoryLogRepository();

        // 초기 테스트 데이터 적재
        User chelsea = userRepo.save(new User("첼시", "chelsea@pims.com", "1234", Role.상담사, "02-1000-0001", "950310", "서울시 중구", "업무 중"));
        User mickey = userRepo.save(new User("미키", "mickey@pims.com", "1234", Role.상담사, "02-1000-0002", "930822", "서울시 중구", "자리 비움"));
        User lucy = userRepo.save(new User("루시", "lucy@pims.com", "1234", Role.상담사, "02-1000-0003", "970105", "서울시 중구", "업무 중"));
        User john = userRepo.save(new User("John Doe", "john@example.com", "pass1234", Role.사용자, "010-user-xxxx", "99011x", "부산시 해운대구 xx동", "정상"));

        AuthService authService = new AuthService(userRepo);
        UserService userService = new UserService(userRepo);
        JobsServiceIF jobService = new JobService(jobRepo);
        LogsServiceIF logService = new LogService(logRepo);

        // 2. 상담사 로그인 및 Role 권한 검증
        User loggedIn = authService.login("chelsea@pims.com", "1234");
        assertTrue(loggedIn.getId() == chelsea.getId(), "상담사 로그인 성공");
        assertTrue("업무 중".equals(loggedIn.getStatus()), "로그인 후 상담사 상태 업무 중");

        boolean customerBlocked = false;
        try {
            authService.login("john@example.com", "pass1234");
        } catch (IllegalArgumentException e) {
            customerBlocked = true;
        }
        assertTrue(customerBlocked, "일반 사용자(Role.사용자) 로그인 차단");
        System.out.println("[PASS] 상담사 로그인 및 Role 권한 검증 통과");

        // 3. 회원 검색 및 본인확인 검증
        assertTrue(userService.checkPhoneNumber("010-user-xxxx").isPresent(), "전화번호로 회원 조회");
        assertTrue(userService.searchCustomers("이름", "John").size() == 1, "이름 검색");
        assertTrue(userService.verifyName(john, "John Doe"), "성함 본인확인 일치");
        assertTrue(userService.verifyBirthDay(john, "99011x"), "생년월일 본인확인 일치");
        assertTrue(userService.verifyAddress(john, "해운대구 xx동"), "주소 본인확인 부분 일치");
        assertTrue(!userService.verifyBirthDay(john, "000000"), "생년월일 불일치 검증");
        System.out.println("[PASS] 회원 검색 및 본인확인(성함·생년월일·주소) 검증 통과");

        // 4. 시나리오 1: 본인확인 상담 완료
        Job job1 = jobService.jobsCallINIT("전화상담", null, "INIT", "010-user-xxxx", john, loggedIn);
        logService.logsChkUser(loggedIn, job1, "회원 전화번호 확인", "010-user-xxxx", LocalDateTime.now());
        jobService.setStatus(job1, JobStatus.IN_PROGRESS);
        logService.logChkName(loggedIn, job1, "회원 성함 확인", "John Doe (일치)", LocalDateTime.now());
        logService.logChkBirthDay(loggedIn, job1, "회원 생년월일 확인", "99011x (일치)", LocalDateTime.now());
        logService.chkAddress(loggedIn, job1, "회원 주소 확인", "부산시 해운대구 xx동 (일치)", LocalDateTime.now());
        jobService.setDescription(job1, "본인확인 상담 완료");
        jobService.setStatus(job1, JobStatus.COMPLETED);
        assertTrue(job1.getStatus() == JobStatus.COMPLETED && job1.getEndDateTime() != null, "시나리오 1 COMPLETED & 종료시각 기록");
        assertTrue(logService.findLogsByJobId(job1.getId()).size() == 4, "시나리오 1 로그 4건 기록");
        System.out.println("[PASS] 시나리오 1 (본인확인) 통과");

        // 5. 시나리오 2: 타 전화번호 안내
        Job job2 = jobService.jobsCallINIT("전화상담", null, "INIT", "010-user-xxxx", john, loggedIn);
        jobService.setStatus(job2, JobStatus.IN_PROGRESS);
        logService.logAnotherNumber(loggedIn, job2, "타 번호 안내", "0123-another-number", LocalDateTime.now());
        jobService.setDescription(job2, "타 번호 안내 완료");
        jobService.setStatus(job2, JobStatus.COMPLETED);
        assertTrue(job2.getStatus() == JobStatus.COMPLETED, "시나리오 2 COMPLETED");
        System.out.println("[PASS] 시나리오 2 (타 전화번호 안내) 통과");

        // 6. 시나리오 3: 다른 상담사 연결 (미키: 자리 비움 -> PENDING, 루시: 업무 중 -> COMPLETED)
        Job job3 = jobService.jobsCallINIT("전화상담", null, "INIT", "010-user-xxxx", john, loggedIn);
        jobService.setStatus(job3, JobStatus.IN_PROGRESS);
        jobService.setTargetAgent(job3, mickey);
        logService.logTransferAgent(loggedIn, job3, "상담사 연결 대기", mickey.getName() + "(" + mickey.getStatus() + ")", LocalDateTime.now());
        jobService.setDescription(job3, "다른 상담사(미키) 연결 대기");
        jobService.setStatus(job3, JobStatus.PENDING);
        assertTrue(job3.getStatus() == JobStatus.PENDING && job3.getEndDateTime() == null, "시나리오 3(자리 비움) PENDING 유지");
        assertTrue("업무 중".equals(lucy.getStatus()), "동료 상담사 루시 업무 중 상태 확인");
        System.out.println("[PASS] 시나리오 3 (다른 상담사 연결 대기 PENDING) 통과");

        // 7. 시나리오 4: 단순 질문 답변 + 비회원 단순상담
        Job job4 = jobService.jobsCallINIT("전화상담", null, "INIT", "010-9999-0000", null, loggedIn);
        assertTrue(job4.getUserId() == null, "비회원 userId null 확인");
        logService.logSimpleAnswer(loggedIn, job4, "단순 문의 답변", "안내드린 절차대로 진행하시면 됩니다.", LocalDateTime.now());
        jobService.setDescription(job4, "단순 문의 상담 완료");
        jobService.setStatus(job4, JobStatus.COMPLETED);
        assertTrue(job4.getStatus() == JobStatus.COMPLETED, "시나리오 4 COMPLETED");
        System.out.println("[PASS] 시나리오 4 (단순 질문 답변 / 비회원) 통과");

        // 8. 임의검색 로그 기록 검증 (Job ID·회원은 null, 담당상담사는 로그인한 상담사 기록 / 항목="임의검색" / 상세내용=텍스트필드 값)
        Log arbitraryLog = logService.logArbitrarySearch(loggedIn, "010-1234-5678", LocalDateTime.now());
        assertTrue(arbitraryLog.getJobId() == null, "임의검색 Job ID는 null");
        assertTrue(Integer.valueOf(loggedIn.getId()).equals(arbitraryLog.getAgentId()), "임의검색 담당상담사 ID는 로그인한 상담사");
        assertTrue("첼시".equals(arbitraryLog.getAgentName()), "임의검색 담당상담사 이름은 첼시");
        assertTrue(arbitraryLog.getUserId() == null, "임의검색 회원 ID는 null");
        assertTrue("임의검색".equals(arbitraryLog.getLogName()), "임의검색 logName 검증");
        assertTrue("010-1234-5678".equals(arbitraryLog.getLogDescription()), "임의검색 상세내용 텍스트필드 값 그대로 저장");
        assertTrue(arbitraryLog.getCreatedAt() != null, "임의검색 기록시각 저장");
        System.out.println("[PASS] 임의검색 로그 기록(담당상담사 이름 포함, JobID/회원 null) 검증 통과");

        // 9. 전체 Job / Log 조회 검증
        assertTrue(jobService.findAllJobs().size() == 4, "전체 Job 4건 조회");
        assertTrue(jobService.findJobsByStatus(JobStatus.PENDING).size() == 1, "PENDING Job 1건 조회");
        assertTrue(logService.findAllLogs().size() == 8, "전체 Log 8건 조회");
        assertTrue(logService.findLogsByJobId(job1.getId()).size() == 4, "특정 Job 로그 조회 시 null jobId 안전 처리");
        assertTrue(logService.searchLogs("타 번호").size() == 1, "Log 키워드 검색");
        assertTrue(logService.searchLogs("임의검색").size() == 1, "임의검색 로그 키워드 검색");

        // 10. 상담사 업무기록(AgentWorkLog) 검증 (로그인, 자리비움, 업무중, 로그아웃)
        InMemoryAgentWorkLogRepository workLogRepo = new InMemoryAgentWorkLogRepository();
        AgentWorkLogServiceIF workLogService = new AgentWorkLogService(workLogRepo);
        AgentWorkLog w1 = workLogService.recordLogin(loggedIn);
        AgentWorkLog w2 = workLogService.recordStatusChange(loggedIn, "자리 비움");
        AgentWorkLog w3 = workLogService.recordStatusChange(loggedIn, "업무 중");
        AgentWorkLog w4 = workLogService.recordLogout(loggedIn);
        assertTrue("업무기록".equals(w1.getCategory()) && "로그인".equals(w1.getDetail()) && "첼시".equals(w1.getAgentName()), "로그인 업무기록");
        assertTrue("자리비움".equals(w2.getDetail()), "자리비움 업무기록");
        assertTrue("업무중".equals(w3.getDetail()), "업무중 업무기록");
        assertTrue("로그아웃".equals(w4.getDetail()) && w4.getRecordedAt() != null, "로그아웃 업무기록");
        assertTrue(workLogService.findMyWorkLogs(loggedIn.getId()).size() == 4, "본인 업무기록 4건 조회");
        assertTrue(workLogService.findMyWorkLogsByDetail(loggedIn.getId(), "자리비움").size() == 1, "상세내용 필터 조회");
        System.out.println("[PASS] 상담사 업무기록(로그인/자리비움/업무중/로그아웃) 검증 통과");

        System.out.println("=== SwingServiceTest 전체 통과 (ALL TESTS PASSED) ===");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    // --- 테스트용 In-Memory Repository 구현체 ---

    private static class InMemoryUserRepository implements UserRepository {
        private final List<User> store = new ArrayList<>();
        private int seq = 1;

        @Override
        public Optional<User> findById(int id) {
            return store.stream().filter(u -> u.getId() == id).findFirst();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return store.stream().filter(u -> email.equals(u.getEmail())).findFirst();
        }

        @Override
        public Optional<User> findByPhoneNumber(String phoneNumber) {
            return store.stream()
                    .filter(u -> u.getRole() == Role.사용자 && phoneNumber.equals(u.getPhoneNumber()))
                    .findFirst();
        }

        @Override
        public List<User> findByRole(Role role) {
            return store.stream().filter(u -> u.getRole() == role).collect(Collectors.toList());
        }

        @Override
        public List<User> searchCustomers(String field, String keyword) {
            String k = keyword == null ? "" : keyword.trim().toLowerCase();
            return store.stream()
                    .filter(u -> u.getRole() == Role.사용자)
                    .filter(u -> k.isEmpty()
                            || (u.getName() != null && u.getName().toLowerCase().contains(k))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(k))
                            || (u.getPhoneNumber() != null && u.getPhoneNumber().toLowerCase().contains(k)))
                    .collect(Collectors.toList());
        }

        @Override
        public User save(User user) {
            user.setId(seq++);
            store.add(user);
            return user;
        }

        @Override
        public void updateStatus(int userId, String status) {
            findById(userId).ifPresent(u -> u.setStatus(status));
        }

        @Override
        public void updateUserInfo(User user) {
            // in-memory 객체 참조 직접 갱신됨
        }
    }

    private static class InMemoryJobRepository implements JobRepository {
        private final List<Job> store = new ArrayList<>();
        private int seq = 1;

        @Override
        public Job save(Job job) {
            job.setId(seq++);
            store.add(job);
            return job;
        }

        @Override
        public void update(Job job) {
            // in-memory 객체 참조 직접 갱신됨
        }

        @Override
        public Optional<Job> findById(int id) {
            return store.stream().filter(j -> j.getId() == id).findFirst();
        }

        @Override
        public List<Job> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public List<Job> findByStatus(JobStatus status) {
            return store.stream().filter(j -> j.getStatus() == status).collect(Collectors.toList());
        }
    }

    private static class InMemoryLogRepository implements LogRepository {
        private final List<Log> store = new ArrayList<>();
        private int seq = 1;

        @Override
        public Log save(Log log) {
            log.setId(seq++);
            store.add(log);
            return log;
        }

        @Override
        public List<Log> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public List<Log> findByJobId(int jobId) {
            return store.stream()
                    .filter(l -> Integer.valueOf(jobId).equals(l.getJobId()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<Log> search(String keyword) {
            String k = keyword == null ? "" : keyword.trim();
            return store.stream()
                    .filter(l -> (l.getLogName() != null && l.getLogName().contains(k))
                            || (l.getLogDescription() != null && l.getLogDescription().contains(k)))
                    .collect(Collectors.toList());
        }
    }

    private static class InMemoryAgentWorkLogRepository implements AgentWorkLogRepository {
        private final List<AgentWorkLog> store = new ArrayList<>();
        private int seq = 1;

        @Override
        public AgentWorkLog save(AgentWorkLog workLog) {
            workLog.setId(seq++);
            store.add(workLog);
            return workLog;
        }

        @Override
        public List<AgentWorkLog> findByAgentId(int agentId) {
            return store.stream().filter(w -> w.getAgentId() == agentId).collect(Collectors.toList());
        }

        @Override
        public List<AgentWorkLog> findByAgentIdAndDetail(int agentId, String detail) {
            return store.stream()
                    .filter(w -> w.getAgentId() == agentId && detail.equals(w.getDetail()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<AgentWorkLog> findAll() {
            return new ArrayList<>(store);
        }
    }
}
