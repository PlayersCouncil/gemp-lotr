package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_185_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Five companions beside Frodo, for "spot 6 companions"; Legolas is the sixth where a test lays him.
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("boromir", "1_97");         // Boromir, Son of Denethor: a skirmish ability used only on purpose
					put("sam", "1_311");            // Sam, Son of Hamfast: a fellowship ability used only on purpose
					put("gimli", "1_13");           // Gimli, Son of Gloin: a skirmish ability used only on purpose
					put("legolas", "1_50");         // Legolas, Greenleaf: an archery ability used only on purpose
					put("maggot", "1_288");         // Farmer Maggot, Chaser of Rascals: an ally, who is no companion

					put("nelya", "2_84");           // Úlairë Nelya: [wraith] Nazgûl, cost 5, site 2; fierce only at 2 burdens
					put("khamul", "103_61");        // Úlairë Attëa, Khamûl of the East: [raider] Nazgûl, cost 6, site 3; his text needs 3 burdens
					put("hound", "7_192");          // Morgul Hound: [wraith] Orc and no Nazgûl, cost 2; stronger at 2 wounds on the Ring-bearer
					put("beckoning", "12_83");      // The Beckoning Shadow: [orc] condition; nothing below 5 burdens
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");         // Fangorn Glade: its text answers a move to it; the game starts there
					put("angle", "12_185");
					// Never laid in these tests.
					put("site3", "11_234");
					put("site5", "18_138");
					put("site6", "12_187");
					put("site7", "13_185");
					put("site8", "11_230");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void TheAngleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: The Angle
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Forest</b>. <b>Shadow:</b> Play a Nazgûl (or spot 6 companions and discard 2 cards from hand) to draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("angle");

		assertEquals("The Angle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void ShadowAbilityPlaysANazgulFromHandToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Five companions and an ally: one short of the six the other way needs.
		scn.MoveCompanionsToTable("aragorn", "boromir", "sam", "gimli");
		scn.MoveAlliesToTable("maggot");

		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(nelya, khamul, hound);

		scn.StartGame(scn.GetFreepsSite("site1"));
		// Enough for any of the three minions, roaming included: Khamûl 6 + 2, the Hound 2 + 2.
		scn.SetTwilight(15);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("angle"));

		var angle = scn.GetCurrentSite();
		assertEquals("The Angle", angle.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowActionAvailable(angle, "Play a Nazgul"));
		assertFalse(scn.ShadowActionAvailable(angle, "Discard 2 cards"));

		scn.ShadowUseCardAction(angle, "Play a Nazgul");
		// A Nazgûl of each culture is offered; a [wraith] minion who is no Nazgûl is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);

		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertInHand(khamul, hound);
		// Nelya gone from the hand and one card drawn.
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSixCompanionsTheShadowPlayerMayDiscardTwoCardsFromHandInsteadToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "boromir", "sam", "gimli", "legolas");

		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var beckoning = scn.GetShadowCard("beckoning");
		// The Shadow player's own copy of a Free Peoples card, in his hand.
		var maggot = scn.GetShadowCard("maggot");
		scn.MoveCardsToHand(nelya, hound, beckoning, maggot);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("angle"));

		var angle = scn.GetCurrentSite();
		assertEquals("The Angle", angle.getBlueprint().getTitle());
		// Nelya costs 5 at site 2, so either way is open, and the Shadow player chooses.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(5, scn.GetTwilight());
		int deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowActionAvailable(angle, "Play a Nazgul"));
		assertTrue(scn.ShadowActionAvailable(angle, "Discard 2 cards"));

		scn.ShadowUseCardAction(angle, "Discard 2 cards");
		// Any card in hand: a Nazgûl, a minion, a condition, a Free Peoples card; exactly two.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, hound, beckoning, maggot));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(hound, maggot);

		assertInDiscard(hound, maggot);
		assertInHand(nelya, beckoning);
		assertEquals(2, scn.GetShadowDiscardCount());
		// Two discarded and one drawn; no Nazgûl was played.
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithThePoolShortOfTheNazgulAndFiveCompanionsUntilASixthCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		// Five companions and an ally, who nearly counts.
		scn.MoveCompanionsToTable("aragorn", "boromir", "sam", "gimli");
		scn.MoveAlliesToTable("maggot");

		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(nelya, hound, beckoning);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("angle"));

		var angle = scn.GetCurrentSite();
		assertEquals("The Angle", angle.getBlueprint().getTitle());

		// Nelya costs 5, and her site number is 2: no roaming at site 2.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(angle, "Play a Nazgul"));
		assertFalse(scn.ShadowActionAvailable(angle, "Discard 2 cards"));

		scn.MoveCompanionsToTable(legolas);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(angle, "Play a Nazgul"));
		assertTrue(scn.ShadowActionAvailable(angle, "Discard 2 cards"));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(angle, "Play a Nazgul"));
	}

	@Test
	public void WithSixCompanionsAndOneCardInHandTheDiscardIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "boromir", "sam", "gimli", "legolas");

		var hound = scn.GetShadowCard("hound");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(hound);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("angle"));

		var angle = scn.GetCurrentSite();
		assertEquals("The Angle", angle.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(angle, "Discard 2 cards"));

		scn.MoveCardsToHand(beckoning);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(angle, "Discard 2 cards"));
	}
}
