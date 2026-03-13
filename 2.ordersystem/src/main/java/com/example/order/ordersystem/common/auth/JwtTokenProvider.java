package com.example.order.ordersystem.common.auth;

import com.example.order.ordersystem.member.domain.Member;
import com.example.order.ordersystem.member.repositroy.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String st_secret_ket;
    @Value("${jwt.expiration}")
    private int expiration;
    private Key secret_key;

    @Value("${jwt.secretKeyRt}")
    private String st_secret_ket_rt;
    @Value("${jwt.expirationRt}")
    private int expirationRt;
    private Key secret_key_rt;

    private final RedisTemplate<String , String> redisTemplate;
    private final MemberRepository memberRepository;
    @Autowired
    public JwtTokenProvider(@Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate, MemberRepository memberRepository) {
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
    }


    @PostConstruct
    public void init(){
        secret_key = new SecretKeySpec(Base64.getDecoder().decode(st_secret_ket), SignatureAlgorithm.HS512.getJcaName());
        secret_key_rt = new SecretKeySpec(Base64.getDecoder().decode(st_secret_ket_rt), SignatureAlgorithm.HS512.getJcaName());
    }
    public String createToken(Member member){

        Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("role", member.getRole().toString());

        Date now = new Date();


        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration*60*1000L)) //
                .signWith(secret_key)
                .compact();

        return accessToken;
    }
    public String createRtToken(Member member){
//        유효기간이 긴 rt토큰 생성
        Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("role", member.getRole().toString());

        Date now = new Date();


        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt*60*1000L)) //
                .signWith(secret_key_rt)
                .compact();
//        rt토큰 redis에 저장
//        opsForSet(또는 zset , List, 등 존재) 레디스 타입 value 일반 스트링 자료구조
//        redisTemplate.opsForValue().set(member.getEmail(), refreshToken);
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, expirationRt, TimeUnit.MINUTES); //3000분 ttl
        return refreshToken;
    }

    public Member validateRt(String refreshToken){
        Claims claims = null;

        try {
//        rt토큰 그 자체를 검증
            claims = Jwts.parserBuilder()
                    .setSigningKey(st_secret_ket_rt)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (Exception e){
            throw new IllegalArgumentException("잘못 된 토큰입니다.");
        }
        String email = claims.getSubject();
        Member member = memberRepository.findAllByEmail(email).orElseThrow(()-> new EntityNotFoundException("entity not found"));

//        redis rt와 비교 겁증
        String redisRt = redisTemplate.opsForValue().get(email);
        if (!redisRt.equals(refreshToken)){
            throw new IllegalArgumentException("잘못 된 토큰입니다.");
        }

        return member;
    }
}
