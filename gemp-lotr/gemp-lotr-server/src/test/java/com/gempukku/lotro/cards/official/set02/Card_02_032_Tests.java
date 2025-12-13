package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brand", "2_32");
					put("brand2", "2_32");
					put("brand3", "2_32");
					put("aragorn", "1_89");
					put("sword", "1_112");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlamingBrandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Flaming Brand
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be a Man.
		 * 		This weapon may be borne in addition to 1 other hand weapon.
		 * 		Bearer is strength +2 and <b>damage +1</b> while skirmishing a Nazgûl.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brand");

		assertEquals("Flaming Brand", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void FlamingBrandCanBePlayedOnManAlreadyBearingHandWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(brand, brand2);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(brand));
		scn.FreepsPlayCard(brand);
		assertEquals(aragorn, sword.getAttachedTo());
		assertEquals(aragorn, brand.getAttachedTo());

		assertFalse(scn.FreepsPlayAvailable(brand2));
	}

	@Test
	public void FlamingBrandCanBePlayedOnManAlreadyBearingAnotherBrand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var brand3 = scn.GetFreepsCard("brand3");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(brand, brand2, brand3);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, brand);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(brand2));
		scn.FreepsPlayCard(brand2);
		assertEquals(aragorn, brand.getAttachedTo());
		assertEquals(aragorn, brand2.getAttachedTo());

		assertFalse(scn.FreepsPlayAvailable(brand3));
	}

	@Test
	public void RegularHandWeaponCanbePlayedonmanAlreadyBearingFlamingBrand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(sword, brand2);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, brand);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertEquals(aragorn, sword.getAttachedTo());
		assertEquals(aragorn, brand.getAttachedTo());

		assertFalse(scn.FreepsPlayAvailable(brand2));
	}


}
