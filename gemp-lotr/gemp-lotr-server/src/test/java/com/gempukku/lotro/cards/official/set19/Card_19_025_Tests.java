package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_025_Tests
{

	// Site 2 is Ettenmoors, whose skirmish ability lets either player exert his own companion or minion.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "19_25");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, valiant and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai minion, vitality 3; damage +1 is its whole text
					put("otsea", "1_235");      // Ulaire Otsea, Lieutenant of Morgul: a Nazgul minion, strength 9, vitality 3; a Shadow ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Éomer, Éored Captain
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Valiant</b>.
		 * 	Éomer is strength +2 for each wound on each minion he is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", card.getBlueprint().getTitle());
		assertEquals("Éored Captain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void HeIsStrengthPlusTwoForEachWoundOnEachMinionHeIsSkirmishingAndForNoOtherWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var otsea = scn.GetShadowCard("otsea");
		scn.MoveCompanionsToTable(eomer, guard);

		scn.StartGame();
		scn.FreepsPass();

		// At Ettenmoors, in the Shadow phase. A wound on Éomer himself is no wound on a minion.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(troop, savage, otsea);
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(savage, 2);
		scn.AddWoundsToChar(otsea, 2);
		scn.AddWoundsToChar(eomer, 1);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(7, scn.GetStrength(eomer));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eomer, troop}, new PhysicalCardImpl[]{guard, otsea});
		// The minion left over: the Shadow player puts the Uruk-hai on Éomer as well.
		scn.ShadowAssignToMinions(eomer, savage);

		// Assigned to two wounded minions, but not yet skirmishing them.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(7, scn.GetStrength(eomer));

		scn.FreepsResolveSkirmish(guard);

		// In the other skirmish: the Nazgul's two wounds are not on a minion Éomer is skirmishing, and the Household
		// Guard, skirmishing that wounded minion, keeps his own strength.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(guard));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(eomer);

		// 1 wound on the Orc and 2 on the Uruk-hai: 7 + 2 x 3.  The Nazgul, still on the table with its 2 wounds,
		// fought elsewhere, and Éomer's own wound is not counted.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(13, scn.GetStrength(eomer));

		// The Shadow player exerts the Orc by Ettenmoors: one more wound on a minion Éomer is skirmishing.  The
		// Uruk-hai and the Nazgul, at 2 wounds of 3, are exhausted, so the Orc is exerted unasked.
		scn.FreepsPass();
		scn.ShadowUseCardAction(scn.GetCurrentSite());

		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(15, scn.GetStrength(eomer));     // 7 + 2 x 4
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
