package com.hrplatform.service.impl;

import com.hrplatform.entity.DocumentSubmission;
import com.hrplatform.repository.DocumentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentPersistenceService {

    private final DocumentSubmissionRepository documentSubmissionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DocumentSubmission saveInNewTransaction(DocumentSubmission submission) {
        return documentSubmissionRepository.save(submission);
    }
}