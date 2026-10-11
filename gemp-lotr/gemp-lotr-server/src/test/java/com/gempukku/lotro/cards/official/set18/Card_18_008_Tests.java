package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_8");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRestoreScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armaments", "18_8");
					// Legolas, Greenleaf: [elven] companion, strength 6, vitality 3; an archer, with an archery
					// ability.  No test here has an archery phase with a minion in it.
					put("legolas", "1_50");
					// Elven Bow: possession, ranged weapon, cost 1.  "Bearer must be an Elf.  Bearer is an archer."
					put("bow", "1_41");
					// Elven Cloak: possession, cloak, cost 1.  "To play, spot an Elf.  Bearer must be a companion."
					put("cloak", "1_42");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenArmamentsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Elven Armaments
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon/shield
		 * Strength: 2
		 * Vitality: 1
		 * Game Text: To play, bearer must not be bearing any possessions. Bearer must be an [elven] companion.<br>Bearer cannot bear any other possessions.<br>While a unique companion bears this possession, the Free Peoples player may not use archery special abilities and the minion archery total is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Elven Armaments", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void ElvenArmamentsTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	// Comprehensive Rules 5.0, "hinder": while Elven Armaments is hindered its text does not apply, so its bearer
	// may take up another possession.  Its text has a bar and no "discard" sentence, so when it is restored
	// nothing is discarded: "because it only has the cannot-bear clause and lacks a discard clause, it will have
	// to begrudgingly co-exist with other possessions played while it was hindered" (Ketura on Q-0223: B).  The
	// restore here is the Free Peoples player's own reconcile.
	@Test
	public void APossessionItsBearerTookUpWhileItWasHinderedStaysWhenItIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRestoreScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, armaments);
		// The Cloak stays in hand, so that the reconcile has a discard to ask about
		scn.MoveCardsToHand(bow, cloak);

		scn.StartGame();

		// "Bearer cannot bear any other possessions": Legolas is the only Elf
		assertEquals(6 + 2, scn.GetStrength(legolas));
		assertFalse(scn.FreepsPlayAvailable(bow));

		scn.HinderCard(armaments);
		scn.FreepsRefreshDecision();
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, legolas);
		assertTrue(scn.IsHindered(armaments));

		// No minion: the Shadow phase is followed by the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.IsHindered(armaments));

		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		// Restored, with its bonus back, and both possessions still on him
		assertFalse(scn.IsHindered(armaments));
		assertEquals(6 + 2, scn.GetStrength(legolas));
		assertAttachedTo(armaments, legolas);
		assertAttachedTo(bow, legolas);
		assertInZone(Zone.HAND, cloak);
	}
}
