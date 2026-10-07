package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_280_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_280");
					put("towers", "7_281");     // Great Siege-towers: an engine; a [sauron] condition in the support area
					put("pinned", "1_192");     // Pinned Down: Shadow condition, support area; idle without a [moria] archer
					// Weary: Shadow condition, plays on a companion or ally; answers its bearer being assigned to an
					// [isengard] tracker and nothing else
					put("weary", "4_212");
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase

					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally; an archery ability
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; stronger against a Nazgul
					put("promise", "2_112");    // A Promise: Free Peoples condition, support area; needs Sam to do anything
					// The Tale of Gil-galad: Free Peoples condition, bearer must be an Elf; a skirmish ability
					put("tale1", "1_66");
					put("tale2", "1_66");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatPerilofFireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Great Peril of Fire
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a besieger or a site you control.<br><b>Maneuver:</b> Discard an engine to discard a condition from the Free Peoples support area.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Great Peril of Fire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GreatPerilofFireTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void ManeuverAbilityReachesAConditionOfEitherPlayersThatAnAllyBearsAndNotOneOnACompanionOrInTheShadowSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("card");
		var towers = scn.GetShadowCard("towers");
		var pinned = scn.GetShadowCard("pinned");
		var weary = scn.GetShadowCard("weary");
		var runner = scn.GetShadowCard("runner");
		var orophin = scn.GetFreepsCard("orophin");
		var arwen = scn.GetFreepsCard("arwen");
		var promise = scn.GetFreepsCard("promise");
		var tale1 = scn.GetFreepsCard("tale1");
		var tale2 = scn.GetFreepsCard("tale2");

		scn.MoveAlliesToTable(orophin);
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(promise, peril, towers, pinned);
		// In the Free Peoples support area: a condition lying there, and what an ally there bears, the Shadow
		// player's own condition included.
		scn.AttachCardsTo(orophin, tale1, weary);
		// Not in it: a Free Peoples condition on a companion, and the conditions in the Shadow player's support
		// area (Pinned Down, and this card and the engine themselves).
		scn.AttachCardsTo(arwen, tale2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(peril));
		scn.ShadowUseCardAction(peril);
		assertInDiscard(towers);
		assertTrue(scn.ShadowHasCardChoiceAvailable(promise, tale1, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(tale2, pinned, peril));
		scn.ShadowChooseCard(tale1);

		assertInDiscard(tale1);
		assertInZone(Zone.SUPPORT, promise, pinned, peril);
		assertAttachedTo(weary, orophin);
		assertAttachedTo(tale2, arwen);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
