package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("squeak", "5_101");
					put("chieftain1", "1_266");   // Orc Chieftain: [sauron] Orc, vitality 2; his text answers only a weapon played on him
					put("chieftain2", "1_266");
					put("trooper", "3_99");       // Orc Trooper: [sauron] Orc, vitality 3; strength +1 for each card in the Shadow hand
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai, no Orc; damage +1 and nothing else
					put("hound", "7_192");        // Morgul Hound: [wraith] Orc; +4 and fierce only at 2 wounds on the Ring-bearer
					put("sam", "1_311");          // Sam, Son of Hamfast: [shire] Hobbit companion; "Fellowship: Exert Sam to remove a burden"
					put("pippin", "103_31");      // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("bounder", "1_286");      // Bounder: [shire] Hobbit ally; a skirmish ability used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man companion, no Hobbit
					// Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, vitality 4; abilities used only on purpose
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IdMakeYouSqueakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: I'd Make You Squeak
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert 2 [sauron] Orcs. Plays to your support area.
		 * The Shadow number of each site is +1 for each wound on a Hobbit (limit +3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("squeak");

		assertEquals("I'd Make You Squeak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayExertsTwoSauronOrcsOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squeak = scn.GetShadowCard("squeak");
		var chieftain1 = scn.GetShadowCard("chieftain1");
		var chieftain2 = scn.GetShadowCard("chieftain2");
		var trooper = scn.GetShadowCard("trooper");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain1, chieftain2, trooper, guard, hound);
		scn.MoveCardsToHand(squeak);
		assertInZone(Zone.HAND, squeak);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(squeak));
		scn.ShadowPlayCard(squeak);

		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain1, chieftain2, trooper));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, hound));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(chieftain1, trooper);

		assertInZone(Zone.SUPPORT, squeak);
		assertEquals(1, scn.GetWoundsOn(chieftain1));
		assertEquals(1, scn.GetWoundsOn(trooper));
		assertEquals(0, scn.GetWoundsOn(chieftain2));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenOnlyOneSauronOrcCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squeak = scn.GetShadowCard("squeak");
		var chieftain1 = scn.GetShadowCard("chieftain1");
		var trooper = scn.GetShadowCard("trooper");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain1, trooper, guard, hound);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(chieftain1, 1);
		scn.MoveCardsToHand(squeak);
		assertEquals(1, scn.GetWoundsOn(chieftain1));
		assertInZone(Zone.HAND, squeak);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(chieftain1));
		// Trooper can exert, and so could the Uruk-hai and the [wraith] Orc: they are not [sauron] Orcs.
		assertFalse(scn.ShadowPlayAvailable(squeak));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachSiteIsOneMoreForEachWoundOnAHobbitAsTheyAreTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squeak = scn.GetShadowCard("squeak");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(squeak);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, squeak);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		// Bag End, Shadow number 0, +1 for Frodo's wound.
		assertEquals(1, scn.GetTwilightCost(scn.GetSite(1)));

		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetTwilightCost(scn.GetSite(1)));

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		// Ettenmoors, Shadow number 2, +2 as well: 2 companions + 2 + 2 added by the move.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilightCost(scn.GetSite(2)));
		assertEquals(2, scn.GetTwilightCost(scn.GetSite(1)));
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WoundsOnAHobbitOfAnyKindAreCountedAndWoundsOnOthersAreNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squeak = scn.GetShadowCard("squeak");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(squeak);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, squeak);
		scn.FreepsPass();
		scn.MoveMinionsToTable(ted);
		scn.AddWoundsToChar(aragorn, 2);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SUPPORT, squeak);
		assertInZone(Zone.SHADOW_CHARACTERS, ted);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// Two wounds on a Man, none on a Hobbit: the printed numbers.
		assertEquals(0, scn.GetTwilightCost(scn.GetSite(1)));
		assertEquals(2, scn.GetTwilightCost(scn.GetSite(2)));

		// A Hobbit ally.
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetTwilightCost(scn.GetSite(1)));
		assertEquals(3, scn.GetTwilightCost(scn.GetSite(2)));

		// A Hobbit minion, the Shadow player's own.
		scn.AddWoundsToChar(ted, 1);
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(2, scn.GetTwilightCost(scn.GetSite(1)));
		assertEquals(4, scn.GetTwilightCost(scn.GetSite(2)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoMoreThanThreeIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squeak = scn.GetShadowCard("squeak");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.MoveCardsToSupportArea(squeak);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, squeak);
		scn.FreepsPass();
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SUPPORT, squeak);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));
		// Three wounds on Hobbits: +3.
		assertEquals(3, scn.GetTwilightCost(scn.GetSite(1)));
		assertEquals(5, scn.GetTwilightCost(scn.GetSite(2)));

		scn.AddWoundsToChar(pippin, 1);
		assertEquals(2, scn.GetWoundsOn(pippin));
		// Four: still +3.
		assertEquals(3, scn.GetTwilightCost(scn.GetSite(1)));
		assertEquals(5, scn.GetTwilightCost(scn.GetSite(2)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
