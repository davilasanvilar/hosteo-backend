package com.viladevcorp.hosteo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viladevcorp.hosteo.forms.ActivitySearchForm;
import com.viladevcorp.hosteo.forms.CreateActivityForm;
import com.viladevcorp.hosteo.model.Activity;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.dto.ActivityDto;
import com.viladevcorp.hosteo.service.ActivityService;
import com.viladevcorp.hosteo.utils.ApiResponse;

@RestController
@RequestMapping("/api")
public class ActivityController {

    private final ActivityService activityService;

    @Autowired
    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/activity")
    public ResponseEntity<ApiResponse<ActivityDto>> createActivity(@RequestBody CreateActivityForm form) {
        Activity activity = activityService.createActivity(form);
        return ResponseEntity.ok().body(new ApiResponse<>(new ActivityDto(activity)));
    }

    @PostMapping("/activity/search")
    public ResponseEntity<ApiResponse<List<ActivityDto>>> searchActivity(@RequestBody ActivitySearchForm form) {
        List<ActivityDto> activityPage = activityService.findActivities(form);
        return ResponseEntity.ok().body(new ApiResponse<>(activityPage));
    }

    @PostMapping("/activity/search/metadata")
    public ResponseEntity<ApiResponse<PageMetadata>> getPageMetadata(@RequestBody ActivitySearchForm form) {
        PageMetadata pageMetadata = activityService.getActivitiesMetadata(form);
        return ResponseEntity.ok().body(new ApiResponse<>(pageMetadata));
    }

}