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
    private String projectTitle;
    private String projectDescription;
    private String projectLink;
    private String freelancerId;
}
