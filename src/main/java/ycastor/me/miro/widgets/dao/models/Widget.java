package ycastor.me.miro.widgets.dao.models;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;


import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Widget implements Comparable<Widget> {
    @Id
    private UUID id;
    @NotNull
    private Integer zIndex;
    @Positive
    private Integer width;
    @Positive
    private Integer height;
    @NotNull
    private Integer xAxis;
    @NotNull
    private Integer yAxis;

    @Override
    public int compareTo(Widget o) {
        return this.zIndex.compareTo(o.zIndex);
    }

    public Integer xAxisMiddlePoint() {
        return xAxis + (height / 2);
    }

    public Integer yAxisMiddlePoint() {
        return yAxis + (width / 2);
    }
}
