package com.e203.meeting.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 기본 생성자를 protected로 설정
@JsonIgnoreProperties(ignoreUnknown = true)
public class SttResponseDto {
    private List<Segment> segments;
    private String text;
    private List<Speaker> speakers;

    // @JsonCreator를 사용한 생성자 Jackson 라이브러리에서 JSON을 Java 객체로 역직렬화할 때 사용할 생성자를 지정하는 역할을 합니다.
    @JsonCreator
    protected SttResponseDto(
            @JsonProperty("segments") List<Segment> segments,
            @JsonProperty("text") String text,
            @JsonProperty("speakers") List<Speaker> speakers
    ) {
        this.segments = segments;
        this.text = text;
        this.speakers = speakers;
    }
}
