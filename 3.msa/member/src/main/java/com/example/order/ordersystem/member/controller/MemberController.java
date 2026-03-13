package com.example.order.ordersystem.member.controller;

import com.example.order.ordersystem.common.auth.JwtTokenProvider;
import com.example.order.ordersystem.member.domain.Member;
import com.example.order.ordersystem.member.dtos.*;
import com.example.order.ordersystem.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/member/create")
    public Long create(@RequestBody MemberCreateDto dto){

        return memberService.save(dto);
    }
    @PostMapping("/member/doLogin")
    public TokenReturnDto login(@RequestBody MemberLoginDto dto){
        Member member = memberService.login(dto);
        String accessToken = jwtTokenProvider.createToken(member);
//        리프레쉬토큰 생성 및 저장 :
        String refreshToken = jwtTokenProvider.createRtToken(member);
        return TokenReturnDto.builder() // TokenReturnDto 만들어 at, rt 함께 리턴
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    @GetMapping("/member/list")
    public List<MemberListDto> findAll(){
        List<MemberListDto> dto = memberService.findAll();
        return dto;
    }
    @GetMapping("/member/myinfo")
//    X로 시작하는 헤더명은 개발자가 인위적으로 만든 Header인 경우에 관례적으로 사용
    public MemberDetailDto myInfo(@RequestHeader("X-User-Email")String email) {
        MemberDetailDto dto = memberService.myInfo(email);
        return dto;
    }
    @GetMapping("/member/detail/{id}")
    public MemberDetailDto findById(@PathVariable Long id){
        MemberDetailDto dto = memberService.findById(id);
        return dto;
    }
    @PostMapping("/member/refresh-at")
    public ResponseEntity<?> refreshAt(@RequestBody RefreshTokenDto dto){
//        rt 검증 (1.토큰 자체 검증 2. redis 조회 검증)
        Member member = jwtTokenProvider.validateRt(dto.getRefreshToken());

//        at신규 생성
        String accessToken = jwtTokenProvider.createToken(member);
//        리프레쉬토큰 생성 및 저장 :
        String refreshToken = jwtTokenProvider.createRtToken(member);
        TokenReturnDto token = TokenReturnDto.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(token);
    }
}
