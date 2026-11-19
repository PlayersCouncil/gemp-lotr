package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("choice", "15_119");
					put("goblin", "11_137");     // Scurrying Goblin: an [orc] Orc; its play trigger is not reached (cheated in)
					put("halftroll", "14_12");   // Half-troll of Far Harad: an [orc] minion who is no Orc; a start-of-assignment trigger only
					put("chieftain", "1_266");   // Orc Chieftain: a [sauron] Orc; answers only a weapon played on him
					put("beckoning", "12_83");   // The Beckoning Shadow: an [orc] condition, a Shadow condition; nothing below 5 burdens

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability used only on purpose
					put("saga", "1_114");        // The Saga of Elendil: a [gondor] condition borne by a [gondor] companion; a skirmish ability
					put("sword", "1_112");       // Ranger's Sword: a Free Peoples possession borne by Aragorn; damage +1 only
					put("mallorn", "1_54");      // Mallorn-trees: an [elven] condition in the support area; a fellowship ability
					put("lord", "1_21");         // Lord of Moria: a [dwarven] condition in the support area; each Dwarf is damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnreasonableChoiceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Unreasonable Choice
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot 2 [orc] minions to discard a Free Peoples condition. The Free Peoples player may add (2) for each Free Peoples condition you can spot to prevent that.
		*/

		var scn = GetScenario();

		var choice = scn.GetFreepsCard("choice");

		assertEquals("Unreasonable Choice", choice.getBlueprint().getTitle());
		assertNull(choice.getBlueprint().getSubtitle());
		assertFalse(choice.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, choice.getBlueprint().getSide());
		assertEquals(Culture.ORC, choice.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, choice.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(choice, Timeword.SHADOW));
		assertEquals(1, choice.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingTwoOrcMinionsItDiscardsAFreePeoplesConditionWhenThePreventionIsDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga, sword);
		scn.MoveCardsToSupportArea(mallorn);

		var choice = scn.GetShadowCard("choice");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(choice);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, mallorn);
		assertInZone(Zone.SUPPORT, beckoning);
		assertInHand(choice);
		scn.MoveMinionsToTable(goblin, halftroll);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(choice));

		scn.ShadowPlayCard(choice);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		int pool = scn.GetTwilight();

		scn.FreepsChooseNo();
		assertEquals(pool, scn.GetTwilight());
		// Free Peoples conditions, one borne and one in the support area; not a Free Peoples possession, nor a Shadow condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, mallorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, beckoning));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(saga);
		assertInDiscard(saga);
		assertInZone(Zone.SUPPORT, mallorn);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, beckoning);
		assertInDiscard(choice);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayAddTwoForEachOfTheTwoFreePeoplesConditionsToPreventTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga, sword);
		scn.MoveCardsToSupportArea(mallorn);

		var choice = scn.GetShadowCard("choice");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(choice);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, mallorn);
		assertInZone(Zone.SUPPORT, beckoning);
		assertInHand(choice);
		scn.MoveMinionsToTable(goblin, halftroll);
		scn.FreepsPass();

		scn.ShadowPlayCard(choice);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		int pool = scn.GetTwilight();

		scn.FreepsChooseYes();
		// Two Free Peoples conditions: (2) for each.  Ranger's Sword is no condition and The Beckoning Shadow no Free
		// Peoples card: counting either would read 6.
		assertEquals(pool + 4, scn.GetTwilight());
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn);
		assertInDiscard(choice);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeFreePeoplesConditionsThePreventionAddsSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var mallorn = scn.GetFreepsCard("mallorn");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(mallorn, lord);

		var choice = scn.GetShadowCard("choice");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(choice);

		scn.StartGame();
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn);
		assertInZone(Zone.SUPPORT, lord);
		assertInHand(choice);
		scn.MoveMinionsToTable(goblin, halftroll);
		scn.FreepsPass();

		scn.ShadowPlayCard(choice);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		int pool = scn.GetTwilight();

		scn.FreepsChooseYes();
		assertEquals(pool + 6, scn.GetTwilight());
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, mallorn);
		assertInZone(Zone.SUPPORT, lord);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedSpottingOneOrcMinionBesideAnOrcConditionAndASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCardsToSupportArea(mallorn);

		var choice = scn.GetShadowCard("choice");
		var goblin = scn.GetShadowCard("goblin");
		var chieftain = scn.GetShadowCard("chieftain");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(choice);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, mallorn);
		assertInZone(Zone.SUPPORT, beckoning);
		assertInHand(choice);
		scn.MoveMinionsToTable(goblin, chieftain);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(choice));
	}

	@Test
	public void WithNoFreePeoplesConditionItIsPlayedForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var choice = scn.GetShadowCard("choice");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(choice);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, beckoning);
		assertInHand(choice);
		scn.MoveMinionsToTable(goblin, halftroll);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(choice));

		scn.ShadowPlayCard(choice);
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, beckoning);
		assertInDiscard(choice);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
