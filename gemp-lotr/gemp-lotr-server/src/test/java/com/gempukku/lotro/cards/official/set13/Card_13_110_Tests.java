package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("informant", "13_110");
					put("informant2", "13_110");
					put("olog", "11_108");      // Beastly Olog-hai: [orc] Troll, cost 6, site 5; to play, spot an [orc] minion; an assignment ability used only on purpose
					put("grunt", "13_117");     // Ordnance Grunt: [orc] Troll, cost 6, site 5; to play, spot an [orc] minion; a Shadow ability from the discard pile
					put("pittroll", "19_22");   // Pit Troll: [orc] Troll, cost 7; laid only in the Free Peoples player's discard pile, a title the Shadow pile does not hold
					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] Half-troll, no Troll, cost 3, site 4; its trigger is at the start of the assignment phase
					put("hilltroll", "8_102");  // Great Hill Troll: [sauron] Troll, cost 9, site 5; its played trigger is for sites 5K to 9K only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardInformantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Isengard Informant
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> Exert this minion to play an [orc] Troll from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("informant");

		assertEquals("Isengard Informant", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityExertsItToPlayAnOrcTrollFromItsOwnersDiscardPileUntilItIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var informant = scn.GetShadowCard("informant");
		var informant2 = scn.GetShadowCard("informant2");
		var olog = scn.GetShadowCard("olog");
		var grunt = scn.GetShadowCard("grunt");
		var halftroll = scn.GetShadowCard("halftroll");
		var hilltroll = scn.GetShadowCard("hilltroll");
		var pittroll = scn.GetFreepsCard("pittroll");

		scn.StartGame();
		scn.MoveCardsToDiscard(olog, grunt, halftroll, hilltroll, pittroll);
		scn.SkipToPhase(Phase.REGROUP);
		// Minions that lived through the skirmishes stand here; the Informant is laid at the start of the regroup phase.
		scn.MoveMinionsToTable(informant, informant2);
		scn.SetTwilight(12);
		assertInDiscard(olog, grunt, halftroll, hilltroll, pittroll);
		assertEquals(0, scn.GetWoundsOn(informant));
		assertEquals(0, scn.GetWoundsOn(informant2));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(informant));
		scn.ShadowUseCardAction(informant);
		// The copy used exerts, and the other copy does not.
		assertEquals(1, scn.GetWoundsOn(informant));
		assertEquals(0, scn.GetWoundsOn(informant2));

		// Only the [orc] Trolls of the Shadow player's own pile: not the [orc] Half-troll, not the [sauron] Troll,
		// not the Free Peoples player's [orc] Troll; each of those the pool of 12 could pay for (5, 11 and 9 with roaming).
		assertTrue(scn.ShadowHasCardChoiceAvailable(olog, grunt));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halftroll, hilltroll, pittroll));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(olog);

		assertInZone(Zone.SHADOW_CHARACTERS, olog);
		assertInDiscard(grunt, halftroll, hilltroll, pittroll);

		// Vitality 2 with 1 wound: it cannot exert again, though the Ordnance Grunt is still there and can be paid for.
		scn.SetTwilight(12);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(informant));
		assertEquals(1, scn.GetWoundsOn(informant));
		assertInDiscard(grunt);
		// The other copy, unwounded, still can.
		assertTrue(scn.ShadowActionAvailable(informant2));
		assertEquals(0, scn.GetWoundsOn(informant2));
	}

	@Test
	public void IsNotOfferedWhenThePoolIsOneShortOfTheTroll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var informant = scn.GetShadowCard("informant");
		var olog = scn.GetShadowCard("olog");

		scn.StartGame();
		scn.MoveCardsToDiscard(olog);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(informant);
		// 6 for the Olog-hai + 2 roaming at site 2 (its site is 5) = 8; one short.
		scn.SetTwilight(7);
		assertInDiscard(olog);
		scn.FreepsPass();

		// The play is a cost (Q-0062): with nothing the pool can pay for, the ability is not offered.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(informant));

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(informant));
		assertEquals(0, scn.GetWoundsOn(informant));
	}

	@Test
	public void IsNotOfferedWithOnlyAHalfTrollAndASauronTrollInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var informant = scn.GetShadowCard("informant");
		var halftroll = scn.GetShadowCard("halftroll");
		var hilltroll = scn.GetShadowCard("hilltroll");
		var pittroll = scn.GetFreepsCard("pittroll");

		scn.StartGame();
		scn.MoveCardsToDiscard(halftroll, hilltroll, pittroll);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(informant);
		// Enough for each of them: 3 + 2 roaming, 9 + 2 roaming, 7 + 2 roaming.
		scn.SetTwilight(12);
		assertInDiscard(halftroll, hilltroll, pittroll);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(informant));
		assertEquals(0, scn.GetWoundsOn(informant));
	}
}
