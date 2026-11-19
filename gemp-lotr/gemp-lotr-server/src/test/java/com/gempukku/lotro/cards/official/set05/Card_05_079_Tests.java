package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armory", "5_79");

					put("recruit", "18_103");   // Rohirrim Recruit: a [rohan] Man companion; strength +2 only with a [rohan] token
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY, home 4T; an ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or Eowyn can be
					// spotted; his other texts answer an exertion or are used only on purpose
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; a skirmish ability used only on purpose
					put("sword", "4_291");      // Sword of Rohan: a [rohan] possession, hand weapon
					put("shield", "5_91");      // Rohirrim Shield: a [rohan] possession, shield
					put("slits", "5_80");       // Arrow-slits: a [rohan] condition, no possession
					put("arrow", "9_46");       // The Red Arrow: a [rohan] artifact, no possession
					put("armor", "1_92");       // Armor: a [gondor] possession

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					// Battering Ram: an [isengard] machine (a condition) in the support area; abilities used only on purpose
					put("ram", "5_44");
					// Uruk Shield Wall: an [isengard] machine that is a possession; its trigger answers only an Uruk-hai's wound
					put("wall", "102_35");
					// Ships of Great Draught: a [raider] possession holding [raider] tokens that is no machine; abilities used only on purpose
					put("ships", "8_65");
					put("mount", "4_287");      // Rider's Mount: a [rohan] possession, the Shadow player's copy in his own discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Armory
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fortification</b>. Plays to your support area.<br><b>Maneuver:</b> Spot 2 [rohan] Men to remove 2 tokens from a machine or to take a [rohan] possession into hand from your discard pile. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armory");

		assertEquals("Armory", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TwoRohanMenLetItRemoveTwoTokensFromAMachineChosenAndThenItIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var recruit = scn.GetFreepsCard("recruit");
		var guma = scn.GetFreepsCard("guma");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(recruit);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToDiscard(sword);

		var troop = scn.GetShadowCard("troop");
		var ram = scn.GetShadowCard("ram");
		var wall = scn.GetShadowCard("wall");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(ram, wall, ships);
		scn.AddTokensToCard(ram, 2);
		scn.AddTokensToCard(wall, 2);
		scn.AddTokensToCard(ships, 2);
		assertEquals(2, scn.GetCultureTokensOn(ram));
		assertEquals(2, scn.GetCultureTokensOn(wall));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertInDiscard(sword);

		assertInZone(Zone.SUPPORT, armory);
		scn.StartGame();
		// Not a fellowship action.
		assertFalse(scn.FreepsActionAvailable(armory));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// The Recruit (a companion) and Guma (an ally) are the two [rohan] Men.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(armory));
		scn.FreepsUseCardAction(armory);

		// Either half can be done in full, so the player is asked which.
		assertTrue(scn.FreepsChoiceAvailable("Remove 2 tokens"));
		assertTrue(scn.FreepsChoiceAvailable("Take"));
		scn.FreepsChoose("Remove 2 tokens");

		// A machine that is a condition and one that is a possession; not a card with tokens that is no machine.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ram, wall));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ships));
		scn.FreepsChooseCard(ram);

		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(2, scn.GetCultureTokensOn(wall));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertInDiscard(armory, sword);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TakesARohanPossessionChosenFromTheDiscardPileIntoHandAndThenItIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var recruit = scn.GetFreepsCard("recruit");
		var guma = scn.GetFreepsCard("guma");
		var sword = scn.GetFreepsCard("sword");
		var shield = scn.GetFreepsCard("shield");
		var slits = scn.GetFreepsCard("slits");
		var arrow = scn.GetFreepsCard("arrow");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(recruit);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToDiscard(sword, shield, slits, arrow, armor);

		var troop = scn.GetShadowCard("troop");
		var ram = scn.GetShadowCard("ram");
		var mount = scn.GetShadowCard("mount");
		scn.MoveCardsToSupportArea(ram);
		scn.AddTokensToCard(ram, 2);
		scn.MoveCardsToDiscard(mount);
		assertEquals(2, scn.GetCultureTokensOn(ram));
		assertInDiscard(sword, shield, slits, arrow, armor, mount);

		assertInZone(Zone.SUPPORT, armory);
		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(armory));
		scn.FreepsUseCardAction(armory);

		assertTrue(scn.FreepsChoiceAvailable("Remove 2 tokens"));
		assertTrue(scn.FreepsChoiceAvailable("Take"));
		scn.FreepsChoose("Take");

		// [rohan] possessions of two classes; not a [rohan] condition, a [rohan] artifact, a [gondor] possession,
		// nor the Shadow player's [rohan] possession in his own discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, shield));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(slits, arrow, armor, mount));
		scn.FreepsChooseCard(sword);

		assertInHand(sword);
		assertInDiscard(armory, shield, slits, arrow, armor, mount);
		assertEquals(2, scn.GetCultureTokensOn(ram));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneTokenOnTheOnlyMachineItTakesAPossessionWithNoChoiceOfHalves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var recruit = scn.GetFreepsCard("recruit");
		var guma = scn.GetFreepsCard("guma");
		var sword = scn.GetFreepsCard("sword");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(recruit);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToDiscard(sword, shield);

		var troop = scn.GetShadowCard("troop");
		var ram = scn.GetShadowCard("ram");
		scn.MoveCardsToSupportArea(ram);
		scn.AddTokensToCard(ram, 1);
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertInDiscard(sword, shield);

		assertInZone(Zone.SUPPORT, armory);
		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(armory));
		scn.FreepsUseCardAction(armory);

		// Two tokens cannot be removed from a machine with one: the take is all there is.
		assertFalse(scn.FreepsChoiceAvailable("Remove 2 tokens"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, shield));
		scn.FreepsChooseCard(shield);

		assertInHand(shield);
		assertInDiscard(armory, sword);
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoRohanPossessionInTheDiscardPileItRemovesTheTokensWithNoChoiceOfHalves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var recruit = scn.GetFreepsCard("recruit");
		var guma = scn.GetFreepsCard("guma");
		var slits = scn.GetFreepsCard("slits");
		var arrow = scn.GetFreepsCard("arrow");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(recruit);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToDiscard(slits, arrow, armor);

		var troop = scn.GetShadowCard("troop");
		var ram = scn.GetShadowCard("ram");
		scn.MoveCardsToSupportArea(ram);
		scn.AddTokensToCard(ram, 3);
		assertEquals(3, scn.GetCultureTokensOn(ram));
		assertInDiscard(slits, arrow, armor);

		assertInZone(Zone.SUPPORT, armory);
		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(armory));
		scn.FreepsUseCardAction(armory);

		// Nothing in the discard pile is a [rohan] possession: the Ram, the one machine, loses 2 of its 3 tokens
		// with nothing asked.
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertInDiscard(armory, slits, arrow, armor);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNeitherHalfPossibleInFullBothAreOfferedAndTheOneTokenIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var recruit = scn.GetFreepsCard("recruit");
		var guma = scn.GetFreepsCard("guma");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(recruit);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToDiscard(armor);

		var troop = scn.GetShadowCard("troop");
		var ram = scn.GetShadowCard("ram");
		scn.MoveCardsToSupportArea(ram);
		scn.AddTokensToCard(ram, 1);
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertInDiscard(armor);

		assertInZone(Zone.SUPPORT, armory);
		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(armory));
		scn.FreepsUseCardAction(armory);

		// Neither half can be done in full: both are offered, and what can be done of the one chosen is done.
		assertTrue(scn.FreepsChoiceAvailable("Remove 2 tokens"));
		assertTrue(scn.FreepsChoiceAvailable("Take"));
		scn.FreepsChoose("Remove 2 tokens");

		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertInDiscard(armory, armor);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithOneRohanManBesideARohanHobbitAndAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var recruit = scn.GetFreepsCard("recruit");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(recruit, merry, boromir);
		scn.MoveCardsToDiscard(sword);

		var troop = scn.GetShadowCard("troop");
		var ram = scn.GetShadowCard("ram");
		scn.MoveCardsToSupportArea(ram);
		scn.AddTokensToCard(ram, 2);
		assertEquals(2, scn.GetCultureTokensOn(ram));
		assertInDiscard(sword);

		assertInZone(Zone.SUPPORT, armory);
		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// Two [rohan] companions: Merry is a Hobbit and no Man.  Boromir is a Man of [gondor].
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(armory));
		assertEquals(2, scn.GetCultureTokensOn(ram));
		assertInDiscard(sword);
		assertInZone(Zone.SUPPORT, armory);
	}
}
