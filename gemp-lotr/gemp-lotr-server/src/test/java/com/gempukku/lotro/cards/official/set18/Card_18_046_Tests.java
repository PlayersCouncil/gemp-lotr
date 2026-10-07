package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_46");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man who is no ranger, vitality 3
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc with no text
					put("troop2", "1_177");
					put("scimitar1", "1_180");  // Goblin Scimitar: a Shadow possession for a [moria] Orc
					put("scimitar2", "1_180");
					put("scimitar3", "1_180");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DisarmedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Disarmed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Strength: -1
		 * Game Text: Bearer cannot bear any Shadow cards.<br>When this condition is transferred to bearer, discard all Shadow cards it bears.<br><b>Maneuver:</b> Exert a [gondor] Man or spot a ranger to transfer this condition from your support area to a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Disarmed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getStrength());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void DisarmedTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void AHinderedShadowCardTheMinionBearsIsDiscardedWithTheOthersWhenThisIsTransferredToIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(card);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var hindered = scn.GetShadowCard("scimitar1");
		var unhindered = scn.GetShadowCard("scimitar2");
		var elsewhere = scn.GetShadowCard("scimitar3");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.AttachCardsTo(troop1, hindered, unhindered);
		scn.AttachCardsTo(troop2, elsewhere);
		// A hindered card has no type or culture, and is a Shadow card still (CR 5.0 "hinder"; Ketura on Q-0098:
		// "all Shadow cards" reaches it).
		scn.HinderCard(hindered);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsHindered(hindered));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(card));

		// No ranger can be spotted, so Boromir exerts.
		scn.FreepsUseCardAction(card);
		scn.FreepsChooseCard(troop1);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertAttachedTo(card, troop1);
		// Both Shadow cards the minion bore are discarded, the hindered one with the other; the other minion's
		// stays.
		assertInZone(Zone.DISCARD, hindered, unhindered);
		assertAttachedTo(elsewhere, troop2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
