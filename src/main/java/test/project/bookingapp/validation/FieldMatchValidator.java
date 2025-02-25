package test.project.bookingapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;
import java.util.Objects;
import org.springframework.beans.BeanWrapperImpl;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(final FieldMatch constraintAnnotation) {
        firstFieldName = constraintAnnotation.firstField();
        secondFieldName = constraintAnnotation.secondField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Object field = new BeanWrapperImpl(value).getPropertyValue(firstFieldName);
            Object fieldMatch = new BeanWrapperImpl(value).getPropertyValue(secondFieldName);

            return Objects.equals(field, fieldMatch);
        } catch (Exception exception) {
            throw new ValidationException("Can't compare objects " + exception);
        }
    }
}
