package com.example.order.ordersystem.member.service;

import com.example.order.ordersystem.member.domain.Member;
import com.example.order.ordersystem.member.dtos.MemberCreateDto;
import com.example.order.ordersystem.member.dtos.MemberDetailDto;
import com.example.order.ordersystem.member.dtos.MemberListDto;
import com.example.order.ordersystem.member.dtos.MemberLoginDto;
import com.example.order.ordersystem.member.repositroy.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Long save(MemberCreateDto dto){
        if (memberRepository.findAllByEmail(dto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이메일 중복");
        }
        Member member = memberRepository.save(dto.toEntity(passwordEncoder.encode(dto.getPassword())));

        return member.getId();
    }
    public Member login(MemberLoginDto dto){
        Optional<Member> optmember = memberRepository.findAllByEmail(dto.getEmail());
        boolean check = true;
        if (!optmember.isPresent()){
            check = false;
        }else {
            if (!passwordEncoder.matches(dto.getPassword(), optmember.get().getPassword())){
                check = false;
            }
        }
        if (!check){
            throw new IllegalArgumentException("email 또는 비밀번호가 일치하지 않습니다.");
        }
        return optmember.get();
    }
    @Transactional(readOnly = true)
    public List<MemberListDto> findAll(){
        List<MemberListDto> dto = memberRepository.findAll().stream().map(a->MemberListDto.fromEntity(a)).collect(Collectors.toList());
        return dto;
    }
    public MemberDetailDto myInfo(){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("이메일이 없습니다"));
        MemberDetailDto dto = MemberDetailDto.fromEntity(member);
        return dto;
    }
    @Transactional(readOnly = true)
    public MemberDetailDto findById(Long id){
        Optional<Member> optMember = memberRepository.findById(id);
        Member member = optMember.orElseThrow(()-> new EntityNotFoundException("아이디가 없습니다"));
        MemberDetailDto dto = MemberDetailDto.fromEntity(member);
        return dto;
    }

    }
