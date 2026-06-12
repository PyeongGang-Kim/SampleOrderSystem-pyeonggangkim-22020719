package com.example.sampleordersystem.model.sample;

public class Sample {

    private final Long id;
    private String name;
    private double prodRate;
    private double yield;

    public Sample(Long id, String name, double prodRate, double yield) {
        this.id = id;
        this.name = name;
        this.prodRate = prodRate;
        this.yield = yield;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public double getProdRate() { return prodRate; }
    public double getYield() { return yield; }

    public void setName(String name) { this.name = name; }
    public void setProdRate(double prodRate) { this.prodRate = prodRate; }
    public void setYield(double yield) { this.yield = yield; }
}
