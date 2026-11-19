package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("days", "12_71");

					// Swarthy Hillman: a [men] Man, cost 2, strength 7, vitality 1, site 4.  An archer only while its
					// player controls a site, and none is controlled.
					put("swarthy", "15_93");
					put("swarthy2", "15_93");
					// Bold Easterling: a [men] Man, cost 2, strength 8, vitality 1, site 4.  Its skirmish ability, which
					// discards it, is never used.
					put("easterling", "15_73");
					// Saruman, Coldly Still: a [men] minion who is no Man (Wizard), cost 4, strength 8, vitality 4, site 4.
					// His +2 is for [men] minions bearing a possession, and none does; his skirmish ability is never used.
					put("saruman", "17_48");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3, site 2.  His abilities
					// are never used.
					put("ted", "18_75");
					// Man of Bree: a [men] Man, vitality 1, site 4 (roaming at site 2); strength +1 for each card in the
					// Free Peoples hand.
					put("bree", "11_90");
					// Axeman of Harad: a [men] Man, strength 11, vitality 3.  Its text only lowers its own cost in hand.
					put("axeman", "11_69");
					// Dunlending Brigand: a [dunland] Man minion, cost 2, strength 8, vitality 1, site 3, no text.
					put("brigand", "4_10");
					put("brigand2", "4_10");
					// Harmless: a [men] condition, cost 2.  Its response is to a lost skirmish, never reached here.
					put("harmless", "13_93");

					// Aragorn, Ranger of the North: a [gondor] Man and ranger, strength 8, vitality 4.  A maneuver
					// ability, never used.
					put("aragorn", "1_89");
					// Ranger's Sword: strength +2 and damage +1 on Aragorn.
					put("sword", "1_112");
					// Gandalf, Friend of the Shirefolk: strength 6, +1 for each race in the fellowship.
					put("gandalf", "1_72");
					// Gandalf's Staff, Focus of Power: vitality +1 on Gandalf; +1 strength while 4 twilight are spotted.
					put("staff", "7_38");
					// Staff Asunder: Skirmish event.  "Discard a staff Gandalf is bearing and then exert him twice to
					// discard a minion he is skirmishing."
					put("asunder", "2_27");
					// What Are They?: Maneuver event.  "Spot a ranger to discard a roaming minion."
					put("whatare", "1_119");
					// Pursuit Just Behind: Regroup event.  "Exert a ranger companion to wound every minion."
					put("pursuit", "1_111");
					// Gondor's Vengeance: Regroup event.  "Exert a ranger companion to discard a minion."
					put("vengeance", "1_106");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A [men] minion of 1 vitality to be killed in a skirmish, and a [men] minion in hand to play.
	protected VirtualTableScenario GetKilledMinionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("days", "12_71");
					put("bree", "11_90");
					put("corps", "11_73");
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LastDaysStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Last Days
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a [men] minion is killed or discarded from play (except during the regroup phase), you may play a [men] minion. Its twilight cost is -2, and it is <b>fierce</b> and strength +2 until the regroup phase.
		*/

		var scn = GetScenario();

		var days = scn.GetFreepsCard("days");

		assertEquals("Last Days", days.getBlueprint().getTitle());
		assertNull(days.getBlueprint().getSubtitle());
		assertTrue(days.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, days.getBlueprint().getSide());
		assertEquals(Culture.MEN, days.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, days.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(days, Keyword.SUPPORT_AREA));
		assertEquals(0, days.getBlueprint().getTwilightCost());
	}

	// "Each time a [men] minion is killed or discarded from play".  A killed minion is not discarded (CR 4.2
	// "killed"; F-0444), so the definition has a `killed` trigger beside its `discarded` one: this holds the kill.
	// The discard, and one offer for each, are in mechanics/wounds/KilledIsNotDiscardedTests.
	@Test
	public void AMenMinionKilledInASkirmishLetsAMenMinionBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKilledMinionScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var days = scn.GetShadowCard("days");
		var bree = scn.GetShadowCard("bree");
		var corps = scn.GetShadowCard("corps");
		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(corps);
		assertInHand(corps);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		scn.SetTwilight(6);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, bree);
		scn.FreepsResolveSkirmish(aragorn);

		// 6 set + 2 companions + 2 for the site
		assertEquals(10, scn.GetTwilight());
		// 8 against 4: Man of Bree is killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, bree);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(days));
		scn.ShadowAcceptOptionalTrigger();

		// Corps of Harad is the only [men] minion in hand: 4 - 2, + 2 for roaming.
		assertInZone(Zone.SHADOW_CHARACTERS, corps);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.HasKeyword(corps, Keyword.FIERCE));
		assertEquals(12, scn.GetStrength(corps));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AMenMinionKilledInASkirmishOffersAMenMinionFromHandThatIsFierceAndStrongerUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var days = scn.GetShadowCard("days");
		var swarthy = scn.GetShadowCard("swarthy");
		var swarthy2 = scn.GetShadowCard("swarthy2");
		var easterling = scn.GetShadowCard("easterling");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var harmless = scn.GetShadowCard("harmless");
		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(easterling, saruman, brigand, harmless);

		scn.StartGame();
		scn.MoveMinionsToTable(swarthy, swarthy2);
		assertInZone(Zone.SUPPORT, days);
		assertInHand(easterling, saruman, brigand, harmless);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, swarthy);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);

		// Aragorn's 8 beats the Swarthy Hillman's 7: it is killed.
		scn.PassSkirmishActions();
		assertInDiscard(swarthy);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(days));
		scn.ShadowAcceptOptionalTrigger();

		// [men] minions in hand: Saruman's 4 + 2 roaming - 2 is just within the 4 in the pool.  Not the [dunland]
		// Man, the [men] condition, or the Hillman just killed, in the discard pile.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(easterling, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, harmless, swarthy));
		scn.ShadowChooseCard(easterling);

		assertInZone(Zone.SHADOW_CHARACTERS, easterling);
		// 2 to play + 2 roaming - 2.
		assertEquals(2, scn.GetTwilight());
		assertInHand(saruman, brigand, harmless);
		// 8 + 2, and fierce; the other Hillman in play is neither.
		assertTrue(scn.HasKeyword(easterling, Keyword.FIERCE));
		assertEquals(10, scn.GetStrength(easterling));
		assertFalse(scn.HasKeyword(swarthy2, Keyword.FIERCE));
		assertEquals(7, scn.GetStrength(swarthy2));

		// Fierce, it is assigned in the fierce round.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, easterling);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(easterling, Keyword.FIERCE));
		assertEquals(10, scn.GetStrength(easterling));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// 10 against 8: Aragorn loses.  In the regroup phase the Easterling is neither fierce nor stronger.
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(easterling, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(easterling));
	}

	@Test
	public void AMenMinionDiscardedInTheManeuverPhaseOffersAPlayAndASecondKillOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whatare = scn.GetFreepsCard("whatare");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatare);

		var days = scn.GetShadowCard("days");
		var bree = scn.GetShadowCard("bree");
		var swarthy = scn.GetShadowCard("swarthy");
		var easterling = scn.GetShadowCard("easterling");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(easterling, saruman);

		scn.StartGame();
		scn.MoveMinionsToTable(bree, swarthy);
		assertInZone(Zone.SUPPORT, days);
		assertInHand(whatare, easterling, saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);

		// What Are They? discards the Man of Bree, roaming at site 2, from play: no kill.
		assertEquals(0, scn.GetWoundsOn(bree));
		scn.FreepsPlayCard(whatare);
		scn.FreepsChooseCard(bree);
		assertInDiscard(bree);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(days));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(easterling);

		assertInZone(Zone.SHADOW_CHARACTERS, easterling);
		// 10 + 1 for What Are They?, less the Easterling's 2 to play + 2 roaming - 2.
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.HasKeyword(easterling, Keyword.FIERCE));
		assertEquals(10, scn.GetStrength(easterling));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, swarthy);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// Played in the maneuver phase, it is still fierce and +2 in the skirmish phase.
		assertTrue(scn.HasKeyword(easterling, Keyword.FIERCE));
		assertEquals(10, scn.GetStrength(easterling));
		int pool = scn.GetTwilight();

		// Aragorn's 8 beats the Swarthy Hillman's 7: a kill offers it again.  Declined, nothing is played.
		scn.PassSkirmishActions();
		assertInDiscard(swarthy);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(days));
		scn.ShadowDeclineOptionalTrigger();
		assertInHand(saruman);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AMenWizardKilledAndAMenHobbitDiscardedInASkirmishEachOfferIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var asunder = scn.GetFreepsCard("asunder");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(asunder);

		var days = scn.GetShadowCard("days");
		var saruman = scn.GetShadowCard("saruman");
		var ted = scn.GetShadowCard("ted");
		var easterling = scn.GetShadowCard("easterling");
		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(easterling);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, ted);
		// Vitality 4 with 3 wounds: one more kills Saruman.
		scn.AddWoundsToChar(saruman, 3);
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.SUPPORT, days);
		assertInHand(asunder, easterling);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, saruman},
				new PhysicalCardImpl[]{gandalf, ted});
		scn.SetTwilight(10);

		// Aragorn's 10 beats Saruman's 8: a [men] Wizard is killed.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(saruman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(days));
		scn.ShadowDeclineOptionalTrigger();
		assertInHand(easterling);

		// Staff Asunder discards Ted Sandyman, a [men] Hobbit, from play: no kill.
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(0, scn.GetWoundsOn(ted));
		scn.FreepsPlayCard(asunder);
		assertInDiscard(ted, staff);
		assertEquals(0, scn.GetWoundsOn(ted));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(days));
		scn.ShadowDeclineOptionalTrigger();
		assertInHand(easterling);
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MinionsOfAnotherCultureDiscardedOrKilledAndMenMinionsKilledOrDiscardedInTheRegroupPhaseOfferNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var whatare = scn.GetFreepsCard("whatare");
		var pursuit = scn.GetFreepsCard("pursuit");
		var vengeance = scn.GetFreepsCard("vengeance");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToHand(whatare, pursuit, vengeance);

		var days = scn.GetShadowCard("days");
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		var swarthy = scn.GetShadowCard("swarthy");
		var axeman = scn.GetShadowCard("axeman");
		var easterling = scn.GetShadowCard("easterling");
		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(easterling);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2, swarthy, axeman);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, days);
		assertInHand(whatare, pursuit, vengeance, easterling);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);

		// What Are They? discards a Brigand, a [dunland] Man roaming at site 2, from play.
		scn.FreepsPlayCard(whatare);
		scn.FreepsChooseCard(brigand2);
		assertInDiscard(brigand2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(days));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's 10 beats the other Brigand's 8: a [dunland] Man is killed.
		scn.PassSkirmishActions();
		assertInDiscard(brigand);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(days));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Pursuit Just Behind kills the Swarthy Hillman in the regroup phase.
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.FreepsPlayCard(pursuit);
		assertInDiscard(swarthy);
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(days));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowPass();

		// Gondor's Vengeance discards the Axeman in the regroup phase.
		scn.FreepsPlayCard(vengeance);
		assertInDiscard(axeman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(days));
		assertInHand(easterling);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMenMinionInHandItCanPayForAKillOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var days = scn.GetShadowCard("days");
		var swarthy = scn.GetShadowCard("swarthy");
		var easterling = scn.GetShadowCard("easterling");
		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(easterling);

		scn.StartGame();
		scn.MoveMinionsToTable(swarthy);
		assertInZone(Zone.SUPPORT, days);
		assertInHand(easterling);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, swarthy);
		scn.FreepsResolveSkirmish(aragorn);
		// The Easterling's 2 + 2 roaming - 2 is more than the 1 in the pool.
		scn.SetTwilight(1);

		scn.PassSkirmishActions();
		assertInDiscard(swarthy);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(days));
		assertInHand(easterling);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
