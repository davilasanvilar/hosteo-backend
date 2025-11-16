package com.template.backtemplate.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.template.backtemplate.forms.ActivitySearchForm;
import com.template.backtemplate.forms.CreateActivityForm;
import com.template.backtemplate.model.Activity;
import com.template.backtemplate.model.PageMetadata;
import com.template.backtemplate.model.dto.ActivityDto;
import com.template.backtemplate.service.ActivityService;
import com.template.backtemplate.utils.ApiResponse;

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