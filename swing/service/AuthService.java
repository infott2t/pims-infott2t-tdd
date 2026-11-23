package swing.service;

import java.util.List;
import java.util.stream.Collectors;

import swing.entity.Role;
import swing.entity.User;
import swing.repository.UserRepository;

/**
 * 상담사 로그인 인증 및 상담사 상태 관리 서비스
 */
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 상담사(Role.상담사) 전용 로그인 인증을 수행합니다.
     */
    public User login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해 주세요.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해 주세요.");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다: " + email));

        if (!password.equals(user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (user.getRole() != Role.상담사) {
            throw new IllegalArgumentException("상담사 권한(Role.상담사) 계정만 로그인할 수 있습니다.");
        }

        // 로그인 시 상담사 상태를 "업무 중"으로 갱신
        user.setStatus("업무 중");
        userRepository.updateStatus(user.getId(), "업무 중");
        return user;
    }

    public void updateAgentStatus(User agent, String status) {
        agent.setStatus(status);
        userRepository.updateStatus(agent.getId(), status);
    }

    public List<User> getAllAgents() {
        return userRepository.findByRole(Role.상담사);
    }

    public List<User> getOtherAgents(int currentAgentId) {
        return userRepository.findByRole(Role.상담사).stream()
                .filter(a -> a.getId() != currentAgentId)
                .collect(Collectors.toList());
    }
}
