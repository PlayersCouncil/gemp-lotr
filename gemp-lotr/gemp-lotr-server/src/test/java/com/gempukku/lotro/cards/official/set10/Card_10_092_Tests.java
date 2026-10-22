package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillager", "10_92");
					put("pillager2", "10_92");

					put("chieftain", "1_266");      // Orc Chieftain: a [sauron] Orc; his trigger answers only a weapon played on him
					put("sentry", "10_84");         // Cirith Ungol Sentry: a [sauron] Uruk-hai, damage +1, no other text
					put("fear", "1_278");           // Strength Born of Fear: a [sauron] support-area condition, no minion; its skirmish ability is never used
					put("troop", "1_177");          // Goblin Patrol Troop: a [moria] Orc, no text
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: a [gollum] Shadow card; a regroup ability only

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability only ever used on purpose
					put("sam", "1_311");            // Sam, Son of Hamfast: [shire] Hobbit companion; his abilities are used only on purpose
					put("bounder", "1_286");        // Bounder: [shire] Hobbit ally; a skirmish ability only
					put("stairs", "1_24");          // Stairs of Khazad-dum: [dwarven] support-area condition, the only [dwarven] card in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorPillagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Mordor Pillager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>. <b>Fierce</b>.<br><b>Shadow:</b> Exert this minion and spot another [sauron] minion to control a site. If you cannot spot 3 Free Peoples cultures, the Free Peoples player may exert a companion to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pillager");

		assertEquals("Mordor Pillager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithThreeFreePeoplesCulturesOneOfThemByAConditionItExertsToControlSite1AndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(stairs);
		assertInZone(Zone.SUPPORT, stairs);

		scn.StartGame();
		// Player 1's second turn: both markers have passed site 1.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(pillager, chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager, chieftain);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertEquals(3, scn.GetCurrentSiteNumber());
		// [shire] Frodo, [gondor] Aragorn and [dwarven] by a condition alone.
		assertEquals(3, scn.GetFreepsCultureCount());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetWoundsOn(pillager));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.ShadowActionAvailable(pillager));

		scn.ShadowUseCardAction(pillager);
		assertEquals(1, scn.GetWoundsOn(pillager));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		// Three cultures can be spotted: the Free Peoples player is not asked.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BesideACirithUngolSentryWithOneCultureTheFreePeoplesPlayerMayDeclineAndSite1IsControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(pillager, sentry);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager, sentry);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertEquals(1, scn.GetFreepsCultureCount());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// A [sauron] Uruk-hai is the other [sauron] minion.
		assertTrue(scn.ShadowActionAvailable(pillager));

		scn.ShadowUseCardAction(pillager);
		assertEquals(1, scn.GetWoundsOn(pillager));
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoCulturesBesideAGollumCardTheFreePeoplesPlayerMayExertACompanionToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pillager = scn.GetShadowCard("pillager");
		var chieftain = scn.GetShadowCard("chieftain");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(2);
		// Sam exhausted: 3 wounds of 4.
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(pillager, chieftain, gollum);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertInZone(Zone.SHADOW_CHARACTERS, pillager, chieftain, gollum);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		// [shire] Frodo, Sam and Bounder, [gondor] Aragorn; a [gollum] Shadow card spots no Free Peoples culture.
		assertEquals(2, scn.GetFreepsCultureCount());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.ShadowUseCardAction(pillager);
		assertEquals(1, scn.GetWoundsOn(pillager));
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// Companions of two cultures and races who can exert; not the exhausted Sam, an ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, bounder, pillager, chieftain, gollum));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));
		// Prevented: no site is controlled, and the Pillager's exertion stays paid.
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetWoundsOn(pillager));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedThePreventionIsNotAskedAndSite1IsControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pillager = scn.GetShadowCard("pillager");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		// Each of vitality 4, with 3 wounds.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(pillager, chieftain);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertEquals(2, scn.GetFreepsCultureCount());
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(aragorn));
		assertFalse(scn.IsSiteControlled(site1));

		scn.ShadowUseCardAction(pillager);
		assertEquals(1, scn.GetWoundsOn(pillager));
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherSauronMinionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager");
		var fear = scn.GetShadowCard("fear");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(fear);
		assertInZone(Zone.SUPPORT, fear);

		scn.StartGame();
		scn.SkipToSite(2);
		// A [sauron] card that is no minion, and a minion of another culture.
		scn.MoveMinionsToTable(pillager, troop);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager, troop);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(pillager));
		assertFalse(scn.IsSiteControlled(scn.GetSite(1)));
		assertFalse(scn.ShadowActionAvailable(pillager));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(pillager, chieftain);
		// Vitality 2, with 1 wound.
		scn.AddWoundsToChar(pillager, 1);
		assertEquals(1, scn.GetWoundsOn(pillager));
		scn.FreepsPass();

		assertEquals(1, scn.GetVitality(pillager));
		assertFalse(scn.IsSiteControlled(scn.GetSite(1)));
		assertFalse(scn.ShadowActionAvailable(pillager));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnTheFirstTurnWithNoSiteToTakeTheExertionIsPaidForNothingAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pillager = scn.GetShadowCard("pillager");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(pillager, chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager, chieftain);
		scn.FreepsPass();

		// Player 2's marker is still on site 1: no site can be controlled.  One culture: a prevention would be asked.
		assertEquals(1, scn.GetFreepsCultureCount());
		assertEquals(0, scn.GetWoundsOn(pillager));
		assertTrue(scn.ShadowActionAvailable(pillager));

		scn.ShadowUseCardAction(pillager);
		assertEquals(1, scn.GetWoundsOn(pillager));
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.IsSiteControlled(scn.GetSite(1)));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BesideASecondPillagerOnlyTheCopyUsedExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager");
		var pillager2 = scn.GetShadowCard("pillager2");

		scn.StartGame();
		scn.SkipToSite(2);
		// Each copy is the other's [sauron] minion.
		scn.MoveMinionsToTable(pillager, pillager2);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager, pillager2);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertEquals(0, scn.GetWoundsOn(pillager));
		assertEquals(0, scn.GetWoundsOn(pillager2));
		assertTrue(scn.ShadowActionAvailable(pillager));
		assertTrue(scn.ShadowActionAvailable(pillager2));

		scn.ShadowUseCardAction(pillager);
		assertEquals(1, scn.GetWoundsOn(pillager));
		assertEquals(0, scn.GetWoundsOn(pillager2));
		// Frodo's is the one culture: he is asked, and lets it be.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
