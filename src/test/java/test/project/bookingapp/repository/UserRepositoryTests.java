package test.project.bookingapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.project.bookingapp.model.User;
import test.project.bookingapp.model.role.RoleName;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/db/add-test-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTests {
    private static final String EXISTENT_EMAIL = "test@example.com";
    private static final String NONEXISTENT_EMAIL = "nonexistent@example.com";

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Check if email exists in database")
    void existsByEmail_EmailExists_ReturnsTrue() {
        boolean exists = userRepository.existsByEmail(EXISTENT_EMAIL);
        assertTrue(exists);
    }

    @Test
    @DisplayName("Check if email does not exist in database")
    void existsByEmail_EmailNotExists_ReturnsFalse() {
        boolean exists = userRepository.existsByEmail(NONEXISTENT_EMAIL);
        assertFalse(exists);
    }

    @Test
    @DisplayName("Find user by email when user exists")
    void findByEmail_UserExists_ReturnsUserWithRoles() {
        Optional<User> userOptional = userRepository.findByEmail(EXISTENT_EMAIL);

        assertTrue(userOptional.isPresent());
        User user = userOptional.get();
        assertEquals(EXISTENT_EMAIL, user.getEmail());
        assertFalse(user.getRoles().isEmpty());
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().stream().anyMatch(
                role -> role.getName() == RoleName.ROLE_CUSTOMER));
    }

    @Test
    @DisplayName("Find user by email when user does not exist")
    void findByEmail_UserNotExists_ReturnsEmpty() {
        Optional<User> userOptional = userRepository.findByEmail(NONEXISTENT_EMAIL);
        assertTrue(userOptional.isEmpty());
    }
}
