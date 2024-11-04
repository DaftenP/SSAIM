package com.e203.project.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.e203.project.dto.request.ProjectMemberCreateRequestDto;
import com.e203.project.dto.request.ProjectCreateRequestDto;

import com.e203.project.entity.Project;
import com.e203.project.entity.ProjectMember;
import com.e203.project.repository.ProjectMemberRepository;
import com.e203.project.repository.ProjectRepository;
import com.e203.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {
	private final ProjectRepository projectRepository;
	private final ProjectMemberRepository projectMemberRepository;
	@Transactional
	public boolean createProject(ProjectCreateRequestDto projectCreateRequestDto) {

		Project entity = projectCreateRequestDto.toEntity();
		Project project = projectRepository.save(entity);

		//ProjectMember 생성
		for(int i=0; i<projectCreateRequestDto.getTeamMembers().size(); i++){
			ProjectMember projectMember = createProjectMember(project, projectCreateRequestDto.getTeamMembers().get(i));
			projectMemberRepository.save(projectMember);
		}
		//if project, projectMember가 잘 저장되었다면
		return true;
	}

	private ProjectMember createProjectMember(Project project, ProjectMemberCreateRequestDto member) {
		User user = userTestStub(member);
		return ProjectMember.builder()
				.user(user)
				.project(project)
				.role(member.getRole())
				.build();
	}

	private User userTestStub(ProjectMemberCreateRequestDto member){
		User user = User.builder().userEmail("test@test.com")
			.userName("name")
			.userClass(1)
			.userCampus(2)
			.userGeneration(11)
			.userPw("34234234234")
			.userNickname("testset")
			.userBirth(LocalDate.now())
			.userGender(2)
			.userPhone("010-1111-1111")
			.userProfileImage("https:s3//image")
			.userSkills("spring boot")
			.userProfileMessage("안녕하세요")
			.build();
		user.setUserId(member.getId());
		return user;
	}
}

