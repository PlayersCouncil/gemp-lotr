package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_134_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orc", "11_134");
					put("orc2", "11_134");

					// Denizen of Khazad-dum: an [orc] Orc.  Strength +2 only at an underground site, never reached.
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc.  Its trigger at the start of the assignment
					// phase is never reached.
					put("halftroll", "14_12");
					// The Beckoning Shadow: an [orc] support-area condition, no minion (nothing below 5 burdens).
					put("beckoning", "12_83");
					// Goblin Patrol Troop: a [moria] Orc, no text.
					put("troop", "1_177");

					// Aragorn, Ranger of the North: a [gondor] Man and a ranger.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Defend It and Hope: Maneuver event, "Exert a [gondor] Man to wound a minion once": kills a
					// minion of vitality 1.
					put("defend", "4_115");
					put("defend2", "4_115");
					// What Are They?: Maneuver event, "Spot a ranger to discard a roaming minion": a discard, no kill.
					put("whatarethey", "1_119");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PersistentOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Persistent Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: Each time this minion is about to be killed or discarded from play, you may spot another [orc] card to place this minion on top of your draw deck instead.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("orc");

		assertEquals("Persistent Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AboutToBeKilledItMaySpotAnOrcOrcToGoOnTopOfTheDrawDeckInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var orc = scn.GetShadowCard("orc");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(orc, denizen);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SHADOW_CHARACTERS, orc);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);

		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(orc, denizen));
		scn.FreepsChooseCard(orc);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(orc));
		scn.ShadowAcceptOptionalTrigger();

		// On top of its owner's draw deck, not killed into the discard pile.
		assertEquals(orc, scn.GetShadowTopOfDeck());
		assertNotInZone(Zone.DISCARD, orc);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AboutToBeDiscardedFromPlayItMaySpotAnOrcConditionToGoOnTopOfTheDrawDeckInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whatarethey = scn.GetFreepsCard("whatarethey");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatarethey);

		var orc = scn.GetShadowCard("orc");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToSupportArea(beckoning);
		assertInZone(Zone.SUPPORT, beckoning);

		scn.StartGame();
		// At site 2 the Orc (site 4) is roaming.
		scn.MoveMinionsToTable(orc);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SHADOW_CHARACTERS, orc);
		assertInZone(Zone.SUPPORT, beckoning);

		scn.FreepsPlayCard(whatarethey);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(orc));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(orc, scn.GetShadowTopOfDeck());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.SUPPORT, beckoning);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DeclinedBesideAnOrcMinionWhoIsNoOrcItIsKilledAsUsual() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var orc = scn.GetShadowCard("orc");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.StartGame();
		scn.MoveMinionsToTable(orc, halftroll);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SHADOW_CHARACTERS, orc);
		var top = scn.GetShadowTopOfDeck();

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(orc);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(orc));
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(orc);
		assertEquals(top, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyAMoriaOrcBesideIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var orc = scn.GetShadowCard("orc");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// The Orc itself is the only [orc] card in play: "another" leaves nothing to spot.
		scn.MoveMinionsToTable(orc, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SHADOW_CHARACTERS, orc);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(orc);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(orc);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ASecondCopyKilledOffersOnlyItsOwnSave() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var orc = scn.GetShadowCard("orc");
		var orc2 = scn.GetShadowCard("orc2");

		scn.StartGame();
		scn.MoveMinionsToTable(orc, orc2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SHADOW_CHARACTERS, orc);
		assertInZone(Zone.SHADOW_CHARACTERS, orc2);

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(orc2);

		// The first copy is the other [orc] card spotted; only the copy about to be killed answers.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(orc2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(orc));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(orc2, scn.GetShadowTopOfDeck());
		assertInZone(Zone.SHADOW_CHARACTERS, orc);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SavedOnceItIsOfferedAgainTheNextTimeItIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		var defend2 = scn.GetFreepsCard("defend2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend, defend2);

		var orc = scn.GetShadowCard("orc");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(orc, denizen);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SHADOW_CHARACTERS, orc);

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(orc);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(orc));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(orc, scn.GetShadowTopOfDeck());

		// The same card back on the table, as a play of it from the deck would put it.
		scn.MoveMinionsToTable(orc);
		assertInZone(Zone.SHADOW_CHARACTERS, orc);
		scn.ShadowPass();

		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(orc);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(orc));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(orc, scn.GetShadowTopOfDeck());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
