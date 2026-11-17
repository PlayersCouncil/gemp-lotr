package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_129_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sieger", "18_129");

					put("hounding", "11_188");   // Hounding Uruk: [uruk-hai] Uruk-hai, cost 2, strength 5; +3 only while a companion is exhausted
					put("saruman", "17_116");    // Saruman, Master of the White Hand: an [uruk-hai] Wizard; companions resistance -1,
					                             // and a skirmish ability used only on purpose
					put("guard", "10_81");       // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1, no other text
					put("endless", "13_161");    // Endless Assault: an [uruk-hai] condition; a skirmish ability used only on purpose
					put("lastchild", "4_148");   // Down to the Last Child: [isengard] support condition that gains a token each time a
					                             // companion loses to an Uruk-hai. Shadow: remove 2 [isengard] tokens to take control of a site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandSiegerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: White Hand Sieger
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 0
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>To play, spot an [uruk-hai] minion and you must control a site.
		*/

		var scn = GetScenario();

		var sieger = scn.GetFreepsCard("sieger");

		assertEquals("White Hand Sieger", sieger.getBlueprint().getTitle());
		assertNull(sieger.getBlueprint().getSubtitle());
		assertFalse(sieger.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sieger.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, sieger.getBlueprint().getCulture());
		assertEquals(CardType.MINION, sieger.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, sieger.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(sieger, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sieger, Keyword.DAMAGE));
		assertEquals(0, sieger.getBlueprint().getTwilightCost());
		assertEquals(7, sieger.getBlueprint().getStrength());
		assertEquals(1, sieger.getBlueprint().getVitality());
		assertEquals(5, sieger.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiCultureMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sieger = scn.GetShadowCard("sieger");
		var hounding = scn.GetShadowCard("hounding");
		var guard = scn.GetShadowCard("guard");
		var endless = scn.GetShadowCard("endless");

		scn.StartGame();
		// Player 1's third turn: site 1 was behind both markers all through his second, when the Shadow player
		// could take it.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCardsToHand(sieger, hounding);
		// A [sauron] Uruk-hai on the table, and an [uruk-hai] card that is no minion.
		scn.MoveMinionsToTable(guard);
		scn.MoveCardsToSupportArea(endless);
		scn.SetTwilight(5);
		assertInHand(sieger, hounding);
		assertInZone(Zone.SUPPORT, endless);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sieger));

		scn.ShadowPlayCard(hounding);
		assertTrue(scn.ShadowPlayAvailable(sieger));

		scn.ShadowPlayCard(sieger);
		assertInZone(Zone.SHADOW_CHARACTERS, sieger);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyUrukHaiCultureMinionIsNoUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sieger = scn.GetShadowCard("sieger");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCardsToHand(sieger);
		scn.MoveMinionsToTable(saruman);
		assertInHand(sieger);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(sieger));
		scn.ShadowPlayCard(sieger);
		assertInZone(Zone.SHADOW_CHARACTERS, sieger);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilASiteIsControlledAndPlayingItTakesNoSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sieger = scn.GetShadowCard("sieger");
		var hounding = scn.GetShadowCard("hounding");
		var lastchild = scn.GetShadowCard("lastchild");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveCardsToHand(sieger);
		scn.MoveMinionsToTable(hounding);
		scn.MoveCardsToSupportArea(lastchild);
		// Tokens from companions lost to Uruk-hai on earlier turns.
		scn.AddTokensToCard(lastchild, 2);
		assertInHand(sieger);
		assertEquals(2, scn.GetCultureTokensOn(lastchild));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.FreepsPass();

		// At site 4, sites 1 and 2 are behind both markers and could be taken, but none is controlled: the site
		// must be controlled already (LR 5), and playing the minion is no way to take one.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sieger));

		scn.ShadowUseCardAction(lastchild);
		assertEquals(0, scn.GetCultureTokensOn(lastchild));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowPlayAvailable(sieger));

		scn.ShadowPlayCard(sieger);
		assertInZone(Zone.SHADOW_CHARACTERS, sieger);
		// No second site was taken by the play.
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertFalse(scn.IsSiteControlled(site2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASiteTheOtherPlayerControlsDoesNotLetItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sieger = scn.GetShadowCard("sieger");
		var hounding = scn.GetShadowCard("hounding");

		scn.StartGame();
		// Player 1's second turn: player 1 took site 1 on player 2's turn, as its Shadow player.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(site1));
		scn.MoveCardsToHand(sieger);
		scn.MoveMinionsToTable(hounding);
		assertInHand(sieger);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.ShadowPlayAvailable(sieger));
	}
}
