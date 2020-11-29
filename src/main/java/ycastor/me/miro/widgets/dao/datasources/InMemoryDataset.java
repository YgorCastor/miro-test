package ycastor.me.miro.widgets.dao.datasources;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import cyclops.control.Option;
import cyclops.data.ImmutableSortedSet;
import cyclops.data.TreeSet;
import cyclops.data.Vector;
import ycastor.me.miro.widgets.commands.FilterArea;
import ycastor.me.miro.widgets.dao.WidgetRepository;
import ycastor.me.miro.widgets.dao.models.Widget;
import ycastor.me.miro.widgets.exceptions.WidgetNotFoundException;
import ycastor.me.miro.widgets.exceptions.WidgetsChangedException;

import static ycastor.me.miro.utils.WidgetUtils.withinArea;

@Component
@ConditionalOnProperty(value = "miro.datasource", havingValue = "IN_MEMORY")
public class InMemoryDataset implements WidgetRepository {

    private final AtomicReference<ImmutableSortedSet<Widget>> widgetsState = new AtomicReference<>();

    public InMemoryDataset() {
        widgetsState.set(TreeSet.empty());
    }

    @Override
    public Option<Widget> findById(UUID id) {
        var currentState = widgetsState.get();
        return currentState.filter(widget -> widget.getId().equals(id)).first();
    }

    @Override
    public ImmutableSortedSet<Widget> listAll(Pageable pageRequest) {
        var currentState = widgetsState.get();
        var grouped = currentState.grouped(pageRequest.getPageSize());
        var page = grouped.get(pageRequest.getPageNumber()).orElse(Vector.empty());
        return TreeSet.fromIterable(page);
    }

    @Override
    public ImmutableSortedSet<Widget> listAllWithinArea(FilterArea filterArea) {
        var currentState = widgetsState.get();
        return currentState.filter(widget -> withinArea(widget, filterArea));
    }

    @Override
    public Widget save(Widget newWidget, ImmutableSortedSet<Widget> collidedWidgets) {
        var widgetWithId = generateId(newWidget);
        var currentState = widgetsState.get();
        var withUpdatedCollisions = collidedWidgets.isEmpty() ? currentState : collidedWidgets.flatMap(it -> appendOrReplace(currentState, it));
        var withUpdatedWidget = appendOrReplace(withUpdatedCollisions, widgetWithId);

        boolean success = widgetsState.compareAndSet(currentState, withUpdatedWidget);

        if (!success) {
            throw new WidgetsChangedException(widgetWithId.getId());
        }

        return findById(widgetWithId.getId()).orElseGet(() -> { throw new WidgetNotFoundException(widgetWithId.getId()); });
    }

    @Override
    public Widget delete(UUID widgetId) {
        var currentState = widgetsState.get();
        return currentState.filter(widget -> widget.getId().equals(widgetId))
                           .first()
                           .fold(widget -> {
                               var updatedState = currentState.removeValue(widget);
                               boolean success = widgetsState.compareAndSet(currentState, updatedState);

                               if (!success) {
                                   throw new WidgetsChangedException(widgetId);
                               }

                               return widget;
                           }, () -> { throw new WidgetNotFoundException(widgetId); });
    }

    @Override
    public Integer findLargestZIndex() {
        var currentState = widgetsState.get();
        return currentState.last()
                           .fold(Widget::getZIndex, () -> 0);
    }

    @Override
    public ImmutableSortedSet<Widget> findFromZIndexUntilGap(Integer zIndex) {
        var currentState = widgetsState.get();
        return currentState.dropUntil(widget -> zIndex.equals(widget.getZIndex()))
                           .takeWhile(widget -> {
                               var currentIndex = currentState.indexOf(it -> it.getId().equals(widget.getId())).orElse(0L).intValue();
                               var previousIndex = currentIndex == 0 ? currentIndex : currentIndex - 1;
                               var lastZIndex = currentState.get(previousIndex).map(Widget::getZIndex).orElse(-1);
                               return widget.getZIndex() <= (lastZIndex + 1);
                           });
    }

    private ImmutableSortedSet<Widget> appendOrReplace(ImmutableSortedSet<Widget> set, Widget widget) {
        var withRemoved = TreeSet.fromIterable(set.removeFirst(w -> w.getId().equals(widget.getId())));
        return withRemoved.add(widget);
    }

    private Widget generateId(Widget newWidget) {
        if (newWidget.getId() == null) {
            return newWidget.toBuilder().id(UUID.randomUUID()).build();
        }

        return newWidget;
    }
}
