package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vengeance", "1_106");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, ranger; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, ranger; +3 only against a Nazgul
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion who is no ranger; a skirmish ability

					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 only
					put("greed", "1_125");      // Greed: [isengard] condition in the support area; its maneuver ability needs 6 companions
					put("wrath", "4_261");      // Wrath of Harad: [raider] support condition; while a [raider] Man is spotted, each Free Peoples event that spots or exerts a ranger gains the cost "wound a companion"
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6, vitality 2, site 4; strength +3 and fierce only at 2 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorsVengeanceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gondor's Vengeance
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert a ranger companion to discard a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vengeance");

		assertEquals("Gondor's Vengeance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupEventExertsARangerCompanionToDiscardOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vengeance = scn.GetFreepsCard("vengeance");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, arwen, boromir);
		scn.MoveCardsToHand(vengeance);

		var patrol = scn.GetShadowCard("patrol");
		var savage = scn.GetShadowCard("savage");
		var greed = scn.GetShadowCard("greed");

		scn.StartGame();
		scn.MoveCardsToSupportArea(greed);
		assertInHand(vengeance);
		assertInZone(Zone.SUPPORT, greed);
		scn.FreepsPass();
		scn.MoveMinionsToTable(patrol, savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(vengeance);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SHADOW_CHARACTERS, patrol, savage);
		assertInZone(Zone.SUPPORT, greed);
		assertTrue(scn.FreepsPlayAvailable(vengeance));

		scn.FreepsPlayCard(vengeance);
		// Rangers of two cultures and races; not a companion who is no ranger, nor the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// A minion of either culture and race; not a Shadow card that is no minion, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(greed, aragorn, arwen, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// One minion, the one chosen.
		assertInDiscard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertInZone(Zone.SUPPORT, greed);
		assertInDiscard(vengeance);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyRangerCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vengeance = scn.GetFreepsCard("vengeance");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(vengeance);

		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInHand(vengeance);
		scn.FreepsPass();
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.REGROUP);

		// Vitality 4 with 3 wounds: Aragorn cannot exert. Boromir could, but he is no ranger; a minion is there to discard.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertInHand(vengeance);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(vengeance));
	}

	@Test
	public void WithNoMinionInPlayItIsPlayedAndTheRangerExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vengeance = scn.GetFreepsCard("vengeance");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(vengeance);

		var greed = scn.GetShadowCard("greed");

		scn.StartGame();
		scn.MoveCardsToSupportArea(greed);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, greed);
		assertTrue(scn.FreepsPlayAvailable(vengeance));

		scn.FreepsPlayCard(vengeance);

		// The cost is paid; there is no minion to discard, and the Shadow condition is not taken in its place.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(vengeance);
		assertInZone(Zone.SUPPORT, greed);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void UnderWrathOfHaradItAlsoCostsAWoundOnACompanionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vengeance = scn.GetFreepsCard("vengeance");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(vengeance);

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
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(vengeance));

		scn.FreepsPlayCard(vengeance);
		// The event exerts a ranger companion (Aragorn, the only one, unasked), so Wrath of Harad, with a [raider] Man
		// spotted, adds "wound a companion" to its cost: a cost of the Free Peoples player's event, which he pays and
		// so chooses (CR 4.2 "cost").
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The only minion is discarded unasked.
		assertInDiscard(axeman);
		assertInDiscard(vengeance);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
