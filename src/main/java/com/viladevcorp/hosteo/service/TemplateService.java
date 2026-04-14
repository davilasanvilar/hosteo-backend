package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Template;
import com.viladevcorp.hosteo.model.forms.TemplateCreateForm;
import com.viladevcorp.hosteo.model.forms.TemplateSearchForm;
import com.viladevcorp.hosteo.model.forms.TemplateUpdateForm;
import com.viladevcorp.hosteo.repository.TemplateRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.management.InstanceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TemplateService {

  private final TemplateRepository templateRepository;

  @Autowired
  public TemplateService(TemplateRepository templateRepository) {
    this.templateRepository = templateRepository;
  }

  public Template createTemplate(TemplateCreateForm form) {
    Template template =
        Template.builder()
            .name(form.getName())
            .type(form.getType())
            .category(form.getCategory())
            .duration(form.getDuration())
            .steps(form.getSteps())
            .build();

    return templateRepository.save(template);
  }

  public Template updateTemplate(TemplateUpdateForm form) throws InstanceNotFoundException {
    Template template = getTemplateById(form.getId());
    BeanUtils.copyProperties(form, template, "id");
    return templateRepository.save(template);
  }

  public Template getTemplateById(UUID id) throws InstanceNotFoundException {
    Optional<Template> template = templateRepository.findById(id, AuthUtils.getUsername());
    if (template.isEmpty()) {
      throw new InstanceNotFoundException("Template not found with id: " + id);
    }
    return template.get();
  }

  public List<Template> findTemplates(TemplateSearchForm form) {
    String name =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";
    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    return templateRepository.advancedSearch(AuthUtils.getUsername(), name, pageRequest);
  }

  public PageMetadata getTemplatesMetadata(TemplateSearchForm form) {
    String name =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";
    int totalRows = templateRepository.advancedCount(AuthUtils.getUsername(), name);
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteTemplate(UUID id) throws InstanceNotFoundException {
    Template template = getTemplateById(id);
    templateRepository.delete(template);
  }
}
