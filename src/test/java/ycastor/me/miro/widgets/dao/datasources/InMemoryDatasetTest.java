package ycastor.me.miro.widgets.dao.datasources;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import cyclops.data.TreeSet;
import ycastor.me.miro.fixtures.WidgetFixture;
import ycastor.me.miro.widgets.commands.FilterArea;
import ycastor.me.miro.widgets.dao.models.Coordinates;
import ycastor.me.miro.widgets.exceptions.WidgetNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryDatasetTest {

    private InMemoryDataset inMemoryDataset;

    @BeforeEach
    void setUp() {
        inMemoryDataset = new InMemoryDataset();
    }

    @Test
    @DisplayName("Saving and fetching an existing Widget by id should return it successfully")
    void fetchExistingWidget_ShouldReturnSuccessfully() {
        var widget = WidgetFixture.oneWidget();

        var saved = inMemoryDataset.save(widget, TreeSet.empty());
        var found = inMemoryDataset.findById(saved.getId());

        assertThat(found.orElse(null)).isEqualTo(saved);
    }

    @Test
    @DisplayName("When there are collisions, the affected widgets should also be updated")
    void saveWithCollision_ShouldUpdateAffected() {
        var newWidget = WidgetFixture.oneWidget().toBuilder().zIndex(1).build();
        var existingWidget1 = WidgetFixture.oneWidget().toBuilder().zIndex(1).build();
        var existingWidget2 = WidgetFixture.oneWidget().toBuilder().zIndex(2).build();
        var existingWidget3 = WidgetFixture.oneWidget().toBuilder().zIndex(4).build();

        var saved1 = inMemoryDataset.save(existingWidget1, TreeSet.empty());
        var saved2 = inMemoryDataset.save(existingWidget2, TreeSet.empty());
        var saved3 = inMemoryDataset.save(existingWidget3, TreeSet.empty());

        var collidedWidget1 = saved1.toBuilder().zIndex(saved1.getZIndex() + 1).build();
        var collidedWidget2 = saved2.toBuilder().zIndex(saved2.getZIndex() + 1).build();

        var savedWidget = inMemoryDataset.save(newWidget, TreeSet.of(collidedWidget1, collidedWidget2));
        var currentState = inMemoryDataset.listAll(PageRequest.of(0, 100));

        assertThat(currentState).containsOnly(savedWidget, collidedWidget1, collidedWidget2, saved3);
    }

    @Test
    @DisplayName("Updating a widget should update the state correctly")
    void updateWidget_ShouldUpdateStateCorrectly() {
        var existingWidget = WidgetFixture.oneWidget();
        var saved = inMemoryDataset.save(existingWidget, TreeSet.empty());

        var update = WidgetFixture.oneWidget().toBuilder().id(saved.getId()).zIndex(2).build();
        var updatedWidget = inMemoryDataset.save(update, TreeSet.empty());

        assertThat(updatedWidget).isEqualTo(update);
    }

    @Test
    @DisplayName("Deleting an existing Widget should update the state correctly")
    void deleteExistingWidget_ShouldUpdateStateCorrectly() {
        var widget = WidgetFixture.oneWidget();
        var saved = inMemoryDataset.save(widget, TreeSet.empty());

        var deleted = inMemoryDataset.delete(saved.getId());
        var found = inMemoryDataset.findById(saved.getId());

        assertThat(deleted).isEqualTo(saved);
        assertThat(found.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Trying to delete a non-existing widget should thrown an exception")
    void deleteNonExisting_ShouldThrowException() {
        assertThatThrownBy(() -> inMemoryDataset.delete(UUID.randomUUID())).isInstanceOf(WidgetNotFoundException.class);
    }

    @Test
    @DisplayName("Searching for the maximum ZIndex should return the largest one")
    void findLargestZIndex_ShouldReturnCorrectly() {
        var widget1 = WidgetFixture.oneWidget();
        var widget2 = WidgetFixture.oneWidget().toBuilder().zIndex(100).build();
        var widget3 = WidgetFixture.oneWidget().toBuilder().zIndex(10000).build();

        inMemoryDataset.save(widget1, TreeSet.empty());
        inMemoryDataset.save(widget2, TreeSet.empty());
        inMemoryDataset.save(widget3, TreeSet.empty());

        int largestZindex = inMemoryDataset.findLargestZIndex();

        assertThat(largestZindex).isEqualTo(10000);
    }

    @Test
    @DisplayName("When finding the largest Z-Index and there are no widgets, the Z-Index should be 0")
    void findLargestZIndex_NoWidgets() {
        int largestZindex = inMemoryDataset.findLargestZIndex();
        assertThat(largestZindex).isEqualTo(0);
    }

    @Test
    @DisplayName("Searching from the Z-Index to the gap should return the expected subset of widgets")
    void findFromZIndexUntilGap_ShouldReturnCorrectly() {
        var widget1 = WidgetFixture.oneWidget();
        var widget2 = WidgetFixture.oneWidget().toBuilder().zIndex(1).build();
        var widget3 = WidgetFixture.oneWidget().toBuilder().zIndex(2).build();
        var widget4 = WidgetFixture.oneWidget().toBuilder().zIndex(4).build();

        var saved1 = inMemoryDataset.save(widget1, TreeSet.empty());
        var saved2 = inMemoryDataset.save(widget2, TreeSet.empty());
        var saved3 = inMemoryDataset.save(widget3, TreeSet.empty());
        inMemoryDataset.save(widget4, TreeSet.empty());

        var subset = inMemoryDataset.findFromZIndexUntilGap(0);

        assertThat(subset).containsOnly(saved1, saved2, saved3);
    }

    @Test
    @DisplayName("Fetching Widgets within an area")
    void findWidgets_OnlyWithinArea() {
        var widget1 = WidgetFixture.oneWidget().toBuilder().height(100).width(100).zIndex(1).xAxis(0).yAxis(0).build();
        var widget2 = WidgetFixture.oneWidget().toBuilder().height(100).width(100).zIndex(2).xAxis(0).yAxis(50).build();
        var widget3 = WidgetFixture.oneWidget().toBuilder().height(100).width(100).zIndex(3).xAxis(50).yAxis(50).build();
        var filterArea = FilterArea.builder().lowerLeft(new Coordinates(0, 0)).upperRight(new Coordinates(100, 150)).build();

        var saved1 = inMemoryDataset.save(widget1, TreeSet.empty());
        var saved2 = inMemoryDataset.save(widget2, TreeSet.empty());
        inMemoryDataset.save(widget3, TreeSet.empty());

        var inArea = inMemoryDataset.listAllWithinArea(filterArea);

        assertThat(inArea).containsOnly(saved1, saved2);
    }
}