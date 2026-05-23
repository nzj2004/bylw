package com.recruitment.controller;

import com.recruitment.dto.*;
import com.recruitment.entity.Application;
import com.recruitment.entity.ApplicationStatusHistory;
import com.recruitment.entity.InterviewRound;
import com.recruitment.entity.Company;
import com.recruitment.entity.Job;
import com.recruitment.entity.Offer;
import com.recruitment.entity.Resume;
import com.recruitment.entity.UserNotification;
import com.recruitment.mapper.ApplicationMapper;
import com.recruitment.mapper.ApplicationStatusHistoryMapper;
import com.recruitment.mapper.CompanyMapper;
import com.recruitment.mapper.InterviewRoundMapper;
import com.recruitment.mapper.JobMapper;
import com.recruitment.mapper.OfferMapper;
import com.recruitment.mapper.ResumeMapper;
import com.recruitment.mapper.UserNotificationMapper;
import com.recruitment.service.ApplicationStatusHistoryService;
import com.recruitment.service.JobService;
import com.recruitment.service.OfferExpirationService;
import com.recruitment.service.UserNotificationService;
import com.recruitment.service.UserService;
import com.recruitment.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ApplicationStatusHistoryMapper statusHistoryMapper;

    @Autowired
    private InterviewRoundMapper interviewRoundMapper;

    @Autowired
    private OfferMapper offerMapper;

    @Autowired
    private UserNotificationMapper notificationMapper;

    @Autowired
    private JobService jobService;

    @Autowired
    private OfferExpirationService offerExpirationService;

    @Autowired
    private ApplicationStatusHistoryService statusHistoryService;

    @Autowired
    private UserNotificationService notificationService;

    @GetMapping("/info")
    public Result<UserDTO> getCurrentUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        return userService.getCurrentUserInfo(userId);
    }

    @GetMapping("/notifications")
    @PreAuthorize("hasAnyRole('USER', 'COMPANY', 'ADMIN', 'OPERATOR')")
    public Result<List<UserNotificationDTO>> getNotifications() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<UserNotificationDTO> dtos = notificationMapper.selectByUserId(userId).stream()
                .map(this::convertNotificationToDTO)
                .collect(Collectors.toList());
        return Result.success(dtos);
    }

    @GetMapping("/notifications/unread-count")
    @PreAuthorize("hasAnyRole('USER', 'COMPANY', 'ADMIN', 'OPERATOR')")
    public Result<Integer> getUnreadNotificationCount() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(notificationMapper.countUnread(userId));
    }

    @PutMapping("/notifications/{id}/read")
    @PreAuthorize("hasAnyRole('USER', 'COMPANY', 'ADMIN', 'OPERATOR')")
    public Result<Boolean> markNotificationRead(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationMapper.markRead(id, userId);
        return Result.success(true);
    }

    @PutMapping("/notifications/read-all")
    @PreAuthorize("hasAnyRole('USER', 'COMPANY', 'ADMIN', 'OPERATOR')")
    public Result<Boolean> markAllNotificationsRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationMapper.markAllRead(userId);
        return Result.success(true);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserDTO>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return userService.listUsers(keyword, role, status, current, size);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        return userService.updateUserStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> createUser(@RequestBody RegisterDTO registerDTO) {
        return userService.createUserByAdmin(registerDTO);
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> resetPassword(@PathVariable Long id) {
        return userService.resetPassword(id);
    }

    // ========== 企业信息相关 API ==========

    @GetMapping("/company/info")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<CompanyDTO> getCompanyInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        Company company = companyMapper.selectByUserId(userId);
        if (company == null) {
            // 如果没有企业信息，返回空的 DTO
            CompanyDTO dto = new CompanyDTO();
            dto.setUserId(userId);
            return Result.success(dto);
        }
        return Result.success(convertCompanyToDTO(company));
    }

    @PostMapping("/company/info")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Boolean> saveCompanyInfo(@RequestBody Company company) {
        Long userId = SecurityUtil.getCurrentUserId();
        Company existing = companyMapper.selectByUserId(userId);
        
        company.setUserId(userId);
        if (existing == null) {
            // 新增时默认为待审核状态
            company.setStatus(Company.STATUS_PENDING);
            companyMapper.insert(company);
        } else {
            savePendingCompanyInfo(existing.getId(), company);
        }
        return Result.success(true);
    }

    @PostMapping("/company/submit-audit")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Boolean> submitCompanyAudit() {
        Long userId = SecurityUtil.getCurrentUserId();
        Company existing = companyMapper.selectByUserId(userId);
        
        if (existing == null) {
            return Result.error("请先填写企业信息");
        }
        
        if (existing.getStatus() == Company.STATUS_APPROVED && !existing.hasPendingChanges()) {
            return Result.error("请先修改并保存企业信息，再提交审核");
        }
        
        // 更新为待审核状态
        Company company = new Company();
        company.setId(existing.getId());
        company.setStatus(Company.STATUS_PENDING);
        company.setRejectReason("");
        companyMapper.updateById(company);
        offlineCompanyJobs(existing.getId());
        
        return Result.success(true);
    }

    // ========== 投递简历相关 API ==========

    @PostMapping("/apply/{jobId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> applyJob(@PathVariable Long jobId) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 检查是否已投递
        int count = applicationMapper.countByJobAndUser(jobId, userId);
        if (count > 0) {
            return Result.error("您已投递过该职位");
        }

        // 获取用户简历
        Resume resume = resumeMapper.selectDefaultByUserId(userId);
        if (resume == null) {
            // 尝试获取任意简历
            List<Resume> resumes = resumeMapper.selectByUserId(userId);
            if (resumes == null || resumes.isEmpty()) {
                return Result.error("请先完善简历信息");
            }
            resume = resumes.get(0);
        }

        // 获取职位信息
        Result<JobDTO> jobResult = jobService.getJobById(jobId);
        if (jobResult.getCode() != 200 || jobResult.getData() == null) {
            return Result.error("职位不存在");
        }
        JobDTO job = jobResult.getData();

        // 创建投递记录
        Application application = new Application();
        application.setJobId(jobId);
        application.setResumeId(resume.getId());
        application.setUserId(userId);
        application.setCompanyId(job.getCompanyId());
        application.setStatus(Application.STATUS_PENDING);
        application.setApplyTime(LocalDateTime.now());
        applicationMapper.insert(application);
        statusHistoryService.recordStatusChange(
                application.getId(),
                null,
                Application.STATUS_PENDING,
                "APPLY_CREATED",
                "求职者投递职位");

        // 增加职位投递数
        jobService.incrementApplyCount(jobId);

        return Result.success(true);
    }

    @GetMapping("/applications")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR', 'COMPANY')")
    public Result<List<ApplicationDTO>> getUserApplications() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<Application> applications = applicationMapper.selectByUserId(userId);
        List<ApplicationDTO> dtos = applications.stream()
                .map(this::convertApplicationToDTO)
                .collect(Collectors.toList());
        return Result.success(dtos);
    }

    /**
     * 检查用户是否已投递某个职位
     */
    @GetMapping("/apply/{jobId}/status")
    public Result<Boolean> checkApplyStatus(@PathVariable Long jobId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.success(false);
        }
        int count = applicationMapper.countByJobAndUser(jobId, userId);
        return Result.success(count > 0);
    }

    @GetMapping("/company/applications")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<PageResult<ApplicationDTO>> getCompanyApplications(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Company company = getCurrentCompany();
        if (company == null) {
            return Result.error("先完成企业信息后再查看申请列表");
        }
        Page<Application> page = new Page<>(current, size);
        List<String> categoryValues = com.recruitment.utils.JobCategoryResolver.resolveCategoryValues(category);
        IPage<Application> result = applicationMapper.selectPageByCompanyId(page, company.getId(), keyword, categoryValues, status);
        List<ApplicationDTO> dtos = result.getRecords().stream()
                .map(this::convertApplicationToDTO)
                .collect(Collectors.toList());
        return Result.success(PageResult.of(result.getTotal(), current, size, dtos));
    }

    @PutMapping("/company/application/{id}/status")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Boolean> updateApplicationStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (!isManualApplicationStatus(status)) {
            return Result.error("状态值仅支持1(已查看)、2(感兴趣)、3(不合适)");
        }
        Application application = getCompanyApplication(id);
        if (application == null) {
            return Result.error("无权处理该申请");
        }
        if (isFinalApplicationStatus(application.getStatus())) {
            return Result.error("最终状态不可再修改");
        }
        if (!isScreeningApplicationStatus(application.getStatus())) {
            return Result.error("当前阶段不能使用初筛状态修改");
        }
        Application update = new Application();
        changeApplicationStatus(application, update, status, "SCREENING_UPDATE", "企业初筛处理");
        return Result.success(true);
    }

    @GetMapping("/resume/{resumeId}")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<ResumeDTO> getResumeById(@PathVariable Long resumeId, @RequestParam(required = false) Long applicationId) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            return Result.error("简历不存在");
        }
        if (!markApplicationViewed(applicationId, resumeId)) {
            return Result.error("当前企业无权查看该简历");
        }
        return Result.success(convertResumeToDTO(resume));
    }

    @GetMapping("/company/application/{applicationId}/interviews")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<List<InterviewRoundDTO>> getCompanyApplicationInterviews(@PathVariable Long applicationId) {
        Application application = getCompanyApplication(applicationId);
        if (application == null) {
            return Result.error("无权查看该申请");
        }
        List<InterviewRound> rounds = interviewRoundMapper.selectByApplicationId(applicationId);
        List<InterviewRoundDTO> dtos = rounds.stream()
                .map(this::convertInterviewRoundToDTO)
                .collect(Collectors.toList());
        return Result.success(dtos);
    }

    @GetMapping("/user/application/{applicationId}/interviews")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR', 'COMPANY')")
    public Result<List<InterviewRoundDTO>> getUserApplicationInterviews(@PathVariable Long applicationId) {
        Application application = getUserApplication(applicationId);
        if (application == null) {
            return Result.error("无权查看该申请");
        }
        List<InterviewRound> rounds = interviewRoundMapper.selectByApplicationId(applicationId);
        List<InterviewRoundDTO> dtos = rounds.stream()
                .map(this::convertInterviewRoundToDTO)
                .collect(Collectors.toList());
        return Result.success(dtos);
    }

    @GetMapping("/company/application/{applicationId}/status-history")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<List<ApplicationStatusHistoryDTO>> getCompanyApplicationStatusHistory(@PathVariable Long applicationId) {
        Application application = getCompanyApplication(applicationId);
        if (application == null) {
            return Result.error("无权查看该申请");
        }
        return Result.success(getApplicationStatusHistoryDtos(applicationId));
    }

    @GetMapping("/application/{applicationId}/status-history")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR', 'COMPANY')")
    public Result<List<ApplicationStatusHistoryDTO>> getUserApplicationStatusHistory(@PathVariable Long applicationId) {
        Application application = getUserApplication(applicationId);
        if (application == null) {
            return Result.error("无权查看该申请");
        }
        return Result.success(getApplicationStatusHistoryDtos(applicationId));
    }

    @PutMapping("/interview-round/{roundId}/confirm")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> confirmInterviewRound(@PathVariable Long roundId) {
        InterviewRound round = interviewRoundMapper.selectByIdIncludeDeleted(roundId);
        if (round == null) {
            return Result.error("面试记录不存在");
        }
        Application application = getUserApplication(round.getApplicationId());
        if (application == null) {
            return Result.error("无权处理该面试");
        }
        if (!Objects.equals(round.getResult(), InterviewRound.RESULT_PENDING)) {
            return Result.error("该面试已结束，不能确认");
        }
        interviewRoundMapper.updateConfirmation(roundId, InterviewRound.CONFIRMATION_CONFIRMED);
        notifyCompany(application, "候选人已确认面试",
                buildApplicationTitle(application) + " 的候选人已确认第 " + round.getRoundNo() + " 轮面试",
                "INTERVIEW", roundId);
        return Result.success(true);
    }

    @PutMapping("/interview-round/{roundId}/reschedule")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> requestInterviewReschedule(@PathVariable Long roundId,
                                                      @RequestBody InterviewRescheduleRequest request) {
        InterviewRound round = interviewRoundMapper.selectByIdIncludeDeleted(roundId);
        if (round == null) {
            return Result.error("面试记录不存在");
        }
        Application application = getUserApplication(round.getApplicationId());
        if (application == null) {
            return Result.error("无权处理该面试");
        }
        if (!Objects.equals(round.getResult(), InterviewRound.RESULT_PENDING)) {
            return Result.error("该面试已结束，不能申请改期");
        }
        LocalDateTime rescheduleTime = request == null ? null : parseDateTime(request.getRescheduleTime());
        if (rescheduleTime == null) {
            return Result.error("请填写期望改期时间");
        }
        if (rescheduleTime.isBefore(LocalDateTime.now())) {
            return Result.error("期望改期时间不能早于当前时间");
        }
        String reason = request.getRescheduleReason();
        interviewRoundMapper.requestReschedule(roundId, InterviewRound.CONFIRMATION_RESCHEDULE_REQUESTED, rescheduleTime, reason);
        notifyCompany(application, "候选人申请面试改期",
                buildApplicationTitle(application) + " 的候选人申请第 " + round.getRoundNo() + " 轮面试改期至 "
                        + rescheduleTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "INTERVIEW", roundId);
        return Result.success(true);
    }

    @PutMapping("/company/interview-round/{roundId}/reschedule/accept")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Boolean> acceptInterviewReschedule(@PathVariable Long roundId) {
        InterviewRound round = interviewRoundMapper.selectByIdIncludeDeleted(roundId);
        if (round == null) {
            return Result.error("面试记录不存在");
        }
        Application application = getCompanyApplication(round.getApplicationId());
        if (application == null) {
            return Result.error("无权处理该面试");
        }
        if (!Objects.equals(round.getConfirmationStatus(), InterviewRound.CONFIRMATION_RESCHEDULE_REQUESTED)
                || round.getRescheduleTime() == null) {
            return Result.error("当前面试没有待处理的改期申请");
        }
        interviewRoundMapper.acceptReschedule(roundId, round.getRescheduleTime(), InterviewRound.CONFIRMATION_PENDING);
        notificationService.notify(application.getUserId(), "面试改期已通过",
                buildApplicationTitle(application) + " 的第 " + round.getRoundNo() + " 轮面试已改期至 "
                        + round.getRescheduleTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "INTERVIEW", "INTERVIEW", roundId);
        return Result.success(true);
    }

    @PostMapping("/company/application/{applicationId}/interviews")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<InterviewRoundDTO> createInterviewRound(@PathVariable Long applicationId,
                                                         @RequestBody InterviewRoundDTO dto) {
        Application application = getCompanyApplication(applicationId);
        if (application == null) {
            return Result.error("无权操作该申请");
        }
        if (isClosedApplicationStatus(application.getStatus())) {
            return Result.error("该申请已结束，不能安排面试");
        }
        List<InterviewRound> existingRounds = interviewRoundMapper.selectByApplicationId(applicationId);
        InterviewRound interviewRound = new InterviewRound();
        interviewRound.setApplicationId(applicationId);
        interviewRound.setRoundNo(dto.getRoundNo() != null ? dto.getRoundNo() : existingRounds.size() + 1);
        interviewRound.setInterviewType(dto.getInterviewType());
        interviewRound.setInterviewTime(parseDateTime(dto.getInterviewTime()));
        interviewRound.setLocation(dto.getLocation());
        interviewRound.setMeetingLink(dto.getMeetingLink());
        interviewRound.setContactPerson(dto.getContactPerson());
        interviewRound.setContactPhone(dto.getContactPhone());
        interviewRound.setResult(InterviewRound.RESULT_PENDING);
        interviewRound.setConfirmationStatus(InterviewRound.CONFIRMATION_PENDING);
        interviewRound.setNotes(dto.getNotes());
        interviewRoundMapper.insert(interviewRound);
        notificationService.notify(application.getUserId(), "新的面试安排",
                buildApplicationTitle(application) + " 已安排第 " + interviewRound.getRoundNo() + " 轮面试，请确认或申请改期",
                "INTERVIEW", "INTERVIEW", interviewRound.getId());

        if (!isFinalApplicationStatus(application.getStatus())) {
            Application update = new Application();
            changeApplicationStatus(application, update, Application.STATUS_INTERVIEWING, "INTERVIEW_CREATED", "企业安排面试");
        }

        return Result.success(convertInterviewRoundToDTO(interviewRound));
    }

    @PutMapping("/company/interview-round/{roundId}/result")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Boolean> updateInterviewRoundResult(@PathVariable Long roundId,
                                                     @RequestBody InterviewResultRequest request) {
        if (request == null || request.getResult() == null) {
            return Result.error("结果不能为空");
        }
        if (!isValidInterviewResult(request.getResult())) {
            return Result.error("面试结果值仅支持0(待定)、1(通过)、2(未通过)、3(取消)");
        }
        InterviewRound round = interviewRoundMapper.selectByIdIncludeDeleted(roundId);
        if (round == null) {
            return Result.error("面试记录不存在");
        }
        Application application = getCompanyApplication(round.getApplicationId());
        if (application == null) {
            return Result.error("无权修改该面试记录");
        }

        InterviewRound update = new InterviewRound();
        update.setId(roundId);
        update.setResult(request.getResult());
        update.setNotes(request.getNotes());
        interviewRoundMapper.updateById(update);

        if (!Objects.equals(request.getResult(), InterviewRound.RESULT_CANCELLED)) {
            Application applicationUpdate = new Application();
            applicationUpdate.setId(round.getApplicationId());
            applicationUpdate.setHandleTime(LocalDateTime.now());
            if (Objects.equals(request.getResult(), InterviewRound.RESULT_PASS)) {
                if (round.getRoundNo() != null && round.getRoundNo() >= 2) {
                    applicationUpdate.setStatus(Application.STATUS_INTERVIEW_PASSED);
                } else {
                    applicationUpdate.setStatus(Application.STATUS_INTERVIEWING);
                }
            } else if (Objects.equals(request.getResult(), InterviewRound.RESULT_FAIL)) {
                applicationUpdate.setStatus(Application.STATUS_INTERVIEW_FAILED);
            }
            if (applicationUpdate.getStatus() != null) {
                changeApplicationStatus(application, applicationUpdate, applicationUpdate.getStatus(), "INTERVIEW_RESULT", "企业更新面试结果");
            }
        }
        return Result.success(true);
    }

    @PostMapping("/company/application/{applicationId}/offer")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<OfferDTO> createOrUpdateOffer(@PathVariable Long applicationId, @RequestBody OfferDTO dto) {
        Application application = getCompanyApplication(applicationId);
        if (application == null) {
            return Result.error("无权管理该申请");
        }
        if (dto == null) {
            return Result.error("参数不能为空");
        }
        if (isClosedApplicationStatus(application.getStatus())) {
            return Result.error("该申请已结束，不能发送Offer");
        }
        Offer offer = offerMapper.selectLatestByApplicationId(applicationId);
        if (offer != null && !Objects.equals(offer.getStatus(), Offer.STATUS_PENDING)) {
            return Result.error("Offer已被响应或过期，不能修改");
        }
        LocalDateTime expireTime = parseDateTime(dto.getExpireTime());
        if (expireTime != null && expireTime.isBefore(LocalDateTime.now())) {
            return Result.error("Offer失效时间不能早于当前时间");
        }
        if (offer == null) {
            offer = new Offer();
        }
        offer.setApplicationId(applicationId);
        offer.setStatus(Offer.STATUS_PENDING);
        offer.setOfferTitle(dto.getOfferTitle());
        offer.setSalaryMin(dto.getSalaryMin());
        offer.setSalaryMax(dto.getSalaryMax());
        offer.setBenefits(dto.getBenefits());
        offer.setWorkLocation(dto.getWorkLocation());
        offer.setOfferContent(dto.getOfferContent());
        offer.setEntryDate(parseDate(dto.getEntryDate()));
        offer.setExpireTime(expireTime);
        offer.setResponseTime(null);
        offer.setResponseComment(null);

        if (offer.getId() == null) {
            offerMapper.insert(offer);
        } else {
            offerMapper.updateById(offer);
        }

        if (!Objects.equals(application.getStatus(), Application.STATUS_OFFER_ACCEPTED)
                && !Objects.equals(application.getStatus(), Application.STATUS_OFFER_REJECTED)
                && !Objects.equals(application.getStatus(), Application.STATUS_ONBOARDED)) {
            Application applicationUpdate = new Application();
            changeApplicationStatus(application, applicationUpdate, Application.STATUS_OFFER_PENDING, "OFFER_SENT", "企业发送Offer");
        }
        return Result.success(convertOfferToDTO(offer));
    }

    @GetMapping("/company/application/{applicationId}/offer")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<OfferDTO> getCompanyApplicationOffer(@PathVariable Long applicationId) {
        Application application = getCompanyApplication(applicationId);
        if (application == null) {
            return Result.error("无权查看该申请");
        }
        offerExpirationService.expirePendingOffers();
        Offer offer = offerMapper.selectLatestByApplicationId(applicationId);
        if (offer == null) {
            return Result.error("未找到Offer");
        }
        return Result.success(convertOfferToDTO(offer));
    }

    @GetMapping({"/offers", "/user/offers"})
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR', 'COMPANY')")
    public Result<List<OfferDTO>> getUserOffers() {
        Long userId = SecurityUtil.getCurrentUserId();
        offerExpirationService.expirePendingOffers();
        List<Offer> offers = offerMapper.selectByUserIdWithMeta(userId);
        List<OfferDTO> dtos = offers.stream()
                .map(this::convertOfferToDTO)
                .collect(Collectors.toList());
        return Result.success(dtos);
    }

    @PutMapping({"/offer/{offerId}/response", "/user/offer/{offerId}/response"})
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR', 'COMPANY')")
    public Result<Boolean> respondOffer(@PathVariable Long offerId, @RequestBody OfferResponseRequest request) {
        if (request == null || request.getStatus() == null) {
            return Result.error("响应状态不能为空");
        }
        Offer offer = offerMapper.selectByIdAndNotDeleted(offerId);
        if (offer == null) {
            return Result.error("Offer不存在");
        }
        Long userId = SecurityUtil.getCurrentUserId();
        Application application = applicationMapper.selectByIdAndUserId(offer.getApplicationId(), userId);
        if (application == null) {
            return Result.error("无权响应该Offer");
        }
        if (Objects.equals(offer.getStatus(), Offer.STATUS_EXPIRED)) {
            return Result.error("Offer已过期，无法响应");
        }
        if (!Objects.equals(offer.getStatus(), Offer.STATUS_PENDING)) {
            return Result.error("Offer状态不可再次响应");
        }
        if (offer.getExpireTime() != null && offer.getExpireTime().isBefore(LocalDateTime.now())) {
            Offer offerUpdate = new Offer();
            offerUpdate.setId(offerId);
            offerUpdate.setStatus(Offer.STATUS_EXPIRED);
            offerMapper.updateById(offerUpdate);
            Application applicationUpdate = new Application();
            changeApplicationStatus(application, applicationUpdate, Application.STATUS_OFFER_EXPIRED, "OFFER_EXPIRED", "Offer超过有效期");
            return Result.error("Offer已过期，无法响应");
        }
        if (!Objects.equals(request.getStatus(), Offer.STATUS_ACCEPTED)
                && !Objects.equals(request.getStatus(), Offer.STATUS_REJECTED)) {
            return Result.error("状态值仅支持1(接受)或2(拒绝)");
        }

        Offer offerUpdate = new Offer();
        offerUpdate.setId(offerId);
        offerUpdate.setStatus(request.getStatus());
        offerUpdate.setResponseTime(LocalDateTime.now());
        offerUpdate.setResponseComment(request.getResponseComment());
        offerMapper.updateById(offerUpdate);

        Application applicationUpdate = new Application();
        Integer newStatus = null;
        String action = null;
        String remark = null;
        if (Objects.equals(request.getStatus(), Offer.STATUS_ACCEPTED)) {
            newStatus = Application.STATUS_OFFER_ACCEPTED;
            action = "OFFER_ACCEPTED";
            remark = "求职者接受Offer";
        } else if (Objects.equals(request.getStatus(), Offer.STATUS_REJECTED)) {
            newStatus = Application.STATUS_OFFER_REJECTED;
            action = "OFFER_REJECTED";
            remark = "求职者拒绝Offer";
        }
        changeApplicationStatus(application, applicationUpdate, newStatus, action, remark);
        notifyCompany(application, remark, buildApplicationTitle(application) + "：" + remark, "OFFER", offerId);
        return Result.success(true);
    }

    @PutMapping("/company/application/{applicationId}/onboard")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Boolean> confirmOnboard(@PathVariable Long applicationId) {
        Application application = getCompanyApplication(applicationId);
        if (application == null) {
            return Result.error("无权处理该申请");
        }
        if (!Objects.equals(application.getStatus(), Application.STATUS_OFFER_ACCEPTED)) {
            return Result.error("仅Offer已接受的申请可以确认入职");
        }
        Application update = new Application();
        changeApplicationStatus(application, update, Application.STATUS_ONBOARDED, "ONBOARDED", "企业确认已入职");
        return Result.success(true);
    }

    private boolean isFinalApplicationStatus(Integer status) {
        return Objects.equals(status, Application.STATUS_OFFER_ACCEPTED)
                || Objects.equals(status, Application.STATUS_OFFER_REJECTED)
                || Objects.equals(status, Application.STATUS_OFFER_EXPIRED)
                || Objects.equals(status, Application.STATUS_ONBOARDED);
    }

    private boolean isClosedApplicationStatus(Integer status) {
        return Objects.equals(status, Application.STATUS_UNSUITABLE)
                || Objects.equals(status, Application.STATUS_INTERVIEW_FAILED)
                || Objects.equals(status, Application.STATUS_OFFER_ACCEPTED)
                || Objects.equals(status, Application.STATUS_OFFER_REJECTED)
                || Objects.equals(status, Application.STATUS_OFFER_EXPIRED)
                || Objects.equals(status, Application.STATUS_ONBOARDED);
    }

    private boolean isManualApplicationStatus(Integer status) {
        return Objects.equals(status, Application.STATUS_VIEWED)
                || Objects.equals(status, Application.STATUS_INTERESTED)
                || Objects.equals(status, Application.STATUS_UNSUITABLE);
    }

    private boolean isScreeningApplicationStatus(Integer status) {
        return Objects.equals(status, Application.STATUS_PENDING)
                || Objects.equals(status, Application.STATUS_VIEWED)
                || Objects.equals(status, Application.STATUS_INTERESTED);
    }

    private boolean isValidInterviewResult(Integer result) {
        return Objects.equals(result, InterviewRound.RESULT_PENDING)
                || Objects.equals(result, InterviewRound.RESULT_PASS)
                || Objects.equals(result, InterviewRound.RESULT_FAIL)
                || Objects.equals(result, InterviewRound.RESULT_CANCELLED);
    }

    private ResumeDTO convertResumeToDTO(Resume resume) {
        ResumeDTO dto = new ResumeDTO();
        BeanUtils.copyProperties(resume, dto);
        dto.setGenderName(resume.getGenderName());
        if (resume.getBirthDate() != null) {
            dto.setBirthDate(resume.getBirthDate().toString());
        }
        if (resume.getCreateTime() != null) {
            dto.setCreateTime(resume.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }

    private CompanyDTO convertCompanyToDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        BeanUtils.copyProperties(company, dto);
        dto.setHasPendingChanges(company.hasPendingChanges());
        if (company.hasPendingChanges()) {
            applyPendingDisplay(company, dto);
        }
        dto.setStatusName(company.getStatusName());
        if (company.getCreateTime() != null) {
            dto.setCreateTime(company.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }

    private void savePendingCompanyInfo(Long companyId, Company company) {
        LambdaUpdateWrapper<Company> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Company::getId, companyId)
                .set(Company::getPendingCompanyName, company.getCompanyName())
                .set(Company::getPendingIndustry, company.getIndustry())
                .set(Company::getPendingScale, company.getScale())
                .set(Company::getPendingAddress, company.getAddress())
                .set(Company::getPendingDescription, company.getDescription())
                .set(Company::getPendingLogoUrl, company.getLogoUrl())
                .set(Company::getPendingWebsite, company.getWebsite())
                .set(Company::getPendingContactName, company.getContactName())
                .set(Company::getPendingContactPhone, company.getContactPhone())
                .set(Company::getPendingContactEmail, company.getContactEmail());
        companyMapper.update(null, wrapper);
    }

    private void applyPendingDisplay(Company company, CompanyDTO dto) {
        dto.setCompanyName(company.getPendingCompanyName());
        dto.setIndustry(company.getPendingIndustry());
        dto.setScale(company.getPendingScale());
        dto.setAddress(company.getPendingAddress());
        dto.setDescription(company.getPendingDescription());
        dto.setLogoUrl(company.getPendingLogoUrl());
        dto.setWebsite(company.getPendingWebsite());
        dto.setContactName(company.getPendingContactName());
        dto.setContactPhone(company.getPendingContactPhone());
        dto.setContactEmail(company.getPendingContactEmail());
    }

    private void offlineCompanyJobs(Long companyId) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getCompanyId, companyId);
        wrapper.ne(Job::getStatus, Job.STATUS_OFFLINE);
        Job update = new Job();
        update.setStatus(Job.STATUS_OFFLINE);
        jobMapper.update(update, wrapper);
    }

    private ApplicationDTO convertApplicationToDTO(Application application) {
        ApplicationDTO dto = new ApplicationDTO();
        BeanUtils.copyProperties(application, dto);
        dto.setStatusName(application.getStatusName());
        if (application.getApplyTime() != null) {
            dto.setApplyTime(application.getApplyTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (application.getHandleTime() != null) {
            dto.setHandleTime(application.getHandleTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (application.getViewedAt() != null) {
            dto.setViewedAt(application.getViewedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        dto.setViewedBy(application.getViewedBy());
        return dto;
    }

    private boolean markApplicationViewed(Long applicationId, Long resumeId) {
        Company company = getCurrentCompany();
        if (company == null) {
            return false;
        }
        Long userId = SecurityUtil.getCurrentUserId();
        Application application;
        if (applicationId != null) {
            application = applicationMapper.selectByIdAndCompanyId(applicationId, company.getId());
        } else {
            application = applicationMapper.selectLatestByResumeAndCompany(resumeId, company.getId());
        }
        if (application == null) {
            return false;
        }
        if (!Objects.equals(application.getResumeId(), resumeId)) {
            return false;
        }
        if (!Objects.equals(application.getStatus(), Application.STATUS_PENDING)) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        Application update = new Application();
        update.setId(application.getId());
        update.setStatus(Application.STATUS_VIEWED);
        update.setHandleTime(now);
        update.setViewedAt(now);
        update.setViewedBy(userId);
        changeApplicationStatus(application, update, Application.STATUS_VIEWED, "RESUME_VIEWED", "企业查看简历");
        return true;
    }

    private void changeApplicationStatus(Application application, Application update, Integer newStatus, String action, String remark) {
        if (application == null || update == null || newStatus == null) {
            return;
        }
        update.setId(application.getId());
        update.setStatus(newStatus);
        if (update.getHandleTime() == null) {
            update.setHandleTime(LocalDateTime.now());
        }
        applicationMapper.updateById(update);
        statusHistoryService.recordStatusChange(application.getId(), application.getStatus(), newStatus, action, remark);
        notifyCandidateForStatusChange(application, newStatus, action, remark);
    }

    private void notifyCandidateForStatusChange(Application application, Integer newStatus, String action, String remark) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (application == null || application.getUserId() == null || Objects.equals(currentUserId, application.getUserId())) {
            return;
        }
        notificationService.notify(application.getUserId(),
                getApplicationStatusName(newStatus),
                buildApplicationTitle(application) + " 状态更新为：" + getApplicationStatusName(newStatus)
                        + (remark == null || remark.isBlank() ? "" : "。" + remark),
                "APPLICATION", "APPLICATION", application.getId());
    }

    private void notifyCompany(Application application, String title, String content, String notificationType, Long businessId) {
        if (application == null || application.getCompanyId() == null) {
            return;
        }
        Company company = companyMapper.selectById(application.getCompanyId());
        if (company == null) {
            return;
        }
        String businessType = "INTERVIEW".equals(notificationType) ? "INTERVIEW"
                : ("OFFER".equals(notificationType) ? "OFFER" : "APPLICATION");
        notificationService.notify(company.getUserId(), title, content, notificationType, businessType, businessId);
    }

    private String buildApplicationTitle(Application application) {
        if (application == null) {
            return "投递";
        }
        if (application.getJobTitle() != null && !application.getJobTitle().isBlank()) {
            return "《" + application.getJobTitle() + "》";
        }
        if (application.getJobId() != null) {
            Result<JobDTO> jobResult = jobService.getJobById(application.getJobId());
            if (jobResult.getCode() == 200 && jobResult.getData() != null && jobResult.getData().getTitle() != null) {
                return "《" + jobResult.getData().getTitle() + "》";
            }
        }
        return "投递";
    }

    private Company getCurrentCompany() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return companyMapper.selectByUserId(userId);
    }

    private Application getCompanyApplication(Long applicationId) {
        Company company = getCurrentCompany();
        if (company == null) {
            return null;
        }
        return applicationMapper.selectByIdAndCompanyId(applicationId, company.getId());
    }

    private Application getUserApplication(Long applicationId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return applicationMapper.selectByIdAndUserId(applicationId, userId);
    }

    private List<ApplicationStatusHistoryDTO> getApplicationStatusHistoryDtos(Long applicationId) {
        return statusHistoryMapper.selectByApplicationId(applicationId).stream()
                .map(this::convertStatusHistoryToDTO)
                .collect(Collectors.toList());
    }

    private ApplicationStatusHistoryDTO convertStatusHistoryToDTO(ApplicationStatusHistory history) {
        ApplicationStatusHistoryDTO dto = new ApplicationStatusHistoryDTO();
        dto.setId(history.getId());
        dto.setApplicationId(history.getApplicationId());
        dto.setOldStatus(history.getOldStatus());
        dto.setOldStatusName(getApplicationStatusName(history.getOldStatus()));
        dto.setNewStatus(history.getNewStatus());
        dto.setNewStatusName(getApplicationStatusName(history.getNewStatus()));
        dto.setChangedBy(history.getChangedBy());
        dto.setChangedRole(history.getChangedRole());
        dto.setAction(history.getAction());
        dto.setActionName(getApplicationStatusActionName(history.getAction()));
        dto.setRemark(history.getRemark());
        if (history.getCreateTime() != null) {
            dto.setCreateTime(history.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }

    private String getApplicationStatusName(Integer status) {
        if (status == null) {
            return "无";
        }
        Application application = new Application();
        application.setStatus(status);
        return application.getStatusName();
    }

    private String getApplicationStatusActionName(String action) {
        if (action == null) {
            return "状态更新";
        }
        return switch (action) {
            case "APPLY_CREATED" -> "投递职位";
            case "RESUME_VIEWED" -> "查看简历";
            case "SCREENING_UPDATE" -> "初筛处理";
            case "INTERVIEW_CREATED" -> "安排面试";
            case "INTERVIEW_RESULT" -> "面试结果";
            case "OFFER_SENT" -> "发送Offer";
            case "OFFER_ACCEPTED" -> "接受Offer";
            case "OFFER_REJECTED" -> "拒绝Offer";
            case "OFFER_EXPIRED" -> "Offer过期";
            case "ONBOARDED" -> "确认入职";
            case "HISTORY_BACKFILL" -> "历史初始化";
            default -> "状态更新";
        };
    }

    private InterviewRoundDTO convertInterviewRoundToDTO(InterviewRound interviewRound) {
        InterviewRoundDTO dto = new InterviewRoundDTO();
        dto.setId(interviewRound.getId());
        dto.setApplicationId(interviewRound.getApplicationId());
        dto.setRoundNo(interviewRound.getRoundNo());
        dto.setInterviewType(interviewRound.getInterviewType());
        if (interviewRound.getInterviewTime() != null) {
            dto.setInterviewTime(interviewRound.getInterviewTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        dto.setLocation(interviewRound.getLocation());
        dto.setMeetingLink(interviewRound.getMeetingLink());
        dto.setContactPerson(interviewRound.getContactPerson());
        dto.setContactPhone(interviewRound.getContactPhone());
        dto.setResult(interviewRound.getResult());
        dto.setResultName(interviewRound.getResultName());
        dto.setNotes(interviewRound.getNotes());
        dto.setConfirmationStatus(interviewRound.getConfirmationStatus() == null
                ? InterviewRound.CONFIRMATION_PENDING
                : interviewRound.getConfirmationStatus());
        dto.setConfirmationStatusName(interviewRound.getConfirmationStatusName());
        if (interviewRound.getCandidateResponseTime() != null) {
            dto.setCandidateResponseTime(interviewRound.getCandidateResponseTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (interviewRound.getRescheduleTime() != null) {
            dto.setRescheduleTime(interviewRound.getRescheduleTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        dto.setRescheduleReason(interviewRound.getRescheduleReason());
        if (interviewRound.getCreateTime() != null) {
            dto.setCreateTime(interviewRound.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (interviewRound.getUpdateTime() != null) {
            dto.setUpdateTime(interviewRound.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }

    private OfferDTO convertOfferToDTO(Offer offer) {
        OfferDTO dto = new OfferDTO();
        dto.setId(offer.getId());
        dto.setApplicationId(offer.getApplicationId());
        dto.setJobTitle(offer.getJobTitle());
        dto.setCompanyName(offer.getCompanyName());
        dto.setOfferTitle(offer.getOfferTitle());
        dto.setStatus(offer.getStatus());
        dto.setStatusName(offer.getStatusName());
        dto.setSalaryMin(offer.getSalaryMin());
        dto.setSalaryMax(offer.getSalaryMax());
        dto.setBenefits(offer.getBenefits());
        dto.setWorkLocation(offer.getWorkLocation());
        dto.setOfferContent(offer.getOfferContent());
        if (offer.getEntryDate() != null) {
            dto.setEntryDate(offer.getEntryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (offer.getExpireTime() != null) {
            dto.setExpireTime(offer.getExpireTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (offer.getResponseTime() != null) {
            dto.setResponseTime(offer.getResponseTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (offer.getCreateTime() != null) {
            dto.setCreateTime(offer.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (offer.getUpdateTime() != null) {
            dto.setUpdateTime(offer.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        dto.setResponseComment(offer.getResponseComment());
        return dto;
    }

    private UserNotificationDTO convertNotificationToDTO(UserNotification notification) {
        UserNotificationDTO dto = new UserNotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setNotificationType(notification.getNotificationType());
        dto.setBusinessType(notification.getBusinessType());
        dto.setBusinessId(notification.getBusinessId());
        dto.setReadStatus(notification.getReadStatus());
        if (notification.getReadTime() != null) {
            dto.setReadTime(notification.getReadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (notification.getCreateTime() != null) {
            dto.setCreateTime(notification.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
