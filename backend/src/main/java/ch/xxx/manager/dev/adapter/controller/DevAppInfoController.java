package ch.xxx.manager.dev.adapter.controller;

import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.usecase.service.AppInfoService;

@Primary
@RestController
@RequestMapping("/rest/dev/app-info")
public class DevAppInfoController {
	private final AppInfoService appInfoService;

	public DevAppInfoController(AppInfoService appInfoService) {
		this.appInfoService = appInfoService;
	}

	@GetMapping(path = "/class-name", produces = MediaType.TEXT_PLAIN_VALUE)
	public String getClassName() {
		return this.appInfoService.getClassName();
	}
}
