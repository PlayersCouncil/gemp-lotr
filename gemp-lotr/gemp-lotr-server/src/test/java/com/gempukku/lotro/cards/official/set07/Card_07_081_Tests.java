package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "7_81");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, vitality 3; a skirmish ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion, vitality 4; to play, spot a [gondor] companion (cheated in); a skirmish ability only
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally, vitality 1; a maneuver ability used only on purpose
					// Narsil, Blade of the Faithful: bearer must be a [gondor] Man; vitality +1, so the Merchant lives with a wound.
					// Its response is used only on purpose.
					put("narsil", "9_34");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: [shire] companion, vitality 4
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Aragorn, Captain of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Knight</b>.<br>At the start of each fellowship phase, you may add (2) to heal another [gondor] companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Captain of Gondor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseHeMayAddTwoToHealAnotherGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, boromir, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(merchant, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.StartGame();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		// The cost: (2) added.
		assertEquals(2, scn.GetTwilight());

		// [gondor] companions of two races; not Aragorn himself, not the [gondor] ally, not a companion of another culture.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merchant, frodo));
		scn.FreepsChooseCard(boromir);

		// One wound of two healed.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoOtherGondorCompanionWoundedItIsOfferedAndAddsTwoForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, boromir);
		// Only Aragorn himself and a companion of another culture are wounded.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetTwilight());
		// The trigger has a cost, so it is offered though the heal can reach nobody (Q-0034).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedNothingChangesAndTheStartOfTheManeuverPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AddWoundsToChar(boromir, 1);
		assertEquals(1, scn.GetWoundsOn(boromir));

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();
		scn.ShadowPass();

		// The start of the next phase he is asked in, with Boromir still wounded.
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsOfferedAgainAtTheStartOfItsOwnersNextFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(boromir));

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetWoundsOn(boromir));
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
