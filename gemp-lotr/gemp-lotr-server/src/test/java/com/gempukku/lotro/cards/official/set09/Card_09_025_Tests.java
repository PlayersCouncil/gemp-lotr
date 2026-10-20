package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("huorn1", "9_25");
					put("huorn2", "9_25");
					// Ent Avenger: [gandalf] Ent companion, strength 6, vitality 4, cost 4; strength +2 only while 3
					// Ents can be spotted (never here)
					put("avenger", "6_27");
					// Treebeard, Earthborn: [gandalf] Ent ally, vitality 4; a response to an unbound Hobbit about to
					// be discarded, and a fellowship ability, used only on purpose
					put("treebeard", "4_103");
					// Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability
					// used only on purpose
					put("aragorn", "1_89");
					// Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop1", "1_177");
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HuornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Huorn
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Tree
		 * Strength: 6
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Damage +1</b>.
		 * 	This companion is strength +1 for each Ent you can spot.
		 * 	<b>Response:</b> If this companion is about to take a wound, exert an Ent to prevent that.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("huorn1");

		assertEquals("Huorn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.TREE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void StrengthIsPlusOneForEachEntSpottedAndAnotherHuornIsNoEnt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var huorn1 = scn.GetFreepsCard("huorn1");
		var huorn2 = scn.GetFreepsCard("huorn2");
		var avenger = scn.GetFreepsCard("avenger");
		var treebeard = scn.GetFreepsCard("treebeard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(huorn1, huorn2, aragorn);
		scn.MoveCardsToHand(avenger);

		scn.StartGame();

		// No Ent: the other Huorn is a Tree.
		assertEquals(6, scn.GetStrength(huorn1));
		assertEquals(6, scn.GetStrength(huorn2));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(avenger);
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		// One Ent: 6 + 1 on each Huorn, each by its own text.
		assertEquals(7, scn.GetStrength(huorn1));
		assertEquals(7, scn.GetStrength(huorn2));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		assertInZone(Zone.DECK, treebeard);
		scn.MoveAlliesToTable(treebeard);
		// Two Ents, a companion and an ally: 6 + 2, not 6 + 4 for two Huorns.
		assertInZone(Zone.SUPPORT, treebeard);
		assertEquals(8, scn.GetStrength(huorn1));
		assertEquals(8, scn.GetStrength(huorn2));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void AboutToTakeAWoundItMayExertAnEntToPreventThatAndNoOtherCompanionIsSaved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var huorn1 = scn.GetFreepsCard("huorn1");
		var huorn2 = scn.GetFreepsCard("huorn2");
		var avenger = scn.GetFreepsCard("avenger");
		var treebeard = scn.GetFreepsCard("treebeard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(huorn1, huorn2, avenger, aragorn);
		scn.MoveAlliesToTable(treebeard);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, troop1},
				new PhysicalCardImpl[]{huorn1, troop2}
		);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn (8) loses to the Troop (13) and takes his wound: no Huorn is offered anything.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(huorn1);
		// Huorn: 6 + 2 Ents.
		assertEquals(8, scn.GetStrength(huorn1));
		assertEquals(0, scn.GetWoundsOn(huorn1));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetWoundsOn(huorn2));
		scn.PassSkirmishActions();

		// The Huorn about to be wounded offers its response; the other Huorn's is not for him.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(huorn1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(huorn2));
		scn.FreepsChooseOptionalTrigger(huorn1);

		// The Ent companion and the Ent ally may pay; a Huorn is no Ent.
		assertTrue(scn.FreepsHasCardChoiceAvailable(avenger, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(huorn1, huorn2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(treebeard);

		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(huorn2));
		assertEquals(0, scn.GetWoundsOn(huorn1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhileTheOnlyEntIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var huorn1 = scn.GetFreepsCard("huorn1");
		var huorn2 = scn.GetFreepsCard("huorn2");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(huorn1, huorn2, avenger);

		scn.StartGame();
		// Vitality 4: 3 wounds leave the Avenger exhausted.  The other Huorn could exert, but is no Ent.
		scn.AddWoundsToChar(avenger, 3);
		assertEquals(3, scn.GetWoundsOn(avenger));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(huorn1, troop);
		scn.FreepsResolveSkirmish(huorn1);

		assertEquals(0, scn.GetWoundsOn(huorn1));
		assertEquals(0, scn.GetWoundsOn(huorn2));
		scn.PassSkirmishActions();

		// Huorn (7) loses to the Troop (13) and takes the wound, with nothing offered.
		assertEquals(1, scn.GetWoundsOn(huorn1));
		assertEquals(3, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(huorn2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
