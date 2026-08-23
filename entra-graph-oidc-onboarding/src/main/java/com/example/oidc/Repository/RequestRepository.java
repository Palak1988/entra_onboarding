package com.example.oidc.Repository;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByApplicationId(Long applicationId);
    
}
