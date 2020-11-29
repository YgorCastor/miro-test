package ycastor.me.miro.widgets.dao;

import java.util.UUID;

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

}
