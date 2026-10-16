package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wraithworld", "2_86");

					// Twilight Nazgûl of two titles.
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: strength 10; fierce and damage +1 at 2 burdens, read by nothing here
					put("witchking", "2_85");   // The Witch-king, Lord of the Nazgûl: twilight; his trigger answers only his own win, and he fights nobody here
					// Úlairë Lemenya, Lieutenant of Morgul: a Nazgûl who is not twilight, strength 9; weakens only
					// characters bearing a [wraith] condition.
					put("lemenya", "1_232");
					// Corpse Lights: a twilight minion who is no Nazgûl (a [sauron] Wraith); a skirmish ability used
					// only on purpose.
					put("corpselights", "6_99");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring: "Maneuver: Wear The One Ring until the regroup phase", at no cost.
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void WraithworldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Wraith-world
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot a twilight Nazgûl and the Ring-bearer wearing The One Ring to add 3 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wraithworld");

		assertEquals("Wraith-world", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithATwilightNazgulAndTheRingWornAddsThreeBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var card = scn.GetShadowCard("wraithworld");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(card);
		assertInHand(card);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(scn.GetRing());
		assertTrue(scn.RBWearingOneRing());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(card));
		scn.ShadowPlayCard(card);

		assertEquals(3, scn.GetBurdens());
		assertInDiscard(card);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ATwilightNazgulOfAnotherTitleInNoSkirmishIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var card = scn.GetShadowCard("wraithworld");
		var witchking = scn.GetShadowCard("witchking");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(card);
		assertInHand(card);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, lemenya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(scn.GetRing());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(witchking));
		assertTrue(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(card);

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ANazgulWhoIsNotTwilightAndATwilightMinionWhoIsNoNazgulAreNotEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var card = scn.GetShadowCard("wraithworld");
		var lemenya = scn.GetShadowCard("lemenya");
		var corpselights = scn.GetShadowCard("corpselights");
		scn.MoveCardsToHand(card);
		assertInHand(card);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, corpselights);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(scn.GetRing());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(card));
		assertInHand(card);
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void CannotBePlayedWhileTheRingBearerIsNotWearingTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var card = scn.GetShadowCard("wraithworld");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(card);
		assertInHand(card);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(card));
		assertInHand(card);
		assertEquals(0, scn.GetBurdens());
	}
}
