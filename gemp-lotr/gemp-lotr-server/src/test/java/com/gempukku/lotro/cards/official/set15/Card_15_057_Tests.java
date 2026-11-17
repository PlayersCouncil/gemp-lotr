package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("damrod", "15_57");
					// Arwen, Daughter of Elrond: a ranger of another culture and race ([elven] Elf), vitality 3; +3 only against a Nazgûl
					put("arwen", "1_30");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion who is no ranger, vitality 3; a skirmish ability only
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] Man ally who is no ranger, vitality 1; a maneuver ability used only on purpose
					// Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so that the Merchant lives with a wound; its
					// response is used only on purpose
					put("narsil", "9_34");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DamrodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Damrod, Dúnadan of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br>At the start of each fellowship phase, you may add (1) to heal a ranger.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("damrod");

		assertEquals("Damrod", card.getBlueprint().getTitle());
		assertEquals("Dúnadan of Gondor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseHeMayAddOneToHealARanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var damrod = scn.GetFreepsCard("damrod");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(damrod, arwen, boromir);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.AddWoundsToChar(damrod, 1);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(merchant, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(damrod));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.StartGame();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(damrod));
		scn.FreepsAcceptOptionalTrigger();

		// The cost: (1) added.
		assertEquals(1, scn.GetTwilight());

		// Rangers of two cultures and races, Damrod himself among them; not a companion or an ally who is no
		// ranger, nor Frodo.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(damrod, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, merchant, frodo));
		scn.FreepsChooseCard(arwen);

		// One wound of two healed.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(damrod));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRangerWoundedItIsOfferedAndAddsOneForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var damrod = scn.GetFreepsCard("damrod");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(damrod, arwen, boromir);
		// Only companions who are no rangers are wounded.
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetTwilight());
		// The trigger has a cost, so it is offered though the heal can reach nobody (Q-0034).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(damrod));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedNothingChangesAndTheStartOfTheManeuverPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var damrod = scn.GetFreepsCard("damrod");
		var arwen = scn.GetFreepsCard("arwen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(damrod, arwen);
		scn.AddWoundsToChar(arwen, 1);
		assertEquals(1, scn.GetWoundsOn(arwen));

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(damrod));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();
		scn.ShadowPass();

		// The start of the next phase he is asked in, with Arwen still wounded.
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsOfferedAgainAtTheStartOfItsOwnersNextFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var damrod = scn.GetFreepsCard("damrod");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(damrod, arwen);
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(damrod));
		scn.FreepsAcceptOptionalTrigger();
		// The one wounded ranger is healed unasked.
		assertEquals(1, scn.GetWoundsOn(arwen));

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetWoundsOn(arwen));
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(damrod));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
