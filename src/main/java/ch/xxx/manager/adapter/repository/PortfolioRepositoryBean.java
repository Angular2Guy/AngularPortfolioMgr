package ch.xxx.manager.adapter.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioRepository;
import ch.xxx.manager.domain.model.entity.dto.PortfolioAndSymbolDto;

@Repository
public class PortfolioRepositoryBean implements PortfolioRepository{
	private final JpaPortfolioRepository jpaPortfolioRepository;
	
	public PortfolioRepositoryBean(JpaPortfolioRepository jpaPortfolioRepository) {
		this.jpaPortfolioRepository = jpaPortfolioRepository;
	}

	@Override
	public List<Portfolio> findByUserId(Long userId) {
		return this.jpaPortfolioRepository.findByUserId(userId);
	}

	@Override
	public List<PortfolioAndSymbolDto> findPortfolioCalcEntitiesByPortfolioId(Long portfolioId) {
		return this.jpaPortfolioRepository.findPortfolioCalcEntitiesByPortfolioId(portfolioId);
	}
}
