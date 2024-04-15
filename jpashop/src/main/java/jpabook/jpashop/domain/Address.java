package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable // JPA 내장타입이므로 달아줌
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    /*
        엔티티 값 타입은 변경 불가능하게 설계해야 함.
        @Setter를 제거하고, 생성자에서 값을 모두 초기화해서 변경 불가능하게 만들어야 함.
        + protected로 기본생성자 설정
     */
    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
