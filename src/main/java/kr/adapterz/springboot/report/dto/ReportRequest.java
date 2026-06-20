package kr.adapterz.springboot.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ReportRequest {
    private Long userId;
    private String reason;
}
