package com.example.oidc.Event;
import lombok.*;
/** if request has any change that will process this event */

@Getter
@Setter
@NoArgsConstructor
@lombok.AllArgsConstructor
public class RequestProcessEvent {
    private Long requestId;
    private string requestType;
    private string trackingId;
    private Authcategory authCategory;

    
}
