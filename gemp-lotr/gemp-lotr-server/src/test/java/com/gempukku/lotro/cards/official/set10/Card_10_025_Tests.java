package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "10_25");
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] companion, vitality 3; an archery ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] companion, vitality 3; +3 against a Nazgûl only
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] companion, vitality 3; a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion, cost 3, vitality 3; a skirmish ability only
					put("orophin", "1_56");     // Orophin, Lórien Bowman: [elven] ally, vitality 3; an archery ability only
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
		 * Set: 10
		 * Name: Aragorn, Elessar Telcontar
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: When you play Aragorn, you may heal another companion.<br>At the start of each fellowship phase, you may exert a companion of one culture to heal a companion of another culture.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Elessar Telcontar", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void WhenPlayedHeMayHealAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(legolas, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(orophin, 1);

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, gimli));
		// A wounded ally is no companion.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin));
		scn.FreepsChooseCard(legolas);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedItHealsNobodyAndAnotherCompanionsPlayIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(aragorn, boromir);

		scn.StartGame();
		scn.AddWoundsToChar(legolas, 2);

		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another companion played while Aragorn is in play, with Legolas still wounded.
		scn.FreepsPlayCard(boromir);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenPlayedWithNoOtherCompanionWoundedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		// Only an ally is wounded.
		scn.AddWoundsToChar(orophin, 1);

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseExertingAGondorCompanionHealsACompanionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var orophin = scn.GetFreepsCard("orophin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, boromir);
		scn.MoveAlliesToTable(orophin);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(orophin, 1);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		// Who exerts: a companion of any culture, and no ally.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, legolas, gimli, boromir, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Who is healed: a companion of a culture other than [gondor].
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, orophin));
		scn.FreepsChooseCard(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingAnElvenCompanionHealsACompanionWhoIsNotElven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, legolas, arwen, boromir);
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));

		// The [gondor] Boromir may be healed now; Arwen, [elven] as Legolas is, may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen));
		scn.FreepsChooseCard(boromir);

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedAtTheStartOfTheFellowshipPhaseNothingChangesAndTheStartOfTheManeuverPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhileEveryCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		// An ally who could exert, and does not count.
		scn.MoveAlliesToTable(orophin);
		// Vitality 4 each: 3 wounds exhaust both companions, who are of two cultures.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();

		assertTrue(scn.IsExhausted(aragorn));
		assertTrue(scn.IsExhausted(frodo));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsOfferedAgainAtTheStartOfItsOwnersNextFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
