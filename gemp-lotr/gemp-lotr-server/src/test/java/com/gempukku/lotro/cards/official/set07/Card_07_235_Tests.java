package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_235_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guthlaf", "7_235");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5, vitality 3; a skirmish ability used only on purpose
					// Well Stored: [rohan] condition in the support area; at the start of the fellowship phase it may heal a
					// villager, and there is none
					put("stored", "4_296");
					// Protecting the Hall: [rohan] condition borne by a [rohan] Man; an assignment ability used only on purpose
					put("hall", "11_149");
					put("chain", "4_278");      // Heavy Chain: [rohan] possession (armor) borne by a Man; a standing overwhelm text
					// Foes of Mordor: [gondor] condition in the support area; a maneuver ability used only on purpose
					put("foes", "1_105");
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuthlafStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Guthláf, Herald
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Skirmish:</b> Discard a [rohan] condition to make Guthláf strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guthlaf");

		assertEquals("Guthláf", card.getBlueprint().getTitle());
		assertEquals("Herald", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishAbilityDiscardsARohanConditionToMakeGuthlafStrengthPlus1UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guthlaf = scn.GetFreepsCard("guthlaf");
		var rider = scn.GetFreepsCard("rider");
		var stored = scn.GetFreepsCard("stored");
		var hall = scn.GetFreepsCard("hall");
		var chain = scn.GetFreepsCard("chain");
		var foes = scn.GetFreepsCard("foes");
		scn.MoveCompanionsToTable(guthlaf, rider);
		scn.AttachCardsTo(rider, hall);
		scn.AttachCardsTo(guthlaf, chain);
		scn.MoveCardsToSupportArea(stored, foes);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		assertAttachedTo(hall, rider);
		assertAttachedTo(chain, guthlaf);
		assertInZone(Zone.SUPPORT, stored, foes);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, savage1}, new PhysicalCardImpl[]{guthlaf, savage2});

		// The Rider's skirmish first: Guthlaf, assigned to a skirmish still to come, is not in it.
		scn.FreepsResolveSkirmish(rider);
		assertEquals(6, scn.GetStrength(guthlaf));
		assertEquals(5, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(guthlaf));
		scn.FreepsUseCardAction(guthlaf);

		// A [rohan] condition in the support area or borne; not a [rohan] possession, nor a condition of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(stored, hall));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(chain, foes));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(stored);

		assertInDiscard(stored);
		assertAttachedTo(hall, rider);
		assertAttachedTo(chain, guthlaf);
		assertInZone(Zone.SUPPORT, foes);
		assertEquals(7, scn.GetStrength(guthlaf));
		// The Rider, the other [rohan] Man in play, is not strengthened.
		assertEquals(5, scn.GetStrength(rider));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still +1 at the next decision of that skirmish; gone in the next skirmish, his own.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(guthlaf));
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(guthlaf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(guthlaf));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoRohanConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guthlaf = scn.GetFreepsCard("guthlaf");
		var chain = scn.GetFreepsCard("chain");
		var foes = scn.GetFreepsCard("foes");
		scn.MoveCompanionsToTable(guthlaf);
		scn.AttachCardsTo(guthlaf, chain);
		scn.MoveCardsToSupportArea(foes);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		assertAttachedTo(chain, guthlaf);
		assertInZone(Zone.SUPPORT, foes);
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guthlaf, savage1);
		scn.FreepsResolveSkirmish(guthlaf);

		// A [rohan] possession and a [gondor] condition only: nothing pays the cost.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(guthlaf));
		assertEquals(6, scn.GetStrength(guthlaf));
	}
}
