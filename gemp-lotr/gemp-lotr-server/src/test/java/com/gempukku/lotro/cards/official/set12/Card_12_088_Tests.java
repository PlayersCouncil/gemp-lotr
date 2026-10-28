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

public class Card_12_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cost", "12_88");
					put("cost2", "12_88");

					// Vile Goblin: an [orc] Orc, strength 10; fierce only when assigned to a Dwarf, so never put on Gimli.
					put("vile", "12_106");
					// Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8.  Its start-of-assignment trigger
					// plays an [orc] Orc from the discard pile, which holds none.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc, strength 13, no text.
					put("troop", "1_177");
					// Orc Skulker: an [orc] Orc, kept in the Shadow hand.
					put("skulker", "12_95");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6, vitality 3.  A skirmish ability, never used.
					put("gimli", "1_13");
					// Boromir, Son of Denethor: a [gondor] Man, strength 7, vitality 3.  A skirmish ability, never used.
					put("boromir", "1_97");
					// Sam, Son of Hamfast: a [shire] Hobbit.  His abilities are never used.
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: a [shire] Hobbit; strength +2 only with a weapon.
					put("merry", "1_303");
					// Thrarin, Dwarven Smith: a Dwarf ally; a maneuver ability, never used.
					put("thrarin", "1_27");
					// Dwarven Axe and Axe Strike: a possession and an event for the Free Peoples hand.
					put("axe", "1_9");
					put("strike", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatCostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Great Cost
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: At the start of each skirmish involving an [orc] minion, you may spot 6 companions to exert a companion in that skirmish.<br><b>Shadow:</b> Discard this condition and spot an [orc] minion to make the Free Peoples player place a card from his or her hand on top of his or her draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("cost");

		assertEquals("Great Cost", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheStartOfEachSkirmishInvolvingAnOrcMinionWithSixCompanionsItMayExertTheCompanionInThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		// Six companions with Frodo.
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam, merry);

		var cost = scn.GetShadowCard("cost");
		var vile = scn.GetShadowCard("vile");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(cost);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, cost);
		scn.MoveMinionsToTable(vile, halftroll, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, troop},
				new PhysicalCardImpl[]{aragorn, vile},
				new PhysicalCardImpl[]{gimli, halftroll});

		// A skirmish against a [moria] Orc: no offer.
		scn.FreepsResolveSkirmish(boromir);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cost));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();
		// 7 against 13: one wound for the loss
		assertEquals(1, scn.GetWoundsOn(boromir));

		// A skirmish against an [orc] Orc: offered, and Aragorn, the companion in it, exerts.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cost));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(vile));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();
		// 8 against 10: a second wound for the loss
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The next, against an [orc] minion who is no Orc: offered again; declined, nobody exerts.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cost));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithFiveCompanionsAndAnAllyItMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Five companions with Frodo, and an ally, who is no companion.
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveAlliesToTable(thrarin);

		var cost = scn.GetShadowCard("cost");
		var vile = scn.GetShadowCard("vile");
		scn.MoveCardsToSupportArea(cost);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, cost);
		scn.MoveMinionsToTable(vile);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, vile);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cost));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheCompanionInThatSkirmishExhaustedItMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam, merry);
		// Aragorn exhausted: 3 wounds on vitality 4.  The other five could still exert.
		scn.AddWoundsToChar(aragorn, 3);

		var cost = scn.GetShadowCard("cost");
		var vile = scn.GetShadowCard("vile");
		scn.MoveCardsToSupportArea(cost);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, cost);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(vile);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, vile);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cost));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ShadowAbilityDiscardsThisCopyToHaveTheFreePeoplesPlayerPutACardOfHisHandOnHisDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var strike = scn.GetFreepsCard("strike");

		var cost = scn.GetShadowCard("cost");
		var cost2 = scn.GetShadowCard("cost2");
		var vile = scn.GetShadowCard("vile");
		var skulker = scn.GetShadowCard("skulker");
		scn.MoveCardsToSupportArea(cost, cost2);

		scn.StartGame();
		scn.SetFreepsHand(gimli, axe, strike);
		scn.SetShadowHand(skulker);
		scn.MoveMinionsToTable(vile);
		assertInZone(Zone.SUPPORT, cost, cost2);
		assertInHand(gimli, axe, strike, skulker);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(cost));
		scn.ShadowUseCardAction(cost);

		assertInDiscard(cost);
		assertInZone(Zone.SUPPORT, cost2);
		// The Free Peoples player chooses, from a companion, a possession and an event; he must choose one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, axe, strike));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);

		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertInHand(gimli, strike);
		assertEquals(2, scn.GetFreepsHandCount());
		// The Shadow player's own hand is not touched.
		assertInHand(skulker);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityWantsAnOrcMinionAndWithAnEmptyHandPlacesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cost = scn.GetShadowCard("cost");
		var troop = scn.GetShadowCard("troop");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToSupportArea(cost);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SUPPORT, cost);
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsPass();
		// Only a [moria] Orc: not offered.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cost));

		// An [orc] minion who is no Orc is enough.
		scn.MoveMinionsToTable(halftroll);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(cost));
		int deck = scn.GetFreepsDeckCount();
		scn.ShadowUseCardAction(cost);

		// Paid, and nothing to place.
		assertInDiscard(cost);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
