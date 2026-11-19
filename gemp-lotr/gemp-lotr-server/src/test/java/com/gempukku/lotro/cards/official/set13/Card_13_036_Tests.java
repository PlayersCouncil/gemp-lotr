package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("palantir", "13_36");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					put("ent", "6_27");         // Ent Avenger: a [gandalf] companion who is no Gandalf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion

					put("orthanc", "3_67");     // The Palantír of Orthanc: [isengard] artifact, support area
					put("seventh", "4_166");    // The Palantír of Orthanc, Seventh Seeing-stone: the same title
					put("library", "9_39");     // Library of Orthanc: [isengard] artifact of another title, support area
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("seeking", "1_276");    // Seeking Its Master: [sauron] condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThePalantirofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: The Palantír of Orthanc, Recovered Seeing Stone
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype:
		 * Resistance: -2
		 * Game Text: Bearer must be Gandalf. To play this, if you can spot The Palantír of Orthanc, discard it.<br>At the start of the fellowship phase, you may add a burden to reveal a Shadow player's hand. He or she must choose a revealed minion and discard it from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("palantir");

		assertEquals("The Palantír of Orthanc", card.getBlueprint().getTitle());
		assertEquals("Recovered Seeing Stone", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(-2, card.getBlueprint().getResistance());
	}

	@Test
	public void NotPlayableWithoutGandalfAndPlayedOnHimLeavingAnotherIsengardArtifactInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetFreepsCard("palantir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(ent, aragorn);
		scn.MoveCardsToHand(palantir);

		var library = scn.GetShadowCard("library");
		scn.MoveCardsToSupportArea(library);
		assertInZone(Zone.SUPPORT, library);

		scn.StartGame();

		// A [gandalf] companion who is no Gandalf, and a companion of another culture: no bearer.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(palantir));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(palantir));
		scn.FreepsPlayCard(palantir);
		assertAttachedTo(palantir, gandalf);

		// No Palantír of Orthanc to spot: the Library of Orthanc stays.
		assertInZone(Zone.SUPPORT, library);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayersPalantirOfOrthancIsDiscardedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetFreepsCard("palantir");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(palantir);

		var orthanc = scn.GetShadowCard("orthanc");
		var library = scn.GetShadowCard("library");
		scn.MoveCardsToSupportArea(orthanc, library);
		assertInZone(Zone.SUPPORT, orthanc, library);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(palantir));
		scn.FreepsPlayCard(palantir);
		assertAttachedTo(palantir, gandalf);
		assertInDiscard(orthanc);
		assertInZone(Zone.SUPPORT, library);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APalantirOfOrthancWithAnotherSubtitleIsDiscardedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetFreepsCard("palantir");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(palantir);

		var seventh = scn.GetShadowCard("seventh");
		scn.MoveCardsToSupportArea(seventh);
		assertInZone(Zone.SUPPORT, seventh);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(palantir));
		scn.FreepsPlayCard(palantir);
		assertAttachedTo(palantir, gandalf);
		assertInDiscard(seventh);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseABurdenRevealsTheShadowHandAndTheShadowPlayerDiscardsAMinionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetFreepsCard("palantir");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, palantir);
		assertAttachedTo(palantir, gandalf);

		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		var seeking = scn.GetShadowCard("seeking");
		var shadowEnt = scn.GetShadowCard("ent");

		scn.StartGame();
		scn.SetShadowHand(runner, brigand, seeking, shadowEnt);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(palantir));
		assertEquals(0, scn.GetBurdens());
		assertEquals(4, scn.GetShadowHandCount());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());

		// The whole hand is shown to the Free Peoples player, and its owner is shown nothing.
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(brigand, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(seeking, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowEnt, false));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDismissRevealedCards();

		// The Shadow player chooses one minion: not a condition, nor a companion.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, brigand));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(seeking, shadowEnt));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(brigand);
		assertInDiscard(brigand);
		assertInZone(Zone.HAND, runner, seeking, shadowEnt);
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningAddsNoBurdenAndRevealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetFreepsCard("palantir");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, palantir);
		assertAttachedTo(palantir, gandalf);

		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SetShadowHand(runner, brigand);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(palantir));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.HAND, runner, brigand);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoMinionInTheShadowHandTheBurdenIsStillPaidAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetFreepsCard("palantir");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, palantir);
		assertAttachedTo(palantir, gandalf);

		var seeking = scn.GetShadowCard("seeking");
		var shadowEnt = scn.GetShadowCard("ent");

		scn.StartGame();
		scn.SetShadowHand(seeking, shadowEnt);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(palantir));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.HAND, seeking, shadowEnt);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedAtTheStartOfTheManeuverPhaseAndOfferedAgainAtTheNextFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetFreepsCard("palantir");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, palantir);
		assertAttachedTo(palantir, gandalf);

		var runner = scn.GetShadowCard("runner");
		var seeking = scn.GetShadowCard("seeking");

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(palantir));
		scn.FreepsDeclineOptionalTrigger();
		scn.MoveMinionsToTable(runner);

		// The start of the maneuver phase: nothing is offered.
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(palantir));

		// The player's next fellowship phase, on turn 3: offered again.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.SetShadowHand(seeking);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(palantir));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertInZone(Zone.HAND, seeking);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
