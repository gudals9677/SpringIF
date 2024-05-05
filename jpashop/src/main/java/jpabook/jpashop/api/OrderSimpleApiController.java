package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDTO;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/*
    ToOne 관계의 성능 최적화
    Order
    Order -> Member (ManyToOne)
    Order -> Delivery (OneToOne)
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /*
        - 엔티티를 직접 노출하는 것은 좋지않다.
        - order → member와 order → delivery는 지연로딩이다. 따라서 실제 엔티티 대신에 프록시 존재
        - 양방형 관계 반복 로딩을 막기위해선 엔티티에 @JsonIgnore 설정을 해줘야하는데 이도 좋지않다
        - LAZY로딩을 피하기위해서 EARGR로 설정하면 안된다.
        - 지연로딩을 기본으로하고, 성능최적화가 필요한 경우엔 fetch join을 사용해야함.
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //LAZY 강제 초기화
            order.getDelivery().getAddress(); //LAZY 강제 초기화
        }
        return all;
    }
    /*
        조회 : v2
            - 엔티티를 DTO로 반환해서 조회하나 N + 1 문제가 발생한다.
            - fetch join 을 사용해 문제를 해결해야함.
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDTO> ordersV2(){

        List<Order> orders = orderRepository.findAll(new OrderSearch());

        List<SimpleOrderDTO> result = orders.stream()
                .map(o -> new SimpleOrderDTO(o))
                .collect(Collectors.toList());

        return result;
    }
    /*
        V3: 엔티티를 조회해서 DTO로 변환(fetch join)
            - fetch join으로 쿼리 1번 호출
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDTO> ordersV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDTO> result = orders.stream()
                .map(o -> new SimpleOrderDTO(o))
                .collect(Collectors.toList());

        return result;
    }
    /*
        V4: JPA에서 DTO로 바로 조회, 쿼리 1번호출,select절에서 원하는 데이터만 조회
            - new명령어를 사용해서 JPQL의 결과를 DTO로 즉시 반환
            - 리포지토리 재사용성이 떨어짐(따로 분리해주는것이 좋기때문에)

        정리: - 우선 엔티티를 DTO로 변환하는 방식을 선택한다.
             - 필요하면 fetch join으로 성능을 최적화한다.(대부분의 성능 이슈가 해결됨)
             - 그래도 안되면 DTO를 조회하는 방법을 사용(V4).
             - 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용하여 SQL을 직접 사용한다.
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDTO> ordersV4() {
        return orderSimpleQueryRepository.findOrderDTOs();
    }

    @Data
    static class SimpleOrderDTO {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDTO(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화
        }
    }
}
