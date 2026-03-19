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
	private final HashMap<String, String> cards = new HashMap<>()
	{{
		// Gimli, Dwarf of Erebor: 6 str, 3 vit, Damage +1
		put("gimli", "1_13");
		// Gimli's Battle Axe: Damage +1 weapon
		put("axe", "1_14");
		put("lurtz", "15_162");
		put("sword", "2_43");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_4", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_4"
		);
	}

	@Test
	public void WeaponDamageLossStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_4
		 * Type: MetaSite
		 * Game Text: Your weapons lose all damage bonuses.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_4", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void YourWeaponsDamageIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_4: "Your weapons lose all damage bonuses."
		// FP's weapons lose damage, Shadow's weapons unaffected.

		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");

		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var lurtz = scn.GetShadowCard("lurtz");
		var sword = scn.GetShadowCard("sword");

		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, sword);

		scn.StartGame();

		// Gimli has innate Damage +1, axe grants Damage +1, so normally Damage +2.
		// With the modifier, the axe's damage bonus should be cancelled.
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		//Lurtz unaffected
		assertEquals(2, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
	}

	@Test
	public void OpponentWeaponsDamageIsNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_4: "Your weapons lose all damage bonuses."
		// When Shadow has this modifier, Freeps weapons should be unaffected.

		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");

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
