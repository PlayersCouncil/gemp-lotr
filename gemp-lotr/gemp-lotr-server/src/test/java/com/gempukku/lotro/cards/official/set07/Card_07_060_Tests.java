package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("heavy", "7_60");
					// Gollum, Nasty Treacherous Creature: the [gollum] Gollum; "Regroup: Discard Gollum to exert a
					// companion or ally"
					put("gollum", "5_24");
					// Gollum, Half a Wraith Himself: the [wraith] Gollum; his texts answer a Nazgûl played and the
					// assignment phase
					put("wraithgollum", "103_66");
					// Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum; enduring, fierce
					put("shelob", "8_26");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HeavyBurdenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Heavy Burden
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Regroup:</b> Discard Gollum and remove a threat to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("heavy");

		assertEquals("Heavy Burden", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityDiscardsGollumAndRemovesAThreatToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heavy = scn.GetShadowCard("heavy");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToSupportArea(heavy);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());

		// "Regroup:" is not the maneuver phase, though both costs can be paid there.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(heavy));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, gollum, shelob);
		assertTrue(scn.ShadowActionAvailable(heavy));
		scn.ShadowUseCardAction(heavy);

		// Gollum is the one card of his title, and goes without a question; Shelob stays.
		assertInZone(Zone.DISCARD, gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheWraithGollumPaysTheDiscardToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heavy = scn.GetShadowCard("heavy");
		var gollum = scn.GetShadowCard("wraithgollum");
		scn.MoveCardsToSupportArea(heavy);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(heavy));
		scn.ShadowUseCardAction(heavy);

		assertInZone(Zone.DISCARD, gollum);
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithoutGollumOrWithoutAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heavy = scn.GetShadowCard("heavy");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToSupportArea(heavy);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// A threat, and a [gollum] minion who is no Gollum.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(heavy));

		// Gollum, and no threat.
		scn.MoveMinionsToTable(gollum);
		scn.RemoveThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(heavy));

		// Both: offered.
		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(heavy));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum, shelob);
		assertEquals(0, scn.GetBurdens());
	}
}
