package swing.repository;

import java.util.List;
import java.util.Optional;

import swing.entity.Role;
import swing.entity.User;

public interface UserRepository {

    Optional<User> findById(int id);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    List<User> findByRole(Role role);

    List<User> searchCustomers(String field, String keyword);

    User save(User user);

    void updateStatus(int userId, String status);

    void updateUserInfo(User user);
}
