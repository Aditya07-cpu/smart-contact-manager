package com.smart.smartcontactmanager.Repository;

import com.smart.smartcontactmanager.Model.Contact;
import com.smart.smartcontactmanager.Model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {

    @Query("from Contact as c where c.user.id=:userId")
    public Page<Contact> findContactsByUser(int userId, Pageable pageable);
}
