package com.EmperorPenguin.SangmyungBank.member.repository;

import com.EmperorPenguin.SangmyungBank.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(Long userId);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByEmail(String Email);

    Optional<Member> findByPhoneNumber(String PhoneNumber);

    @Modifying(clearAutomatically = true)
    @Query("update Member u set u.loginDate = :loginTime where u.loginId =:loginId")
    void updateLoginDate(@Param("loginTime")String loginTime, @Param("loginId")String LoginId);

    @Modifying(clearAutomatically = true)
    @Query("update Member u set u.password = ?1, u.usingTempPassword = false where u.userId = ?2")
    void updateUserPassword(@Param("newPassword")String newPassword, @Param("userId")Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update Member u set u.password = ?1, u.usingTempPassword = true where u.userId = ?2")
    void updateUserTemplatePassword(@Param("templatePassword")String templatePassword, @Param("userId")Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update Member u set u.modifyDate = ?1 where u.userId = ?2")
    void updateUserModifyDate(@Param("modifyDate")String modifyDate, @Param("userid")Long userid);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}