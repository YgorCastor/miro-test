package ycastor.me.miro.widgets.exceptions;

import java.util.UUID;

public class WidgetNotFoundException extends RuntimeException {
    public WidgetNotFoundException(UUID widgetId) {
        super(String.format("Widget with id <%s> was not found!", widgetId));
    }
}
