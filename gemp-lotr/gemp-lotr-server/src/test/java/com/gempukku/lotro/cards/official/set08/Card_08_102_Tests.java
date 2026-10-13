package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_102_Tests
{
	// The King block: site 1 Edoras Hall, site 2 King's Tent, site 5 Pelennor Flat (a maneuver trigger), site 9
	// Dagorlad (the move to it adds threats).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "8_102");
					put("troll2", "8_102");      // a second copy: not unique

					// [sauron] engines, one of each type
					put("grond", "8_103");       // Grond: unique [sauron] engine possession, support area; abilities used only on purpose
					put("towers", "7_281");      // Great Siege-towers: [sauron] engine condition, cost 1, no requirement to play; abilities used only on purpose

					put("banner", "1_263");      // Orc Banner: [sauron] support condition that is no engine; answers a skirmish lost
					put("ladder", "6_58");       // Assault Ladder: [isengard] machine, a support condition that is no engine; abilities used only on purpose

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man, to bear The Saga of Elendil and Coat of Mail
					put("lord", "1_21");         // Lord of Moria: [dwarven] support condition, each Dwarf damage +1
					put("saga", "1_114");        // The Saga of Elendil: [gondor] condition borne by a [gondor] companion; a skirmish ability
					put("coat", "1_101");        // Coat of Mail: [gondor] possession for a Man, no number
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship block: site 5 is The Bridge of Khazad-dum, a site 5 of another block.
	protected VirtualTableScenario GetFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "8_102");
					put("troll2", "8_102");      // a second copy: not unique

					// [sauron] engines, one of each type
					put("grond", "8_103");       // Grond: unique [sauron] engine possession, support area; abilities used only on purpose
					put("towers", "7_281");      // Great Siege-towers: [sauron] engine condition, cost 1, no requirement to play; abilities used only on purpose

					put("banner", "1_263");      // Orc Banner: [sauron] support condition that is no engine; answers a skirmish lost
					put("ladder", "6_58");       // Assault Ladder: [isengard] machine, a support condition that is no engine; abilities used only on purpose

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man, to bear The Saga of Elendil and Coat of Mail
					put("lord", "1_21");         // Lord of Moria: [dwarven] support condition, each Dwarf damage +1
					put("saga", "1_114");        // The Saga of Elendil: [gondor] condition borne by a [gondor] companion; a skirmish ability
					put("coat", "1_101");        // Coat of Mail: [gondor] possession for a Man, no number
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
		 * Game Text: <b>Besieger</b>. <b>Fierce</b>.<br>The twilight cost of this minion is -1 for each [sauron] engine you spot.<br>When you play this minion at sites 5[K] to 9[K], you may discard a Free Peoples condition.
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
	public void CostsOneLessForEachSauronEngineAndIsPlayedForThat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var troll2 = scn.GetShadowCard("troll2");
		var grond = scn.GetShadowCard("grond");
		var towers = scn.GetShadowCard("towers");
		var banner = scn.GetShadowCard("banner");
		var ladder = scn.GetShadowCard("ladder");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToHand(troll, troll2, towers);
		scn.MoveCardsToSupportArea(banner, ladder);
		scn.MoveCardsToSupportArea(lord);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Shadow phase at King's Tent, site 2.  A [sauron] condition that is no engine, an [isengard] machine and
		// an engine in hand do not count.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(9, scn.GetTwilightCost(troll));
		assertEquals(9, scn.GetTwilightCost(troll2));

		scn.MoveCardsToSupportArea(grond);
		// One engine, a possession.  Each copy in hand is 1 less, not 2: "this minion" does not reach the other.
		assertEquals(8, scn.GetTwilightCost(troll));
		assertEquals(8, scn.GetTwilightCost(troll2));

		scn.ShadowPlayCard(towers);
		assertInZone(Zone.SUPPORT, towers);
		// Two engines, the second a condition played from hand.
		assertEquals(7, scn.GetTwilightCost(troll));
		assertEquals(7, scn.GetTwilightCost(troll2));

		// 7 + 2 roaming at site 2: one short, then exactly enough.
		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(troll));
		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(0, scn.GetTwilight());
		// At site 2 of the King block nothing is offered for the Free Peoples condition in play.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertInZone(Zone.SUPPORT, lord);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtSite5OfTheKingBlockItMayDiscardAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var troll2 = scn.GetShadowCard("troll2");
		var towers = scn.GetShadowCard("towers");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		var saga = scn.GetFreepsCard("saga");
		var coat = scn.GetFreepsCard("coat");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga, coat);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(troll);
		scn.MoveCardsToSupportArea(towers);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(troll2);
		// 9 - 1 engine; no roaming at site 5.
		int before = scn.GetTwilight();
		scn.ShadowPlayCard(troll);
		assertEquals(before - 8, scn.GetTwilight());

		// Only the copy that was played offers its trigger.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll2));
		scn.ShadowAcceptOptionalTrigger();

		// Free Peoples conditions of two cultures, in the support area and borne; not a Free Peoples possession,
		// nor a Shadow condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lord, saga));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(coat, towers));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(saga);

		assertInDiscard(saga);
		assertInZone(Zone.SUPPORT, lord, towers);
		assertAttachedTo(coat, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtSite9OfTheKingBlockItIsOfferedAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var lord = scn.GetFreepsCard("lord");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(troll);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(troll);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, lord);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtSite4OfTheKingBlockItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var lord = scn.GetFreepsCard("lord");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(troll);
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertInZone(Zone.SUPPORT, lord);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtSite5OfTheFellowshipBlockItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var troll = scn.GetShadowCard("troll");
		var lord = scn.GetFreepsCard("lord");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(troll);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Bridge of Khazad-dum: site 5, but not of the King block.
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertInZone(Zone.SUPPORT, lord);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtSite5OfTheKingBlockWithNoFreePeoplesConditionNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var towers = scn.GetShadowCard("towers");
		var aragorn = scn.GetFreepsCard("aragorn");
		var coat = scn.GetFreepsCard("coat");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, coat);
		scn.MoveCardsToHand(troll);
		scn.MoveCardsToSupportArea(towers);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);

		// A Free Peoples possession and a Shadow condition only: the offer would discard nothing.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertAttachedTo(coat, aragorn);
		assertInZone(Zone.SUPPORT, towers);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
