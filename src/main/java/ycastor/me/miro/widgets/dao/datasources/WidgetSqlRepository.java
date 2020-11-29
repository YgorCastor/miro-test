package ycastor.me.miro.widgets.dao.datasources;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import ycastor.me.miro.widgets.dao.models.Widget;

@ConditionalOnProperty(value = "miro.datasource", havingValue = "SQL")
public interface WidgetSqlRepository extends PagingAndSortingRepository<Widget, UUID> {

    @Query("SELECT w.z_index FROM widget w ORDER BY w.z_index DESC LIMIT 1")
    Integer findLargestZIndex();

    Set<Widget> findAllByzIndexGreaterThanEqual(Integer zindex);

    @Query("SELECT * FROM widget" +
            " WHERE x_axis + (height / 2) > :lowerLeftX" +
            " AND x_axis + (height / 2) < :topRightX" +
            " AND y_axis + (width / 2) > :lowerLeftY" +
            " AND y_axis + (width / 2) < :topRightY")
    List<Widget> findAllWithinArea(int lowerLeftX, int lowerLeftY, int topRightX, int topRightY);

}
