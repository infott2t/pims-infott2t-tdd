package swing.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import swing.db.DatabaseManager;
import swing.entity.Role;
import swing.entity.User;
import swing.repository.UserRepository;

public class JdbcUserRepository implements UserRepository {

    private final DatabaseManager dbManager;

    public JdbcUserRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT id, name, email, password, role, phone_number, birth_day, address, status "
                + "FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("회원 조회(ID) 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, name, email, password, role, phone_number, birth_day, address, status "
                + "FROM users WHERE email = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("회원 조회(이메일) 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        String sql = "SELECT id, name, email, password, role, phone_number, birth_day, address, status "
                + "FROM users WHERE phone_number = ? AND role = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phoneNumber);
            ps.setString(2, Role.사용자.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("회원 조회(전화번호) 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findByRole(Role role) {
        String sql = "SELECT id, name, email, password, role, phone_number, birth_day, address, status "
                + "FROM users WHERE role = ? ORDER BY id ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("역할별 사용자 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> searchCustomers(String field, String keyword) {
        String trimmed = (keyword == null) ? "" : keyword.trim();
        if (trimmed.isEmpty()) {
            return findByRole(Role.사용자);
        }

        String likePattern = "%" + trimmed + "%";
        String sql;
        boolean searchAll = false;

        if ("전화번호".equals(field)) {
            sql = "SELECT id, name, email, password, role, phone_number, birth_day, address, status "
                    + "FROM users WHERE role = ? AND phone_number LIKE ? ORDER BY id ASC";
        } else if ("이름".equals(field)) {
            sql = "SELECT id, name, email, password, role, phone_number, birth_day, address, status "
                    + "FROM users WHERE role = ? AND name LIKE ? ORDER BY id ASC";
        } else if ("이메일".equals(field)) {
            sql = "SELECT id, name, email, password, role, phone_number, birth_day, address, status "
                    + "FROM users WHERE role = ? AND email LIKE ? ORDER BY id ASC";
        } else {
            searchAll = true;
            sql = "SELECT id, name, email, password, role, phone_number, birth_day, address, status "
                    + "FROM users WHERE role = ? AND (name LIKE ? OR email LIKE ? OR phone_number LIKE ? OR address LIKE ?) "
                    + "ORDER BY id ASC";
        }

        List<User> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Role.사용자.name());
            if (searchAll) {
                ps.setString(2, likePattern);
                ps.setString(3, likePattern);
                ps.setString(4, likePattern);
                ps.setString(5, likePattern);
            } else {
                ps.setString(2, likePattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("회원 검색 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (name, email, password, role, phone_number, birth_day, address, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword() != null ? user.getPassword() : "pass1234");
            ps.setString(4, user.getRole() != null ? user.getRole().name() : Role.사용자.name());
            ps.setString(5, user.getPhoneNumber());
            ps.setString(6, user.getBirthDay());
            ps.setString(7, user.getAddress());
            ps.setString(8, user.getStatus() != null ? user.getStatus() : "정상");
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("회원 등록 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("상태 변경 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateUserInfo(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, phone_number = ?, birth_day = ?, address = ?, status = ? "
                + "WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhoneNumber());
            ps.setString(4, user.getBirthDay());
            ps.setString(5, user.getAddress());
            ps.setString(6, user.getStatus());
            ps.setInt(7, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("회원 정보 수정 실패: " + e.getMessage(), e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                Role.fromString(rs.getString("role")),
                rs.getString("phone_number"),
                rs.getString("birth_day"),
                rs.getString("address"),
                rs.getString("status")
        );
    }
}
