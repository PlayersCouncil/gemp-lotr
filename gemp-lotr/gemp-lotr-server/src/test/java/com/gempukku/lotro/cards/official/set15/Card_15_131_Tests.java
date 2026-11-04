package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_131_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("inspiration", "15_131");
					put("guard", "5_83");        // Household Guard: [rohan] Man companion, valiant, no hunter, no other text
					put("aldor", "4_262");       // Aldor, Soldier of Edoras: [rohan] Man ALLY, no hunter; an archery ability that needs a villager
					// Faramir, Captain of Ithilien: a hunter companion, a Man of [gondor]; a Maneuver heal used only on purpose
					put("faramir", "18_48");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion, no hunter; a Maneuver ability used only on purpose
					// Merry, Master Holbytla: a [rohan] character who is no Man while fewer than 3 [rohan] companions and no Eowyn
					put("merry", "103_85");
					put("tracking", "15_171");   // Tracking Uruk: [uruk-hai] Uruk-hai, hunter 1 is its whole text
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, no hunter, no text
					put("sword", "4_291");       // Sword of Rohan: [rohan] possession, hand weapon, bearer must be a [rohan] Man
					put("coat", "1_101");        // Coat of Mail: [gondor] possession, armor, bearer must be a Man
					put("armor", "1_92");        // Armor: [gondor] possession, armor, bearer must be a Man
					put("dwarvenarmor", "1_8");  // Dwarven Armor: [dwarven] possession, bearer must be a Dwarf
					put("arrow", "9_46");        // The Red Arrow: [rohan] ARTIFACT, support area
					put("supplies", "4_290");    // Supplies of the Mark: [rohan] condition, support area
					put("eored", "12_113");      // Éored Warrior: [rohan] Man companion
					put("axe", "15_136");        // Rohirrim Axe: [rohan] possession, hand weapon, bearer must be a [rohan] Man
				}},
				// Bag End at site 1: no discount on [rohan] possessions, as Edoras Hall would give.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OurInspirationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Our Inspiration
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot a [rohan] Man to play a possession from your discard pile for each hunter character you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("inspiration");

		assertEquals("Our Inspiration", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAHunterCompanionAndAHunterMinionPlaysTwoPossessionsFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inspiration = scn.GetFreepsCard("inspiration");
		var guard = scn.GetFreepsCard("guard");
		var faramir = scn.GetFreepsCard("faramir");
		var sword = scn.GetFreepsCard("sword");
		var coat = scn.GetFreepsCard("coat");
		var armor = scn.GetFreepsCard("armor");
		var arrow = scn.GetFreepsCard("arrow");
		var supplies = scn.GetFreepsCard("supplies");
		var eored = scn.GetFreepsCard("eored");
		var shadowAxe = scn.GetShadowCard("axe");
		var tracking = scn.GetShadowCard("tracking");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, faramir);
		scn.MoveCardsToHand(inspiration);
		scn.MoveCardsToDiscard(sword, coat, armor, arrow, supplies, eored);
		// A possession in the other player's discard pile, of a title the player's own pile does not hold.
		scn.MoveCardsToDiscard(shadowAxe);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Hunters: a companion and a minion.  Not hunters: the Household Guard (the [rohan] Man spotted) and the Troop.
		assertTrue(scn.HasKeyword(faramir, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(tracking, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(guard, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(troop, Keyword.HUNTER));
		assertInZone(Zone.DISCARD, sword, coat, armor, arrow, supplies, eored, shadowAxe);
		assertTrue(scn.FreepsPlayAvailable(inspiration));

		scn.FreepsPlayCard(inspiration);
		// Possessions of two cultures and two item classes; not an artifact, a condition or a companion, and not the
		// possession in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, coat, armor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arrow, supplies, eored, shadowAxe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(coat);
		// Coat of Mail's own "Bearer must be a Man".
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, faramir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(faramir);
		assertAttachedTo(coat, faramir);

		// The second play.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, armor));
		scn.FreepsChooseCardBPFromSelection(sword);
		// Sword of Rohan goes on the one [rohan] Man.
		assertAttachedTo(sword, guard);

		// Two hunters, two plays: the third possession is still in the discard pile.
		assertInZone(Zone.DISCARD, armor, arrow, supplies, eored);
		assertInDiscard(inspiration);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ARohanManWhoIsAnAllyIsSpottedAndOneHunterMinionPlaysOnePossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inspiration = scn.GetFreepsCard("inspiration");
		var aldor = scn.GetFreepsCard("aldor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var coat = scn.GetFreepsCard("coat");
		var armor = scn.GetFreepsCard("armor");
		var tracking = scn.GetShadowCard("tracking");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(inspiration);
		scn.MoveCardsToDiscard(coat, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// The one [rohan] Man is an ally; the one hunter is a minion.
		assertFalse(scn.HasKeyword(aragorn, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(aldor, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(tracking, Keyword.HUNTER));
		assertInZone(Zone.DISCARD, coat, armor);
		assertTrue(scn.FreepsPlayAvailable(inspiration));

		scn.FreepsPlayCard(inspiration);
		assertTrue(scn.FreepsHasCardChoiceAvailable(coat, armor));
		scn.FreepsChooseCardBPFromSelection(coat);
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(coat, aragorn);
		// One hunter, one play.
		assertInZone(Zone.DISCARD, armor);
		assertInDiscard(inspiration);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithTwoHuntersAndOnePossessionThatCanBePlayedItPlaysThatOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inspiration = scn.GetFreepsCard("inspiration");
		var guard = scn.GetFreepsCard("guard");
		var faramir = scn.GetFreepsCard("faramir");
		var sword = scn.GetFreepsCard("sword");
		var dwarvenarmor = scn.GetFreepsCard("dwarvenarmor");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCompanionsToTable(guard, faramir);
		scn.MoveCardsToHand(inspiration);
		scn.MoveCardsToDiscard(sword, dwarvenarmor);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.DISCARD, sword, dwarvenarmor);
		// The first play can be made (Ketura on Q-0062: "we should at least confirm that the first play is possible").
		assertTrue(scn.FreepsPlayAvailable(inspiration));

		scn.FreepsPlayCard(inspiration);
		// Sword of Rohan is the one possession that can be played, on the one [rohan] Man: made unasked.  Nobody can
		// bear Dwarven Armor, so the second play has nothing.
		assertAttachedTo(sword, guard);
		assertInZone(Zone.DISCARD, dwarvenarmor);
		assertInDiscard(inspiration);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotPlayableWithNoPossessionInTheDiscardPileThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inspiration = scn.GetFreepsCard("inspiration");
		var guard = scn.GetFreepsCard("guard");
		var faramir = scn.GetFreepsCard("faramir");
		var dwarvenarmor = scn.GetFreepsCard("dwarvenarmor");
		var arrow = scn.GetFreepsCard("arrow");
		var supplies = scn.GetFreepsCard("supplies");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCompanionsToTable(guard, faramir);
		scn.MoveCardsToHand(inspiration);
		// A possession nobody on the table may bear, an artifact and a condition.
		scn.MoveCardsToDiscard(dwarvenarmor, arrow, supplies);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.DISCARD, dwarvenarmor, arrow, supplies);
		assertTrue(scn.HasKeyword(faramir, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// Playing a card from the discard pile as an effect is a cost (Q-0062): with none to play, no play.
		assertFalse(scn.FreepsPlayAvailable(inspiration));
	}

	@Test
	public void IsNotPlayableWithoutARohanManThoughAHunterManAndARohanHobbitCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inspiration = scn.GetFreepsCard("inspiration");
		var faramir = scn.GetFreepsCard("faramir");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var coat = scn.GetFreepsCard("coat");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCompanionsToTable(faramir, merry);
		scn.MoveCardsToHand(inspiration);
		scn.MoveCardsToDiscard(sword, coat);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.SkipToPhase(Phase.MANEUVER);

		// A Man of [gondor] (a hunter) and a [rohan] Hobbit who is no Man, with one [rohan] companion and no Eowyn.
		assertEquals(Culture.GONDOR, faramir.getBlueprint().getCulture());
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertInZone(Zone.DISCARD, sword, coat);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(inspiration));
	}
}
