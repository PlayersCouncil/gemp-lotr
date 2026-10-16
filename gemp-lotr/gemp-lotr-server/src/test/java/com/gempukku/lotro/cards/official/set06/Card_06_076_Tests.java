package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
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
					put("balrog", "6_76");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCostScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balrog", "6_76");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion, cost 1; "When you play this minion, you may add (2)"
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll minion; its own cost text is for underground sites only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, cost 6, no text: a second minion in hand
					put("relics", "1_195");     // Relics of Moria: a [moria] card that is no minion; a Shadow ability used only on purpose
					put("soldier", "1_271");    // Orc Soldier: an Orc minion of another culture ([sauron]); a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balrog", "6_76");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					// Two allies whose home is site 3, so that either player may assign them there:
					put("thrarin", "1_27");     // Thrarin: strength 4; a maneuver ability used only on purpose
					put("elrond", "1_40");      // Elrond: strength 8; heals allies of site 3 at the start of the turn, draws by a fellowship ability
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

		var card = scn.GetFreepsCard("balrog");

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

	@Test
	public void NeitherPlayerMayAssignTheBalrogToAnAllyOfStrengthUnderSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		var thrarin = scn.GetFreepsCard("thrarin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(thrarin, elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(balrog, troop);
		// The fellowship moves on to site 3, the home of both allies.
		scn.SkipToAssignments();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(8, scn.GetStrength(elrond));

		assertFalse(scn.FreepsCanAssignAgainst(thrarin, balrog));
		assertTrue(scn.FreepsCanAssignAgainst(elrond, balrog));
		assertFalse(scn.ShadowCanAssignAgainst(thrarin, balrog));
		assertTrue(scn.ShadowCanAssignAgainst(elrond, balrog));
		// Thrarin may be assigned to a minion with no text.
		assertTrue(scn.FreepsCanAssignAgainst(thrarin, troop));
		assertTrue(scn.ShadowCanAssignAgainst(thrarin, troop));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elrond, balrog}, new PhysicalCardImpl[]{thrarin, troop});
		assertTrue(scn.IsCharAssignedAgainst(elrond, balrog));
		assertTrue(scn.IsCharAssignedAgainst(thrarin, troop));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TwilightCostIsThreeLessOnceAMoriaMinionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCostScenario();

		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var relics = scn.GetShadowCard("relics");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(balrog, runner, troop);
		scn.MoveCardsToSupportArea(relics);
		assertInZone(Zone.SUPPORT, relics);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, relics);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		// A [moria] condition and a [sauron] Orc minion: neither is a [moria] minion.
		assertEquals(14, scn.GetTwilightCost(balrog));
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(11, scn.GetTwilightCost(balrog));
		// Only The Balrog's own cost changes.
		assertEquals(6, scn.GetTwilightCost(troop));

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(balrog);
		// 14 - 3 + 2 roaming (a site 4 minion at site 2)
		assertEquals(twilight - 13, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACaveTrollIsAMoriaMinionThatLowersTheCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCostScenario();

		var balrog = scn.GetShadowCard("balrog");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToHand(balrog);

		scn.StartGame();
		assertEquals(14, scn.GetTwilightCost(balrog));

		scn.MoveMinionsToTable(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		// A [moria] minion that is a Troll and no Orc.
		assertEquals(11, scn.GetTwilightCost(balrog));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
