package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional // transactional덕분에 db autoincrement쪽은 쿼리문에 insert가 안됨 데이터가 저장되지않게 롤백해버림
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
   // @Rollback(false) // rollback을해서 transactional 무시하고 insert되게 해줌
    public void 회원가입() throws Exception{
        //given
        Member member = new Member();
        member.setName("kim");
        //when
        Long savedId = memberService.join(member);

        //then
        em.flush(); //flush는 데이터 변경을 쿼리에 적용시켜줌
        Assertions.assertThat(member.getId()).isEqualTo(savedId);
    }

    @Test
    public void 중복_회원_예외() throws Exception{
        //given
        Member member1 = new Member();
        member1.setName("kim1");

        Member member2 = new Member();
        member2.setName("kim1");

        //when
        memberService.join(member1);
        try {
            memberService.join(member2);
        }catch(IllegalStateException e){
            return;
        }
        //then
        fail("예외가 발생해야 한다.");
    }

}