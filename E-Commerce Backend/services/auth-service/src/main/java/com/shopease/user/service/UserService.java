package com.shopease.user.service;

import com.shopease.common.wrapper.PageResponse;
import com.shopease.user.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface UserService {
    UpdateUserDetailsResponse updateCompleteUserDetails(@NotNull UUID id, @Valid UpdateUserDetailsRequest request);

    UpdateUserDetailsResponse patchUserDetails(@NotNull UUID id, @Valid UpdateUserDetailsRequest request);

    void confirmPasswordChange(@Valid UpdatePasswordRequest request);

    void requestPasswordChangeOtp(@Valid EmailRequest request);

    void verifyPasswordChangeOtp(@Valid VerifyPasswordRequest request);

    void confirmPasswordChange(@Valid PasswordRequest request);

    void requestEmailChangeOtp(@Valid EmailRequest request);

    void verifyEmailChangeOtp(@Valid NewEmailRequest request);


    PageResponse<UserResponse> getAllUsersWithRolesAndPermission(int page, int size, String keyword, String sortBy, String sortDirection);

    void updateRole(UUID userId, UUID roleId);
}
