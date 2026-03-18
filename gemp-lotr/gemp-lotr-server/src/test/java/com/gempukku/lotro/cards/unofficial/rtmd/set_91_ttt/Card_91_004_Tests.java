package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_004_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mod", "91_4");
					// Gimli, Dwarf of Erebor: 6 str, 3 vit, Damage +1
					put("gimli", "1_13");
					// Gimli's Battle Axe: Damage +1 weapon
					put("axe", "1_14");
					put("lurtz", "15_162");
					put("sword", "2_43");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeaponDamageLossStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_4
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: Your weapons lose all damage bonuses.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_4", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(4, card.getBlueprint().getIntensity());
	}

	@Test
	public void YourWeaponsDamageIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_4: "Your weapons lose all damage bonuses."
		// A weapon with Damage +1 should lose it when the modifier is active.

		var scn = GetScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");

		scn.MoveCardsToSupportArea(freepsMod);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var lurtz = scn.GetShadowCard("lurtz");
		var sword = scn.GetShadowCard("sword");

		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, sword);

		scn.StartGame();

		// Gimli has innate Damage +1, axe grants Damage +1, so normally Damage +2.
		// With the modifier, the axe's damage bonus should be cancelled.
		// Gimli should still have his innate Damage +1, only weapon bonuses are removed.
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		//Lurtz unaffected
		assertEquals(2, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
	}

	@Test
	public void OpponentWeaponsDamageIsNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_4: "Your weapons lose all damage bonuses."
		// The opponent's weapons should NOT be affected by this modifier.
		// When Shadow has this modifier, Freeps weapons should be unaffected.

		var scn = GetScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");

		scn.MoveCardsToSupportArea(shadowMod);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var lurtz = scn.GetShadowCard("lurtz");
		var sword = scn.GetShadowCard("sword");

		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, sword);

		scn.StartGame();

		// Shadow's modifier should not affect Freeps weapons
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		//Lurtz however is now affected
		assertEquals(1, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
	}
}
