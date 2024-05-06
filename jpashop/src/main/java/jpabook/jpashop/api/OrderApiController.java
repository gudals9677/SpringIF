package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDTO;
import jpabook.jpashop.repository.order.query.OrderQueryDTO;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /*
        V1: 엔티티 직접 노출
         - 엔티티가 변하면 API 스펙이 변한다.
         - 트랜잭션 안에서 지연 로딩 필요
         - 양방향 연관관계 문제
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
            }
        return all;
    }
    /*
        V2: 지연 로딩 문제
         - 너무 많은 SQL이 실행되므로 큰 문제가 됨
     */

    @GetMapping("/api/v2/orders")
    public List<OrderDTO> ordersV2(){
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<OrderDTO> collect = orders.stream()
                .map(o -> new OrderDTO(o))
                .collect(Collectors.toList());

        return collect;
    }
    /*
        V3: fetch join으로 SQL이 1번만 실행됨
         - distinct를 사용한 이유는 1대다 조인이 있으므로 일어나는 중복을 줄여줌
         - 1대다 fetch join을 하면 페이징처리가 불가능하다.
         - 1대다 fetch join은 1개만 사용 할 수 있다. 둘이상 사용 할 시 데이터가 부정합하게 조회 될 수 있다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDTO> ordersV3(){
        List<Order> orders = orderRepository.findAllwithItem();
        List<OrderDTO> collect = orders.stream()
                .map(o -> new OrderDTO(o))
                .collect(Collectors.toList());

        return collect;
    }
    /*
        - ToOne 관계는 fetch join해도 페이징에 영향을 주지않는다. 따라서 ToOne은 fetch join으로 쿼리 수를 줄이고, 나머지는 hibernate.defalut.batch_fetch_size로 최적화하자.
        - hibernate.defalut.batch_fetch_size의 size는 100~1000을 권장
        - @BatchSize는 다(N)인 엔티티에 직접 개별 설정
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDTO> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit)
    {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDTO> collect = orders.stream()
                .map(o -> new OrderDTO(o))
                .collect(Collectors.toList());

        return collect;
    }

    /*
        V4: Query는 루트1번, 컬렉션 N번 실행
         - ToOne 관계들을 먼저 조회하고, ToMany 관계는 각각 별도로 처리한다.
            - ToOne 관게는 조인해도 데이터 row수가 증가하지 않는다.
            - ToMany 관계는 조인하면 row 수가 증가한다.
         - row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화 하기 쉬우므로 한번에 조회하고, ToMany 관계는
            최적화 하기 어려우므로 findOrderItems()같은 별도의 메서드로 조회한다.
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDTO> ordersV4(){
        return orderQueryRepository.findOrderQueryDTOs();
    }
    /*
        V5: Query는 루트1번 ,컬렉션 1번
         - ToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId로 ToMany 관계는 OrderItem을 한꺼번에 조회
         - Map을 사용해서 매칭 성능 향상
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDTO> ordersV5(){
        return orderQueryRepository.findAllByDTO_optimiztion();
    }
    /*
        V6: Query는 1번
         - 단점
          - 쿼리는 한번이지만 조인으로 인해 DB에서 전달하는 데이터가 중복되므로 V5보다 느릴 수 있다.
          - 페이징 불가능
     */
    @GetMapping("/api/v6/orders")
    public List<OrderFlatDTO> ordersV6(){
        return orderQueryRepository.findAllByDTO_flat();
    }

    @Data
    static class OrderDTO {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDTO> orderItems;
        public OrderDTO(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDTO(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDTO {
        private String itemName;//상품 명
        private int orderPrice; //주문 가격
        private int count;
        //주문 수량
        public OrderItemDTO(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
