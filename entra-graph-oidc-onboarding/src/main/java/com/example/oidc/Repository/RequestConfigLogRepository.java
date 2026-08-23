package com.example.oidc.Repository;

public interface RequestConfigLogRepository extends JpaRepository<RequestConfigLog, Long> {
    List<RequestConfigLog> findByApplicationId(Long applicationId);
    
}
