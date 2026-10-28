package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rumil", "12_22");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: another Elf companion, no archer; +3 only against a Nazgûl
					put("defiance", "1_37");    // Defiance: an [elven] skirmish event, for the top of the deck
					put("lorien", "1_53");      // Lórien Elf: an [elven] companion card, for the top of the deck
					put("strike", "1_3");       // Axe Strike: a [dwarven] skirmish event, no [elven] card
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RumilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Rúmil, Brother of Haldir
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Maneuver:</b> Reveal the top card of your draw deck. If it is an [elven] card, Rúmil is an <b>archer</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");

		assertEquals("Rúmil", rumil.getBlueprint().getTitle());
		assertEquals("Brother of Haldir", rumil.getBlueprint().getSubtitle());
		assertTrue(rumil.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, rumil.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, rumil.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, rumil.getBlueprint().getCardType());
		assertEquals(Race.ELF, rumil.getBlueprint().getRace());
		assertEquals(2, rumil.getBlueprint().getTwilightCost());
		assertEquals(6, rumil.getBlueprint().getStrength());
		assertEquals(3, rumil.getBlueprint().getVitality());
		assertEquals(6, rumil.getBlueprint().getResistance());
	}

	@Test
	public void AnElvenEventRevealedMakesRumilAnArcherUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		var strike = scn.GetFreepsCard("strike");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(rumil, arwen);

		scn.StartGame();
		// The last card named is the top card: Defiance, with Axe Strike under it.
		scn.MoveCardsToTopOfDeck(strike, defiance);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(defiance, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertTrue(scn.FreepsActionAvailable(rumil));

		scn.FreepsUseCardAction(rumil);
		// The top card is shown to both players, and the card under it to neither.
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(strike, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(defiance, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(strike, false));
		scn.DismissRevealedCards();

		// Rúmil is an archer, and the other Elf companion is not.  The card revealed stays on top, and the deck is
		// not shuffled: a look at the top card is no search.
		assertTrue(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertEquals(defiance, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Already an archer, he is offered the ability again.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(rumil));

		// The archery phase: he is the fellowship's one archer.
		scn.FreepsPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// Still an archer in the skirmish.
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(rumil, Keyword.ARCHER));
		scn.FreepsAssignToMinions(arwen, troop);
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.HasKeyword(rumil, Keyword.ARCHER));

		// Gone as the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));
	}

	@Test
	public void AnElvenCompanionCardRevealedFromADeckOfOneMakesRumilAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var lorien = scn.GetFreepsCard("lorien");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(rumil);

		scn.StartGame();
		scn.SetFreepsDrawDeck(lorien);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(lorien, scn.GetFreepsTopOfDeck());
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertTrue(scn.FreepsActionAvailable(rumil));

		scn.FreepsUseCardAction(rumil);
		scn.DismissRevealedCards();

		assertTrue(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(lorien, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACardOfAnotherCultureRevealedMakesNoArcherThoughAnElvenCardLiesUnderIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var defiance = scn.GetFreepsCard("defiance");
		var strike = scn.GetFreepsCard("strike");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(rumil);

		scn.StartGame();
		// Axe Strike on top, Defiance under it.
		scn.MoveCardsToTopOfDeck(defiance, strike);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(strike, scn.GetFreepsTopOfDeck());
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertTrue(scn.FreepsActionAvailable(rumil));

		scn.FreepsUseCardAction(rumil);
		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, false));
		scn.DismissRevealedCards();

		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertEquals(strike, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// In the archery phase the fellowship has no archer.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(rumil);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Nothing to reveal, so nothing for the rest of the text to read (Q-0148).
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rumil));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var defiance = scn.GetFreepsCard("defiance");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(rumil);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(defiance);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(defiance, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rumil));
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));
	}
}
