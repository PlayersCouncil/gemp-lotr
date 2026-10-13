package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stronghold", "102_48");

					put("guard", "5_83");       // Household Guard: valiant [rohan] Man, strength 6, vitality 3; no other text
					put("elite", "4_265");      // Elite Rider: [rohan] Man who is not valiant; strength +2 against a wounded minion
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man who is not valiant; a skirmish ability while mounted
					put("rohirrim", "7_246");   // Rohirrim Guard: [rohan] Man who is not valiant; cost 2; a skirmish ability
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] ally, vitality 2; a regroup ability
					put("haldir", "102_8");     // Haldir, Naith Commander: valiant [elven] companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man
					// Merry, Master Holbytla: a [rohan] Hobbit companion, vitality 4, valiant.  He is a Man as well
					// only while 3 [rohan] companions (or Eowyn) can be spotted; his trigger answers another [rohan]
					// Man exerting, and his skirmish ability is never used.
					put("merry", "103_85");

					put("chain", "4_278");      // Heavy Chain: [rohan] armor, bearer must be a Man
					put("spear", "4_288");      // Rider's Spear: [rohan] hand weapon, strength +2
					put("sword", "4_291");      // Sword of Rohan: [rohan] hand weapon, strength +2
					put("helm", "5_89");        // Rohirrim Helm: [rohan] helm with a skirmish ability
					put("longspear", "7_240");  // Long Spear: [rohan] hand weapon, kept in hand
					put("mail", "1_101");       // Coat of Mail: [gondor] armor, bearer must be a Man
					put("doom", "8_86");        // Doom Drove Them: [rohan] condition in the support area
					put("arrow", "9_46");       // The Red Arrow: [rohan] artifact in the support area

					put("slayer", "3_93");      // Morgul Slayer: Regroup: exert this minion to wound a companion
					put("slayer2", "3_93");     //   (except the Ring-bearer)
					put("assassin", "2_93");    // Tower Assassin: Maneuver: exert to wound an ally
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrongholdofMyFathersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Stronghold of My Fathers
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Fortification. To play, spot 3 [rohan] companions (or 2 valiant companions).
		 * 	Response: If a [rohan] companion is about to take a wound, discard this or 2 other [rohan] possessions to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stronghold");

		assertEquals("Stronghold of My Fathers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAThirdRohanCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var elite = scn.GetFreepsCard("elite");
		var rider = scn.GetFreepsCard("rider");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Two [rohan] companions, a [rohan] ally, and two companions of other cultures (Aragorn and Frodo).
		// Nobody here is valiant.
		scn.MoveCompanionsToTable(elite, rider, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(stronghold, rohirrim);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(stronghold));

		scn.FreepsPlayCard(rohirrim);
		// 2 for the Rohirrim Guard
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(stronghold));

		scn.FreepsPlayCard(stronghold);
		assertInZone(Zone.SUPPORT, stronghold);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWithTwoValiantCompanionsOfAnyCultureInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var haldir = scn.GetFreepsCard("haldir");
		var elite = scn.GetFreepsCard("elite");
		var guard = scn.GetFreepsCard("guard");
		// One valiant companion (an Elf) and one [rohan] companion who is not valiant.
		scn.MoveCompanionsToTable(haldir, elite);
		scn.MoveCardsToHand(stronghold, guard);

		scn.StartGame();

		assertTrue(scn.HasKeyword(haldir, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(elite, Keyword.VALIANT));
		assertFalse(scn.FreepsPlayAvailable(stronghold));

		// The Household Guard is the second valiant companion and only the second [rohan] companion.
		scn.FreepsPlayCard(guard);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(stronghold));

		scn.FreepsPlayCard(stronghold);
		assertInZone(Zone.SUPPORT, stronghold);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardingStrongholdPreventsAWoundWhenThereAreNotTwoOtherRohanPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var guard = scn.GetFreepsCard("guard");
		var elite = scn.GetFreepsCard("elite");
		var spear = scn.GetFreepsCard("spear");
		var mail = scn.GetFreepsCard("mail");
		var doom = scn.GetFreepsCard("doom");
		var arrow = scn.GetFreepsCard("arrow");
		scn.MoveCompanionsToTable(guard, elite);
		// One other [rohan] possession; beside it a [gondor] possession, a [rohan] condition and a [rohan] artifact.
		scn.AttachCardsTo(guard, spear);
		scn.AttachCardsTo(elite, mail);
		scn.MoveCardsToSupportArea(stronghold, doom, arrow);

		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, slayer2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(guard));
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard);

		// Declined, the wound is taken.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stronghold));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.SUPPORT, stronghold);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(guard);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stronghold));
		scn.FreepsChooseOptionalTrigger(stronghold);

		// Only one of the two costs can be paid, so it is paid without asking.
		assertInDiscard(stronghold);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertAttachedTo(spear, guard);
		assertAttachedTo(mail, elite);
		assertInZone(Zone.SUPPORT, doom, arrow);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoOtherRohanPossessionsMayBeDiscardedInsteadAndEachUsePreventsOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var guard = scn.GetFreepsCard("guard");
		var elite = scn.GetFreepsCard("elite");
		var chain = scn.GetFreepsCard("chain");
		var spear = scn.GetFreepsCard("spear");
		var sword = scn.GetFreepsCard("sword");
		var helm = scn.GetFreepsCard("helm");
		var longspear = scn.GetFreepsCard("longspear");
		var mail = scn.GetFreepsCard("mail");
		var doom = scn.GetFreepsCard("doom");
		var arrow = scn.GetFreepsCard("arrow");
		scn.MoveCompanionsToTable(guard, elite);
		// Four other [rohan] possessions in play, on the companion who is wounded and on another.
		scn.AttachCardsTo(guard, chain, spear);
		scn.AttachCardsTo(elite, sword, helm);
		// Not [rohan] possessions in play: a [gondor] possession, a [rohan] condition, a [rohan] artifact,
		// and a [rohan] possession in hand.
		scn.AttachCardsTo(elite, mail);
		scn.MoveCardsToSupportArea(stronghold, doom, arrow);
		scn.MoveCardsToHand(longspear);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();

		// 6 + 2 for the Rider's Spear against 9: the Household Guard loses, and to a minion with damage +1
		// that is 2 wounds.
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(9, scn.GetStrength(raiders));
		scn.FreepsAssignAndResolve(guard, raiders);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stronghold));
		scn.FreepsChooseOptionalTrigger(stronghold);

		assertTrue(scn.FreepsChoiceAvailable("Discard Stronghold"));
		assertTrue(scn.FreepsChoiceAvailable("Discard 2 other"));
		scn.FreepsChoose("Discard 2 other");

		assertTrue(scn.FreepsHasCardChoiceAvailable(chain, spear, sword, helm));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stronghold, mail, doom, arrow, longspear));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(chain, sword);

		assertInDiscard(chain, sword);
		assertInZone(Zone.SUPPORT, stronghold);
		assertAttachedTo(spear, guard);
		assertAttachedTo(helm, elite);
		assertEquals(0, scn.GetWoundsOn(guard));

		// The second wound of the same skirmish is another wound to prevent.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stronghold));
		scn.FreepsChooseOptionalTrigger(stronghold);

		assertTrue(scn.FreepsChoiceAvailable("Discard Stronghold"));
		assertTrue(scn.FreepsChoiceAvailable("Discard 2 other"));
		scn.FreepsChoose("Discard Stronghold");

		assertInDiscard(stronghold);
		assertAttachedTo(spear, guard);
		assertAttachedTo(helm, elite);
		assertAttachedTo(mail, elite);
		assertInZone(Zone.SUPPORT, doom, arrow);
		assertInHand(longspear);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedForAWoundToACompanionOfAnotherCultureOrToARohanAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetFreepsCard("stronghold");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var spear = scn.GetFreepsCard("spear");
		var helm = scn.GetFreepsCard("helm");
		// Merry, Master Holbytla is the one [rohan] companion in print who is no Man: with only two [rohan]
		// companions on the table he is a Hobbit and nothing else.
		scn.MoveCompanionsToTable(guard, aragorn, merry);
		scn.MoveAlliesToTable(herdsman);
		// Either cost can be paid.
		scn.AttachCardsTo(guard, spear, helm);
		scn.MoveCardsToSupportArea(stronghold);

		var assassin = scn.GetShadowCard("assassin");
		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, slayer, slayer2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A [rohan] ally: the Herdsman is the only ally, so the Tower Assassin wounds him.
		assertEquals(0, scn.GetWoundsOn(herdsman));
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// A Man of another culture.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A [rohan] companion who is a Hobbit and no Man.
		scn.FreepsPass();
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(merry);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stronghold));
		scn.FreepsChooseOptionalTrigger(stronghold);
		scn.FreepsChoose("Discard Stronghold");

		assertInDiscard(stronghold);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertAttachedTo(spear, guard);
		assertAttachedTo(helm, guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
