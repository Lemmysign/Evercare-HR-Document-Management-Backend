package com.hrplatform.service;

import com.hrplatform.dto.response.HrUserResponse;
import com.hrplatform.entity.HrUser;

import java.util.List;
import java.util.UUID;

public interface HrUserService {

    HrUser findByEmail(String email);

    HrUser findById(UUID id);

    HrUserResponse getHrUserById(UUID id);

    List<HrUserResponse> getAllHrUsers();

    void updateLastLogin(String email);

    void markFirstLoginComplete(String email);

    boolean existsByEmail(String email);

    HrUser save(HrUser hrUser);


}