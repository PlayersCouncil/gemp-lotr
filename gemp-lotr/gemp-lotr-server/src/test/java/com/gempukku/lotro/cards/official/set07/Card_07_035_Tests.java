package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.common.Zone;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hope", "7_35");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus and nothing else
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: Defender +1 and no other text; bears Shadow cards here
					// Cards in hand to discard, of three types and cultures.
					put("elf", "1_53");         // Lorien Elf: an [elven] companion
					put("axe", "1_9");          // Dwarven Axe: a [dwarven] possession
					put("deftness", "1_293");   // Halfling Deftness: a [shire] Skirmish event
					put("walls", "7_111");      // Man the Walls: a Free Peoples ([gondor]) condition in the support area

					put("tidings", "7_130");    // Dark Tidings: [raider] condition, support area, a regroup ability
					put("tidings2", "7_130");
					put("tidings3", "7_130");
					put("beetles", "10_65");    // Swarming Like Beetles: [wraith] condition, support area, a maneuver ability
					put("wielding", "11_105");  // Wielding the Ring: [men] condition borne by a companion, no other text
					put("poison", "8_28");      // Spider Poison: a Shadow possession borne by a companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FoolsHopeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Fool's Hope
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot Gandalf and discard 2 cards from hand to make an opponent discard all but 2 Shadow conditions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hope");

		assertEquals("Fool's Hope", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var elf = scn.GetFreepsCard("elf");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(hope, gandalf, elf, axe);

		scn.StartGame();
		scn.MoveCardsToSupportArea(scn.GetShadowCard("tidings"));
		scn.FreepsRefreshDecision();

		// A [gandalf] companion of another title; two cards to discard beside the event.
		assertFalse(scn.FreepsPlayAvailable(hope));

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(hope));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyOneOtherCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elf = scn.GetFreepsCard("elf");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(hope, elf);

		scn.StartGame();
		scn.MoveCardsToSupportArea(scn.GetShadowCard("tidings"));
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsPlayAvailable(hope));

		scn.MoveCardsToHand(axe);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(hope));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOpponentKeepsTwoShadowConditionsOfHisChoiceAndDiscardsTheRest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elf = scn.GetFreepsCard("elf");
		var axe = scn.GetFreepsCard("axe");
		var deftness = scn.GetFreepsCard("deftness");
		var walls = scn.GetFreepsCard("walls");
		var tidings = scn.GetShadowCard("tidings");
		var tidings2 = scn.GetShadowCard("tidings2");
		var tidings3 = scn.GetShadowCard("tidings3");
		var beetles = scn.GetShadowCard("beetles");
		var wielding = scn.GetShadowCard("wielding");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(walls);
		scn.MoveCardsToHand(hope, elf, axe, deftness);
		assertInHand(elf, axe, deftness);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tidings, tidings2, tidings3, beetles);
		scn.AttachCardsTo(aragorn, wielding, poison);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, tidings, tidings2, tidings3, beetles, walls);
		assertAttachedTo(wielding, aragorn);
		assertAttachedTo(poison, aragorn);

		assertTrue(scn.FreepsPlayAvailable(hope));
		scn.FreepsPlayCard(hope);

		// Any two cards from hand: a companion, a possession and an event are all offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, axe, deftness));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(elf, axe);
		assertInDiscard(elf, axe);
		assertInHand(deftness);

		// The opponent chooses the two to keep: Shadow conditions of three cultures, in the support area and on a
		// companion.  Not his Shadow possession, not a Free Peoples condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tidings, tidings2, tidings3, beetles, wielding));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poison, walls));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(beetles, wielding);

		// Five Shadow conditions, all but two discarded: three go, which is neither "2" nor "1".
		assertInDiscard(tidings, tidings2, tidings3, hope);
		assertInZone(Zone.SUPPORT, beetles, walls);
		assertAttachedTo(wielding, aragorn);
		assertAttachedTo(poison, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyTwoShadowConditionsTheEventIsPlayedAndNoneIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elf = scn.GetFreepsCard("elf");
		var axe = scn.GetFreepsCard("axe");
		var tidings = scn.GetShadowCard("tidings");
		var wielding = scn.GetShadowCard("wielding");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(hope, elf, axe);
		assertInHand(elf, axe);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tidings);
		scn.AttachCardsTo(aragorn, wielding, poison);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, tidings);
		assertAttachedTo(wielding, aragorn);
		assertAttachedTo(poison, aragorn);

		// With two cards beside the event in hand, both are discarded unasked.
		scn.FreepsPlayCard(hope);

		// All but 2 of 2 is none.
		assertInDiscard(elf, axe, hope);
		assertInZone(Zone.SUPPORT, tidings);
		assertAttachedTo(wielding, aragorn);
		assertAttachedTo(poison, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
