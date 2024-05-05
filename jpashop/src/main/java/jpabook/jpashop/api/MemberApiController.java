package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /*
        조회 V1 : 응답 값으로 Member 엔티티를 외부에 직접 노출
        - 문제점
            - 기본적으로 엔티티의 값이 외부에 노출된다.
            - 응답 조건을 맞추기위해 엔티티에 추가적인 로직을 작성해야함.
            - 엔티티가 변경되면 API의 스펙이 변한다.
        - 결론
            - API 응답 조건에 맞춰서 별도의 DTO로 반환한다.
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    /*
        조회 V2 : 응답 값으로 엔티티가 아닌 별도의 DTO를 반환한다.
            - 엔티티를 DTO로 변환해서 반환(Stream)
     */
    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDTO> collect = findMembers.stream()
                .map(m -> new MemberDTO(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDTO{
        private String name;
    }
    /*
        V1 : 요청 값으로 Member 엔티티를 직접 받음
         - 문제점
            - 엔티티에 API 검증을 위한 로직이 들어간다(@NotEmpty 등등)
            - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요구사항을 담긴 힘듬
            - 엔티티가 변경이 되면 스펙이 변한다

         - 해결방안
            - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.

         - @RequestBody : Json으로온 body를 Member에 맵핑
    */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    /*
        V2 : 요청값으로 별도의 클래스(CreateMemberRequest)를 받는다
            - 엔티티와 프레젠테이션 계층을 위한 로직을 분리 할 수 있다.
            - 엔티티와 API 스펙을 명확하게 분리 할 수 있다.
            - 엔티티가 변해도 API 스펙이 변하지않는다.

       참고 : 실무에서는 엔티티를 외부에 노출하면 안된다.
    */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberReqeust request) {

        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberReqeust {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }


    @Data
    static class CreateMemberRequest {
        private String name;
    }
    @Data
    static class CreateMemberResponse {
        private Long id;
        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
