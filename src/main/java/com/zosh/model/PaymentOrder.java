package com.zosh.model;

import com.zosh.domain.PaymentMethod;
import com.zosh.domain.PaymentOrderStaus;
import jakarta.persistence.*;

import java.security.PrivateKey;

@Entity
public class PaymentOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long amount;

    private PaymentOrderStaus status;

    private PaymentMethod paymentMethod;

    @ManyToOne
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public PaymentOrderStaus getStatus() {
        return status;
    }

    public void setStatus(PaymentOrderStaus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
