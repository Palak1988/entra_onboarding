package com.example.oidc.Repository;
import com.example.oidc.model.*;

public interface ApplicationLogRepository extends JpaRepository<ApplicationLog, Long> {
    List<ApplicationLog> findByApplicationId(Long applicationId);
}
