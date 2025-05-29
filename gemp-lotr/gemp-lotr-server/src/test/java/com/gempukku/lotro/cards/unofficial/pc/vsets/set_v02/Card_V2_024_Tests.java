package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hound", "102_24");
					put("isenorc", "3_58");
					put("wargrider", "5_50");
					put("voices", "5_68");
					put("power", "1_136");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HoundofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Hound of Isengard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 1
		 * Vitality: 1
		 * Game Text: Bearer must be a warg-rider.
		 * When you play this possession, you may take an [isengard] skirmish event into hand from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hound");

		assertEquals("Hound of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void HoundofIsengardGoesOnWargRider() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hound = scn.GetShadowCard("hound");
		var isenorc = scn.GetShadowCard("isenorc");
		var wargrider = scn.GetShadowCard("wargrider");
		scn.MoveMinionsToTable(isenorc, wargrider);
		scn.MoveCardsToHand(hound);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(hound));
		scn.ShadowPlayCard(hound);
		assertEquals(Zone.ATTACHED, hound.getZone());
		//Hound should auto attach to the only valid target
		assertEquals(wargrider, hound.getAttachedTo());
	}

	@Test
	public void HoundofIsengardGPullsSkirmishEventFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hound = scn.GetShadowCard("hound");
		var wargrider = scn.GetShadowCard("wargrider");
		var voices = scn.GetShadowCard("voices");
		var power = scn.GetShadowCard("power");
		scn.MoveMinionsToTable(wargrider);
		scn.MoveCardsToHand(hound);
		scn.MoveCardsToDiscard(voices, power);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.DISCARD, voices.getZone());
		assertEquals(Zone.DISCARD, power.getZone());
		assertTrue(scn.ShadowPlayAvailable(hound));
		scn.ShadowPlayCard(hound);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		//The only available skirmish event should get taken.  Saruman's Power is a Shadow event.
		assertEquals(Zone.HAND, voices.getZone());
		assertEquals(Zone.DISCARD, power.getZone());
	}
}
