package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_216_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_216");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "1_216");
					// Blade Tip: [wraith] condition, cost 0.  Its own response moves it from the support area to a
					// character who loses to a Nazgûl, which no test here reaches.
					put("tip", "1_209");
					// Úlairë Nelya, Ringwraith in Twilight: Nazgûl, strength 10; fierce and damage +1 only at 2
					// burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					put("savage", "1_151");     // Uruk Savage: a minion for the companion Nelya is not fighting

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the companion Nelya fights
					put("gimli", "1_13");       // Gimli, Son of Glóin: assigned to the Savage, his skirmish not yet begun
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Morgul Blade
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be a Nazgûl.<br>This weapon may be borne in addition to 1 other hand weapon.<br><b>Skirmish:</b> Discard this possession to transfer Blade Tip from your support area or discard pile to a companion bearer is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Morgul Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void DiscardedToTransferBladeTipFromTheSupportAreaToTheCompanionItsBearerIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var blade = scn.GetShadowCard("blade");
		var tip = scn.GetShadowCard("tip");
		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(tip);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);
		scn.AttachCardsTo(nelya, blade);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, nelya }, new PhysicalCardImpl[] { gimli, savage });
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 3 companions + 2 for Ettenmoors
		assertEquals(5, scn.GetTwilight());
		assertAttachedTo(blade, nelya);
		assertInZone(Zone.SUPPORT, tip);
		assertTrue(scn.ShadowActionAvailable(blade));

		scn.ShadowUseCardAction(blade);
		// Aragorn is the one companion Nelya is skirmishing, so nothing is asked.  Not Gimli, who is assigned
		// to another minion, and not the Ring-bearer, who is assigned to none.
		assertInDiscard(blade);
		assertAttachedTo(tip, aragorn);
		assertEquals(0, scn.GetAttachedCards(gimli).size());
		assertNotAttachedTo(tip, frodo);
		// A transfer is not a play: it costs the number printed on the card, and Blade Tip's is 0.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DiscardedToTransferBladeTipFromTheDiscardPileToTheCompanionItsBearerIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var blade = scn.GetShadowCard("blade");
		var tip = scn.GetShadowCard("tip");
		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToDiscard(tip);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);
		scn.AttachCardsTo(nelya, blade);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, nelya }, new PhysicalCardImpl[] { gimli, savage });
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 3 companions + 2 for Ettenmoors
		assertEquals(5, scn.GetTwilight());
		assertAttachedTo(blade, nelya);
		assertInDiscard(tip);
		assertTrue(scn.ShadowActionAvailable(blade));

		scn.ShadowUseCardAction(blade);
		// The Blade has gone to the discard pile and Blade Tip has come out of it, onto the one companion
		// Nelya is skirmishing.
		assertInDiscard(blade);
		assertAttachedTo(tip, aragorn);
		assertEquals(0, scn.GetAttachedCards(gimli).size());
		assertNotAttachedTo(tip, frodo);
		// A transfer is not a play: it costs the number printed on the card, and Blade Tip's is 0.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
