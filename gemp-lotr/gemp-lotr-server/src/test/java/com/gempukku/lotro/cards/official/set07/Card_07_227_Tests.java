package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_227_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "7_227");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, cost 2, valiant and nothing else
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or Eowyn can be spotted
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: a [rohan] Man ALLY, cost 1; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose

					// For the discard pile.
					put("chain", "4_278");      // Heavy Chain: [rohan] possession (armor), bearer must be a Man
					put("villagers", "4_273");  // Fight for the Villagers: [rohan] Skirmish event
					put("work", "4_297");       // Work for the Sword: [rohan] Skirmish event
					put("eorlingas", "4_275");  // Forth Eorlingas!: [rohan] Maneuver event
					put("stored", "4_296");     // Well Stored: [rohan] condition
					put("arrow", "9_46");       // The Red Arrow: [rohan] artifact
					put("armor", "1_92");       // Armor: [gondor] possession, bearer must be a Man
					put("swordarm", "1_116");   // Swordarm of the White Tower: [gondor] Skirmish event
					put("sword", "4_291");      // Sword of Rohan: [rohan] possession; only the Shadow player's copy is laid, in his own discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Éomer, Skilled Tactician
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Valiant. While you can spot a [rohan] Man, Éomer's twilight cost is -1.
		 * 	Fellowship: Play a [rohan] companion to take a [rohan] possession or [rohan] skirmish event into hand
		 * 	from your discard pile.
		 */

		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", eomer.getBlueprint().getTitle());
		assertEquals("Skilled Tactician", eomer.getBlueprint().getSubtitle());
		assertTrue(eomer.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, eomer.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, eomer.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, eomer.getBlueprint().getCardType());
		assertEquals(Race.MAN, eomer.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(eomer, Keyword.VALIANT));
		assertEquals(3, eomer.getBlueprint().getTwilightCost());
		assertEquals(8, eomer.getBlueprint().getStrength());
		assertEquals(3, eomer.getBlueprint().getVitality());
		assertEquals(6, eomer.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, eomer.getBlueprint().getSignet());
	}

	@Test
	public void WithARohanManCompanionToSpotEomerIsPlayedForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, aragorn);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		// Another companion in the hand, beside the [rohan] Man he spots, costs what it is printed at.
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(eomer);

		// The text names Éomer himself, so it applies to him as he is played (CR 4.2 "modifier").
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanHobbitAndAGondorManDoNotLowerHisCostAndARohanManAllyPlayedThenDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var aldor = scn.GetFreepsCard("aldor");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, aldor);
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();

		// A [rohan] companion who is no Man, and a Man who is not [rohan].
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetTwilightCost(eomer));

		scn.FreepsPlayCard(aldor);

		assertInZone(Zone.SUPPORT, aldor);
		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(eomer);

		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		// Two [rohan] companions: Merry is still no Man.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityPlaysARohanCompanionToTakeARohanPossessionOrSkirmishEventFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var aldor = scn.GetFreepsCard("aldor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var chain = scn.GetFreepsCard("chain");
		var villagers = scn.GetFreepsCard("villagers");
		var work = scn.GetFreepsCard("work");
		var eorlingas = scn.GetFreepsCard("eorlingas");
		var stored = scn.GetFreepsCard("stored");
		var arrow = scn.GetFreepsCard("arrow");
		var armor = scn.GetFreepsCard("armor");
		var swordarm = scn.GetFreepsCard("swordarm");
		var shadowSword = scn.GetShadowCard("sword");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(guard, merry, aldor, aragorn);
		scn.MoveCardsToDiscard(chain, villagers, work, eorlingas, stored, arrow, armor, swordarm, shadowSword);

		scn.StartGame();

		assertInHand(guard, merry, aldor, aragorn);
		assertInDiscard(chain, villagers, work, eorlingas, stored, arrow, armor, swordarm, shadowSword);
		assertTrue(scn.FreepsActionAvailable(eomer));

		scn.FreepsUseCardAction(eomer);

		// The cost: a [rohan] companion from hand, a Man or a Hobbit; not a [rohan] ally, nor a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aldor, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);

		// The effect: a [rohan] possession or a [rohan] skirmish event from his own discard pile.  Not a [rohan] event of
		// another phase, a [rohan] condition or artifact, a possession or skirmish event of another culture, nor a [rohan]
		// possession in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(chain, villagers, work));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eorlingas, stored, arrow, armor, swordarm, shadowSword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chain);

		assertInHand(chain);
		assertInDiscard(villagers, work, eorlingas, stored, arrow, armor, swordarm, shadowSword);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Again, with the [rohan] Hobbit paying, the only [rohan] companion left in hand (played unasked): a skirmish
		// event this time.
		assertTrue(scn.FreepsActionAvailable(eomer));
		scn.FreepsUseCardAction(eomer);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.FreepsHasCardChoiceAvailable(villagers, work));
		scn.FreepsChooseCard(villagers);

		assertInHand(villagers);
		assertInDiscard(work);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWithNoRohanCompanionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var aldor = scn.GetFreepsCard("aldor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(aldor, aragorn);
		scn.MoveCardsToDiscard(chain);

		scn.StartGame();

		assertInHand(aldor, aragorn);
		assertInDiscard(chain);
		// Both could be played: only the cost's filter keeps them from paying.
		assertTrue(scn.FreepsPlayAvailable(aldor));
		assertTrue(scn.FreepsPlayAvailable(aragorn));
		assertFalse(scn.FreepsActionAvailable(eomer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNothingToTakeTheAbilityIsOfferedAndTheCompanionIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var eorlingas = scn.GetFreepsCard("eorlingas");
		var stored = scn.GetFreepsCard("stored");
		var arrow = scn.GetFreepsCard("arrow");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(guard);
		scn.MoveCardsToDiscard(eorlingas, stored, arrow);

		scn.StartGame();

		assertInHand(guard);
		assertInDiscard(eorlingas, stored, arrow);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(eomer));

		// The Guard is the only [rohan] companion in hand, so he is played unasked.
		scn.FreepsUseCardAction(eomer);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertInDiscard(eorlingas, stored, arrow);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
