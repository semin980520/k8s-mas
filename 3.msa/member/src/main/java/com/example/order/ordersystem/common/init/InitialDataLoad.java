package com.example.order.ordersystem.common.init;

import com.example.order.ordersystem.member.domain.Member;
import com.example.order.ordersystem.member.domain.Role;
import com.example.order.ordersystem.member.repositroy.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class InitialDataLoad implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoad(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if(memberRepository.findAllByEmail("admin@naver.com").isPresent()){
            return;
        }
        memberRepository.save(Member.builder()
                .name("admin")
                .email("admin@naver.com")
                .password(passwordEncoder.encode("12341234"))
                .role(Role.ADMIN)
                .build());
    }

}
