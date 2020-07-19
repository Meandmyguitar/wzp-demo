package com.wzp.cloud.graphql.query;

import java.util.*;

public final class QueryOption {

    private Long offset;

    private Long limit;

    private List<Sort> sorts;

    private boolean includeTotal;

    public QueryOption(Long offset, Long limit, List<Sort> sorts, boolean includeTotal) {
        this.offset = offset;
        this.limit = limit;
        this.sorts = Collections.unmodifiableList(sorts == null ? Collections.emptyList() : sorts);
        this.includeTotal = includeTotal;
    }

    // 默认构造函数，供反序列化使用
    @SuppressWarnings("unused")
    private QueryOption() {

    }

    public Long getOffset() {
        return offset;
    }

    public Long getLimit() {
        return limit;
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public boolean isIncludeTotal() {
        return includeTotal;
    }

    public static class Sort {

        private String field;

        private SortDirection direction;

        // 默认构造函数，供反序列化使用
        private Sort() {

        }

        public Sort(String field, SortDirection direction) {
            this.field = field;
            this.direction = direction;
        }

        public String getField() {
            return field;
        }

        public SortDirection getDirection() {
            return direction;
        }
    }


    public static class Builder {

        public static Builder create() {
            return new Builder();
        }

        private Long offset;

        private Long limit;

        private Map<String, Object> filters = new LinkedHashMap<>();

        private List<Sort> sorts = new ArrayList<>();

        private boolean includeTotal;

        public Builder offset(Long offset) {
            this.offset = offset;
            return this;
        }

        public Builder limit(Long limit) {
            this.limit = limit;
            return this;
        }

        public Builder sort(String field, SortDirection direction) {
            this.sorts.add(new Sort(field, direction));
            return this;
        }

        public Builder includeTotal(boolean value) {
            this.includeTotal = value;
            return this;
        }

        public QueryOption build() {
            return new QueryOption(offset, limit, sorts, includeTotal);
        }
    }
}
