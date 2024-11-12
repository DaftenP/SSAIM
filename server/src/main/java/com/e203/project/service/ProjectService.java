package com.e203.project.service;

import com.e203.document.service.ApiDocsService;
import com.e203.document.service.FunctionDescriptionService;
import com.e203.document.service.ProposalService;
import com.e203.project.dto.request.ProjectCreateRequestDto;
import com.e203.project.dto.request.ProjectMemberCreateRequestDto;
import com.e203.project.dto.response.ProjectFindResponseDto;
import com.e203.project.dto.response.ProjectMemberFindResponseDto;
import com.e203.project.entity.Project;
import com.e203.project.entity.ProjectMember;
import com.e203.project.repository.ProjectMemberRepository;
import com.e203.project.repository.ProjectRepository;
import com.e203.user.entity.User;
import com.e203.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberService projectMemberService;
    private final ApiDocsService apiDocsService;
    private final FunctionDescriptionService functionDescriptionService;
    private final ProposalService proposalService;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public boolean createProject(ProjectCreateRequestDto projectCreateRequestDto) {

        Project entity = projectCreateRequestDto.toEntity();
        Project project = projectRepository.save(entity);

        projectCreateRequestDto.getTeamMembers().stream()
                .map(member -> createProjectMember(project, member))
                .forEach(projectMemberService::save);

        apiDocsService.saveApiDocs(String.valueOf(project.getId()));
        functionDescriptionService.saveFuncDesc(String.valueOf(project.getId()));
        proposalService.saveProposal(String.valueOf(project.getId()));

        //if project, projectMember가 잘 저장되었다면
        return true;
    }

    public Project findEntity(Integer projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        return project.orElse(null);
    }

    public List<Project> findAll(int userId) {
        return projectMemberRepository.findProjectsByUserId(userId);
    }

    public List<ProjectFindResponseDto> findAllProjects(int userId) {
        List<Project> projects = findAll(userId);
        List<ProjectFindResponseDto> projectFindResponseDtos = new ArrayList<>();
        for (Project project : projects) {
            ProjectFindResponseDto projectFindResponseDto = getProjectFindResponseDto(project);
            projectFindResponseDtos.add(projectFindResponseDto);
        }
        return projectFindResponseDtos;
    }

    public ProjectFindResponseDto findProjectInfo(Integer projectId) {
        Project project = findEntity(projectId);
        return getProjectFindResponseDto(project);
    }

    private ProjectFindResponseDto getProjectFindResponseDto(Project project) {
        List<ProjectMemberFindResponseDto> pmDto = createProjectMemberFindResponseDtos(project);
        return ProjectFindResponseDto.builder()
                .id(project.getId())
                .name(project.getName())
                .title(project.getTitle())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .jiraApi(project.getJiraApi())               // 프로젝트의 Jira API 정보
                .gitlabApi(project.getGitlabApi())           // 프로젝트의 GitLab API 정보
                .progressBack(project.getProgressBack())    // 백엔드 진행도
                .progressFront(project.getProgressFront())// 프론트엔드 진행도
                .projectMembers(pmDto)
                .build();
    }

    private List<ProjectMemberFindResponseDto> createProjectMemberFindResponseDtos(Project project) {
        List<ProjectMemberFindResponseDto> pmDtos = new ArrayList<>();
        for (ProjectMember member : project.getProjectMemberList()) {
            ProjectMemberFindResponseDto dto =
                    ProjectMemberFindResponseDto.builder()
                            .pmId(member.getId())
                            .userId(member.getUser().getUserId())
                            .name(member.getUser().getUserName())
                            .profileImage(member.getUser().getUserProfileImage())
                            .role(member.getRole())
                            .build();
            pmDtos.add(dto);
        }
        return pmDtos;
    }

    private ProjectMember createProjectMember(Project project, ProjectMemberCreateRequestDto member) {
        User user = userRepository.findById(member.getId()).orElse(null);
        return ProjectMember.builder()
                .user(user)
                .project(project)
                .role(member.getRole())
                .build();
    }
}

