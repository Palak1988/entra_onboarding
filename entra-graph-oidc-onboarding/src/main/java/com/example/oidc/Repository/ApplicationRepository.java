package com.example.oidc.Repository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByApplicationId(Long applicationId);
    
}
