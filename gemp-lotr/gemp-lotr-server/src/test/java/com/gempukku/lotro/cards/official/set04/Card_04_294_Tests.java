package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_294_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("store", "4_294");

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3; valiant and nothing else
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally; a regroup ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions (or Eowyn) can be spotted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion, to bear Aiglos; text only against a Nazgul

					put("sword", "4_291");      // Sword of Rohan: [rohan] possession, hand weapon, on a [rohan] Man; text only against an Uruk-hai
					put("aiglos", "9_12");      // Aiglos: [elven] ARTIFACT, hand weapon, on an Elf; a regroup ability used only on purpose
					put("bow", "5_88");         // Rohirrim Bow: [rohan] possession, RANGED weapon, on a [rohan] Man; an archery ability
					put("shield", "5_91");      // Rohirrim Shield: [rohan] possession, shield, on a [rohan] Man; the minion archery total -1
					put("spear", "4_288");      // Rider's Spear: [rohan] possession, hand weapon, on a [rohan] Man; damage +1 while mounted
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeaponStoreStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Weapon Store
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	<b>Fellowship:</b> Exert a [rohan] Man to play a hand weapon from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("store");

		assertEquals("Weapon Store", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityExertsARohanManToPlayAHandWeaponFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var store = scn.GetFreepsCard("store");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var sword = scn.GetFreepsCard("sword");
		var aiglos = scn.GetFreepsCard("aiglos");
		var bow = scn.GetFreepsCard("bow");
		var shield = scn.GetFreepsCard("shield");
		var shadowSpear = scn.GetShadowCard("spear");
		scn.MoveCardsToSupportArea(store);
		scn.MoveCompanionsToTable(guard, merry, aragorn, arwen);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToDiscard(sword, aiglos, bow, shield, shadowSpear);
		assertInDiscard(sword, aiglos, bow, shield, shadowSpear);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsActionAvailable(store));
		scn.FreepsUseCardAction(store);
		// [rohan] Men, a companion and an ally; not Merry, a [rohan] Hobbit (two [rohan] companions only), Aragorn, a
		// [gondor] Man, or Arwen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, arwen));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));

		// Merry answers the exertion of another [rohan] Man, and is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();

		// Hand weapons of two cultures, a possession and an artifact; not a ranged weapon, a shield, or a hand weapon
		// in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, aiglos));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bow, shield, shadowSpear));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sword);

		// The Sword is played on a [rohan] Man, the ally chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman));
		scn.FreepsChooseCard(herdsman);
		assertAttachedTo(sword, herdsman);
		assertInDiscard(aiglos, bow, shield, shadowSpear);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheOnlyRohanManExhaustedTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var store = scn.GetFreepsCard("store");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToSupportArea(store);
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveCardsToDiscard(sword);
		scn.AddWoundsToChar(guard, 2);
		assertInDiscard(sword);
		assertEquals(2, scn.GetWoundsOn(guard));

		scn.StartGame();

		// Vitality 3 with 2 wounds: the Guard cannot exert.  Merry (no Man) and Aragorn ([gondor]) cannot pay.
		assertFalse(scn.FreepsActionAvailable(store));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInDiscard(sword);
	}

	@Test
	public void WithNoHandWeaponInTheDiscardPileThatCanBePlayedTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var store = scn.GetFreepsCard("store");
		var guard = scn.GetFreepsCard("guard");
		var aiglos = scn.GetFreepsCard("aiglos");
		var bow = scn.GetFreepsCard("bow");
		var shield = scn.GetFreepsCard("shield");
		var shadowSpear = scn.GetShadowCard("spear");
		scn.MoveCardsToSupportArea(store);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToDiscard(aiglos, bow, shield, shadowSpear);
		assertInDiscard(aiglos, bow, shield, shadowSpear);

		scn.StartGame();

		// The play is a cost (Q-0062): Aiglos has no Elf to bear it, the Bow and the Shield are no hand weapons, and
		// the Spear is in the other player's discard pile.  The Guard could exert.
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsActionAvailable(store));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInDiscard(aiglos, bow, shield, shadowSpear);
	}
}
