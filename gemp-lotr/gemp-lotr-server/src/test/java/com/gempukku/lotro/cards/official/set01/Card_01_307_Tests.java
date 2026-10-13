package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_307_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "1_307");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability
					put("merry", "1_303");        // Merry, From O'er the Brandywine: a companion; his text needs a weapon
					// Hobbit Party Guest: a Hobbit ally; heals himself at the start of the turn, so he is wounded after it
					put("guest", "1_297");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Pippin, Hobbit of Some Intelligence
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: When you play Pippin, remove a burden or wound from a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Hobbit of Some Intelligence", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void WithBurdensAndWoundsBothOnHandRemovingABurdenLeavesEveryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pippin);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);

		assertEquals(2, scn.GetBurdens());
		scn.FreepsPlayCard(pippin);

		// Required: no offer, only the choice of which to remove.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsChoiceAvailable("Remove a burden"));
		assertTrue(scn.FreepsChoiceAvailable("Heal a companion"));
		scn.FreepsChoose("Remove a burden");

		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBurdensAndWoundsBothOnHandOneWoundComesOffOneCompanionOfAnyKind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guest = scn.GetFreepsCard("guest");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(guest);
		scn.MoveCardsToHand(pippin);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guest, 1);

		scn.FreepsPlayCard(pippin);
		scn.FreepsChoose("Heal a companion");

		// The Ring-bearer, a [shire] Hobbit, and a [gondor] Man; not a wounded ally, nor Pippin with no wound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guest, pippin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// One wound off the one chosen, of the two he had.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(guest));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoBurdenAWoundComesOffWithoutTheChoiceBeingPut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pippin);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPlayCard(pippin);

		// No burden to remove: the wound is what comes off, and Aragorn is the one companion wounded.
		assertFalse(scn.FreepsChoiceAvailable("Remove a burden"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNobodyWoundedPippinRemovesABurdenUnaskedAndAnotherCompanionPlayedAfterHimRemovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var guest = scn.GetFreepsCard("guest");
		scn.MoveAlliesToTable(guest);
		scn.MoveCardsToHand(pippin, merry);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddWoundsToChar(guest, 1);

		// No companion is wounded (only an ally is): the burden is what comes off, unasked.
		scn.FreepsPlayCard(pippin);
		assertFalse(scn.FreepsChoiceAvailable("Heal a companion"));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(guest));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another companion played while Pippin is in play: nothing is asked and nothing removed.
		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(guest));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ChosenIntoTheStartingFellowshipPippinRemovesTheBidBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");

		// Player 1 bid 1 burden, and is choosing his starting fellowship.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(1, scn.FreepsGetBurdens());

		scn.FreepsChooseCard(pippin);

		// CR 4.2 "starting fellowship": "When you play" text is used on a starting companion.
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(0, scn.FreepsGetBurdens());
		// Merry, at cost 1, still fits within the 4 a starting fellowship may cost: player 1 chooses on.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
	}
}
