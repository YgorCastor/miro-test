package ycastor.me.miro.widgets.commands;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cyclops.control.Option;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WidgetCreateCommand implements Command {
    private Integer zIndex;
    @Positive(message = "The width must be positive")
    private Integer width;
    @Positive(message = "The height must be positive")
    private Integer height;
    @NotNull(message = "The x-axis must be informed")
    private Integer xAxis;
    @NotNull(message = "The y-axis must be informed")
    private Integer yAxis;

    @JsonIgnore
    public Option<Integer> maybeZIndex() {
        return Option.ofNullable(zIndex);
    }
}
