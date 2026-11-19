package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_097_Tests
{

	// The Great Ring 19_1: "Skirmish: Add a burden to make the Ring-bearer strength +3", a burden the Free Peoples
	// player adds; its other texts work only while it is worn, and it is never put on here.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mine", "10_97");

					put("miscreant", "11_131"); // Orc Miscreant: [orc] Orc, vitality 3; "Regroup: Exert this minion to add a burden"
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; Damage +1 only
					put("towers", "7_281");     // Great Siege-towers: [sauron] condition; abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void TheRingisMineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: The Ring is Mine!
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a burden is added, spot a [sauron] minion to add an additional burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mine");

		assertEquals("The Ring is Mine!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenAShadowCardAddsABurdenSpottingASauronOrcAddsAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mine = scn.GetShadowCard("mine");
		var miscreant = scn.GetShadowCard("miscreant");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(mine);
		assertInHand(mine);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant, chieftain);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetBurdens());
		assertInHand(mine);
		assertFalse(scn.ShadowPlayAvailable(mine));

		scn.ShadowUseCardAction(miscreant);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(mine));

		scn.ShadowPlayCard(mine);
		assertEquals(2, scn.GetBurdens());
		assertInDiscard(mine);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheFreePeoplesPlayerAddsABurdenSpottingASauronUrukHaiAddsAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();

		var mine = scn.GetShadowCard("mine");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(mine);
		assertInHand(mine);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, guard);
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(0, scn.GetBurdens());
		assertInHand(mine);

		scn.FreepsUseCardAction(ring);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(mine));

		scn.ShadowPlayCard(mine);
		assertEquals(2, scn.GetBurdens());
		assertInDiscard(mine);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyAnOrcOfAnotherCultureAndASauronConditionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mine = scn.GetShadowCard("mine");
		var miscreant = scn.GetShadowCard("miscreant");
		var towers = scn.GetShadowCard("towers");
		scn.MoveCardsToHand(mine);
		assertInHand(mine);
		scn.MoveCardsToSupportArea(towers);
		assertInZone(Zone.SUPPORT, towers);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetBurdens());

		// A burden is added, and the only cards to spot are an [orc] Orc and a [sauron] condition.
		scn.ShadowUseCardAction(miscreant);
		// No response window opens: the Shadow player is asked nothing, and the game goes on.
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInHand(mine);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheRingBearerTakesAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var mine = scn.GetShadowCard("mine");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(mine);
		assertInHand(mine);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		// Frodo at 3 + 2 = 5 loses to the Chieftain's 7 and is not overwhelmed.
		scn.ApplyAdHocModifier(new StrengthModifier(null, frodo, null, 2));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, chieftain);
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInHand(mine);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
