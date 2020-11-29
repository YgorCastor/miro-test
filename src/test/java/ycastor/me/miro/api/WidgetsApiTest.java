package ycastor.me.miro.api;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import cyclops.control.Option;
import cyclops.data.TreeSet;
import ycastor.me.miro.fixtures.WidgetFixture;
import ycastor.me.miro.shared.Problem;
import ycastor.me.miro.widgets.WidgetsService;
import ycastor.me.miro.widgets.commands.FilterArea;
import ycastor.me.miro.widgets.dao.models.Coordinates;
import ycastor.me.miro.widgets.exceptions.WidgetNotFoundException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class WidgetsApiTest {

    @MockBean
    private WidgetsService widgetsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Fetching a widget by Id")
    void fetchWidgetById() throws Exception {
        var uuid = UUID.randomUUID();
        var widget = WidgetFixture.oneWidget().toBuilder().id(uuid).build();

        var expectedWidget = objectMapper.writeValueAsString(widget);

        when(widgetsService.fetch(uuid)).thenReturn(Option.of(widget));

        mockMvc.perform(get("/widget/" + uuid.toString()))
               .andExpect(status().isOk())
               .andExpect(content().json(expectedWidget));

        verify(widgetsService).fetch(uuid);
    }

    @Test
    @DisplayName("Fetching a list of objects")
    void fetchListOfObjects() throws Exception {
        var uuid = UUID.randomUUID();
        var widget = WidgetFixture.oneWidget().toBuilder().id(uuid).build();
        var listOf = TreeSet.of(widget);

        var expectedWidget = objectMapper.writeValueAsString(listOf);

        when(widgetsService.list(any())).thenReturn(TreeSet.of(widget));

        mockMvc.perform(get("/widget"))
               .andExpect(status().isOk())
               .andExpect(content().json(expectedWidget));

        verify(widgetsService).list(any());
    }

    @Test
    @DisplayName("Fetching objects within area")
    void fetchListOfObjectsWithinArea() throws Exception {
        var uuid = UUID.randomUUID();
        var widget = WidgetFixture.oneWidget().toBuilder().id(uuid).build();
        var filterArea = FilterArea.builder()
                                   .upperRight(new Coordinates(1, 1))
                                   .lowerLeft(new Coordinates(1, 1))
                                   .build();
        var listOf = TreeSet.of(widget);

        var expectedWidget = objectMapper.writeValueAsString(listOf);
        var filter = objectMapper.writeValueAsString(filterArea);

        when(widgetsService.filterInArea(any())).thenReturn(TreeSet.of(widget));

        mockMvc.perform(post("/widget/in-area").content(filter).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().json(expectedWidget));

        verify(widgetsService).filterInArea(any());
    }

    @Test
    @DisplayName("Deleting a widget by Id")
    void deleteWidgetById() throws Exception {
        var uuid = UUID.randomUUID();
        var widget = WidgetFixture.oneWidget().toBuilder().id(uuid).build();

        var expectedWidget = objectMapper.writeValueAsString(widget);

        when(widgetsService.delete(uuid)).thenReturn(widget);

        mockMvc.perform(delete("/widget/" + uuid.toString()))
               .andExpect(status().isOk())
               .andExpect(content().json(expectedWidget));

        verify(widgetsService).delete(uuid);
    }

    @Test
    @DisplayName("Creating a new Widget")
    void createWidget() throws Exception {
        var widget = WidgetFixture.oneWidget().toBuilder().build();
        var widgetCommand = WidgetFixture.widgetCreateCommand();

        var widgetCommandJson = objectMapper.writeValueAsString(widgetCommand);
        var expectedWidget = objectMapper.writeValueAsString(widget);

        when(widgetsService.create(any())).thenReturn(widget);

        mockMvc.perform(post("/widget").content(widgetCommandJson).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().json(expectedWidget));

        verify(widgetsService).create(any());
    }

    @Test
    @DisplayName("Updating a Widget")
    void updateWidget() throws Exception {
        var uuid = UUID.randomUUID();
        var widget = WidgetFixture.oneWidget().toBuilder().id(uuid).build();
        var widgetCommand = WidgetFixture.widgetUpdateCommand();

        var widgetCommandJson = objectMapper.writeValueAsString(widgetCommand);
        var expectedWidget = objectMapper.writeValueAsString(widget);

        when(widgetsService.update(uuid, widgetCommand)).thenReturn(widget);

        mockMvc.perform(post("/widget/" + uuid.toString()).content(widgetCommandJson).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().string(expectedWidget));

        verify(widgetsService).update(uuid, widgetCommand);
    }

    @Test
    @DisplayName("Updating a non-existing widget should fail")
    void updateNonExistingWidget() throws Exception {
        var uuid = UUID.randomUUID();
        var widgetCommand = WidgetFixture.widgetUpdateCommand();
        var expectedError = new Problem("Widget not found", String.format("Widget with id <%s> was not found!", uuid));

        var widgetCommandJson = objectMapper.writeValueAsString(widgetCommand);
        var expectedErrorJson = objectMapper.writeValueAsString(expectedError);

        when(widgetsService.update(uuid, widgetCommand)).thenThrow(new WidgetNotFoundException(uuid));

        mockMvc.perform(post("/widget/" + uuid.toString()).content(widgetCommandJson).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound())
               .andExpect(content().json(expectedErrorJson));

        verify(widgetsService).update(uuid, widgetCommand);
    }

    @Test
    @DisplayName("Updating a with invalid command should fail")
    void updateInvalidCommand() throws Exception {
        var uuid = UUID.randomUUID();
        var widgetCommand = WidgetFixture.widgetUpdateCommand().toBuilder().height(-1000).build();
        var expectedError = new Problem("Invalid Request", "The widget height must be positive");

        var widgetCommandJson = objectMapper.writeValueAsString(widgetCommand);
        var expectedErrorJson = objectMapper.writeValueAsString(expectedError);

        mockMvc.perform(post("/widget/" + uuid.toString()).content(widgetCommandJson).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(content().json(expectedErrorJson));
    }
}