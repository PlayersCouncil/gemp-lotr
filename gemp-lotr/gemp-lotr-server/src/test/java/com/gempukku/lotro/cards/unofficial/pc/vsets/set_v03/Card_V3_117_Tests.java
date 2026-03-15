package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_117_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "103_117");
					put("sam", "1_311");
					put("runner", "1_178");
					put("companion1", "1_89");
					put("companion2", "1_89");
					put("companion3", "1_89");
					put("march1", "103_22");       // Ethereal March - raises threat limit +2
					put("march2", "103_22");
					put("wraith1", "103_32");      // Tormented Revenant - Wraith companion
					put("wraith2", "103_32");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sting, Sharp Elven-blade
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Resistance: 1
		 * Game Text: Bearer must be a Ring-bound Hobbit.
		 * 	Skirmish: If you cannot spot more than 4 companions, add a threat to make bearer
		 * 	strength +1 for each threat you can spot (limit +6).
		 * 	Response: If The One Ring is transferred, play this on the new Ring-bearer.
		 * 	You may use this ability from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sting");

		assertEquals("Sting", card.getBlueprint().getTitle());
		assertEquals("Sharp Elven-blade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getResistance());
	}

// ======== SKIRMISH ABILITY ========

	@Test
	public void StingSkirmishAbilityAddsStrengthPerThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var comp1 = scn.GetFreepsCard("companion1");
		var comp2 = scn.GetFreepsCard("companion2");
		var runner = scn.GetShadowCard("runner");

		scn.AttachCardsTo(frodo, sting);
		scn.MoveCompanionsToTable(sam, comp1, comp2);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Frodo + Sam + 2 others = 4 companions (boundary: ability requires <=4)
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());

		// Frodo base 3 + Ruling Ring 1 + Sting 1 = 5
		assertEquals(5, scn.GetStrength(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		assertTrue(scn.FreepsActionAvailable(sting));
		scn.FreepsUseCardAction(sting);

		// Added 1 threat (now 3). +3 per ForEachThreat.
		// Frodo: 5 + 3 = 8
		assertEquals(3, scn.GetThreats());
		assertEquals(8, scn.GetStrength(frodo));
	}

	@Test
	public void StingStrengthBonusCapsAtPlusSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var wraith1 = scn.GetFreepsCard("wraith1");
		var wraith2 = scn.GetFreepsCard("wraith2");
		var march1 = scn.GetShadowCard("march1");
		var march2 = scn.GetShadowCard("march2");
		var runner = scn.GetShadowCard("runner");

		scn.AttachCardsTo(frodo, sting);
		scn.MoveCompanionsToTable(wraith1, wraith2);
		scn.MoveCardsToSupportArea(march1, march2);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Frodo + 2 Wraiths = 3 companions. Threat limit: 3 base + 2 + 2 = 7.
		scn.AddThreats(6);
		assertEquals(6, scn.GetThreats());

		// Frodo base 3 + Ruling Ring 1 + Sting 1 = 5 before using ability
		assertEquals(5, scn.GetStrength(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		// Use Sting's ability — adds 1 threat (now 7, at threat limit)
		scn.FreepsUseCardAction(sting);

		// 7 threats exist, but strength limit is +6
		// Frodo: 5 + 6 = 11
		assertEquals(7, scn.GetThreats());
		assertEquals(11, scn.GetStrength(frodo));
	}

	@Test
	public void StingAbilityNotAvailableWithFiveOrMoreCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var comp1 = scn.GetFreepsCard("companion1");
		var comp2 = scn.GetFreepsCard("companion2");
		var comp3 = scn.GetFreepsCard("companion3");
		var runner = scn.GetShadowCard("runner");

		scn.AttachCardsTo(frodo, sting);
		scn.MoveCompanionsToTable(sam, comp1, comp2, comp3);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Frodo + Sam + 3 others = 5 companions
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		// Ability requires CantSpot 5 companions — should be unavailable
		assertFalse(scn.FreepsActionAvailable(sting));
	}

// ======== RESPONSE: RING-BEARER TRANSFER ========

	@Test
	public void StingTransfersToNewRingBearerFromPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, sting);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.AddWoundsToChar(frodo, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);
		scn.FreepsPass();
		scn.ShadowPass();

		// The One Ring offering to convert wound to burden
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(sam, ring.getAttachedTo());

		// Sting went to discard when Frodo died
		assertInZone(Zone.DISCARD, sting);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.ATTACHED, sting);
		assertEquals(sam, sting.getAttachedTo());
	}

	@Test
	public void StingPlaysOnNewRingBearerFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(sting);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.AddWoundsToChar(frodo, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);
		scn.FreepsPass();
		scn.ShadowPass();

		// The One Ring offering to convert wound to burden
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(sam, ring.getAttachedTo());

		assertInZone(Zone.DISCARD, sting);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.ATTACHED, sting);
		assertEquals(sam, sting.getAttachedTo());
	}
}
