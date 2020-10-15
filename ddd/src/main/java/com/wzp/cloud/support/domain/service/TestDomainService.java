package com.wzp.cloud.support.domain.service;


import java.math.BigDecimal;

public class TestDomainService {

    /**
     * 1。贫血模型（贫血的领域对象：Order）
     * 这种方式依然时一种面向过程的编程范式，不是OO原则。
     * <p>
     * 导致时指责划分不清晰，本应该内聚在Order中的业务，都放在service中整合，导致Order只是充当了一个数据容器的贫血模型。
     * <p>
     * 可能会出现新的问题：在项目演化的过程中，业务都在domainService和appService中，对之后的代码扩展能力会减弱。
     */
    public void changeProductCount1(Long id, ChangeProductCountCommand command) {
        Order order = DAO.findById(id);
        if (order.getStatus() == OrderStatus.PAID) {
            throw new OrderCannotBeModifiedException(id);
        }
        //面向过程：根据商品id查询商品详情，setcount，settotalprice，调用Dao保存
        Order orderItem = order.getOrderItem(command.getProductId());
        orderItem.setCount(command.getCount());
        order.setTotalPrice(calculateTotalPrice(order));
        DAO.saveOrUpdate(order);
    }

    /**
     * 2。事务脚本
     * 领域对象Order存在的意义就是为了包装mapper，不使用orm的情况下毫无意义。
     * <p>
     * 注：在系统业务逻辑极度简单的情况下，确实是个非常好的实践。但是度不是很好把握。。。
     * <p>
     * 业务逻辑分散：DAO封装出了多个能力，摒弃了面向领域对象处理业务的思想，用一行行脚本的形式去实现业务。
     * <p>
     * 可能会出现新的问题：业务不停发展，这种模式对日后的影响比贫血模型可怕的多。
     */
    public void changeProductCount2(Long id, ChangeProductCountCommand command) {
        OrderStatus orderStatus = DAO.getOrderStatus(id);
        if (orderStatus == OrderStatus.PAID) {
            throw new OrderCannotBeModifiedException(id);
        }
        DAO.updateProductCount(id, command.getProductId(), command.getCount());
        DAO.updateTotalPrice(id);
    }

    //领域对象
    public class Order {
        Long id;
        Long count;
        BigDecimal totalPrice;
        OrderStatus status;

        /**
         * 3。基于领域对象
         */
        public void changeProductCount(Long productId, int count) {
            if (this.status == OrderStatus.PAID) {
                throw new OrderCannotBeModifiedException(productId);
            }
            Order order = retrieveItem(productId);
            order.updateCount(count);
        }

        public Order getOrderItem(Long productId) {
            // todo 获取Order对象item
            return null;
        }

        /**
         * 更新product-count
         */
        private void updateCount(int count) {
            //todo
        }

        /**
         * 检索order-item
         */
        private Order retrieveItem(Long productId) {
            //todo
            return null;
        }


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }

        public OrderStatus getStatus() {
            return status;
        }

        public void setStatus(OrderStatus status) {
            this.status = status;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }


    }

    public class ChangeProductCountCommand {
        private Long productId;
        private Long count;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    public enum OrderStatus {
        PAID
    }


    public class OrderCannotBeModifiedException extends RuntimeException {
        public OrderCannotBeModifiedException(Long id) {

        }
    }

    /**
     * 计算价格
     */
    private BigDecimal calculateTotalPrice(Order order) {
        return null;
    }

    /**
     * dao接口（方便模拟）
     */
    public static class DAO{
        public static Order findById(Long id) {
            return null;
        }

        public static void saveOrUpdate(Order order) {

        }

        public static OrderStatus getOrderStatus(Long id) {
            return null;
        }

        public static void updateProductCount(Long id, Long productId, Long count) {

        }

        public static void updateTotalPrice(Long id) {

        }
    }

}
