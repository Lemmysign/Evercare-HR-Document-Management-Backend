package com.hrplatform.service.impl;

import com.hrplatform.dto.response.HrUserResponse;
import com.hrplatform.entity.HrUser;
import com.hrplatform.exception.ResourceNotFoundException;
import com.hrplatform.mapper.HrUserMapper;
import com.hrplatform.repository.HrUserRepository;
import com.hrplatform.service.HrUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrUserServiceImpl implements HrUserService {

    private final HrUserRepository hrUserRepository;
    private final HrUserMapper hrUserMapper;

    @Override
    @Transactional(readOnly = true)
    public HrUser findByEmail(String email) {
        return hrUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("HR user not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public HrUser findById(UUID id) {
        return hrUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HR user not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public HrUserResponse getHrUserById(UUID id) {
        HrUser hrUser = findById(id);
        return hrUserMapper.toResponse(hrUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HrUserResponse> getAllHrUsers() {
        return hrUserRepository.findAll().stream()
                .map(hrUserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HrUser save(HrUser hrUser) {
        HrUser savedUser = hrUserRepository.save(hrUser);
        log.info("Saved HR user: {}", hrUser.getEmail());
        return savedUser;
    }


    @Override
    @Transactional
    public void updateLastLogin(String email) {
        HrUser hrUser = findByEmail(email);
        hrUser.setLastLoginAt(LocalDateTime.now());
        hrUserRepository.save(hrUser);

        log.info("Updated last login time for HR user: {}", email);
    }

    @Override
    @Transactional
    public void markFirstLoginComplete(String email) {
        HrUser hrUser = findByEmail(email);
        hrUser.setIsFirstLogin(false);
        hrUserRepository.save(hrUser);

        log.info("Marked first login complete for HR user: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return hrUserRepository.existsByEmail(email);
    }
}