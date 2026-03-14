package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_157_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armory", "51_157");
					put("uruk", "1_151");    // Uruk Lieutenant
					put("possession", "1_125"); // Lurtz's Sword (Isengard possession)
					put("runner", "1_178");
					put("guard", "1_7");     // Dwarf Guard (for archery)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiArmoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk-hai Armory
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot an Uruk-hai, the fellowship archery total is -1.
		 * <br><b>Shadow:</b> Discard this condition to play an [Isengard] possession from your discard pile.
		 * <br><b>Response:</b> If your [Isengard] minion is about to take a wound, remove (1) and
		 * discard this condition to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armory");

		assertEquals("Uruk-hai Armory", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ArmoryReducesFellowshipArcheryAndShadowAbilityPlaysIsengardPossession() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory");
		var uruk = scn.GetShadowCard("uruk");
		var possession = scn.GetShadowCard("possession");

		scn.MoveCardsToSupportArea(armory);
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToDiscard(possession);

		scn.StartGame();

		// With Uruk-hai on the table, fellowship archery should be reduced by 1
		assertEquals(-1, scn.GetArcheryTotal(Side.FREE_PEOPLE));

		scn.SkipToPhase(Phase.SHADOW);

		// Shadow ability: discard armory to play an Isengard possession from discard
		assertTrue(scn.ShadowActionAvailable(armory));
		scn.ShadowUseCardAction(armory);

		// Armory discarded, Lurtz's Sword auto-selected and played from discard
		assertInDiscard(armory);
	}
}
