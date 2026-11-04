package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_132_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smith1", "11_132");
					put("smith2", "11_132");
					put("denizen", "11_116");      // Denizen of Moria: an [orc] Orc, cost 2; stronger at an underground site and nothing else

					// [orc] conditions for the discard pile. None is ever in play here, so their texts do nothing.
					put("spurred", "11_139");      // Spurred to Battle: support area, cost 1, not unique
					put("hordes", "11_123");       // Goblin Hordes: support area, cost 2, not unique
					put("blow", "15_115");         // Pummeling Blow: unique, cost 0, plays on the fellowship's current site
					put("retribution", "12_101");  // Retribution: unique, support area, cost 0

					// Cards that are nearly [orc] conditions in a discard pile.
					put("scimitar", "11_128");     // Mordor Scimitar: an [orc] possession
					put("halls", "11_112");        // Conquered Halls: an [orc] event
					put("wings", "11_208");        // Dark Wings: a [wraith] support-area condition

					// [orc] conditions that lie somewhere else.
					put("effort", "15_106");       // Coordinated Effort: adds (2) each time the Free Peoples player plays a possession; none is played here
					put("demoralized", "11_114");  // Demoralized: stays in hand
					put("cost", "12_88");          // Great Cost: lies in the other player's discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishSmithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Orkish Smith
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may take an [orc] condition from your discard pile into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smith1");

		assertEquals("Orkish Smith", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ItsOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smith = scn.GetShadowCard("smith1");
		var spurred = scn.GetShadowCard("spurred");
		scn.MoveCardsToHand(smith);
		scn.MoveCardsToDiscard(spurred);
		assertInDiscard(spurred);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(smith);

		assertInZone(Zone.SHADOW_CHARACTERS, smith);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(smith));
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(spurred);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedItMayTakeAnOrcConditionFromTheDiscardPileIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smith = scn.GetShadowCard("smith1");
		var discardedSmith = scn.GetShadowCard("smith2");
		var spurred = scn.GetShadowCard("spurred");
		var hordes = scn.GetShadowCard("hordes");
		var blow = scn.GetShadowCard("blow");
		var retribution = scn.GetShadowCard("retribution");
		var scimitar = scn.GetShadowCard("scimitar");
		var halls = scn.GetShadowCard("halls");
		var wings = scn.GetShadowCard("wings");
		var effort = scn.GetShadowCard("effort");
		var demoralized = scn.GetShadowCard("demoralized");
		var opponentsCost = scn.GetFreepsCard("cost");
		// An [orc] condition in play, one in hand, and one in the other player's discard pile.
		scn.MoveCardsToSupportArea(effort);
		scn.MoveCardsToHand(smith, demoralized);
		scn.MoveCardsToDiscard(opponentsCost);
		// Four [orc] conditions, unique and not, of the support area and of a site; and an [orc] minion, an [orc]
		// possession, an [orc] event and a [wraith] condition.
		scn.MoveCardsToDiscard(spurred, hordes, blow, retribution, discardedSmith, scimitar, halls, wings);
		assertInZone(Zone.SUPPORT, effort);
		assertInZone(Zone.HAND, demoralized);
		assertInDiscard(opponentsCost, spurred, hordes, blow, retribution, discardedSmith, scimitar, halls, wings);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(smith);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(smith));
		scn.ShadowChooseOptionalTrigger(smith);

		assertTrue(scn.ShadowHasCardChoiceAvailable(spurred, hordes, blow, retribution));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(discardedSmith, scimitar, halls, wings, effort, demoralized, opponentsCost));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		// The one that plays on a site
		scn.ShadowChooseCard(blow);

		assertInHand(blow);
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(spurred, hordes, retribution, discardedSmith, scimitar, halls, wings);
		assertInZone(Zone.SUPPORT, effort);
		assertInZone(Zone.HAND, demoralized);
		assertInDiscard(opponentsCost);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOrcConditionInTheDiscardPileNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smith = scn.GetShadowCard("smith1");
		var discardedSmith = scn.GetShadowCard("smith2");
		var scimitar = scn.GetShadowCard("scimitar");
		var wings = scn.GetShadowCard("wings");
		var effort = scn.GetShadowCard("effort");
		var demoralized = scn.GetShadowCard("demoralized");
		var opponentsCost = scn.GetFreepsCard("cost");
		// An [orc] condition in play, one in hand and one in the other player's discard pile; in his own discard
		// pile an [orc] minion, an [orc] possession and a [wraith] condition.
		scn.MoveCardsToSupportArea(effort);
		scn.MoveCardsToHand(smith, demoralized);
		scn.MoveCardsToDiscard(discardedSmith, scimitar, wings, opponentsCost);
		assertInZone(Zone.SUPPORT, effort);
		assertInZone(Zone.HAND, demoralized);
		assertInDiscard(discardedSmith, scimitar, wings, opponentsCost);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(smith);

		assertInZone(Zone.SHADOW_CHARACTERS, smith);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(smith));
		assertInDiscard(discardedSmith, scimitar, wings, opponentsCost);
		assertInZone(Zone.HAND, demoralized);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherOrcMinionPlayedMakesNoOfferAndASecondSmithsOfferIsItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smith = scn.GetShadowCard("smith1");
		var earlierSmith = scn.GetShadowCard("smith2");
		var denizen = scn.GetShadowCard("denizen");
		var spurred = scn.GetShadowCard("spurred");
		var hordes = scn.GetShadowCard("hordes");
		scn.MoveCardsToHand(smith, denizen);
		scn.MoveCardsToDiscard(spurred, hordes);
		assertInDiscard(spurred, hordes);

		scn.StartGame();
		scn.MoveMinionsToTable(earlierSmith);
		assertInZone(Zone.SHADOW_CHARACTERS, earlierSmith);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		// Another [orc] Orc is played while a Smith is in play.
		scn.ShadowPlayCard(denizen);

		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(spurred, hordes);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Smith is played: the copy played makes the offer and the copy in play makes none.
		scn.ShadowPlayCard(smith);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(smith));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(earlierSmith));

		scn.ShadowChooseOptionalTrigger(smith);
		assertTrue(scn.ShadowHasCardChoiceAvailable(spurred, hordes));
		scn.ShadowChooseCard(spurred);

		assertInHand(spurred);
		assertInDiscard(hordes);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
