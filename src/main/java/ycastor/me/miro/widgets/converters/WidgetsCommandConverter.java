package ycastor.me.miro.widgets.converters;

import org.springframework.stereotype.Component;

import ycastor.me.miro.widgets.commands.Command;
import ycastor.me.miro.widgets.commands.WidgetCreateCommand;
import ycastor.me.miro.widgets.commands.WidgetUpdateCommand;
import ycastor.me.miro.widgets.dao.models.Widget;
import ycastor.me.miro.widgets.exceptions.UnknownCommandException;

@Component
public class WidgetsCommandConverter {

    public Widget fromCommand(Command command, Integer zIndex) {
        if (command instanceof WidgetCreateCommand) {
            return fromCreateCommand(((WidgetCreateCommand) command).toBuilder().zIndex(zIndex).build());
        }

        if (command instanceof WidgetUpdateCommand) {
            return fromUpdateCommand(((WidgetUpdateCommand) command).toBuilder().zIndex(zIndex).build());
        }

        throw new UnknownCommandException();
    }

    private Widget fromCreateCommand(WidgetCreateCommand createCommand) {
        return Widget.builder()
                     .zIndex(createCommand.maybeZIndex().orElse(0))
                     .height(createCommand.getHeight())
                     .width(createCommand.getWidth())
                     .xAxis(createCommand.getXAxis())
                     .yAxis(createCommand.getYAxis())
                     .build();
    }

    private Widget fromUpdateCommand(WidgetUpdateCommand updateCommand) {
        return Widget.builder()
                     .zIndex(updateCommand.maybeZIndex().orElse(0))
                     .height(updateCommand.getHeight())
                     .width(updateCommand.getWidth())
                     .xAxis(updateCommand.getXAxis())
                     .yAxis(updateCommand.getYAxis())
                     .build();
    }

}
