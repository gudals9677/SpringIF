package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDTO;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

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
}
