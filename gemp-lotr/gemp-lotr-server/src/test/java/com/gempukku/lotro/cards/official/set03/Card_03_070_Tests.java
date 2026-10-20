package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servants1", "3_70");
					put("servants2", "3_70");

					put("retainer", "3_57");    // Isengard Retainer: [isengard] Orc, strength 8, vitality 3; a regroup ability only
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; a regroup ability only
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6; a Shadow ability only
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, damage +1 only
					put("scout", "1_191");      // Moria Scout: a [moria] Orc, strength 6; a trigger on its own play only
					put("filler1", "1_185");    // Goblin Warrior: [moria] minion, only ever held in hand here
					put("filler2", "1_185");
					put("filler3", "1_185");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ServantstoSarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Servants to Saruman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an [isengard] Orc strength +2 (or +3 if you have fewer than 3 cards in hand).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("servants1");

		assertEquals("Servants to Saruman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnIsengardOrcPlusTwoFromThreeCardsInHandAndPlusThreeFromTwoForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var servants1 = scn.GetShadowCard("servants1");
		var servants2 = scn.GetShadowCard("servants2");
		var retainer = scn.GetShadowCard("retainer");
		var servant = scn.GetShadowCard("servant");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");

		scn.StartGame();
		scn.MoveCardsToHand(servants1, servants2, filler1, filler2);
		scn.MoveMinionsToTable(retainer, servant, forger, savage, scout);
		assertInZone(Zone.SHADOW_CHARACTERS, retainer, servant, forger, savage, scout);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, retainer}, new PhysicalCardImpl[]{gimli, servant});
		// The Forger, the Savage and the Scout stay out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(retainer));
		assertEquals(7, scn.GetStrength(servant));
		assertEquals(6, scn.GetStrength(forger));
		assertTrue(scn.ShadowPlayAvailable(servants1));

		scn.ShadowPlayCard(servants1);
		// An [isengard] Orc in this skirmish, in one still to come or in none; not an [isengard] Uruk-hai, nor a
		// [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(retainer, servant, forger));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, scout, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(retainer);

		// The event is in the void while it resolves (CR 4.2 "playing a card"): three cards are left in hand, which
		// is not fewer than 3, so +2.  The Free Peoples player's empty hand is not "you".
		assertInDiscard(servants1);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(10, scn.GetStrength(retainer));
		assertEquals(7, scn.GetStrength(servant));
		assertEquals(6, scn.GetStrength(forger));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();

		scn.ShadowPlayCard(servants2);
		scn.ShadowChooseCard(servant);

		// Two cards are left while the second copy resolves: +3 for the Servant, whose skirmish is still to come.
		// The Retainer's +2 was set as the first copy resolved and does not grow now that the hand is smaller.
		assertInDiscard(servants2);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(10, scn.GetStrength(servant));
		assertEquals(10, scn.GetStrength(retainer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still in force at the next decision of this skirmish; gone once the next skirmish has begun.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(retainer));
		assertEquals(10, scn.GetStrength(servant));
		scn.ShadowPass();

		// Aragorn's 8 loses to 10.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(retainer));
		assertEquals(7, scn.GetStrength(servant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayedBeforeTheSkirmishesAndMakesAnOrcInNoSkirmishPlusThreeFromAnEmptyHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var ffiller1 = scn.GetFreepsCard("filler1");
		var ffiller2 = scn.GetFreepsCard("filler2");
		var ffiller3 = scn.GetFreepsCard("filler3");
		scn.MoveCompanionsToTable(aragorn);

		var servants1 = scn.GetShadowCard("servants1");
		var retainer = scn.GetShadowCard("retainer");
		var forger = scn.GetShadowCard("forger");

		scn.StartGame();
		scn.MoveCardsToHand(servants1);
		// Three cards in the Free Peoples hand: his is not the hand the text counts.
		scn.MoveCardsToHand(ffiller1, ffiller2, ffiller3);
		scn.MoveMinionsToTable(retainer, forger);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(3, scn.GetFreepsHandCount());

		// Not in the assignment phase, the Shadow player's last chance to act before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(servants1));
		scn.ShadowPass();

		scn.FreepsAssignAndResolve(aragorn, retainer);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(forger));
		assertEquals(6, scn.GetStrength(forger));
		assertTrue(scn.ShadowPlayAvailable(servants1));

		scn.ShadowPlayCard(servants1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(retainer, forger));
		scn.ShadowChooseCard(forger);

		// No card left in hand while it resolves: +3, though the Forger skirmishes nobody (the text says "an
		// [isengard] Orc", not a skirmishing one).
		assertInDiscard(servants1);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(9, scn.GetStrength(forger));
		assertEquals(8, scn.GetStrength(retainer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
