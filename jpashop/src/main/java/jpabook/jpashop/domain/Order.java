package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    /*
        양방향 연관관계에서는 ex) Member 테이블의 데이터를 변경했는데
        Order 테이블의 데이터가 변경 될 수 있으므로 연관관계의 주인을 설정해줘야하는데
        fk가 가까운곳으로 지정해주면 됨. Member(orders) & Order(member) case
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // fk키 설정
    private Member member;

    /*
        원래라면 orderItems마다 persist 적용을 해줘야하는데
        cascade = CascadeType.ALL를 통해 orderItems 전체를 persist해줌

        ex) persist(orderItemA)
            persist(orderItemB)
            persist(orderItemC)
            presist(order)
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    //마찬가지로 order를 저장할때 cascase = CascadeType.ALL을 통해 delivery쪽도 persist해줌
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; //주문시간, Java8이상에서는 LocalDateTime 사용하면 hibernate가 처리해줌

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL]

    //==양방향 연관관계 메서드 ==// 메서드 위치는 컨트롤하는쪽에 작성하는것이 관례
    public void setMember(Member member){
        this.member = member;
        member.getOrders().add(this);
    }
    //==양방향 연관관계 메서드 ==//
    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    //==양방향 연관관계 메서드 ==//
    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성 메서드==//
    //OrderItem... ...문법을통해 여러개를 넘길수있게해줌
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems){
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        //order 상태를 처음부터 ORDER로 강제
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //==비즈니스 로직==//
    //주문취소
    public void cancel(){
        //DeliveryStatus.COMP는 배송 완료
        if(delivery.getStatus() == DeliveryStatus.COMP){
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다");
        }
        //로직을 통과하면 취소로 변환
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    //==조회 로직==//
    //전체 주문 가격 조회
    public int getTotalPrice(){
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }
}
