package com.smart.smartcontactmanager.Repository;

import com.smart.smartcontactmanager.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(value = "select * from user u where u.email=:email", nativeQuery = true)
    public User getUserByUsername(String email);
}
