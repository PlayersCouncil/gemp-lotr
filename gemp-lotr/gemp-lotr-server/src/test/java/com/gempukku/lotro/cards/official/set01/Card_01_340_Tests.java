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

public class Card_01_340_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "1_96");     // Boromir, Lord of Gondor: unique [gondor] Man companion, cost 3
					put("boromir2", "1_96");
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] Man ally, home 1F, cost 0; a fellowship ability
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is no Man; to play, spot a [gondor] companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] companion Pippin's play spots
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion who is no Man; a skirmish ability
					put("merry", "1_303");      // Merry, From O'er the Brandywine: the card laid on top of the draw deck
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_340");     // Rivendell Terrace in place of Rivendell Valley
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
	public void RivendellTerraceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Rivendell Terrace
		 * Unique: False
		 * Shadow Number: 0
		 * Type: Site
		 * Block: Fellowship
		 * Direction: Right
		 * Site Number: 3
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Play a Man to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Rivendell Terrace", card.getBlueprint().getTitle());
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
	public void FellowshipActionPlaysAManCompanionOrAllyOfAnyCultureFromHandToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var barliman = scn.GetFreepsCard("barliman");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		var terrace = scn.GetCurrentSite();
		assertEquals("Rivendell Terrace", terrace.getBlueprint().getTitle());
		scn.SetFreepsHand(boromir, barliman, pippin, gimli);
		scn.MoveCardsToTopOfDeck(merry);
		scn.FreepsRefreshDecision();
		int deck = scn.GetFreepsDeckCount();
		assertInZone(Zone.DECK, merry);

		// Pippin and Gimli could each be played on their own.
		assertTrue(scn.FreepsPlayAvailable(pippin));
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertTrue(scn.FreepsActionAvailable(terrace));
		scn.FreepsUseCardAction(terrace);

		// A [gondor] Man companion and a [gandalf] Man ally; not a [gondor] companion who is no Man, nor a
		// companion of another culture who is no Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, barliman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(barliman);

		assertInZone(Zone.SUPPORT, barliman);
		assertInHand(boromir, pippin, gimli, merry);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoManInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var boromir2 = scn.GetFreepsCard("boromir2");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.SkipToSite(3);
		var terrace = scn.GetCurrentSite();
		assertEquals("Rivendell Terrace", terrace.getBlueprint().getTitle());
		// A Man who cannot be played (Boromir is unique and in play) and a companion who is no Man.
		scn.SetFreepsHand(boromir2, gimli);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertFalse(scn.FreepsActionAvailable(terrace));
	}

	@Test
	public void IsNotOfferedInTheManeuverPhaseAtRivendellTerrace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(boromir);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));

		// The fellowship moves on to site 3, where the maneuver phase is played.
		scn.SkipToPhase(Phase.MANEUVER);
		var terrace = scn.GetCurrentSite();
		assertEquals("Rivendell Terrace", terrace.getBlueprint().getTitle());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(terrace));
		assertInHand(boromir);
	}
}
