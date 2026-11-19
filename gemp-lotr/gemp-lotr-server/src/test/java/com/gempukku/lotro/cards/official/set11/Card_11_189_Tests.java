package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_189_Tests
{

	// Site 2 is Eastfold 4_331 (plains, no battleground); site 4 is White Rocks 4_346 (battleground only).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruk", "11_189");
					put("uruk2", "11_189");
					put("savage", "1_151");     // Uruk Savage: another Uruk-hai minion, cost 2; damage +1 and no other text

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer, resistance 5; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man, resistance 6; a maneuver ability only
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound [shire] Hobbit, resistance 6; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 2 is Uruk Camp 4_335 (battleground and plains; each Uruk-hai played must exert).
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruk", "11_189");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man, resistance 6; a maneuver ability only
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound [shire] Hobbit, resistance 6; a skirmish ability used only on purpose
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

	@Test
	public void IntimidatingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Intimidating Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this minion at a battleground site, spot an unbound companion to make that companion resistance -2 until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("uruk");

		assertEquals("Intimidating Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedAtABattlegroundItMakesTheChosenUnboundCompanionResistanceMinus2AndAnotherUrukHaiPlayedAfterDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, aragorn, merry);
		scn.MoveAlliesToTable(bounder);

		var uruk = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(uruk, savage);
		assertInHand(uruk, savage);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetTwilight(10);
		scn.FreepsPass();
		// A second copy already in play: its trigger answers its own play only.
		scn.MoveMinionsToTable(uruk2);
		scn.ShadowRefreshDecision();

		// White Rocks, site 4: a battleground.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(uruk, savage);
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(merry));

		scn.ShadowPlayCard(uruk);
		// The unbound companions of two cultures and races are offered; the Ring-bearer, Sam (Ring-bound) and an
		// ally are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// One choice and one -2: the copy already in play did not answer this one's play.
		assertInZone(Zone.SHADOW_CHARACTERS, uruk, uruk2);
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another Uruk-hai played at the same battleground sets nothing off.
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(merry));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtABattlegroundThatIsAlsoAPlainsTheResistanceIsLowerUntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToHand(uruk);
		assertInHand(uruk);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Uruk Camp, site 2: a battleground and a plains.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(merry));

		scn.ShadowPlayCard(uruk);
		// The site's exertion and this minion's trigger answer the same play; the Free Peoples player orders them.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Uruk Camp");
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, merry));
		scn.ShadowChooseCard(merry);

		assertEquals(4, scn.GetResistance(merry));
		assertEquals(6, scn.GetResistance(aragorn));

		// Still in force in the Shadow phase after the fellowship's second move of the turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetResistance(merry));

		// The turn over, Merry is back at 6.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(6, scn.GetResistance(aragorn));
	}

	@Test
	public void PlayedAwayFromABattlegroundItDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToHand(uruk);
		assertInHand(uruk);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Eastfold, site 2: a plains, and no battleground.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(merry));

		scn.ShadowPlayCard(uruk);

		assertInZone(Zone.SHADOW_CHARACTERS, uruk);
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(merry));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtABattlegroundWithNoUnboundCompanionItDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToHand(uruk);
		assertInHand(uruk);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));

		scn.ShadowPlayCard(uruk);

		// Only Ring-bound companions: nobody to spot, and nothing is asked.
		assertInZone(Zone.SHADOW_CHARACTERS, uruk);
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
