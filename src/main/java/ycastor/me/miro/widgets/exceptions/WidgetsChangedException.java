package ycastor.me.miro.widgets.exceptions;

import java.util.UUID;

public class WidgetsChangedException extends RuntimeException {
    public WidgetsChangedException(UUID widgetId) {
        super(String.format("Failed to update/create the widget <%s>, someone changes the board state", widgetId));
    }
}
