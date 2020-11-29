package ycastor.me.miro.widgets.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ycastor.me.miro.fixtures.WidgetFixture;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetsCommandConverterTest {

    private WidgetsCommandConverter widgetsCommandConverter;

    @BeforeEach
    void setUp() {
        widgetsCommandConverter = new WidgetsCommandConverter();
    }

    @Test
    @DisplayName("The create command conversion is successful")
    void fromCommand_Create() {
        var createCommand = WidgetFixture.widgetCreateCommand();

        var converted = widgetsCommandConverter.fromCommand(createCommand, createCommand.maybeZIndex().orElse(0));

        assertThat(converted.getZIndex()).isEqualTo(createCommand.maybeZIndex().orElse(0));
        assertThat(converted.getHeight()).isEqualTo(createCommand.getHeight());
        assertThat(converted.getWidth()).isEqualTo(createCommand.getWidth());
        assertThat(converted.getXAxis()).isEqualTo(createCommand.getXAxis());
        assertThat(converted.getYAxis()).isEqualTo(createCommand.getYAxis());
    }

    @Test
    @DisplayName("The update command conversion is successful")
    void fromCommand_Update() {
        var updateCommand = WidgetFixture.widgetUpdateCommand();

        var converted = widgetsCommandConverter.fromCommand(updateCommand, updateCommand.maybeZIndex().orElse(0));

        assertThat(converted.getZIndex()).isEqualTo(updateCommand.maybeZIndex().orElse(0));
        assertThat(converted.getHeight()).isEqualTo(updateCommand.getHeight());
        assertThat(converted.getWidth()).isEqualTo(updateCommand.getWidth());
        assertThat(converted.getXAxis()).isEqualTo(updateCommand.getXAxis());
        assertThat(converted.getYAxis()).isEqualTo(updateCommand.getYAxis());
    }

}