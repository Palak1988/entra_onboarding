package com.example.oidc.Repository;

import java.util.Optional;

public interface OnboardingEnvironmentRepository extends JpaRepository<OnboardingEnvironment, Long> {
    Optional<OnboardingEnvironment> findByApplicationId(Long applicationId);
    Optional<OnboardingEnvironment> findByApplicationIdAndEnvironment(Long applicationId, String environment);
    OnboardingEnvironment findOnboardingEnvironmentByApplicationId(Long applicationId);
    
}
