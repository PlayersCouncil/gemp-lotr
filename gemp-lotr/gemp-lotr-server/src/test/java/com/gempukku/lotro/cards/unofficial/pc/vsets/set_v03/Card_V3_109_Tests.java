package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gaffer", "103_109");
					put("sam", "10_122");
					put("promise1", "2_112");
					put("promise2", "2_112");
					put("promise3", "2_112");
					put("promise4", "2_112");

					put("goblin5_1", "1_178"); //5 strength
					put("goblin5_2", "1_181"); //5 strength
					put("goblin8_3", "1_179"); //8 strength
					put("goblin6_4", "1_184"); //6 strength

					put("sauron", "9_48");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForMyOldGafferStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: For My Old Gaffer
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot a Hobbit skirmishing more than 1 minion.  Resolve skirmishes between that Hobbit and those minions individually.  Until the regroup phase, add a burden each time that Hobbit loses a skirmish and a threat each time that Hobbit wins.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gaffer");

		assertEquals("For My Old Gaffer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ForMyOldGafferResolvesSkirmishesProperly() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gaffer = scn.GetFreepsCard("gaffer");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(gaffer);
		scn.MoveCompanionsToTable(sam);
		scn.MoveFreepsCardsToSupportArea("promise1", "promise2", "promise3");

		var goblin_1 = scn.GetShadowCard("goblin5_1");
		var goblin_2 = scn.GetShadowCard("goblin5_2");
		var goblin_3 = scn.GetShadowCard("goblin8_3");
		var goblin_4 = scn.GetShadowCard("goblin6_4");

		scn.MoveMinionsToTable(goblin_1, goblin_2, goblin_3, goblin_4);

		scn.StartGame();
		scn.RemoveBurdens(1);

		//Cheat to make Sam defender +2
		scn.ApplyAdHocModifier(new AddKeywordModifier(sam, sam, null, Keyword.DEFENDER, 2));

		//3 base +2 from game text +3 from 3 copies of A Promise
		assertEquals(8, scn.GetStrength(sam));
		assertEquals(2, scn.GetKeywordCount(sam, Keyword.DEFENDER));

		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { frodo, goblin_4 },
				new PhysicalCardImpl[] {   sam, goblin_1, goblin_2, goblin_3 }
		);

		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		assertTrue(scn.FreepsPlayAvailable(gaffer));

		scn.FreepsPlayCard(gaffer);
		scn.FreepsChooseCard(goblin_1);

		//Sam is now only technically skirmishing against goblin 1
		assertTrue(scn.IsCharSkirmishing(sam));
		assertTrue(scn.IsCharSkirmishing(goblin_1));
		//but goblins 2 and 3 are "assigned" to him for a subsequent resolution
		assertFalse(scn.IsCharSkirmishing(goblin_2));
		assertFalse(scn.IsCharSkirmishing(goblin_3));

		assertTrue(scn.IsCharAssignedAgainst(goblin_2, sam));
		assertTrue(scn.IsCharAssignedAgainst(goblin_3, sam));

		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveRuleFirst();

		//Goblin 1, at 5 strength 1 vitality, was killed by Sam's 8 strength
		assertInZone(Zone.DISCARD, goblin_1);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		//We are now presented with which minion to resolve the next subskirmish for
		assertTrue(scn.FreepsHasCardChoicesAvailable(goblin_2, goblin_3));
		//Frodo's skirmish is not an option
		assertFalse(scn.FreepsHasCardChoicesAvailable(frodo));
		assertFalse(scn.FreepsHasCardChoicesAvailable(goblin_4));

		scn.FreepsResolveSkirmish(goblin_2);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();

		//Goblin 2, also at 5 strength 1 vitality, was killed by Sam's 8 strength
		assertInZone(Zone.DISCARD, goblin_2);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		//We are now presented with only one remaining minion to resolve the next subskirmish for (but the choice must still be made)
		assertTrue(scn.FreepsHasCardChoicesAvailable(goblin_3));
		//Frodo's skirmish remains not an option
		assertFalse(scn.FreepsHasCardChoicesAvailable(frodo));
		assertFalse(scn.FreepsHasCardChoicesAvailable(goblin_4));

		scn.FreepsResolveSkirmish(goblin_3);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();

		//Sam, tied at 8 strength, loses to goblin 3
		assertInZone(Zone.SHADOW_CHARACTERS, goblin_3);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());

		//Finally Frodo's skirmish can be resolved
		assertTrue(scn.FreepsHasCardChoicesAvailable(frodo));
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishing(frodo));
		assertTrue(scn.IsCharSkirmishing(goblin_4));
		assertFalse(scn.IsCharSkirmishing(sam));
		assertFalse(scn.IsCharSkirmishing(goblin_1));
		assertFalse(scn.IsCharSkirmishing(goblin_2));
		assertFalse(scn.IsCharSkirmishing(goblin_3));

		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertNull(scn.gameState().getSkirmish());
		assertEquals(0, scn.gameState().getAssignments().size());
	}

	@Test
	public void ForMyOldGafferResolvesSkirmishesProperlyWhenHobbitDies() throws DecisionResultInvalidException, CardNotFoundException {

		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gaffer = scn.GetFreepsCard("gaffer");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(gaffer);
		scn.MoveCompanionsToTable(sam);

		var goblin_1 = scn.GetShadowCard("goblin5_1");
		var goblin_2 = scn.GetShadowCard("goblin5_2");
		var goblin_3 = scn.GetShadowCard("goblin6_4");
		var sauron = scn.GetShadowCard("sauron");

		scn.MoveMinionsToTable(goblin_1, goblin_2, goblin_3, sauron);

		scn.StartGame();
		scn.RemoveBurdens(1);

		//Cheat to make Sam defender +2
		scn.ApplyAdHocModifier(new AddKeywordModifier(sam, sam, null, Keyword.DEFENDER, 2));

		assertEquals(2, scn.GetKeywordCount(sam, Keyword.DEFENDER));

		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { frodo, goblin_3 },
				new PhysicalCardImpl[] {   sam, goblin_1, goblin_2, sauron }
		);

		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		assertTrue(scn.FreepsPlayAvailable(gaffer));

		scn.FreepsPlayCard(gaffer);
		scn.FreepsChooseCard(sauron);

		//Sam is now only technically skirmishing against sauron
		assertTrue(scn.IsCharSkirmishing(sam));
		assertTrue(scn.IsCharSkirmishing(sauron));
		//but goblins 1 and 2 are "assigned" to him for a subsequent resolution
		assertFalse(scn.IsCharSkirmishing(goblin_1));
		assertFalse(scn.IsCharSkirmishing(goblin_2));

		assertTrue(scn.IsCharAssignedAgainst(goblin_1, sam));
		assertTrue(scn.IsCharAssignedAgainst(goblin_2, sam));

		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveRuleFirst();

		//Sam is obliterated by Sauron
		assertInZone(Zone.DEAD, sam);
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());

		//We are now presented with which skirmish to do next.  The other two minions should no longer be assigned
		// and only Frodo's skirmish is left
		assertTrue(scn.FreepsHasCardChoicesAvailable(frodo));
		assertFalse(scn.FreepsHasCardChoicesAvailable(goblin_1));
		assertFalse(scn.FreepsHasCardChoicesAvailable(goblin_2));
		assertFalse(scn.IsCharAssigned(goblin_1));
		assertFalse(scn.IsCharAssigned(goblin_2));

		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsDeclineOptionalTrigger(); //ring

		//We've progressed to the next phase without issues
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ForMyOldGafferNotPlayableWhenOnly1MinionAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gaffer = scn.GetFreepsCard("gaffer");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(gaffer);
		scn.MoveCompanionsToTable(sam);
		scn.MoveFreepsCardsToSupportArea("promise1", "promise2", "promise3");

		var goblin_1 = scn.GetShadowCard("goblin5_1");

		scn.MoveMinionsToTable(goblin_1);

		scn.StartGame();
		scn.RemoveBurdens(1);

		//Cheat to make Sam defender +2
		scn.ApplyAdHocModifier(new AddKeywordModifier(sam, sam, null, Keyword.DEFENDER, 2));

		//3 base +2 from game text +3 from 3 copies of A Promise
		assertEquals(8, scn.GetStrength(sam));
		assertEquals(2, scn.GetKeywordCount(sam, Keyword.DEFENDER));

		scn.SkipToAssignments();

		scn.FreepsAssignAndResolve(sam, goblin_1);
		
		assertFalse(scn.FreepsPlayAvailable(gaffer));
	}
}
