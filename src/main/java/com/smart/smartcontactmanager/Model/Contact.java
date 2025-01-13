package com.smart.smartcontactmanager.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "CONTACT")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cId;
    private String name;
    private String secondName;
    private String work;
    private String email;
    private String phone;
    private String image;
    @Column(length = 1000)
    private String description;
    @ManyToOne
    private User user;

}
