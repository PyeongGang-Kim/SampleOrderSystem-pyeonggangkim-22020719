package com.example.sampleordersystem.repository;

import com.example.sampleordersystem.model.sample.Sample;

import java.util.List;
import java.util.Optional;

public interface SampleRepository {
    Sample save(Sample sample);
    Optional<Sample> findById(Long id);
    List<Sample> findAll();
    Sample update(Sample sample);
    void deleteById(Long id);
}
