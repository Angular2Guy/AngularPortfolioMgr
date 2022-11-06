package ch.xxx.manager.adapter.controller;

import org.springframework.http.MediaType;
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

	@GetMapping(path = "/profiles", produces =  MediaType.TEXT_PLAIN_VALUE)
	public String getProfiles() {
		return this.appInfoService.getProfiles();
	}
	
	@GetMapping(path = "/importpath", produces =  MediaType.TEXT_PLAIN_VALUE)
	public String getImportPath() {
		return this.appInfoService.getFinancialDataImportPath();
	}
}
