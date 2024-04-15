package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    /*
        @PersistenceContext
        private EntityManager em;
         - 스프링이 EntityManager를 만들어서 주입시켜줌
         - Spring Data JPA에선 @RequiredArgsConstructor으로 EntityManager 생성자 생성
     */
    
    private final EntityManager em;

    //회원 저장
    public void save(Member member){
        em.persist(member);
    }

    //회원 조회
    public Member findOne(Long id){
        return em.find(Member.class, id);
    }

    //회원 목록 조회
    //리스트는 em.createQuery작성
    public List<Member> findAll(){

        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    //:name은 parameter값, .setParmeter("name",name)
    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
