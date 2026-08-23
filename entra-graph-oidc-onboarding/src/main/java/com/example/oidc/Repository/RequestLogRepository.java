package com.example.oidc.Repository;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
    List<RequestLog> findByRequestId(Long requestId);
    
}
