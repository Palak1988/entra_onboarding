package com.example.oidc.Repository;

public interface StatusRepository extends JpaRepository<Status, Long> {
    Optional<Status> findFirstBystatus(Long applicationId);
    Status findByApplicationIdAndStatus(Long applicationId, String status);
    
}
