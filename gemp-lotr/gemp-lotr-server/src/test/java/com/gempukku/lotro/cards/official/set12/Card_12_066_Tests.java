package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gathering", "12_66");
					put("axeman", "11_69");     // Axeman of Harad: [men] Man minion, strength 11, vitality 3; a cost reduction only
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, strength 3, vitality 4; abilities used only on purpose
					put("dagger", "18_69");     // Henchman's Dagger: [men] hand weapon, strength +1, bearer must be a [men] minion
					put("easterling", "4_224"); // Easterling Axeman: [raider] Man minion, strength 6; +3 and fierce only at 2 burdens
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GatheringStrengthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Gathering Strength
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Toil 2</b>. <helper>(For each [men] character you exert when playing this, its twilight cost is -2.)</helper><br>Make a [men] minion strength +6.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gathering");

		assertEquals("Gathering Strength", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(6, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachMenCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetShadowCard("gathering");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(gathering);
		scn.MoveMinionsToTable(axeman, ted, troop);
		assertInHand(gathering);
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, axeman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 6 - 2 - 2 is the least it can cost, with both [men] minions exerted; the [moria] Troop does not count.
		// Toil 1 would want 4, and toil 3 (or the Troop counted) would let 1 do.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(gathering));
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(gathering));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(gathering);
		// The pool is 2 short of its 6: one exertion is owed, and the second is the Shadow player's to make or not.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(ted);
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 4 - (6 - 2)
		assertEquals(0, scn.GetTwilight());

		assertEquals(11, scn.GetStrength(axeman));
		scn.ShadowChooseCard(axeman);
		assertEquals(17, scn.GetStrength(axeman));
		assertInDiscard(gathering);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesAMenMinionStrengthPlus6WhetherOrNotItIsInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetShadowCard("gathering");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var dagger = scn.GetShadowCard("dagger");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(gathering);
		scn.MoveMinionsToTable(axeman, ted, easterling);
		scn.AttachCardsTo(ted, dagger);
		assertInHand(gathering);
		assertInZone(Zone.ATTACHED, dagger);
		scn.SetTwilight(10);

		// The Axeman of Harad and the Easterling Axeman both skirmish Aragorn; Ted is assigned to nobody.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, axeman);
		scn.ShadowAssignToMinions(aragorn, easterling);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Ted: 3 + 1 for the dagger.
		assertEquals(4, scn.GetStrength(ted));
		assertEquals(11, scn.GetStrength(axeman));
		assertEquals(6, scn.GetStrength(easterling));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(ted));
		scn.ShadowPlayCard(gathering);
		// The pool pays in full: toil asks which [men] characters to exert, from none, and none is exerted.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(easterling, dagger, aragorn));
		scn.ShadowChooseCard(ted);

		assertEquals(10, scn.GetStrength(ted));
		assertEquals(11, scn.GetStrength(axeman));
		assertEquals(6, scn.GetStrength(easterling));
		assertInDiscard(gathering);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetShadowCard("gathering");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(gathering);
		scn.MoveMinionsToTable(axeman);
		assertInHand(gathering);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		// The pool pays its full 6 without toil.
		assertTrue(scn.GetTwilight() >= 6);
		assertFalse(scn.ShadowPlayAvailable(gathering));
	}

	@Test
	public void TheBonusIsGoneInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetShadowCard("gathering");
		var axeman = scn.GetShadowCard("axeman");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(gathering);
		scn.MoveMinionsToTable(axeman, easterling);
		assertInHand(gathering);
		scn.SetTwilight(10);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, easterling}, new PhysicalCardImpl[]{frodo, axeman});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Played in Aragorn's skirmish on the Axeman of Harad, whose own skirmish with Frodo is still to come.
		assertEquals(11, scn.GetStrength(axeman));
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.ShadowPlayCard(gathering);
		// The pool pays in full: toil asks which [men] character to exert, from none, and none is exerted.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(17, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn (8) beats the Easterling Axeman (6); each skirmish is a phase of its own.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(11, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
