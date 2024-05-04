package study.datajpa.repository;

import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDTO;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
    
   // @Query(name = "Member.findByUsername") 생략해도 nameQuery 참조
    List<Member> findByUsername(@Param("username") String username);

    //실행 시점에 문법 오류 발견 가능(큰 장점)
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username")String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    // Query값 DTO로 조회
    @Query("select new study.datajpa.dto.MemberDTO(m.id,m.username,t.name) from Member m join m.team t")
    List<MemberDTO> findMemberDTO();

    List<Member> findListByUsername(String username); //컬렉션
    Member findMemberByUsername(String username); //단건
    Optional<Member> findOptionalByUsername(String username);

    // Count 쿼리를 분리시키는게 성능향상에 도움됨
    Page<Member> findByAge(int age, Pageable pageable);
    
     // 스프링데이터 JPA로 회원 나이 한번에 수정 쿼리((clearAutomatically = true)추가해줌. em.clear()와 동일한 기능
     @Modifying(clearAutomatically = true)
     @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
     int bulkAgePlus(@Param("age") int age);

     // fetch 조인을하면 Member를 조회할때 연관됨 Team도 한번에 조회함
     @Query("select m from Member m left join fetch m.team")
     List<Member> findMemberFetchJoin();

     @Override
     @EntityGraph(attributePaths = {"team"})
     List<Member> findAll();

}
