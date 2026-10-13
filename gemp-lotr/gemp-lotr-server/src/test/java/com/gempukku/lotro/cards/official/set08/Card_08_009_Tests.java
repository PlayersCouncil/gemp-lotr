package com.gempukku.lotro.cards.official.set08;

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

public class Card_08_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_9");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetArcheryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ship", "8_9");
					put("legolas", "1_50");     // Legolas, Greenleaf: the Elf companion; an archer with an archery special ability
					put("garrison", "8_36");    // Garrison of Gondor: a [gondor] Man and an archer; his other text needs a fortification
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AGreyShipStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: A Grey Ship
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot an Elf companion and make the fellowship archery total -X (to a minimum of 0) to heal X unbound companions. You cannot use archery special abilities.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("A Grey Ship", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheXChosenComesOffTheFellowshipArcheryTotalAndThatManyUnboundCompanionsAreHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var frodo = scn.GetRingBearer();
		var ship = scn.GetFreepsCard("ship");
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(legolas, garrison, sam);
		scn.MoveCardsToHand(ship);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Two unbound companions and two Ring-bound ones are wounded; the Garrison twice, so that one heal shows.
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(garrison, 2);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Two archers: the fellowship archery total is 2.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(legolas));
		assertTrue(scn.FreepsPlayAvailable(ship));
		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(ship);

		// X is anything from 0 to the archery total.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		// One unbound companion is healed, of the player's choosing; the Ring-bound ones are not on offer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, garrison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(garrison);

		// 2 - 1 off the total; one of the Garrison's two wounds healed and nobody else's.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetWoundsOn(garrison));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		// The event's cost of 2.
		assertEquals(twilight + 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, ship);

		// "You cannot use archery special abilities": Legolas's is no longer offered when the player is next asked.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
	}
}
