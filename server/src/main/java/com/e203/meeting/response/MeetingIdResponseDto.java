package com.e203.meeting.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingIdResponseDto {

    private Integer meetingId;

    @Builder
    private MeetingIdResponseDto(Integer meetingId) {
        this.meetingId = meetingId;
    }
}
