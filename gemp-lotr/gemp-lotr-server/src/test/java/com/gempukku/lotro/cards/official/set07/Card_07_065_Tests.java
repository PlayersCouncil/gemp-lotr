package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("never", "7_65");
					// Sméagol, Poor Creature: the [gollum] Sméagol; to play, add a burden; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol; an assignment ability used only on purpose
					put("smeagol2", "103_116");
					// Aragorn, Ranger of the North: a companion who is no Sméagol; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Don't Look at Them: a [gollum] card in play that is no Sméagol; a skirmish ability used only on purpose
					put("look", "6_39");
					// Goblin Runner: a minion on the table, so that there is a maneuver phase; a play trigger only
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NeverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Never
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot Sméagol.<br><b>Regroup:</b> Discard Sméagol to remove 2 threats.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("never");

		assertEquals("Never", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilSmeagolIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var never = scn.GetFreepsCard("never");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var look = scn.GetFreepsCard("look");
		// A companion who is no Sméagol, and a [gollum] card that is no Sméagol.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(look);
		scn.MoveCardsToHand(never, smeagol);
		assertInZone(Zone.SUPPORT, look);
		assertInHand(never, smeagol);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(never));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsPlayAvailable(never));

		scn.FreepsPlayCard(never);
		assertInZone(Zone.SUPPORT, never);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SmeagolOnceLikeUsIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var never = scn.GetFreepsCard("never");
		var smeagol2 = scn.GetFreepsCard("smeagol2");
		scn.MoveCompanionsToTable(smeagol2);
		scn.MoveCardsToHand(never);
		assertInHand(never);

		scn.StartGame();

		// The [shire] Sméagol is a Sméagol as the [gollum] one is.
		assertTrue(scn.FreepsPlayAvailable(never));
		scn.FreepsPlayCard(never);
		assertInZone(Zone.SUPPORT, never);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsSmeagolToRemoveTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var never = scn.GetFreepsCard("never");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(never);
		assertInZone(Zone.SUPPORT, never);

		scn.StartGame();
		// Three companions, Frodo, Sméagol and Aragorn, so that three threats can be laid.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		// No minion is in play, so the game goes from the move straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, smeagol, aragorn);
		assertTrue(scn.FreepsActionAvailable(never));

		// Sméagol is the only Sméagol: he is discarded without a choice, and Aragorn stays.
		scn.FreepsUseCardAction(never);
		assertInZone(Zone.DISCARD, smeagol);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// 3 - 2: two removed, not all of them.
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, never);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void SmeagolOnceLikeUsIsDiscardedToRemoveTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var never = scn.GetFreepsCard("never");
		var smeagol2 = scn.GetFreepsCard("smeagol2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol2, aragorn);
		scn.MoveCardsToSupportArea(never);
		assertInZone(Zone.SUPPORT, never);

		scn.StartGame();
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.FREE_CHARACTERS, smeagol2, aragorn);
		assertTrue(scn.FreepsActionAvailable(never));

		scn.FreepsUseCardAction(never);
		assertInZone(Zone.DISCARD, smeagol2);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var never = scn.GetFreepsCard("never");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(never);
		assertInZone(Zone.SUPPORT, never);

		scn.StartGame();
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(never));
	}

	@Test
	public void NotOfferedWithNoSmeagolToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var never = scn.GetFreepsCard("never");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Aragorn could be discarded, and is no Sméagol.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(never);
		assertInZone(Zone.SUPPORT, never);

		scn.StartGame();
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(never));
	}
}
