package test.project.bookingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.project.bookingapp.model.role.Role;
import test.project.bookingapp.model.role.RoleName;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Sql(scripts = "/db/clean-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/db/add-test-roles.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTests {
    private static final RoleName EXISTING_ROLE = RoleName.ROLE_CUSTOMER;
    private static final RoleName NON_EXISTENT_ROLE = RoleName.ROLE_ADMIN;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Find role by name when role exists")
    void findByName_RoleExists_ReturnsRole() {
        Optional<Role> roleOptional = roleRepository.findByName(EXISTING_ROLE);

        assertThat(roleOptional).isPresent();
        assertThat(roleOptional.get().getName()).isEqualTo(EXISTING_ROLE);
    }

    @Test
    @DisplayName("Find role by name when role does not exist")
    void findByName_RoleNotExists_ReturnsEmpty() {
        Optional<Role> roleOptional = roleRepository.findByName(NON_EXISTENT_ROLE);

        assertThat(roleOptional).isEmpty();
    }
}
