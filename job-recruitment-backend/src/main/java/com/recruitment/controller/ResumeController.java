package com.recruitment.controller;

import com.recruitment.dto.Result;
import com.recruitment.entity.Resume;
import com.recruitment.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/resume")
@PreAuthorize("hasRole('USER')")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    /**
     * 获取简历信息
     */
    @GetMapping("/info")
    public Result<Resume> getResumeInfo() {
        return resumeService.getResumeInfo();
    }

    /**
     * 保存简历
     */
    @PostMapping("/info")
    public Result<Boolean> saveResume(@RequestBody Resume resume) {
        return resumeService.saveResume(resume);
    }
}
