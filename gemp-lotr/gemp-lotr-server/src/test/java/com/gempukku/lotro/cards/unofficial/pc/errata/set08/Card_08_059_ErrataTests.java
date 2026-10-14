package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_059_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galley", "58_59");

					put("sails", "8_50");       // Black Sails of Umbar: [raider] support possession, "When you play this possession, you may add a [raider] token here"
					put("ladder", "6_58");      // Assault Ladder: [isengard] support condition that holds [isengard] tokens
					put("freebooter", "8_54");  // Corsair Freebooter: [raider] Man minion (Corsair); its text is a when-played trigger only
					put("soldier", "4_254");    // Southron Soldier: [raider] Man minion (Southron), Ambush (2), nothing else
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: [raider] Nazgul minion; his text needs 3 burdens
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text

					// Cards for the Free Peoples player's hand, which sets who has initiative.
					put("card1", "1_177");
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairWarGalleyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Corsair War Galley
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: When you play this possession, you may add a [raider] token here.
		 * While you can spot 6 [raider] tokens and a [raider] Man, the Shadow has initiative, regardless of the Free Peoples player's hand.
		 * Regroup: Add (1) for each [raider] token you can spot. Discard this possession.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("galley");

		assertEquals("Corsair War Galley", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysForOneToTheSupportAreaAndMayAddARaiderTokenToItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galley = scn.GetShadowCard("galley");
		var sails = scn.GetShadowCard("sails");
		scn.MoveCardsToHand(galley);
		scn.MoveCardsToSupportArea(sails);
		scn.AddTokensToCard(sails, 1);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set + 1 companion + 2 for Ettenmoors
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(galley));
		scn.ShadowPlayCard(galley);

		// A possession does not roam.
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, galley);
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(galley));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(galley));
		assertEquals(1, scn.GetCultureTokensOn(sails));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineToAddTheToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galley = scn.GetShadowCard("galley");
		scn.MoveCardsToHand(galley);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(galley);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(galley));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertInZone(Zone.SUPPORT, galley);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSixthRaiderTokenGivesTheShadowInitiativeWithAFullFreePeoplesHandAndAnotherRaiderPlayAddsNoTokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galley = scn.GetShadowCard("galley");
		var sails = scn.GetShadowCard("sails");
		var ladder = scn.GetShadowCard("ladder");
		var freebooter = scn.GetShadowCard("freebooter");
		scn.MoveCardsToSupportArea(galley, ladder);
		scn.MoveCardsToHand(sails);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.MoveMinionsToTable(freebooter);
		// 5 [raider] tokens, on the Galley and on a minion, and an [isengard] token: 6 tokens, 5 of them [raider].
		scn.AddTokensToCard(galley, 2);
		scn.AddTokensToCard(freebooter, 3);
		scn.AddTokensToCard(ladder, 1);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		scn.ShadowPlayCard(sails);
		// Black Sails offers its own token; the Galley offers nothing for another [raider] card's play.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(galley));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sails));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetCultureTokensOn(galley));
		assertEquals(1, scn.GetCultureTokensOn(sails));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SixRaiderTokensWithARaiderNazgulAndADunlandManLeaveTheInitiativeUntilARaiderManIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galley = scn.GetShadowCard("galley");
		var sails = scn.GetShadowCard("sails");
		var soldier = scn.GetShadowCard("soldier");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(galley, sails);
		scn.MoveCardsToHand(soldier);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.MoveMinionsToTable(khamul, brigand);
		scn.AddTokensToCard(galley, 3);
		scn.AddTokensToCard(sails, 3);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		scn.ShadowPlayCard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionAddsOneForEachRaiderTokenItsOwnIncludedAndDiscardsTheGalley() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galley = scn.GetShadowCard("galley");
		var sails = scn.GetShadowCard("sails");
		var ladder = scn.GetShadowCard("ladder");
		var freebooter = scn.GetShadowCard("freebooter");
		scn.MoveCardsToSupportArea(galley, sails, ladder);

		scn.StartGame();
		scn.AddTokensToCard(galley, 2);
		scn.AddTokensToCard(sails, 1);
		scn.AddTokensToCard(ladder, 4);

		// No minions, so the game goes from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(freebooter);
		scn.AddTokensToCard(freebooter, 2);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(galley));
		scn.ShadowUseCardAction(galley);

		// 1 + 2 tokens on the Galley + 1 on Black Sails + 2 on a minion; the 4 [isengard] tokens do not count.
		assertEquals(6, scn.GetTwilight());
		assertInDiscard(galley);
		assertEquals(1, scn.GetCultureTokensOn(sails));
		assertEquals(2, scn.GetCultureTokensOn(freebooter));
		assertEquals(4, scn.GetCultureTokensOn(ladder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedInTheShadowPhaseAndAddsOneForASingleRaiderToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galley = scn.GetShadowCard("galley");
		var ladder = scn.GetShadowCard("ladder");
		scn.MoveCardsToSupportArea(galley, ladder);

		scn.StartGame();
		scn.AddTokensToCard(galley, 1);
		scn.AddTokensToCard(ladder, 2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(galley));
		scn.ShadowPass();

		// No minions, so the game goes from the Shadow phase to the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(galley));
		scn.ShadowUseCardAction(galley);

		// 1 + 1 [raider] token; the 2 [isengard] tokens do not count.
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(galley);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
