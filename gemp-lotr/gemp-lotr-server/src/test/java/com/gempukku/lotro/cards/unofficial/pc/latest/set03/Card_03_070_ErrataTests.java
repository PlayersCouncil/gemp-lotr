package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_070_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servants", "53_70");
					put("goblinman", "2_42");  // Goblin Man (Isengard Orc, STR 6, VIT 2)
					put("lurtz", "1_127");     // Lurtz (Isengard Uruk-hai, STR 13, VIT 3)
					put("guard", "1_7");       // Dwarf Guard (companion, STR 4, VIT 2)
					put("runner", "1_178");    // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ServantsToSarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Servants to Saruman
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an [isengard] Orc strength +2 (or +4 if that Orc is strength 7 or less).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("servants");

		assertEquals("Servants to Saruman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ServantsMakesLowStrengthOrcPlusFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var goblinman = scn.GetShadowCard("goblinman");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(goblinman);
		scn.MoveCardsToHand(servants);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, goblinman);

		// Goblin Man base STR 6 (7 or less), should get +4
		assertEquals(6, scn.GetStrength(goblinman));

		// Freeps passes, Shadow plays event
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(servants);
		// Goblin Man auto-selected as only Isengard Orc
		assertEquals(10, scn.GetStrength(goblinman));
	}

	@Test
	public void ServantsMakesHighStrengthOrcPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var lurtz = scn.GetShadowCard("lurtz");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(lurtz);
		scn.MoveCardsToHand(servants);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, lurtz);

		// Lurtz base STR 13 (greater than 7), should get +2
		assertEquals(13, scn.GetStrength(lurtz));

		// Freeps passes, Shadow plays event
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(servants);
		// Lurtz auto-selected as only Isengard Orc/Uruk-hai
		assertEquals(15, scn.GetStrength(lurtz));
	}
}
