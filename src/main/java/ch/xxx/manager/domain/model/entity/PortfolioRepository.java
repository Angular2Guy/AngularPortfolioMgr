package ch.xxx.manager.domain.model.entity;

import java.util.List;

import ch.xxx.manager.domain.model.entity.dto.PortfolioAndSymbolDto;

public interface PortfolioRepository {
	List<Portfolio> findByUserId(Long userId);
	List<PortfolioAndSymbolDto> findPortfolioCalcEntitiesByPortfolioId(Long portfolioId);
}
