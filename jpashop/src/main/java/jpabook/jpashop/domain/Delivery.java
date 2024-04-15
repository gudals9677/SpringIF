package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Delivery {

    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    private Address address;

    @Enumerated(EnumType.STRING) //String 넣어줘야 장애가 안남
    private DeliveryStatus status; //READY(배송준비), COMP(배송중)

}
