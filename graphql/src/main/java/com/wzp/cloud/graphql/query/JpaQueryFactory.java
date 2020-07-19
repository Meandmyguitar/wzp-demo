package com.wzp.cloud.graphql.query;

import com.lanmaoly.util.lang.StreamUtils;
import com.querydsl.jpa.impl.JPAQuery;
import org.hibernate.SessionFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public class JpaQueryFactory {

    private final SessionFactory sessionFactory;

    private final PlatformTransactionManager transactionManager;

    public JpaQueryFactory(SessionFactory sessionFactory, PlatformTransactionManager transactionManager) {
        this.sessionFactory = sessionFactory;
        this.transactionManager = transactionManager;
    }

    public <T, F> Query<F> create(JpaQuery<F> query) {
        return new InternalQuery<F>(query);
    }

    class InternalQuery<F> implements Query<F> {

        private final JpaQuery<F> query;

        public InternalQuery(JpaQuery<F> query) {
            this.query = query;
        }

        @Override
        public <T> DataSet<T> execute(QueryContext<F> context) {
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            return template.execute(s -> {
                JPAQuery<?> query = new JPAQuery<>(sessionFactory.getCurrentSession());
                this.query.select(query, context.getFilters());
                JpaQueryHelper helper = new JpaQueryHelper(query, this.query::columnMapping);
                DataSet<T> dataSet = helper.query(context.getOption());

                if (this.query instanceof JpaQueryTransform) {
                    JpaQueryTransform<T> transform = (JpaQueryTransform<T>)this.query;
                    dataSet = DataSet.newDataSet(StreamUtils.map(dataSet.getData(), transform::transform), dataSet.getTotal());
                }
                return dataSet;
            });
        }
    }
}
