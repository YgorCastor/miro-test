package ycastor.me.miro.widgets;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cyclops.control.Option;
import cyclops.data.ImmutableSortedSet;
import ycastor.me.miro.widgets.commands.Command;
import ycastor.me.miro.widgets.commands.FilterArea;
import ycastor.me.miro.widgets.commands.WidgetCreateCommand;
import ycastor.me.miro.widgets.commands.WidgetUpdateCommand;
import ycastor.me.miro.widgets.converters.WidgetsCommandConverter;
import ycastor.me.miro.widgets.dao.WidgetRepository;
import ycastor.me.miro.widgets.dao.models.Widget;
import ycastor.me.miro.widgets.exceptions.WidgetNotFoundException;

@Service
public class WidgetsService {
    private final WidgetRepository widgetRepository;
    private final WidgetsCommandConverter widgetsCommandConverter;

    @Autowired
    public WidgetsService(WidgetRepository widgetRepository, WidgetsCommandConverter widgetsCommandConverter) {
        this.widgetRepository = widgetRepository;
        this.widgetsCommandConverter = widgetsCommandConverter;
    }

    public ImmutableSortedSet<Widget> list(Pageable pageRequest) {
        return widgetRepository.listAll(pageRequest);
    }

    public ImmutableSortedSet<Widget> filterInArea(FilterArea filterArea) {
        return widgetRepository.listAllWithinArea(filterArea);
    }

    public Option<Widget> fetch(UUID widgetId) {
        return widgetRepository.findById(widgetId);
    }

    public Widget create(WidgetCreateCommand command) {
        var indexedWidget = convertWithZIndex(command, command.maybeZIndex());
        var fixedWidgets = fixCollisions(indexedWidget);
        return widgetRepository.save(indexedWidget, fixedWidgets);
    }

    public Widget update(UUID widgetId, WidgetUpdateCommand command) {
        widgetRepository.findById(widgetId)
                        .orElseGet(() -> { throw new WidgetNotFoundException(widgetId); });
        var indexedWidget = convertWithZIndex(command, command.maybeZIndex()).toBuilder().id(widgetId).build();
        var fixedWidgets = fixCollisions(indexedWidget);
        return widgetRepository.save(indexedWidget, fixedWidgets);
    }

    public Widget delete(UUID widgetId) {
        return widgetRepository.delete(widgetId);
    }

    private ImmutableSortedSet<Widget> fixCollisions(Widget indexedWidget) {
        var collision = widgetRepository.findFromZIndexUntilGap(indexedWidget.getZIndex());
        return collision.map(widget -> widget.toBuilder()
                                             .zIndex(widget.getZIndex() + 1)
                                             .build());
    }

    private <T extends Command> Widget convertWithZIndex(T command, Option<Integer> zIndex) {
        return zIndex.fold(
                z -> widgetsCommandConverter.fromCommand(command, z),
                () -> {
                    int z = widgetRepository.findLargestZIndex() + 1;
                    return widgetsCommandConverter.fromCommand(command, z);
                }
        );
    }
}
