package com.assessment.transaction_categorisation_engine.repository;

import com.assessment.transaction_categorisation_engine.domain.MerchantCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MerchantCategoryRepository extends JpaRepository<MerchantCategory, Long> {

    Optional<MerchantCategory> findByMerchantName(String merchantName);

    List<MerchantCategory> findByMerchantNameIn(Collection<String> merchantNames);

    boolean existsByMerchantName(String merchantName);
}
