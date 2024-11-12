package com.e203.meeting.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OneMeetingResponseDto {

    private Integer meetingId;
    private Integer projectId;
    private String meetingTitle;
    private String meetingVoiceUrl;
    private SttResponseDto sttResponseDto;
    private LocalDateTime meetingCreateTime;
    private Integer meetingVoiceTime;

    @Builder
    private OneMeetingResponseDto(Integer meetingId, Integer projectId, String meetingTitle
            , String meetingVoiceUrl, LocalDateTime meetingCreateTime, SttResponseDto sttResponseDto
            , Integer meetingVoiceTime) {

        this.meetingId = meetingId;
        this.projectId = projectId;
        this.meetingTitle = meetingTitle;
        this.meetingVoiceUrl = meetingVoiceUrl;
        this.sttResponseDto = sttResponseDto;
        this.meetingCreateTime = meetingCreateTime;
        this.meetingVoiceTime = meetingVoiceTime;
    }
}
