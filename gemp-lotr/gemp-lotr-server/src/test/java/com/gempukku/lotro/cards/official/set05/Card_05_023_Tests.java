package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("follow", "5_23");
					// Sméagol, Slinker: [gollum] companion, strength 3, vitality 4; to play, add a burden; his
					// skirmish ability is never used
					put("smeagol", "5_29");
					// Smeagol, Once Like Us: the [shire] Smeagol, vitality 4; his assignment ability is never used
					put("shiresmeagol", "103_116");
					// Gollum, Nasty Treacherous Creature: [gollum] minion, vitality 4; his regroup ability is never used
					put("gollum", "5_24");
					// Gollum, Half a Wraith Himself: the [wraith] Gollum, vitality 4; his texts answer a Nazgul played
					// and the assignment phase, and never come up
					put("wraithgollum", "103_66");
					put("savage", "1_151");     // Uruk Savage: a minion that is no Gollum, vitality 3, damage +1 only
					// Sam, Son of Hamfast: a companion who is no Sméagol; his abilities are never used
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FollowSmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Follow Sméagol
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Game Text: Plays on Sméagol.
		 * <b>Regroup:</b> Exert Sméagol or Gollum to play the fellowship's next site (replacing opponent's site if necessary).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("follow");

		assertEquals("Follow Sméagol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilSmeagolIsInPlayAndThenGoesOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follow = scn.GetFreepsCard("follow");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(follow, smeagol);

		scn.StartGame();

		// Frodo and Sam are no Sméagol.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(follow));

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsPlayAvailable(follow));

		// Sméagol is unique, so he is the one bearer it can have and nobody is asked.
		scn.FreepsPlayCard(follow);
		assertAttachedTo(follow, smeagol);
		assertTrue(scn.GetAttachedCards(sam).isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SmeagolExertsToPlayTheFreePeoplesPlayersNextSiteInPlaceOfTheShadowPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follow = scn.GetFreepsCard("follow");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, follow);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);

		// The fellowship is at site 3, and the Shadow player's site 4 lies next, as where he played his own next
		// site on his turn.
		var freepsSite4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);
		scn.MoveCardToAdventurePath(shadowSite4);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(shadowSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite4);
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsActionAvailable(follow));

		// No Gollum is in play, so Sméagol exerts with no question.
		scn.FreepsUseCardAction(follow);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertSame(freepsSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_PATH, freepsSite4);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// With his own site 4 already next, a second use on this numbered path plays nothing: the exertion is
		// paid for nothing.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(follow));
		scn.FreepsUseCardAction(follow);
		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertSame(freepsSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithSmeagolAndTheShadowPlayersGollumBothAbleTheFreePeoplesPlayerChoosesAndGollumMayExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var follow = scn.GetFreepsCard("follow");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, follow);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum, savage);
		scn.FreepsRefreshDecision();

		// The fellowship is at site 2 and site 3 is not yet laid.
		var freepsSite3 = scn.GetFreepsSite(3);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(follow));

		scn.FreepsUseCardAction(follow);
		// Sméagol and the Shadow player's Gollum can each pay; Frodo and the Uruk Savage are neither.
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol, gollum));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gollum);
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertSame(freepsSite3, scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void PlayedOnTheShireSmeagolAndWithHimExhaustedTheWraithGollumExertsWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follow = scn.GetFreepsCard("follow");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(shiresmeagol);
		scn.MoveCardsToHand(follow);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(follow));
		scn.FreepsPlayCard(follow);
		assertAttachedTo(follow, shiresmeagol);

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(wraithgollum, savage);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(shiresmeagol, 3);
		scn.FreepsRefreshDecision();

		var freepsSite3 = scn.GetFreepsSite(3);
		assertNull(scn.GetSite(3));
		assertEquals(3, scn.GetWoundsOn(shiresmeagol));
		assertEquals(0, scn.GetWoundsOn(wraithgollum));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(follow));

		// The Gollum is the only one who can pay, so nobody is asked.
		scn.FreepsUseCardAction(follow);
		assertEquals(1, scn.GetWoundsOn(wraithgollum));
		assertEquals(3, scn.GetWoundsOn(shiresmeagol));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertSame(freepsSite3, scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithTheWraithGollumExhaustedTheShireSmeagolExertsWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var follow = scn.GetFreepsCard("follow");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(shiresmeagol);
		scn.AttachCardsTo(shiresmeagol, follow);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(wraithgollum, savage);
		// Vitality 4 with 3 wounds: the Gollum cannot exert.  Frodo and the Uruk Savage still can.
		scn.AddWoundsToChar(wraithgollum, 3);
		scn.FreepsRefreshDecision();

		var freepsSite3 = scn.GetFreepsSite(3);
		assertNull(scn.GetSite(3));
		assertEquals(0, scn.GetWoundsOn(shiresmeagol));
		assertEquals(3, scn.GetWoundsOn(wraithgollum));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(follow));

		// The Smeagol is the only one who can pay, so nobody is asked.
		scn.FreepsUseCardAction(follow);
		assertEquals(1, scn.GetWoundsOn(shiresmeagol));
		assertEquals(3, scn.GetWoundsOn(wraithgollum));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertSame(freepsSite3, scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithSmeagolAndGollumBothExhausted()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var follow = scn.GetFreepsCard("follow");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, follow);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum, savage);
		// Vitality 4 each with 3 wounds: neither can exert.  Frodo and the Uruk Savage still can.
		scn.AddWoundsToChar(smeagol, 3);
		scn.AddWoundsToChar(gollum, 3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(smeagol));
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(follow));
	}
}
