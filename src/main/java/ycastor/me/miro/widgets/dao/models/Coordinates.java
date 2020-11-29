package ycastor.me.miro.widgets.dao.models;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coordinates {
    @NotNull(message = "A value for the x-axis is required")
    private Integer xAxis;
    @NotNull(message = "A value for the y-axis is required")
    private Integer yAxis;
}
