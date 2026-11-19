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

public class Card_05_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("men", "5_37");
					put("wall", "5_33");        // City Wall: a [gondor] fortification, strength -3 to the minion that bears it
					// Citadel of the Stars: a [gondor] fortification, strength -2 and vitality -1 to the minion that bears it
					put("citadel", "5_32");
					// Disarmed: a [gondor] condition a minion bears that is no fortification; its bearer bears no Shadow cards
					put("disarmed", "18_46");
					// Ranger of Osgiliath: a [gondor] Man companion; a regroup ability, never used
					put("ranger", "7_116");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3, damage +1, no other text
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					// Goblin Runner: a [moria] Orc of vitality 1; its "when you play" never comes up, as it is put on the table
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MenofNumenorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Men of Númenor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Wound a minion bearing a [gondor] fortification.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("men");

		assertEquals("Men of Númenor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WoundsOneMinionBearingAGondorFortificationOnceAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var men = scn.GetFreepsCard("men");
		var wall = scn.GetFreepsCard("wall");
		var citadel = scn.GetFreepsCard("citadel");
		var disarmed = scn.GetFreepsCard("disarmed");
		var ranger = scn.GetFreepsCard("ranger");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(men);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, brigand, runner);
		// Two minions of two cultures and races, each bearing a [gondor] fortification; a minion bearing a [gondor]
		// condition that is no fortification; a minion bearing nothing.
		scn.AttachCardsTo(troop, wall);
		scn.AttachCardsTo(savage, citadel);
		scn.AttachCardsTo(brigand, disarmed);
		assertAttachedTo(wall, troop);
		assertAttachedTo(citadel, savage);
		assertAttachedTo(disarmed, brigand);
		assertInHand(men);

		// The phase before the skirmishes: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(men));

		// The Savage skirmishes the Ranger; the Troop is left unassigned.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(ranger, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(ranger);

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsPlayAvailable(men));
		scn.FreepsPlayCard(men);
		// Either minion bearing a [gondor] fortification, in the skirmish or not; not the minion bearing a [gondor]
		// condition that is no fortification, nor the one bearing nothing.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brigand, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// One wound, on the Troop alone (vitality 3: it lives).
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInDiscard(men);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnExhaustedMinionBearingAGondorFortificationIsWoundedAndKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var men = scn.GetFreepsCard("men");
		var wall = scn.GetFreepsCard("wall");
		var citadel = scn.GetFreepsCard("citadel");
		var ranger = scn.GetFreepsCard("ranger");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(men);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.AttachCardsTo(runner, wall);
		scn.AttachCardsTo(savage, citadel);
		assertAttachedTo(wall, runner);
		assertAttachedTo(citadel, savage);
		assertInHand(men);

		// The Savage skirmishes the Ranger; the Runner is left unassigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(ranger, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(ranger);

		// The Runner, of vitality 1, can take a wound though it could not exert.
		assertEquals(1, scn.GetVitality(runner));
		assertTrue(scn.FreepsPlayAvailable(men));
		scn.FreepsPlayCard(men);
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		scn.FreepsChooseCard(runner);

		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(men);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoMinionBearingAGondorFortificationItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var men = scn.GetFreepsCard("men");
		var wall = scn.GetFreepsCard("wall");
		var ranger = scn.GetFreepsCard("ranger");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(ranger);
		// A [gondor] fortification in play, but in the support area and borne by no minion.
		scn.MoveCardsToSupportArea(wall);
		scn.MoveCardsToHand(men);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertInZone(Zone.SUPPORT, wall);
		assertInHand(men);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(ranger, savage);
		scn.FreepsResolveSkirmish(ranger);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(men));
		scn.FreepsPlayCard(men);

		assertInDiscard(men);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertInZone(Zone.SUPPORT, wall);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
