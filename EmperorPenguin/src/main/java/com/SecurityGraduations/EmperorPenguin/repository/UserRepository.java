package com.SecurityGraduations.EmperorPenguin.repository;

import com.SecurityGraduations.EmperorPenguin.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByID(String Id);
    List<User> findAll();
}
