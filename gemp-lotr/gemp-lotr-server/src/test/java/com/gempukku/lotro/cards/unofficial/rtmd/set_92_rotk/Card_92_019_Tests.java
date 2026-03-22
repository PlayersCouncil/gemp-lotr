package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.DeckValidationContext;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_019_Tests
{
	// Mixed Movie-block sites: mostly Fellowship, with a Towers site3 and King site6
	private final HashMap<String, String> MixedMovieSites = new HashMap<>() {{
		put("site1", "1_319");  // Fellowship
		put("site2", "1_327");  // Fellowship
		put("site3", "4_337");  // Towers
		put("site4", "1_343");  // Fellowship
		put("site5", "1_349");  // Fellowship
		put("site6", "7_350");  // King
		put("site7", "1_353");  // Fellowship
		put("site8", "1_356");  // Fellowship
		put("site9", "1_360");  // Fellowship
	}};

	// Mixed Shadows + Movie sites (should always be invalid, even with meta-site)
	private final HashMap<String, String> MixedShadowsMovieSites = new HashMap<>() {{
		put("site1", "1_319");  // Fellowship
		put("site2", "1_327");  // Fellowship
		put("site3", "1_341");  // Fellowship
		put("site4", "1_343");  // Fellowship
		put("site5", "11_239"); // Shadows
		put("site6", "1_351");  // Fellowship
		put("site7", "1_353");  // Fellowship
		put("site8", "1_356");  // Fellowship
		put("site9", "1_360");  // Fellowship
	}};

	private DeckValidationContext metaSiteContext() {
		return new DeckValidationContext().setSkipSiteBlockValidation(true);
	}

	@Test
	public void StatsAreCorrect() throws CardNotFoundException {
		var bp = VirtualTableScenario.FindCard("92_19");
		assertEquals("Race Text 92_19", bp.getTitle());
		assertEquals(CardType.METASITE, bp.getCardType());
	}

	// ===== Movie Block (multipath) =====

	@Test
	public void MovieBlock_CanMixMovieSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.Multipath);
		var deck = dvs.buildDeckWithSites(MixedMovieSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertFalse("Mixed Movie sites should pass with context", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void MovieBlock_CanUseShadowsSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.Multipath);
		var deck = dvs.buildDeckWithSites(DeckValidationScenario.ShadowsSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertFalse("Shadows sites should pass with context in Movie Block", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void MovieBlock_CannotMixShadowsAndMovieSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.Multipath);
		var deck = dvs.buildDeckWithSites(MixedShadowsMovieSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertTrue("Mixed Shadows+Movie sites should fail even with context", dvs.hasSiteBlockError(errors));
	}

	// ===== Fellowship Block =====

	@Test
	public void FellowshipBlock_CanMixMovieSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.Fellowship);
		var deck = dvs.buildDeckWithSites(MixedMovieSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertFalse("Mixed Movie sites should pass with context in Fellowship Block", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void FellowshipBlock_CanUseShadowsSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.Fellowship);
		var deck = dvs.buildDeckWithSites(DeckValidationScenario.ShadowsSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertFalse("Shadows sites should pass with context in Fellowship Block", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void FellowshipBlock_CannotMixShadowsAndMovieSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.Fellowship);
		var deck = dvs.buildDeckWithSites(MixedShadowsMovieSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertTrue("Mixed Shadows+Movie sites should fail even with context in Fellowship Block", dvs.hasSiteBlockError(errors));
	}

	// ===== PC-Movie (with maps) =====

	@Test
	public void PCMovie_WithFellowshipMap_CanMixMovieSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.PCMovie);
		var deck = dvs.buildDeckWithMap("102_74", MixedMovieSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertFalse("Mixed Movie sites should pass with Fellowship map + context", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void PCMovie_WithTowersMap_CanMixMovieSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.PCMovie);
		var deck = dvs.buildDeckWithMap("102_75", MixedMovieSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertFalse("Mixed Movie sites should pass with Towers map + context", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void PCMovie_WithKingMap_CanMixMovieSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.PCMovie);
		var deck = dvs.buildDeckWithMap("102_76", MixedMovieSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertFalse("Mixed Movie sites should pass with King map + context", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void PCMovie_WithFellowshipMap_CanUseShadowsSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.PCMovie);
		var deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.ShadowsSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertFalse("Shadows sites should pass with Fellowship map + context", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void PCMovie_WithFellowshipMap_CannotMixShadowsAndMovieSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.PCMovie);
		var deck = dvs.buildDeckWithMap("102_74", MixedShadowsMovieSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertTrue("Mixed Shadows+Movie sites should fail even with map + context", dvs.hasSiteBlockError(errors));
	}

	// ===== Expanded =====

	@Test
	public void Expanded_CanMixMovieSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.Shadows);
		var deck = dvs.buildDeckWithSites(MixedMovieSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertFalse("Mixed Movie sites should pass with context in Expanded", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void Expanded_CannotMixShadowsAndMovieSitesWithContext() {
		var dvs = new DeckValidationScenario(DeckValidationScenario.Shadows);
		var deck = dvs.buildDeckWithSites(MixedShadowsMovieSites);

		var errors = dvs.validate(deck, metaSiteContext());
		assertTrue("Mixed Shadows+Movie sites should fail even with context in Expanded", dvs.hasSiteBlockError(errors));
	}
}
