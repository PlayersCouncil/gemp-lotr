package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "6_76");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balrog", "6_76");
					put("troop", "1_177");              // Goblin Patrol Troop: a minion with no text
					// Two [gondor] Men who differ in strength and in nothing that bears on an assignment:
					put("gondorianKnight", "5_35");     // Gondorian Knight: strength 6; "Knight" is his whole text
					put("knightOfGondor", "5_36");      // Knight of Gondor: strength 5; a Knight with a fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheBalrogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: The Balrog, Terror of Flame and Shadow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 14
		 * Type: Minion
		 * Subtype: Balrog
		 * Strength: 17
		 * Vitality: 5
		 * Site Number: 4
		 * Game Text: <b>Damage +2</b>.<br>The twilight cost of The Balrog is -3 if you can spot a [moria] minion.<br>The Balrog cannot be assigned to skirmish companions or allies of strength less than 6.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("The Balrog", card.getBlueprint().getTitle());
		assertEquals("Terror of Flame and Shadow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.BALROG, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(14, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void NeitherPlayerMayAssignTheBalrogToACompanionOfStrengthUnderSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		var six = scn.GetFreepsCard("gondorianKnight");
		var five = scn.GetFreepsCard("knightOfGondor");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(six, five);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, troop);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		// Frodo is 3 + 1 for The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(5, scn.GetStrength(five));
		assertEquals(6, scn.GetStrength(six));

		// The bar is on a pair and not on a card: everybody is still on the Free Peoples player's lists.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(balrog));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(five));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(six));

		// Strength 4 and strength 5 are less than 6; strength 6 is not.
		assertFalse(scn.FreepsCanAssignAgainst(frodo, balrog));
		assertFalse(scn.FreepsCanAssignAgainst(five, balrog));
		assertTrue(scn.FreepsCanAssignAgainst(six, balrog));
		// The text names no player: the Shadow player may not make those assignments either.
		assertFalse(scn.ShadowCanAssignAgainst(frodo, balrog));
		assertFalse(scn.ShadowCanAssignAgainst(five, balrog));
		assertTrue(scn.ShadowCanAssignAgainst(six, balrog));
		// The same three against a minion with no text.
		assertTrue(scn.FreepsCanAssignAgainst(frodo, troop));
		assertTrue(scn.FreepsCanAssignAgainst(five, troop));
		assertTrue(scn.FreepsCanAssignAgainst(six, troop));
		assertTrue(scn.ShadowCanAssignAgainst(frodo, troop));
		assertTrue(scn.ShadowCanAssignAgainst(five, troop));
		assertTrue(scn.ShadowCanAssignAgainst(six, troop));

		// The Free Peoples player leaves the Balrog alone, and the Shadow player has it to assign.
		scn.FreepsAssignToMinions(five, troop);
		assertTrue(scn.IsCharAssignedAgainst(five, troop));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(balrog));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(six));
		assertFalse(scn.ShadowCanAssignAgainst(frodo, balrog));
		assertFalse(scn.ShadowCanAssignAgainst(five, balrog));
		assertTrue(scn.ShadowCanAssignAgainst(six, balrog));

		// The companion of strength 6 is assigned to the Balrog, and it stands.
		scn.ShadowAssignToMinions(six, balrog);
		assertTrue(scn.IsCharAssignedAgainst(six, balrog));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
