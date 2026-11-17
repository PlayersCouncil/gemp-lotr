package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_233_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");         // Sam, Son of Hamfast: a Hobbit companion; abilities used only on purpose
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					put("boromir", "1_97");      // Boromir, Son of Denethor: a [gondor] Man; a skirmish ability used only on purpose
					put("sword", "1_299");       // Hobbit Sword: [shire] possession, hand weapon, cost 1, on a Hobbit; no text
					put("sword2", "1_299");
					// Narsil, Blade of the Faithful: [gondor] ARTIFACT, hand weapon, cost 2, on a [gondor] Man; its bonus
					// is for Elendil or Isildur only, and its response is never answered here
					put("narsil", "9_34");
				}},
				new HashMap<>()
				{{
					// An unordered Shadows-block path, with the Chamber as the site the game starts on: its text is in
					// force in the first fellowship phase.
					putAll(VirtualTableScenario.ShadowsSites);
					put("site1", "11_233");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChamberofMazarbulStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Chamber of Mazarbul
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Right
		 * Game Text: <b>Underground</b>. The Free Peoples player may transfer Free Peoples artifacts and possessions at no twilight cost.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site1");

		assertEquals("Chamber of Mazarbul", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void APossessionAndAnArtifactAreTransferredForNothingButAPossessionPlayedStillCostsItsTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(sam, aragorn, boromir);
		scn.AttachCardsTo(frodo, sword);
		scn.AttachCardsTo(aragorn, narsil);
		scn.MoveCardsToHand(sword2);

		scn.StartGame(scn.GetFreepsSite("site1"));

		assertEquals("Chamber of Mazarbul", scn.GetCurrentSite().getBlueprint().getTitle());
		assertAttachedTo(sword, frodo);
		assertAttachedTo(narsil, aragorn);
		assertInZone(Zone.HAND, sword2);
		assertEquals(0, scn.GetTwilight());

		// A [shire] possession of cost 1, to the only other Hobbit: nothing is added.
		assertTrue(scn.FreepsTransferAvailable(sword));
		scn.FreepsTransferCard(sword);
		assertAttachedTo(sword, sam);
		assertEquals(0, scn.GetTwilight());

		// A [gondor] artifact of cost 2, to the only other [gondor] Man: nothing is added.
		assertTrue(scn.FreepsTransferAvailable(narsil));
		scn.FreepsTransferCard(narsil);
		assertAttachedTo(narsil, boromir);
		assertEquals(0, scn.GetTwilight());

		// A play is no transfer: the second Hobbit Sword, played on Frodo (Sam already has a hand weapon), costs its 1.
		scn.FreepsPlayCard(sword2);
		assertAttachedTo(sword2, frodo);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheChamberATransferCostsItsTwilightAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, sword);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertAttachedTo(sword, frodo);

		// The fellowship moves on to Sirannon Ruins (a River and no other text), and the first player's next turn
		// starts there, with the Chamber behind it at site 1.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals("Sirannon Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertAttachedTo(sword, frodo);
		int before = scn.GetTwilight();

		scn.FreepsTransferCard(sword);
		assertAttachedTo(sword, sam);
		// The Hobbit Sword's own 1.
		assertEquals(before + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
