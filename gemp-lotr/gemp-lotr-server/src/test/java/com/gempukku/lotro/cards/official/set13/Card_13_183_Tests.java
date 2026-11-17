package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_183_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "13_183");

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a ranger to bear the condition and the sword
					put("nostranger", "1_108");     // No Stranger to the Shadows: [gondor] condition on a ranger; each site's Shadow number -1
					put("sword", "1_112");          // Ranger's Sword: [gondor] possession on Aragorn, damage +1; no condition
					put("lord", "1_21");            // Lord of Moria: [dwarven] condition in the support area; each Dwarf damage +1
					put("weary", "4_212");          // Weary: [isengard] condition on a companion; it answers an assignment to an [isengard] tracker
					put("beckoning", "12_83");      // The Beckoning Shadow: [orc] condition in the support area; nothing below 5 burdens

					put("nelya", "2_84");           // Úlairë Nelya: [wraith] Nazgûl; fierce only at 2 burdens
					put("khamul", "103_61");        // Úlairë Attëa, Khamûl of the East: the one Nazgûl who is not [wraith] ([raider])
					put("hound", "7_192");          // Morgul Hound: a [wraith] Orc and no Nazgûl
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Úlairë Lemenya, Servant of the Shadow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br><b>Shadow:</b> Exert Úlairë Lemenya and discard a Nazgûl from your hand to discard a condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("lemenya");

		assertEquals("Úlairë Lemenya", card.getBlueprint().getTitle());
		assertEquals("Servant of the Shadow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsHerAndDiscardsANazgulFromHandToDiscardAConditionOfEitherSideUntilSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var nostranger = scn.GetFreepsCard("nostranger");
		var sword = scn.GetFreepsCard("sword");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, nostranger, sword);
		scn.MoveCardsToSupportArea(lord);

		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var beckoning = scn.GetShadowCard("beckoning");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCardsToSupportArea(beckoning);
		scn.AttachCardsTo(frodo, weary);
		scn.MoveCardsToHand(nelya, khamul, hound);

		scn.StartGame();
		assertInHand(nelya, khamul, hound);
		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.ATTACHED, nostranger, sword, weary);
		assertInZone(Zone.SUPPORT, lord, beckoning);
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowActionAvailable(lemenya));

		scn.ShadowUseCardAction(lemenya);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		// A Nazgûl of each culture is offered; a [wraith] minion who is no Nazgûl is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);

		assertInDiscard(nelya);
		assertInHand(khamul, hound);
		assertEquals(2, scn.GetShadowHandCount());

		// Conditions of either side, in a support area or borne, are offered; a possession is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nostranger, lord, beckoning, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(weary);

		assertInDiscard(weary);
		assertInZone(Zone.ATTACHED, nostranger, sword);
		assertInZone(Zone.SUPPORT, lord, beckoning);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Vitality 2 with 1 wound: exhausted, so she cannot exert again, with a Nazgûl in hand and conditions in play.
		assertFalse(scn.ShadowActionAvailable(lemenya));
	}

	@Test
	public void WithNoNazgulInHandTheAbilityIsNotOfferedUntilOneIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToSupportArea(lord);

		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(hound);

		scn.StartGame();
		// The only card in hand is a [wraith] minion who is no Nazgûl.
		assertInHand(hound);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.SUPPORT, lord);
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertFalse(scn.ShadowActionAvailable(lemenya));

		scn.MoveCardsToHand(nelya);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(lemenya));
	}

	@Test
	public void WithNoConditionInPlayTheAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(nelya);

		scn.StartGame();
		assertInHand(nelya);
		assertInZone(Zone.ATTACHED, sword);
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowActionAvailable(lemenya));

		scn.ShadowUseCardAction(lemenya);

		// The costs are paid; with no condition in play nothing else leaves play.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInDiscard(nelya);
		assertInZone(Zone.ATTACHED, sword);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
