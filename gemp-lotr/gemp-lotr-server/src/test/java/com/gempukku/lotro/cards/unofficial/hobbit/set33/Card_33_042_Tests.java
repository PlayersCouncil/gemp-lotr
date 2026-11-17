package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_042_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  Its mountains are Erebor 30_55 (site 7, also a river) and Smaug's Den 30_56 (site 8, also underground);
	// Esgaroth 30_54 (site 6) is a river and a sanctuary and no mountain.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("giant", "33_42");
					// A second copy: its own play from the discard pile, its own exertion.
					put("giant2", "33_42");
					// Orcs of two cultures, and a [moria] minion who is no Orc.
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc; an Assignment ability, not used
					put("footman", "30_42");     // Goblin Footman: [moria] Orc; a "When you play" trigger, cheated in
					put("troll", "32_31");       // Demolition Troll: [moria] Troll; a Skirmish ability, not used
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GraniteGiantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Granite Giant
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Giant
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Shadow:</b> At a mountain, remove (2) to play this minion from your discard pile.<br><b>Archery:</b> Exert this minion and discard an Orc from play to make the minion archery total +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("giant");

		assertEquals("Granite Giant", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.GIANT, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtEreborRemoving2PlaysThisCopyFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");
		var giant2 = scn.GetShadowCard("giant2");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToDiscard(giant, giant2);
		assertInZone(Zone.DISCARD, giant, giant2);
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DISCARD, giant, giant2);
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();

		// 2 removed + 5 to play: the giant's site is 4, so it is not roaming at site 7.
		assertTrue(scn.ShadowActionAvailable(giant));
		scn.ShadowUseCardAction(giant);
		assertInZone(Zone.SHADOW_CHARACTERS, giant);
		assertInZone(Zone.DISCARD, giant2);
		// 7 - 2 removed - 5 to play
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtEreborItIsNotOfferedWithOneTooFewForTheRemovalAndThePlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToDiscard(giant);
		assertInZone(Zone.DISCARD, giant);
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, giant);
		// 2 removed + 5 to play - 1
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(giant));
	}

	@Test
	public void AtSmaugsDenItIsPlayedFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToDiscard(giant);
		assertInZone(Zone.DISCARD, giant);
		scn.FreepsPass();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DISCARD, giant);
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(giant));
		scn.ShadowUseCardAction(giant);
		assertInZone(Zone.SHADOW_CHARACTERS, giant);
		// 7 - 2 removed - 5 to play
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtEsgarothARiverThatIsNoMountainItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToDiscard(giant);
		assertInZone(Zone.DISCARD, giant);
		scn.FreepsPass();

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, giant);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(giant));
		assertInZone(Zone.DISCARD, giant);
	}

	@Test
	public void ArcheryExertsThisCopyAndDiscardsAnOrcToAdd2ToTheMinionArcheryTotalForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");
		var giant2 = scn.GetShadowCard("giant2");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveMinionsToTable(giant, giant2, aggressor, footman, troll);
		assertInZone(Zone.SHADOW_CHARACTERS, giant, giant2, aggressor, footman, troll);
		assertEquals(0, scn.GetWoundsOn(giant));
		assertEquals(0, scn.GetWoundsOn(giant2));

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// No minion here is an archer.
		assertEquals(0, scn.GetShadowArcheryTotal());

		assertTrue(scn.ShadowActionAvailable(giant));
		scn.ShadowUseCardAction(giant);
		// The exertion is this copy's own.
		assertEquals(1, scn.GetWoundsOn(giant));
		assertEquals(0, scn.GetWoundsOn(giant2));
		// Orcs of two cultures; not a [moria] Troll, nor either giant.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, footman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, giant, giant2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(footman);
		assertInZone(Zone.DISCARD, footman);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, troll, giant2);
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// The +2 is fired at Bilbo, the only companion, and is gone once the archery phase is over.
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		scn.PassArcheryActions();
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(2, scn.GetWoundsOn(scn.GetRingBearer()));
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ArcheryIsNotOfferedOnAnExhaustedCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");
		var giant2 = scn.GetShadowCard("giant2");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveMinionsToTable(giant, giant2, footman);
		scn.AddWoundsToChar(giant, 2);
		assertEquals(2, scn.GetWoundsOn(giant));
		assertEquals(0, scn.GetWoundsOn(giant2));
		assertInZone(Zone.SHADOW_CHARACTERS, footman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// Vitality 3 with 2 wounds: exhausted, and so it cannot exert.  The other copy can.
		assertFalse(scn.ShadowActionAvailable(giant));
		assertTrue(scn.ShadowActionAvailable(giant2));
	}

	@Test
	public void ArcheryIsNotOfferedWithNoOrcToDiscardUntilOneIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");
		var footman = scn.GetShadowCard("footman");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveMinionsToTable(giant, troll);
		assertEquals(0, scn.GetWoundsOn(giant));

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// A [moria] Troll is no Orc.
		assertFalse(scn.ShadowActionAvailable(giant));

		scn.MoveMinionsToTable(footman);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		assertTrue(scn.ShadowActionAvailable(giant));
	}
}
