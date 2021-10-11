package ch.xxx.manager.adapter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.usecase.service.AppInfoService;

@RestController
@RequestMapping("rest/config")
public class ConfigController {
	private final AppInfoService appInfoService;
	
	public ConfigController(AppInfoService appInfoService) {
		this.appInfoService = appInfoService;
	}
	
	@GetMapping("/profiles")
	public String getProfiles() {
		return this.appInfoService.getProfiles();
	}
}
