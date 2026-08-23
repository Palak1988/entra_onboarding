package com.example.oidc.Listener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.oidc.Event.*;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class applicationProcessListener {
    @Resource
    private Appconfigservice appconfigservice;

    @EventListener
    public void handleApplicationProcessEvent(ApplicationProcessEvent event) {
        log.info("Received ApplicationProcessEvent for trackingId [{}]", event.getTrackingId());
        appconfigservice.saveOrUpdateOpenidConfig(event.getApplicationId());
    }
    
}
