package com.wzp.cloud.graphql.query;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.support.NumberConversions;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQuery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class JpaQueryHelper {

    private final JPAQuery<?> query;

    private final Function<String, ComparableExpressionBase<?>> columnMapping;

    public JpaQueryHelper(JPAQuery<?> query, Function<String, ComparableExpressionBase<?>> columnMapping) {
        this.query = query.clone();
        this.columnMapping = columnMapping;
    }

    public <T> DataSet<T> query(QueryOption option) {

        JPAQuery<?> q = query.clone();

        if (option != null) {
            paging(q, option);
            orderBy(q, option);
        }

        Long total = null;
        List<?> list;
        if (option != null && option.isIncludeTotal()) {
            QueryResults<?> r = q.fetchResults();
            total = r.getTotal();
            list = r.getResults();
        } else {
            list = q.fetch();
        }
        list = list.stream().map(this::map).collect(Collectors.toList());
        return (DataSet<T>) DataSet.newDataSet(list, total);
    }

    private Object map(Object o) {
        if (o instanceof Tuple) {
            Tuple tuple = (Tuple) o;
            List<Expression<?>> list = getQTuple(query.getMetadata().getProjection());
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            for (Expression<?> arg : list) {
                String name = getName(arg);
                Object value = tuple.get(arg);
                map.put(name, value);
            }
            return map;
        } else {
            return o;
        }
    }

    private List<Expression<?>> getQTuple(Expression<?> projection) {
        if (projection instanceof QTuple) {
            return ((QTuple) projection).getArgs();
        } else if (projection instanceof NumberConversions) {
            return ((NumberConversions<?>) projection).getArgs();
        } else {
            throw new IllegalStateException();
        }
    }

    private String getName(Expression<?> arg) {
        String name = null;
        if (arg instanceof Path) {
            Path<?> path = (Path<?>) arg;
            name = path.getMetadata().getName();
        } else if (arg instanceof Operation) {
            Operation op = (Operation) arg;
            if (op.getOperator() == Ops.ALIAS) {
                name = op.getArg(1).toString();
            }
        } else {
            throw new IllegalStateException("找不到名称: " + arg);
        }

        return name;
    }

    /**
     * 处理分页
     */
    private void paging(JPAQuery<?> query, QueryOption option) {
        if (option != null) {
            // 处理分页
            if (option.getOffset() != null) {
                query.offset(option.getOffset());
            }
            if (option.getLimit() != null) {
                query.limit(option.getLimit());
            }
        }
    }

    /**
     * 处理排序
     */
    private void orderBy(JPAQuery<?> query, QueryOption option) {
        if (option.getSorts() != null) {
            List<OrderSpecifier<?>> o = option.getSorts().stream().map(sort -> {
                ComparableExpressionBase<?> column = columnMapping.apply(sort.getField());
                if (column == null) {
                    throw new IllegalStateException("不支持对" + sort.getField() + "进行排序");
                }
                if (sort.getDirection() == SortDirection.asc) {
                    return column.asc();
                } else if (sort.getDirection() == SortDirection.desc) {
                    return column.desc();
                } else {
                    throw new IllegalStateException("不支持的排序方式: " + sort.getDirection());
                }
            }).collect(Collectors.toList());
            query.orderBy(o.toArray(new OrderSpecifier[]{}));
        }
    }
}
