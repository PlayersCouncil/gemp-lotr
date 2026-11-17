package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_339_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_303");      // Merry, From O'er the Brandywine: unique [shire] Hobbit companion, cost 1
					put("merry2", "1_303");
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, home 2F, not unique; a skirmish ability
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; to play, spot a [gondor] companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] companion Pippin's play spots
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion who is no Hobbit; a skirmish ability
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; to play, add a burden
					put("boromir", "1_96");     // Boromir, Lord of Gondor: the card laid on top of the draw deck
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_339");     // Frodo's Bedroom in place of Rivendell Valley
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodosBedroomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Frodo's Bedroom
		 * Unique: False
		 * Shadow Number: 0
		 * Type: Site
		 * Block: Fellowship
		 * Direction: Right
		 * Site Number: 3
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Play a Hobbit to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Frodo's Bedroom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionPlaysAHobbitCompanionOrAllyOfAnyCultureFromHandToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var smeagol = scn.GetFreepsCard("smeagol");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		var bedroom = scn.GetCurrentSite();
		assertEquals("Frodo's Bedroom", bedroom.getBlueprint().getTitle());
		scn.SetFreepsHand(merry, bounder, pippin, gimli, smeagol);
		scn.MoveCardsToTopOfDeck(boromir);
		scn.FreepsRefreshDecision();
		int deck = scn.GetFreepsDeckCount();
		assertInZone(Zone.DECK, boromir);

		// Gimli and Smeagol could each be played on their own.
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertTrue(scn.FreepsPlayAvailable(smeagol));
		assertTrue(scn.FreepsActionAvailable(bedroom));
		scn.FreepsUseCardAction(bedroom);

		// A [shire] Hobbit companion, a Hobbit ally and a [gondor] Hobbit; not a companion who is no Hobbit,
		// nor a [shire] companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, bounder, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertInHand(merry, bounder, gimli, smeagol, boromir);
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoHobbitInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var merry2 = scn.GetFreepsCard("merry2");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(3);
		var bedroom = scn.GetCurrentSite();
		assertEquals("Frodo's Bedroom", bedroom.getBlueprint().getTitle());
		// A Hobbit who cannot be played (Merry is unique and in play) and a companion who is no Hobbit.
		scn.SetFreepsHand(merry2, gimli);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertFalse(scn.FreepsActionAvailable(bedroom));
	}

	@Test
	public void IsNotOfferedInTheManeuverPhaseAtFrodosBedroom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(merry);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));

		// The fellowship moves on to site 3, where the maneuver phase is played.
		scn.SkipToPhase(Phase.MANEUVER);
		var bedroom = scn.GetCurrentSite();
		assertEquals("Frodo's Bedroom", bedroom.getBlueprint().getTitle());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bedroom));
		assertInHand(merry);
	}
}
