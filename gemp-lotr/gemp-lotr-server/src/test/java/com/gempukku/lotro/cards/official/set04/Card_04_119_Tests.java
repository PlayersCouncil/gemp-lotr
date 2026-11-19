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

public class Card_04_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cloak", "4_119");
					put("faramir", "4_117");      // Faramir, Son of Denethor: Ring-bound [gondor] Man, strength 7; bars only Shadow skirmish actions
					put("boromir", "0_65");       // Boromir, Steward's Heir: Ring-bound [gondor] Man, strength 7; keywords only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound Man, strength 8; a maneuver ability only

					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, site 6; answers a weapon played on him
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, site 5, damage +1
					put("savage2", "1_151");
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, site 4; harmless when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirsCloakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Faramir's Cloak
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Cloak
		 * Game Text: Bearer must be Faramir.<br>Each roaming minion skirmishing a Ring-bound Man is strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cloak");

		assertEquals("Faramir's Cloak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.CLOAK));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BearerMustBeFaramir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToHand(cloak, faramir);

		scn.StartGame();
		assertInHand(cloak);
		assertInHand(faramir);

		// A Ring-bound [gondor] Man, an unbound one and Frodo: no Faramir, so nobody can bear it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(cloak));

		scn.FreepsPlayCard(faramir);
		assertInZone(Zone.FREE_CHARACTERS, faramir);

		// Faramir is the one bearer it can have, and it goes on him unasked.
		assertTrue(scn.FreepsPlayAvailable(cloak));
		scn.FreepsPlayCard(cloak);
		assertAttachedTo(cloak, faramir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachRoamingMinionSkirmishingFaramirIsStrengthMinus1AndNotWhileWaitingForHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, aragorn);
		scn.AttachCardsTo(faramir, cloak);

		var chieftain = scn.GetShadowCard("chieftain");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertAttachedTo(cloak, faramir);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(chieftain, savage, runner);

		// To Balin's Tomb, site 4: a site 6 Orc and a site 5 Uruk-hai are roaming there.
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, chieftain},
				new PhysicalCardImpl[]{aragorn, runner});
		// The Savage goes to Faramir too, so that two roaming minions of other cultures and races skirmish him.
		scn.ShadowAssignToMinions(faramir, savage);
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(savage));

		// Aragorn's skirmish first: the two are assigned to Faramir but not yet skirmishing him.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharAssignedAgainst(faramir, chieftain));
		assertTrue(scn.IsCharAssignedAgainst(faramir, savage));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(savage));
		// 8 against 5: Aragorn wins and the Runner dies.
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, chieftain));
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, savage));
		// 7 - 1 and 5 - 1
		assertEquals(6, scn.GetStrength(chieftain));
		assertEquals(4, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ItReachesARingBoundManWhoIsNotTheBearerAndNoOtherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var cloak = scn.GetFreepsCard("cloak");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, boromir, aragorn);
		scn.AttachCardsTo(faramir, cloak);

		var chieftain = scn.GetShadowCard("chieftain");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertAttachedTo(cloak, faramir);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(chieftain, savage, savage2, runner);

		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage2, Keyword.ROAMING));
		// A site 4 minion at site 4 is not roaming.
		assertFalse(scn.HasKeyword(runner, Keyword.ROAMING));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, savage},
				new PhysicalCardImpl[]{aragorn, chieftain},
				new PhysicalCardImpl[]{faramir, runner},
				new PhysicalCardImpl[]{frodo, savage2});

		// A roaming minion skirmishing a Ring-bound Man who does not bear the Cloak: 5 - 1.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, savage));
		assertEquals(4, scn.GetStrength(savage));
		scn.PassSkirmishActions();

		// A roaming minion skirmishing an unbound Man keeps his 7.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertEquals(7, scn.GetStrength(chieftain));
		scn.PassSkirmishActions();

		// A minion who is not roaming, skirmishing the bearer, keeps his 5.
		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, runner));
		assertEquals(5, scn.GetStrength(runner));
		scn.PassSkirmishActions();

		// A roaming minion skirmishing the Ring-bearer, Ring-bound and no Man, keeps his 5.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage2));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
