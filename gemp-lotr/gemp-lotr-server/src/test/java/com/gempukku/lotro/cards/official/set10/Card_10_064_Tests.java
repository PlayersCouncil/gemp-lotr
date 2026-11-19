package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stooping", "10_64");
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					put("mongrel", "7_194");    // Morgul Mongrel: [wraith] Orc, strength 7; fierce and damage +1 only at 3 wounds on the Ring-bearer
					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul, strength 9; text only on bearers of [wraith] conditions
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7; text only on a weapon played on him
					// Streaming to the Field: a [wraith] condition, no minion.  Its initiative text is beside the point
					// here (the Free Peoples hand is empty) and its ability is not used.
					put("streaming", "8_78");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, strength 6; skirmish ability only
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] support-area condition, cost 0; played to take the hand from four to three
					put("filler1", "1_177");    // Goblin Patrol Troop: no text; only held in the Free Peoples hand to make it up
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StoopingtotheKillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Stooping to the Kill
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you have initiative, each of your [wraith] minions is <b>damage +1</b>.
		 * 	<b>Skirmish:</b> Discard this condition to make a [wraith] minion strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stooping");

		assertEquals("Stooping to the Kill", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileTheShadowHasInitiativeEachWraithMinionIsDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");

		var stooping = scn.GetShadowCard("stooping");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(stooping);

		scn.StartGame();
		// Laid in the fellowship phase so that a Free Peoples play can hand the initiative over (Q-0228).
		scn.MoveMinionsToTable(lemenya, hound, chieftain);
		scn.SetFreepsHand(stairs, filler1, filler2, filler3);

		// Four cards: the Free Peoples player has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));

		// Three cards: the Shadow player has it.
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(stairs);
		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		// A Nazgul and an Orc of the culture; not an Orc of another culture, nor the [wraith] condition itself.
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(chieftain, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(stooping, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Four cards again: the bonus is gone.
		scn.SetFreepsHand(filler1, filler2, filler3, filler4);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
	}

	@Test
	public void SkirmishAbilityDiscardsItToMakeAWraithMinionStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stooping = scn.GetShadowCard("stooping");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");
		var streaming = scn.GetShadowCard("streaming");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(stooping, streaming);
		assertInZone(Zone.SUPPORT, stooping);
		assertInZone(Zone.SUPPORT, streaming);

		scn.StartGame();
		scn.MoveMinionsToTable(hound, mongrel, lemenya, chieftain);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase next door: the Shadow player's assignment actions.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stooping));
		scn.ShadowPass();

		// The Hound and the Chieftain are left out of every skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, mongrel}, new PhysicalCardImpl[]{gimli, lemenya});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(7, scn.GetStrength(mongrel));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.ShadowActionAvailable(stooping));

		scn.ShadowUseCardAction(stooping);
		// The text names no skirmish: the Orc in this skirmish, the Nazgul in the next and the Orc in none are all
		// offered; not the [sauron] Orc, nor the [wraith] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mongrel, lemenya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, streaming));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);

		assertInDiscard(stooping);
		assertEquals(7, scn.GetStrength(hound));
		assertEquals(7, scn.GetStrength(mongrel));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn (8) beats the Mongrel (7), who lives with one wound; in the next skirmish the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityWithNoWraithMinionOnTheTableIsStillOfferedAndItsCostPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stooping = scn.GetShadowCard("stooping");
		var chieftain = scn.GetShadowCard("chieftain");
		var streaming = scn.GetShadowCard("streaming");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(stooping, streaming);
		assertInZone(Zone.SUPPORT, stooping);

		scn.StartGame();
		// A minion of another culture, and a [wraith] card that is no minion: no [wraith] minion anywhere.
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(gimli, chieftain);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.ShadowActionAvailable(stooping));

		scn.ShadowUseCardAction(stooping);
		assertInDiscard(stooping);
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
