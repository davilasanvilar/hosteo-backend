package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Activity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivityDto extends BaseEntityDto {

    String name;

    String description;

    UserDto user;

    public ActivityDto(Activity activity) {
        this.name = activity.getName();
        this.description = activity.getDescription();
        this.user = new UserDto(activity.getUser());
    }
}
