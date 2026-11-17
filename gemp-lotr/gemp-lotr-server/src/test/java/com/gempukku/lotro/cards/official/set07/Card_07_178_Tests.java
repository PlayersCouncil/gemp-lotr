package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_178_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("clutches", "7_178");
					put("clutches2", "7_178");  // a second copy: an event in the hand, discarded to pay
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, strength 9; her text reads [wraith] conditions on companions
					put("nertea", "1_234");     // Úlairë Nertëa, Messenger of Dol Guldur: [wraith] Nazgûl, strength 9; a play trigger only
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, strength 12, fierce; his text needs 3 or 5 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl, strength 5; its text needs 2 wounds on the Ring-bearer
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text, for the hand
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession with no other text, for the hand
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; his text is about being overwhelmed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FoulClutchesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Foul Clutches
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Discard 3 cards from hand to make a Nazgûl strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("clutches");

		assertEquals("Foul Clutches", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardingThreeCardsFromHandMakesANazgulStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clutches = scn.GetShadowCard("clutches");
		var clutches2 = scn.GetShadowCard("clutches2");
		var troop = scn.GetShadowCard("troop");
		var sword = scn.GetShadowCard("sword");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Four cards beside the event: a minion, an event, and a Free Peoples possession and companion.
		scn.MoveCardsToHand(clutches, clutches2, troop, sword, shadowAragorn);
		assertInHand(clutches, clutches2, troop, sword, shadowAragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		// The Morgul Hound joins Lemenya against Aragorn; Khamûl is left unassigned.
		scn.ShadowAssignToMinions(aragorn, hound);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, lemenya));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, hound));
		assertFalse(scn.IsCharAssigned(khamul));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.ShadowPlayAvailable(clutches));

		scn.ShadowPlayCard(clutches);
		// The cost: any 3 of the 4 other cards, the event itself not among them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(clutches2, troop, sword, shadowAragorn));
		assertEquals(3, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(clutches2, troop, sword);
		assertInDiscard(clutches2, troop, sword);
		assertInHand(shadowAragorn);

		// A Nazgûl of either culture, in the skirmish or not; not the [wraith] Orc beside them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertEquals(12, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound));
		assertInDiscard(clutches);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlayedOnANazgulWhoseSkirmishIsStillToComeItLastsOnlyThisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clutches = scn.GetShadowCard("clutches");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(clutches, scn.GetShadowCard("clutches2"), scn.GetShadowCard("troop"), scn.GetShadowCard("sword"));
		assertInHand(clutches, scn.GetShadowCard("clutches2"), scn.GetShadowCard("troop"), scn.GetShadowCard("sword"));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nertea);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, nertea});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Played on Nertëa, whose skirmish with Boromir is still to come, while Lemenya fights Aragorn.
		assertEquals(9, scn.GetStrength(nertea));
		scn.ShadowPlayCard(clutches);
		// The three other cards are all there is: they go unasked.
		assertInDiscard(scn.GetShadowCard("clutches2"), scn.GetShadowCard("troop"), scn.GetShadowCard("sword"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nertea));
		scn.ShadowChooseCard(nertea);
		assertEquals(12, scn.GetStrength(nertea));
		assertEquals(9, scn.GetStrength(lemenya));

		// Still so at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(12, scn.GetStrength(nertea));

		// Lemenya, 9 against 8, wins.  Nertëa's own skirmish is another phase: the bonus is gone.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(9, scn.GetStrength(nertea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoNazgulInPlayItIsPlayedAndPaidForForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clutches = scn.GetShadowCard("clutches");
		var clutches2 = scn.GetShadowCard("clutches2");
		var troop = scn.GetShadowCard("troop");
		var sword = scn.GetShadowCard("sword");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(clutches, clutches2, troop, sword);
		assertInHand(clutches, clutches2, troop, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hound);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(clutches));

		scn.ShadowPlayCard(clutches);
		// The other three cards are all there is: they go unasked.
		assertInDiscard(clutches, clutches2, troop, sword);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyTwoOtherCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clutches = scn.GetShadowCard("clutches");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(clutches, scn.GetShadowCard("clutches2"), scn.GetShadowCard("troop"));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Three cards in hand, the event one of them: it cannot pay for itself.
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(clutches));

		// A third other card: now it can.
		scn.MoveCardsToHand(scn.GetShadowCard("sword"));
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(clutches));
		assertEquals(9, scn.GetStrength(lemenya));
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clutches = scn.GetShadowCard("clutches");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(clutches, scn.GetShadowCard("clutches2"), scn.GetShadowCard("troop"), scn.GetShadowCard("sword"));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);

		// The Shadow player's assignment actions, with three cards to discard and a Nazgûl in play.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(4, scn.GetShadowHandCount());
		assertFalse(scn.ShadowPlayAvailable(clutches));
		scn.ShadowPass();

		// In the skirmish it is.
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(clutches));
	}
}
