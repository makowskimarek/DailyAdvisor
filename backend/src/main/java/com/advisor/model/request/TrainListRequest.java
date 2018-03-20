package com.advisor.model.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class TrainListRequest {
    @NotNull
    private Set<TrainingRequest> trainings;

    @NotNull
    private long trainId;

    @NotNull
    private long creatorId;

    @NotBlank
    private String trainName;

    @NotBlank
    private String trainText;


    public TrainListRequest() {
    }

}