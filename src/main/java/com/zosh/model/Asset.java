package com.zosh.model;

import jakarta.persistence.*;
import lombok.*;

import java.security.PrivateKey;

@Entity
@Getter
@Setter
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private double quantity;
    private double buyPrice;


    @ManyToOne
    private Coin coin;

    @ManyToOne
    private User user;

    public void setId(Long id) {
        this.id = id;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public void setCoin(Coin coin) {
        this.coin = coin;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
