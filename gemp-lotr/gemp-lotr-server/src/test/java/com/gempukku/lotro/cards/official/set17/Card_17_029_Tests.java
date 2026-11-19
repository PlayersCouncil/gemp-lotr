package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "17_29");

					put("faramir", "4_117");    // Faramir, Son of Denethor: [gondor] Man ranger, vitality 3; bars the opponent's skirmish events and abilities in his skirmishes
					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] Man ranger, vitality 3; a regroup ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion who is no ranger; Knight is its whole text
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] Elf ranger; +3 only against a Nazgul

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, vitality 3, site 4; no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, site 5; Damage +1 only
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, site 5; Damage +1 only
					put("siege", "8_106");      // Siege Troop: [sauron] Orc, vitality 4 (exhaust is 3 wounds, exert twice is 2); its regroup ability is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirsBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Faramir's Bow, Ithilien Longbow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Game Text: Bearer must be a [gondor] ranger.<br>If bearer is Faramir, he is an <b>archer</b>.<br>The site number of each minion skirmishing a [gondor] ranger is +1.<br><b>Skirmish:</b> Exert bearer twice to exhaust a minion Faramir is skirmishing.
		*/

		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");

		assertEquals("Faramir's Bow", bow.getBlueprint().getTitle());
		assertEquals("Ithilien Longbow", bow.getBlueprint().getSubtitle());
		assertTrue(bow.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, bow.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, bow.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, bow.getBlueprint().getCardType());
		assertTrue(bow.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, bow.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnAGondorRangerAndBorneByOneWhoIsNotFaramirMakesNobodyAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var arwen = scn.GetFreepsCard("arwen");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(faramir, ranger, knight, arwen);
		scn.MoveCardsToHand(bow);
		assertInHand(bow);

		scn.StartGame();

		assertFalse(scn.HasKeyword(faramir, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(ranger, Keyword.ARCHER));
		assertTrue(scn.FreepsPlayAvailable(bow));

		scn.FreepsPlayCard(bow);
		// Two [gondor] rangers; not a [gondor] Man who is no ranger, nor a ranger of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, ranger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, arwen, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ranger);

		assertEquals(ranger, bow.getAttachedTo());
		// Borne by a ranger who is not Faramir: neither he nor Faramir is an archer.
		assertFalse(scn.HasKeyword(ranger, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(faramir, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByFaramirHeIsAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(faramir, ranger);
		scn.MoveCardsToHand(bow);
		assertInHand(bow);

		scn.StartGame();

		assertFalse(scn.HasKeyword(faramir, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(ranger, Keyword.ARCHER));

		scn.FreepsPlayCard(bow);
		scn.FreepsChooseCard(faramir);

		assertEquals(faramir, bow.getAttachedTo());
		assertTrue(scn.HasKeyword(faramir, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(ranger, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMinionSkirmishingAGondorRangerIsSiteNumberPlusOneWhileThatSkirmishLasts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(faramir, ranger, knight, arwen);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, savage, warrior);
		scn.SkipToAssignments();
		// Put on Faramir after the archery phase, so that his archery does not get in the way.
		scn.AttachCardsTo(faramir, bow);
		assertEquals(faramir, bow.getAttachedTo());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ranger, troop1}, new PhysicalCardImpl[]{knight, troop2},
				new PhysicalCardImpl[]{arwen, warrior});
		// The Savage, left over, is put on the Ranger beside the first Troop.
		scn.ShadowAssignToMinions(ranger, savage);

		// Assigned, but no skirmish has begun: every minion has its printed number.
		assertEquals(4, scn.GetMinionSiteNumber(troop1));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(4, scn.GetMinionSiteNumber(troop2));
		assertEquals(5, scn.GetMinionSiteNumber(warrior));

		// A minion skirmishing a [gondor] Man who is no ranger is not +1; nor are those assigned to the Ranger,
		// whose skirmish has not begun.
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharSkirmishing(troop2));
		assertEquals(4, scn.GetMinionSiteNumber(troop2));
		assertEquals(4, scn.GetMinionSiteNumber(troop1));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		scn.PassSkirmishActions();

		// The Ranger's skirmish: both minions skirmishing him, of two cultures and races, are +1, though he is not
		// the bearer.
		scn.FreepsResolveSkirmish(ranger);
		assertEquals(5, scn.GetMinionSiteNumber(troop1));    // 4 + 1
		assertEquals(6, scn.GetMinionSiteNumber(savage));    // 5 + 1
		assertEquals(5, scn.GetMinionSiteNumber(warrior));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Once that skirmish is over, they are back to their printed numbers.
		scn.PassSkirmishActions();
		assertEquals(4, scn.GetMinionSiteNumber(troop1));
		assertEquals(5, scn.GetMinionSiteNumber(savage));

		// A minion skirmishing a ranger of another culture is not +1.
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishing(warrior));
		assertEquals(5, scn.GetMinionSiteNumber(warrior));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsFaramirTwiceToExhaustAMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		var ranger = scn.GetFreepsCard("ranger");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(faramir, ranger);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");
		var siege = scn.GetShadowCard("siege");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, savage, siege);
		scn.SkipToAssignments();
		scn.AttachCardsTo(faramir, bow);
		assertEquals(faramir, bow.getAttachedTo());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{faramir, troop1}, new PhysicalCardImpl[]{ranger, troop2});
		scn.ShadowAssignToMinions(faramir, savage, siege);

		scn.FreepsResolveSkirmish(faramir);
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(siege));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		assertEquals(2, scn.GetWoundsOn(faramir));

		// Any minion skirmishing Faramir; not the one assigned to the Ranger.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage, siege));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2, faramir, ranger, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(siege);

		// Vitality 4: exhausted is 3 exertions, where exerting twice would be 2.
		assertEquals(3, scn.GetWoundsOn(siege));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void BorneByAnotherRangerItsAbilityExhaustsAMinionFaramirIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(faramir, ranger);
		scn.AttachCardsTo(ranger, bow);
		assertEquals(ranger, bow.getAttachedTo());

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{faramir, troop1}, new PhysicalCardImpl[]{ranger, troop2});
		scn.ShadowAssignToMinions(faramir, savage);

		scn.FreepsResolveSkirmish(faramir);
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		// The bearer pays, though the skirmish is Faramir's.
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(faramir));

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2));
		scn.FreepsChooseCard(troop1);

		assertEquals(2, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheBearersOwnSkirmishWithFaramirStillWaitingTheAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(faramir, ranger);
		scn.AttachCardsTo(ranger, bow);
		assertEquals(ranger, bow.getAttachedTo());

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{faramir, troop1}, new PhysicalCardImpl[]{ranger, troop2});

		// The Ranger's skirmish first: Faramir is assigned and his skirmish is still to come.
		scn.FreepsResolveSkirmish(ranger);
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		assertEquals(2, scn.GetWoundsOn(ranger));
		// Neither the minion skirmishing the bearer nor the one waiting for Faramir is exhausted.
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileTheBearerCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);
		// Vitality 3 with 1 wound: one exertion left in him, not two.
		scn.AddWoundsToChar(faramir, 1);
		assertEquals(1, scn.GetWoundsOn(faramir));

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.AttachCardsTo(faramir, bow);
		assertEquals(faramir, bow.getAttachedTo());
		scn.FreepsAssignToMinions(faramir, troop1);

		scn.FreepsResolveSkirmish(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
	}
}
