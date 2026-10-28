package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_109_Tests
{

	// The Fellowship path. The skirmishes are at site 2 (Ettenmoors), where a minion roams if its site number is 3
	// or more.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("prepared", "7_109");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, vitality 3; Knight is its whole text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, vitality 1; a maneuver ability never used
					// Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so that the Merchant can exert; its
					// response answers a wound to its bearer, who takes none here
					put("narsil", "9_34");
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability never used
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is its whole text
					// Stairs of Khazad-dum: another Free Peoples condition in the support area; its bonus is for a Dwarf
					put("stairs", "1_24");

					// Site number above 2: roaming at site 2.
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, site 4; no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, site 5; Damage +1 only
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, vitality 2, site 5; Damage +1 only
					// Site number 2: not roaming at site 2.
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: vitality 3; nothing else below 2 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two [gondor] Men, one to skirmish a minion that is not roaming at site 2 and one to skirmish a roaming one.
	protected VirtualTableScenario GetRoamingAndNotScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("prepared", "7_109");
					put("aragorn", "1_89");
					put("boromir", "1_97");
					put("nelya", "2_84");
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LongPreparedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Long Prepared
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, add 2 threats.
		 * 	<b>Skirmish:</b> Exert a [gondor] Man to wound a roaming minion that Man is skirmishing.
		 * 	Discard this condition at the start of the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("prepared");

		assertEquals("Long Prepared", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWhileTwoThreatsCannotBothBeAddedThenAddsTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prepared = scn.GetFreepsCard("prepared");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(prepared, knight);
		assertInHand(prepared, knight);

		scn.StartGame();
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		// Two companions and 1 threat: only 1 more can be added, and the cost is judged payable in full.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(prepared));

		// A third companion: room for 2 more.
		scn.FreepsPlayCard(knight);
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertTrue(scn.FreepsPlayAvailable(prepared));

		scn.FreepsPlayCard(prepared);
		assertInZone(Zone.SUPPORT, prepared);
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingAGondorManWoundsARoamingMinionHeIsSkirmishingAndNothingWhenHeIsSkirmishingNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prepared = scn.GetFreepsCard("prepared");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, knight, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(prepared);
		assertAttachedTo(narsil, merchant);
		assertInZone(Zone.SUPPORT, prepared);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, warrior, nelya);
		// The Uruk Savage, vitality 3, is exhausted.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{knight, warrior});
		// The minions left over are put on Aragorn by the Shadow player.
		scn.ShadowAssignToMinions(aragorn, savage, nelya);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(warrior, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertTrue(scn.IsCharSkirmishing(troop));
		assertTrue(scn.IsCharSkirmishing(savage));
		assertTrue(scn.IsCharSkirmishing(nelya));
		assertFalse(scn.IsCharSkirmishing(warrior));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(prepared));

		scn.FreepsUseCardAction(prepared);
		// [gondor] Men: companions in and out of this skirmish, and the ally; not a [gondor] Hobbit, a [rohan] Man
		// or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The roaming minions Aragorn is skirmishing, of two cultures and races, the exhausted one among them; not
		// Nelya, who is not roaming, nor the Orthanc Warrior, who roams but waits for the Knight.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nelya, warrior));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Knight exerts: he is skirmishing nobody yet, so no minion is wounded.
		scn.ShadowPass();
		scn.FreepsUseCardAction(prepared);
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn again: a wound, not an exertion, so the exhausted Savage can be chosen, and is killed.
		scn.ShadowPass();
		scn.FreepsUseCardAction(prepared);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(savage);
		assertInDiscard(savage);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheOnlyGondorManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prepared = scn.GetFreepsCard("prepared");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, pippin, guard);
		scn.MoveCardsToSupportArea(prepared);
		assertInZone(Zone.SUPPORT, prepared);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Vitality 4: exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// A roaming minion skirmishing him; a [gondor] Hobbit and a [rohan] Man can exert, he cannot.
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(prepared));
	}

	@Test
	public void IsDiscardedAtTheStartOfTheRegroupPhaseAndNotBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prepared = scn.GetFreepsCard("prepared");
		var stairs = scn.GetFreepsCard("stairs");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(prepared, stairs);
		assertInZone(Zone.SUPPORT, prepared, stairs);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();

		// Through the starts of the maneuver, archery and assignment phases it stays.
		assertInZone(Zone.SUPPORT, prepared);
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		assertInZone(Zone.SUPPORT, prepared);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// At the start of the regroup phase it is discarded, and the other condition is not.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(prepared);
		assertInZone(Zone.SUPPORT, stairs);
	}

	// "to wound a roaming minion that Man is skirmishing": the definition wounded any minion he was skirmishing.
	@Test
	public void OnlyARoamingMinionTheManIsSkirmishingIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingAndNotScenario();

		var prepared = scn.GetFreepsCard("prepared");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(prepared);

		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{boromir, savage});

		// At site 2 Úlairë Nelya (site number 2) is not roaming and the Uruk Savage (5) is.
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsUseCardAction(prepared);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 8 against 10: Aragorn loses and takes a wound.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsUseCardAction(prepared);
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
