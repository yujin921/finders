package net.datasa.finders.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FindFreelancerDTO;
import net.datasa.finders.service.FindService;

@Slf4j
@RestController
@RequestMapping("find")
@RequiredArgsConstructor
public class FindRestController {
	
	private final FindService findService;
	
	@GetMapping("findFreelancer")
	public List<FindFreelancerDTO> findFreelancer(@RequestParam("fields") String[] fields
			,@RequestParam("areas") String[] areas
			,@RequestParam("search") String search) {
		
		log.debug("{}, {}",fields, areas);
		
		List<FindFreelancerDTO> findFreelancerList = findService.findFreelancerList(fields, areas, search);
		
		log.debug("{}", findFreelancerList);
		
		return findFreelancerList;
	}
}
