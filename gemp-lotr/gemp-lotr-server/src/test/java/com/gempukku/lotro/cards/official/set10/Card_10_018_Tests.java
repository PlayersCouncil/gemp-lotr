package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treebeard", "10_18");
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: [gandalf] Wizard companion; a fellowship ability used only on purpose
					put("avenger", "6_27");       // Ent Avenger: [gandalf] Ent companion, not unique; +2 only while 3 Ents can be spotted
					put("avenger2", "6_27");
					put("erland", "11_30");       // Erland, Dale Counselor: [gandalf] Man companion; to play, spot a [gandalf] companion
					put("hugin", "2_24");         // Hugin, Emissary from Laketown: a [gandalf] ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] companion
					put("wisdom", "2_23");        // Gandalf's Wisdom: [gandalf] support-area condition, cost 0; a discount on items for Gandalf
					put("draught", "9_24");       // Ent Draught: [gandalf] condition on a companion, cost 1; to play, spot an Ent
					put("walkers", "1_79");       // The Nine Walkers: [gandalf] support-area condition; to play, spot Gandalf
					put("echoes", "7_34");        // Echoes of Valinor: [gandalf] support-area condition (the Shadow player's copy, in his own pile)
					put("foes", "1_105");         // Foes of Mordor: [gondor] support-area condition, cost 1
					put("strength", "1_278");     // Strength Born of Fear: [sauron] support-area condition; a skirmish ability
					put("weary", "4_212");        // Weary: [isengard] condition on a companion or ally; answers an [isengard] tracker's assignment only
					put("poison", "8_28");        // Spider Poison: [gollum] possession in the support area; answers its bearer's assignment only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreebeardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Treebeard, Keeper of the Watchwood
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 12
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: To play, spot 3 [gandalf] companions.
		 * Regroup: Exert Treebeard twice to play a [gandalf] condition from your discard pile and make an opponent discard one of his or her conditions.
		*/

		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");

		assertEquals("Treebeard", treebeard.getBlueprint().getTitle());
		assertEquals("Keeper of the Watchwood", treebeard.getBlueprint().getSubtitle());
		assertTrue(treebeard.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, treebeard.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, treebeard.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, treebeard.getBlueprint().getCardType());
		assertEquals(Race.ENT, treebeard.getBlueprint().getRace());
		assertEquals(5, treebeard.getBlueprint().getTwilightCost());
		assertEquals(12, treebeard.getBlueprint().getStrength());
		assertEquals(4, treebeard.getBlueprint().getVitality());
		assertEquals(6, treebeard.getBlueprint().getResistance());
	}

	@Test
	public void CanBePlayedOnlyOnceThreeGandalfCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var erland = scn.GetFreepsCard("erland");
		var hugin = scn.GetFreepsCard("hugin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveAlliesToTable(hugin);
		scn.MoveCardsToHand(treebeard, erland);

		scn.StartGame();

		// A Wizard and an Ent of [gandalf], a [gandalf] ally and a [gondor] companion: two [gandalf] companions.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(treebeard));

		// A [gandalf] Man companion makes three.
		scn.FreepsPlayCard(erland);
		assertTrue(scn.FreepsPlayAvailable(treebeard));
		scn.FreepsPlayCard(treebeard);

		assertInZone(Zone.FREE_CHARACTERS, treebeard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionExertsTreebeardTwiceToPlayAGandalfConditionFromTheDiscardPileAndMakeTheShadowPlayerDiscardOneOfHisConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var treebeard = scn.GetFreepsCard("treebeard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wisdom = scn.GetFreepsCard("wisdom");
		var draught = scn.GetFreepsCard("draught");
		var foes = scn.GetFreepsCard("foes");
		var avenger2 = scn.GetFreepsCard("avenger2");
		scn.MoveCompanionsToTable(treebeard, aragorn);
		scn.MoveCardsToDiscard(wisdom, draught, foes, avenger2);

		var echoes = scn.GetShadowCard("echoes");
		var strength = scn.GetShadowCard("strength");
		var weary = scn.GetShadowCard("weary");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToDiscard(echoes);
		scn.MoveCardsToSupportArea(strength, poison);
		scn.AttachCardsTo(aragorn, weary);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertTrue(scn.FreepsActionAvailable(treebeard));
		scn.FreepsUseCardAction(treebeard);
		assertEquals(2, scn.GetWoundsOn(treebeard));

		// [gandalf] conditions of his own discard pile, of either kind: to the support area and onto a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wisdom, draught));
		// Not a condition of another culture, a [gandalf] card that is no condition, or one in the other player's pile.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(foes, avenger2, echoes));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(draught);
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(draught, frodo);

		// The opponent discards one of his own conditions, of either kind, by his own choice.
		assertTrue(scn.ShadowHasCardChoiceAvailable(strength, weary));
		// Not his possession, and not the Free Peoples player's condition.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poison, draught));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(weary);

		assertInDiscard(weary, wisdom, foes);
		assertInZone(Zone.SUPPORT, strength, poison);
		assertAttachedTo(draught, frodo);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhileTreebeardCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var wisdom = scn.GetFreepsCard("wisdom");
		scn.MoveCompanionsToTable(treebeard);
		scn.MoveCardsToDiscard(wisdom);
		// Vitality 4 with 2 wounds: one exertion left.
		scn.AddWoundsToChar(treebeard, 2);

		var strength = scn.GetShadowCard("strength");
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(treebeard));
	}

	@Test
	public void RegroupActionIsNotOfferedWithNoGandalfConditionThatCanBePlayedAndWithNoShadowConditionItDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var walkers = scn.GetFreepsCard("walkers");
		var foes = scn.GetFreepsCard("foes");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var wisdom = scn.GetFreepsCard("wisdom");
		scn.MoveCompanionsToTable(treebeard);
		// The Nine Walkers cannot be played with no Gandalf to spot.
		scn.MoveCardsToDiscard(walkers, foes, avenger2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(treebeard));

		scn.MoveCardsToDiscard(wisdom);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(treebeard));

		// The Shadow player has no condition: the condition is played all the same, and nothing more is asked.
		scn.FreepsUseCardAction(treebeard);
		assertEquals(2, scn.GetWoundsOn(treebeard));
		assertInZone(Zone.SUPPORT, wisdom);
		assertInDiscard(walkers, foes, avenger2);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
