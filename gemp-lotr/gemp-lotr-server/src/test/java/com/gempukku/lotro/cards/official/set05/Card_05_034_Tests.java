package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fallback", "5_34");
					put("wall", "5_33");        // City Wall: a [gondor] fortification, a Free Peoples condition that a minion bears
					put("wall2", "5_33");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability, never used
					// Ranger of Osgiliath: a [gondor] Man companion; a regroup ability, never used
					put("ranger", "7_116");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, damage +1, no other text
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					// Goblin Runner: a [moria] Orc; its "when you play" never comes up, as it is put on the table
					put("runner", "1_178");
					put("scimitar1", "1_180");  // Goblin Scimitar: a [moria] possession, a Shadow card a minion bears
					put("sword", "1_160");      // Uruk-hai Sword: an [isengard] possession on an Uruk-hai; its win trigger never comes up
					// Let Her Deal With Them: a [gollum] condition on a minion, damage +1; it plays from the discard pile
					// only in the Shadow phase, which none of these tests reaches again
					put("deal1", "7_63");
					put("deal2", "7_63");
					put("deal3", "7_63");
					// Disarmed: a [gondor] condition on a minion that is no fortification; its bearer bears no Shadow cards
					put("disarmed", "18_46");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FallBackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Fall Back
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Discard all Shadow cards borne by a minion bearing a [gondor] fortification.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fallback");

		assertEquals("Fall Back", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsEveryShadowCardBorneByTheMinionChosenAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fallback = scn.GetFreepsCard("fallback");
		var wall = scn.GetFreepsCard("wall");
		var wall2 = scn.GetFreepsCard("wall2");
		var disarmed = scn.GetFreepsCard("disarmed");
		var ranger = scn.GetFreepsCard("ranger");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar1");
		var sword = scn.GetShadowCard("sword");
		var deal1 = scn.GetShadowCard("deal1");
		var deal2 = scn.GetShadowCard("deal2");
		var deal3 = scn.GetShadowCard("deal3");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(fallback);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, brigand, runner);
		// Two minions of two cultures and races bearing a [gondor] fortification, each with a Shadow possession and
		// a Shadow condition; a minion bearing a Shadow card and no fortification; a minion bearing a [gondor]
		// condition that is no fortification.
		scn.AttachCardsTo(troop, wall2, scimitar, deal1);
		scn.AttachCardsTo(savage, wall, sword, deal2);
		scn.AttachCardsTo(brigand, deal3);
		scn.AttachCardsTo(runner, disarmed);
		assertAttachedTo(wall2, troop);
		assertAttachedTo(scimitar, troop);
		assertAttachedTo(deal1, troop);
		assertAttachedTo(wall, savage);
		assertAttachedTo(sword, savage);
		assertAttachedTo(deal2, savage);
		assertAttachedTo(deal3, brigand);
		assertAttachedTo(disarmed, runner);
		assertInHand(fallback);

		// The phase before the skirmishes: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fallback));

		// The Savage skirmishes the Ranger; the Troop is left unassigned.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(ranger, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(ranger);

		assertTrue(scn.FreepsPlayAvailable(fallback));
		scn.FreepsPlayCard(fallback);
		// Either minion bearing a [gondor] fortification, in the skirmish or not; not the minion bearing only a
		// Shadow card, nor the one bearing a [gondor] condition that is no fortification.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brigand, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// The Troop's possession and condition are discarded; the fortification, a Free Peoples card, stays.
		assertInDiscard(scimitar, deal1);
		assertAttachedTo(wall2, troop);
		// One minion only: the Savage keeps all it bears, and so does the Brigand.
		assertAttachedTo(wall, savage);
		assertAttachedTo(sword, savage);
		assertAttachedTo(deal2, savage);
		assertAttachedTo(deal3, brigand);
		assertAttachedTo(disarmed, runner);
		assertInDiscard(fallback);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoMinionBearingAFortificationItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fallback = scn.GetFreepsCard("fallback");
		var wall = scn.GetFreepsCard("wall");
		var ranger = scn.GetFreepsCard("ranger");
		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var deal1 = scn.GetShadowCard("deal1");
		scn.MoveCompanionsToTable(ranger);
		// A [gondor] fortification in play, but in the support area and borne by no minion.
		scn.MoveCardsToSupportArea(wall);
		scn.MoveCardsToHand(fallback);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, sword, deal1);
		assertAttachedTo(sword, savage);
		assertAttachedTo(deal1, savage);
		assertInZone(Zone.SUPPORT, wall);
		assertInHand(fallback);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(ranger, savage);
		scn.FreepsResolveSkirmish(ranger);

		assertTrue(scn.FreepsPlayAvailable(fallback));
		scn.FreepsPlayCard(fallback);

		assertInDiscard(fallback);
		assertAttachedTo(sword, savage);
		assertAttachedTo(deal1, savage);
		assertInZone(Zone.SUPPORT, wall);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AHinderedShadowCardBorneByThatMinionIsDiscardedWithTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("fallback");
		var wall = scn.GetFreepsCard("wall");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(card);

		var troop = scn.GetShadowCard("troop");
		var hindered = scn.GetShadowCard("scimitar1");
		// A Shadow condition beside the Scimitar: a second hand weapon on one bearer is no board a game reaches.
		var unhindered = scn.GetShadowCard("deal1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, wall, hindered, unhindered);
		// A hindered card has no type or culture, and is a Shadow card still (CR 5.0 "hinder"; Ketura on Q-0098).
		scn.HinderCard(hindered);
		assertAttachedTo(wall, troop);
		assertAttachedTo(hindered, troop);
		assertAttachedTo(unhindered, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		assertTrue(scn.IsHindered(hindered));
		assertTrue(scn.FreepsPlayAvailable(card));
		scn.FreepsPlayCard(card);

		// Both Shadow cards the minion bore are discarded, the hindered one with the other; the Free Peoples
		// fortification stays on it.
		assertInDiscard(hindered, unhindered);
		assertAttachedTo(wall, troop);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
