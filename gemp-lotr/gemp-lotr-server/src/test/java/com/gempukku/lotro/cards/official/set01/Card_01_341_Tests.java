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

public class Card_01_341_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Rivendell Valley is site 3 of the Fellowship sites below; the tests use the copy on the path.
					put("legolas", "1_50");     // Legolas, Greenleaf: unique Elf companion, cost 2; an archery ability
					put("legolas2", "1_50");
					put("orophin", "1_56");     // Orophin, Lorien Bowman: unique Elf ally, home 6F; an archery ability
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion who is no Elf; a skirmish ability
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the card laid on top of the draw deck
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RivendellValleyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Rivendell Valley
		 * Unique: False
		 * Shadow Number: 0
		 * Type: Site
		 * Block: Fellowship
		 * Direction: Right
		 * Site Number: 3
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Play an Elf to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Rivendell Valley", card.getBlueprint().getTitle());
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
	public void FellowshipActionPlaysAnElfCompanionOrAllyFromHandToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(3);
		var valley = scn.GetCurrentSite();
		assertEquals("Rivendell Valley", valley.getBlueprint().getTitle());
		scn.SetFreepsHand(legolas, orophin, gimli);
		scn.MoveCardsToTopOfDeck(aragorn);
		scn.FreepsRefreshDecision();
		int deck = scn.GetFreepsDeckCount();

		assertTrue(scn.FreepsActionAvailable(valley));
		scn.FreepsUseCardAction(valley);

		// An Elf companion and an Elf ally; not a companion who is no Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);

		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInHand(orophin, gimli, aragorn);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoElfInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var legolas2 = scn.GetFreepsCard("legolas2");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(3);
		var valley = scn.GetCurrentSite();
		// An Elf who cannot be played (Legolas is unique and in play) and a companion who is no Elf.
		scn.SetFreepsHand(legolas2, gimli);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertFalse(scn.FreepsActionAvailable(valley));
	}

	@Test
	public void IsNotOfferedInTheManeuverPhaseAtRivendellValley() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(legolas);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));

		// The fellowship moves on to site 3, where the maneuver phase is played.
		scn.SkipToPhase(Phase.MANEUVER);
		var valley = scn.GetCurrentSite();
		assertEquals("Rivendell Valley", valley.getBlueprint().getTitle());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(valley));
		assertInHand(legolas);
	}
}
