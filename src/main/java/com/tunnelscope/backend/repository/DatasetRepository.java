package com.tunnelscope.backend.repository;

import com.tunnelscope.backend.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {
}