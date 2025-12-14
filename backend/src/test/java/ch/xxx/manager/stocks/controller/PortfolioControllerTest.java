/**
 *    Copyright 2019 Sven Loesekann
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package ch.xxx.manager.stocks.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import ch.xxx.manager.ComponentScanCustomFilter;
import ch.xxx.manager.common.entity.AppUser;
import ch.xxx.manager.stocks.entity.Portfolio;
import ch.xxx.manager.stocks.entity.dto.PortfolioWithElements;
import ch.xxx.manager.common.utils.DataHelper;
import ch.xxx.manager.common.utils.DataHelper.Role;
import ch.xxx.manager.common.utils.JwtUtils;
import ch.xxx.manager.common.JwtTokenService;
import ch.xxx.manager.stocks.PortfolioService;
import jakarta.servlet.http.HttpServletRequest;

@DisabledInAotMode
@WebMvcTest(PortfolioController.class)
@ComponentScan(basePackages = "ch.xxx.manager",excludeFilters = 
@Filter(type = FilterType.CUSTOM, classes = ComponentScanCustomFilter.class))
public class PortfolioControllerTest extends BaseControllerTest {
	private static final String TEST_SECRECT_KEY = "l4v46cegVyPzuqCPs2bZw1egItei_5n-FrZChxcg8iYVZcEs6_2TbvtlYVtmuheU77O4AurSah3JCAyfuapG"
			+ "CRSLpttN9dMqam85wSRjhoKDz-_QWAjbUMptwFlskNa_8vZ-DvwwnkcvbEfBSvVJSUt8_4ZrWpBq1tX56PTOobbI-oXasUk-meYdD2tLDvErmPXC"
			+ "ntTSqGB7c4jcoPT3IX1mUsNZp5hYPUWpZjXDSmx2Os1JhY2ezTJJBpMq0o559aSJPs1rkqH1zEFrYDs41-mFTujaIrxv4iC8wGsXqvixamg9mC0P8n"
			+ "645McBJ6Q3X0PElFGbF6gmKtvrOqpQHA==";
	@MockitoBean
	private PortfolioService portfolioService;
	@MockitoBean
	private JwtTokenService jwtTokenService;
	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void init() {
		Mockito.when(this.jwtTokenService.createToken(any(String.class), any(List.class), any(Optional.class)))
				.thenCallRealMethod();
		Mockito.when(this.jwtTokenService.resolveToken(any(HttpServletRequest.class))).thenReturn(Optional.of(""));
		Mockito.when(this.jwtTokenService.validateToken(any(String.class))).thenReturn(true);
		Mockito.when(this.jwtTokenService.getAuthentication(any(String.class))).thenCallRealMethod();
		Mockito.when(this.jwtTokenService.getTokenUserRoles(any(Map.class))).thenCallRealMethod();
		Mockito.when(this.jwtTokenService.getUsername(any(String.class))).thenReturn("XXX");
		Mockito.when(this.jwtTokenService.getAuthorities(any(String.class))).thenReturn(List.of(DataHelper.Role.USERS));
		Mockito.when(this.portfolioService.getPortfolioById(any(Long.class))).thenReturn(this.createPortfolioWithElements());
		ReflectionTestUtils.setField(this.jwtTokenService, "secretKey", TEST_SECRECT_KEY);
		ReflectionTestUtils.setField(this.jwtTokenService, "validityInMilliseconds", 60000);
		Mockito.doCallRealMethod().when(this.jwtTokenService).init();
		this.jwtTokenService.init();
	}

	@Test
	public void findPortfolioByIdNotFound() throws Exception {
		String myToken = this.jwtTokenService.createToken("XXX", List.of(Role.USERS), Optional.empty());
		PortfolioWithElements myPortfolio = createPortfolioWithElements();
		Mockito.when(this.portfolioService.getPortfolioById(any(Long.class))).thenReturn(myPortfolio);
		this.mockMvc.perform(get("/rest/portfolio/id/{portfolioId}", 1L)
				.header(JwtUtils.AUTHORIZATION, String.format("Bearer %s", myToken)).servletPath("/rest/portfolio/id/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("MyPortfolio"));
	}

	private PortfolioWithElements createPortfolioWithElements() {
		Portfolio myPortfolio = new Portfolio();
		ReflectionTestUtils.setField(myPortfolio, "createdAt", LocalDate.now());
		myPortfolio.setId(1L);
		myPortfolio.setName("MyPortfolio");
		myPortfolio.setAppUser(new AppUser());
		return new PortfolioWithElements(myPortfolio, List.of(), List.of(), List.of());
	}
}
