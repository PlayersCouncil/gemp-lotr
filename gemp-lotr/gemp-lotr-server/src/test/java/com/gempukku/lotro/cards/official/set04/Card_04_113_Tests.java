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

public class Card_04_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("curse", "4_113");
					put("curse2", "4_113");
					put("faramir", "4_117");      // Faramir, Son of Denethor: Ring-bound [gondor] Man; bars only Shadow skirmish actions
					put("boromir", "0_65");       // Boromir, Steward's Heir: Ring-bound [gondor] Man; keywords only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound Man, strength 8; a maneuver ability only

					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, vitality 2, site 6; answers a weapon played on him
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, site 5, damage +1
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1, site 4; harmless when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("curse", "4_113");
					put("faramir", "4_117");      // Faramir, Son of Denethor: Ring-bound [gondor] Man
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, site 6
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	protected VirtualTableScenario GetMultipathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("curse", "4_113");
					put("faramir", "4_117");      // Faramir, Son of Denethor: Ring-bound [gondor] Man
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, site 6
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
		);
	}

	@Test
	public void CurseThemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Curse Them
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert a Ring-bound Man to cancel a skirmish involving a roaming minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("curse");

		assertEquals("Curse Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsARingBoundManToCancelASkirmishInvolvingARoamingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var curse = scn.GetFreepsCard("curse");
		var curse2 = scn.GetFreepsCard("curse2");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, boromir, aragorn);
		scn.MoveCardsToHand(curse, curse2);

		var chieftain = scn.GetShadowCard("chieftain");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInHand(curse);
		assertInHand(curse2);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(chieftain, savage);

		// To Balin's Tomb, site 4: a site 6 Orc and a site 5 Uruk-hai are roaming there.
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, chieftain},
				new PhysicalCardImpl[]{boromir, savage});

		scn.FreepsResolveSkirmish(aragorn);
		assertInHand(curse);
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsPlayAvailable(curse));
		scn.FreepsPlayCard(curse);

		// Ring-bound Men of two titles; not the Ring-bearer (Ring-bound, no Man) or Aragorn (a Man, unbound).
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn));
		scn.FreepsChooseCard(faramir);

		// Cancelled: Aragorn's 8 would have beaten the Chieftain's 7 and wounded him.
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInZone(Zone.DISCARD, curse);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// A roaming Uruk-hai of [isengard]: his skirmish too is cancelled, paid for by Boromir, who is in it.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsPlayCard(curse2);
		scn.FreepsChooseCard(boromir);

		// Cancelled: Boromir's 7 would have beaten the Savage's 5 and wounded him.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.IsCharSkirmishing(boromir));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.DISCARD, curse2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InASkirmishOfAMinionWhoIsNotRoamingItIsPlayedAndCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var curse = scn.GetFreepsCard("curse");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, boromir, aragorn);
		scn.MoveCardsToHand(curse);

		var chieftain = scn.GetShadowCard("chieftain");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(curse);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(chieftain, runner);

		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		// A site 4 minion at site 4 is not roaming; the Chieftain is, and waits for Faramir's skirmish.
		assertFalse(scn.HasKeyword(runner, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{faramir, chieftain});

		scn.FreepsResolveSkirmish(aragorn);
		assertInHand(curse);
		assertEquals(0, scn.GetWoundsOn(boromir));
		// The exertion can be paid: the event is played, whatever its target.
		assertTrue(scn.FreepsPlayAvailable(curse));
		scn.FreepsPlayCard(curse);
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInZone(Zone.DISCARD, curse);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(faramir, chieftain));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The skirmish goes on: Aragorn's 8 against the Runner's 5, and the Runner dies.
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.IsCharAssignedAgainst(faramir, chieftain));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithTheOnlyRingBoundManExhaustedItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var curse = scn.GetFreepsCard("curse");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, aragorn);
		scn.MoveCardsToHand(curse);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		assertInHand(curse);
		scn.SkipToSite(3);
		// Vitality 3 with two wounds: exhausted. Frodo and Aragorn could exert, but they are no Ring-bound Men.
		scn.AddWoundsToChar(faramir, 2);
		scn.MoveMinionsToTable(chieftain);
		assertEquals(2, scn.GetWoundsOn(faramir));

		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(curse));
		assertInHand(curse);
	}

	@Test
	public void InTheFellowshipBlockFormatItCancelsTheRingBearersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var frodo = scn.GetRingBearer();
		var curse = scn.GetFreepsCard("curse");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToHand(curse);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		assertInHand(curse);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(chieftain);

		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		scn.FreepsAssignToMinions(frodo, chieftain);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, chieftain));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsPlayAvailable(curse));
		scn.FreepsPlayCard(curse);

		// Faramir is the one Ring-bound Man, and exerts unasked. Cancelled: the Chieftain's 7 would have beaten
		// Frodo's 4.
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, curse);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathItIsPlayedAndTheRingBearersSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathScenario();

		var frodo = scn.GetRingBearer();
		var curse = scn.GetFreepsCard("curse");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToHand(curse);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		assertInHand(curse);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(chieftain);

		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		scn.FreepsAssignToMinions(frodo, chieftain);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, chieftain));
		assertEquals(0, scn.GetWoundsOn(faramir));
		// The exertion can be paid, so the event is played; the skirmish is the Ring-bearer's and goes on.
		assertTrue(scn.FreepsPlayAvailable(curse));
		scn.FreepsPlayCard(curse);

		assertEquals(1, scn.GetWoundsOn(faramir));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, chieftain));
		assertInZone(Zone.DISCARD, curse);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
