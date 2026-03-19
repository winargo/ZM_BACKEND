package com.billermanagement.persistance.domain.resultset;

import com.billermanagement.persistance.domain.TransHistory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransHistoryResp {

    @JsonProperty("totalPages")
    private int totalPages;
    @JsonProperty("totalElements")
    private long totalElements;
    @JsonProperty("transHistoryResults")
    private List<TransHistory> transHistoryResults;
}
