package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_072_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Free Peoples cards that sit in the support area, of three types and two cultures.
					// Stairs of Khazad-dum: [dwarven] condition; its text is about a Dwarf skirmishing a [moria] minion
					put("stairs", "1_24");
					// Old Toby: [shire] possession; "When you play this possession, you may draw a card."
					put("toby", "1_305");
					// The Gaffer: [shire] ally; a fellowship ability that is only ever used on purpose
					put("gaffer", "1_291");
					// Free Peoples cards in play that are not in the support area.
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion
					put("armor", "1_8");        // Dwarven Armor: a possession borne by a Dwarf

					// Shadow cards that sit in the support area, of two types and two cultures.
					// Pinned Down: [moria] condition; it does nothing without a [moria] archer
					put("pinned", "1_192");
					// Scaffolding: [isengard] possession; its text is about [isengard] Orcs
					put("scaffolding", "6_73");
					// Shadow cards in play that are not in the support area.
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("spear", "1_182");      // Goblin Spear: a possession borne by a [moria] minion
					// Spider Poison: a Shadow possession borne by a companion; it does nothing outside a skirmish
					put("poison", "8_28");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "102_72");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesolationofSarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Desolation of Saruman
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 8
		 * Shadow Number: 8
		 * Direction: Left
		 * Game Text: When the fellowship moves to this site, each player may discard a card from their opponent's
		 * 	support area.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Desolation of Saruman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void EachPlayerMayDiscardACardFromTheOtherPlayersSupportAreaWhenTheFellowshipMovesToTheDesolation() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		var toby = scn.GetFreepsCard("toby");
		var gaffer = scn.GetFreepsCard("gaffer");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var pinned = scn.GetShadowCard("pinned");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.SkipToSite(7);

		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor, poison);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToSupportArea(stairs, toby, pinned, scaffolding);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear);

		scn.FreepsPass();
		assertEquals("Desolation of Saruman", scn.GetCurrentSite().getBlueprint().getTitle());

		// The Free Peoples player: the Shadow player's condition and possession in the support area, and not his
		// minion, what it bears, or what he has put on a companion.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, scaffolding));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, spear, poison, stairs, toby, gaffer, gimli, armor));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(scaffolding);

		assertInDiscard(scaffolding);
		assertInZone(Zone.SUPPORT, pinned);

		// The Shadow player: the Free Peoples player's condition, possession and ally in the support area, and not
		// his companion or what the companion bears.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));
		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(stairs, toby, gaffer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, armor, poison, pinned, troop, spear));
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(gaffer);

		assertInDiscard(gaffer);
		assertInZone(Zone.SUPPORT, stairs, toby, pinned);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(poison, gimli);
		assertAttachedTo(spear, troop);

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BothPlayersMayDeclineAndNothingIsOfferedWhenTheFellowshipMovesOnFromTheDesolation() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		var toby = scn.GetFreepsCard("toby");
		var pinned = scn.GetShadowCard("pinned");
		var scaffolding = scn.GetShadowCard("scaffolding");

		scn.StartGame();
		scn.SkipToSite(7);

		scn.MoveCardsToSupportArea(stairs, toby, pinned, scaffolding);

		scn.FreepsPass();
		assertEquals("Desolation of Saruman", scn.GetCurrentSite().getBlueprint().getTitle());

		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseNo();
		// The Shadow player is asked all the same.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));
		scn.ShadowChooseNo();

		assertInZone(Zone.SUPPORT, stairs, toby, pinned, scaffolding);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No minions, so no skirmishes; the fellowship moves on, from the Desolation to site 9.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard"));
		assertFalse(scn.ShadowDecisionAvailable("Would you like to discard"));
		assertInZone(Zone.SUPPORT, stairs, toby, pinned, scaffolding);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSecondFellowshipToMoveToTheDesolationSetsItOffAgainWithTheRolesTheOtherWayRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Player 1's cards: Free Peoples cards on his own turn, Shadow cards on player 2's.
		var stairs1 = scn.GetFreepsCard("stairs");
		var toby1 = scn.GetFreepsCard("toby");
		var pinned1 = scn.GetFreepsCard("pinned");
		var scaffolding1 = scn.GetFreepsCard("scaffolding");
		// Player 2's cards.
		var stairs2 = scn.GetShadowCard("stairs");
		var gaffer2 = scn.GetShadowCard("gaffer");
		var pinned2 = scn.GetShadowCard("pinned");
		var scaffolding2 = scn.GetShadowCard("scaffolding");

		scn.StartGame();
		scn.SkipToSite(7);

		// Player 1's fellowship moves to the Desolation first; both players have something to lose and decline.
		scn.MoveCardsToSupportArea(stairs1, toby1, pinned2, scaffolding2);
		scn.FreepsPass();
		assertEquals("Desolation of Saruman", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsChooseNo();
		scn.ShadowChooseNo();

		scn.SkipToOtherPlayersTurn();

		// Player 2's fellowship phase at site 7.  Each player now has cards of both sides in his support area; the
		// ones left from player 1's turn are not active on this one.
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveCardsToSupportArea(stairs2, pinned1, scaffolding1);
		scn.MoveAlliesToTable(gaffer2);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.ShadowPass();

		assertEquals("Desolation of Saruman", scn.GetCurrentSite().getBlueprint().getTitle());

		// Player 2 is the Free Peoples player now: he may discard one of player 1's Shadow cards.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));
		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(pinned1, scaffolding1));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(stairs1, toby1, stairs2, gaffer2, pinned2, scaffolding2));
		scn.ShadowChooseCard(pinned1);
		assertInDiscard(pinned1);

		// Player 1 is the Shadow player: he may discard one of player 2's Free Peoples cards.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs2, gaffer2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pinned2, scaffolding2, stairs1, toby1, scaffolding1));
		scn.FreepsChooseCard(stairs2);
		assertInDiscard(stairs2);

		assertInZone(Zone.SUPPORT, stairs1, toby1, scaffolding1, gaffer2, pinned2, scaffolding2);
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void TheFreePeoplesPlayerIsNotAskedWhenTheShadowPlayerHasNothingInHisSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		var toby = scn.GetFreepsCard("toby");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.SkipToSite(7);

		// The Shadow player has cards in play, and none of them in his support area.
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, poison);
		scn.MoveCardsToSupportArea(stairs, toby);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear);

		scn.FreepsPass();
		assertEquals("Desolation of Saruman", scn.GetCurrentSite().getBlueprint().getTitle());

		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard"));
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));
		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(stairs, toby));
		scn.ShadowChooseCard(toby);
		assertInDiscard(toby);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard"));
		assertInZone(Zone.SUPPORT, stairs);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertAttachedTo(spear, troop);
		assertAttachedTo(poison, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerIsNotAskedWhenTheFreePeoplesPlayerHasNothingInHisSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var pinned = scn.GetShadowCard("pinned");
		var scaffolding = scn.GetShadowCard("scaffolding");

		scn.StartGame();
		scn.SkipToSite(7);

		// The Free Peoples player has cards in play, and none of them in his support area.
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToSupportArea(pinned, scaffolding);

		scn.FreepsPass();
		assertEquals("Desolation of Saruman", scn.GetCurrentSite().getBlueprint().getTitle());

		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, scaffolding));
		scn.FreepsChooseCard(pinned);
		assertInDiscard(pinned);

		assertFalse(scn.ShadowDecisionAvailable("Would you like to discard"));
		assertInZone(Zone.SUPPORT, scaffolding);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	@Ignore("F-0093: the Shadow player is offered a site he controls himself when it came from the other player's adventure deck")
	public void TheShadowPlayerIsNotOfferedASiteHeControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		var toby = scn.GetFreepsCard("toby");
		var pinned = scn.GetShadowCard("pinned");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		var poison = scn.GetShadowCard("poison");
		// Site 1 on the path is from the Free Peoples player's adventure deck, site 2 from the Shadow player's.
		var bagEnd = scn.GetFreepsSite(1);
		var ettenmoors = scn.GetShadowSite(2);

		scn.StartGame();
		scn.SkipToSite(6);

		scn.MoveCardsToSupportArea(stairs, toby, pinned, scaffolding);
		// The rig takes a site for the Shadow player during his Shadow phase, and only while he has nothing he
		// could play: the rest of his Shadow cards are put out of his hand.
		scn.MoveCardsToDiscard(troop, spear, poison);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());

		// A controlled site lies in the support area of the player who controls it, whoever's card it is.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(bagEnd));
		assertTrue(scn.ShadowControls(ettenmoors));
		assertInZone(Zone.SUPPORT, bagEnd, ettenmoors);

		// No minions, so no skirmishes; the fellowship moves a second time, from site 7 to the Desolation.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("Desolation of Saruman", scn.GetCurrentSite().getBlueprint().getTitle());

		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard"));
		scn.FreepsChooseNo();

		// The Shadow player's opponent controls no site.  Both sites are in the Shadow player's own support area,
		// and he is offered neither: only the Free Peoples player's condition and possession.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));
		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(stairs, toby));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bagEnd, ettenmoors, pinned, scaffolding));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(toby);

		assertInDiscard(toby);
		assertInZone(Zone.SUPPORT, stairs, pinned, scaffolding, bagEnd, ettenmoors);
		assertTrue(scn.ShadowControls(bagEnd));
		assertTrue(scn.ShadowControls(ettenmoors));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
