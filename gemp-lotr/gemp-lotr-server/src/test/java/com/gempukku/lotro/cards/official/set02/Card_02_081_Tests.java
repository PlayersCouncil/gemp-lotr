package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("findring", "2_81");

					// Three Nazgûl of three titles and two cultures.
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: [wraith], vitality 3; fierce and damage +1 at 2 burdens, which nothing here reads
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, vitality 3; his bonuses go to Easterlings, which nothing here reads
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith], vitality 2; weakens only characters bearing a [wraith] condition
					// Morgul Hound: a [wraith] minion who is no Nazgûl (an Orc), vitality 2, so he could exert.
					put("hound", "7_192");

					// Companions of four cultures and races beside the Ring-bearer.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, vitality 3, exhausted where he is laid
					// Albert Dreary, Entertainer from Bree: an ally (a character who is no companion), vitality 3.
					put("albert", "1_69");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheyWillFindtheRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: They Will Find the Ring
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert 2 Nazgûl and spot X burdens to exert X companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("findring");

		assertEquals("They Will Find the Ring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTwoNazgulOfThePlayersChoiceAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var card = scn.GetShadowCard("findring");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(card);
		assertInHand(card);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(nelya, khamul, lemenya, hound);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.ShadowPlayAvailable(card));
		scn.ShadowPlayCard(card);

		// Any Nazgûl, of either culture; the [wraith] Orc is no Nazgûl.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul, lemenya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(nelya, khamul);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));

		// One burden: one companion, the Shadow player's choice.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(card);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithThreeBurdensExertsThreeCompanionsAndNoExhaustedCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, boromir);
		scn.MoveAlliesToTable(albert);

		var card = scn.GetShadowCard("findring");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(card);
		assertInHand(card);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(nelya, lemenya);
		// Vitality 3 with 2 wounds: Boromir cannot exert.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.ShadowPlayCard(card);
		// The only two Nazgûl exert with no question.
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetWoundsOn(lemenya));

		assertEquals(3, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, legolas, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, albert, nelya));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(albert));
		scn.ShadowChooseCards(frodo, legolas, gimli);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ThePlayerMaySpotFewerBurdensThanThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);

		var card = scn.GetShadowCard("findring");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(card);
		assertInHand(card);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(nelya, lemenya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetBurdens());
		scn.ShadowPlayCard(card);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetWoundsOn(lemenya));

		// "Spot X burdens": he need not spot every one (CR 4.2 "spot"), so X runs up to all three; "exert X
		// companions" is an exertion of at least one (Q-0076), so from one.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCards(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyOneNazgulAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var card = scn.GetShadowCard("findring");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(card);
		assertInHand(card);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(nelya, lemenya, hound);
		// Vitality 2 with 1 wound: Lemenya cannot exert.  Nelya could exert twice, and the Hound, who is no
		// Nazgûl, once.
		scn.AddWoundsToChar(lemenya, 1);
		assertEquals(1, scn.GetWoundsOn(lemenya));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(card));
		assertInHand(card);
	}
}
