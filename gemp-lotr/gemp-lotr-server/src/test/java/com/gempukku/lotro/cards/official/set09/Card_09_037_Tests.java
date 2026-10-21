package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stone", "9_37");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1 to a [gondor] Man; its response answers wounds only
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: a [rohan] Man, vitality 3; his strength text reads a skirmish
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit), vitality 4
					put("guard1", "1_7");       // Dwarf Guard: cards with no text to hold in hand and to draw
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("guard6", "1_7");
					put("guard7", "1_7");
					put("guard8", "1_7");
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeeingStoneofMinasAnorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Seeing Stone of Minas Anor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: To play, spot a [gondor] Man with 3 or more vitality (or spot a [gondor] Man and add 2 threats).
		 * 	<b>Regroup:</b> Exert a [gondor] Man or discard this artifact to discard your hand and draw 3 cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stone");

		assertEquals("Seeing Stone of Minas Anor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedBySpottingAGondorManWithExactly3VitalityAndNoThreatsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(stone);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		// Frodo and Aragorn: room for 2 threats, and 1 is laid, so the parenthesis cannot be paid.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetVitality(aragorn));
		assertEquals(1, scn.GetThreats());
		assertInHand(stone);
		assertTrue(scn.FreepsPlayAvailable(stone));

		scn.FreepsPlayCard(stone);

		// No question: the one way open is taken.
		assertInZone(Zone.SUPPORT, stone);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBothWaysOpenThePlayerIsAskedAndMayAdd2Threats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(stone);

		assertInHand(stone);
		scn.StartGame();

		assertEquals(4, scn.GetVitality(aragorn));
		assertEquals(0, scn.GetThreats());
		scn.FreepsPlayCard(stone);

		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("3 or more vitality"));
		assertTrue(scn.FreepsChoiceAvailable("add 2 threats"));
		scn.FreepsChooseOption("add 2 threats");

		assertInZone(Zone.SUPPORT, stone);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheGondorManAt2VitalityThe2ThreatsArePaidThoughOtherCardsHave3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var eomer = scn.GetFreepsCard("eomer");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, eomer, pippin);
		scn.MoveCardsToHand(stone);
		scn.AddWoundsToChar(aragorn, 2);

		assertInHand(stone);
		scn.StartGame();

		// A [rohan] Man and a [gondor] Hobbit with 3 vitality or more do not stand for a [gondor] Man.
		assertEquals(2, scn.GetVitality(aragorn));
		assertEquals(3, scn.GetVitality(eomer));
		assertEquals(4, scn.GetVitality(pippin));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(stone));

		scn.FreepsPlayCard(stone);

		assertInZone(Zone.SUPPORT, stone);
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheGondorAllyIsSpottedForTheThreatsWay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var merchant = scn.GetFreepsCard("merchant");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveAlliesToTable(merchant);
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(stone);

		assertInHand(stone);
		scn.StartGame();

		assertEquals(1, scn.GetVitality(merchant));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(stone));

		scn.FreepsPlayCard(stone);

		assertInZone(Zone.SUPPORT, stone);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithTheGondorManAt2VitalityAndNoRoomFor2Threats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(stone);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		// Two companions: 1 threat laid leaves room for 1 more, not 2.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetVitality(aragorn));
		assertEquals(1, scn.GetThreats());
		assertInHand(stone);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stone));
	}

	@Test
	public void CannotBePlayedWithoutAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var eomer = scn.GetFreepsCard("eomer");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(eomer, pippin);
		scn.MoveCardsToHand(stone);

		assertInHand(stone);
		scn.StartGame();

		// Room for 2 threats, and a Man and a [gondor] companion of 3 vitality or more, but no [gondor] Man.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stone));
	}

	@Test
	public void RegroupAbilityExertsAGondorManOrDiscardsItselfToDiscardTheHandAndDraw3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var eomer = scn.GetFreepsCard("eomer");
		var pippin = scn.GetFreepsCard("pippin");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionsToTable(aragorn, eomer, pippin);
		scn.MoveAlliesToTable(merchant);
		// Narsil gives the Merchant the vitality to exert.
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(guard1, guard2);

		var shadowGuard = scn.GetShadowCard("guard1");
		scn.MoveCardsToHand(shadowGuard);

		assertInZone(Zone.SUPPORT, stone);
		assertInZone(Zone.ATTACHED, narsil);
		assertInHand(guard1, guard2, shadowGuard);
		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetVitality(merchant));
		assertEquals(2, scn.GetFreepsHandCount());
		var top1 = scn.GetFromTopOfFreepsDeck(1);
		var top2 = scn.GetFromTopOfFreepsDeck(2);
		var top3 = scn.GetFromTopOfFreepsDeck(3);
		var top4 = scn.GetFromTopOfFreepsDeck(4);
		assertTrue(scn.FreepsActionAvailable(stone));

		scn.FreepsUseCardAction(stone);

		// Both prices can be paid: the player is asked which.
		assertEquals(java.util.List.of("Exert a GONDOR Man", "Discard this artifact"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Exert a GONDOR Man");
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eomer, pippin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, stone);
		assertInDiscard(guard1, guard2);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(top1, top2, top3);
		assertInHand(shadowGuard);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// Again, by the other price: the artifact goes, nobody exerts.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(stone));
		scn.FreepsUseCardAction(stone);
		scn.FreepsChooseOption("Discard this artifact");

		assertInDiscard(stone);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertInDiscard(top1, top2, top3);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(top4);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithTheOnlyGondorManExhaustedTheArtifactIsDiscardedUnaskedAndAnEmptyHandDraws3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionsToTable(aragorn, eomer);

		assertInZone(Zone.SUPPORT, stone);
		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.AddWoundsToChar(aragorn, 3);
		scn.FreepsRefreshDecision();

		// Aragorn has 1 vitality left; Eomer can exert but is no [gondor] Man.
		assertEquals(1, scn.GetVitality(aragorn));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		var top1 = scn.GetFromTopOfFreepsDeck(1);
		var top2 = scn.GetFromTopOfFreepsDeck(2);
		var top3 = scn.GetFromTopOfFreepsDeck(3);
		assertTrue(scn.FreepsActionAvailable(stone));

		scn.FreepsUseCardAction(stone);

		assertInDiscard(stone);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(top1, top2, top3);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionsToTable(aragorn);

		var runner = scn.GetShadowCard("runner");

		assertInZone(Zone.SUPPORT, stone);
		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(stone));
	}
}
