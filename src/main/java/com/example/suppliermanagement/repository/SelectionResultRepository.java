package com.example.suppliermanagement.repository;

import com.example.suppliermanagement.model.SelectionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelectionResultRepository extends JpaRepository<SelectionResult, Long> {

    // 按抽取类型查询结果
    Page<SelectionResult> findBySelectionTypeOrderByCreatedAtDesc(String selectionType, Pageable pageable);
}
    