package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("otsea", "8_81");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireOtseaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Úlairë Otsëa, Thrall of The One
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Enduring</b>. <b>Fierce</b>.<br><b>Regroup:</b> Discard Úlairë Otsëa to add (X), where X is the current site number.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("otsea");

		assertEquals("Úlairë Otsëa", card.getBlueprint().getTitle());
		assertEquals("Thrall of The One", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityDiscardsHerToAddTwoAtSite2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");

		scn.StartGame();
		// No minions on the way, so no skirmish: she is laid in the regroup phase, as if she had lived through hers.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(otsea);
		scn.SetTwilight(3);
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertEquals(3, scn.GetTwilight());
		scn.FreepsPass();

		// The fellowship is at site 2; the Shadow player's marker is still at site 1, and her own site number is 3.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(otsea));
		scn.ShadowUseCardAction(otsea);

		assertInDiscard(otsea);
		// 3 + 2 for site 2
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtSite4TheRegroupAbilityAddsFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(otsea);
		scn.SetTwilight(3);
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertEquals(3, scn.GetTwilight());
		scn.FreepsPass();

		// Site 4 is Balin's Tomb, Shadow number 3; the Shadow player's marker is at site 3, her own site number.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		assertTrue(scn.ShadowActionAvailable(otsea));
		scn.ShadowUseCardAction(otsea);

		assertInDiscard(otsea);
		// 3 + 4 for site 4
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(otsea));
	}
}
