package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_297_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guest", "1_297");
					put("guest2", "1_297");
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: Hobbit ally, home 1F, vitality 3; a fellowship ability used only on purpose
					put("albert", "1_69");      // Albert Dreary, Entertainer from Bree: [gandalf] Man ally, home 1F, vitality 3; a maneuver ability only
					// Master Worrywort, Hobbit Farmer: Hobbit ally, home 1 of the Hobbit sets (1H), vitality 3; texts about Bilbo and a skirmish only
					put("worrywort", "33_53");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guest", "1_297");
					// Rosie Cotton, Hobbiton Lass and The Gaffer, Sam's Father: Hobbit allies whose home is site 1.
					// Their own texts are about Frodo and Sam.
					put("rosie", "1_309");
					put("gaffer", "1_291");
					put("bounder", "1_286");    // Bounder: a Hobbit ally whose home is site 2; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HobbitPartyGuestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Hobbit Party Guest
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 2
		 * Site Number: 1
		 * Game Text: At the start of each of your turns, heal this ally.<br><b>Fellowship:</b> Exert this ally to heal another Hobbit ally whose home is site 1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guest");

		assertEquals("Hobbit Party Guest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void ExertsToHealAnotherHobbitAllyWhoseHomeIsSite1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealScenario();

		var guest = scn.GetFreepsCard("guest");
		var rosie = scn.GetFreepsCard("rosie");
		var gaffer = scn.GetFreepsCard("gaffer");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(guest, rosie, gaffer, bounder);

		scn.StartGame();
		// Each of the three has 2 vitality: one wound is all any of them can carry.
		scn.AddWoundsToChar(rosie, 1);
		scn.AddWoundsToChar(gaffer, 1);
		scn.AddWoundsToChar(bounder, 1);

		assertEquals(0, scn.GetWoundsOn(guest));
		assertEquals(1, scn.GetWoundsOn(rosie));
		assertTrue(scn.FreepsActionAvailable(guest));

		scn.FreepsUseCardAction(guest);
		assertEquals(1, scn.GetWoundsOn(guest));
		// The two wounded Hobbit allies of site 1; not the one of site 2, and not the Guest, who is wounded now too.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rosie, gaffer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, guest));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(rosie);
		assertEquals(0, scn.GetWoundsOn(rosie));
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(guest));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
	@Test
	public void AtTheStartOfEachOfItsOwnersTurnsItHealsOneWoundFromItselfAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guest = scn.GetFreepsCard("guest");
		var guest2 = scn.GetFreepsCard("guest2");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveAlliesToTable(guest, guest2, maggot);
		// Vitality 2 holds one wound only; one more by hand lets "heal" be told from "heal every wound".
		scn.ApplyAdHocModifier(new VitalityModifier(null, guest, null, 1));
		scn.AddWoundsToChar(guest, 2);
		scn.AddWoundsToChar(guest2, 1);
		scn.AddWoundsToChar(maggot, 1);
		assertEquals(2, scn.GetWoundsOn(guest));
		assertEquals(1, scn.GetWoundsOn(guest2));
		assertEquals(1, scn.GetWoundsOn(maggot));

		// The owner's first turn starts: each Guest heals itself once, and not the other.
		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(guest));
		assertEquals(0, scn.GetWoundsOn(guest2));
		assertEquals(1, scn.GetWoundsOn(maggot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The opponent's turn starts: not one of "your turns".
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertEquals(1, scn.GetWoundsOn(guest));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// The owner's second turn starts: the Guest is healed again.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(0, scn.GetWoundsOn(guest));
		assertEquals(0, scn.GetWoundsOn(guest2));
		assertEquals(1, scn.GetWoundsOn(maggot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealsOneWoundFromAHobbitAllyOfSite1AndNotFromAManAllyAHobbitCompanionOrAHobbitAllyOfAnotherBlocksSite1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guest = scn.GetFreepsCard("guest");
		var guest2 = scn.GetFreepsCard("guest2");
		var maggot = scn.GetFreepsCard("maggot");
		var albert = scn.GetFreepsCard("albert");
		var worrywort = scn.GetFreepsCard("worrywort");
		scn.MoveAlliesToTable(guest, guest2, maggot, albert, worrywort);

		scn.StartGame();
		scn.AddWoundsToChar(maggot, 2);
		scn.AddWoundsToChar(guest2, 1);
		scn.AddWoundsToChar(albert, 1);
		scn.AddWoundsToChar(worrywort, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(0, scn.GetWoundsOn(guest));
		assertEquals(2, scn.GetWoundsOn(maggot));
		assertEquals(1, scn.GetWoundsOn(guest2));
		assertEquals(1, scn.GetWoundsOn(albert));
		assertEquals(1, scn.GetWoundsOn(worrywort));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsUseCardAction(guest);
		assertEquals(1, scn.GetWoundsOn(guest));
		// A unique Hobbit ally of site 1 and another Guest; not a Man ally of site 1, a Hobbit ally of the
		// Hobbit sets' site 1, the Ring-bearer (a Hobbit companion), or this Guest.
		assertTrue(scn.FreepsHasCardChoiceAvailable(maggot, guest2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(albert, worrywort, frodo, guest));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(maggot);
		assertEquals(1, scn.GetWoundsOn(maggot));
		assertEquals(1, scn.GetWoundsOn(guest2));
		assertEquals(1, scn.GetWoundsOn(albert));
		assertEquals(1, scn.GetWoundsOn(worrywort));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheActionIsNotOfferedWhileThisAllyCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guest = scn.GetFreepsCard("guest");
		var guest2 = scn.GetFreepsCard("guest2");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveAlliesToTable(guest, guest2, maggot);

		scn.StartGame();
		// One wound on vitality 2: exhausted.
		scn.AddWoundsToChar(guest, 1);
		scn.AddWoundsToChar(maggot, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(guest));
		assertEquals(0, scn.GetWoundsOn(guest2));
		assertEquals(1, scn.GetWoundsOn(maggot));

		// Maggot is there to be healed; the other Guest, who can exert, is offered the same action.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(guest));
		assertTrue(scn.FreepsActionAvailable(guest2));
	}
}
