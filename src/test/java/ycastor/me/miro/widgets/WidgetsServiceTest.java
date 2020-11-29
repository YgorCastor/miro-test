package ycastor.me.miro.widgets;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import cyclops.control.Option;
import cyclops.data.TreeSet;
import ycastor.me.miro.fixtures.WidgetFixture;
import ycastor.me.miro.widgets.commands.FilterArea;
import ycastor.me.miro.widgets.converters.WidgetsCommandConverter;
import ycastor.me.miro.widgets.dao.WidgetRepository;
import ycastor.me.miro.widgets.dao.models.Coordinates;
import ycastor.me.miro.widgets.exceptions.WidgetNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WidgetsServiceTest {

    @Mock
    private WidgetRepository widgetRepository;
    @Mock
    private WidgetsCommandConverter widgetsCommandConverter;
    @InjectMocks
    private WidgetsService widgetsService;

    @Test
    @DisplayName("Listing widgets")
    void listWidgets() {
        var widget = WidgetFixture.oneWidget();
        var request = PageRequest.of(1, 100);

        when(widgetRepository.listAll(request)).thenReturn(TreeSet.of(widget));

        var found = widgetsService.list(PageRequest.of(1, 100));

        assertThat(found).containsOnly(widget);

        verify(widgetRepository).listAll(request);

        verifyNoMoreInteractions(widgetRepository);
        verifyNoMoreInteractions(widgetsCommandConverter);
    }

    @Test
    @DisplayName("Listing widgets in Area")
    void listWidgetsInArea() {
        var widget = WidgetFixture.oneWidget();
        var filterArea = FilterArea.builder()
                                   .lowerLeft(new Coordinates(1, 1))
                                   .upperRight(new Coordinates(1, 1))
                                   .build();

        when(widgetRepository.listAllWithinArea(filterArea)).thenReturn(TreeSet.of(widget));

        var found = widgetsService.filterInArea(filterArea);

        assertThat(found).containsOnly(widget);

        verify(widgetRepository).listAllWithinArea(filterArea);

        verifyNoMoreInteractions(widgetRepository);
        verifyNoMoreInteractions(widgetsCommandConverter);
    }

    @Test
    @DisplayName("Fetching a widget by ID should return correctly")
    void fetch_ExistingWidgetById() {
        var uuid = UUID.randomUUID();
        var widget = WidgetFixture.oneWidget();

        when(widgetRepository.findById(uuid)).thenReturn(Option.of(widget));

        var found = widgetsService.fetch(uuid);

        assertThat(found.orElse(null)).isEqualTo(widget);

        verify(widgetRepository).findById(uuid);

        verifyNoMoreInteractions(widgetRepository);
        verifyNoMoreInteractions(widgetsCommandConverter);
    }

    @Test
    @DisplayName("Creating a Widget without collision")
    void create_WithoutCollision() {
        var newWidget = WidgetFixture.widgetCreateCommand();
        var widget = WidgetFixture.oneWidget();

        when(widgetsCommandConverter.fromCommand(newWidget, newWidget.maybeZIndex().orElse(0))).thenReturn(widget);
        when(widgetRepository.save(widget, TreeSet.empty())).thenReturn(widget);
        when(widgetRepository.findFromZIndexUntilGap(newWidget.maybeZIndex().orElse(0))).thenReturn(TreeSet.empty());

        var createdWidget = widgetsService.create(newWidget);

        assertThat(createdWidget).isEqualTo(widget);

        verify(widgetsCommandConverter).fromCommand(newWidget, newWidget.maybeZIndex().orElse(0));
        verify(widgetRepository).save(widget, TreeSet.empty());
        verify(widgetRepository).findFromZIndexUntilGap(newWidget.maybeZIndex().orElse(0));
        verify(widgetRepository, never()).findLargestZIndex();

        verifyNoMoreInteractions(widgetRepository);
        verifyNoMoreInteractions(widgetsCommandConverter);
    }

    @Test
    @DisplayName("Creating a Widget without informing a Z-Index it should go to the largest index")
    void create_NotInformingZIndex() {
        var newWidget = WidgetFixture.widgetCreateCommand().toBuilder().zIndex(null).build();
        var widget = WidgetFixture.oneWidget().toBuilder().zIndex(2).build();

        when(widgetsCommandConverter.fromCommand(newWidget, 2)).thenReturn(widget);
        when(widgetRepository.save(widget, TreeSet.empty())).thenReturn(widget);
        when(widgetRepository.findFromZIndexUntilGap(2)).thenReturn(TreeSet.empty());
        when(widgetRepository.findLargestZIndex()).thenReturn(1);

        var createdWidget = widgetsService.create(newWidget);

        assertThat(createdWidget).isEqualTo(widget);

        verify(widgetsCommandConverter).fromCommand(newWidget, 2);
        verify(widgetRepository).save(widget, TreeSet.empty());
        verify(widgetRepository).findFromZIndexUntilGap(2);
        verify(widgetRepository).findLargestZIndex();

        verifyNoMoreInteractions(widgetRepository);
        verifyNoMoreInteractions(widgetsCommandConverter);
    }

    @Test
    @DisplayName("Creating a Widget with collision")
    void create_WithCollision() {
        var newWidget = WidgetFixture.widgetCreateCommand();
        var widget = WidgetFixture.oneWidget();
        var collidedWidget = WidgetFixture.oneWidget();
        var fixedCollidedWidget = collidedWidget.toBuilder().zIndex(1).build();

        when(widgetsCommandConverter.fromCommand(newWidget, newWidget.maybeZIndex().orElse(0))).thenReturn(widget);
        when(widgetRepository.save(widget, TreeSet.of(fixedCollidedWidget))).thenReturn(widget);
        when(widgetRepository.findFromZIndexUntilGap(newWidget.maybeZIndex().orElse(0))).thenReturn(TreeSet.of(collidedWidget));

        var createdWidget = widgetsService.create(newWidget);

        assertThat(createdWidget).isEqualTo(widget);

        verify(widgetsCommandConverter).fromCommand(newWidget, newWidget.maybeZIndex().orElse(0));
        verify(widgetRepository).save(widget, TreeSet.of(fixedCollidedWidget));
        verify(widgetRepository).findFromZIndexUntilGap(newWidget.maybeZIndex().orElse(0));
        verify(widgetRepository, never()).findLargestZIndex();

        verifyNoMoreInteractions(widgetRepository);
        verifyNoMoreInteractions(widgetsCommandConverter);
    }

    @Test
    @DisplayName("Updating a Widget without collision")
    void update_WithoutCollision() {
        var uuid = UUID.randomUUID();
        var newWidget = WidgetFixture.widgetUpdateCommand();
        var widget = WidgetFixture.oneWidget().toBuilder().id(uuid).build();

        when(widgetRepository.findById(uuid)).thenReturn(Option.of(widget));
        when(widgetsCommandConverter.fromCommand(newWidget, newWidget.maybeZIndex().orElse(0))).thenReturn(widget);
        when(widgetRepository.save(widget, TreeSet.empty())).thenReturn(widget);
        when(widgetRepository.findFromZIndexUntilGap(newWidget.maybeZIndex().orElse(0))).thenReturn(TreeSet.empty());

        var createdWidget = widgetsService.update(uuid, newWidget);

        assertThat(createdWidget).isEqualTo(widget);

        verify(widgetsCommandConverter).fromCommand(newWidget, newWidget.maybeZIndex().orElse(0));
        verify(widgetRepository).findById(uuid);
        verify(widgetRepository).save(widget, TreeSet.empty());
        verify(widgetRepository).findFromZIndexUntilGap(newWidget.maybeZIndex().orElse(0));
        verify(widgetRepository, never()).findLargestZIndex();

        verifyNoMoreInteractions(widgetRepository);
        verifyNoMoreInteractions(widgetsCommandConverter);
    }

    @Test
    @DisplayName("Updating a Widget without informing a Z-Index it should go to the largest index")
    void updating_NotInformingZIndex() {
        var uuid = UUID.randomUUID();
        var newWidget = WidgetFixture.widgetUpdateCommand().toBuilder().zIndex(null).build();
        var widget = WidgetFixture.oneWidget().toBuilder().zIndex(2).id(uuid).build();

        when(widgetRepository.findById(uuid)).thenReturn(Option.of(widget));
        when(widgetsCommandConverter.fromCommand(newWidget, 2)).thenReturn(widget);
        when(widgetRepository.save(widget, TreeSet.empty())).thenReturn(widget);
        when(widgetRepository.findFromZIndexUntilGap(2)).thenReturn(TreeSet.empty());
        when(widgetRepository.findLargestZIndex()).thenReturn(1);

        var createdWidget = widgetsService.update(uuid, newWidget);

        assertThat(createdWidget).isEqualTo(widget);

        verify(widgetsCommandConverter).fromCommand(newWidget, 2);
        verify(widgetRepository).findById(uuid);
        verify(widgetRepository).save(widget, TreeSet.empty());
        verify(widgetRepository).findFromZIndexUntilGap(2);
        verify(widgetRepository).findLargestZIndex();

        verifyNoMoreInteractions(widgetRepository);
        verifyNoMoreInteractions(widgetsCommandConverter);
    }

    @Test
    @DisplayName("Updating a Widget with collision")
    void updating_WithCollision() {
        var uuid = UUID.randomUUID();
        var newWidget = WidgetFixture.widgetUpdateCommand();
        var widget = WidgetFixture.oneWidget().toBuilder().id(uuid).build();
        var collidedWidget = WidgetFixture.oneWidget();
        var fixedCollidedWidget = collidedWidget.toBuilder().zIndex(1).build();

        when(widgetRepository.findById(uuid)).thenReturn(Option.of(widget));
        when(widgetsCommandConverter.fromCommand(newWidget, newWidget.maybeZIndex().orElse(0))).thenReturn(widget);
        when(widgetRepository.save(widget, TreeSet.of(fixedCollidedWidget))).thenReturn(widget);
        when(widgetRepository.findFromZIndexUntilGap(newWidget.maybeZIndex().orElse(0))).thenReturn(TreeSet.of(collidedWidget));

        var createdWidget = widgetsService.update(uuid, newWidget);

        assertThat(createdWidget).isEqualTo(widget);

        verify(widgetRepository).findById(uuid);
        verify(widgetsCommandConverter).fromCommand(newWidget, newWidget.maybeZIndex().orElse(0));
        verify(widgetRepository).save(widget, TreeSet.of(fixedCollidedWidget));
        verify(widgetRepository).findFromZIndexUntilGap(newWidget.maybeZIndex().orElse(0));
        verify(widgetRepository, never()).findLargestZIndex();

        verifyNoMoreInteractions(widgetRepository);
        verifyNoMoreInteractions(widgetsCommandConverter);
    }

    @Test
    @DisplayName("Updating an unknown Widget should thrown an exception")
    void updating_Unknown() {
        var uuid = UUID.randomUUID();
        var newWidget = WidgetFixture.widgetUpdateCommand().toBuilder().build();

        when(widgetRepository.findById(uuid)).thenReturn(Option.none());

        assertThatThrownBy(() -> widgetsService.update(uuid, newWidget)).isInstanceOf(WidgetNotFoundException.class);

        verify(widgetRepository).findById(uuid);

        verifyNoMoreInteractions(widgetRepository);
        verifyNoMoreInteractions(widgetsCommandConverter);
    }

    @Test
    @DisplayName("Deleting a widget by ID should return correctly")
    void fetch_DeleteWidgetById() {
        var uuid = UUID.randomUUID();
        var widget = WidgetFixture.oneWidget();

        when(widgetRepository.delete(uuid)).thenReturn(widget);

        var found = widgetsService.delete(uuid);

        assertThat(found).isEqualTo(widget);

        verify(widgetRepository).delete(uuid);

        verifyNoMoreInteractions(widgetRepository);
        verifyNoMoreInteractions(widgetsCommandConverter);
    }
}