package com.viladevcorp.hosteo.service;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Template;
import com.viladevcorp.hosteo.model.forms.TemplateCreateForm;
import com.viladevcorp.hosteo.model.forms.TemplateSearchForm;
import com.viladevcorp.hosteo.model.forms.TemplateUpdateForm;
import com.viladevcorp.hosteo.repository.TemplateRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TemplateService {

    private TemplateRepository templateRepository;

    @Autowired
    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public Template createTemplate(TemplateCreateForm form) {
        Template template = Template.builder()
                .name(form.getName())
                .category(form.getCategory())
                .duration(form.getDuration())
                .prepTask(form.isPrepTask())
                .steps(form.getSteps())
                .build();

        return templateRepository.save(template);
    }

    public Template updateTemplate(TemplateUpdateForm form)
            throws InstanceNotFoundException, NotAllowedResourceException {
        Template template = getTemplateById(form.getId());

        template.setName(form.getName());
        template.setCategory(form.getCategory());
        template.setDuration(form.getDuration());
        template.setPrepTask(form.isPrepTask());
        template.setSteps(form.getSteps());

        return templateRepository.save(template);
    }

    public Template getTemplateById(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[TemplateService.getTemplateById] - Template not found with id: {}", id);
                    return new InstanceNotFoundException("Template not found with id: " + id);
                });
        try {
            AuthUtils.checkIfCreator(template, "template");
        } catch (NotAllowedResourceException e) {
            log.error("[TemplateService.getTemplateById] - Not allowed to access template with id: {}", id);
            throw e;
        }
        return template;
    }

    public List<Template> findTemplates(TemplateSearchForm form) {
        String name = form.getName() == null || form.getName().isEmpty() ? null
                : "%" + form.getName().toLowerCase() + "%";
        PageRequest pageRequest = null;
        if (form.getPageSize() > 0) {
            int pageNumber = form.getPageNumber() <= 0 ? 0 : form.getPageNumber();
            pageRequest = PageRequest.of(pageNumber, form.getPageSize());
        }
        return templateRepository.advancedSearch(
                AuthUtils.getUsername(),
                name,
                pageRequest);
    }

    public PageMetadata getTemplatesMetadata(TemplateSearchForm form) {
        String name = form.getName() == null || form.getName().isEmpty() ? null
                : "%" + form.getName().toLowerCase() + "%";
        int totalRows = templateRepository.advancedCount(
                AuthUtils.getUsername(),
                name);
        int totalPages = form.getPageSize() > 0 ? ((Double) Math.ceil((double) totalRows /
                form.getPageSize())).intValue() : 1;
        return new PageMetadata(totalPages, totalRows);
    }

    public void deleteTemplate(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        Template template = getTemplateById(id);
        templateRepository.delete(template);
    }
}
