package com.EmperorPenguin.SangmyungBank.otp.repository;

import com.EmperorPenguin.SangmyungBank.member.entity.Member;
import com.EmperorPenguin.SangmyungBank.otp.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
        Optional<Otp> findByMemberId(Member loginUser);
}


