package com.e203.user.service;

import com.e203.global.utils.FileUploader;
import com.e203.user.entity.User;
import com.e203.user.repository.UserRepository;
import com.e203.user.request.UserEditInfoDto;
import com.e203.user.request.UserSignUpRequestDto;
import com.e203.user.response.UserInfoResponseDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final FileUploader fileUploader;

    public boolean createUser(UserSignUpRequestDto userSignupRequestDto, MultipartFile profileImage) {
        if (userRepository.existsByUserEmail(userSignupRequestDto.getUserEmail())) {
            return false;
        }

        User user = User.builder()
                .userName(userSignupRequestDto.getUserName())
                .userEmail(userSignupRequestDto.getUserEmail())
                .userCampus(userSignupRequestDto.getUserCampus())
                .userGender(userSignupRequestDto.getUserGender())
                .userPhone(userSignupRequestDto.getUserPhone())
                .userClass(userSignupRequestDto.getUserClass())
                .userBirth(userSignupRequestDto.getUserBirth())
                .userPw(hashPassword(userSignupRequestDto.getUserPw()))
                .userNickname(userSignupRequestDto.getUserNickname())
                .userGeneration(userSignupRequestDto.getUserGeneration())
                .userRole(0)
                .userProfileImage(uploadProfileImage(profileImage))
                .build();

        userRepository.save(user);
        return true;
    }


    private String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public UserInfoResponseDto getUserInfo(int userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            return null;
        }

        return UserInfoResponseDto.fromEntity(user);
    }

    public List<UserInfoResponseDto> searchUsers(String userName, String userEmail, Integer userClass,
                                                 Integer userCampus, Integer userGeneration, String userNickname,
                                                 Integer userRole, LocalDate userBirth, Integer userGender) {

        List<User> users = userRepository.searchUsers(userName, userEmail, userClass, userCampus, userGeneration,
                userNickname, userRole, userBirth, userGender);

        return users.stream().map(UserInfoResponseDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public void modifyInfo(int userId, UserEditInfoDto dto, MultipartFile profileImage) {
        String imageUrl = uploadProfileImage(profileImage);
        User user = userRepository.findByUserId(userId);

        updateUserFields(user, dto);
        Optional.ofNullable(imageUrl).ifPresent(user::setUserProfileImage);
    }

    private String uploadProfileImage(MultipartFile profileImage) {
        return Optional.ofNullable(profileImage)
                .map(fileUploader::upload)
                .orElse(null);
    }

    private void updateUserFields(User user, UserEditInfoDto dto) {
        updateField(dto.getUserEmail(), user::setUserEmail);
        updateField(dto.getUserName(), user::setUserName);
        updateField(dto.getUserNickname(), user::setUserNickname);
        updateField(dto.getUserPw(), pw -> user.setUserPw(hashPassword(pw)));
        updateField(dto.getUserGender(), user::setUserGender);
        updateField(dto.getUserSkills(), user::setUserSkills);
        updateField(dto.getUserBirth(), user::setUserBirth);
        updateField(dto.getUserClass(), user::setUserClass);
        updateField(dto.getUserGeneration(), user::setUserGeneration);
        updateField(dto.getUserCampus(), user::setUserCampus);
        updateField(dto.getUserPhone(), user::setUserPhone);
        updateField(dto.getUserProfileMessage(), user::setUserProfileMessage);
    }

    private <T> void updateField(T value, Consumer<T> setter) {
        Optional.ofNullable(value).ifPresent(setter);
    }


}
