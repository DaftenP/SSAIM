package com.e203.weeklyremind.service;

import com.e203.dailyremind.entity.DailyRemind;
import com.e203.dailyremind.repository.DailyRemindRepository;
import com.e203.global.utils.ChatAiService;
import com.e203.project.entity.Project;
import com.e203.project.entity.ProjectMember;
import com.e203.project.repository.ProjectMemberRepository;
import com.e203.project.repository.ProjectRepository;
import com.e203.weeklyremind.entity.WeeklyRemind;
import com.e203.weeklyremind.repository.WeeklyRemindRepository;
import com.e203.weeklyremind.request.WeeklyRemindRequestDto;
import com.e203.weeklyremind.response.DevelopmentStoryResponseDto;
import com.e203.weeklyremind.response.WeeklyRemindDto;
import com.e203.weeklyremind.response.WeeklyRemindResponseDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyRemindService {

    private final WeeklyRemindRepository weeklyRemindRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ChatAiService chatAiService;
    private final ProjectRepository projectRepository;
    private final DailyRemindRepository dailyRemindRepository;

    public boolean saveWeeklyRemind(WeeklyRemindRequestDto message, int projectId) {

        ProjectMember projectMember = projectMemberRepository
                .findById(message.getProjectMemberId())
                .orElse(null);

        if (projectMember == null) {
            return false;
        }
        Project project = projectRepository.findById(projectId).orElse(null);

        List<DailyRemind> dailyRemindList = dailyRemindRepository.searchDailyReminds(message.getProjectMemberId(),
                message.getStartDate(), message.getEndDate(), projectId);

        StringBuilder dailyReminds = new StringBuilder();

        for (DailyRemind dailyRemind : dailyRemindList) {
            dailyReminds.append(dailyRemind.getDailyRemindContents());
        }

        String summary = chatAiService.generateWeeklyRemind(dailyReminds.toString());

        WeeklyRemind weeklyRemind = WeeklyRemind.builder()
                .weeklyRemindContents(summary)
                .projectId(project)
                .weeklyRemindAuthor(projectMember)
                .weeklyRemindStardDate(message.getStartDate())
                .weeklyRemindEndDate(message.getEndDate()).build();

        weeklyRemindRepository.save(weeklyRemind);

        return true;
    }

    public List<WeeklyRemindResponseDto> searchWeeklyRemind (int projectId, Integer author
            ,LocalDate checkDate ,LocalDate startDate, LocalDate endDate) {

        List<WeeklyRemind> weeklyRemindList = weeklyRemindRepository.findWeeklyReminds(projectId, author
                , checkDate, startDate ,endDate);
        List<WeeklyRemindResponseDto> weeklyRemindResponseDtoList = new ArrayList<>();

        for(WeeklyRemind weeklyRemind : weeklyRemindList) {

            weeklyRemindResponseDtoList.add(WeeklyRemindResponseDto.builder()
                    .projectMemberId(weeklyRemind.getWeeklyRemindAuthor().getId())
                    .projectId(weeklyRemind.getProjectId().getId())
                    .username(weeklyRemind.getWeeklyRemindAuthor().getUser().getUserName())
                    .userImage(weeklyRemind.getWeeklyRemindAuthor().getUser().getUserProfileImage())
                    .content(weeklyRemind.getWeeklyRemindContents())
                    .weeklyRemindId(weeklyRemind.getWeeklyRemindId())
                    .startDate(weeklyRemind.getWeeklyRemindStardDate())
                    .endDate(weeklyRemind.getWeeklyRemindEndDate()).build());
        }

        return weeklyRemindResponseDtoList;
    }

    public List<DevelopmentStoryResponseDto> searchDevelopmentStory (Integer userId) {

        List<WeeklyRemind> weeklyRemindList = weeklyRemindRepository.findWeeklyRemindsByUserId(userId);
        Map<Integer, DevelopmentStoryResponseDto> developmentStoryResponseDtoMap = new HashMap<>();

        for (WeeklyRemind weeklyRemind : weeklyRemindList) {

            Integer projectId = weeklyRemind.getProjectId().getId();

            WeeklyRemindDto weeklyRemindDto = WeeklyRemindDto.builder()
                    .weeklyRemindId(weeklyRemind.getWeeklyRemindId())
                    .content(weeklyRemind.getWeeklyRemindContents())
                    .startDate(weeklyRemind.getWeeklyRemindStardDate())
                    .endDate(weeklyRemind.getWeeklyRemindEndDate()).build();

            if (developmentStoryResponseDtoMap.containsKey(projectId)) {
                developmentStoryResponseDtoMap.get(projectId).getWeeklyRemind().add(weeklyRemindDto);
            } else {
                // 없는 경우 새로운 DevelopmentStoryResponseDto 생성 및 리스트 초기화
                List<WeeklyRemindDto> weeklyRemindDtos = new ArrayList<>();
                weeklyRemindDtos.add(weeklyRemindDto);

                DevelopmentStoryResponseDto dto = DevelopmentStoryResponseDto.builder()
                        .projectMemberId(weeklyRemind.getWeeklyRemindAuthor().getId())
                        .projectId(projectId)
                        .username(weeklyRemind.getWeeklyRemindAuthor().getUser().getUserName())
                        .userId(weeklyRemind.getWeeklyRemindAuthor().getUser().getUserId())
                        .projectName(weeklyRemind.getProjectId().getTitle())
                        .projectStartDate(weeklyRemind.getProjectId().getStartDate().toLocalDate())
                        .projectEndDate(weeklyRemind.getProjectId().getEndDate().toLocalDate())
                        .weeklyRemind(weeklyRemindDtos)  // 리스트로 설정
                        .build();

                developmentStoryResponseDtoMap.put(projectId, dto);
            }
        }

        return new ArrayList<>(developmentStoryResponseDtoMap.values());

    }

    @Transactional
    public boolean editWeeklyRemind(int weeklyRemindId, int projectId, WeeklyRemindRequestDto message) {
        WeeklyRemind weeklyRemind = weeklyRemindRepository.findById(weeklyRemindId).orElse(null);
        if (weeklyRemind == null) {
            return false;
        }

        List<DailyRemind> dailyRemindList = dailyRemindRepository.searchDailyReminds(message.getProjectMemberId(),
                message.getStartDate(), message.getEndDate(), projectId);

        StringBuilder dailyReminds = new StringBuilder();

        for (DailyRemind dailyRemind : dailyRemindList) {
            dailyReminds.append(dailyRemind.getDailyRemindContents());
        }

        String summary = chatAiService.generateWeeklyRemind(dailyReminds.toString());

        weeklyRemind.updateWeeklyRemind(summary);

        return true;
    }
}
