package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    /*
        @Transactional(readOnly = true)는 읽기전용으로 관리하게 하여 성능향상을 해줌. 데이터변경이 필요없는곳에 유용
        데이터변경이 있을곳은 따로 @Transactional처리를 해줘 쓰기전용으로 변경해줘야함
     */

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    /*
        변경기능 감지 영속성 컨텍스트에서 엔티티를 다시 조회한 후에 데이터를 수정하는 방법
        트랜잭션 안에서 엔티티를 조회, 변경할 값을 선택 -> 트랜잭션 커밋 시점에 변경감지가 동작
        해서 db에 update sql 실행
     */
    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity){
        Item findItem = itemRepository.findOne(itemId);
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
    }

    public List<Item> findItems(){
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId){
        return itemRepository.findOne(itemId);
    }
}
