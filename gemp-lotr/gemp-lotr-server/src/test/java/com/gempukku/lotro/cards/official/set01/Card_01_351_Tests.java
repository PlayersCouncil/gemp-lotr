package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_351_Tests
{

	// The Glade is site 6 of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion, vitality 3; an archery ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lórien Bowman: an Elf ALLY, vitality 3; an archery ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: a companion who is no Elf, vitality 3; a skirmish ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("runner", "1_178");     // Goblin Runner: a minion, here only a card in a hand
					put("sam", "1_311");        // Sam, Son of Hamfast: here only a card in a hand
					put("hobbitsword", "1_299");// Hobbit Sword: here only a card in a hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GaladrielsGladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Galadriel's Glade
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Exert an Elf to look at an opponent's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Galadriel's Glade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void FellowshipActionExertsAnElfToShowTheFreePeoplesPlayerTheShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		var shadowTroop = scn.GetShadowCard("troop");
		var shadowRunner = scn.GetShadowCard("runner");
		var shadowGimli = scn.GetShadowCard("gimli");
		var freepsSam = scn.GetFreepsCard("sam");
		var freepsSword = scn.GetFreepsCard("hobbitsword");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetShadowHand(shadowTroop, shadowRunner, shadowGimli);
		scn.SetFreepsHand(freepsSam, freepsSword);
		scn.FreepsRefreshDecision();

		var glade = scn.GetCurrentSite();
		assertEquals("Galadriel's Glade", glade.getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());

		assertTrue(scn.FreepsActionAvailable(glade));
		scn.FreepsUseCardAction(glade);

		// An Elf companion and an Elf ally may pay; a companion who is no Elf may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(orophin);

		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		// The three cards of the Shadow hand are shown to the Free Peoples player, none to be chosen; his own hand of
		// two is not among them, and the Shadow player is shown nothing.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowTroop, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowRunner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowGimli, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(freepsSam, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, shadowTroop, shadowRunner, shadowGimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheShadowHandEmptyTheElfStillExertsAndNothingIsShown() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetShadowHand();
		scn.FreepsRefreshDecision();

		var glade = scn.GetCurrentSite();
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(legolas));

		// The action has a cost, so it is offered with nothing to look at.
		assertTrue(scn.FreepsActionAvailable(glade));
		scn.FreepsUseCardAction(glade);
		scn.FreepsChooseCard(legolas);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheOnlyElfExhaustedTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var shadowTroop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas, gimli);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetShadowHand(shadowTroop);
		// Vitality 3 with 2 wounds: Legolas cannot exert; Gimli, who could, is no Elf.
		scn.AddWoundsToChar(legolas, 2);
		scn.FreepsRefreshDecision();

		var glade = scn.GetCurrentSite();
		assertEquals("Galadriel's Glade", glade.getBlueprint().getTitle());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetShadowHandCount());

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(glade));
	}

	@Test
	public void IsNotOfferedInTheManeuverPhaseAtTheGlade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		var shadowRunner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetShadowHand(shadowRunner);
		scn.FreepsPass();

		var glade = scn.GetCurrentSite();
		assertEquals("Galadriel's Glade", glade.getBlueprint().getTitle());
		// A minion on the table, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(glade));
	}
}
