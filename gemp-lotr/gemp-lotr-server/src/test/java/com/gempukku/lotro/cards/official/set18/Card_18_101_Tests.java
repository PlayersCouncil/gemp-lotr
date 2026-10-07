package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_101");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("spear", "4_288");      // Rider's Spear: a [rohan] possession, put on Aragorn by the rig
					put("poison1", "8_28");     // Spider Poison: a Shadow card that a companion bears
					put("poison2", "8_28");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PreciseAttackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Precise Attack
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Discard a [rohan] possession from play to choose one: wound an unwounded minion; exert a minion; or discard a Shadow card the Ring-bearer is bearing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Precise Attack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void PreciseAttackTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void AHinderedShadowCardTheRingBearerBearsMayBeTheOneDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var card = scn.GetFreepsCard("card");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, spear);
		scn.MoveCardsToHand(card);

		var hindered = scn.GetShadowCard("poison1");
		var unhindered = scn.GetShadowCard("poison2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(frodo, hindered, unhindered);
		// A hindered card has no type or culture, and is a Shadow card still (CR 5.0 "hinder"; Ketura on Q-0098:
		// "a Shadow card" may be a hindered one).
		scn.HinderCard(hindered);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.IsHindered(hindered));
		assertTrue(scn.FreepsPlayAvailable(card));

		// The Spear is the one [rohan] possession in play.
		scn.FreepsPlayCard(card);
		assertInZone(Zone.DISCARD, spear);
		scn.FreepsChoose("Discard a Shadow card");

		assertTrue(scn.FreepsHasCardChoiceAvailable(hindered, unhindered));
		scn.FreepsChooseCard(hindered);

		assertInZone(Zone.DISCARD, hindered);
		assertAttachedTo(unhindered, frodo);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
