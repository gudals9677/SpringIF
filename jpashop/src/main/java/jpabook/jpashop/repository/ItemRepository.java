package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    // 처음 생성하면 id값이 없으므로 em.persist(item);를 통해 신규 아이템을 등록. 일단 em.merge는 update라고 생각하자
    public void save(Item item){
        if(item.getId() == null){
            em.persist(item);
        } else{
            em.merge(item);
        }
    }
    // item 찾기
    public Item findOne(Long id){
        return em.find(Item.class, id);
    }

    // item 리스트 찾기
    public List<Item> findAll(){
        return em.createQuery("select i from Item i ", Item.class)
                .getResultList();
    }
}
