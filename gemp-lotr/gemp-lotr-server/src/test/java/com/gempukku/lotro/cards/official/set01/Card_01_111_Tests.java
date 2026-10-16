package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pursuit", "1_111");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, ranger; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, ranger; +3 only against a Nazgul
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion who is no ranger; a skirmish ability
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally; a fellowship ability used only on purpose

					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, vitality 1, no text: a wound kills him, and an exertion could not touch him
					put("wrath", "4_261");      // Wrath of Harad: [raider] support condition; while a [raider] Man is spotted, each Free Peoples event that spots or exerts a ranger gains the cost "wound a companion"
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6, vitality 2, site 4; strength +3 and fierce only at 2 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PursuitJustBehindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Pursuit Just Behind
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert a ranger companion to wound every minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pursuit");

		assertEquals("Pursuit Just Behind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupEventExertsARangerCompanionToWoundEveryMinionOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuit = scn.GetFreepsCard("pursuit");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, arwen, boromir);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(pursuit);

		var patrol = scn.GetShadowCard("patrol");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertInHand(pursuit);
		scn.FreepsPass();
		scn.MoveMinionsToTable(patrol, savage, brigand);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(pursuit);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, patrol, savage, brigand);
		assertTrue(scn.FreepsPlayAvailable(pursuit));

		scn.FreepsPlayCard(pursuit);
		// Rangers of two cultures and races; not a companion who is no ranger, nor the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		// Every minion, of three cultures and races, takes one wound (vitality 3 leaves room for more); the Brigand,
		// at vitality 1, is killed by it, which an exertion could not do. No companion or ally is wounded.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, patrol, savage);
		assertInDiscard(brigand);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(pursuit);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyRangerCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuit = scn.GetFreepsCard("pursuit");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(pursuit);

		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInHand(pursuit);
		scn.FreepsPass();
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.REGROUP);

		// Vitality 4 with 3 wounds: Aragorn cannot exert. Boromir could, but he is no ranger; a minion is there to wound.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertInHand(pursuit);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(pursuit));
	}

	@Test
	public void WithNoMinionInPlayItIsPlayedAndTheRangerExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuit = scn.GetFreepsCard("pursuit");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(pursuit);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsPlayAvailable(pursuit));

		scn.FreepsPlayCard(pursuit);

		// The cost is paid; there is no minion to wound, and no companion is wounded in its place.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInDiscard(pursuit);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void UnderWrathOfHaradItAlsoCostsAWoundOnACompanionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuit = scn.GetFreepsCard("pursuit");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(pursuit);

		var wrath = scn.GetShadowCard("wrath");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(wrath);
		assertInZone(Zone.SUPPORT, wrath);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertInZone(Zone.SUPPORT, wrath);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(pursuit));

		scn.FreepsPlayCard(pursuit);
		// The event exerts a ranger companion (Aragorn, the only one, unasked), so Wrath of Harad, with a [raider] Man
		// spotted, adds "wound a companion" to its cost: a cost of the Free Peoples player's event, which he pays and
		// so chooses (CR 4.2 "cost").
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Vitality 2: the Axeman lives through the one wound.
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertInDiscard(pursuit);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
