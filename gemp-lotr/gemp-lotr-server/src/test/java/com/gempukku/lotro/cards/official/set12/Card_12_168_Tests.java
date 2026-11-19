package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_168_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "12_168");
					put("nertea", "1_234");     // Úlairë Nertëa, Messenger of Dol Guldur: a [wraith] Nazgûl, strength 9; his text is a play trigger, and he is cheated in
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl; nothing of his text below 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl; nothing below 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Uruk Camp 4_335 (Battleground, Plains; exerts each Uruk-hai played, and none is) at site 2.
	// Site 1, Eastemnet Gullies 4_325, is neither battleground nor forest.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "12_168");
					put("nertea", "1_234");     // Úlairë Nertëa: a [wraith] Nazgûl, strength 9, to bear the Blade
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: another Nazgûl, strength 10; nothing below 2 burdens
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path as it is: Barrows of Edoras 4_337 (a sanctuary only) at site 3, White Rocks 4_346 (a battleground
	// and nothing else; its Shadow number counts mounted companions, and none is) at site 4.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "12_168");
					put("nertea", "1_234");     // Úlairë Nertëa: a [wraith] Nazgûl, strength 9, to bear the Blade
					put("nelya", "2_84");       // Úlairë Nelya: another Nazgûl, strength 10; nothing below 2 burdens
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with two forests: Trollshaw Forest 1_334 at site 2 (Forest alone; draws when a possession is
	// played on a companion, and none is) and Rivendell Waterfall 1_342 at site 3 (Forest and Sanctuary; a move limit
	// bonus while a ranger is there, and none is).
	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "12_168");
					put("nertea", "1_234");     // Úlairë Nertëa: a [wraith] Nazgûl, strength 9, to bear the Blade
					put("nelya", "2_84");       // Úlairë Nelya: another Nazgûl, strength 10; nothing below 2 burdens
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_334");
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NazgulBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Nazgûl Blade
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be a Nazgûl.<br>While bearer is at a battleground or forest site, he is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("Nazgûl Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnANazgulOfAnyCultureAndNotOnAnotherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var blade = scn.GetShadowCard("blade");
		var nertea = scn.GetShadowCard("nertea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(blade);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea, khamul, hound);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(blade));
		scn.ShadowPlayCard(blade);
		// A [wraith] Nazgûl and a [raider] one; not the [wraith] Orc, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nertea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, frodo, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);
		assertAttachedTo(blade, khamul);
		assertNotAttachedTo(blade, nertea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus1AtUrukCampAndNotBeforeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var blade = scn.GetShadowCard("blade");
		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nertea, nelya);
		scn.AttachCardsTo(nertea, blade);

		assertAttachedTo(blade, nertea);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		// 9 + 1 printed on the Blade
		assertEquals(10, scn.GetStrength(nertea));
		assertEquals(10, scn.GetStrength(nelya));

		// The fellowship moves to Uruk Camp, a battleground that is a plains as well.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		// 9 + 1 printed + 1 for the battleground
		assertEquals(11, scn.GetStrength(nertea));
		// Another Nazgûl at the same site, who does not bear it, is untouched.
		assertEquals(10, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus1AtWhiteRocksAndNotAtTheSanctuaryBeforeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var blade = scn.GetShadowCard("blade");
		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(nertea, nelya);
		scn.AttachCardsTo(nertea, blade);

		assertAttachedTo(blade, nertea);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertEquals(10, scn.GetStrength(nertea));
		assertEquals(10, scn.GetStrength(nelya));

		// The fellowship moves to White Rocks, a battleground and nothing else.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(11, scn.GetStrength(nertea));
		assertEquals(10, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus1AtEachForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var blade = scn.GetShadowCard("blade");
		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(nertea, nelya);
		scn.AttachCardsTo(nertea, blade);

		assertAttachedTo(blade, nertea);
		// Trollshaw Forest, a forest and nothing else.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(11, scn.GetStrength(nertea));
		assertEquals(10, scn.GetStrength(nelya));

		// Rivendell Waterfall, a forest that is a sanctuary as well.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(11, scn.GetStrength(nertea));
		assertEquals(10, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
