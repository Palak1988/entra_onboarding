package com.example.oidc.Repository;

public interface StageRepository extends JpaRepository<Stage, Long> {
    Optional<Stage> findFirstBystage(String stage);
    
}
