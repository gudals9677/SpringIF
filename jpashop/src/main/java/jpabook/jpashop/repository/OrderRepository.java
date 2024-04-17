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
    //public List<Order> findAll(OrderSearch orderSearch){}
}
