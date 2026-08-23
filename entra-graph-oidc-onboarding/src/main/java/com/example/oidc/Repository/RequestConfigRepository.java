package com.example.oidc.Repository;

public interface RequestConfigRepository extends JpaRepository<RequestConfig, Long> {
    List<RequestConfig> findByApplicationId(Long applicationId);
    
}
