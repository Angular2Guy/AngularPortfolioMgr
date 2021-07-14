package ch.xxx.manager.domain.model.entity;

import java.util.Optional;

import ch.xxx.manager.domain.utils.CurrencyKey;

public interface CurrencyRepository {
	Optional<Currency> findByToCurr(CurrencyKey toCurr);
}
