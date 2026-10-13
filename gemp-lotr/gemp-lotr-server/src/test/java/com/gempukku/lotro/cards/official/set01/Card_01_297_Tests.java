package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
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
					put("card", "1_297");
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

		var card = scn.GetFreepsCard("card");

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
}
