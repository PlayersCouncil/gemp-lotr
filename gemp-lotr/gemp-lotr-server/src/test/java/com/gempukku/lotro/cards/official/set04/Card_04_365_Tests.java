package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_365_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "4_365");
					put("spear", "4_288");      // Rider's Spear: a [rohan] possession, cost 1; bearer must be a [rohan] Man
					put("mount", "4_287");      // Rider's Mount: a [rohan] possession, cost 1; bearer must be a [rohan] Man

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3, valiant and no other text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability only
					// Rohirrim Herdsman: [rohan] Man ally, home 3T; a regroup ability used only on purpose
					put("herdsman", "7_247");
					// Merry, Master Holbytla: a [rohan] Hobbit companion, vitality 4; a Man only while 3 [rohan]
					// companions (or Éowyn) can be spotted, so beside Théoden alone he is no Man.
					put("merry", "103_85");

					put("chain", "4_278");      // Heavy Chain: [rohan] possession, armor; bearer must be a Man
					put("sword", "4_291");      // Sword of Rohan: [rohan] possession, hand weapon; bearer must be a [rohan] Man
					put("banner", "6_90");      // Banner of the Mark: [rohan] possession, hand weapon; bearer must be a [rohan] companion
					put("mail", "1_101");       // Coat of Mail: [gondor] possession, armor; bearer must be a Man
					put("herugrim", "54_280");  // Herugrim: [rohan] ARTIFACT, hand weapon; bearer must be Théoden
					put("eowynsword", "4_272"); // Éowyn's Sword: [rohan] possession; bearer must be Éowyn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Théoden, Lord of the Mark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Fellowship:</b> Play a [rohan] possession on a [rohan] companion to heal that companion (limit once per turn).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", card.getBlueprint().getTitle());
		assertEquals("Lord of the Mark", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet());
	}

	@Test
	public void PlaysARohanPossessionOnTheChosenRohanCompanionAndHealsHimOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetFreepsCard("herdsman");
		var chain = scn.GetFreepsCard("chain");
		var sword = scn.GetFreepsCard("sword");
		var mail = scn.GetFreepsCard("mail");
		var herugrim = scn.GetFreepsCard("herugrim");
		scn.MoveCompanionsToTable(theoden, guard, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(chain, sword, mail, herugrim);
		scn.AddWoundsToChar(theoden, 1);
		scn.AddWoundsToChar(guard, 2);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertInHand(chain, sword, mail, herugrim);
		// The Coat of Mail and Herugrim can each be played as they are: only the ability's words keep them out.
		assertTrue(scn.FreepsPlayAvailable(mail));
		assertTrue(scn.FreepsPlayAvailable(herugrim));
		assertTrue(scn.FreepsActionAvailable(theoden));

		scn.FreepsUseCardAction(theoden);
		// Armor and a hand weapon, both [rohan] possessions; not a [gondor] possession, nor a [rohan] artifact.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(chain, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(mail, herugrim));
		scn.FreepsChooseCard(chain);

		// Heavy Chain may go on any Man: the ability lets only a [rohan] companion bear it, Théoden himself among
		// them, and not a [gondor] companion or a [rohan] ally.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(theoden, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, herdsman));
		scn.FreepsChooseCard(guard);

		assertAttachedTo(chain, guard);
		assertInHand(sword, mail, herugrim);
		// One of his two wounds healed; Théoden, also wounded, is not "that companion".
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanCompanionWhoIsAHobbitMayBearThePossessionAndIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var merry = scn.GetFreepsCard("merry");
		var banner = scn.GetFreepsCard("banner");
		scn.MoveCompanionsToTable(theoden, merry);
		scn.MoveCardsToHand(banner);
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertInHand(banner);

		scn.FreepsUseCardAction(theoden);
		// The Banner is the one possession, and is taken unasked; the bearer is asked.
		assertTrue(scn.FreepsHasCardChoiceAvailable(theoden, merry));
		scn.FreepsChooseCard(merry);

		assertAttachedTo(banner, merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNobodyWoundedTheAbilityIsOfferedAndThePossessionPlayedForNoHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInHand(sword);
		assertTrue(scn.FreepsActionAvailable(theoden));

		scn.FreepsUseCardAction(theoden);
		// Unwounded companions are bearers all the same.
		assertTrue(scn.FreepsHasCardChoiceAvailable(theoden, guard));
		scn.FreepsChooseCard(theoden);

		assertAttachedTo(sword, theoden);
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithNoRohanPossessionThatARohanCompanionCanBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var mail = scn.GetFreepsCard("mail");
		var herugrim = scn.GetFreepsCard("herugrim");
		var eowynsword = scn.GetFreepsCard("eowynsword");
		scn.MoveCompanionsToTable(theoden, guard);
		// A [gondor] possession, a [rohan] artifact, and a [rohan] possession that only Éowyn may bear.
		scn.MoveCardsToHand(mail, herugrim, eowynsword);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInHand(mail, herugrim, eowynsword);
		assertTrue(scn.FreepsPlayAvailable(mail));
		assertTrue(scn.FreepsPlayAvailable(herugrim));
		assertFalse(scn.FreepsPlayAvailable(eowynsword));
		assertFalse(scn.FreepsActionAvailable(theoden));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedAgainInTheTurnItWasUsed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");
		var spear = scn.GetFreepsCard("spear");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(card);
		scn.MoveCardsToHand(spear, mount);
		scn.AddWoundsToChar(card, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(card));
		assertInHand(spear, mount);

		scn.FreepsUseCardAction(card);
		// Theoden is the only [rohan] companion, so he is the bearer and the one healed.
		scn.FreepsChooseCard(spear);
		assertAttachedTo(spear, card);
		assertEquals(0, scn.GetWoundsOn(card));

		// A wound put back by the rig, and a second possession in hand that could be played: everything a
		// second use would need.
		scn.AddWoundsToChar(card, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(card));

		// Limit once per turn: a limit on uses, so the ability is not offered again (Ketura on Q-0093).
		assertFalse(scn.FreepsActionAvailable(card));
		assertTrue(scn.FreepsPlayAvailable(mount));
		assertInHand(mount);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// His next turn is turn 3, at site 2: the limit is spent no longer.
		scn.SkipToSite(2);
		scn.SetFreepsHand(mount);
		scn.FreepsRefreshDecision();
		assertInHand(mount);
		assertTrue(scn.FreepsActionAvailable(card));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
