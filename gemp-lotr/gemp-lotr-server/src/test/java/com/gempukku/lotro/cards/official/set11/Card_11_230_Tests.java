package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_230_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a ranger to bear the condition and the sword
					put("nostranger", "1_108");     // No Stranger to the Shadows: [gondor] condition on a ranger; each site's Shadow number -1
					put("sword", "1_112");          // Ranger's Sword: [gondor] possession on Aragorn, damage +1; no condition
					put("lord", "1_21");            // Lord of Moria: [dwarven] condition in the support area; each Dwarf damage +1
					put("weary", "4_212");          // Weary: [isengard] condition on a companion; it answers an assignment to an [isengard] tracker

					put("nelya", "2_84");           // Úlairë Nelya: [wraith] Nazgûl, cost 5, site 2; fierce only at 2 burdens
					put("khamul", "103_61");        // Úlairë Attëa, Khamûl of the East: [raider] Nazgûl, cost 6, site 3; his text needs 3 burdens
					put("hound", "7_192");          // Morgul Hound: [wraith] Orc and no Nazgûl, cost 2; stronger at 2 wounds on the Ring-bearer
					put("beckoning", "12_83");      // The Beckoning Shadow: [orc] condition in the support area; nothing below 5 burdens
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");         // Fangorn Glade: its text answers a move to it; the game starts there
					put("homestead", "11_230");
					// Never laid in these tests.
					put("site3", "11_234");
					put("site5", "18_138");
					put("site6", "12_187");
					put("site7", "13_185");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void BucklandHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Buckland Homestead
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Dwelling</b>. <b>Shadow:</b> Play a Nazgûl to discard a condition from play.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("homestead");

		assertEquals("Buckland Homestead", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.DWELLING));
	}

	@Test
	public void ShadowAbilityPlaysANazgulFromHandToDiscardAConditionOfEitherSideFromPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var nostranger = scn.GetFreepsCard("nostranger");
		var sword = scn.GetFreepsCard("sword");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, nostranger, sword);
		scn.MoveCardsToSupportArea(lord);

		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var beckoning = scn.GetShadowCard("beckoning");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCardsToSupportArea(beckoning);
		scn.AttachCardsTo(frodo, weary);
		scn.MoveCardsToHand(nelya, khamul, hound);

		scn.StartGame(scn.GetFreepsSite("site1"));
		// Enough for any of the three minions, roaming included: Khamûl 6 + 2, the Hound 2 + 2.
		scn.SetTwilight(15);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("homestead"));

		var homestead = scn.GetCurrentSite();
		assertEquals("Buckland Homestead", homestead.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.ATTACHED, nostranger, sword, weary);
		assertInZone(Zone.SUPPORT, lord, beckoning);
		assertTrue(scn.ShadowActionAvailable(homestead));

		scn.ShadowUseCardAction(homestead);
		// A Nazgûl of each culture is offered; a [wraith] minion who is no Nazgûl is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);

		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertInHand(khamul, hound);
		assertEquals(2, scn.GetShadowHandCount());

		// Conditions of either side, in a support area or borne, are offered; a possession is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nostranger, lord, beckoning, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nostranger);

		assertInDiscard(nostranger);
		assertInZone(Zone.ATTACHED, sword, weary);
		assertInZone(Zone.SUPPORT, lord, beckoning);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithThePoolOneShortOfTheNazgulsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToSupportArea(lord);

		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(nelya);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("homestead"));

		var homestead = scn.GetCurrentSite();
		assertEquals("Buckland Homestead", homestead.getBlueprint().getTitle());

		// Nelya costs 5, and her site number is 2: no roaming at site 2.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(homestead));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(homestead));
	}
}
