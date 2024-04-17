package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) //상속관계 전략 지정. 1. 싱글테이블 전략 2. JOINED(정규화된 스타일)
@DiscriminatorColumn(name = "dtype" ) //자식클래스 구분하기 위해 지정
@Getter @Setter
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    //==비즈니스 로직 ==//
    //재고 증가 메서드
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }
    //재고 감소 메서드, 0보다 작아지면 안됨
    //응집력이 강한쪽에 메서드들을 생성해줘야하고, setter로 바깥쪽에서 데이터를 변경하기보단 엔티티안에서 로직을 짜야함
    public void removeStock(int quantity){
        int restStock = this.stockQuantity - quantity;
        if(restStock < 0){
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}
