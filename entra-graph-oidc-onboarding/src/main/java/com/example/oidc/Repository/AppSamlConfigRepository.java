package com.example.oidc.Repository;

public interface AppSamlConfigRepository extends JpaRepository<AppSamlConfig, Long> {
    List<AppSamlConfig> findByApplicationId(Long applicationId);
    
}
