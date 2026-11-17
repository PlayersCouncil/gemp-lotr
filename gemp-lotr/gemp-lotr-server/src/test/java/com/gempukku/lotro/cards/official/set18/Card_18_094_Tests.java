package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cast", "18_94");
					put("cast2", "18_94");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, vitality 3; valiant is its whole text
					// Rohirrim Herdsman: a [rohan] Man ally; a regroup ability used only on purpose
					put("herdsman", "7_247");
					// Merry, Master Holbytla: a [rohan] Hobbit companion (a Man only beside 3 [rohan] companions or Éowyn);
					// his trigger answers a [rohan] Man exerting, which nobody does here
					put("merry", "103_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8
					put("theoden", "4_292");    // Théoden, Son of Thengel: a [rohan] Man; a regroup ability used only on purpose

					put("spear", "4_288");      // Rider's Spear: a [rohan] possession, hand weapon, strength +2, on a [rohan] Man
					put("mount", "4_287");      // Rider's Mount: a [rohan] possession, mount, on a [rohan] Man; a start-of-skirmish trigger only
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession on Aragorn
					put("supplies", "4_290");   // Supplies of the Mark: a [rohan] condition, support area; a fellowship ability only
					put("herugrim", "54_280");  // Herugrim: a [rohan] ARTIFACT on Théoden
					// Simbelmynë: a [rohan] event that plays a [rohan] possession from the draw deck
					put("simbelmyne", "4_289");

					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger does not fire when cheated in
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CastfromtheHallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Cast from the Hall
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time you play a [rohan] possession from hand, add a [rohan] token here.<br><b>Skirmish:</b> Remove 3 [rohan] tokens from here to make a [rohan] companion strength +1 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cast");

		assertEquals("Cast from the Hall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachRohanPossessionPlayedFromHandAddsATokenToEachCopyAndNoOtherCardDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetFreepsCard("cast");
		var cast2 = scn.GetFreepsCard("cast2");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var theoden = scn.GetFreepsCard("theoden");
		var spear = scn.GetFreepsCard("spear");
		var mount = scn.GetFreepsCard("mount");
		var sword = scn.GetFreepsCard("sword");
		var supplies = scn.GetFreepsCard("supplies");
		var herugrim = scn.GetFreepsCard("herugrim");
		scn.MoveCompanionsToTable(guard, aragorn, theoden);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(cast, cast2);
		scn.MoveCardsToHand(spear, mount, sword, supplies, herugrim);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(cast));
		assertEquals(0, scn.GetCultureTokensOn(cast2));

		// A [rohan] hand weapon on a companion: one token on each copy, each its own.
		scn.FreepsPlayCard(spear);
		scn.FreepsChooseCard(guard);
		assertAttachedTo(spear, guard);
		assertEquals(1, scn.GetCultureTokensOn(cast));
		assertEquals(1, scn.GetCultureTokensOn(cast2));

		// A [rohan] mount on an ally: each copy answers again.
		scn.FreepsPlayCard(mount);
		scn.FreepsChooseCard(herdsman);
		assertAttachedTo(mount, herdsman);
		assertEquals(2, scn.GetCultureTokensOn(cast));
		assertEquals(2, scn.GetCultureTokensOn(cast2));

		// A [gondor] possession, a [rohan] condition and a [rohan] artifact, each from hand: no token.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);
		scn.FreepsPlayCard(supplies);
		assertInZone(Zone.SUPPORT, supplies);
		scn.FreepsPlayCard(herugrim);
		assertAttachedTo(herugrim, theoden);
		assertEquals(2, scn.GetCultureTokensOn(cast));
		assertEquals(2, scn.GetCultureTokensOn(cast2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanPossessionPlayedFromTheDrawDeckAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetFreepsCard("cast");
		var guard = scn.GetFreepsCard("guard");
		var spear = scn.GetFreepsCard("spear");
		var simbelmyne = scn.GetFreepsCard("simbelmyne");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(cast);
		scn.MoveCardsToHand(simbelmyne);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(cast));

		// Simbelmynë (a [rohan] event, from hand) spots the valiant Guard and plays the Spear from the draw deck.
		scn.FreepsPlayCard(simbelmyne);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(spear);

		assertAttachedTo(spear, guard);
		assertInZone(Zone.DISCARD, simbelmyne);
		assertEquals(0, scn.GetCultureTokensOn(cast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesThreeTokensToMakeARohanCompanionStrengthPlus1AndDamagePlus1ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var cast = scn.GetFreepsCard("cast");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(cast);
		// A card whose own text gives it tokens.
		scn.AddTokensToCard(cast, 4);
		assertEquals(4, scn.GetCultureTokensOn(cast));

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, runner1},
				new PhysicalCardImpl[]{aragorn, runner2});
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, runner1));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		assertTrue(scn.FreepsActionAvailable(cast));
		scn.FreepsUseCardAction(cast);
		assertEquals(1, scn.GetCultureTokensOn(cast));
		// [rohan] companions, in this skirmish and out of any: the Guard and Merry (a Hobbit); not the [rohan] ally,
		// not Aragorn or Frodo (companions of other cultures), not a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herdsman, aragorn, frodo, runner1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertEquals(7, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner2));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		// One token left: not offered.
		assertFalse(scn.FreepsActionAvailable(cast));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithTwoTokensHereThoughTheOtherCopyHasThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetFreepsCard("cast");
		var cast2 = scn.GetFreepsCard("cast2");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(cast, cast2);
		scn.AddTokensToCard(cast, 2);
		scn.AddTokensToCard(cast2, 3);
		assertEquals(2, scn.GetCultureTokensOn(cast));
		assertEquals(3, scn.GetCultureTokensOn(cast2));

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner1);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cast));
		assertTrue(scn.FreepsActionAvailable(cast2));
		assertEquals(2, scn.GetCultureTokensOn(cast));
		assertEquals(3, scn.GetCultureTokensOn(cast2));
	}

	@Test
	public void WithNoRohanCompanionTheTokensAreRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetFreepsCard("cast");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(cast);
		scn.AddTokensToCard(cast, 3);
		assertEquals(3, scn.GetCultureTokensOn(cast));

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(herdsman));

		// Only a [rohan] ally and a [gondor] companion: offered all the same, its cost paid, and nobody is changed.
		assertTrue(scn.FreepsActionAvailable(cast));
		scn.FreepsUseCardAction(cast);

		assertEquals(0, scn.GetCultureTokensOn(cast));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(herdsman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
