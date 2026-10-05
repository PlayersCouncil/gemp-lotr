package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_058_Tests
{
	// Every minion here has a home site above 2, so each costs 2 more than its printed cost at this site.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Goblin Backstabber: [moria] Orc, strength 5, vitality 1.  Its text is about being in a skirmish
					// with another [moria] Orc, which never happens here.
					put("backstabber1", "1_174");
					put("backstabber2", "1_174");
					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					// Goblin Spearman: [moria] Orc, strength 6, vitality 1; its text needs a Goblin Spear
					put("spearman", "2_65");
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability that is never used
					put("soldier", "1_271");

					put("merry", "1_302");          // Merry, Friend to Sam: a companion of strength 3 and cost 1
					// Aragorn, Ranger of the North: a [gondor] companion of strength 8 and vitality 4, with the Gandalf
					// signet.  His maneuver ability is never used.
					put("aragorn", "1_89");
					// Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability that is never used
					put("boromir", "1_97");
					put("guard", "1_7");            // Dwarf Guard: a [dwarven] companion with no text in play

					put("gandalf", "1_72");         // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion
					// Gandalf's Cart: a [gandalf] possession that sits in the support area; a fellowship ability only
					put("cart", "1_73");
					put("wisdom", "2_23");          // Gandalf's Wisdom: a [gandalf] condition
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "101_58");
					put("site3", "1_341");          // Rivendell Valley: shadow number 0
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PrancingPonySpareRoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Prancing Pony Spare Room
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 2
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: Each time a minion of strength 6 or less is played, make it strength +3 until the regroup
		 * 	phase (unless you can spot a [gandalf] card).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Prancing Pony Spare Room", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void EachMinionOfStrengthSixOrLessIsStrengthPlusThreeWhenPlayedAndAMinionOfStrengthSevenIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var backstabber1 = scn.GetShadowCard("backstabber1");
		var backstabber2 = scn.GetShadowCard("backstabber2");
		var savage = scn.GetShadowCard("savage");
		var spearman = scn.GetShadowCard("spearman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(backstabber1, backstabber2, spearman, soldier);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals("Prancing Pony Spare Room", scn.GetCurrentSite().getBlueprint().getTitle());
		// 20 set + 1 companion + 2 for the Spare Room
		assertEquals(23, scn.GetTwilight());
		// A minion of strength 5 that is on the table without having been played.
		scn.MoveMinionsToTable(savage);
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(backstabber1));
		assertEquals(5, scn.GetStrength(backstabber2));
		assertEquals(6, scn.GetStrength(spearman));
		assertEquals(7, scn.GetStrength(soldier));

		scn.ShadowPlayCard(backstabber1);
		assertInZone(Zone.SHADOW_CHARACTERS, backstabber1);
		assertEquals(8, scn.GetStrength(backstabber1));
		assertEquals(5, scn.GetStrength(savage));
		// Nothing was asked.
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Each minion played gets its own +3.
		scn.ShadowPlayCard(backstabber2);
		assertEquals(8, scn.GetStrength(backstabber2));
		assertEquals(8, scn.GetStrength(backstabber1));
		assertEquals(5, scn.GetStrength(savage));

		// Strength 6 is "6 or less".
		scn.ShadowPlayCard(spearman);
		assertEquals(9, scn.GetStrength(spearman));

		// Strength 7 is not.
		scn.ShadowPlayCard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(8, scn.GetStrength(backstabber1));
		assertEquals(8, scn.GetStrength(backstabber2));
		assertEquals(9, scn.GetStrength(spearman));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GoesByTheStrengthTheMinionHasWhenPlayedAndNotByThePrintedNumber() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var backstabber1 = scn.GetShadowCard("backstabber1");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(backstabber1, soldier);
		// An Orc Soldier printed at 7 who comes into play at 6, and a Goblin Backstabber printed at 5 who comes
		// into play at 7.
		scn.ApplyAdHocModifier(new StrengthModifier(null, scn.GenerateShadowFilter("name(Orc Soldier)"), null, -1));
		scn.ApplyAdHocModifier(new StrengthModifier(null, scn.GenerateShadowFilter("name(Goblin Backstabber)"), null, 2));

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(soldier));
		assertEquals(7, scn.GetStrength(backstabber1));

		// 7 - 1 + 3
		scn.ShadowPlayCard(soldier);
		assertEquals(9, scn.GetStrength(soldier));

		// 5 + 2, and no more
		scn.ShadowPlayCard(backstabber1);
		assertInZone(Zone.SHADOW_CHARACTERS, backstabber1);
		assertEquals(7, scn.GetStrength(backstabber1));
		assertEquals(9, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheBonusLastsThroughTheSkirmishesAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var backstabber1 = scn.GetShadowCard("backstabber1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(backstabber1, savage);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(backstabber1);
		scn.ShadowPlayCard(savage);
		assertEquals(8, scn.GetStrength(backstabber1));
		assertEquals(8, scn.GetStrength(savage));

		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(8, scn.GetStrength(backstabber1));
		assertEquals(8, scn.GetStrength(savage));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, backstabber1}, new PhysicalCardImpl[]{aragorn, savage});

		// Boromir at 7 loses to a Goblin Backstabber at 8.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(backstabber1));
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SHADOW_CHARACTERS, backstabber1);

		// In the second skirmish both minions still have it.  Aragorn at 8 against an Uruk Savage at 8: a tie
		// goes to the Shadow, and the Savage is damage +1.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(backstabber1));
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, backstabber1, savage);
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(backstabber1));
	}

	@Test
	public void AMinionPlayedWhileGandalfCanBeSpottedGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var backstabber1 = scn.GetShadowCard("backstabber1");
		var spearman = scn.GetShadowCard("spearman");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(backstabber1, spearman);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(5, scn.GetStrength(backstabber1));
		assertEquals(6, scn.GetStrength(spearman));

		scn.ShadowPlayCard(backstabber1);
		assertInZone(Zone.SHADOW_CHARACTERS, backstabber1);
		assertEquals(5, scn.GetStrength(backstabber1));

		scn.ShadowPlayCard(spearman);
		assertInZone(Zone.SHADOW_CHARACTERS, spearman);
		assertEquals(6, scn.GetStrength(spearman));
		assertEquals(5, scn.GetStrength(backstabber1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhetherAGandalfCardCanBeSpottedIsReadWhenEachMinionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cart = scn.GetFreepsCard("cart");
		var backstabber1 = scn.GetShadowCard("backstabber1");
		var backstabber2 = scn.GetShadowCard("backstabber2");
		var savage = scn.GetShadowCard("savage");
		// A [gandalf] card that is neither a Wizard nor a character.
		scn.MoveCardsToSupportArea(cart);
		scn.MoveCardsToHand(backstabber1, backstabber2, savage);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertInZone(Zone.SUPPORT, cart);
		scn.ShadowPlayCard(backstabber1);
		assertEquals(5, scn.GetStrength(backstabber1));

		// The Cart goes.  The minion that was played beside it gains nothing now; the next one played does.
		scn.MoveCardsToDiscard(cart);
		assertEquals(5, scn.GetStrength(backstabber1));
		scn.ShadowPlayCard(backstabber2);
		assertEquals(8, scn.GetStrength(backstabber2));
		assertEquals(5, scn.GetStrength(backstabber1));

		// The Cart comes back.  The minion that has its +3 keeps it; the next one played gets none.
		scn.MoveCardsToSupportArea(cart);
		assertEquals(8, scn.GetStrength(backstabber2));
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(backstabber2));
		assertEquals(5, scn.GetStrength(backstabber1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingShortOfAGandalfCardInPlayStopsTheBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var cart = scn.GetFreepsCard("cart");
		var wisdom = scn.GetFreepsCard("wisdom");
		var backstabber1 = scn.GetShadowCard("backstabber1");
		// Aragorn has the Gandalf signet and is a [gondor] card.  Gandalf himself is dead, his Cart is in the
		// discard pile and Gandalf's Wisdom is in hand.
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToDeadPile(gandalf);
		scn.MoveCardsToDiscard(cart);
		scn.MoveCardsToHand(wisdom);
		scn.MoveCardsToHand(backstabber1);

		scn.StartGame();
		// A Wizard who is no [gandalf] card.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, guard, null, Race.WIZARD));
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DEAD, gandalf);
		assertInDiscard(cart);
		assertInHand(wisdom);
		assertEquals(5, scn.GetStrength(backstabber1));

		scn.ShadowPlayCard(backstabber1);
		assertEquals(8, scn.GetStrength(backstabber1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionOfStrengthThreePlayedHereGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(merry);

		scn.StartGame();
		// The first player's second turn starts where his first one ended, at site 2.
		scn.SkipToSite(2);

		assertEquals("Prancing Pony Spare Room", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsPlayAvailable(merry));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnTheFirstPlayersMinionIsStrengthenedTheSameWay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// On the second player's turn the first player is the Shadow player.
		var backstabber1 = scn.GetFreepsCard("backstabber1");
		scn.MoveCardsToHand(backstabber1);

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInHand(backstabber1);
		scn.SetTwilight(20);
		scn.ShadowPassCurrentPhaseAction();

		assertEquals("Prancing Pony Spare Room", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		assertEquals(5, scn.GetStrength(backstabber1));

		scn.FreepsPlayCard(backstabber1);
		assertInZone(Zone.SHADOW_CHARACTERS, backstabber1);
		assertEquals(8, scn.GetStrength(backstabber1));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
