package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.types.CategoryEnum;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class TaskDto extends BaseEntityDto {

  public TaskDto(Task task) {
    BeanUtils.copyProperties(task, this, "apartment");
  }

  private String name;

  private CategoryEnum category;

  private int duration;

  private boolean extra;

  private List<String> steps = new ArrayList<>();
}
