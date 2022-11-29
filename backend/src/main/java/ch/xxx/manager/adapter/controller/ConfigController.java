package ch.xxx.manager.adapter.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.domain.model.dto.FilterNumberDto;
import ch.xxx.manager.domain.model.dto.FilterStringDto;
import ch.xxx.manager.domain.utils.DataHelper;
import ch.xxx.manager.domain.utils.DataHelper.TermType;
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
	
	@GetMapping(path = "/operators/string")
	public List<FilterStringDto.Operation> getStringOperators() {
		return List.of(FilterStringDto.Operation.values());
	}
	
	@GetMapping(path = "/operators/number")
	public List<FilterNumberDto.Operation> getNumberOperators() {
		return List.of(FilterNumberDto.Operation.values());
	}
	
	@GetMapping(path = "/operators/query")
	public List<DataHelper.Operation> getQueryOperators() {
		return List.of(DataHelper.Operation.values());
	}
}
