package ycastor.me.miro.api;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cyclops.data.ImmutableSortedSet;
import ycastor.me.miro.widgets.WidgetsService;
import ycastor.me.miro.widgets.commands.FilterArea;
import ycastor.me.miro.widgets.commands.WidgetCreateCommand;
import ycastor.me.miro.widgets.commands.WidgetUpdateCommand;
import ycastor.me.miro.widgets.dao.models.Widget;

@RestController
@RequestMapping("/widget")
@Validated
public class WidgetsApi {
    private final WidgetsService widgetsService;

    @Autowired
    public WidgetsApi(WidgetsService widgetsService) {
        this.widgetsService = widgetsService;
    }

    @PostMapping
    public ResponseEntity<Widget> create(@Valid @RequestBody WidgetCreateCommand createCommand) {
        var widget = widgetsService.create(createCommand);
        return ResponseEntity.ok(widget);
    }

    @PostMapping("/{widgetId}")
    public ResponseEntity<Widget> update(@NotNull @PathVariable UUID widgetId, @Valid @RequestBody WidgetUpdateCommand widgetUpdateCommand) {
        var widget = widgetsService.update(widgetId, widgetUpdateCommand);
        return ResponseEntity.ok(widget);
    }

    @GetMapping("/{widgetId}")
    public ResponseEntity<Widget> getById(@NotNull @PathVariable UUID widgetId) {
        return widgetsService.fetch(widgetId).fold(ResponseEntity::ok, () -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{widgetId}")
    public ResponseEntity<Widget> deleteById(@NotNull @PathVariable UUID widgetId) {
        var widget = widgetsService.delete(widgetId);
        return ResponseEntity.ok(widget);
    }

    @GetMapping
    public ResponseEntity<ImmutableSortedSet<Widget>> getById(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        var widgets = widgetsService.list(PageRequest.of(page, pageSize));
        return ResponseEntity.ok(widgets);
    }

    @PostMapping("/in-area")
    public ResponseEntity<ImmutableSortedSet<Widget>> inArea(@Valid @RequestBody FilterArea filterArea) {
        var widgets = widgetsService.filterInArea(filterArea);
        return ResponseEntity.ok(widgets);
    }
}
