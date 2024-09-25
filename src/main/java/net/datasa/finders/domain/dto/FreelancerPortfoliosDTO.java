package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreelancerPortfoliosDTO {
	
    private int portfolioId;
    private String portfolioTitle;
    private String portfolioDescription;
    private String projectLink;
    private String freelancerId;
}
