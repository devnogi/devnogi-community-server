package until.the.eternity.dcs.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.user.dto.request.UserSummaryCreateRequest;
import until.the.eternity.dcs.domain.user.dto.request.UserSummaryUpdateRequest;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryDetailResponse;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryPersistResponse;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.enums.UserGrade;
import until.the.eternity.dcs.domain.user.exception.UserAlreadyExistsException;
import until.the.eternity.dcs.domain.user.exception.UserGradeNotFoundException;
import until.the.eternity.dcs.domain.user.exception.UserNotFoundException;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSummaryService {
    private final UserSummaryRepository userSummaryRepository;
    private final UserSummaryConverter userSummaryConverter;

    @Transactional
    public UserSummaryPersistResponse createUserSummary(
            UserSummaryCreateRequest userSummaryCreateRequest) {
        Long userId = userSummaryCreateRequest.userId();
        if (existsUserSummaryById(userId)) {
            throw new UserAlreadyExistsException(userId);
        }
        UserSummary userSummary =
                userSummaryConverter.createUserSummaryToUserSummary(userSummaryCreateRequest);
        UserSummary savedUserSummary = userSummaryRepository.save(userSummary);
        return userSummaryConverter.userSummaryToUserSummaryPersistResponse(savedUserSummary);
    }

    public UserSummaryDetailResponse findUserSummary(Long userId) {
        if (!existsUserSummaryById(userId)) {
            throw new UserNotFoundException(userId);
        }
        UserSummary userSummary = findUserSummaryById(userId);
        return userSummaryConverter.userSummaryToUserSummaryDetailResponse(userSummary);
    }

    @Transactional
    public UserSummaryPersistResponse updateUserSummary(
            UserSummaryUpdateRequest userSummaryUpdateRequest) {
        Long userId = userSummaryUpdateRequest.userId();
        UserSummary user = findUserSummaryById(userId);
        UserGrade userGrade =
                UserGrade.fromCode(userSummaryUpdateRequest.grade())
                        .orElseThrow(
                                () ->
                                        new UserGradeNotFoundException(
                                                userSummaryUpdateRequest.grade()));
        user.update(
                userSummaryUpdateRequest.nickname(), userSummaryUpdateRequest.level(), userGrade);
        userSummaryRepository.save(user);
        return userSummaryConverter.userSummaryToUserSummaryPersistResponse(user);
    }

    @Transactional
    public void deleteUserSummary(Long userId) {
        if (!existsUserSummaryById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userSummaryRepository.deleteById(userId);
    }

    private UserSummary findUserSummaryById(Long id) {
        return userSummaryRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public boolean existsUserSummaryById(Long userId) {
        return userSummaryRepository.existsById(userId);
    }

    public List<UserSummary> findByIdIn(List<Long> userIds) {
        return userSummaryRepository.findByIdIn(userIds);
    }
}
