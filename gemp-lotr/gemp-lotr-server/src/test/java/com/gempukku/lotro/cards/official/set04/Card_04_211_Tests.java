package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_211_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("weapons1", "4_211");
					put("weapons2", "4_211");
					put("assassin", "1_131");   // Orthanc Assassin: unique [isengard] Uruk-hai archer, cost 5, site 5, no other text
					put("bowmen", "4_138");     // Band of Uruk Bowmen: [isengard] Uruk-hai archer, cost 5, site 5; +1 to the minion archery total only while its owner controls a site
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, cost 3, Archer is its whole text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai who is no archer, cost 2, Damage +1 and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4, to take the archery wounds
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeaponsofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Weapons of Isengard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Machine</b>. Plays to your support area.<br><b>Shadow:</b> Play an [isengard] archer to place an [isengard] token on this card.<br><b>Archery:</b> Remove 2 [isengard] tokens from this card to make the minion archery total +2. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("weapons1");

		assertEquals("Weapons of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityPlaysAnIsengardArcherFromHandToPlaceATokenOnTheCopyUsedOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons1 = scn.GetShadowCard("weapons1");
		var weapons2 = scn.GetShadowCard("weapons2");
		var assassin = scn.GetShadowCard("assassin");
		var bowmen = scn.GetShadowCard("bowmen");
		var marksman = scn.GetShadowCard("marksman");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(weapons1, weapons2);
		scn.MoveCardsToHand(assassin, bowmen, marksman, savage);
		assertInHand(assassin, bowmen, marksman, savage);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(weapons1));
		assertEquals(0, scn.GetCultureTokensOn(weapons2));
		assertTrue(scn.ShadowActionAvailable(weapons1));

		scn.ShadowUseCardAction(weapons1);
		// [isengard] archers of two titles; not a [moria] archer, not an [isengard] minion who is no archer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(assassin, bowmen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(marksman, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(assassin);

		assertInZone(Zone.SHADOW_CHARACTERS, assassin);
		assertInHand(bowmen, marksman, savage);
		// One archer played, one token, on the copy used alone.
		assertEquals(1, scn.GetCultureTokensOn(weapons1));
		assertEquals(0, scn.GetCultureTokensOn(weapons2));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The other copy wants an archer of its own.
		assertTrue(scn.ShadowActionAvailable(weapons2));
		scn.ShadowUseCardAction(weapons2);
		assertInZone(Zone.SHADOW_CHARACTERS, bowmen);
		assertEquals(1, scn.GetCultureTokensOn(weapons1));
		assertEquals(1, scn.GetCultureTokensOn(weapons2));

		// No [isengard] archer is left in hand: neither copy is offered, though the [moria] archer and the
		// [isengard] Uruk-hai who is no archer can both be played.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(marksman));
		assertTrue(scn.ShadowPlayAvailable(savage));
		assertFalse(scn.ShadowActionAvailable(weapons1));
		assertFalse(scn.ShadowActionAvailable(weapons2));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheIsengardArcherInHandCannotBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons1 = scn.GetShadowCard("weapons1");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCardsToSupportArea(weapons1);
		scn.MoveCardsToHand(assassin);

		scn.StartGame();
		scn.FreepsPass();

		// 5 for the Assassin + 2 for roaming at site 2: one short.
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(assassin));
		assertFalse(scn.ShadowActionAvailable(weapons1));

		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(assassin));
		assertTrue(scn.ShadowActionAvailable(weapons1));
		assertEquals(0, scn.GetCultureTokensOn(weapons1));
	}

	@Test
	public void ArcheryAbilityRemovesTwoTokensFromThisCopyToMakeTheMinionArcheryTotalPlusTwoForThePhaseAndDiscardsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons1 = scn.GetShadowCard("weapons1");
		var weapons2 = scn.GetShadowCard("weapons2");
		var marksman = scn.GetShadowCard("marksman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(weapons1, weapons2);
		assertInZone(Zone.SUPPORT, weapons1, weapons2);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		// The card gives itself tokens; laid here by hand.
		scn.AddTokensToCard(weapons1, 2);
		scn.AddTokensToCard(weapons2, 1);
		assertEquals(2, scn.GetCultureTokensOn(weapons1));
		assertEquals(1, scn.GetCultureTokensOn(weapons2));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// The Marksman alone.
		assertEquals(1, scn.GetShadowArcheryTotal());
		// One token on this copy is not enough, though the other copy holds two.
		assertFalse(scn.ShadowActionAvailable(weapons2));
		assertTrue(scn.ShadowActionAvailable(weapons1));

		scn.ShadowUseCardAction(weapons1);
		// 1 for the Marksman + 2
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertInDiscard(weapons1);
		assertInZone(Zone.SUPPORT, weapons2);
		assertEquals(1, scn.GetCultureTokensOn(weapons2));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// Still in force as the three archery wounds are placed, and gone with the phase.
		scn.PassArcheryActions();
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetShadowArcheryTotal());
	}
}
