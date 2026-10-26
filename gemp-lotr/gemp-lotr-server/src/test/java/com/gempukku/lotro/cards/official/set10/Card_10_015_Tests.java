package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brooding", "10_15");

					// The [gandalf] companions, and the cards that nearly are.
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; a strength bonus only
					put("ent", "6_27");         // Ent Avenger: a [gandalf] Ent companion; a strength bonus with 3 Ents only
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability only

					// Elendil's Valor: Maneuver event, cost 0: "Make a [gondor] companion defender +1 until the regroup phase."
					// Aragorn is the only [gondor] companion on each board, and is chosen unasked.
					// Each play takes a card from the hand.
					put("valor", "1_103");
					put("valor2", "1_103");
					put("valor3", "1_103");
					// Stairs of Khazad-dum: [dwarven] condition, cost 0, to the support area; played to take the hand
					// from four to three in the fellowship phase.
					put("stairs", "1_24");
					// Dwarven Armor: cards to hold in hand and nothing else (no Dwarf to bear one).
					put("filler1", "1_8");
					put("filler2", "1_8");
					put("filler3", "1_8");
					put("filler4", "1_8");

					// The Free Peoples player's own condition.
					put("walkers", "1_79");     // The Nine Walkers: [gandalf] condition, support area; a discount on companions

					// The Shadow player's cards.
					put("relics", "1_195");     // Relics of Moria: [moria] condition, support area; a Shadow ability
					put("evil", "1_283");       // You Bring Great Evil: [sauron] condition borne by a companion; no healing at a sanctuary
					put("poison", "8_28");      // Spider Poison: a [gollum] possession, support area; a skirmish ability
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BroodingonTomorrowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Brooding on Tomorrow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time you lose initiative (except during the fellowship phase), you may spot a [gandalf] companion to choose an opponent who must discard one of his or her conditions.
		*/

		var scn = GetScenario();

		var brooding = scn.GetFreepsCard("brooding");

		assertEquals("Brooding on Tomorrow", brooding.getBlueprint().getTitle());
		assertNull(brooding.getBlueprint().getSubtitle());
		assertTrue(brooding.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, brooding.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, brooding.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, brooding.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(brooding, Keyword.SUPPORT_AREA));
		assertEquals(1, brooding.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachLossOfInitiativeWithGandalfOffersTheShadowPlayerADiscardOfOneOfHisConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooding = scn.GetFreepsCard("brooding");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var walkers = scn.GetFreepsCard("walkers");
		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var filler4 = scn.GetFreepsCard("filler4");
		var relics = scn.GetShadowCard("relics");
		var evil = scn.GetShadowCard("evil");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(brooding, walkers, relics, poison);
		scn.AttachCardsTo(aragorn, evil);
		scn.MoveCardsToHand(valor, valor2, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"));
		assertInZone(Zone.SUPPORT, brooding, walkers, relics, poison);
		assertInZone(Zone.ATTACHED, evil);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// Four cards to three: initiative is lost, and the offer is made. Declined, nothing is discarded.
		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brooding));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, relics);
		assertInZone(Zone.ATTACHED, evil);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A card more makes four: initiative is back from the next answer on.
		scn.MoveCardsToHand(filler4);
		scn.ShadowPass();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// Lost a second time in the same phase: offered again. The one opponent is chosen unasked, and he picks
		// one of his conditions: the one in his support area or the one a companion bears, not his possession,
		// and none of the Free Peoples player's conditions.
		scn.FreepsPlayCard(valor2);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brooding));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(relics, evil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poison, walkers, brooding));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(evil);

		assertInDiscard(evil);
		assertInZone(Zone.SUPPORT, relics, poison, walkers, brooding);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithNoGandalfCompanionUntilAnEntAvengerIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooding = scn.GetFreepsCard("brooding");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ent = scn.GetFreepsCard("ent");
		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var relics = scn.GetShadowCard("relics");
		// Barliman is [gandalf] and no companion, Aragorn a companion who is not [gandalf].
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(brooding, relics);
		scn.MoveCardsToHand(valor, valor2, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"));
		assertInZone(Zone.SUPPORT, brooding, relics);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brooding));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, relics);

		// The Ent Avenger, a [gandalf] companion who is no Wizard, and a hand of four again.
		scn.MoveCompanionsToTable(ent);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler3"));
		scn.ShadowPass();
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(valor2);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brooding));
		// Relics of Moria is his one condition, and goes unasked.
		scn.FreepsAcceptOptionalTrigger();
		assertInDiscard(relics);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ALossInTheFellowshipPhaseOrAHandGoingFromFiveToFourOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooding = scn.GetFreepsCard("brooding");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var relics = scn.GetShadowCard("relics");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(brooding, relics);
		scn.MoveCardsToHand(stairs, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		assertInZone(Zone.SUPPORT, brooding, relics);

		scn.StartGame();

		// Four cards to three in the fellowship phase: initiative is lost and nothing is offered.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(stairs);
		assertFalse(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brooding));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.SUPPORT, relics);

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);
		// Five cards in hand.
		scn.MoveCardsToHand(valor, valor2);
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// Five cards to four: initiative is kept.
		scn.FreepsPlayCard(valor);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brooding));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();
		assertInZone(Zone.SUPPORT, relics);

		// Four cards to three on the same board: the offer is made.
		scn.FreepsPlayCard(valor2);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brooding));
	}

	@Test
	public void NotOfferedWhenTheOpponentHasNoCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooding = scn.GetFreepsCard("brooding");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var walkers = scn.GetFreepsCard("walkers");
		var valor = scn.GetFreepsCard("valor");
		var poison = scn.GetShadowCard("poison");
		// The Shadow player has a possession in his support area and no condition; the Free Peoples player has one.
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(brooding, walkers, poison);
		scn.MoveCardsToHand(valor, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		assertInZone(Zone.SUPPORT, brooding, walkers, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brooding));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, brooding, walkers, poison);
	}
}
