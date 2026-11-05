package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_125_Tests
{

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("henneth", "4_125");
					// Madril, Defender of Osgiliath: an unbound [gondor] Man.  At the start of the maneuver phase each minion
					// is site number +1 for each threat until the regroup phase: with 2 threats, a site 5 or 6 minion is
					// roaming at site 6, and a site 4 minion is not.
					put("madril", "15_64");
					put("boromir", "0_65");       // Boromir, Steward's Heir: Ring-bound [gondor] Man, strength 7; keywords only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound Man, strength 8; a maneuver ability only

					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, vitality 2, site 6; answers a weapon played on him
					put("chieftain2", "1_266");
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, site 5, damage +1
					put("savage2", "1_151");
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1, site 4; harmless when cheated in
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("henneth", "4_125");
					put("madril", "15_64");       // Madril, Defender of Osgiliath: each minion site number +1 for each threat, maneuver to regroup
					put("boromir", "0_65");       // Boromir, Steward's Heir: Ring-bound [gondor] Man, strength 7; keywords only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man, strength 8; a maneuver ability only
					put("merchant", "7_97");      // Gondorian Merchant: the one [gondor] ally, a Man; a maneuver ability that needs initiative
					// Pippin, Trollslayer: a [gondor] Hobbit, no Man; enduring, and a skirmish ability used only on purpose
					put("pippin", "103_31");
					put("guard", "5_83");         // Household Guard: [rohan] Man companion, strength 6; Valiant only

					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, vitality 2, site 6
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, site 5, damage +1
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1, site 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HennethAnnunStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Henneth Annûn
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * While the fellowship is at site 6[T], each roaming minion skirmishing a Ring-bound companion is strength -2.
		 * <b>Skirmish:</b> Spot a [gondor] Man and discard this condition to wound a roaming minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("henneth");

		assertEquals("Henneth Annûn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtSite6TEachRoamingMinionSkirmishingARingBoundCompanionIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var frodo = scn.GetRingBearer();
		var henneth = scn.GetFreepsCard("henneth");
		var madril = scn.GetFreepsCard("madril");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(madril, boromir, aragorn);
		scn.MoveCardsToSupportArea(henneth);

		var chieftain = scn.GetShadowCard("chieftain");
		var chieftain2 = scn.GetShadowCard("chieftain2");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, henneth);
		scn.SkipToSite(5);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		// The fellowship moves to site 6, Caves of Aglarond.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(chieftain, chieftain2, savage, savage2, runner);
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(savage, Keyword.ROAMING));

		// Madril's trigger at the start of the maneuver phase: each site number +2.
		scn.SkipToAssignments();
		assertEquals(8, scn.GetMinionSiteNumber(chieftain));
		assertEquals(7, scn.GetMinionSiteNumber(savage));
		assertEquals(6, scn.GetMinionSiteNumber(runner));
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(chieftain2, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage2, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(runner, Keyword.ROAMING));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, chieftain},
				new PhysicalCardImpl[]{boromir, savage2},
				new PhysicalCardImpl[]{aragorn, chieftain2});
		// Two minions of other cultures and races on Frodo, and a minion who is not roaming beside Boromir's.
		scn.ShadowAssignToMinions(
				new PhysicalCardImpl[]{frodo, savage},
				new PhysicalCardImpl[]{boromir, runner});

		// Aragorn's skirmish first: the minions waiting for Frodo and Boromir are not yet skirmishing them, and the
		// roaming Orc skirmishing Aragorn, who is unbound, keeps his 7.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain2));
		assertTrue(scn.IsCharAssignedAgainst(frodo, chieftain));
		assertTrue(scn.IsCharAssignedAgainst(boromir, savage2));
		assertEquals(7, scn.GetStrength(chieftain2));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(savage2));
		// 8 against 7: Aragorn wins.
		scn.PassSkirmishActions();

		// Boromir, a Ring-bound Man: his roaming Uruk-hai is 5 - 2; the Runner beside it, not roaming, keeps 5.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, savage2));
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner));
		assertEquals(3, scn.GetStrength(savage2));
		assertEquals(5, scn.GetStrength(runner));
		// 7 against 3 + 5: Boromir loses and takes 2 wounds (damage +1), and lives.
		scn.PassSkirmishActions();

		// Frodo, the Ring-bound Ring-bearer: both roaming minions skirmishing him, 7 - 2 and 5 - 2.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, chieftain));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertEquals(5, scn.GetStrength(chieftain));
		assertEquals(3, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite5TARoamingMinionSkirmishingARingBoundCompanionKeepsItsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var henneth = scn.GetFreepsCard("henneth");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(henneth);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, henneth);
		scn.SkipToSite(4);
		// The fellowship moves to site 5, Deeping Wall, where a site 6 minion is roaming.
		scn.FreepsPass();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));

		scn.FreepsAssignToMinions(boromir, chieftain);
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, chieftain));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite7TARoamingMinionSkirmishingARingBoundCompanionKeepsItsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var henneth = scn.GetFreepsCard("henneth");
		var madril = scn.GetFreepsCard("madril");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(madril, boromir);
		scn.MoveCardsToSupportArea(henneth);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, henneth);
		scn.SkipToSite(6);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		// The fellowship moves to site 7, one past the site the text names.
		scn.FreepsPass();
		scn.MoveMinionsToTable(chieftain);
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));

		// Madril's trigger: site number 6 + 2, roaming at site 7.
		scn.SkipToAssignments();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetMinionSiteNumber(chieftain));
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));

		scn.FreepsAssignToMinions(boromir, chieftain);
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, chieftain));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite6OfTheFellowshipBlockARoamingMinionSkirmishingARingBoundCompanionKeepsItsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var henneth = scn.GetFreepsCard("henneth");
		var madril = scn.GetFreepsCard("madril");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(madril, boromir);
		scn.MoveCardsToSupportArea(henneth);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, henneth);
		scn.SkipToSite(5);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		// The fellowship moves to site 6 of the Fellowship block, Galadriel's Glade.
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		assertFalse(scn.HasKeyword(savage, Keyword.ROAMING));

		// Madril's trigger: site number 5 + 2.
		scn.SkipToAssignments();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));

		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, savage));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsItToWoundARoamingMinionInOrOutOfTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var henneth = scn.GetFreepsCard("henneth");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(henneth);

		var chieftain = scn.GetShadowCard("chieftain");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, henneth);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(chieftain, savage, runner);
		// Vitality 2 with one wound: exhausted, so that the wound kills him.
		scn.AddWoundsToChar(chieftain, 1);
		assertEquals(1, scn.GetWoundsOn(chieftain));

		// To Balin's Tomb, site 4: the site 6 Orc and the site 5 Uruk-hai are roaming there, the site 4 Runner is not.
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(runner, Keyword.ROAMING));
		scn.FreepsAssignToMinions(aragorn, savage);
		// The Runner goes to Aragorn too; the Orc Chieftain is left in no skirmish.
		scn.ShadowAssignToMinions(aragorn, runner);

		scn.FreepsResolveSkirmish(aragorn);
		assertInZone(Zone.SUPPORT, henneth);
		assertTrue(scn.FreepsActionAvailable(henneth));
		scn.FreepsUseCardAction(henneth);
		assertInZone(Zone.DISCARD, henneth);

		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chieftain);

		assertInZone(Zone.DISCARD, chieftain);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedUntilAGondorManIsSpottedAndAnAllyWillDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var henneth = scn.GetFreepsCard("henneth");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(pippin, guard);
		scn.MoveCardsToSupportArea(henneth);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, henneth);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		// Site 2: the site 5 Uruk-hai is roaming.
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		// A [gondor] Hobbit and a [rohan] Man: no [gondor] Man to spot.
		assertInZone(Zone.SUPPORT, henneth);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(henneth));

		// The [gondor] ally, away from his home: a spot does not ask where he is.
		scn.MoveAlliesToTable(merchant);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(henneth));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsUseCardAction(henneth);

		// The one roaming minion takes the wound unasked.
		assertInZone(Zone.DISCARD, henneth);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoRoamingMinionTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var henneth = scn.GetFreepsCard("henneth");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(henneth);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, henneth);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		// A site 4 minion at site 4.
		assertFalse(scn.HasKeyword(runner, Keyword.ROAMING));
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsActionAvailable(henneth));
		scn.FreepsUseCardAction(henneth);

		assertInZone(Zone.DISCARD, henneth);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
