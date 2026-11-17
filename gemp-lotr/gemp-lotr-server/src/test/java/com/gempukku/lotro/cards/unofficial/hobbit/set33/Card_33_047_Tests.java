package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_047_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's Shadow phase
	// is at The Troll Hoard 30_50 (site 2); the regroup move goes on to Rivendell 30_51 (site 3).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("malevolent", "33_47");

					put("anger", "30_32");      // Anger: [gundabad] Skirmish event
					put("rise", "33_51");       // Rise of the Necromancer: [sauron] Skirmish event
					put("jail", "32_39");       // Jail: [sauron] condition
					put("notathome", "30_37");  // Not at Home: [gundabad] condition
					// Goblin Song: [moria] Maneuver event; its play from the discard pile needs The Great Goblin
					put("song", "31_33");
					put("attack", "32_38");     // Hidden Attack: [sauron] Regroup event
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void MalevolentOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Malevolent Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: At the end of your Shadow phase, you may take a [gundabad] or [sauron] event from your discard pile into hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("malevolent");

		assertEquals("Malevolent Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheEndOfTheShadowPhaseMayTakeAGundabadOrSauronEventIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malevolent = scn.GetShadowCard("malevolent");
		var anger = scn.GetShadowCard("anger");
		var rise = scn.GetShadowCard("rise");
		var jail = scn.GetShadowCard("jail");
		var notathome = scn.GetShadowCard("notathome");
		var song = scn.GetShadowCard("song");
		var freepsAttack = scn.GetFreepsCard("attack");

		scn.StartGame();
		scn.MoveCardsToDiscard(anger, rise, jail, notathome, song, freepsAttack);
		assertInDiscard(anger, rise, jail, notathome, song, freepsAttack);
		scn.FreepsPass();
		scn.MoveMinionsToTable(malevolent);

		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(malevolent));
		scn.ShadowAcceptOptionalTrigger();
		// An event of each culture named; not a [sauron] or [gundabad] condition, not a [moria] event, not the Free
		// Peoples player's [sauron] event in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(anger, rise));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(jail, notathome, song, freepsAttack));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(anger);

		assertInHand(anger);
		assertInDiscard(rise, jail, notathome, song, freepsAttack);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DeclinedItIsOfferedAgainAtTheEndOfASecondShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malevolent = scn.GetShadowCard("malevolent");
		var rise = scn.GetShadowCard("rise");

		scn.StartGame();
		scn.MoveCardsToDiscard(rise);
		assertInDiscard(rise);
		scn.FreepsPass();
		scn.MoveMinionsToTable(malevolent);

		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(malevolent));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(rise);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The Malevolent Orc is left unassigned and stands into the regroup phase; the fellowship moves again.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInDiscard(rise);

		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(malevolent));
		scn.ShadowAcceptOptionalTrigger();
		// The one such event, taken unasked.
		assertInHand(rise);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NothingIsOfferedAtTheEndOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malevolent = scn.GetShadowCard("malevolent");
		var rise = scn.GetShadowCard("rise");

		scn.StartGame();
		scn.MoveCardsToDiscard(rise);
		assertInDiscard(rise);
		scn.FreepsPass();
		scn.MoveMinionsToTable(malevolent);
		scn.ShadowPass();
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(rise);

		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(rise);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWithNoGundabadOrSauronEventInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malevolent = scn.GetShadowCard("malevolent");
		var jail = scn.GetShadowCard("jail");
		var notathome = scn.GetShadowCard("notathome");
		var song = scn.GetShadowCard("song");

		scn.StartGame();
		scn.MoveCardsToDiscard(jail, notathome, song);
		assertInDiscard(jail, notathome, song);
		scn.FreepsPass();
		scn.MoveMinionsToTable(malevolent);

		scn.ShadowPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(jail, notathome, song);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
