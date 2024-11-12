package com.e203.meeting.request;

import com.e203.project.entity.Project;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingRequestDto {

    private String meetingTitle;
    private Integer projectId;

    @Builder
    private MeetingRequestDto(String meetingTitle, Integer projectId) {
        this.meetingTitle = meetingTitle;
        this.projectId = projectId;
    }
}
