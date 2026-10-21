package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_051_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("castamir", "58_51");

					put("buccaneer", "8_53");   // Corsair Buccaneer: corsair, cost 3; when played, may remove a threat to add tokens
					put("plunderer", "8_58");   // Corsair Plunderer: corsair, cost 2; when played, may discard 2 cards from hand to add tokens
					put("southron", "4_254");   // Southron Soldier: [raider] Man, cost 2, not a corsair; Southron and ambush only

					put("galley", "8_59");      // Corsair War Galley: [raider] possession in the support area that holds [raider] tokens
					put("raiders", "4_242");    // Raiders From the East: [raider] condition in the support area that holds [raider] tokens
					put("sails", "8_50");       // Black Sails of Umbar: [raider] possession in the support area that holds [raider] tokens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CastamirofUmbarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Castamir of Umbar
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Corsair. Enduring.
		 * While in region 2 or 3, this minion is fierce.
		 * Shadow: Exert Castamir of Umbar and play a corsair to reinforce a [raider] card twice.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("castamir");

		assertEquals("Castamir of Umbar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsFierceInRegionsTwoAndThreeAndNotInRegionOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castamir = scn.GetShadowCard("castamir");
		// Another [raider] corsair beside him, who is never fierce: only this minion is.
		var plunderer = scn.GetShadowCard("plunderer");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(castamir, plunderer);

		// Site 3: region 1.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(castamir, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(plunderer, Keyword.FIERCE));

		// Moving to site 4: region 2.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(castamir, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(plunderer, Keyword.FIERCE));

		// Site 7: region 3.  Minions do not outlast the turn, so they are put down again.
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(castamir, plunderer);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(castamir, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(plunderer, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsHimAndPlaysACorsairToReinforceOneRaiderCardTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castamir = scn.GetShadowCard("castamir");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var plunderer = scn.GetShadowCard("plunderer");
		var southron = scn.GetShadowCard("southron");
		var galley = scn.GetShadowCard("galley");
		var raiders = scn.GetShadowCard("raiders");
		var sails = scn.GetShadowCard("sails");
		scn.MoveCardsToHand(buccaneer, plunderer, southron);
		scn.MoveCardsToSupportArea(galley, raiders, sails);

		scn.StartGame();
		scn.MoveMinionsToTable(castamir);
		scn.AddTokensToCard(galley, 1);
		scn.AddTokensToCard(raiders, 1);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 + 2 for site 2 + 1 for the one companion.
		assertEquals(10, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(castamir));
		assertTrue(scn.ShadowActionAvailable(castamir));
		scn.ShadowUseCardAction(castamir);

		assertEquals(1, scn.GetWoundsOn(castamir));
		assertTrue(scn.ShadowHasCardChoiceAvailable(buccaneer, plunderer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(southron));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(buccaneer);

		// 3 to play + 2 roaming.
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, buccaneer);
		assertInHand(plunderer, southron);

		// Black Sails has no [raider] token, so it cannot be reinforced.
		assertTrue(scn.ShadowHasCardChoiceAvailable(galley, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sails));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.GetCultureTokensOn(galley));
		scn.ShadowChooseCard(galley);

		assertEquals(3, scn.GetCultureTokensOn(galley));
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertEquals(0, scn.GetCultureTokensOn(sails));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileCastamirIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castamir = scn.GetShadowCard("castamir");
		var plunderer = scn.GetShadowCard("plunderer");
		var galley = scn.GetShadowCard("galley");
		scn.MoveCardsToHand(plunderer);
		scn.MoveCardsToSupportArea(galley);

		scn.StartGame();
		scn.MoveMinionsToTable(castamir);
		scn.AddTokensToCard(galley, 1);
		// Vitality 4, 3 wounds.
		scn.AddWoundsToChar(castamir, 3);
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(plunderer));
		assertFalse(scn.ShadowActionAvailable(castamir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithoutACorsairInHandThatCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castamir = scn.GetShadowCard("castamir");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var southron = scn.GetShadowCard("southron");
		var galley = scn.GetShadowCard("galley");
		scn.MoveCardsToHand(buccaneer, southron);
		scn.MoveCardsToSupportArea(galley);

		scn.StartGame();
		scn.MoveMinionsToTable(castamir);
		scn.AddTokensToCard(galley, 1);
		scn.SetTwilight(1);
		scn.FreepsPass();

		// 1 + 2 for site 2 + 1 for the one companion: 4.  The Southron Soldier (2 + 2 roaming) can be played and
		// is no corsair; the Corsair Buccaneer (3 + 2 roaming) cannot be paid for.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(southron));
		assertFalse(scn.ShadowPlayAvailable(buccaneer));
		assertFalse(scn.ShadowActionAvailable(castamir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
