package com.recruitment.service;

import com.recruitment.dto.Result;
import com.recruitment.entity.Resume;

public interface ResumeService {
    Result<Resume> getResumeInfo();
    Result<Boolean> saveResume(Resume resume);
}
