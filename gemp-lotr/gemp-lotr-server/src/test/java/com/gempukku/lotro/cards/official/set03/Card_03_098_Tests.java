package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swordsman", "3_98");
					put("swordsman2", "3_98");

					// Orc Scimitar: [sauron] hand weapon, cost 1, strength +2, bearer must be a [sauron] Orc; a skirmish
					// ability used only on purpose
					put("scimitar", "1_269");
					// Orkish Bow (Hobbit set): [sauron] RANGED weapon, strength +1, bearer must be a [sauron] Orc; makes
					// bearer an archer, and an optional trigger on its own play that a cheat never sets off
					put("bow", "32_41");
					// Gothmog's Warg: unique [sauron] mount, strength +1, bearer must be a [sauron] Orc; +1 more for each
					// site its owner controls (none here)
					put("warg", "10_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcSwordsmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orc Swordsman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: While this minion bears a weapon, he is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swordsman");

		assertEquals("Orc Swordsman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrength2MoreOnlyOnceHeBearsAWeaponHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordsman = scn.GetShadowCard("swordsman");
		var swordsman2 = scn.GetShadowCard("swordsman2");
		var scimitar = scn.GetShadowCard("scimitar");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman, swordsman2);
		scn.AttachCardsTo(swordsman, warg);

		// A mount is no weapon: 9 + 1 from the warg, and nothing for bearing it.
		assertAttachedTo(warg, swordsman);
		assertEquals(10, scn.GetStrength(swordsman));
		assertEquals(9, scn.GetStrength(swordsman2));
		assertInHand(scimitar);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(scimitar);
		assertTrue(scn.ShadowHasCardChoiceAvailable(swordsman, swordsman2));
		scn.ShadowChooseCard(swordsman);
		assertAttachedTo(scimitar, swordsman);

		// 9 + 1 from the warg + 2 from the scimitar + 2 for bearing a weapon
		assertEquals(14, scn.GetStrength(swordsman));
		// The other copy bears nothing and is as he was: the bonus is the bearer's own.
		assertEquals(9, scn.GetStrength(swordsman2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARangedWeaponMakesHimStrength2MoreAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordsman = scn.GetShadowCard("swordsman");
		var bow = scn.GetShadowCard("bow");

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman);
		assertEquals(9, scn.GetStrength(swordsman));

		scn.AttachCardsTo(swordsman, bow);
		assertAttachedTo(bow, swordsman);
		// 9 + 1 from the bow + 2 for bearing a weapon
		assertEquals(12, scn.GetStrength(swordsman));
	}
}
