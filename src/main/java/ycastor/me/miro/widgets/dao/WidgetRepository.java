package ycastor.me.miro.widgets.dao;

import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.data.domain.Pageable;

import cyclops.control.Option;
import cyclops.data.ImmutableSortedSet;
import ycastor.me.miro.widgets.commands.FilterArea;
import ycastor.me.miro.widgets.dao.models.Widget;

public interface WidgetRepository {

    ImmutableSortedSet<Widget> listAll(Pageable pageRequest);

    Widget save(Widget newWidget, ImmutableSortedSet<Widget> collidedWidgets);

    Widget delete(UUID widgetId);

    Integer findLargestZIndex();

    ImmutableSortedSet<Widget> findFromZIndexUntilGap(Integer zIndex);

    Option<Widget> findById(UUID id);

    ImmutableSortedSet<Widget> listAllWithinArea(FilterArea filterArea);

    default Predicate<Widget> toGap(ImmutableSortedSet<Widget> currentState) {
        return widget -> {
            var currentIndex = currentState.indexOf(it -> it.getId().equals(widget.getId())).orElse(0L).intValue();
            var previousIndex = currentIndex == 0 ? currentIndex : currentIndex - 1;
            var lastZIndex = currentState.get(previousIndex).map(Widget::getZIndex).orElse(-1);
            return widget.getZIndex() <= (lastZIndex + 1);
        };
    }

}
