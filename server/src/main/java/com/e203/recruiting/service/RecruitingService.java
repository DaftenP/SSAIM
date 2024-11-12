package com.e203.recruiting.service;

import com.e203.global.entity.ProjectDomain;
import com.e203.global.response.PaginationResponseDto;
import com.e203.recruiting.entity.BoardRecruiting;
import com.e203.recruiting.entity.RecruitingMember;
import com.e203.recruiting.repository.RecruitingMemberRepository;
import com.e203.recruiting.repository.RecruitingRepository;
import com.e203.recruiting.request.*;
import com.e203.recruiting.response.*;
import com.e203.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitingService {

    private final RecruitingRepository recruitingRepository;

    private final RecruitingMemberRepository recruitingMemberRepository;

    @Transactional
    public void createPost(RecruitingWriteRequestDto dto) {

        BoardRecruiting boardRecruiting = BoardRecruiting.builder()
                .author(new User(dto.getAuthor()))
                .title(dto.getTitle())
                .content(dto.getContent())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .firstDomain(Optional.ofNullable(dto.getFirstDomain())
                        .map(ProjectDomain::new)
                        .orElse(null))
                .secondDomain(Optional.ofNullable(dto.getSecondDomain())
                        .map(ProjectDomain::new)
                        .orElse(null))
                .campus(dto.getCampus())
                .memberTotal(dto.getMemberTotal())
                .memberInfra(dto.getMemberInfra())
                .memberFrontend(dto.getMemberFrontend())
                .memberBackend(dto.getMemberBackend())
                .build();

        boardRecruiting.setRecruitingMembers(List.of(RecruitingMember.builder()
                .boardRecruiting(boardRecruiting)
                .user(boardRecruiting.getAuthor())
                .recruitingMemberStatus(1)
                .recruitingMemberPosition(dto.getPosition())
                .build()));

        recruitingRepository.save(boardRecruiting);

    }

    @Transactional
    public RecruitingPostDetailResponseDto getPost(int postId, int userId) {
        BoardRecruiting recruiting = findRecruitingPost(postId);
        if (recruiting == null) {
            return null;
        }
        List<RecruitingMember> members = recruiting.getRecruitingMembers().stream()
                .filter(member -> member.getDeletedAt() == null).toList();
        List<RecruitingMemberResponseDto> recruitingMembers = getActiveRecruitingMembers(members);
        List<RecruitingCandidateResponseDto> recruitingCandidates = getRecruitingCandidates(recruiting, members, userId);

        return createResponseDto(recruiting, members, recruitingMembers, recruitingCandidates);
    }

    private BoardRecruiting findRecruitingPost(int postId) {
        return recruitingRepository.findByRecruitingIdAndDeletedAtIsNull(postId);
    }

    private List<RecruitingMemberResponseDto> getActiveRecruitingMembers(List<RecruitingMember> recruiting) {
        return recruiting.stream()
                .filter(member -> member.getRecruitingMemberStatus() == 1)
                .map(RecruitingMemberResponseDto::fromEntity)
                .toList();
    }

    private List<RecruitingCandidateResponseDto> getRecruitingCandidates(BoardRecruiting recruiting,
                                                                         List<RecruitingMember> members, int userId) {
        if (userId == recruiting.getAuthor().getUserId()) {
            return getAllCandidates(members);
        } else {
            return getUserCandidates(members, userId);
        }
    }

    private List<RecruitingCandidateResponseDto> getAllCandidates(List<RecruitingMember> members) {
        return members.stream()
                .filter(member -> member.getRecruitingMemberStatus() != 1)
                .map(RecruitingCandidateResponseDto::fromEntity)
                .toList();
    }

    private List<RecruitingCandidateResponseDto> getUserCandidates(List<RecruitingMember> members, int userId) {
        return members.stream()
                .filter(member -> member.getUser().getUserId() == userId
                        && member.getRecruitingMemberStatus() != 1)
                .map(RecruitingCandidateResponseDto::fromEntity)
                .toList();
    }

    private RecruitingPostDetailResponseDto createResponseDto(BoardRecruiting recruiting,
                                                              List<RecruitingMember> members,
                                                              List<RecruitingMemberResponseDto> recruitingMembers,
                                                              List<RecruitingCandidateResponseDto> recruitingCandidates) {
        RecruitingPostDetailResponseDto dto = RecruitingPostDetailResponseDto.fromEntity(recruiting, recruitingMembers, recruitingCandidates);
        dto.setRecruitedTotal(recruitingMembers.size());
        dto.setCandidateCount(members.size() - recruitingMembers.size());
        return dto;
    }


    public PaginationResponseDto<RecruitingPostResponseDto> searchPosts(String title, Integer position, Integer campus,
                                                                        Integer domain, Integer status, Integer size,
                                                                        Integer pageNum) {

        Pageable pageable = PageRequest.of(pageNum - 1, size);
        Page<BoardRecruiting> recruiting = recruitingRepository.searchPosts(title, position, campus, domain, status, pageable);

        return PaginationResponseDto.<RecruitingPostResponseDto>builder()
                .totalPages(recruiting.getTotalPages())
                .currentPage(recruiting.getNumber() + 1)
                .pageSize(size)
                .totalCount(recruiting.getTotalElements())
                .data(recruiting.stream()
                        .map(RecruitingPostResponseDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public String updatePost(int postId, RecruitingEditRequestDto dto, int userId) {
        BoardRecruiting post = findPost(postId);
        if (post == null) {
            return "Not found";
        }
        if (!validateUserAuthorization(post, userId)) {
            return "Not authorized";
        }

        updatePostFields(post, dto);
        updateRecruitingMembers(post, dto.getRecruitingMembers());

        return "Updated";
    }

    private BoardRecruiting findPost(int postId) {
        return recruitingRepository.findByRecruitingIdAndDeletedAtIsNull(postId);
    }

    private boolean validateUserAuthorization(BoardRecruiting post, int userId) {
        return post.getAuthor().getUserId() == userId;
    }

    private void updatePostFields(BoardRecruiting post, RecruitingEditRequestDto dto) {
        updateFieldIfPresent(dto.getTitle(), post::setTitle);
        updateFieldIfPresent(dto.getContent(), post::setContent);
        updateFieldIfPresent(dto.getCampus(), post::setCampus);
        updateFieldIfPresent(dto.getStartDate(), post::setStartDate);
        updateFieldIfPresent(dto.getEndDate(), post::setEndDate);
        updateFieldIfPresent(dto.getMemberFrontend(), post::setMemberFrontend);
        updateFieldIfPresent(dto.getMemberBackend(), post::setMemberBackend);
        updateFieldIfPresent(dto.getMemberInfra(), post::setMemberInfra);
        updateFieldIfPresent(dto.getMemberTotal(), post::setMemberTotal);
        updateDomainIfPresent(dto.getFirstDomain(), post::setFirstDomain);
        updateDomainIfPresent(dto.getSecondDomain(), post::setSecondDomain);
    }

    private void updateDomainIfPresent(Integer domain, Consumer<ProjectDomain> setter) {
        setter.accept(domain != null ? new ProjectDomain(domain) : null);
    }

    private void updateRecruitingMembers(BoardRecruiting post, List<RecruitingMemberEditRequestDto> memberEdits) {
        if (memberEdits == null) return;

        for (RecruitingMember member : post.getRecruitingMembers()) {
            memberEdits.stream()
                    .filter(memberEdit -> member.getUser().getUserId() == memberEdit.getUserId())
                    .forEach(memberEdit -> updateMember(member, memberEdit));
        }
    }

    private void updateMember(RecruitingMember member, RecruitingMemberEditRequestDto memberEdit) {
        updateFieldIfPresent(memberEdit.getPosition(), member::setRecruitingMemberPosition);
        if (memberEdit.isDelete()) {
            member.setDeletedAt(LocalDateTime.now());
        }
    }

    private <T> void updateFieldIfPresent(T value, Consumer<T> setter) {
        Optional.ofNullable(value).ifPresent(setter);
    }

    @Transactional
    public String removePost(int postId, int userId) {
        BoardRecruiting post = findPost(postId);
        if (post == null) {
            return "Not found";
        }
        if (!validateUserAuthorization(post, userId)) {
            return "Not authorized";
        }
        post.setDeletedAt(LocalDateTime.now());
        post.getRecruitingMembers().stream()
                .filter(member -> member.getDeletedAt() == null)
                .forEach(member -> member.setDeletedAt(LocalDateTime.now()));

        return "Deleted";
    }

    @Transactional
    public String crateRecruitingMember(int postId, RecruitingApplyRequestDto dto) {

        BoardRecruiting recruiting = recruitingRepository.findByRecruitingIdAndDeletedAtIsNull(postId);

        if (recruiting == null) {
            return "Not found";
        }

        List<RecruitingMember> members = recruitingMemberRepository.searchMemberByUserId(dto.getUserId());

        if (!members.isEmpty()) {
            return "Duplicated";
        }

        RecruitingMember applicant = RecruitingMember.builder()
                .boardRecruiting(recruiting)
                .user(new User(dto.getUserId()))
                .recruitingMemberMessage(dto.getMessage())
                .recruitingMemberPosition(dto.getPosition())
                .recruitingMemberStatus(0)
                .build();

        recruitingMemberRepository.save(applicant);

        return "Done";
    }

    @Transactional
    public String updateApplicant(int postId, int applicantId, int userId, RecruitingApplicantEditRequestDto dto) {

        BoardRecruiting recruiting = recruitingRepository.findByRecruitingIdAndDeletedAtIsNull(postId);
        RecruitingMember applicant = recruitingMemberRepository.findByIdAndDeletedAtIsNull(applicantId);

        if (recruiting == null || applicant == null) {
            return "Not found";
        }
        if (recruiting.getRecruitingId() != applicant.getBoardRecruiting().getRecruitingId()) {
            return "Does not match";
        }

        if (applicant.getUser().getUserId() == userId) {
            if (dto.getPosition() != null) {
                applicant.setRecruitingMemberPosition(dto.getPosition());
            }
            if (dto.getMessage() != null) {
                applicant.setRecruitingMemberMessage(dto.getMessage());
            }
        } else if (recruiting.getAuthor().getUserId() == userId) {
            if (dto.getStatus() != null) {
                applicant.setRecruitingMemberStatus(dto.getStatus());
            }
        } else {
            return "Not authorized";
        }

        return "Done";
    }

    @Transactional
    public String removeApplicant(int postId, int applicantId, int userId) {

        BoardRecruiting recruiting = recruitingRepository.findByRecruitingIdAndDeletedAtIsNull(postId);
        RecruitingMember applicant = recruitingMemberRepository.findByIdAndDeletedAtIsNull(applicantId);

        if (recruiting == null || applicant == null) {
            return "Not found";
        }
        if (recruiting.getRecruitingId() != applicant.getBoardRecruiting().getRecruitingId()) {
            return "Does not match";
        }

        if (applicant.getUser().getUserId() == userId) {
            applicant.setDeletedAt(LocalDateTime.now());
            return "Done";
        } else {
            return "Not authorized";
        }

    }

    public List<RecruitingApplyResponseDto> searchApplication(int userId) {

        return recruitingMemberRepository.searchMemberByUserId(userId).stream()
                .map(RecruitingApplyResponseDto::fromEntity)
                .toList();
    }
}
