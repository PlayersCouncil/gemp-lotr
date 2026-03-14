package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_072_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trapped", "53_72");
					put("uruk", "1_151");      // Uruk Lieutenant (Isengard Uruk-hai)
					put("goblinman", "2_42");  // Goblin Man (Isengard Orc)
					put("guard", "1_7");       // Dwarf Guard (companion)
					put("runner", "1_178");    // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrappedAndAloneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Trapped and Alone
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each character skirmishing an [isengard] Orc loses all <b>damage</b> bonuses from weapons.
		 * 	Skirmish: Spot a skirmishing [Isengard] Orc.  Make the Free Peoples shuffle another
		 *  [Isengard] Orc into your draw deck to cancel the active skirmish.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trapped");

		assertEquals("Trapped and Alone", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishAbilityCancelsSkirmishByShufflingOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapped = scn.GetShadowCard("trapped");
		var uruk = scn.GetShadowCard("uruk");
		var goblinman = scn.GetShadowCard("goblinman");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCardsToSupportArea(trapped);
		scn.MoveMinionsToTable(uruk, goblinman);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, uruk);

		// Uruk is in a skirmish with Guard, Goblin Man is not in a skirmish
		// Shadow uses Trapped and Alone ability during skirmish
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(trapped));
		scn.ShadowUseCardAction(trapped);

		// Freeps must choose another Isengard Orc to shuffle (Goblin Man)
		scn.FreepsChooseCard(goblinman);

		// Goblin Man should have been shuffled into Shadow's draw deck
		// Skirmish should be cancelled -- Guard should survive unharmed
		assertEquals(0, scn.GetWoundsOn(guard));
	}
}
