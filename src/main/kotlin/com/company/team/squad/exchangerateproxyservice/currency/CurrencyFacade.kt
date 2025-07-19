package com.company.team.squad.exchangerateproxyservice.currency

import com.company.team.squad.exchangerateproxyservice.currency.usecase.GetCurrenciesUsecase
import org.springframework.stereotype.Component

@Component
class CurrencyFacade(
    private val getCurrenciesUsecase: GetCurrenciesUsecase
) {
    val getCurrencies = getCurrenciesUsecase::execute
}