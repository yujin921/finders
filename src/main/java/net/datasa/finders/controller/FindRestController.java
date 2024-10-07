package net.datasa.finders.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	public Page<FindFreelancerDTO> findFreelancer(@RequestParam("fields") List<String> fields
			,@RequestParam("areas") List<String> areas
			,@RequestParam("search") String search,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "10") int size) {
		
		log.debug("{}, {}",fields, areas);
		
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "memberId"));
		Page<FindFreelancerDTO> findFreelancerList = findService.findFreelancerList(fields, areas, search, pageable);
		
		log.debug("{}", findFreelancerList);
		
		return findFreelancerList;
	}
}
