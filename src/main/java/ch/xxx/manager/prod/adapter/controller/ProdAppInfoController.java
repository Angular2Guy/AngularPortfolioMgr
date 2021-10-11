package ch.xxx.manager.prod.adapter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.usecase.service.AppInfoService;

@RestController
@RequestMapping("/rest/prod/app-info")
public class ProdAppInfoController {
	private final AppInfoService appInfoService;
	
	public ProdAppInfoController(AppInfoService appInfoService) {
		this.appInfoService = appInfoService;
	}
	
	@GetMapping("/class-name")
	public String getClassName() {
		return this.appInfoService.getClassName();
	}
}
