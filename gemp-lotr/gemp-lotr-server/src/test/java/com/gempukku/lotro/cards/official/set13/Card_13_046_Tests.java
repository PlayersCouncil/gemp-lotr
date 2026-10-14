package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deagol", "13_46");
					// Sméagol, Slinker: [gollum] companion; to play, add a burden; his skirmish ability is never used
					put("smeagol", "5_29");
					// Smeagol, Once Like Us: the [shire] Smeagol; his assignment ability is never used
					put("shiresmeagol", "103_116");
					// Sam, Son of Hamfast: a Hobbit companion who is no Sméagol; his abilities are never used
					put("sam", "1_311");
					// Aragorn, Heir of Elendil: a companion who is no Hobbit; defender +1 is his whole text
					put("aragorn", "4_109");

					// Hobbit Sword: [shire] possession, hand weapon, "Bearer must be a Hobbit"
					put("sword", "1_299");
					// Phial of Galadriel, Star-glass: [elven] artifact, cost 0, "Bearer must be a Hobbit"; its skirmish
					// ability is never used
					put("phial", "10_13");
					// There and Back Again: [shire] condition that a Hobbit companion may bear; its maneuver ability is
					// never used
					put("tale", "1_317");
					// Ranger's Sword: a possession only Aragorn may bear
					put("rangersword", "1_112");

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 only
					// Morgul Hound: strength 5, vitality 2; +4 and fierce only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Déagol, Fateful Finder
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Follower
		 * Resistance: +1
		 * Game Text: To play, spot Sméagol.
		 * <b>Aid</b> – Add a burden.
		 * <b>Skirmish:</b> If bearer is not assigned to a skirmish, discard this from play to play an artifact or possession from your draw deck on bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("deagol");

		assertEquals("Déagol", card.getBlueprint().getTitle());
		assertEquals("Fateful Finder", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getResistance());
		// The Aid's cost is a burden, which no reading of the card gives: the header carries it.
	}

	@Test
	public void CannotBePlayedUntilSmeagolIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deagol = scn.GetFreepsCard("deagol");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(deagol, smeagol);

		scn.StartGame();

		// Frodo and Sam are no Sméagol.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(deagol));

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsPlayAvailable(deagol));

		scn.FreepsPlayCard(deagol);
		assertInZone(Zone.SUPPORT, deagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedWithTheShireSmeagolSpottedAndItsAidAddsABurdenToGoToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var deagol = scn.GetFreepsCard("deagol");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(shiresmeagol);
		scn.MoveCardsToHand(deagol);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(deagol));
		scn.FreepsPlayCard(deagol);
		assertInZone(Zone.SUPPORT, deagol);

		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(deagol, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, shiresmeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage));

		scn.FreepsChooseCard(shiresmeagol);
		assertAttachedTo(deagol, shiresmeagol);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhileBearerIsUnassignedItIsDiscardedToPlayAnArtifactOrPossessionFromTheDrawDeckOnBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var deagol = scn.GetFreepsCard("deagol");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var phial = scn.GetFreepsCard("phial");
		var tale = scn.GetFreepsCard("tale");
		var rangersword = scn.GetFreepsCard("rangersword");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, deagol);

		scn.StartGame();
		scn.SetFreepsDrawDeck(sword, phial, tale, rangersword);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		// Sam, who bears Déagol, is assigned to no skirmish.
		assertFalse(scn.IsCharAssigned(sam));
		assertAttachedTo(deagol, sam);
		assertInZone(Zone.DECK, sword, phial, tale, rangersword);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(deagol));

		scn.FreepsUseCardAction(deagol);
		assertInDiscard(deagol);
		scn.FreepsDismissRevealedCards();
		// An artifact and a possession Sam may bear; neither a condition he may bear, nor a possession only
		// Aragorn, also in play, may bear.  Looking through a draw deck never forces a card to be taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, phial));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tale, rangersword));
		assertEquals(0, scn.FreepsGetChoiceMin());

		// Frodo is a Hobbit and could bear it too, but it goes on Sam with no question.
		scn.FreepsChooseCardBPFromSelection(sword);
		assertAttachedTo(sword, sam);
		assertTrue(scn.GetAttachedCards(aragorn).isEmpty());
		assertInZone(Zone.DECK, phial, tale, rangersword);
		// The deck was looked through: shuffled as the sword leaves it, and again when the look is over.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileBearerIsAssignedToASkirmishStillToCome() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var deagol = scn.GetFreepsCard("deagol");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, deagol);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, savage}, new PhysicalCardImpl[]{sam, hound});
		scn.FreepsResolveSkirmish(frodo);

		// Sam's skirmish is still to come; Aragorn is assigned to none, but bears nothing.
		assertTrue(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertAttachedTo(deagol, sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(deagol));
	}

	@Test
	public void NotOfferedFromTheSupportAreaWhereItHasNoBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var deagol = scn.GetFreepsCard("deagol");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(deagol);

		scn.StartGame();
		scn.SetFreepsDrawDeck(sword);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		assertInZone(Zone.SUPPORT, deagol);
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(deagol));
	}
}
