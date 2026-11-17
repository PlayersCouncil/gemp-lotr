package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_115_Tests
{

	// The Towers path with Rocks of Emyn Muil as its site 1.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, Frodo signet; an Archery ability only
					put("legolas2", "1_50");    // a second copy, which cannot be played while the first is in play
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, Frodo signet; a Skirmish ability only
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit companion, Frodo signet; a bar on discarding tales only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion with the Gandalf signet; a Maneuver ability only
				}},
				new HashMap<>()
				{{
					put("site1", "6_115");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RocksofEmynMuilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Rocks of Emyn Muil
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 1T
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Play a companion with the Frodo signet to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Rocks of Emyn Muil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionPlaysACompanionWithTheFrodoSignetFromHandToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rocks = scn.GetFreepsSite(1);
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(legolas, boromir, pippin, aragorn);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertInHand(legolas, boromir, pippin, aragorn);
		assertTrue(scn.FreepsPlayAvailable(aragorn));
		assertTrue(scn.FreepsActionAvailable(rocks));

		scn.FreepsUseCardAction(rocks);
		// Frodo-signet companions of three cultures and races; not Aragorn, a companion with the Gandalf signet
		// whom the player could play.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, boromir, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);

		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertInHand(legolas, pippin, aragorn);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheOnlyFrodoSignetCompanionInHandCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rocks = scn.GetFreepsSite(1);
		var legolas = scn.GetFreepsCard("legolas");
		var legolas2 = scn.GetFreepsCard("legolas2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(legolas2, aragorn);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInHand(legolas2, aragorn);

		// Legolas is unique and in play, so the second Legolas cannot be played; Aragorn can, and has the Gandalf signet.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(legolas2));
		assertTrue(scn.FreepsPlayAvailable(aragorn));
		assertFalse(scn.FreepsActionAvailable(rocks));
		assertEquals(1, scn.GetBurdens());
	}
}
