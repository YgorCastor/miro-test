package ycastor.me.miro.widgets.commands;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ycastor.me.miro.widgets.dao.models.Coordinates;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FilterArea {
    @NotNull(message = "Lower Left coordinates are required")
    private Coordinates lowerLeft;
    @NotNull(message = "Upper Right coordinates are required")
    private Coordinates upperRight;
}
