package com.zosh.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Roi {

    private Double times;

    private String currency;

    private Double percentage;

    public Roi() {
    }

    public Double getTimes() {
        return times;
    }

    public void setTimes(Double times) {
        this.times = times;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}