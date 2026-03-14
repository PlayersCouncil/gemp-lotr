package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_03_072_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trapped", "53_72");
					put("smith", "3_60");      // Isengard Smith
					put("warrior", "3_61");  // Isengard Warrior
					put("aragorn", "1_89");
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
	public void TrappedAndAloneSkirmishAbilityCancelsSkirmishByShufflingOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapped = scn.GetShadowCard("trapped");
		var smith = scn.GetShadowCard("smith");
		var warrior = scn.GetShadowCard("warrior");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(trapped);
		scn.MoveMinionsToTable(smith, warrior);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, warrior);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Warrior is in a skirmish with Aragorn, Smith is not in a skirmish
		// Shadow uses Trapped and Alone ability during skirmish
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(trapped));
		scn.ShadowUseCardAction(trapped);

		// Freeps must choose another Isengard Orc to shuffle (Smith is the only option)
		// Smith should have been shuffled into Shadow's draw deck
		// Skirmish should be cancelled -- Warrior should survive unharmed
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertInZone(Zone.DECK,  smith);
	}
}
