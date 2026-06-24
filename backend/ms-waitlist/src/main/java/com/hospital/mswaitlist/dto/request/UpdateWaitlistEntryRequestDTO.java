package com.hospital.mswaitlist.dto.request;

import com.hospital.mswaitlist.entity.enums.Priority;
import com.hospital.mswaitlist.entity.enums.WaitlistStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWaitlistEntryRequestDTO {

    private WaitlistStatus status;
    private Priority priority;
    private String notes;
}
