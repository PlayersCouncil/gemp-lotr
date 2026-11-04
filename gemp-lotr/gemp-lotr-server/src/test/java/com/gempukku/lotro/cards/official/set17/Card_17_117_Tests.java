package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_117_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "17_117");
					put("butcher", "17_122");   // White Hand Butcher: [uruk-hai] Uruk-hai, hunter 6 and nothing else
					put("tracking", "15_171");  // Tracking Uruk: [uruk-hai] Uruk-hai, hunter 1 and nothing else
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard; companions resistance -1,
					// and a skirmish ability used only on purpose
					put("saruman", "17_116");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1
					put("party", "1_158");      // Uruk-hai Raiding Party: an [isengard] Uruk-hai, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpearoftheWhiteHandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Spear of the White Hand
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +1
		 * Vitality: +1
		 * Game Text: Bearer must be an [uruk-hai] Uruk-hai.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spear");

		assertEquals("Spear of the White Hand", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void MayBePlayedOnlyOnAnUrukHaiCultureUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var butcher = scn.GetShadowCard("butcher");
		var tracking = scn.GetShadowCard("tracking");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToHand(spear);
		assertInHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, guard, party);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertInHand(spear);
		// An [uruk-hai] minion who is a Wizard, a [sauron] Uruk-hai and an [isengard] Uruk-hai.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(spear));

		scn.MoveMinionsToTable(butcher, tracking);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(spear));

		scn.ShadowPlayCard(spear);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(butcher, tracking));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, guard, party));
		scn.ShadowChooseCard(tracking);
		assertInZone(Zone.ATTACHED, spear);
		assertSame(tracking, spear.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
