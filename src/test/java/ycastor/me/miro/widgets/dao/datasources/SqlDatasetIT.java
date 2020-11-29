package ycastor.me.miro.widgets.dao.datasources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import cyclops.data.TreeSet;
import ycastor.me.miro.fixtures.WidgetFixture;
import ycastor.me.miro.widgets.commands.FilterArea;
import ycastor.me.miro.widgets.dao.models.Coordinates;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("sql")
class SqlDatasetIT {

    @Autowired
    private SqlDataset sqlDataset;

    @Autowired
    private WidgetSqlRepository widgetSqlRepository;

    @BeforeEach
    void setUp() {
        widgetSqlRepository.deleteAll();
    }

    @Test
    @DisplayName("Find a widget by Id")
    void findById() {
        var widget = WidgetFixture.oneWidget();
        var saved = sqlDataset.save(widget, TreeSet.empty());

        var found = sqlDataset.findById(saved.getId());

        assertThat(found.orElse(null)).isEqualTo(saved);
    }

    @Test
    @DisplayName("Find widgets in a paged way")
    void searchPaged() {
        var widget1 = WidgetFixture.oneWidget().toBuilder().zIndex(1).build();
        var widget2 = WidgetFixture.oneWidget().toBuilder().zIndex(2).build();

        var saved1 = sqlDataset.save(widget1, TreeSet.empty());
        var saved2 = sqlDataset.save(widget2, TreeSet.empty());

        var page1 = sqlDataset.listAll(PageRequest.of(0, 1));
        var page2 = sqlDataset.listAll(PageRequest.of(1, 1));

        assertThat(page1).containsOnly(saved1);
        assertThat(page2).containsOnly(saved2);
    }

    @Test
    @DisplayName("Find largest Z-index")
    void largestZindex() {
        var widget1 = WidgetFixture.oneWidget().toBuilder().zIndex(1).build();
        var widget2 = WidgetFixture.oneWidget().toBuilder().zIndex(2).build();

        sqlDataset.save(widget1, TreeSet.empty());
        sqlDataset.save(widget2, TreeSet.empty());

        var maximumZIndex = sqlDataset.findLargestZIndex();

        assertThat(maximumZIndex).isEqualTo(2);
    }

    @Test
    @DisplayName("When there are collisions, the affected widgets should also be updated")
    void save_WithConflics() {
        var newWidget = WidgetFixture.oneWidget().toBuilder().zIndex(1).build();
        var existingWidget1 = WidgetFixture.oneWidget().toBuilder().zIndex(1).build();
        var existingWidget2 = WidgetFixture.oneWidget().toBuilder().zIndex(2).build();
        var existingWidget3 = WidgetFixture.oneWidget().toBuilder().zIndex(4).build();

        var savedWidget1 = sqlDataset.save(existingWidget1, TreeSet.empty());
        var savedWidget2 = sqlDataset.save(existingWidget2, TreeSet.empty());
        var savedWidget3 = sqlDataset.save(existingWidget3, TreeSet.empty());

        var collidedWidget1 = savedWidget1.toBuilder().zIndex(savedWidget1.getZIndex() + 1).build();
        var collidedWidget2 = savedWidget2.toBuilder().zIndex(savedWidget2.getZIndex() + 1).build();

        var savedWidget = sqlDataset.save(newWidget, TreeSet.of(collidedWidget1, collidedWidget2));
        var currentState = sqlDataset.listAll(Pageable.unpaged());

        assertThat(currentState).containsOnly(savedWidget, collidedWidget1, collidedWidget2, savedWidget3);
    }

    @Test
    @DisplayName("Searching from the Z-Index to the gap should return the expected subset of widgets")
    void findFromZIndexUntilGap_ShouldReturnCorrectly() {
        var widget1 = WidgetFixture.oneWidget();
        var widget2 = WidgetFixture.oneWidget().toBuilder().zIndex(1).build();
        var widget3 = WidgetFixture.oneWidget().toBuilder().zIndex(2).build();
        var widget4 = WidgetFixture.oneWidget().toBuilder().zIndex(4).build();

        var saved1 = sqlDataset.save(widget1, TreeSet.empty());
        var saved2 = sqlDataset.save(widget2, TreeSet.empty());
        var saved3 = sqlDataset.save(widget3, TreeSet.empty());
        sqlDataset.save(widget4, TreeSet.empty());

        var subset = sqlDataset.findFromZIndexUntilGap(0);

        assertThat(subset).containsOnly(saved1, saved2, saved3);
    }

    @Test
    @DisplayName("Fetching Widgets within an area")
    void findWidgets_OnlyWithinArea() {
        var widget1 = WidgetFixture.oneWidget().toBuilder().height(100).width(100).zIndex(1).xAxis(0).yAxis(0).build();
        var widget2 = WidgetFixture.oneWidget().toBuilder().height(100).width(100).zIndex(2).xAxis(0).yAxis(50).build();
        var widget3 = WidgetFixture.oneWidget().toBuilder().height(100).width(100).zIndex(3).xAxis(50).yAxis(50).build();
        var filterArea = FilterArea.builder().lowerLeft(new Coordinates(0, 0)).upperRight(new Coordinates(100, 150)).build();

        var saved1 = sqlDataset.save(widget1, TreeSet.empty());
        var saved2 = sqlDataset.save(widget2, TreeSet.empty());
        sqlDataset.save(widget3, TreeSet.empty());

        var inArea = sqlDataset.listAllWithinArea(filterArea);

        assertThat(inArea).containsOnly(saved1, saved2);
    }
}