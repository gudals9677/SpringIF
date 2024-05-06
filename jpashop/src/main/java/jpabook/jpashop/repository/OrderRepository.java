package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    //주문 저장
    public void save(Order order){
        em.persist(order);
    }
    //특정 주문 조회
    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    // 검색 기능. 일단 보류
    public List<Order> findAll(OrderSearch orderSearch){

        return em.createQuery("select o from Order o join o.member m" +
                " where o.status = :status" +
                " and m.name like :name", Order.class)
                .setParameter("status",orderSearch.getOrderStatus())
                .setParameter("name",orderSearch.getMemberName())
                //.setFirstResult(100) 페이징처리/ 100부터 1000까지 가져온다는 뜻
                .setMaxResults(1000) // 최대 1000개
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    public List<Order> findAllwithItem() {

        return em.createQuery("select distinct o from Order o" +
                                        " join fetch o.member m" +
                                        " join fetch o.delivery d" +
                                        " join fetch o.orderItems oi" +
                                        " join fetch oi.item", Order.class)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {

        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        )
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}