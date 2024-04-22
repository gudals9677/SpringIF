package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDTO;
import study.querydsl.dto.QMemberDTO;
import study.querydsl.dto.UserDTO;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    /*
        member.username.eq("member1") // username = 'member1'
        member.username.ne("member1") //username != 'member1'
        member.username.eq("member1").not() // username != 'member1'
        member.username.isNotNull() //이름이 is not null
        member.age.in(10, 20) // age in (10,20)
        member.age.notIn(10, 20) // age not in (10, 20)
        member.age.between(10,30) //between 10, 30
        member.age.goe(30) // age >= 30
        member.age.gt(30) // age > 30
        member.age.loe(30) // age <= 30
        member.age.lt(30) // age < 30
        member.username.like("member%") //like 검색
        member.username.contains("member") // like ‘%member%’ 검색
        member.username.startsWith("member") //like ‘member%’ 검색
     */
    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;



    @BeforeEach
    public void before(){
        jpaQueryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamA);

        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",40,teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL(){
        //member1을 찾아라.
        Member findByJPQL = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findByJPQL.getUsername()).isEqualTo("member1");
    }

    /*
        JPAQueryFactory를 만들때 em를 생성자에 넘겨줘야함
        QMember m = Qmember.member;
        Qmember.member를 사용하고 static import로 줄일수있음
     */
    @Test
    public void startQuerydsl(){

        Member findMember = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) //파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search(){
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam(){
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch(){
        /*
            .fetch(); 은 List 처리
            .fetchOne(); 은 단일처리
            .fetchFirst(); 은 Limit(). + fetchOne();
         */
//        List<Member> fetch = jpaQueryFactory
//                .selectFrom(member)
//                .fetch();
//
//        Member fetchOne = jpaQueryFactory
//                .selectFrom(member)
//                .fetchOne();
//
//        Member fetchFirst = jpaQueryFactory
//                .selectFrom(member)
//                //.limit(1).fetchOne();
//                .fetchFirst();
//
        QueryResults<Member> results = jpaQueryFactory
                .selectFrom(member)
                .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();

        long total = jpaQueryFactory
                .selectFrom(member)
                .fetchCount();
    }

    /*
        -회원 정렬 순서-
            1. 회원 나이 내림차순(desc)
            2. 회원 이름 올림차순(asc)
            단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort(){
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1(){
        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2(){
        QueryResults<Member> queryResults = jpaQueryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation(){
        // Tuple은 여러개 타입이 있을때 꺼내오는것
        List<Tuple> result = jpaQueryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }
    /*
        팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception{
        List<Tuple> result = jpaQueryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /*
        팀 A에 소속된 모든 회원
     */
    @Test
    public void join(){
        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1","member2");
    }

    /*
        세타 조인
        회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = jpaQueryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /*
        예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
        JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering(){

        List<Tuple> result = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /*
       연관관계 없는 엔티티 외부 조인
       회원의 이름이 팀 이름과 같은 대상 외부 조인
    */
    @Test
    public void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    // Member에 team부분이 fetch = FetchType.LAZY이므로 데이터를 불러오지않음
    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo(){
        em.flush();
        em.clear();

        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    public void fetchJoinUse(){
        em.flush();
        em.clear();

        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isTrue();
    }

    /*
        나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery(){

        //member랑 충돌나니까 따로 생성해줌
        QMember memberSub = new QMember("memberSub");

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /*
    나이가 평균 이상인 회원 조회
    */
    @Test
    public void subQueryGoe(){

        //member랑 충돌나니까 따로 생성해줌
        QMember memberSub = new QMember("memberSub");

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30,40);
    }

    @Test
    public void subQueryIn(){

        //member랑 충돌나니까 따로 생성해줌
        QMember memberSub = new QMember("memberSub");

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20,30,40);
    }

    @Test
    public void selectSubQuery(){

        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = jpaQueryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void basicCase(){
        List<String> result = jpaQueryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void complexCase(){
        List<String> result = jpaQueryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void constant(){
        List<Tuple> result = jpaQueryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void concat(){

        //{username}_{age}이 나오게 하기위해서는 age가 숫자기때문에 stringValue() 문자열로 변경 해줘야함
        List<String> result = jpaQueryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void simpleProjection(){
        List<String> result = jpaQueryFactory
                .select(member.username)
                .from(member)
                .fetch();
    }
    // Tuple 조회, 반환 객체가 여러개일때 Tuple로 반환, 가급적 repository에서만 사용
    @Test
    public void tupleProjection(){
        List<Tuple> result = jpaQueryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }
    // DTO 조회 JPQL new operation 문법(생성자가 꼭 있어야함)
    @Test
    public void findDtoByJPQL(){
        List<MemberDTO> result = em.createQuery("select new study.querydsl.dto.MemberDTO(m.username, m.age) from Member m", MemberDTO.class)
                .getResultList();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }
    // DTO 조회 , query dsl Setter 이용한 프로퍼티 접근 방법
    @Test
    public void findDtoBySetter(){
        List<MemberDTO> result = jpaQueryFactory
                .select(Projections.bean(MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }
    // DTO 조회 , query dsl Filed 이용한 프로퍼티 접근 방법(getter,setter 필요없음), 필드명이 일치해야함
    @Test
    public void findDtoByFiled(){
        List<MemberDTO> result = jpaQueryFactory
                .select(Projections.fields(MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }
    /*
        필드로 조회할때 필드명이 일치하지않으면 null로 값이 들어옴. .as("필드명") 넣어주면 값이 들어옴

     */
    @Test
    public void findUserDto(){
        QMember memberSub = new QMember("memberSub");
        List<UserDTO> result = jpaQueryFactory
                .select(Projections.fields(UserDTO.class,
                        member.username.as("name"),

                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                    .from(memberSub), "age")
                ))
                .from(member)
                .fetch();
        for (UserDTO userDTO : result) {
            System.out.println("userDTO = " + userDTO);
        }
    }
    // DTO 조회 , query dsl 생성자 이용한 프로퍼티 접근 방법(getter,setter 필요없음)
    @Test
    public void findDtoByConstructor(){
        List<MemberDTO> result = jpaQueryFactory
                .select(Projections.constructor(MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }
    /*
        위의 프로퍼티 방법은 컴파일 오류를 잡지못하고 런타임으로 오류가 떠서 찾기가 힘듦에 비해 Projection은 컴파일 오류로 띄워줌
        DTO 생성자 위에 @QueryProjection 달아주고 CompileJava를 하면 QDTO가 생성됨
        그러나 dto가 quesy dsl을 의존하게 되므로 지양해서 사용
     */
    @Test
    public void findDtoByQueryProjection(){
        List<MemberDTO> result = jpaQueryFactory
                .select(new QMemberDTO(member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }
    /*
        BooleanBuilder 동적 쿼리 해결
     */
    @Test
    public void dynamicQuery_BooleanBuilder(){
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameParam, Integer ageParam) {

        BooleanBuilder builder = new BooleanBuilder();
        if(usernameParam != null){
            builder.and(member.username.eq(usernameParam));
        }
        if(ageParam != null){
            builder.and(member.age.eq(ageParam));
        }

        return jpaQueryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }
    /*
       Where 다중 파라미터 사용해서 동적 쿼리 해결
     */
    @Test
    public void dynamicQuery_WhereParam(){
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(usernameEq(usernameParam), ageEq(ageParam))
                .fetch();
    }

    private Predicate usernameEq(String usernameParam) {
        // usernameParam이 null이 아니면 member.username.eq(usernameParam)반환, null이면 null 반환
        return usernameParam != null ? member.username.eq(usernameParam) : null;
    }

    private Predicate ageEq(Integer ageParam) {
        return ageParam != null ? member.age.eq(ageParam) : null;
    }
    /*
        -수정,삭제 벌크연산-
            벌크 연산은 엔티티 영속성을 무시하고 DB 데이터를 바꿔버림
            영속성 컨테스트에는 데이터가 그대로 유지되므로 데이터가 서로 맞지않게 되는데 영속성 컨테스트 데이터가 우선권을 가짐
            해결하기위해 em.flush(); em.clear(); (초기화 작업) 추가
     */
    @Test
    public void bulkUpdate(){
        //member1 = 10 -> 비회원
        //member2 = 20 -> 비회원
        //member3 = 30 -> 유지
        //member4 = 40 -> 유지

        long count = jpaQueryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .fetch();
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }
    // 벌크연산 더하기, 곱하기는 add대신 multiply ex)member.age.multiply(3)
    @Test
    public void bulkAdd(){
        long count = jpaQueryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();
    }
    @Test
    public void bulkDelete(){
        jpaQueryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
    }
    @Test
    public void sqlFunction(){
        List<String> result = jpaQueryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
    // function 소문자로 변경, .lower()
    @Test
    public void sqlFunction2(){
        List<String> result = jpaQueryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(member.username.lower()))
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }

    }
}
