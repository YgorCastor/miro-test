package ycastor.me.miro.utils;

import ycastor.me.miro.widgets.commands.FilterArea;
import ycastor.me.miro.widgets.dao.models.Widget;

public class WidgetUtils {

    public static boolean withinArea(Widget widget, FilterArea filterArea) {
        var yAxisInside = widget.yAxisMiddlePoint() > filterArea.getLowerLeft().getYAxis() && widget.yAxisMiddlePoint() < filterArea.getUpperRight().getYAxis();
        var xAxisInside = widget.xAxisMiddlePoint() > filterArea.getLowerLeft().getXAxis() && widget.xAxisMiddlePoint() < filterArea.getUpperRight().getXAxis();
        return xAxisInside && yAxisInside;
    }

}
