package ch.xxx.manager.adapter.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.CurrencyRepository;
import ch.xxx.manager.domain.utils.CurrencyKey;

@Repository
public class CurrencyRepositoryBean implements CurrencyRepository {
	private final JpaCurrencyRepository jpaCurrencyRepository;
	
	public CurrencyRepositoryBean(JpaCurrencyRepository jpaCurrencyRepository) {
		this.jpaCurrencyRepository = jpaCurrencyRepository;
	}

	@Override
	public Optional<Currency> findByToCurr(CurrencyKey toCurr) {
		return this.jpaCurrencyRepository.findByToCurr(toCurr);
	}
	
	
}
