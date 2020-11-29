package ycastor.me.miro.widgets.exceptions;

public class UnknownCommandException extends RuntimeException {
    public UnknownCommandException() {
        super("Unknown Command");
    }
}
