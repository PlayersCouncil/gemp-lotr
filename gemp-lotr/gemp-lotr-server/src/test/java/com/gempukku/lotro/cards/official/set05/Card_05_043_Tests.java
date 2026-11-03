package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("war", "5_43");
					put("war2", "5_43");        // a second copy, for a second reading in one skirmish

					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, strength 6, no text but knight
					// Ranger of Osgiliath: [gondor] Man, strength 6; a regroup ability, never used
					put("ranger", "7_116");
					// Gondorian Merchant: a [gondor] Man ally; his maneuver ability is never used
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] Hobbit; his skirmish ability is never used
					put("pippin", "103_31");
					// Ceorl, Weary Horseman: a [rohan] Man; his response is about sites and never comes up
					put("ceorl", "4_264");

					// City Wall: [gondor] fortification; its Skirmish transfer is never used
					put("wall", "5_33");
					// Disarmed: a [gondor] condition that is no fortification; cheated onto a minion, so its
					// "when transferred" text never fires
					put("disarmed", "18_46");

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarMustBeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: War Must Be
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [gondor] Man strength +2 (or +4 if skirmishing a minion bearing a [gondor] fortification).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("war");

		assertEquals("War Must Be", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAGondorManStrengthPlusTwoForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetFreepsCard("war");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var ceorl = scn.GetFreepsCard("ceorl");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight, ranger, pippin, ceorl);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(war);

		scn.StartGame();
		assertInHand(war);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The assignment phase, next door to the skirmish: not playable.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(war));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop}, new PhysicalCardImpl[]{ranger, savage});
		scn.FreepsResolveSkirmish(knight);

		assertEquals(6, scn.GetStrength(knight));
		assertEquals(6, scn.GetStrength(ranger));
		assertTrue(scn.FreepsPlayAvailable(war));
		scn.FreepsPlayCard(war);
		// [gondor] Men: a companion in the skirmish, one in no skirmish yet, and the ally; not a [gondor] Hobbit,
		// a [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, ranger, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, ceorl, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ranger);

		// 6 + 2; nobody skirmishing a fortified minion.
		assertEquals(8, scn.GetStrength(ranger));
		assertEquals(6, scn.GetStrength(knight));
		assertInDiscard(war);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Troop wins; the next skirmish is a phase of its own, and the +2 is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(ranger);
		assertEquals(6, scn.GetStrength(ranger));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlusFourWhenSkirmishingAMinionBearingAGondorFortificationAndPlusTwoForAManInNoSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetFreepsCard("war");
		var war2 = scn.GetFreepsCard("war2");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var wall = scn.GetFreepsCard("wall");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight, ranger);
		scn.MoveCardsToHand(war, war2);

		scn.StartGame();
		assertInHand(war, war2);
		scn.MoveMinionsToTable(troop, savage);
		scn.AttachCardsTo(savage, wall);
		assertAttachedTo(wall, savage);
		scn.SkipToAssignments();
		// Two minions on the Knight; the second, put there by the Shadow player, bears the City Wall.
		scn.FreepsAssignToMinions(knight, troop);
		scn.ShadowAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(6, scn.GetStrength(knight));
		scn.FreepsPlayCard(war);
		scn.FreepsChooseCard(knight);
		// 6 + 4
		assertEquals(10, scn.GetStrength(knight));

		// The Ranger, beside him and in no skirmish, is skirmishing nobody: only +2.
		scn.ShadowPass();
		assertEquals(6, scn.GetStrength(ranger));
		scn.FreepsPlayCard(war2);
		scn.FreepsChooseCard(ranger);
		assertEquals(8, scn.GetStrength(ranger));
		assertEquals(10, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PlusTwoAgainstAMinionBearingAGondorConditionThatIsNoFortificationWhileAFortifiedMinionWaitsElsewhere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetFreepsCard("war");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var wall = scn.GetFreepsCard("wall");
		var disarmed = scn.GetFreepsCard("disarmed");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight, ranger);
		scn.MoveCardsToHand(war);

		scn.StartGame();
		assertInHand(war);
		scn.MoveMinionsToTable(troop, savage);
		scn.AttachCardsTo(troop, disarmed);
		scn.AttachCardsTo(savage, wall);
		assertAttachedTo(disarmed, troop);
		assertAttachedTo(wall, savage);
		scn.SkipToAssignments();
		// The Knight skirmishes the Troop, who bears a [gondor] condition that is no fortification; the Savage,
		// bearing the City Wall, is assigned to the Ranger, whose skirmish has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop}, new PhysicalCardImpl[]{ranger, savage});
		scn.FreepsResolveSkirmish(knight);

		assertEquals(6, scn.GetStrength(knight));
		scn.FreepsPlayCard(war);
		scn.FreepsChooseCard(knight);
		// 6 + 2
		assertEquals(8, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
