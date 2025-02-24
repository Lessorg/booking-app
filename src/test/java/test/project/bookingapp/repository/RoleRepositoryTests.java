package test.project.bookingapp.repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import test.project.bookingapp.model.Role;
import test.project.bookingapp.model.RoleName;

@DataJpaTest
@Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/db/add-test-roles.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryTests {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Find role by name")
    void findByName_RoleExists_ReturnsRole() {
        Role role = roleRepository.findByName(RoleName.ROLE_CUSTOMER).orElse(null);

        assertNotNull(role);
        assertEquals(RoleName.ROLE_CUSTOMER, role.getName());
    }

    @Test
    @DisplayName("Find role by name when role does not exist")
    void findByName_RoleNotExists_ReturnsEmpty() {
        Optional<Role> roleOptional = roleRepository.findByName(RoleName.ROLE_ADMIN);

        assertTrue(roleOptional.isEmpty());
    }
}
