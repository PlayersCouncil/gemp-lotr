package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("allies", "3_102");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability never used
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2; damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, no ally; a maneuver ability never used
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally; an archery ability never used
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally; a fellowship ability never used
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally; a maneuver ability never used
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a Hobbit ally, laid for the Shadow player; a fellowship ability never used
					put("sword", "1_299");      // Hobbit Sword: a card for the Free Peoples hand; no text but its bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OurListofAlliesGrowsThinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Our List of Allies Grows Thin
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert a [sauron] minion. Plays to your support area.
		 * 	Each time the fellowship moves, add (1) for each ally you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("allies");

		assertEquals("Our List of Allies Grows Thin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronMinionOfEitherRaceToPlayToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allies = scn.GetShadowCard("allies");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(allies);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, troop);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(allies));
		scn.ShadowPlayCard(allies);

		// A [sauron] Orc and a [sauron] Uruk-hai are offered; the [moria] Orc is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentry);

		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, allies);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allies = scn.GetShadowCard("allies");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(allies);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(soldier, 1);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The [moria] Orc beside it could exert, but is not [sauron].
		assertTrue(scn.IsExhausted(soldier));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(allies));

		// A [sauron] minion able to exert makes it playable.
		scn.MoveMinionsToTable(sentry);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(allies));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveAddsOneForEachAllyOfTheFreePeoplesPlayerCountedAtThatMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allies = scn.GetShadowCard("allies");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var thrarin = scn.GetFreepsCard("thrarin");
		var maggot = scn.GetShadowCard("maggot");
		scn.MoveCardsToSupportArea(allies);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin);
		// The Shadow player's own ally is not active on the Free Peoples player's turn.
		scn.MoveAlliesToTable(maggot);
		assertInZone(Zone.SUPPORT, allies, orophin, maggot);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		// To Ettenmoors: 2 for its Shadow number + 2 for Frodo and Aragorn + 1 for Orophin.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.MoveAlliesToTable(bilbo, thrarin);
		assertInZone(Zone.SUPPORT, bilbo, thrarin);
		int pool = scn.GetTwilight();

		scn.FreepsChooseToMove();
		// To Rivendell Valley: 0 for its Shadow number + 2 for the companions + 3 for Orophin, Bilbo and Thrarin.
		assertEquals(pool + 5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allies = scn.GetShadowCard("allies");
		var orophin = scn.GetFreepsCard("orophin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveAlliesToTable(orophin, bilbo);

		scn.StartGame();
		scn.FreepsPass();
		// Put down after the turn's first move, which it would otherwise answer.
		scn.MoveCardsToSupportArea(allies);
		assertInZone(Zone.SUPPORT, allies);

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		// A card in hand, so that the Free Peoples player is asked to reconcile and the turn does not end at once.
		scn.SetFreepsHand(sword);
		int pool = scn.GetTwilight();

		scn.FreepsChooseToStay();
		assertEquals(pool, scn.GetTwilight());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
	}
}
