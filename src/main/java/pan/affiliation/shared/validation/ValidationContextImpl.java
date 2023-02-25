package pan.affiliation.shared.validation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ValidationContextImpl implements ValidationContext {

    private final List<Error> errors;

    private ValidationStatus validationStatus;

    public ValidationContextImpl() {
        this.errors = new ArrayList<>();
    }

    @Override
    public List<Error> getErrors() {
        return Collections.unmodifiableList(this.errors);
    }

    @Override
    public Boolean hasErrors() {
        return this.errors.size() == 0;
    }

    @Override
    public ValidationStatus getValidationStatus() {
        return this.validationStatus;
    }

    @Override
    public void setStatus(ValidationStatus status) {
        this.validationStatus = status;
    }

    @Override
    public void addNotification(String key, String message) {
        this.errors.add(new Error(key, message));
    }

    @Override
    public void addNotification(Error error) {
        this.errors.add(error);
    }

    @Override
    public void addNotifications(List<Error> errors) {
        errors.forEach(e -> this.errors.add(e));
    }

    @Override
    public void addNotifications(ValidationResult validationResult) {
        validationResult.getErrors().forEach(e -> this.errors.add(e));
    }
}
