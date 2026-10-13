package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_102_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "58_102");
					put("troll2", "58_102");     // a second copy, in play when the first is played: not unique

					// [sauron] engines
					put("grond", "8_103");       // Grond: unique [sauron] engine possession, support area; abilities used only on purpose
					put("towers", "7_281");      // Great Siege-towers: [sauron] engine condition, cost 1, no requirement to play; abilities used only on purpose
					put("towers2", "7_281");
					put("walkway", "7_315");     // Tower Walkway: [sauron] engine condition; besiegers strength +1 per site the Shadow player controls (none)
					put("trooptower", "7_316");  // Troop Tower: [sauron] engine condition; [sauron] Orcs +1 per besieger stacked on a site (none)
					put("companies", "8_107");   // Their Marching Companies: [sauron] engine condition; abilities need threats or stacked minions

					put("bowmen", "1_264");      // Orc Bowmen: [sauron] support condition that is no engine; archery only
					put("ladder", "6_58");       // Assault Ladder: [isengard] machine, a support condition that is no engine; abilities used only on purpose

					put("legolas", "1_50");      // Legolas, Greenleaf: Elf companion, to bear The Tale of Gil-galad
					put("lord", "1_21");         // Lord of Moria: [dwarven] support condition, each Dwarf damage +1
					put("tale", "1_66");         // The Tale of Gil-galad: [elven] condition borne by an Elf
					put("sword", "1_299");       // Hobbit Sword: [shire] possession, bearer must be a Hobbit, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatHillTrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Great Hill Troll
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 9
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 16
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: Besieger. Fierce.
		 * The twilight cost of this minion is -1 for each [Sauron] engine you can spot (limit -5).
		 * When you play this minion at any site 5 or higher, you may discard an engine to discard a Free Peoples condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troll");

		assertEquals("Great Hill Troll", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(16, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CostsOneLessForEachSauronEngineDownToFourAndIsPlayedForThat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var grond = scn.GetShadowCard("grond");
		var towers = scn.GetShadowCard("towers");
		var walkway = scn.GetShadowCard("walkway");
		var trooptower = scn.GetShadowCard("trooptower");
		var companies = scn.GetShadowCard("companies");
		var towers2 = scn.GetShadowCard("towers2");
		var bowmen = scn.GetShadowCard("bowmen");
		var ladder = scn.GetShadowCard("ladder");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(troll, towers);
		scn.MoveCardsToSupportArea(bowmen, ladder);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// At site 5: no roaming.  An [isengard] machine and a [sauron] condition that is no engine do not count.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(9, scn.GetTwilightCost(troll));

		scn.MoveCardsToSupportArea(grond);
		assertEquals(8, scn.GetTwilightCost(troll));

		scn.ShadowPlayCard(towers);
		assertInZone(Zone.SUPPORT, towers);
		assertEquals(7, scn.GetTwilightCost(troll));

		scn.MoveCardsToSupportArea(walkway, trooptower, companies);
		// 5 [sauron] engines
		assertEquals(4, scn.GetTwilightCost(troll));

		scn.MoveCardsToSupportArea(towers2);
		// 6 [sauron] engines: limit -5
		assertEquals(4, scn.GetTwilightCost(troll));

		int before = scn.GetTwilight();
		scn.ShadowPlayCard(troll);
		assertEquals(before - 4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troll);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, grond, towers, walkway, trooptower, companies, towers2, ladder, bowmen);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtSite5ItMayDiscardAnEngineToDiscardAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var troll2 = scn.GetShadowCard("troll2");
		var grond = scn.GetShadowCard("grond");
		var towers = scn.GetShadowCard("towers");
		var bowmen = scn.GetShadowCard("bowmen");
		var ladder = scn.GetShadowCard("ladder");
		var legolas = scn.GetFreepsCard("legolas");
		var lord = scn.GetFreepsCard("lord");
		var tale = scn.GetFreepsCard("tale");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, tale);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(troll);
		scn.MoveCardsToSupportArea(grond, towers, bowmen, ladder);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.MoveMinionsToTable(troll2);
		scn.ShadowPlayCard(troll);
		// Only the copy that was played offers its trigger.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll2));
		scn.ShadowAcceptOptionalTrigger();

		// "An engine": a possession or a condition; a [sauron] condition that is no engine and an [isengard] machine are not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(grond, towers));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bowmen, ladder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(grond);
		assertInDiscard(grond);
		assertInZone(Zone.SUPPORT, towers, bowmen, ladder);

		// Free Peoples conditions in the support area and borne; not a Free Peoples possession, nor a Shadow condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lord, tale));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, bowmen, towers, ladder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(tale);

		assertInDiscard(tale);
		assertInZone(Zone.SUPPORT, lord);
		assertAttachedTo(sword, frodo);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtSite9StillMayDiscardAnEngineToDiscardAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var grond = scn.GetShadowCard("grond");
		var towers = scn.GetShadowCard("towers");
		var lord = scn.GetFreepsCard("lord");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(troll);
		scn.MoveCardsToSupportArea(grond, towers);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// At site 9 the minion does not roam: 9 - 2 [sauron] engines.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(7, scn.GetTwilightCost(troll));
		int before = scn.GetTwilight();
		scn.ShadowPlayCard(troll);
		assertEquals(before - 7, scn.GetTwilight());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(grond, towers));
		scn.ShadowChooseCard(towers);

		assertInDiscard(towers);
		assertInDiscard(lord);
		assertInZone(Zone.SUPPORT, grond);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtSite4ItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var grond = scn.GetShadowCard("grond");
		var towers = scn.GetShadowCard("towers");
		var lord = scn.GetFreepsCard("lord");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(troll);
		scn.MoveCardsToSupportArea(grond, towers);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 9 - 2 [sauron] engines; the cost read in hand leaves roaming out
		assertEquals(7, scn.GetTwilightCost(troll));
		int before = scn.GetTwilight();
		scn.ShadowPlayCard(troll);

		// 7 + 2 roaming at site 4
		assertEquals(before - 9, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertInZone(Zone.SUPPORT, grond, towers, lord);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtSite5WithNoEngineInPlayItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var bowmen = scn.GetShadowCard("bowmen");
		var lord = scn.GetFreepsCard("lord");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(troll);
		scn.MoveCardsToSupportArea(bowmen);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int before = scn.GetTwilight();
		scn.ShadowPlayCard(troll);

		assertEquals(before - 9, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertInZone(Zone.SUPPORT, lord, bowmen);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
