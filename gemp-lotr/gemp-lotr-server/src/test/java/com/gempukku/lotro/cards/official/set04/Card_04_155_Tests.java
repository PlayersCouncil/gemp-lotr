package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_155_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_155");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haunting", "4_155");
					put("grima", "4_154");      // Gríma, Wormtongue: [isengard] Man, vitality 3; a maneuver ability only
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc with no text, vitality 3
					put("rumil", "1_57");       // Rúmil, Elven Protector: ally, vitality 3, "Archery: Exert Rúmil to wound an Orc."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HauntingHerStepsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Haunting Her Steps
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If a special ability of an ally or unbound companion is used, exert Gríma to cancel that action.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Haunting Her Steps", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseExertsGrimaToCancelASpecialAbilityOfAnAllyAndItsCostStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var haunting = scn.GetShadowCard("haunting");
		var grima = scn.GetShadowCard("grima");
		var troop = scn.GetShadowCard("troop");
		var rumil = scn.GetFreepsCard("rumil");
		scn.MoveAlliesToTable(rumil);
		scn.MoveCardsToHand(haunting);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(rumil));

		// The Troop is the only Orc, so Rúmil's ability would wound it with nothing asked.
		scn.FreepsUseCardAction(rumil);
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertTrue(scn.ShadowPlayAvailable(haunting));

		scn.ShadowPlayCard(haunting);
		assertEquals(1, scn.GetWoundsOn(grima));
		assertInZone(Zone.DISCARD, haunting);
		// The ability is cancelled: no wound.  Its cost stays paid.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
