package com.zosh.model;

import com.zosh.domain.WithdrawalStatus;
import jakarta.persistence.*;

import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class Withdrawal {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private WithdrawalStatus Status;

    private Long amount;


    @ManyToOne
    private User user;


    private LocalDateTime date =LocalDateTime.now();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WithdrawalStatus getStatus() {
        return Status;
    }

    public void setStatus(WithdrawalStatus status) {
        Status = status;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
