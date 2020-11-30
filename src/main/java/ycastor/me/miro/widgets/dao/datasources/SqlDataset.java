package ycastor.me.miro.widgets.dao.datasources;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cyclops.control.Option;
import cyclops.data.ImmutableSortedSet;
import cyclops.data.TreeSet;
import ycastor.me.miro.widgets.commands.FilterArea;
import ycastor.me.miro.widgets.dao.WidgetRepository;
import ycastor.me.miro.widgets.dao.models.Widget;
import ycastor.me.miro.widgets.exceptions.WidgetNotFoundException;

@Component
@ConditionalOnProperty(value = "miro.datasource", havingValue = "SQL")
public class SqlDataset implements WidgetRepository {

    private final WidgetSqlRepository widgetSqlRepository;

    @Autowired
    public SqlDataset(WidgetSqlRepository widgetSqlRepository) {
        this.widgetSqlRepository = widgetSqlRepository;
    }

    @Override
    public ImmutableSortedSet<Widget> listAll(Pageable pageRequest) {
        var widgets = widgetSqlRepository.findAll(pageRequest);
        return TreeSet.fromIterable(widgets);
    }

    @Override
    @Transactional
    public Widget save(Widget newWidget, ImmutableSortedSet<Widget> collidedWidgets) {
        widgetSqlRepository.saveAll(collidedWidgets);
        return widgetSqlRepository.save(newWidget);
    }

    @Override
    @Transactional
    public Widget delete(UUID widgetId) {
        var found = findById(widgetId);
        return found.fold(
                widget -> {
                    widgetSqlRepository.delete(widget);
                    return widget;
                },
                () -> { throw new WidgetNotFoundException(widgetId); }
        );
    }

    @Override
    public Integer findLargestZIndex() {
        var zindex = widgetSqlRepository.findLargestZIndex();
        return zindex != null ? zindex : 0;
    }

    @Override
    public ImmutableSortedSet<Widget> findFromZIndexUntilGap(Integer zIndex) {
        TreeSet<Widget> widgets = TreeSet.fromIterable(widgetSqlRepository.findAllByzIndexGreaterThanEqual(zIndex));

        return widgets.takeWhile(toGap(widgets));
    }

    @Override
    public Option<Widget> findById(UUID id) {
        var found = widgetSqlRepository.findById(id);
        return Option.fromOptional(found);
    }

    @Override
    public ImmutableSortedSet<Widget> listAllWithinArea(FilterArea filterArea) {
        var currentState = widgetSqlRepository.findAllWithinArea(
                filterArea.getLowerLeft().getXAxis(),
                filterArea.getLowerLeft().getYAxis(),
                filterArea.getUpperRight().getXAxis(),
                filterArea.getUpperRight().getYAxis()
        );

        return TreeSet.fromIterable(currentState);
    }
}
