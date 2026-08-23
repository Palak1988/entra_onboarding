package com.example.oidc.Repository;

public interface AppOpenidConfigRepository extends JpaRepository<AppOpenidConfig, Long> {
    List<AppOpenidConfig> findByApplicationId(Long applicationId);
    
}
