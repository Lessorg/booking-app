package test.project.bookingapp.config;

import java.util.Set;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import test.project.bookingapp.model.User;
import test.project.bookingapp.model.role.Role;
import test.project.bookingapp.model.role.RoleName;

public class CustomSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        User user = new User();
        user.setId(customUser.id());
        user.setEmail(customUser.email());
        user.setPassword("password");
        Role role = new Role();
        role.setName(RoleName.ROLE_CUSTOMER);
        user.setRoles(Set.of(role));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(user,
                null, user.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}

