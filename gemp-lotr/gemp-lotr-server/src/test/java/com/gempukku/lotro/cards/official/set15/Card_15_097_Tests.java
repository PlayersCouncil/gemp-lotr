package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beasts", "15_97");
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, cost 1; its trigger needs an underground site
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] Half-troll, cost 3; its start-of-assignment trigger reaches no test here
					put("mocking", "11_127");    // Mocking Goblin: [orc] Orc, cost 5, site 4; a trigger on its own win only
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc minion, no text
					put("beckoning", "12_83");   // The Beckoning Shadow: [orc] support-area condition; nothing below 5 burdens
					put("beckoning2", "12_83");
					put("blow", "15_115");       // Pummeling Blow: [orc] condition borne by a site
					put("grond", "18_82");       // Grond, Forged With Black Steel: [orc] support-area possession; a regroup-move trigger only
					put("sbof", "1_278");        // Strength Born of Fear: [sauron] support-area condition, a skirmish ability only
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: companion; a maneuver ability used only on purpose
					put("boromir", "1_97");      // Boromir, Son of Denethor: companion; a skirmish ability only
					put("gimli", "1_13");        // Gimli, Son of Gloin: companion; a skirmish ability only
					put("sam", "1_311");         // Sam, Son of Hamfast: companion; a fellowship ability used only on purpose
					put("merry", "1_303");       // Merry, From O'er the Brandywine: companion; strength +2 with a weapon only
					put("thrarin", "1_27");      // Thrarin, Dwarven Smith: an ally, a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeastsofBurdenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Beasts of Burden
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [orc] minions.<br>At the start of your Shadow phase, you may discard an [orc] condition from play to play an [orc] minion. It is twilight cost -2 (or -4 if you can spot 6 or more companions).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beasts");

		assertEquals("Beasts of Burden", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoOrcMinionsAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beasts = scn.GetShadowCard("beasts");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToHand(beasts, halftroll);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beckoning);
		assertInHand(beasts, halftroll);
		scn.MoveMinionsToTable(goblin, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// One [orc] minion, beside an Orc minion of another culture and an [orc] card that is no minion.
		assertInZone(Zone.SHADOW_CHARACTERS, goblin, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(beasts));

		// A second [orc] minion, of another race.
		scn.ShadowPlayCard(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertTrue(scn.ShadowPlayAvailable(beasts));

		scn.ShadowPlayCard(beasts);
		assertInZone(Zone.SUPPORT, beasts);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheStartOfTheShadowPhaseDiscardsAnOrcConditionFromPlayToPlayAnOrcMinionFromHandAtTwilightCostMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"),
				scn.GetFreepsCard("gimli"), scn.GetFreepsCard("sam"));
		scn.MoveAlliesToTable(thrarin);

		var beasts = scn.GetShadowCard("beasts");
		var beckoning = scn.GetShadowCard("beckoning");
		var beckoning2 = scn.GetShadowCard("beckoning2");
		var blow = scn.GetShadowCard("blow");
		var grond = scn.GetShadowCard("grond");
		var sbof = scn.GetShadowCard("sbof");
		var mocking = scn.GetShadowCard("mocking");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(beasts, beckoning, grond, sbof);
		scn.MoveCardsToHand(mocking, halftroll, troop, beckoning2);

		scn.StartGame();
		scn.AttachCardsTo(scn.GetCurrentSite(), blow);
		assertInZone(Zone.SUPPORT, beasts, beckoning, grond, sbof);
		assertInZone(Zone.ATTACHED, blow);
		assertInHand(mocking, halftroll, troop, beckoning2);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set + 5 companions + 2 for Ettenmoors.
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(beasts));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.ShadowGetChoiceMin());
		// [orc] conditions in play: itself, one in the support area and one on a site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(beasts, beckoning, blow));
		// An [orc] possession, a [sauron] condition, and an [orc] condition in hand, not in play.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(grond, sbof, beckoning2));
		scn.ShadowChooseCard(beckoning);
		assertInDiscard(beckoning);

		// [orc] minions in hand of two races; not a minion of another culture, nor an [orc] card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mocking, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, beckoning2));
		scn.ShadowChooseCard(mocking);

		assertInZone(Zone.SHADOW_CHARACTERS, mocking);
		assertInZone(Zone.SUPPORT, beasts, grond, sbof);
		assertInZone(Zone.ATTACHED, blow);
		assertInHand(halftroll, troop, beckoning2);
		// Five companions and an ally are not six companions: 5 - 2 + 2 roaming at Ettenmoors.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The discount was that minion's alone: the Half-troll from hand costs 3 + 2 roaming.
		scn.ShadowPlayCard(halftroll);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSixCompanionsItCanDiscardItselfAndTheMinionIsTwilightCostMinus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"),
				scn.GetFreepsCard("gimli"), scn.GetFreepsCard("sam"), scn.GetFreepsCard("merry"));

		var beasts = scn.GetShadowCard("beasts");
		var beckoning = scn.GetShadowCard("beckoning");
		var mocking = scn.GetShadowCard("mocking");
		scn.MoveCardsToSupportArea(beasts, beckoning);
		scn.MoveCardsToHand(mocking);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beasts, beckoning);
		assertInHand(mocking);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set + 6 companions + 2 for Ettenmoors.
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(beasts));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(beasts);
		assertInDiscard(beasts);

		// The only [orc] minion in hand is played unasked.
		assertInZone(Zone.SHADOW_CHARACTERS, mocking);
		assertInZone(Zone.SUPPORT, beckoning);
		// 5 - 4 + 2 roaming, with no question of which discount.
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesEverythingAsItWasAndItIsNotOfferedAtTheStartOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var beasts = scn.GetShadowCard("beasts");
		var beckoning = scn.GetShadowCard("beckoning");
		var mocking = scn.GetShadowCard("mocking");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToSupportArea(beasts, beckoning);
		scn.MoveCardsToHand(mocking);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beasts, beckoning);
		assertInHand(mocking);
		// A minion in play, so that there is a maneuver phase.
		scn.MoveMinionsToTable(goblin);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 2 companions + 2 for Ettenmoors.
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(beasts));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, beasts, beckoning);
		assertInHand(mocking);
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// No Shadow-phase action of its own either.
		assertFalse(scn.ShadowActionAvailable(beasts));

		// The pool can still pay for the Mocking Goblin; the maneuver phase opens on the Free Peoples player's actions,
		// with no offer to the Shadow player ahead of them.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(14, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(beasts));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, beasts, beckoning);
		assertInHand(mocking);
	}

	@Test
	public void NotOfferedWithNoOrcMinionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beasts = scn.GetShadowCard("beasts");
		var beckoning = scn.GetShadowCard("beckoning");
		var beckoning2 = scn.GetShadowCard("beckoning2");
		var troop = scn.GetShadowCard("troop");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToSupportArea(beasts, beckoning);
		// An [orc] minion in the discard pile is not in hand.
		scn.MoveCardsToDiscard(goblin);
		scn.MoveCardsToHand(troop, beckoning2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beasts, beckoning);
		assertInDiscard(goblin);
		assertInHand(troop, beckoning2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(beasts));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, beasts, beckoning);
	}

	@Test
	public void NotOfferedWithThePoolOneShortOfTheDiscountedCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beasts = scn.GetShadowCard("beasts");
		var mocking = scn.GetShadowCard("mocking");
		scn.MoveCardsToSupportArea(beasts);
		scn.MoveCardsToHand(mocking);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beasts);
		assertInHand(mocking);
		scn.SetTwilight(1);
		scn.FreepsPass();

		// 1 set + 1 companion + 2 for Ettenmoors: one short of 5 - 2 + 2 roaming.
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(beasts));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, beasts);
	}

	@Test
	public void OfferedWithThePoolExactlyAtTheDiscountedCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beasts = scn.GetShadowCard("beasts");
		var mocking = scn.GetShadowCard("mocking");
		scn.MoveCardsToSupportArea(beasts);
		scn.MoveCardsToHand(mocking);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beasts);
		assertInHand(mocking);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 companion + 2 for Ettenmoors: exactly 5 - 2 + 2 roaming.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(beasts));
		scn.ShadowAcceptOptionalTrigger();

		// Itself the only [orc] condition and the Goblin the only [orc] minion: both taken unasked.
		assertInDiscard(beasts);
		assertInZone(Zone.SHADOW_CHARACTERS, mocking);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
