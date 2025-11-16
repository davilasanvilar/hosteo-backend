package com.viladevcorp.hosteo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.forms.ActivitySearchForm;
import com.viladevcorp.hosteo.forms.CreateActivityForm;
import com.viladevcorp.hosteo.model.Activity;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.dto.ActivityDto;
import com.viladevcorp.hosteo.repository.ActivityRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class ActivityService {

    private ActivityRepository activityRepository;

    private UserRepository userRepository;

    @Autowired
    public ActivityService(ActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    public Activity createActivity(CreateActivityForm form) {
        User creator = userRepository.findByUsername(AuthUtils.getUsername());
        return activityRepository
                .save(new Activity(form.getName(), form.getDescription(), creator));
    }

    public Activity getActivityById(UUID id) {
        return activityRepository.findById(id).orElse(null);
    }

    public List<ActivityDto> findActivities(ActivitySearchForm form) {
        String activityName = form.getName() == null || form.getName().isEmpty() ? null
                : "%" + form.getName().toLowerCase() + "%";
        return activityRepository.advancedSearch(AuthUtils.getUsername(),
                activityName,
                PageRequest.of(form.getPage(), form.getPageSize()));
    }

    public PageMetadata getActivitiesMetadata(ActivitySearchForm form) {
        String activityName = form.getName() == null || form.getName().isEmpty() ? null
                : "%" + form.getName().toLowerCase() + "%";
        int totalRows = activityRepository.advancedCount(AuthUtils.getUsername(), activityName);
        int totalPages = ((Double) Math.ceil((double) totalRows / form.getPageSize())).intValue();
        return new PageMetadata(totalPages, totalRows);
    }

}
