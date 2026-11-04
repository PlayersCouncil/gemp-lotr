package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "13_67");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability that is never used here
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally; a maneuver ability that needs initiative
					put("foes", "1_105");       // Foes of Mordor: [gondor] condition, support area; a maneuver ability
					// I Will Go: [gondor] condition, unique, bearer must be a [gondor] Man; defender +1 with initiative
					put("go1", "7_105");
					// Spirit of the White Tree: [gondor] condition on a [gondor] companion; bars the opponent's skirmish
					// events and abilities in bearer's skirmishes only
					put("spirit", "17_34");
					put("promise", "2_112");    // A Promise: [shire] condition, support area; needs Sam to do anything
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Not unique: two copies, one in a skirmish and one left unassigned
					put("guard1", "13_67");
					put("guard2", "13_67");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, no text but Knight

					// Support of the City: [gondor] support-area condition, not unique; fellowship abilities only
					put("city", "7_123");
					// Houses of Healing: [gondor] support-area condition, unique; its tokens come only as it is played,
					// and its ability is a fellowship one
					put("houses", "11_61");
					// Songs of the Blessed Realm: an [elven] support-area condition; answers only an [elven] tale played
					put("songs", "1_61");
					// Seeing Stone of Minas Anor: a [gondor] ARTIFACT in the support area; a regroup ability only
					put("stone", "9_37");
					// I Will Go: a [gondor] condition borne by a [gondor] Man, and so not in the support area;
					// defender +1 with initiative, which changes no strength
					put("willgo", "7_105");

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuardoftheWhiteTreeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Guard of the White Tree
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: <b>Knight</b>.<br><b>Skirmish:</b> Discard a [gondor] condition from your support area to make this companion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guard");

		assertEquals("Guard of the White Tree", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishAbilityMayDiscardAGondorConditionAnAllyBearsAndNotOneACompanionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var foes = scn.GetFreepsCard("foes");
		var go1 = scn.GetFreepsCard("go1");
		var spirit = scn.GetFreepsCard("spirit");
		var promise = scn.GetFreepsCard("promise");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(foes, promise);
		// In your support area: a [gondor] condition lying there, and one an ally there bears.
		scn.AttachCardsTo(merchant, go1);
		// Not in it: a [gondor] condition on a companion.  In it and not [gondor]: A Promise.
		scn.AttachCardsTo(aragorn, spirit);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, foes, promise);
		assertAttachedTo(go1, merchant);
		assertAttachedTo(spirit, aragorn);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, runner);

		assertEquals(7, scn.GetStrength(guard));
		assertTrue(scn.FreepsActionAvailable(guard));
		scn.FreepsUseCardAction(guard);
		assertTrue(scn.FreepsHasCardChoiceAvailable(foes, go1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(spirit, promise));
		scn.FreepsChooseCard(go1);

		assertInDiscard(go1);
		assertEquals(9, scn.GetStrength(guard));
		assertInZone(Zone.SUPPORT, foes, promise);
		assertAttachedTo(spirit, aragorn);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsAGondorSupportConditionToMakeThatGuardStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var knight = scn.GetFreepsCard("knight");
		var city = scn.GetFreepsCard("city");
		var houses = scn.GetFreepsCard("houses");
		var songs = scn.GetFreepsCard("songs");
		var stone = scn.GetFreepsCard("stone");
		var willgo = scn.GetFreepsCard("willgo");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(guard1, guard2, knight);
		scn.MoveCardsToSupportArea(city, houses, songs, stone);
		scn.AttachCardsTo(knight, willgo);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, city, houses, songs, stone);
		assertAttachedTo(willgo, knight);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		// The second Guard is left out of the skirmishes.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard1, runner1},
				new PhysicalCardImpl[]{knight, runner2}
		);
		scn.FreepsResolveSkirmish(guard1);

		assertEquals(7, scn.GetStrength(guard1));
		assertEquals(7, scn.GetStrength(guard2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(guard1));

		scn.FreepsUseCardAction(guard1);
		// Two [gondor] conditions in the support area; not the [elven] one there, the [gondor] artifact there, or the
		// [gondor] condition the Knight bears.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(city, houses));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(songs, stone, willgo));
		scn.FreepsChooseCard(city);

		assertInDiscard(city);
		assertInZone(Zone.SUPPORT, houses, songs, stone);
		assertAttachedTo(willgo, knight);
		// "This companion": the other copy is not made stronger.
		assertEquals(9, scn.GetStrength(guard1));
		assertEquals(7, scn.GetStrength(guard2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight);

		// A skirmish ability lasts for the skirmish phase it is used in; this is the next one.
		assertEquals(7, scn.GetStrength(guard1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The text names no skirmish of his own: the Guard who is in none may use it too, and the one [gondor]
		// condition left in the support area is taken unasked.
		assertTrue(scn.FreepsActionAvailable(guard2));
		scn.FreepsUseCardAction(guard2);
		assertInDiscard(houses);
		assertEquals(9, scn.GetStrength(guard2));
		assertEquals(7, scn.GetStrength(guard1));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoGondorConditionInYourSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var knight = scn.GetFreepsCard("knight");
		var songs = scn.GetFreepsCard("songs");
		var stone = scn.GetFreepsCard("stone");
		var willgo = scn.GetFreepsCard("willgo");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(guard1, knight);
		scn.MoveCardsToSupportArea(songs, stone);
		scn.AttachCardsTo(knight, willgo);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, songs, stone);
		assertAttachedTo(willgo, knight);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard1, runner1);

		// An [elven] condition and a [gondor] artifact in the support area, and a [gondor] condition borne by a
		// companion: nothing can pay.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(guard1));
		assertEquals(7, scn.GetStrength(guard1));
	}
}
