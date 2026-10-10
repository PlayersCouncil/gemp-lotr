package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_166_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_166");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow", "12_166");
					// Sam, Son of Hamfast: a companion who bears no Lingering Shadow; his fellowship ability is not used
					put("sam", "1_311");
					// The Gaffer, Sam's Father: ally, vitality 2.  "Fellowship: Exert The Gaffer to heal Frodo or Sam."
					put("gaffer", "1_291");
					// Rosie Cotton, Hobbiton Lass: ally, vitality 2.  "Fellowship: Exert Rosie Cotton to heal Sam."
					// (Sam is strength +1 beside her, which no test reads.)
					put("rosie", "1_309");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LingeringShadowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Lingering Shadow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Transfer this condition from your support area to a character skirmishing a Nazgûl.<br>Each time a wound is removed from bearer, add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Lingering Shadow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AWoundRemovedFromBearerAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gaffer = scn.GetFreepsCard("gaffer");
		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(gaffer, rosie);

		// The Shadow player's condition, on Frodo as it would be after his skirmish with a Nazgûl.
		var shadow = scn.GetShadowCard("shadow");
		scn.AttachCardsTo(frodo, shadow);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 1);

		assertAttachedTo(shadow, frodo);
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));

		// The Gaffer heals Frodo or Sam, and both are wounded: Frodo, the bearer, is chosen.
		scn.FreepsUseCardAction(gaffer);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);

		// One wound is gone from the bearer, and one burden has come for it.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(sam));

		// A wound removed from Sam, who bears no Lingering Shadow, adds none.
		scn.FreepsUseCardAction(rosie);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
