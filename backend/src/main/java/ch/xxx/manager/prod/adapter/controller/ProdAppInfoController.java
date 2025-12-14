package ch.xxx.manager.prod.adapter.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.common.AppInfoService;

@RestController
@RequestMapping("/rest/prod/app-info")
public class ProdAppInfoController {
	private final AppInfoService appInfoService;

	public ProdAppInfoController(AppInfoService appInfoService) {
		this.appInfoService = appInfoService;
	}

	@GetMapping(path = "/class-name", produces = MediaType.TEXT_PLAIN_VALUE)
	public String getClassName() {
		return this.appInfoService.getClassName();
	}
}
