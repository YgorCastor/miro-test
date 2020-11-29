package ycastor.me.miro.fixtures;

import java.util.UUID;

import ycastor.me.miro.widgets.commands.WidgetCreateCommand;
import ycastor.me.miro.widgets.commands.WidgetUpdateCommand;
import ycastor.me.miro.widgets.dao.models.Widget;

public class WidgetFixture {

    public static Widget oneWidget() {
        return Widget.builder()
                     .xAxis(5)
                     .yAxis(25)
                     .width(100)
                     .height(100)
                     .zIndex(0)
                     .build();
    }

    public static WidgetCreateCommand widgetCreateCommand() {
        return WidgetCreateCommand.builder()
                                  .zIndex(0)
                                  .height(100)
                                  .width(100)
                                  .xAxis(1)
                                  .yAxis(1)
                                  .build();
    }

    public static WidgetUpdateCommand widgetUpdateCommand() {
        return WidgetUpdateCommand.builder()
                                  .zIndex(0)
                                  .height(100)
                                  .width(100)
                                  .xAxis(1)
                                  .yAxis(1)
                                  .build();
    }
}
