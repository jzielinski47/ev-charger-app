package jz.pk.evcm.exception;

public class LastAdminException extends RuntimeException {
    public LastAdminException() {
        super("The last admin account cannot be deleted");
    }
}
