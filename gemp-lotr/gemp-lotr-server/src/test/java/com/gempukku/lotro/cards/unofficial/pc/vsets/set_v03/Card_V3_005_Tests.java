package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "103_5");
					put("gandalf", "1_364");
					put("narya", "3_34");
					put("lemenya", "1_232"); // Nazgul, strength 9
					put("fellbeast", "6_83"); // Mount, +2 strength, fierce
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowfaxStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Shadowfax, Swiftest of All
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be Gandalf.
		* 	While you can spot a [gandalf] artifact, minions skirmishing bearer gain no strength or keyword bonuses from Shadow cards they bear.
		* 	While bearer is unwounded or exhausted, bearer is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadowfax");

		assertEquals("Shadowfax", card.getBlueprint().getTitle());
		assertEquals("Swiftest of All", card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowfaxAttachesToGandalfAndGivesStrengthWhenUnwoundedOrExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		int gandalfBaseStrength = scn.GetStrength(gandalf);

		// Shadowfax should auto-attach to Gandalf (only valid target)
		scn.FreepsPlayCard(shadowfax);
		assertAttachedTo(shadowfax, gandalf);

		// Unwounded: gets +3
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(gandalfBaseStrength + 3, scn.GetStrength(gandalf));

		// Add 1 wound: wounded but not exhausted, no +3
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(gandalfBaseStrength, scn.GetStrength(gandalf));

		// Add another wound: still not exhausted
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(gandalfBaseStrength, scn.GetStrength(gandalf));

		// Add 3rd wound: now exhausted (vitality 4, so 3 wounds = 1 away from death)
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(gandalfBaseStrength + 3, scn.GetStrength(gandalf));
	}

	@Test
	public void ShadowfaxCancelsBonusesFromShadowCardsWithAndWithoutArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var narya = scn.GetFreepsCard("narya");
		var lemenya = scn.GetShadowCard("lemenya");
		var fellbeast = scn.GetShadowCard("fellbeast");

		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax, narya);
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, fellbeast);

		scn.StartGame();
		scn.SkipToAssignments();

		// Before assignment: Fell Beast gives +2 strength and fierce
		assertEquals(11, scn.GetStrength(lemenya)); // 9 base + 2 from Fell Beast
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));

		scn.FreepsAssignToMinions(gandalf, lemenya);
		scn.FreepsResolveSkirmish(gandalf);

		// During first skirmish (with Narya): bonuses canceled
		assertEquals(9, scn.GetStrength(lemenya)); // Fell Beast bonus canceled
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE)); // Fierce canceled

		scn.PassCurrentPhaseActions();

		// After first skirmish: bonuses return
		assertEquals(11, scn.GetStrength(lemenya));
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));

		// Discard Narya (cheat to remove artifact)
		scn.MoveCardsToDiscard(narya);

		// Return to Assignment phase for second fierce skirmish
		scn.BothPass(); // Both players pass Assignment actions
		scn.FreepsAssignToMinions(gandalf, lemenya);
		scn.FreepsResolveSkirmish(gandalf);

		// During second skirmish (without Narya): bonuses NOT canceled
		assertEquals(11, scn.GetStrength(lemenya)); // Fell Beast bonus active
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE)); // Fierce active

		scn.PassCurrentPhaseActions();

		// After second skirmish: bonuses still present
		assertEquals(11, scn.GetStrength(lemenya));
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));
	}
}
