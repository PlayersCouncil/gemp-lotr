package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "4_26");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					// Easterling Axeman: a [raider] Man, strength 6; strength +3 and fierce only at 2 burdens.
					put("axeman", "4_224");
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man.  His trigger answers the
					// fellowship's move, so he is laid after it; his skirmish ability is used only on purpose.
					put("saruman", "4_33");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IronAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Iron Axe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +3
		 * Game Text: Bearer must be a [dunland] Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Iron Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnADunlandMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(axe);
		assertInHand(axe);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPass();
		// Laid once the fellowship has moved, so that Saruman's move trigger has nothing to answer.
		scn.MoveMinionsToTable(brigand, savage, axeman, saruman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(axe));

		scn.ShadowPlayCard(axe);
		// [dunland] Men of two titles; not a Man of another culture, nor a [dunland] minion who is no Man.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(savage);
		assertAttachedTo(axe, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
