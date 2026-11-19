package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aggressor", "12_89");
					put("aggressor2", "12_89");
					put("vile", "12_106");      // Vile Goblin: [orc] Orc, strength 10; fierce only when assigned to a Dwarf
					put("grunt", "13_117");     // Ordnance Grunt: [orc] Troll, strength 12; a discard-pile ability only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("ax", "18_84");         // Orkish Ax: [orc] hand weapon, cost 1, strength +2, bearer must be an [orc] Orc
					put("gauntlets", "11_109"); // Bladed Gauntlets: [orc] armor, no strength bonus, bearer must be an [orc] minion
					put("lethim", "7_63");      // Let Her Deal With Them: [gollum] condition borne by a minion, bearer damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorAggressorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Mordor Aggressor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While this minion is bearing a possession, each [orc] minion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("aggressor");

		assertEquals("Mordor Aggressor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachOrcMinionIsStrengthPlus2OnceAPossessionIsPlayedOnHimAndNotForAnotherMinionsPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggressor = scn.GetShadowCard("aggressor");
		var aggressor2 = scn.GetShadowCard("aggressor2");
		var vile = scn.GetShadowCard("vile");
		var grunt = scn.GetShadowCard("grunt");
		var troop = scn.GetShadowCard("troop");
		var ax = scn.GetShadowCard("ax");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var lethim = scn.GetShadowCard("lethim");

		scn.StartGame();
		scn.SetShadowHand(ax);
		scn.MoveMinionsToTable(aggressor, aggressor2, vile, grunt, troop);
		// The second copy bears a condition, which is no possession; the Vile Goblin bears a possession of his own.
		scn.AttachCardsTo(aggressor2, lethim);
		scn.AttachCardsTo(vile, gauntlets);
		assertInHand(ax);
		assertInZone(Zone.ATTACHED, lethim, gauntlets);

		// Neither Aggressor bears a possession: every minion reads its printed strength.
		assertEquals(12, scn.GetStrength(aggressor));
		assertEquals(12, scn.GetStrength(aggressor2));
		assertEquals(10, scn.GetStrength(vile));
		assertEquals(12, scn.GetStrength(grunt));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(ax);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, aggressor2, vile));
		scn.ShadowChooseCard(aggressor);
		assertAttachedTo(ax, aggressor);

		// 12 + 2 for the Ax + 2
		assertEquals(16, scn.GetStrength(aggressor));
		// one bonus, from the copy that bears a possession
		assertEquals(14, scn.GetStrength(aggressor2));
		assertEquals(12, scn.GetStrength(vile));
		assertEquals(14, scn.GetStrength(grunt));
		// a [moria] Orc
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ArmorIsAPossessionAsAWeaponIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggressor = scn.GetShadowCard("aggressor");
		var vile = scn.GetShadowCard("vile");
		var gauntlets = scn.GetShadowCard("gauntlets");

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, vile);
		assertEquals(12, scn.GetStrength(aggressor));
		assertEquals(10, scn.GetStrength(vile));
		scn.AttachCardsTo(aggressor, gauntlets);
		assertAttachedTo(gauntlets, aggressor);

		assertEquals(14, scn.GetStrength(aggressor));
		assertEquals(12, scn.GetStrength(vile));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
