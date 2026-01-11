package com.hrplatform.service;

import com.hrplatform.exception.VirusScanException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Virus scanning service interface
 *
 * Allows for multiple implementations:
 * - ClamAVServiceImpl (current)
 * - WindowsDefenderServiceImpl (future)
 * - VirusTotalServiceImpl (future)
 */
public interface VirusScanService {

    /**
     * Scan file for viruses and malware
     *
     * @param file MultipartFile to scan
     * @throws VirusScanException if virus detected or scan fails
     */
    void scanFile(MultipartFile file) throws VirusScanException;

    /**
     * Check if virus scanning is enabled
     *
     * @return true if scanning is enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Test connection to virus scanner
     *
     * @return true if scanner is available, false otherwise
     */
    boolean testConnection();
}

/*
 * IMPLEMENTATION NOTES:
 *
 * This interface allows you to easily switch between different
 * antivirus implementations without changing FileStorageService.
 *
 * Current implementation: ClamAVServiceImpl
 *
 * Future implementations could include:
 * - Windows Defender integration
 * - VirusTotal API
 * - McAfee, Sophos, etc.
 *
 * To switch implementations, just change the @Primary annotation
 * or use @Qualifier in FileStorageServiceImpl
 */