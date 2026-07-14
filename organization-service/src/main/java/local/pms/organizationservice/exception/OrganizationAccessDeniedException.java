package local.pms.organizationservice.exception;

public class OrganizationAccessDeniedException extends RuntimeException {

    public OrganizationAccessDeniedException(String message) {
        super(message);
    }
}
