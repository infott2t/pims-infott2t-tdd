package swing.service;

import java.util.List;
import java.util.Optional;

import swing.entity.Role;
import swing.entity.User;
import swing.repository.UserRepository;

/**
 * 회원정보 조회, 등록 및 본인확인(이름·생년월일·주소) 검증 서비스
 */
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> checkPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByPhoneNumber(phoneNumber.trim());
    }

    public List<User> searchCustomers(String field, String keyword) {
        return userRepository.searchCustomers(field, keyword);
    }

    public List<User> findAllCustomers() {
        return userRepository.findByRole(Role.사용자);
    }

    public User registerCustomer(String name, String email, String phoneNumber, String birthDay, String address) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("회원 이름은 필수입니다.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        if (userRepository.findByEmail(email.trim()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + email);
        }
        User customer = new User(
                name.trim(),
                email.trim(),
                "pass1234",
                Role.사용자,
                phoneNumber.trim(),
                birthDay != null ? birthDay.trim() : "",
                address != null ? address.trim() : "",
                "정상"
        );
        return userRepository.save(customer);
    }

    public boolean verifyName(User customer, String inputName) {
        if (customer == null || customer.getName() == null || inputName == null) {
            return false;
        }
        return customer.getName().trim().equalsIgnoreCase(inputName.trim());
    }

    public boolean verifyBirthDay(User customer, String inputBirthDay) {
        if (customer == null || customer.getBirthDay() == null || inputBirthDay == null) {
            return false;
        }
        return customer.getBirthDay().trim().equalsIgnoreCase(inputBirthDay.trim());
    }

    public boolean verifyAddress(User customer, String inputAddress) {
        if (customer == null || customer.getAddress() == null || inputAddress == null || inputAddress.isBlank()) {
            return false;
        }
        String stored = customer.getAddress().replaceAll("\\s+", "");
        String given = inputAddress.replaceAll("\\s+", "");
        return stored.equalsIgnoreCase(given) || stored.contains(given) || given.contains(stored);
    }
}
