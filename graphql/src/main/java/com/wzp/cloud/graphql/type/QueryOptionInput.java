package com.wzp.cloud.graphql.type;

import com.lanmaoly.cloud.graphql.query.SortDirection;

import java.util.Collections;
import java.util.List;

public class QueryOptionInput {

    private Long offset;

    private Long limit;

    private List<Sort> sorts;

    public Long getOffset() {
        return offset;
    }

    public Long getLimit() {
        return limit;
    }

    public List<Sort> getSorts() {
        if (sorts == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(sorts);
    }

    public static class Sort {

        private String field;

        private SortDirection direction;

        public String getField() {
            return field;
        }

        public SortDirection getDirection() {
            return direction;
        }
    }

}
