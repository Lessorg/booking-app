package test.project.bookingapp.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithSecurityContext;
import test.project.bookingapp.model.role.RoleName;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomSecurityContextFactory.class)
public @interface WithMockCustomUser {
    long id() default 1L;

    String email() default "testuser@gmail.com";

    RoleName role() default RoleName.ROLE_CUSTOMER;
}
